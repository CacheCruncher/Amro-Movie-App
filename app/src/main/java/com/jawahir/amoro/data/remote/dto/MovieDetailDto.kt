package com.jawahir.amoro.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieDetailDto(
    val id: Int?,
    val title: String?,
    val tagline: String?,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    val genres: List<GenreDto>?,
    @SerializedName("release_date") val releaseDate: String?,
    val runtime: Int?,
    val status: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("vote_count") val voteCount: Int?,
    val budget: Long?,
    val revenue: Long?,
    @SerializedName("imdb_id") val imdbId: String?,
    val popularity: Double?
)