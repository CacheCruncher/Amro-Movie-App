package com.jawahir.amoro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jawahir.amoro.data.local.entity.GenreEntity
import com.jawahir.amoro.data.local.entity.MovieEntity
import com.jawahir.amoro.data.local.entity.MovieFetchMeta
import com.jawahir.amoro.data.local.entity.MovieGenreCrossRef
import com.jawahir.amoro.data.local.entity.MovieWithGenres
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieGenreCrossRefs(crossRefs: List<MovieGenreCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFetchTime(meta: MovieFetchMeta)

    @Query("SELECT fetchedAt FROM  movie_fetch_meta WHERE id = 0 ")
    suspend fun getLastFetchTime():Long?

    // Single transaction to save movies + their genre links
    @Transaction
    suspend fun insertMoviesWithGenres(movies: List<MovieEntity>, crossRefs: List<MovieGenreCrossRef>) {
        insertMovies(movies)
        insertMovieGenreCrossRefs(crossRefs)
    }

    // Returns Flow so UI observes live updates from DB
    @Transaction
    @Query("SELECT * FROM movie_table")
    fun getMoviesWithGenres(): Flow<List<MovieWithGenres>>

    @Transaction
    @Query("SELECT * FROM movie_table WHERE id = :movieId")
    suspend fun getMovieWithGenres(movieId: Int): MovieWithGenres?

    @Query("SELECT * FROM genre_table")
    suspend fun getGenres(): List<GenreEntity>?

    @Query("DELETE FROM movie_table")
    suspend fun clearMovies()

    @Query("DELETE FROM movie_genre_cross_ref")
    suspend fun clearCrossRefs()
}
