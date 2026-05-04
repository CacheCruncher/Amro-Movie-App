package com.jawahir.amoro.domain.usecase


import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.repository.MovieRepository
import com.jawahir.amoro.domain.result.NetworkResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for GetTrendingMovies use case.
 */
class GetTrendingMoviesTest {

    private val repository = mockk<MovieRepository>()
    private val useCase = GetTrendingMovies(repository)

    @Test
    fun `invoke - delegates to repository`() = runTest {
        // 1. SETUP: Tell the fake repository what to return
        every { repository.getTrendingMovies() } returns flowOf(NetworkResult.Success(emptyList()))

        // 2. ACTION: Call the Use Case
        useCase()

        // 3. VERIFY: Did the Use Case actually call the repository method?
        verify(exactly = 1) { repository.getTrendingMovies() }
    }

    @Test
    fun `invoke - passes data through without changing it`() = runTest {
        // 1. SETUP: Create two "waves" of data (emissions)
        val wave1 = NetworkResult.Success(listOf(movie(1)))
        val wave2 = NetworkResult.Success(listOf(movie(1), movie(2)))

        every { repository.getTrendingMovies() } returns flowOf(wave1, wave2)

        // 2. ACTION: Collect the flow into a List
        val results = useCase().toList()

        // 3. VERIFY:
        assertEquals(2, results.size) // Did we get 2 waves?
        assertEquals(wave1, results[0]) // Is the 1st wave identical?
        assertEquals(wave2, results[1]) // Is the 2nd wave identical?
    }

    // Helper to create a dummy movie object
    private fun movie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        posterPath = "",
        genres = emptyList(),
        popularity = 0.0,
        releaseDate = "",
        voteAverage = 0.0
    )
}