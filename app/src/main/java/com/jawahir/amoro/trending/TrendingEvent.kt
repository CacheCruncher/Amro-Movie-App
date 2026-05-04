package com.jawahir.amoro.trending

import androidx.annotation.StringRes
import com.jawahir.amoro.R
import com.jawahir.amoro.domain.model.Genre

/**
 * User actions that trigger changes in the Trending Screen.
 */
sealed interface TrendingEvent {
    /** Initial action to fetch the pages of movies. */
    data object LoadMovies : TrendingEvent
    /** Triggered when a user picks a genre or clears the filter (null). */
    data class SelectGenre(val genre: Genre?) : TrendingEvent
    /** Changes the sorting criteria (e.g., from Popularity to Title). */
    data class SelectSort(val option: SortOption) : TrendingEvent
    /** Flips the sort order between Ascending and Descending. */
    data object ToggleSortDirection : TrendingEvent
}

/**
 * Available sorting choices for the movie list.
 */
enum class SortOption(@StringRes val labelRes: Int) {
    POPULARITY(R.string.sort_popularity),
    TITLE(R.string.sort_title),
    RELEASE_DATE(R.string.sort_release_date),
}