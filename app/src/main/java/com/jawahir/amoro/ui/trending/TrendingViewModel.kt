package com.jawahir.amoro.ui.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jawahir.amoro.R
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.domain.result.NetworkResult
import com.jawahir.amoro.domain.usecase.GetTrendingMovies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrendingViewModel @Inject constructor(
    val getTrendingMovies: GetTrendingMovies
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

    // Internal raw data (not exposed)
    private var allMovies: List<Movie> = emptyList()

    init {
        handleEvent(TrendingEvent.LoadMovies)
    }


    fun handleEvent(event: TrendingEvent) {
        when (event) {
            TrendingEvent.LoadMovies -> loadMovies()
            is TrendingEvent.SelectGenre -> {
                _filterSort.update { it.copy(selectedGenre = event.genre) }
                updateMovies()
            }

            is TrendingEvent.SelectSort -> {
                _filterSort.update { it.copy(sortOption = event.option) }
                updateMovies()
            }

            TrendingEvent.ToggleSortDirection -> {
                _filterSort.update { it.copy(sortAscending = !it.sortAscending) }
                updateMovies()
            }
        }
    }

    private fun updateMovies() {
        val current = _uiState.value
        if (current is TrendingUiState.Success) {
            _uiState.value = current.copy(
                movies = _filterSort.value.applyTo(allMovies)
            )
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _uiState.value = TrendingUiState.Loading

            getTrendingMovies().collect { result ->
                when (result) {

                    is NetworkResult.Success -> {
                        allMovies = result.data

                        _uiState.value = TrendingUiState.Success(
                            movies = _filterSort.value.applyTo(result.data),
                            genres = result.data.toUniqueGenreList(),
                            isLoadingMore = true // still loading until flow completes
                        )
                    }

                    is NetworkResult.HttpError -> {
                        val messageRes = R.string.error_server
                        if (allMovies.isNotEmpty()) {
                            _effects.emit(TrendingEffect.ShowSnackbar(messageRes, result.code))
                            updateLoadingState(false)
                        } else {
                            _uiState.value = TrendingUiState.Error(messageRes, result.code)
                        }

                    }

                    is NetworkResult.NetworkError -> {
                        val messageRes = R.string.error_network

                        if (allMovies.isNotEmpty()) {
                            _effects.emit(TrendingEffect.ShowSnackbar(messageRes))
                            // Stop loading indicator but keep data
                            updateLoadingState(false)

                        } else {
                            // No data → full error
                            _uiState.value = TrendingUiState.Error(messageRes)
                        }
                    }
                }
            }

            // Flow completed → no more loading
            updateLoadingState(false)
        }
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