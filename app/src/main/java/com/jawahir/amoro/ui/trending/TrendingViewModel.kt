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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val getTrendingMovies: GetTrendingMovies,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Loading)
    private val _filterSort = MutableStateFlow(FilterSortState())
    val filterSort = _filterSort.asStateFlow()
    private val _effects = MutableSharedFlow<TrendingEffect>()
    val effects = _effects.asSharedFlow()

    private var loadJob: Job? = null


    val uiState: StateFlow<TrendingUiState> = combine(
        _loadState,
        _filterSort,
    ) { loadState, filterSort ->
        when (loadState) {
            is LoadState.Loading -> TrendingUiState.Loading
            is LoadState.Error -> TrendingUiState.Error(loadState.messageRes, loadState.arg)
            is LoadState.Success -> TrendingUiState.Success(
                movies = filterSort.applyTo(loadState.movies),
                genres = loadState.genres,
                isLoadingMore = loadState.isLoadingMore,
            )
        }
    }.flowOn(defaultDispatcher).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrendingUiState.Loading,
    )

    init {
        handleEvent(TrendingEvent.LoadMovies)
    }

    fun handleEvent(event: TrendingEvent) {
        when (event) {
            TrendingEvent.LoadMovies -> loadMovies()
            is TrendingEvent.SelectGenre -> _filterSort.update { it.copy(selectedGenre = event.genre) }
            is TrendingEvent.SelectSort -> _filterSort.update { it.copy(sortOption = event.option) }
            TrendingEvent.ToggleSortDirection -> _filterSort.update { it.copy(sortAscending = !it.sortAscending) }
        }
    }

    private fun loadMovies() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {

            getTrendingMovies()
                .onStart {
                    _loadState.value = LoadState.Loading
                }
                .onCompletion {
                    _loadState.update { state ->
                        if (state is LoadState.Success) state.copy(isLoadingMore = false)
                        else state
                    }
                }
                .collectLatest { result ->
                    when (result) {

                        is NetworkResult.Success -> {
                            _loadState.value = LoadState.Success(
                                movies = result.data,
                                genres = result.data.toUniqueGenreList(),
                                isLoadingMore = true
                            )
                        }

                        is NetworkResult.HttpError,
                        is NetworkResult.NetworkError,
                        is NetworkResult.UnknownError -> {

                            val (messageRes, code) = when (result) {
                                is NetworkResult.HttpError -> R.string.error_server to result.code
                                is NetworkResult.NetworkError -> R.string.error_network to null
                                is NetworkResult.UnknownError -> R.string.error_unknown to null
                            }

                            val current = _loadState.value
                            if (current is LoadState.Success && current.movies.isNotEmpty()) {
                                _effects.emit(TrendingEffect.ShowSnackbar(messageRes, code))
                                _loadState.update { state ->
                                    if (state is LoadState.Success) state.copy(isLoadingMore = false) else state
                                }
                            } else {
                                _loadState.value = LoadState.Error(messageRes, code)
                            }
                        }
                    }
                }
        }
    }

    private fun List<Movie>.toUniqueGenreList(): List<Genre> {
        return flatMap { it.genres }
            .distinctBy { it.id }
            .sortedBy { it.name }
    }
}

/**
 *
 * NetworkResult tells us what happened on the network:
 *   Success(data) | HttpError(code) | NetworkError(throwable)
 *
 * But combine() needs to know 3 things to build TrendingUiState:
 *   1. The raw movie list (to re-filter when genre changes)
 *   2. The genre list
 *   3. Whether more pages are loading
 *
 * NetworkResult.Success only carries List<Movie> — no genres, no isLoadingMore.
 * We cannot add those to NetworkResult — it lives in the domain layer and
 * knows nothing about UI concerns like loading spinners or genre chips.
 *
 * LoadState bridges the gap: it holds everything combine() needs,
 * translated from the network result. Private to this file — never exposed.
 */
private sealed interface LoadState {
    data object Loading : LoadState
    data class Success(
        val movies: List<Movie>,
        val genres: List<Genre>,
        val isLoadingMore: Boolean,
    ) : LoadState

    data class Error(
        val messageRes: Int,
        val arg: Int? = null,
    ) : LoadState
}