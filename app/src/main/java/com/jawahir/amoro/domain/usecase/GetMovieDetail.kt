package com.jawahir.amoro.domain.usecase

import com.jawahir.amoro.domain.model.MovieDetail
import com.jawahir.amoro.domain.repository.MovieRepository
import com.jawahir.amoro.domain.result.NetworkResult
import jakarta.inject.Inject
/**
 * Use case: fetch full detail for a single movie by ID.
 */
class GetMovieDetail @Inject constructor(private val movieRepository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): NetworkResult<MovieDetail> =
        movieRepository.getMovieDetail(movieId)
}