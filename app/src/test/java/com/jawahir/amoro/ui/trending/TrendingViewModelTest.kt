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
import kotlinx.coroutines.launch
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

    private fun createViewModel() = TrendingViewModel(
        getTrendingMovies = getTrendingMovies,
        defaultDispatcher = testDispatcher/*,
        sharingStarted = SharingStarted.Eagerly*/
    )

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

    // Success states

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
    fun `genres are unique and sorted alphabetically`() = runTest {
        val movies = listOf(
            movie(1, genres = listOf(comedy, action)),
            movie(2, genres = listOf(action)),
            movie(3, genres = listOf(comedy)),
        )

        every { getTrendingMovies() } returns flowOf(NetworkResult.Success(movies))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading

            val success = awaitItem() as TrendingUiState.Success

            assertEquals(2, success.genres.size)
            assertEquals("Action", success.genres[0].name)
            assertEquals("Comedy", success.genres[1].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty movie list emits Success with empty genres`() = runTest {
        every { getTrendingMovies() } returns flowOf(
            NetworkResult.Success(emptyList())
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            val success = awaitItem() as TrendingUiState.Success

            assertTrue(success.movies.isEmpty())
            assertTrue(success.genres.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Error states

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
        }
    }

    @Test
    fun `unknown error on first load transitions to Error state`() = runTest {
        every { getTrendingMovies() } returns flowOf(NetworkResult.UnknownError(Throwable()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            val error = awaitItem() as TrendingUiState.Error

            assertEquals(R.string.error_unknown, error.messageRes)
        }
    }

    // Partial failure
    @Test
    fun `error after success  snackbar emitted, Success state preserved`() = runTest {
        every { getTrendingMovies() } returns flowOf(
            NetworkResult.Success(listOf(movie(1))),
            NetworkResult.HttpError(500, ""),
        )

        val viewModel = createViewModel() // no sharingStarted needed

        viewModel.effects.test {
            // Subscribing to uiState activates combine() — WhileSubscribed works
            val uiStates = mutableListOf<TrendingUiState>()
            val uiJob = launch { viewModel.uiState.collect { uiStates.add(it) } }

            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem() as TrendingEffect.ShowSnackbar
            assertEquals(R.string.error_server, effect.messageRes)

            assertTrue(uiStates.last() is TrendingUiState.Success)
            uiJob.cancel()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Filter events

    @Test
    fun `selecting and clearing genre updates filter and affects output`() = runTest {
        val movies = listOf(
            movie(1, genres = listOf(action)),
            movie(2, genres = listOf(comedy)),
        )

        every { getTrendingMovies() } returns flowOf(NetworkResult.Success(movies))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // loading
            // Skip all initial Success emissions
            var state = awaitItem()
            while (state is TrendingUiState.Success && state.isLoadingMore) {
                state = awaitItem()
            }

            // Apply filter
            viewModel.handleEvent(TrendingEvent.SelectGenre(action))
            // Wait for async filter to finish
            testDispatcher.scheduler.advanceUntilIdle()

            val filtered = awaitItem() as TrendingUiState.Success
            assertEquals(1, filtered.movies.size)

            // Clear filter
            viewModel.handleEvent(TrendingEvent.SelectGenre(null))
            testDispatcher.scheduler.advanceUntilIdle()

            val cleared = awaitItem() as TrendingUiState.Success
            assertEquals(2, cleared.movies.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Sort events
    @Test
    fun `toggle sort direction flips ascending flag`() = runTest {
        every { getTrendingMovies() } returns flowOf()

        val viewModel = createViewModel()

        viewModel.filterSort.test {
            val initial = awaitItem()
            assertFalse(initial.sortAscending)

            viewModel.handleEvent(TrendingEvent.ToggleSortDirection)
            assertTrue(awaitItem().sortAscending)
        }
    }

    // State persistence

    @Test
    fun `filter survives reload`() = runTest {
        every { getTrendingMovies() } returns flowOf(NetworkResult.Success(listOf(movie(1))))

        val viewModel = createViewModel()
        viewModel.handleEvent(TrendingEvent.SelectGenre(action))

        testDispatcher.scheduler.advanceUntilIdle()

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

        assertEquals(action, viewModel.filterSort.value.selectedGenre)
    }
}