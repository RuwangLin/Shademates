package com.example.a5120_shademate.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.a5120_shademate.data.UserProfile
import com.example.a5120_shademate.data.UserProfilePreferences
import com.example.a5120_shademate.ui.components.CenterTitleTopBar
import com.example.a5120_shademate.ui.theme.ShadeMatePalette
import kotlinx.coroutines.delay

private enum class CustomiseSelectorKey {
    AGE,
    HEALTH,
}

private val HeatSensitivityOptions = listOf(
    "No, I handle heat well.",
    "Yes, respiratory conditions (e.g., Asthma).",
    "Yes, cardiovascular / heart conditions.",
    "Yes, I am pregnant.",
    "Yes, other conditions / age-related sensitivity (65+ or managing young children).",
)

@Composable
fun ProfileCustomisationScreen(
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    saveButtonText: String = "Save And Return",
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val savedProfile = remember(context) { UserProfilePreferences.read(context) }
    val savedHealthSelection = remember(savedProfile.skinHistory) {
        mapSavedHeatSensitivity(savedProfile.skinHistory)
    }

    var ageGroup by rememberSaveable {
        mutableStateOf(
            savedProfile.ageGroup.takeIf { it in UserProfilePreferences.ageGroupOptions }.orEmpty()
        )
    }
    var healthCondition by rememberSaveable { mutableStateOf(savedHealthSelection) }
    var expandedSelector by rememberSaveable { mutableStateOf<CustomiseSelectorKey?>(null) }
    var visibleStep by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        visibleStep = 0
        delay(40)
        visibleStep = 1
        delay(70)
        visibleStep = 2
        delay(70)
        visibleStep = 3
        delay(70)
        visibleStep = 4
        delay(70)
        visibleStep = 5
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAF6),
                        Color(0xFFF2F6F0),
                        ShadeMatePalette.AppBackground,
                    ),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CustomiseEntranceBlock(visible = visibleStep >= 1) {
                CenterTitleTopBar(
                    title = "Customise",
                    onBack = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                    titleSize = 28.sp,
                    showBackButton = showBackButton,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                CustomiseEntranceBlock(visible = visibleStep >= 2) {
                    IntroCard()
                }

                CustomiseEntranceBlock(visible = visibleStep >= 3) {
                    ProfileSectionCard(
                        title = "Age Group",
                        description = "Helps tailor heat and UV guidance to your likely risk profile.",
                    ) {
                        CompactSelector(
                            value = ageGroup.takeIf { it.isNotBlank() },
                            placeholder = "Select age group",
                            expanded = expandedSelector == CustomiseSelectorKey.AGE,
                            onToggle = {
                                expandedSelector = if (expandedSelector == CustomiseSelectorKey.AGE) {
                                    null
                                } else {
                                    CustomiseSelectorKey.AGE
                                }
                            },
                            options = UserProfilePreferences.ageGroupOptions,
                            onOptionSelected = { selection ->
                                ageGroup = selection
                                expandedSelector = null
                            },
                        )
                    }
                }

                CustomiseEntranceBlock(visible = visibleStep >= 4) {
                    ProfileSectionCard(
                        title = "Heat Sensitivity",
                        description = "Do you have any pre-existing health conditions or factors that make you more sensitive to extreme heat?",
                    ) {
                        CompactSelector(
                            value = healthCondition,
                            placeholder = "Select one option",
                            expanded = expandedSelector == CustomiseSelectorKey.HEALTH,
                            onToggle = {
                                expandedSelector = if (expandedSelector == CustomiseSelectorKey.HEALTH) {
                                    null
                                } else {
                                    CustomiseSelectorKey.HEALTH
                                }
                            },
                            options = HeatSensitivityOptions,
                            onOptionSelected = { selection ->
                                healthCondition = selection
                                expandedSelector = null
                            },
                        )
                    }
                }

                CustomiseEntranceBlock(visible = visibleStep >= 5) {
                    SaveButton(
                        text = saveButtonText,
                        onClick = {
                            val resolvedAgeGroup = ageGroup.ifBlank { UserProfilePreferences.defaultAgeGroup }
                            val resolvedHealthCondition = healthCondition
                                ?: savedProfile.skinHistory.takeIf {
                                    it.isNotBlank() && mapSavedHeatSensitivity(it) == null
                                }
                                ?: UserProfilePreferences.defaultSkinHistory

                            UserProfilePreferences.write(
                                context = context,
                                profile = UserProfile(
                                    ageGroup = resolvedAgeGroup,
                                    // The selector UI captures heat sensitivity,
                                    // but we intentionally keep the original
                                    // skinHistory model field until backend and
                                    // frontend are renamed together.
                                    skinHistory = resolvedHealthCondition,
                                ),
                            )
                            onBack()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroCard() {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.62f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF7FCF7),
                            Color(0xFFEDF6EF),
                            Color(0xFFE6F1E8),
                        ),
                    ),
                ),
        ) {
            val compact = maxWidth <= 340.dp
            Column(
                modifier = Modifier.padding(
                    horizontal = if (compact) 16.dp else 22.dp,
                    vertical = 22.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Personalise Safety Guidance",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = if (compact) 20.sp else 24.sp,
                        lineHeight = if (compact) 24.sp else 28.sp,
                    ),
                    color = ShadeMatePalette.PrimaryText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "These preferences help the UV advice service generate calmer, more personal heat and sun protection guidance on the home page.",
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                    color = ShadeMatePalette.SecondaryText,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.84f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.68f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                    ),
                    color = ShadeMatePalette.PrimaryText,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                    color = ShadeMatePalette.SecondaryText,
                    fontWeight = FontWeight.Normal,
                )
            }
            content()
        }
    }
}

@Composable
private fun CompactSelector(
    value: String?,
    placeholder: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "selectorChevronRotation",
    )
    val selectorInteraction = remember { MutableInteractionSource() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .iosPressAnimation(selectorInteraction)
                .clickable(
                    interactionSource = selectorInteraction,
                    indication = null,
                    onClick = onToggle,
                ),
            shape = RoundedCornerShape(22.dp),
            color = Color.White.copy(alpha = 0.76f),
            border = BorderStroke(1.dp, Color(0xFFDDE8DF)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value ?: placeholder,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    ),
                    color = if (value == null) ShadeMatePalette.SecondaryText else ShadeMatePalette.PrimaryText,
                    fontWeight = if (value == null) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.size(12.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ShadeMatePalette.SecondaryText,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer { rotationZ = chevronRotation },
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(220)) + expandVertically(animationSpec = tween(220)),
            exit = fadeOut(tween(160)) + shrinkVertically(animationSpec = tween(160)),
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.84f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    options.forEachIndexed { index, option ->
                        SelectorOptionRow(
                            label = option,
                            selected = value == option,
                            onClick = { onOptionSelected(option) },
                        )
                        if (index != options.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                color = Color(0xFFE5ECE6),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .iosPressAnimation(interactionSource)
            .background(
                color = if (selected) Color(0xFFE5F1E7) else Color.Transparent,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
            color = ShadeMatePalette.PrimaryText,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )

        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.zIndex(1f),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(ShadeMatePalette.PrimaryGreen, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    text: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "customiseSaveButtonScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "customiseSaveButtonAlpha",
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 20.dp,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "customiseSaveButtonShadow",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
    ) {
        val compact = maxWidth <= 340.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = ShadeMatePalette.PrimaryGreen.copy(alpha = 0.30f),
                    spotColor = Color.Black.copy(alpha = 0.12f),
                )
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF74B15D),
                            Color(0xFF4B8A43),
                        ),
                    ),
                    shape = shape,
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.26f),
                    shape = shape,
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = if (compact) 14.dp else 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (compact) 14.sp else 16.sp,
                    lineHeight = if (compact) 18.sp else 20.sp,
                ),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.08.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CustomiseEntranceBlock(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(260)) + slideInVertically(
            animationSpec = tween(260),
            initialOffsetY = { it / 6 },
        ),
        exit = fadeOut(animationSpec = tween(160)),
    ) {
        content()
    }
}

private fun Modifier.iosPressAnimation(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
    pressedAlpha: Float = 0.92f,
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "customisePressScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) pressedAlpha else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "customisePressAlpha",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

private fun mapSavedHeatSensitivity(rawValue: String): String? {
    val normalized = rawValue.trim()
    if (normalized.isBlank()) return null
    HeatSensitivityOptions.firstOrNull { it.equals(normalized, ignoreCase = true) }?.let { return it }

    val lower = normalized.lowercase()
    return when {
        lower.contains("no recorded") ||
            lower.contains("no known") ||
            lower.contains("handle heat") ||
            lower.contains("heat well") -> HeatSensitivityOptions[0]

        lower.contains("asthma") ||
            lower.contains("respir") -> HeatSensitivityOptions[1]

        lower.contains("heart") ||
            lower.contains("cardio") ||
            lower.contains("cardiovascular") ||
            lower.contains("blood pressure") -> HeatSensitivityOptions[2]

        lower.contains("pregnan") -> HeatSensitivityOptions[3]

        lower.contains("65+") ||
            lower.contains("65") ||
            lower.contains("young children") ||
            lower.contains("children") ||
            lower.contains("senior") ||
            lower.contains("elder") -> HeatSensitivityOptions[4]

        else -> null
    }
}
