package com.jawahir.amoro.ui.trending

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jawahir.amoro.R
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.ui.component.DropDownChip
import com.jawahir.amoro.ui.component.DropDownItem
import com.jawahir.amoro.ui.component.ErrorView
import com.jawahir.amoro.ui.component.MovieList
import com.jawahir.amoro.ui.theme.Dimens

@Composable
fun TrendingScreen(
    onMovieClick: (movieId: Int) -> Unit,
    viewModel: TrendingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterSort by viewModel.filterSort.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TrendingEffect.ShowSnackbar -> {
                    val message = effect.arg?.let {
                        resources.getString(effect.messageRes, it)
                    } ?: resources.getString(effect.messageRes)

                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    TrendingContent(
        uiState = uiState,
        filterSort = filterSort,
        onMovieClick = onMovieClick,
        onEvent = viewModel::handleEvent,
        snackbarHostState = snackbarHostState
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingContent(
    uiState: TrendingUiState,
    filterSort: FilterSortState,
    onMovieClick: (movieId: Int) -> Unit,
    onEvent: (TrendingEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {

    val ascending = stringResource(R.string.sort_ascending)
    val descending = stringResource(R.string.sort_descending)
    val allLabel = stringResource(R.string.genre_all)
    val checkmark = stringResource(R.string.checkmark_prefix)


    // Resolve each label — these are stable strings that rarely change
    val popularityLabel = stringResource(SortOption.POPULARITY.labelRes)
    val titleLabel = stringResource(SortOption.TITLE.labelRes)
    val releaseDateLabel = stringResource(SortOption.RELEASE_DATE.labelRes)

    val resolvedSortLabels = mapOf(
        SortOption.POPULARITY to popularityLabel,
        SortOption.TITLE to titleLabel,
        SortOption.RELEASE_DATE to releaseDateLabel,
    )


    /**
     * - Avoids rebuilding dropdown list on every recomposition
     * - Recomputes only when key change
     */
    val sortItems = remember(filterSort, resolvedSortLabels) {
        buildSortItems(filterSort, ascending, descending, resolvedSortLabels,checkmark, onEvent)
    }

    val genreItems = remember(uiState, filterSort) {
        if (uiState is TrendingUiState.Success) {
            buildGenreItems(uiState.genres, filterSort, allLabel, checkmark, onEvent)
        } else emptyList()
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trending_title)) },
                actions = {
                    if (uiState is TrendingUiState.Success) {
                        val directionLabel = if (filterSort.sortAscending) ascending else descending
                        val sortLabel = stringResource(filterSort.sortOption.labelRes)

                        DropDownChip(
                            label = "$directionLabel $sortLabel",
                            isActive = false,
                            items = sortItems,
                            modifier = Modifier.padding(end = Dimens.SpacingSmall)
                        )

                        DropDownChip(
                            label = filterSort.selectedGenre?.name
                                ?: stringResource(R.string.genre_label),
                            isActive = filterSort.selectedGenre != null,
                            items = genreItems,
                            modifier = Modifier.padding(end = Dimens.SpacingSmall)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {
                TrendingUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is TrendingUiState.Error -> {
                    ErrorView(
                        message = if (uiState.arg != null) stringResource(
                            uiState.messageRes,
                            uiState.arg
                        ) else stringResource(uiState.messageRes),
                        onRetry = { onEvent(TrendingEvent.LoadMovies) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is TrendingUiState.Success -> {
                    if (uiState.movies.isEmpty()) {
                        EmptyState()
                    } else {
                        MovieList(
                            movies = uiState.movies,
                            isLoadingMore = uiState.isLoadingMore,
                            onMovieClick = onMovieClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_movies_found),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun buildSortItems(
    filterSort: FilterSortState,
    ascending: String,
    descending: String,
    sortOptionLabels: Map<SortOption, String>,
    checkmark: String,
    onEvent: (TrendingEvent) -> Unit
): List<DropDownItem> {
    return buildList {
        add(
            DropDownItem(
                label = if (filterSort.sortAscending) ascending else descending,
                onSelected = { onEvent(TrendingEvent.ToggleSortDirection) }
            )
        )

        SortOption.entries.forEach { option ->
            val label = sortOptionLabels[option] ?: option.name
            add(DropDownItem(
                label = if (filterSort.sortOption == option) "$checkmark $label" else label,
                onSelected = { onEvent(TrendingEvent.SelectSort(option)) },
            ))
        }
    }
}

private fun buildGenreItems(
    genres: List<Genre>,
    filterSort: FilterSortState,
    allLabel: String,
    checkmark: String,
    onEvent: (TrendingEvent) -> Unit
): List<DropDownItem> {
    return buildList {
        add(
            DropDownItem(
                label = if (filterSort.selectedGenre == null) "$checkmark  $allLabel" else allLabel,
                onSelected = { onEvent(TrendingEvent.SelectGenre(null)) }
            )
        )

        genres.forEach { genre ->
            add(
                DropDownItem(
                    label = if (filterSort.selectedGenre?.id == genre.id) "$checkmark  ${genre.name}"
                    else genre.name,
                    onSelected = { onEvent(TrendingEvent.SelectGenre(genre)) }
                )
            )
        }
    }
}