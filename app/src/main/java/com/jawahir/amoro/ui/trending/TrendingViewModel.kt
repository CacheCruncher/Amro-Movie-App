package com.jawahir.amoro.ui.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jawahir.amoro.R
import com.jawahir.amoro.di.DefaultDispatcher
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.result.NetworkResult
import com.jawahir.amoro.domain.usecase.GetTrendingMovies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val getTrendingMovies: GetTrendingMovies,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    // Single source of truth
    private val _uiState = MutableStateFlow<TrendingUiState>(TrendingUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // User preferences (independent state)
    private val _filterSort = MutableStateFlow(FilterSortState())
    val filterSort = _filterSort.asStateFlow()

    // One-off effects
    private val _effects = MutableSharedFlow<TrendingEffect>()
    val effects = _effects.asSharedFlow()

    private var allMovies: List<Movie> = emptyList()
    private var cachedGenre: List<Genre> = emptyList()

    private var filterJob: Job? = null

    init {
        handleEvent(TrendingEvent.LoadMovies)
    }


    fun handleEvent(event: TrendingEvent) {
        when (event) {
            TrendingEvent.LoadMovies -> loadMovies()
            is TrendingEvent.SelectGenre -> {
                _filterSort.update { it.copy(selectedGenre = event.genre) }
                applyFilterAsync()
            }

            is TrendingEvent.SelectSort -> {
                _filterSort.update { it.copy(sortOption = event.option) }
                applyFilterAsync()
            }

            TrendingEvent.ToggleSortDirection -> {
                _filterSort.update { it.copy(sortAscending = !it.sortAscending) }
                applyFilterAsync()
            }
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _uiState.value = TrendingUiState.Loading

            getTrendingMovies().collectLatest { result ->
                when (result) {

                    is NetworkResult.Success -> {
                        allMovies = result.data
                        cachedGenre = result.data.toUniqueGenreList()

                        applyFilter(isLoading = true)
                    }

                    is NetworkResult.HttpError,
                    is NetworkResult.NetworkError,
                    is NetworkResult.UnknownError -> {

                        val (messageRes, code) = when (result) {
                            is NetworkResult.HttpError -> R.string.error_server to result.code
                            is NetworkResult.NetworkError -> R.string.error_network to null
                            is NetworkResult.UnknownError -> R.string.error_unknown to null
                        }

                        if (allMovies.isNotEmpty()) {
                            _effects.emit(TrendingEffect.ShowSnackbar(messageRes, code))
                            updateLoadingState(false)
                        } else {
                            _uiState.value = TrendingUiState.Error(messageRes, code)
                        }
                    }
                }
            }

            // Flow completed → no more loading
            updateLoadingState(false)
        }
    }

    /**
     * Cancels any in-flight filter job and starts a new one.
     */
    private fun applyFilterAsync() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            applyFilter()
        }
    }

    /**
     * **Note on Suspension:**
     * Being a `suspend` function ensures the caller (e.g., loadMovies) waits for
     * filtering to finish before triggering further state updates. This prevents
     * race conditions where completion logic might finish before the filtered
     * UI state is actually emitted.
     */
    private suspend fun applyFilter(isLoading: Boolean = false) {
        val filtered = withContext(defaultDispatcher) {
            _filterSort.value.applyTo(allMovies)
        }

        _uiState.value = TrendingUiState.Success(
            movies = filtered,
            genres = cachedGenre,
            isLoadingMore = isLoading
        )
    }

    private fun updateLoadingState(isLoading: Boolean) {
        val current = _uiState.value
        if (current is TrendingUiState.Success) {
            _uiState.value = current.copy(isLoadingMore = isLoading)
        }
    }

    private fun List<Movie>.toUniqueGenreList(): List<Genre> {
        return flatMap { it.genres }
            .distinctBy { it.id }
            .sortedBy { it.name }
    }
}