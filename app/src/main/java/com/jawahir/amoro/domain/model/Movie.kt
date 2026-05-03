package com.jawahir.amoro.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String,
    val genres: List<Genre>,
    val popularity: Double,
    val releaseDate: String,
    val voteAverage: Double,
)
