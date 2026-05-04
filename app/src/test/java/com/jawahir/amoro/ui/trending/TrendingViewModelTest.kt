package com.jawahir.amoro.ui.trending

import app.cash.turbine.test
import com.jawahir.amoro.R
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.result.NetworkResult
import com.jawahir.amoro.domain.usecase.GetTrendingMovies
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Tests for TrendingViewModel state transitions.
 *
 * StandardTestDispatcher gives us full control over coroutine execution.
 * It does not execute coroutines automatically — we advance time manually
 * with runTest's built-in scheduler. This makes tests deterministic.
 *
 * Turbine:
 * StateFlow emissions happen asynchronously. Turbine's .test {} block
 * collects all emissions in order and provides awaitItem(), skipItems(),
 * and awaitComplete()
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrendingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getTrendingMovies = mockk<GetTrendingMovies>()

    private fun createViewModel() = TrendingViewModel(getTrendingMovies, testDispatcher)

    private val action = Genre(id = 28, name = "Action")
    private val comedy = Genre(id = 35, name = "Comedy")

    private fun movie(id: Int, genres: List<Genre> = listOf(action)) = Movie(
        id = id,
        title = "Movie $id",
        posterPath = "",
        genres = genres,
        popularity = id.toDouble() * 10,
        releaseDate = "2026-0$id-01",
        voteAverage = 7.0,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() = runTest {
        every { getTrendingMovies() } returns flowOf()
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is TrendingUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Success states ───────────────────────────────────────────────────────

    @Test
    fun `successful load transitions to Success with movies`() = runTest {
        val movies = listOf(movie(1), movie(2), movie(3))
        every { getTrendingMovies() } returns flowOf(NetworkResult.Success(movies))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is TrendingUiState.Loading)
            val success = awaitItem() as TrendingUiState.Success
            assertEquals(3, success.movies.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful load extracts unique genres from movies`() = runTest {
        val movies = listOf(
            movie(1, genres = listOf(action, comedy)),
            movie(2, genres = listOf(action)),
            movie(3, genres = listOf(comedy)),
        )
        every { getTrendingMovies() } returns flowOf(NetworkResult.Success(movies))

        val viewModel = createViewModel()


        viewModel.uiState.test {
            //testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // Loading
            val success = awaitItem() as TrendingUiState.Success
            // Action and Comedy — deduplicated
            assertEquals(2, success.genres.size)
            assertTrue(success.genres.any { it.id == action.id })
            assertTrue(success.genres.any { it.id == comedy.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `genres are sorted alphabetically`() = runTest {
        val movies = listOf(
            movie(1, genres = listOf(comedy, action)), // Comedy before Action in input
        )
        every { getTrendingMovies() } returns flowOf(NetworkResult.Success(movies))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as TrendingUiState.Success
            // Alphabetical: Action before Comedy
            assertEquals("Action", success.genres.first().name)
            assertEquals("Comedy", success.genres.last().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isLoadingMore is false after completion`() = runTest {
        val movies = listOf(movie(1))
        every { getTrendingMovies() } returns flowOf(NetworkResult.Success(movies))

        val viewModel = createViewModel()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as TrendingUiState.Success

        assertFalse(state.isLoadingMore)
    }

    // ─── Error states ─────────────────────────────────────────────────────────

    @Test
    fun `HTTP error on first load transitions to Error state`() = runTest {
        every { getTrendingMovies() } returns flowOf(NetworkResult.HttpError(404, "Not found"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val error = awaitItem() as TrendingUiState.Error
            assertEquals(R.string.error_server, error.messageRes)
            assertEquals(404, error.arg)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network error on first load transitions to Error state`() = runTest {
        every { getTrendingMovies() } returns flowOf(NetworkResult.NetworkError(IOException("No internet")))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val error = awaitItem() as TrendingUiState.Error
            assertEquals(R.string.error_network, error.messageRes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `HTTP error after partial load emits snackbar effect not full screen error`() = runTest {
        val movies = listOf(movie(1), movie(2))
        every { getTrendingMovies() } returns flowOf(
            NetworkResult.Success(movies),
            NetworkResult.HttpError(503, "Service unavailable"),
        )

        val viewModel = createViewModel()



        viewModel.effects.test {
            // Advance past initial load
            testDispatcher.scheduler.advanceUntilIdle()
            // Trigger collection — effect already emitted
            val effect = awaitItem() as TrendingEffect.ShowSnackbar
            assertEquals(R.string.error_server, effect.messageRes)
            cancelAndIgnoreRemainingEvents()
        }

        // uiState should still be Success — not replaced by error
        assertTrue(viewModel.uiState.value is TrendingUiState.Success)
    }

    // ─── Filter events ────────────────────────────────────────────────────────

    @Test
    fun `SelectGenre event updates filterSort selectedGenre`() = runTest {
        every { getTrendingMovies() } returns flowOf()

        val viewModel = createViewModel()

        viewModel.filterSort.test {
            awaitItem() // initial FilterSortState

            viewModel.handleEvent(TrendingEvent.SelectGenre(action))

            val updated = awaitItem()
            assertEquals(action, updated.selectedGenre)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectGenre with null clears the genre filter`() = runTest {
        every { getTrendingMovies() } returns flowOf()

        val viewModel = createViewModel()
        viewModel.handleEvent(TrendingEvent.SelectGenre(action))

        viewModel.filterSort.test {
            awaitItem() // current state with action selected

            viewModel.handleEvent(TrendingEvent.SelectGenre(null))

            val updated = awaitItem()
            assertEquals(null, updated.selectedGenre)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Sort events ──────────────────────────────────────────────────────────

    @Test
    fun `SelectSort event updates sortOption`() = runTest {
        every { getTrendingMovies() } returns flowOf()

        val viewModel = createViewModel()

        viewModel.filterSort.test {
            awaitItem() // initial

            viewModel.handleEvent(TrendingEvent.SelectSort(SortOption.TITLE))

            val updated = awaitItem()
            assertEquals(SortOption.TITLE, updated.sortOption)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleSortDirection flips sortAscending`() = runTest {
        every { getTrendingMovies() } returns flowOf()

        val viewModel = createViewModel()

        viewModel.filterSort.test {
            val initial = awaitItem()
            assertFalse(initial.sortAscending) // default is descending

            viewModel.handleEvent(TrendingEvent.ToggleSortDirection)
            assertTrue(awaitItem().sortAscending)

            viewModel.handleEvent(TrendingEvent.ToggleSortDirection)
            assertFalse(awaitItem().sortAscending)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filterSort survives a movie reload`() = runTest {
        every { getTrendingMovies() } returns flowOf(NetworkResult.Success(listOf(movie(1))))

        val viewModel = createViewModel()
        viewModel.handleEvent(TrendingEvent.SelectGenre(action))
        testDispatcher.scheduler.advanceUntilIdle()

        // Trigger reload
        every { getTrendingMovies() } returns flowOf(
            NetworkResult.Success(
                listOf(
                    movie(1),
                    movie(2)
                )
            )
        )
        viewModel.handleEvent(TrendingEvent.LoadMovies)
        testDispatcher.scheduler.advanceUntilIdle()

        // Genre selection preserved after reload
        assertEquals(action, viewModel.filterSort.value.selectedGenre)
    }
}