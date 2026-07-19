package com.example.a5120_shademate.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a5120_shademate.data.MockMode
import com.example.a5120_shademate.ui.theme.ShadeMateTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MockModeSelector(
    label: String,
    selectedMode: MockMode,
    onModeSelected: (MockMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Text(
                            text = "DEV",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                FilterChip(
                    selected = expanded,
                    onClick = { expanded = !expanded },
                    label = { Text(if (expanded) "Hide" else "Show") },
                    shape = RoundedCornerShape(16.dp),
                    border = null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFFF4F7F4),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            if (expanded) {
                Text(
                    text = "Testing helper only. Safe to remove after backend integration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MockMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { onModeSelected(mode) },
                            label = {
                                Text(
                                    mode.name.lowercase().replaceFirstChar { firstChar ->
                                        firstChar.uppercase()
                                    },
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF4F7F4),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MockModeSelectorPreview() {
    ShadeMateTheme {
        MockModeSelector(
            label = "Heat map mock mode",
            selectedMode = MockMode.SUCCESS,
            onModeSelected = {},
        )
    }
}
