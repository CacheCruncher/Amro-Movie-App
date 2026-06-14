package com.jawahir.amoro.data.mapper

import com.jawahir.amoro.data.local.entity.GenreEntity
import com.jawahir.amoro.data.local.entity.MovieDetailEntity
import com.jawahir.amoro.data.local.entity.MovieDetailWithGenres
import com.jawahir.amoro.data.local.entity.MovieEntity
import com.jawahir.amoro.data.local.entity.MovieGenreMap
import com.jawahir.amoro.data.local.entity.MovieWithGenres
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.model.MovieDetail

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

fun MovieDetailWithGenres.toDomain() = MovieDetail(
    id = movieDetail.id,
    title = movieDetail.title,
    tagline = movieDetail.tagline,
    overview = movieDetail.overview,
    posterPath = movieDetail.posterPath,
    backdropPath = movieDetail.backdropPath,
    genres = genres.map { it.toDomain() },
    releaseDate = movieDetail.releaseDate,
    runtime = movieDetail.runtime,
    status = movieDetail.status,
    voteAverage = movieDetail.voteAverage,
    voteCount = movieDetail.voteCount,
    budget = movieDetail.budget,
    revenue = movieDetail.revenue,
    imdbId = movieDetail.imdbId,
    popularity = movieDetail.popularity,
)

fun MovieDetail.toEntity() = MovieDetailEntity(
    id = id,
    title = title,
    tagline = tagline,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtime = runtime,
    status = status,
    voteAverage = voteAverage,
    voteCount = voteCount,
    budget = budget,
    revenue = revenue,
    imdbId = imdbId,
    popularity = popularity
)