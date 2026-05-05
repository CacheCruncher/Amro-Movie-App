package com.jawahir.amoro.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jawahir.amoro.R
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.MovieDetail
import com.jawahir.amoro.domain.result.NetworkResult
import com.jawahir.amoro.domain.usecase.GetMovieDetail
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Tests for DetailViewModel state transitions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getMovieDetail = mockk<GetMovieDetail>()

    private fun createViewModel(movieId: Int = 1): DetailViewModel {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["movieId"] = movieId

        return DetailViewModel(getMovieDetail, savedStateHandle)
    }

    private fun movieDetail(id: Int = 1) = MovieDetail(
        id = id,
        title = "Apex",
        tagline = "Prey or be preyed",
        overview = "A thriller set in the wild.",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        genres = listOf(Genre(28, "Action")),
        releaseDate = "2026-04-24",
        runtime = 98,
        status = "Released",
        voteAverage = 6.5,
        voteCount = 464,
        budget = 15_000_000L,
        revenue = 28_000_000L,
        imdbId = "tt1234567",
        popularity = 72.5,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    //  Success

    @Test
    fun `successful load transitions to Success with correct movie`() = runTest {
        val detail = movieDetail(id = 42)
        coEvery { getMovieDetail(42) } returns NetworkResult.Success(detail)

        val viewModel = createViewModel(42)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // Loading
            val success = awaitItem() as DetailUiState.Success
            assertEquals(42, success.movieDetail.id)
            assertEquals("Apex", success.movieDetail.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Error states

    @Test
    fun `HTTP error transitions to Error with server message and code`() = runTest {
        coEvery { getMovieDetail(any()) } returns NetworkResult.HttpError(404, "Not found")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // Loading
            val error = awaitItem() as DetailUiState.Error
            assertEquals(R.string.error_server, error.messageRes)
            assertEquals(404, error.arg)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network error transitions to Error with network message`() = runTest {
        coEvery { getMovieDetail(any()) } returns NetworkResult.NetworkError(IOException("No internet"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // Loading
            val error = awaitItem() as DetailUiState.Error
            assertEquals(R.string.error_network, error.messageRes)
            assertEquals(null, error.arg)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unknown error transitions to Error with unknown message`() = runTest {
        coEvery { getMovieDetail(any()) } returns
                NetworkResult.UnknownError(Throwable())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // Loading
            val error = awaitItem() as DetailUiState.Error
            assertEquals(R.string.error_unknown, error.messageRes)
            assertEquals(null, error.arg)
        }
    }

    // Retry

    @Test
    fun `LoadDetail event retries and transitions to Success`() = runTest {
        coEvery { getMovieDetail(any()) }
            .returnsMany(
                NetworkResult.NetworkError(IOException("First attempt fails")),
                NetworkResult.Success(movieDetail()),
            )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // Loading
            awaitItem() // Error from first attempt

            viewModel.handleEvent(DetailEvent.LoadDetail)
            testDispatcher.scheduler.advanceUntilIdle()

            awaitItem() // Loading again
            val success = awaitItem() as DetailUiState.Success
            assertEquals("Apex", success.movieDetail.title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}