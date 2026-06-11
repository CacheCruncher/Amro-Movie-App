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

@Entity(tableName = "movie_fetch_meta")
data class MovieFetchMeta(
    @PrimaryKey val id:Int = 0,
    val fetchedAt: Long
)
@Entity(
    tableName = "movie_genre_cross_ref",
    primaryKeys = ["movieId", "genreId"],
    indices = [Index("genreId")] // index on FK side for fast JOIN
)
data class MovieGenreCrossRef(
    val movieId: Int,
    val genreId: Int
)
