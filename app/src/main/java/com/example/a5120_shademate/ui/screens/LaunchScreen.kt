package com.example.a5120_shademate.ui.screens

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.a5120_shademate.R
import com.example.a5120_shademate.ui.theme.ShadeMatePalette
import kotlinx.coroutines.delay

private data class OnboardingFeature(
    val title: String,
)

@Composable
fun LaunchScreen(
    onCustomize: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val features = remember {
        listOf(
            OnboardingFeature(
                title = "Get smart route suggestions with more shade and less heat exposure",
            ),
            OnboardingFeature(
                title = "Find nearby libraries, parks, shopping centres, and other cool spots around you",
            ),
            OnboardingFeature(
                title = "Receive heat alerts and helpful tips to protect your skin and health every day",
            ),
        )
    }
    var titleVisible by remember { mutableStateOf(false) }
    var illustrationVisible by remember { mutableStateOf(false) }
    val cardVisibilities = remember { mutableStateListOf(false, false, false) }
    var actionsVisible by remember { mutableStateOf(false) }
    var selectedCardIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        titleVisible = true
        delay(120)
        illustrationVisible = true
        delay(180)
        cardVisibilities.indices.forEach { index ->
            cardVisibilities[index] = true
            delay(95)
        }
        delay(120)
        actionsVisible = true
    }

    LaunchedEffect(selectedCardIndex) {
        if (selectedCardIndex >= 0) {
            val capturedIndex = selectedCardIndex
            delay(900)
            if (selectedCardIndex == capturedIndex) {
                selectedCardIndex = -1
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val compactScreen = maxWidth <= 380.dp
        val narrowScreen = maxWidth <= 360.dp
        val shortScreen = maxHeight <= 760.dp
        val horizontalScreenPadding = when {
            narrowScreen -> 16.dp
            compactScreen -> 20.dp
            else -> 30.dp
        }
        val titleStartPadding = when {
            narrowScreen -> 4.dp
            compactScreen -> 8.dp
            else -> 16.dp
        }
        val featureColumnHorizontalPadding = if (compactScreen) 0.dp else 4.dp
        val featureCardSpacing = if (shortScreen) 8.dp else 10.dp
        val buttonWidthFraction = if (narrowScreen) 1f else if (compactScreen) 0.96f else 0.92f
        val titleTopPadding = if (shortScreen) 4.dp else 10.dp
        val sectionBottomSpacerWeight = if (shortScreen) 0.88f else 1f
        val buttonTopSpacing = if (shortScreen) 20.dp else 28.dp
        val welcomeFontSize = if (narrowScreen) 20.sp else 22.sp
        val welcomeLineHeight = if (narrowScreen) 25.sp else 28.sp
        val appNameFontSize = if (narrowScreen) 36.sp else 42.sp
        val appNameLineHeight = if (narrowScreen) 40.sp else 46.sp

        LoopingOnboardingVideo(
            modifier = Modifier
                .fillMaxSize()
                .animatedEntrance(
                    visible = illustrationVisible,
                    initialOffsetY = 0f,
                    initialScale = 1.035f,
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = horizontalScreenPadding, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = titleTopPadding, start = titleStartPadding)
                    .animatedEntrance(
                        visible = titleVisible,
                        initialOffsetY = 22f,
                        initialScale = 0.985f,
                    ),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Welcome to",
                    color = Color(0xCC6A6E71),
                    fontSize = welcomeFontSize,
                    lineHeight = welcomeLineHeight,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.15.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = Color.White.copy(alpha = 0.28f),
                            blurRadius = 8f,
                        ),
                    ),
                )
                Text(
                    text = "ShadeMates",
                    color = ShadeMatePalette.PrimaryText.copy(alpha = 0.94f),
                    fontSize = appNameFontSize,
                    lineHeight = appNameLineHeight,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.7).sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = Color.White.copy(alpha = 0.22f),
                            blurRadius = 10f,
                        ),
                    ),
                )
            }

            Spacer(modifier = Modifier.weight(sectionBottomSpacerWeight))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = featureColumnHorizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(featureCardSpacing),
            ) {
                features.forEachIndexed { index, feature ->
                    OnboardingFeatureCard(
                        feature = feature,
                        visible = cardVisibilities[index],
                        selected = selectedCardIndex == index,
                        onClick = { selectedCardIndex = index },
                    )
                }
            }

            Spacer(modifier = Modifier.height(buttonTopSpacing))

            OnboardingPrimaryButton(
                text = "Customize Your Preference Now",
                onClick = onCustomize,
                modifier = Modifier
                    .fillMaxWidth(buttonWidthFraction)
                    .heightIn(min = if (shortScreen) 54.dp else 58.dp)
                    .animatedEntrance(
                        visible = actionsVisible,
                        initialOffsetY = 26f,
                        initialScale = 0.985f,
                    ),
            )

            TextButton(
                onClick = onSkip,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xCC6A6E73),
                ),
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .animatedEntrance(
                        visible = actionsVisible,
                        initialOffsetY = 24f,
                        initialScale = 0.985f,
                    ),
            ) {
                Text(
                    text = "Skip",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.1.sp,
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureCard(
    feature: OnboardingFeature,
    visible: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(30.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.986f
            selected -> 1.01f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "onboarding_card_scale",
    )
    val elevation by animateDpAsState(
        targetValue = when {
            pressed -> 10.dp
            selected -> 18.dp
            else -> 14.dp
        },
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "onboarding_card_elevation",
    )
    val borderAlpha by animateFloatAsState(
        targetValue = when {
            pressed -> 0.86f
            selected -> 0.72f
            else -> 0.56f
        },
        animationSpec = tween(durationMillis = 150),
        label = "onboarding_card_border_alpha",
    )
    val overlayAlpha by animateFloatAsState(
        targetValue = when {
            pressed -> 0.13f
            selected -> 0.07f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 140),
        label = "onboarding_card_overlay_alpha",
    )
    val verticalPadding by animateDpAsState(
        targetValue = if (selected) 12.dp else 10.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "onboarding_card_vertical_padding",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .animatedEntrance(
                visible = visible,
                initialOffsetY = 30f,
                initialScale = 0.98f,
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.White.copy(alpha = 0.30f),
                spotColor = ShadeMatePalette.PrimaryGreen.copy(alpha = 0.12f),
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        val compact = maxWidth <= 336.dp
        val extraCompact = maxWidth <= 312.dp
        val horizontalPadding = when {
            extraCompact -> 6.dp
            compact -> 8.dp
            else -> 16.dp
        }
        val textSize = when {
            extraCompact -> 11.6.sp
            compact -> 12.6.sp
            else -> 14.4.sp
        }
        val textLineHeight = when {
            extraCompact -> 15.sp
            compact -> 16.6.sp
            else -> 19.2.sp
        }
        val minCardHeight = when {
            extraCompact -> 58.dp
            compact -> 60.dp
            else -> 66.dp
        }
        val maxCardHeight = when {
            extraCompact -> 80.dp
            compact -> 74.dp
            else -> 70.dp
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(180, easing = FastOutSlowInEasing)),
            shape = shape,
            color = Color.Transparent,
            shadowElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.42f + (overlayAlpha * 0.65f)),
                                Color.White.copy(alpha = 0.25f + (overlayAlpha * 0.45f)),
                            ),
                        ),
                        shape = shape,
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFBCD48E).copy(alpha = borderAlpha),
                        shape = shape,
                    )
                    .heightIn(min = minCardHeight, max = maxCardHeight)
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = feature.title,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xE658625F),
                    fontSize = textSize,
                    lineHeight = textLineHeight,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = if (compact) 0.sp else 0.08.sp,
                    textAlign = TextAlign.Center,
                    maxLines = if (extraCompact) 3 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "onboarding_button_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "onboarding_button_alpha",
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (pressed) 12.dp else 20.dp,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "onboarding_button_shadow",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val compact = maxWidth <= 340.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                        brush = Brush.verticalGradient(
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
                    color = Color.White,
                    fontSize = if (compact) 14.sp else 16.sp,
                    lineHeight = if (compact) 18.sp else 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.08.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun Modifier.animatedEntrance(
    visible: Boolean,
    initialOffsetY: Float,
    initialScale: Float,
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
        label = "onboarding_entrance_alpha",
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else initialOffsetY,
        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
        label = "onboarding_entrance_translation",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else initialScale,
        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
        label = "onboarding_entrance_scale",
    )
    return graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
        scaleX = scale
        scaleY = scale
    }
}

@Composable
private fun LoopingOnboardingVideo(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val videoUri = remember(context) {
        Uri.parse("android.resource://${context.packageName}/${R.raw.onboarding}")
    }
    val player = remember(context, videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
            volume = 0f
            prepare()
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                setKeepContentOnPlayerReset(true)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
        },
        update = { playerView ->
            playerView.player = player
        },
    )
}
