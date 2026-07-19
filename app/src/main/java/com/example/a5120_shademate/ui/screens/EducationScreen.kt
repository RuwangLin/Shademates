package com.example.a5120_shademate.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a5120_shademate.data.HeatRepository
import com.example.a5120_shademate.model.ContentType
import com.example.a5120_shademate.model.EducationContent
import com.example.a5120_shademate.model.LoadState
import com.example.a5120_shademate.ui.components.EmptyStateView
import com.example.a5120_shademate.ui.components.ErrorStateView
import com.example.a5120_shademate.ui.components.LoadingStateView
import com.example.a5120_shademate.ui.theme.ShadeMatePalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EducationScreen(
    repository: HeatRepository,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf<LoadState<List<EducationContent>>>(LoadState.Loading) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            state = LoadState.Loading
            state = try {
                val result = repository.getEducationContent()
                if (result.isEmpty()) LoadState.Empty else LoadState.Success(result)
            } catch (exception: Exception) {
                LoadState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    LaunchedEffect(repository) { loadData() }

    EducationScreenContent(
        state = state,
        onRetry = ::loadData,
        modifier = modifier,
    )
}

@Composable
fun EducationScreenContent(
    state: LoadState<List<EducationContent>>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visibleStep by remember(state) { mutableIntStateOf(0) }

    LaunchedEffect(state) {
        visibleStep = 0
        visibleStep = 1
        delay(80)
        visibleStep = 2
        delay(80)
        visibleStep = 3
        delay(80)
        visibleStep = 4
        delay(80)
        visibleStep = 5
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF6F8F5),
                        Color(0xFFFBFCFA),
                        Color.White,
                    ),
                ),
            )
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = 142.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            EducationEntranceBlock(visible = visibleStep >= 1) {
                EducationPageHeader()
            }

            when (state) {
                is LoadState.Loading -> {
                    EducationEntranceBlock(visible = visibleStep >= 2) {
                        LoadingStateView(
                            title = "Loading...",
                            message = "Fetching education content.",
                        )
                    }
                }

                is LoadState.Empty -> {
                    EducationEntranceBlock(visible = visibleStep >= 2) {
                        EmptyStateView(
                            title = "No content found",
                            message = "We couldn't find any education content at the moment.",
                        )
                    }
                }

                is LoadState.Error -> {
                    EducationEntranceBlock(visible = visibleStep >= 2) {
                        ErrorStateView(
                            title = "Error",
                            message = state.message,
                            onRetry = onRetry,
                        )
                    }
                }

                is LoadState.Success -> {
                    val allData = state.data
                    val facts = allData.filter { it.contentType == ContentType.FACT }
                    val statistics = allData.filter { it.contentType == ContentType.STATISTIC }
                    val records = allData.filter { it.contentType == ContentType.RECORD }

                    EducationEntranceBlock(visible = visibleStep >= 2) {
                        EducationHighlightCard(
                            content = facts.firstOrNull() ?: statistics.firstOrNull() ?: records.firstOrNull(),
                        )
                    }

                    EducationEntranceBlock(visible = visibleStep >= 3) {
                        EducationPagerCard(
                            sectionTitle = "Fact Check Time!",
                            buttonLabel = "Next fact",
                            items = facts,
                        )
                    }

                    EducationEntranceBlock(visible = visibleStep >= 4) {
                        EducationPagerCard(
                            sectionTitle = "Impact Statistics",
                            buttonLabel = "Next stat",
                            items = statistics,
                        )
                    }

                    EducationEntranceBlock(visible = visibleStep >= 5) {
                        EducationPagerCard(
                            sectionTitle = "Climate Records",
                            buttonLabel = "Next",
                            items = records,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EducationEntranceBlock(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(260)) + slideInVertically(
            animationSpec = tween(260, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 6 },
        ),
        exit = fadeOut(animationSpec = tween(140)),
    ) {
        content()
    }
}

@Composable
private fun EducationPageHeader() {
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = Color.White.copy(alpha = 0.28f),
                spotColor = ShadeMatePalette.PrimaryGreen.copy(alpha = 0.08f),
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FCF9).copy(alpha = 0.90f),
                        Color(0xFFEEF7F1).copy(alpha = 0.94f),
                        Color(0xFFE8F4EC).copy(alpha = 0.96f),
                    ),
                ),
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.72f),
                shape = shape,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Heat Education",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 38.sp,
                    letterSpacing = (-0.4).sp,
                ),
                color = ShadeMatePalette.PrimaryText.copy(alpha = 0.95f),
            )
            Text(
                text = "Educational resources for urban heat awareness.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 23.sp,
                    letterSpacing = 0.04.sp,
                ),
                color = ShadeMatePalette.SecondaryText.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun EducationHighlightCard(content: EducationContent?) {
    if (content == null) return

    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = shape,
                ambientColor = ShadeMatePalette.LowUvGreen.copy(alpha = 0.20f),
                spotColor = Color.Black.copy(alpha = 0.10f),
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF72A758),
                        Color(0xFF4B8641),
                    ),
                ),
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.22f),
                shape = shape,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Did You Know?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.25).sp,
                ),
                color = Color.White,
            )
            Text(
                text = content.description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 25.sp,
                    letterSpacing = 0.02.sp,
                ),
                color = Color.White.copy(alpha = 0.95f),
            )
        }
    }
}

@Composable
private fun EducationPagerCard(
    sectionTitle: String,
    buttonLabel: String,
    items: List<EducationContent>,
) {
    if (items.isEmpty()) return

    var selectedIndex by remember(items) { mutableIntStateOf(0) }
    val item = items[selectedIndex]
    val shape = RoundedCornerShape(30.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.992f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "education_card_scale",
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (pressed) 10.dp else 16.dp,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "education_card_shadow",
    )
    val glassAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 0.80f,
        animationSpec = tween(durationMillis = 130),
        label = "education_card_glass_alpha",
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.72f else 0.56f,
        animationSpec = tween(durationMillis = 130),
        label = "education_card_border_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.White.copy(alpha = 0.26f),
                spotColor = ShadeMatePalette.PrimaryGreen.copy(alpha = 0.10f),
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FCF9).copy(alpha = glassAlpha),
                        Color(0xFFEEF7F1).copy(alpha = glassAlpha),
                        Color(0xFFE8F4EC).copy(alpha = glassAlpha),
                    ),
                ),
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = borderAlpha),
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {},
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp)
                .animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.18).sp,
                ),
                color = ShadeMatePalette.PrimaryText.copy(alpha = 0.88f),
            )

            AnimatedContent(
                targetState = item,
                label = "education_card_content",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(230, delayMillis = 45)) +
                        slideInVertically(
                            animationSpec = tween(230, easing = FastOutSlowInEasing),
                            initialOffsetY = { it / 5 },
                        )) togetherWith
                        (fadeOut(animationSpec = tween(150)) +
                            slideOutVertically(
                                animationSpec = tween(180, easing = FastOutSlowInEasing),
                                targetOffsetY = { -it / 6 },
                            ))
                },
            ) { currentItem ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = currentItem.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 31.sp,
                            letterSpacing = (-0.28).sp,
                        ),
                        color = ShadeMatePalette.PrimaryText.copy(alpha = 0.92f),
                    )

                    Text(
                        text = currentItem.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp,
                            letterSpacing = 0.04.sp,
                        ),
                        color = ShadeMatePalette.SecondaryText.copy(alpha = 0.94f),
                    )

                    currentItem.sourceName?.takeIf { it.isNotBlank() }?.let { source ->
                        Text(
                            text = source,
                            style = MaterialTheme.typography.labelMedium.copy(
                                lineHeight = 18.sp,
                                letterSpacing = 0.04.sp,
                            ),
                            color = ShadeMatePalette.SecondaryText.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF4FAF6).copy(alpha = 0.82f),
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = "${selectedIndex + 1} / ${items.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.05.sp,
                        ),
                        color = ShadeMatePalette.SecondaryText.copy(alpha = 0.92f),
                    )
                }

                EducationActionButton(
                    label = buttonLabel,
                    onClick = {
                        selectedIndex = (selectedIndex + 1) % items.size
                    },
                )
            }
        }
    }
}

@Composable
private fun EducationActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "education_button_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "education_button_alpha",
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 10.dp else 16.dp,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "education_button_elevation",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = ShadeMatePalette.PrimaryGreen.copy(alpha = 0.24f),
                spotColor = Color.Black.copy(alpha = 0.12f),
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF76B260),
                        Color(0xFF4E8B46),
                    ),
                ),
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.04.sp,
                ),
                color = Color.White,
            )
        }
    }
}
