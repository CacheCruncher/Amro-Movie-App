package com.jawahir.amoro.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.jawahir.amoro.R
import com.jawahir.amoro.domain.model.Genre
import com.jawahir.amoro.domain.model.Movie
import com.jawahir.amoro.ui.theme.AMOROTheme
import com.jawahir.amoro.ui.theme.Dimens
import com.jawahir.amoro.util.posterUrl
import com.valentinilk.shimmer.shimmer

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpacingMedium)
        ) {
            var isImageLoading by remember { mutableStateOf(true) }

            AsyncImage(
                model = movie.posterUrl(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = Dimens.PosterWidth, height = Dimens.PosterHeight)
                    .clip(RoundedCornerShape(Dimens.PosterRadius))
                    .then(if (isImageLoading) Modifier.shimmer() else Modifier),
                onSuccess = { isImageLoading = false },
                onError = { isImageLoading = false },
                placeholder = ColorPainter(MaterialTheme.colorScheme.onSurfaceVariant),
                error = painterResource(R.drawable.ic_broken_image)
            )

            Spacer(modifier = Modifier.width(Dimens.SpacingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingXSmall))
                Text(
                    text = movie.releaseDate.take(4),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingXSmall))
                Text(
                    text = movie.genres.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingXSmall))
                Text(
                    text = "★ ${"%.1f".format(movie.voteAverage)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieCardPreview() {
    val sampleMovie = Movie(
        id = 1,
        title = "The Dark Knight",
        posterPath = "/dark_night.jpg",
        genres = listOf(Genre(1, "Action"), Genre(2, "Crime")),
        popularity = 85.0,
        releaseDate = "2008-07-18",
        voteAverage = 9.0
    )

    AMOROTheme() {
        MovieCard(
            movie = sampleMovie,
            onClick = { /* Handle Click */ },
            modifier = Modifier.padding(Dimens.SpacingMedium)
        )
    }
}