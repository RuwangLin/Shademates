package com.example.a5120_shademate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a5120_shademate.model.EducationContent
import com.example.a5120_shademate.model.ContentType
import com.example.a5120_shademate.model.EducationCategory
import com.example.a5120_shademate.ui.theme.ShadeMateTheme

@Composable
fun EducationCard(
    content: EducationContent,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val isStatistic = content.contentType == ContentType.STATISTIC
    val tagColor = if (isStatistic) Color(0xFF2C6A8D) else MaterialTheme.colorScheme.primary
    val tagBg = if (isStatistic) Color(0xFFE9F3FF) else Color(0xFFEAF4EF)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !content.mediaUrl.isNullOrEmpty()) {
                content.mediaUrl?.let { url ->
                    // Basic check to ensure it's a valid link format for the handler
                    if (url.startsWith("http")) {
                        uriHandler.openUri(url)
                    }
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = tagBg),
                    ) {
                        Text(
                            text = content.contentType.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = tagColor,
                        )
                    }

                    // Category Tag
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Text(
                            text = content.category.name.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Text(
                text = content.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = content.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            content.sourceName?.let { source ->
                Text(
                    text = if (!content.mediaUrl.isNullOrEmpty()) "Source: $source (Click to learn more)" else "Source: $source",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    textDecoration = if (!content.mediaUrl.isNullOrEmpty()) TextDecoration.Underline else TextDecoration.None,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EducationCardPreview() {
    ShadeMateTheme {
        EducationCard(
            content = EducationContent(
                contentId = 1,
                title = "Heat fact preview",
                contentType = ContentType.FACT,
                category = EducationCategory.HEALTH_IMPACT,
                description = "This card is used for clean, mobile-friendly education content.",
                sourceName = "Official Health Source"
            ),
        )
    }
}
