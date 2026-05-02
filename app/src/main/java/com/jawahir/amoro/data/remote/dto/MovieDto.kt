package com.jawahir.amoro.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieDto(
    val id: Int?,
    val title: String?,
    @SerializedName("poster_path")  val posterPath: String?,
    @SerializedName("genre_ids")    val genreIds: List<Int>?,
    val popularity: Double?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?
)

data class TrendingResponseDto(
    val results: List<MovieDto>?,
    @SerializedName("total_pages") val totalPages: Int?,
)