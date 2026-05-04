package com.jawahir.amoro.data.repository

import com.jawahir.amoro.data.mapper.toDomainOrNull
import com.jawahir.amoro.data.remote.api.TmdbApiService
import com.jawahir.amoro.data.remote.api.safeApiCall
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.model.MovieDetail
import com.jawahir.amoro.domain.repository.MovieRepository
import com.jawahir.amoro.domain.result.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    private val apiService: TmdbApiService
) : MovieRepository {

    companion object{
        private const val MAX_MOVIE_COUNT = 100 // as per the assignment
    }

    // Cache stored as a Map for O(1) lookup speed when mapping movies.
    private var cachedGenreMap: Map<Int, String>? = null

    override fun getTrendingMovies(): Flow<NetworkResult<List<Movie>>> = flow {
        // Step 1: Ensure genres are available
        val genreMap = cachedGenreMap ?: run {
            val response = safeApiCall { apiService.getGenres() }
            when (response) {
                is NetworkResult.Success -> {
                    val map = response.data.genres
                        ?.mapNotNull { dto -> dto.toDomainOrNull()?.let { it.id to it.name } }
                        ?.toMap()
                        ?: emptyMap()
                    cachedGenreMap = map
                    if (map.isEmpty()) {
                        emit(NetworkResult.NetworkError(Exception("Failed to load genres")))
                        return@flow
                    }
                    map
                }

                is NetworkResult.HttpError, is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    emit(response)
                    return@flow
                }
            }
        }

        // step 2: Progressive Pagination.Fetch pages until we have 100 movies
        val movies = linkedSetOf<Movie>()
        var page = 1

        while (movies.size < MAX_MOVIE_COUNT) {
            val response = safeApiCall { apiService.getTrendingMovies(page = page) }
            when (response) {
                is NetworkResult.Success -> {
                    val newMovies = response.data.movies.orEmpty().mapNotNull { movieDto ->
                        movieDto.toDomainOrNull(genreMap)
                    }

                    movies.addAll(newMovies)
                    emit(NetworkResult.Success(movies.take(MAX_MOVIE_COUNT)))

                    val totalPages = response.data.totalPages ?: 0
                    if (page >= totalPages) break
                    page++
                }

                is NetworkResult.HttpError, is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    // break alone ends flow silently — user sees partial list with no
                    // explanation. Emitting lets ViewModel show a snackbar notification.
                    // WHY NOT retry? Risk of infinite loop if network stays down.
                    emit(response)
                    break
                }
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