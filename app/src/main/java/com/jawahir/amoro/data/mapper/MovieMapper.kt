package com.jawahir.amoro.data.mapper

import com.jawahir.amoro.data.remote.dto.GenreDto
import com.jawahir.amoro.data.remote.dto.MovieDetailDto
import com.jawahir.amoro.data.remote.dto.MovieDto
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.model.MovieDetail

fun GenreDto.toDomainOrNull(): Genre? {
    val genreId = id ?: return null
    val genreName = name ?: return null
    return Genre(genreId, genreName)
}

fun MovieDto.toDomainOrNull(genreMap: Map<Int, String>): Movie? {
    val id = id ?: return null
    val title = title ?: return null

    val genres = genreIds?.mapNotNull { id ->
        val name = genreMap[id]
        if (name != null) Genre(id, name) else null
    } ?: emptyList()

    return Movie(
        id = id,
        title = title,
        posterPath = posterPath.orEmpty(),
        genres = genres,
        popularity = popularity ?: 0.0,
        releaseDate = releaseDate.orEmpty(),
        voteAverage = voteAverage ?: 0.0
    )
}

fun MovieDetailDto.toDomainOrNull(): MovieDetail? {
    val id = id ?: return null
    val title = title ?: return null
    return MovieDetail(
        id = id,
        title = title,
        tagline = tagline.orEmpty(),
        overview = overview.orEmpty(),
        posterPath = posterPath.orEmpty(),
        backdropPath = backdropPath.orEmpty(),
        genres =
            genres?.mapNotNull
            { genreDto ->
                genreDto.toDomainOrNull()
            }.orEmpty(),
        releaseDate = releaseDate.orEmpty(),
        runtime = runtime ?: 0,
        status = status.orEmpty(),
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        budget = budget ?: 0,
        revenue = revenue ?: 0,
        imdbId = imdbId.orEmpty(),
        popularity = popularity ?: 0.0,
    )
}