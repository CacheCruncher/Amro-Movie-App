package com.jawahir.amoro.data.repository

import android.util.Log
import com.jawahir.amoro.data.local.dao.MovieDao
import com.jawahir.amoro.data.local.entity.GenreEntity
import com.jawahir.amoro.data.local.entity.MovieEntity
import com.jawahir.amoro.data.local.entity.MovieFetchMeta
import com.jawahir.amoro.data.local.entity.MovieGenreCrossRef
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Implementation of [MovieRepository] that handles data fetching from TMDB API.
 *
 * **Design Choice: Singleton**
 * Marked as @Singleton because it manages an in-memory cache ([cachedGenreMap]).
 * This prevents redundant network calls for static genre data across different screens.
 */
@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val apiService: TmdbApiService,
    private val dao: MovieDao
) : MovieRepository {

    companion object {
        private const val MAX_MOVIE_COUNT = 100 // as per the assignment
        private const val CACHE_EXPIRY_MS = 10*1000 // test-10 sec //30*60*1000L // 30 minutes
    }

    // Cache stored as a Map for O(1) lookup speed when mapping movies.
    private var cachedGenreMap: Map<Int, String>? = null

    override fun getTrendingMovies():Flow<NetworkResult<List<Movie>>> = networkBoundResource(
        fetchFromDb = {
            dao.getMoviesWithGenres().map { list -> list.map { it.toDomain() }  }
        },
        shouldFetch = {cachedMovies ->
            if(cachedMovies.isEmpty()) return@networkBoundResource true
            val lastFetch = dao.getLastFetchTime() ?: return@networkBoundResource true
            val isStale = System.currentTimeMillis() - lastFetch >  CACHE_EXPIRY_MS
            isStale
        },
        fetchFromRemote = {
            fetchAndSaveMovies()
        },
        onFetchFailed = {
            Log.d("TAG", "getTrendingMovies: ${it.message}")
        }
    )

    private suspend fun fetchAndSaveMovies() {
        // step 1: clear old data from movie and map tables
        dao.clearMovies()
        dao.clearCrossRefs()

        // step2: fetch genre: db first, network fallback
        val genreMap = cachedGenreMap ?: run {
            val genreData = dao.getGenres()?.associate { it.id to it.name } ?: emptyMap()

            genreData.ifEmpty {
                val response = safeApiCall { apiService.getGenres() }
                when (response) {
                    is NetworkResult.Success -> {
                        val map = response.data.genres
                            ?.mapNotNull { dto -> dto.toDomainOrNull()?.let { it.id to it.name } }
                            ?.toMap() ?: emptyMap()
                        if (map.isNotEmpty())
                            dao.insertGenres(map.map { (id, value) -> GenreEntity(id, value) })
                        map
                    }

                    is NetworkResult.HttpError, is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                        //emit(response)
                        //return@flow
                        throw Exception("fail to load genres")
                    }
                }
            }
        }
        cachedGenreMap = genreMap

        // step 3: paginate, emit after each page so UI progressively updates
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
                        if(movies[movie.id] == null){
                            movies[movie.id] = movie
                            list.add(movie)
                        }
                    }

                    // emit partial result — ViewModel sets isLoadingMore = true here
                    //emit(NetworkResult.Success(movies.values.take(MAX_MOVIE_COUNT)))
                    if(movies.size<=MAX_MOVIE_COUNT){
                        delay(2000)
                        dao.insertMovies(getMovieEntities(list))
                    }else{
                        val prevMoviesSize = (movies.size-list.size)
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
                    //emit(response)
                    break
                }
            }
        }
        //step4: save to DB in on transaction
        // flow completes after this → onCompletion in ViewModel sets isLoadingMore = false
        val moviesToSave = movies.values.take(MAX_MOVIE_COUNT)
        dao.insertMoviesWithGenres(getMovieEntities(moviesToSave), getListMovieGenre(moviesToSave))
        dao.saveFetchTime(MovieFetchMeta(fetchedAt = System.currentTimeMillis()))
    }

    private fun getMovieEntities(moviesToSave: List<Movie>): List<MovieEntity> =
        moviesToSave.map { it.toEntity() }

    private fun getListMovieGenre(movies: List<Movie>): List<MovieGenreCrossRef> {
        return movies.flatMap { movie ->
            val movieId = movie.id
            val genreList = movie.genres
            genreList.map { genre ->
                MovieGenreCrossRef(movieId, genre.id)
            }
        }
    }

    override suspend fun getMovieDetail(movieId: Int): NetworkResult<MovieDetail> {
        val response = safeApiCall { apiService.getMovieDetail(movieId) }
        when (response) {
            is NetworkResult.Success -> {
                val movieDetail =
                    response.data.toDomainOrNull() ?: return NetworkResult.NetworkError(
                        Exception("Failed to parse move details")
                    )
                return NetworkResult.Success(movieDetail)
            }

            is NetworkResult.HttpError, is NetworkResult.NetworkError, is NetworkResult.UnknownError -> return response
        }
    }
}