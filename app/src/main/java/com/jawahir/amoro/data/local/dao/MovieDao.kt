package com.jawahir.amoro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jawahir.amoro.data.local.entity.GenreEntity
import com.jawahir.amoro.data.local.entity.MovieDetailEntity
import com.jawahir.amoro.data.local.entity.MovieDetailGenreMap
import com.jawahir.amoro.data.local.entity.MovieDetailWithGenres
import com.jawahir.amoro.data.local.entity.MovieEntity
import com.jawahir.amoro.data.local.entity.MovieFetchMeta
import com.jawahir.amoro.data.local.entity.MovieGenreMap
import com.jawahir.amoro.data.local.entity.MovieWithGenres
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieGenreMap(crossRefs: List<MovieGenreMap>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieDetail(movieDetail: MovieDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieDetailGenreMap(map: List<MovieDetailGenreMap>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFetchTime(meta: MovieFetchMeta)

    @Query("SELECT fetchedAt FROM  movie_fetch_meta WHERE id = 0 ")
    suspend fun getLastFetchTime():Long?

    // Returns Flow so UI observes live updates from DB
    @Transaction
    @Query("SELECT * FROM movie_table")
    fun getMoviesWithGenres(): Flow<List<MovieWithGenres>>

    @Query("SELECT * FROM genre_table")
    suspend fun getGenres(): List<GenreEntity>

    @Transaction
    @Query("SELECT * FROM movie_detail_table WHERE id = :movieId")
    suspend fun getMovieDetailWithGenres(movieId:Int): MovieDetailWithGenres?

}
