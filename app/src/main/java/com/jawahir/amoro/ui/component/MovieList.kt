package com.jawahir.amoro.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.ui.theme.Dimens

@Composable
fun MovieList(
    isLoadingMore: Boolean,
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(Dimens.SpacingLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
    ) {
        items(
            items = movies,
            key = { it.id },
            // Tells Compose all items share the same composable structure.
            // Compose reuses composition slots across items instead of
            // creating new ones — reduces allocation and improves scroll
            // performance when new items arrive during progressive loading.
            contentType = { "movie_card" }
        ) { movie ->
            MovieCard(
                movie = movie,
                onClick = remember(movie.id) {
                    { onMovieClick(movie.id) }
                },
            )
        }

        if (isLoadingMore) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.SpacingSmall),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimens.ProgressSize),
                        strokeWidth = Dimens.ProgressStroke
                    )

                }
            }
        }

    }
}