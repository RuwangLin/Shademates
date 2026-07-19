package com.example.a5120_shademate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a5120_shademate.ui.theme.ShadeMateTheme

@Composable
fun LoadingStateView(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    ScreenStateCard(
        title = title,
        message = message,
        modifier = modifier,
        topContent = {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        },
    )
}

@Composable
fun EmptyStateView(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    ScreenStateCard(
        title = title,
        message = message,
        modifier = modifier,
    )
}

@Composable
fun ErrorStateView(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenStateCard(
        title = title,
        message = message,
        modifier = modifier,
        bottomContent = {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(text = "Retry")
            }
        },
    )
}

@Composable
private fun ScreenStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    topContent: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Text(
                    text = "Status",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            topContent?.invoke()
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            bottomContent?.invoke()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    ShadeMateTheme {
        LoadingStateView(
            title = "Loading heat map",
            message = "Mock data is being loaded for the Melbourne map view.",
        )
    }
}
