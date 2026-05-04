package com.jawahir.amoro.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jawahir.amoro.R
import com.jawahir.amoro.domain.model.MovieDetail
import com.jawahir.amoro.ui.component.ErrorView
import com.jawahir.amoro.ui.component.InfoRow
import com.jawahir.amoro.ui.theme.Dimens
import com.jawahir.amoro.util.ExternalUrl.IMDB_TITLE
import com.jawahir.amoro.util.backdropUrl
import com.jawahir.amoro.util.toFormattedCurrency
import com.valentinilk.shimmer.shimmer

@Composable
fun DetailScreen(
    onBackClick: () -> Unit,
    onImdbClick: (url: String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onImdbClick = onImdbClick,
        onEvent = viewModel::handleEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    uiState: DetailUiState,
    onBackClick: () -> Unit,
    onImdbClick: (url: String) -> Unit,
    onEvent: (DetailEvent) -> Unit
) {
    val back = stringResource(R.string.detail_back)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is DetailUiState.Success) Text(uiState.movieDetail.title)
                },
                navigationIcon = {
                    IconButton(onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = back
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                DetailUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                is DetailUiState.Success -> MovieDetailBody(uiState.movieDetail, onImdbClick)
                is DetailUiState.Error -> ErrorView(
                    message = if (uiState.arg != null) {
                        stringResource(uiState.messageRes, uiState.arg)
                    } else {
                        stringResource(uiState.messageRes)
                    },
                    onRetry = { onEvent(DetailEvent.LoadDetail) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

        }

    }
}

@Composable
fun MovieDetailBody(
    movieDetail: MovieDetail,
    onImdbClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        var isImageLoading by remember { mutableStateOf(true) }

        AsyncImage(
            model = movieDetail.backdropUrl(),
            contentDescription = movieDetail.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(Dimens.CardRadius))
                .then(if (isImageLoading) Modifier.shimmer() else Modifier),
            onSuccess = { isImageLoading = false },
            onError = { isImageLoading = false },
            placeholder = ColorPainter(MaterialTheme.colorScheme.onSurfaceVariant),
            error = painterResource(R.drawable.img_error_movie_detail)
        )

        Column(modifier = Modifier.padding(Dimens.SpacingLarge)) {
            Text(
                text = movieDetail.title,
                style = MaterialTheme.typography.headlineMedium,
            )

            if (movieDetail.tagline.isNotBlank()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingXSmall))
                Text(
                    text = movieDetail.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)) {
                movieDetail.genres.forEach { genre ->
                    AssistChip(
                        onClick = {},
                        label = { Text(genre.name) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "★ ${"%.1f".format(movieDetail.voteAverage)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(Dimens.SpacingSmall))
                Text(
                    text = "(${movieDetail.voteCount} votes)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

            Text(
                text = movieDetail.overview,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

            InfoRow(label = "Status", value = movieDetail.status)
            InfoRow(label = "Release date", value = movieDetail.releaseDate)
            InfoRow(label = "Runtime", value = "${movieDetail.runtime} min")
            InfoRow(label = "Budget", value = movieDetail.budget.toFormattedCurrency())
            InfoRow(label = "Revenue", value = movieDetail.revenue.toFormattedCurrency())

            if (movieDetail.imdbId.isNotBlank()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                TextButton(onClick = {
                    onImdbClick(IMDB_TITLE + movieDetail.imdbId)
                }) {
                    Text(stringResource(R.string.detail_imdb))
                    Spacer(Modifier.width(Dimens.SpacingXSmall))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.ImdbIconSize),
                    )
                }
            }
        }
    }
}