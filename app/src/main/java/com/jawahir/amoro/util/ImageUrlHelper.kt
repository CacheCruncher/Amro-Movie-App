package com.jawahir.amoro.util

import com.jawahir.amoro.data.remote.api.TmdbApiService.Companion.IMAGE_BASE_URL
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.model.MovieDetail

// Sizes from TMDB /configuration endpoint:
// Poster:   w92, w154, w185, w342, w500, w780, original
// Backdrop: w300, w780, w1280, original
fun Movie.posterUrl(size: String = "w185"): String =
    "$IMAGE_BASE_URL$size$posterPath"

fun MovieDetail.posterUrl(size: String = "w342"): String =
    "$IMAGE_BASE_URL$size$posterPath"

fun MovieDetail.backdropUrl(size: String = "w780"): String =
    "$IMAGE_BASE_URL$size$backdropPath"