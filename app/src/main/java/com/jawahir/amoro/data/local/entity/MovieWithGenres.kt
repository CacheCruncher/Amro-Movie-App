package com.jawahir.amoro.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MovieWithGenres(

    // "read the movie columns from the current row"
    @Embedded
    val movie: MovieEntity,

    // "go to genre_table via the bridge table and attach the result as a list"
    @Relation(
        parentColumn = "id",          // start from movie.id
        entityColumn = "id",          // match to genre.id
        associateBy = Junction(      // go through this bridge table
            value = MovieGenreMap::class,
            parentColumn = "movieId", // bridge col pointing to movie
            entityColumn = "genreId"  // bridge col pointing to genre
        )
    )
    val genres: List<GenreEntity>
)

data class MovieDetailWithGenres(
    @Embedded
    val movieDetail: MovieDetailEntity,


    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MovieDetailGenreMap::class,
            parentColumn = "movieDetailId",
            entityColumn = "genreId"
        )
    )
    val genres:List<GenreEntity>
)
/*
*
When this query runs, Room does this internally:

Step 1 — fetch all movies
──────────────────────────
SELECT * FROM movie_table
→ gives you: [Dune(id=1), Oppenheimer(id=2)]


Step 2 — for each movie, find its genre IDs from the bridge table
──────────────────────────────────────────────────────────────────
SELECT genreId FROM movie_genre_cross_ref WHERE movieId = 1
→ gives you: [28, 16]

SELECT genreId FROM movie_genre_cross_ref WHERE movieId = 2
→ gives you: [28, 12]


Step 3 — fetch the actual genre rows using those IDs
─────────────────────────────────────────────────────
SELECT * FROM genre_table WHERE id IN (28, 16)
→ gives you: [Action, Sci-Fi]

SELECT * FROM genre_table WHERE id IN (28, 12)
→ gives you: [Action, Drama]


Step 4 — Room assembles the final result
─────────────────────────────────────────
MovieWithGenres(
    movie  = Dune,
    genres = [Action, Sci-Fi]
)

MovieWithGenres(
    movie  = Oppenheimer,
    genres = [Action, Drama]
)
* */