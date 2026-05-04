package com.jawahir.amoro.ui.trending

import androidx.annotation.StringRes
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.Movie

/**
 * Represents every possible state of the trending screen.
 *
 *  - Loading  : operation in progress, show spinner
 *  - Error    : operation failed with no data, show error + retry
 *  - Success  : operation succeeded, show content
 *
 * **Why is isLoadingMore inside the Success state?**
 *
 * Instead of creating a separate "LoadingMore" state, we keep it inside
 * "Success" so the app doesn't hide the movies already on the screen.
 *
 *  **The Benefit:** The user can keep scrolling and reading the first
 *  set of movies while the next page loads silently in the background.
 */
sealed interface TrendingUiState {
    data object Loading : TrendingUiState
    data class Error(@StringRes val messageRes: Int, val arg: Int? = null) : TrendingUiState
    data class Success(
        val movies: List<Movie>,
        val genres: List<Genre>,
        // true while pages 2-5 are still loading in background
        val isLoadingMore: Boolean = false,
    ) : TrendingUiState
}

/**
 * One-time UI actions (side effects) that do not stick around in the state.
 */
sealed interface TrendingEffect {
    data class ShowSnackbar(@StringRes val messageRes: Int, val arg: Int? = null) : TrendingEffect
}

/**
 * User's filter and sort preferences.
 */
data class FilterSortState(
    val selectedGenre: Genre? = null,
    val sortOption: SortOption = SortOption.POPULARITY,
    val sortAscending: Boolean = false,
) {
    /**
     * Applies filtering and sorting to the provided list.
     */
    fun applyTo(movies: List<Movie>): List<Movie> {
        val genreId = selectedGenre?.id

        // 1. Filter
        val filtered = if (genreId == null) movies else {
            movies.filter { movie -> movie.genres.any { it.id == genreId } }
        }

        // 2. Sort - Create the comparator once
        val comparator = when (sortOption) {
            SortOption.POPULARITY -> compareBy<Movie> { it.popularity }
            SortOption.TITLE -> compareBy { it.title }
            SortOption.RELEASE_DATE -> compareBy { it.releaseDate }
        }

        // 3. Apply direction and sort in one pass
        return if (sortAscending) {
            filtered.sortedWith(comparator)
        } else {
            filtered.sortedWith(comparator.reversed())
        }
    }
}