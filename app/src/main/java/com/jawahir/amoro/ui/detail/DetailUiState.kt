package com.jawahir.amoro.ui.detail

import androidx.annotation.StringRes
import com.jawahir.amoro.domain.model.MovieDetail

/**
 * UI states for the Movie Detail screen.
 *
 * **States:**
 * - **Loading:** Initial state while fetching movie details.
 * - **Success:** Data loaded successfully. Holds the [movieDetail].
 * - **Error:** Fetch failed.
 */
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val movieDetail: MovieDetail) : DetailUiState
    data class Error(@StringRes val messageRes: Int, val arg: Int? = null) : DetailUiState
}