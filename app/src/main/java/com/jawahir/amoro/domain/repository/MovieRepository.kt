package com.jawahir.amoro.domain.repository

import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.model.MovieDetail
import com.jawahir.amoro.domain.result.NetworkResult
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    /**
     * Retrieves trending movies with progressive loading across multiple pages.
     *
     * **Why Flow?**
     * To avoid waiting for the full 100-movie set. This emits after each page fetch
     * so the UI can render partial results immediately while loading continues.
     *
     * **Loading Sequence:**
     * 1. **Genre Fetch:** Must complete first to resolve genre names for all movies.
     * 2. **Page 1:** Emits the first 20 movies as soon as they are mapped.
     * 3. **Subsequent Pages:** Sequentially fetches and emits growing lists (40, 60, 80, 100).
     *
     * **Tradeoff:**
     * First emission is delayed until the genre fetch finishes. Currently, names are
     * resolved during the fetch phase rather than at the display phase.
     *
     * **Future Optimization:**
     * Parallelize the initial Genre and Page 1 fetches to reduce the time to first emission.
     *
     * @return A [Flow] emitting cumulative [NetworkResult] after each page load.
     */
    fun getTrendingMovies(): Flow<NetworkResult<List<Movie>>>
    suspend fun getMovieDetail(movieId:Int): NetworkResult<MovieDetail>
}