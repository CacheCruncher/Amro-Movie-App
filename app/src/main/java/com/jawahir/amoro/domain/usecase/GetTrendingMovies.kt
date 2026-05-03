package com.jawahir.amoro.domain.usecase

import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.repository.MovieRepository
import com.jawahir.amoro.domain.result.NetworkResult
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get the trending movies of the week.
 */
class GetTrendingMovies @Inject constructor(private val repository: MovieRepository) {
    operator fun invoke(): Flow<NetworkResult<List<Movie>>> = repository.getTrendingMovies()
}