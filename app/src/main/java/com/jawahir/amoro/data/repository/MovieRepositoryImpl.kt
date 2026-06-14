package com.jawahir.amoro.data.repository

import android.util.Log
import com.jawahir.amoro.data.local.dao.MovieDao
import com.jawahir.amoro.data.local.entity.GenreEntity
import com.jawahir.amoro.data.local.entity.MovieDetailGenreMap
import com.jawahir.amoro.data.local.entity.MovieEntity
import com.jawahir.amoro.data.local.entity.MovieFetchMeta
import com.jawahir.amoro.data.local.entity.MovieGenreMap
import com.jawahir.amoro.data.mapper.toDomain
import com.jawahir.amoro.data.mapper.toDomainOrNull
import com.jawahir.amoro.data.mapper.toEntity
import com.jawahir.amoro.data.remote.api.TmdbApiService
import com.jawahir.amoro.data.remote.api.safeApiCall
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.model.MovieDetail
import com.jawahir.amoro.domain.repository.MovieRepository
import com.jawahir.amoro.domain.result.NetworkResult
import com.jawahir.amoro.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of [MovieRepository] that handles data fetching from TMDB API.
 *
 * **Design Choice: Singleton**
 * Marked as @Singleton because it manages an in-memory cache ([cachedGenreMap]).
 * This prevents redundant network calls for static genre data across different screens.
 */
/*
@Singleton
*/
class MovieRepositoryImpl @Inject constructor(
    private val apiService: TmdbApiService,
    private val dao: MovieDao
) : MovieRepository {

    companion object {
        private const val MAX_MOVIE_COUNT = 100 // as per the assignment
        private const val CACHE_EXPIRY_MS = 10 * 1000 // test-10 sec //30*60*1000L // 30 minutes
    }

    override fun getTrendingMovies(): Flow<NetworkResult<List<Movie>>> = networkBoundResource(
        fetchFromDb = {
            dao.getMoviesWithGenres().map { list -> list.map { it.toDomain() } }
        },
        isEmpty = { movies ->
            movies.isEmpty()
        },
        shouldFetch = {
            val lastFetch = dao.getLastFetchTime() ?: return@networkBoundResource true
            val isStale = System.currentTimeMillis() - lastFetch > CACHE_EXPIRY_MS
            isStale
        },
        fetchFromRemote = {
            fetchAndSaveMovies()
        },
        onFetchFailed = {
            Log.e("Failed", "getTrendingMovies: ${it.message}")
        }
    )

    private suspend fun fetchAndSaveMovies() {
        // step1: fetch genre: db first, network fallback
        var genreMap = dao.getGenres().associate { it.id to it.name }

        if(genreMap.isEmpty()){
            val response = safeApiCall { apiService.getGenres() }
            when (response) {
                is NetworkResult.Success -> {
                    val map = response.data.genres
                        ?.mapNotNull { dto -> dto.toDomainOrNull()?.let { it.id to it.name } }
                        ?.toMap() ?: emptyMap()
                    if (map.isNotEmpty())
                        dao.insertGenres(map.map { (id, value) -> GenreEntity(id, value) })
                    genreMap = map
                }

                is NetworkResult.HttpError, is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    throw Exception("fail to load genres")
                }
            }
        }

        // step 2: paginate, emit after each page so UI progressively updates
        val movies = linkedMapOf<Int, Movie>()
        var page = 1

        while (movies.size < MAX_MOVIE_COUNT) {
            val response = safeApiCall { apiService.getTrendingMovies(page = page) }
            when (response) {
                is NetworkResult.Success -> {
                    val newMovies = response.data.movies.orEmpty().mapNotNull { movieDto ->
                        movieDto.toDomainOrNull(genreMap)
                    }
                    val list = arrayListOf<Movie>()
                    newMovies.forEach { movie ->
                        if (movies[movie.id] == null) {
                            movies[movie.id] = movie
                            list.add(movie)
                        }
                    }

                    if (list.isNotEmpty() && movies.size <= MAX_MOVIE_COUNT) {
                        dao.insertMovies(getMovieEntities(list))
                    } else {
                        val prevMoviesSize = (movies.size - list.size)
                        val leftOverSize = MAX_MOVIE_COUNT - prevMoviesSize
                        val leftMovies = list.take(leftOverSize)
                        dao.insertMovies(getMovieEntities(leftMovies))
                    }
                    val totalPages = response.data.totalPages ?: 0
                    if (page >= totalPages) break
                    page++
                }

                is NetworkResult.HttpError, is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    // break alone ends flow silently — user sees partial list with no
                    // explanation. Emitting lets ViewModel show a snackbar notification.
                    // WHY NOT retry? Risk of infinite loop if network stays down.
                    break
                }
            }
        }
        //step4: save to DB
        val moviesToSave = movies.values.take(MAX_MOVIE_COUNT)
        dao.insertMovieGenreMap(getListMovieGenre(moviesToSave))
        dao.saveFetchTime(MovieFetchMeta(fetchedAt = System.currentTimeMillis()))
    }

    private fun getMovieEntities(moviesToSave: List<Movie>): List<MovieEntity> =
        moviesToSave.map { it.toEntity() }

    private fun getListMovieGenre(movies: List<Movie>): List<MovieGenreMap> {
        return movies.flatMap { movie ->
            val movieId = movie.id
            val genreList = movie.genres
            genreList.map { genre ->
                MovieGenreMap(movieId, genre.id)
            }
        }
    }

    override suspend fun getMovieDetail(movieId: Int): NetworkResult<MovieDetail> {
        // fetch from db, fallback to network
        val dbData = dao.getMovieDetailWithGenres(movieId)?.toDomain()
        return if(dbData != null){
            NetworkResult.Success(dbData)
        } else{
            fetchAndSaveMovieDetail(movieId)
        }
    }

    suspend fun fetchAndSaveMovieDetail(movieId: Int): NetworkResult<MovieDetail> {
        val response = safeApiCall { apiService.getMovieDetail(movieId) }
        when (response) {
            is NetworkResult.Success -> {
                val movieDetail =
                    response.data.toDomainOrNull() ?: return NetworkResult.NetworkError(
                        Exception("Failed to parse move details")
                    )
                dao.insertMovieDetail(movieDetail.toEntity())
                dao.insertMovieDetailGenreMap(getListMovieDetailGenre(movieDetail))
                return NetworkResult.Success(movieDetail)
            }

            is NetworkResult.HttpError, is NetworkResult.NetworkError, is NetworkResult.UnknownError -> return response
        }
    }

    private fun getListMovieDetailGenre(movieDetail: MovieDetail): List<MovieDetailGenreMap> {
        return movieDetail.genres.map {
            MovieDetailGenreMap(movieDetail.id,it.id)
        }
    }
}