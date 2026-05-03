package com.jawahir.amoro.domain.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val posterPath: String,
    val backdropPath: String,
    val genres: List<Genre>,
    val releaseDate: String,
    val runtime: Int,
    val status: String,
    val voteAverage: Double,
    val voteCount: Int,
    val budget: Long,
    val revenue: Long,
    val imdbId: String,
    val popularity: Double
)
