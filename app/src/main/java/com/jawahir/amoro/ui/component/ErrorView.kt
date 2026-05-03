package com.jawahir.amoro.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jawahir.amoro.Greeting
import com.jawahir.amoro.R
import com.jawahir.amoro.ui.theme.AMOROTheme
import com.jawahir.amoro.ui.theme.Dimens

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Dimens.SpacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.padding(Dimens.SpacingSmall))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.padding(Dimens.SpacingLarge))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.error_retry))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun previewErrorView(){
    AMOROTheme {
        ErrorView(
            message = "Unknow server error",
            onRetry = {}
        )
    }
}