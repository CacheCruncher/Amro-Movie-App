package com.jawahir.amoro.data.mapper

import com.jawahir.amoro.data.local.entity.GenreEntity
import com.jawahir.amoro.data.local.entity.MovieEntity
import com.jawahir.amoro.data.local.entity.MovieGenreMap
import com.jawahir.amoro.data.local.entity.MovieWithGenres
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.Movie

// Entity → Domain
fun GenreEntity.toDomain() = Genre(id = id, name = name)

fun MovieWithGenres.toDomain() = Movie(
    id = movie.id,
    title = movie.title,
    posterPath = movie.posterPath,
    genres = genres.map { it.toDomain() },
    popularity = movie.popularity,
    releaseDate = movie.releaseDate,
    voteAverage = movie.voteAverage
)

// Domain → Entity
fun Genre.toEntity() = GenreEntity(id = id, name = name)

fun Movie.toEntity() = MovieEntity(
    id = id,
    title = title,
    posterPath = posterPath,
    popularity = popularity,
    releaseDate = releaseDate,
    voteAverage = voteAverage
)

fun Movie.toCrossRefs() = genres.map { genre ->
    MovieGenreMap(movieId = id, genreId = genre.id)
}