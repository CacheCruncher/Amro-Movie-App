package com.jawahir.amoro.data.remote.api

import com.jawahir.amoro.data.remote.dto.GenreResponseDto
import com.jawahir.amoro.data.remote.dto.MovieDetailDto
import com.jawahir.amoro.data.remote.dto.TrendingResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

const val BASE_URL = "https://api.themoviedb.org/3/"
const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

interface TmdbApiService {

    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String = "week",
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<TrendingResponseDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): Response<MovieDetailDto>

    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("language") language: String = "en-US"
    ): Response<GenreResponseDto>

}