package com.jawahir.amoro.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jawahir.amoro.R
import com.jawahir.amoro.domain.result.NetworkResult
import com.jawahir.amoro.domain.usecase.GetMovieDetail
import com.jawahir.amoro.ui.navigation.Detail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    val getMovieDetail: GetMovieDetail,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val args = savedStateHandle.toRoute<Detail>()
    private val movieId = args.movieId

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        handleEvent(DetailEvent.LoadDetail)
    }

    fun handleEvent(event: DetailEvent) {
        when (event) {
            DetailEvent.LoadDetail -> loadDetail()

        }

    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val response = getMovieDetail(movieId)
            when (response) {
                is NetworkResult.Success -> _uiState.value =
                    DetailUiState.Success(response.data)

                is NetworkResult.HttpError -> _uiState.value =
                    DetailUiState.Error(R.string.error_server, response.code)

                is NetworkResult.NetworkError -> _uiState.value =
                    DetailUiState.Error(R.string.error_network)
            }

        }
    }
}