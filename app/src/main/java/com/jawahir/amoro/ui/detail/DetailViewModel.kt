package com.jawahir.amoro.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jawahir.amoro.R
import com.jawahir.amoro.domain.result.NetworkResult
import com.jawahir.amoro.domain.usecase.GetMovieDetail
import com.jawahir.amoro.ui.detail.DetailUiState.Error
import com.jawahir.amoro.ui.detail.DetailUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getMovieDetail: GetMovieDetail,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieIdKey = "movieId"

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun setMovieId(movieId: Int) {
        savedStateHandle[movieIdKey] = movieId
    }

    init {
        observeMovieId()
    }

    /**
     * Observe movieId as a reactive stream.
     *
     * - Single trigger point for loading
     * - Handles process death automatically
     * - Avoids manual calls from multiple places
     */
    private fun observeMovieId() {
        viewModelScope.launch {
            savedStateHandle
                .getStateFlow<Int?>(movieIdKey, null)
                .filterNotNull()              // ignore initial null
                .distinctUntilChanged()      // avoid duplicate calls
                .collect { movieId ->
                    loadDetail(movieId)
                }
        }
    }

    fun handleEvent(event: DetailEvent) {
        when (event) {
            DetailEvent.LoadDetail -> {
                savedStateHandle.get<Int>(movieIdKey)?.let { id ->
                    loadDetail(id)
                }
            }
        }
    }

    private fun loadDetail(movieId: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            when (val response = getMovieDetail(movieId)) {
                is NetworkResult.Success -> _uiState.value = Success(response.data)
                is NetworkResult.HttpError -> _uiState.value = Error(R.string.error_server, response.code)
                is NetworkResult.NetworkError -> _uiState.value = Error(R.string.error_network)
                is NetworkResult.UnknownError -> _uiState.value = Error(R.string.error_unknown)
            }
        }
    }
}