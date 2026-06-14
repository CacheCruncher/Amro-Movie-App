package com.jawahir.amoro.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "genre_table")
data class GenreEntity(
    @PrimaryKey val id: Int,
    val name: String
)


@Entity(tableName = "movie_table")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String,
    val popularity: Double,
    val releaseDate: String,
    val voteAverage: Double
)

@Entity(
    tableName = "movie_genre_map",
    primaryKeys = ["movieId", "genreId"],
    indices = [Index("genreId")] // index on FK side for fast JOIN
)
data class MovieGenreMap(
    val movieId: Int,
    val genreId: Int
)

@Entity(tableName = "movie_fetch_meta")
data class MovieFetchMeta(
    @PrimaryKey val id: Int = 0,
    val fetchedAt: Long
)


@Entity(tableName = "movie_detail_table")
data class MovieDetailEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val posterPath: String,
    val backdropPath: String,
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

@Entity(
    tableName = "movie_detail_genre_map",
    primaryKeys = ["movieDetailId", "genreId"],
    indices = [Index("genreId")] // index on FK side for fast JOIN
)
data class MovieDetailGenreMap(
    val movieDetailId: Int,
    val genreId: Int
)