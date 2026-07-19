package com.example.a5120_shademate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.a5120_shademate.R
import com.example.a5120_shademate.data.CoolPlaceRepository
import com.example.a5120_shademate.data.HeatRepository
import com.example.a5120_shademate.data.HomeOverviewRepository
import com.example.a5120_shademate.data.api.WalkingRouteRepository
import com.example.a5120_shademate.model.CurrentLocationWeather
import com.example.a5120_shademate.model.HomeAreaTemperature
import com.example.a5120_shademate.model.HomeHeatOverview
import com.example.a5120_shademate.model.HeatLevel
import com.example.a5120_shademate.model.HeatMapData
import com.example.a5120_shademate.model.HeatZone
import com.example.a5120_shademate.model.LoadState
import com.example.a5120_shademate.ui.components.CoolPlaceCategory
import com.example.a5120_shademate.ui.components.CoolPlaceCard
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import com.example.a5120_shademate.ui.components.CoolPlaceImage
import com.example.a5120_shademate.ui.components.ShadeMateProfileButton
import com.example.a5120_shademate.ui.components.ShadeMateTopBar
import com.example.a5120_shademate.ui.components.colorForHeatLevel
import com.example.a5120_shademate.ui.theme.ShadeMatePalette
import com.example.a5120_shademate.ui.viewmodel.HomeCoordinates
import com.example.a5120_shademate.ui.viewmodel.HomeViewModel
import com.example.a5120_shademate.ui.viewmodel.HomeViewModelFactory
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import kotlin.coroutines.resume
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val HERO_IMAGE_OVERLAY_ALPHA = 0.56f
private const val UV_CARD_IMAGE_OVERLAY_ALPHA = 0.46f
private const val WEATHER_CARD_IMAGE_OVERLAY_ALPHA = 0.42f
private const val TIPS_CARD_IMAGE_OVERLAY_ALPHA = 0.54f
private const val TIPS_EXPAND_ANIMATION_MS = 420
private const val WEATHER_EXPAND_ANIMATION_MS = 420
private val TipsExpandedExtraSpace = 44.dp
private val TipsExpandedHeroHeight = 132.dp
private val TipsExpandedMinHeight = 0.dp
private val TipsExpandedContentTopPadding = 56.dp
private val TipsExpandedItemSpacing = 22.dp
private val WeatherExpandedMinHeight = 300.dp
private val WeatherExpandedExtraSpace = 40.dp
private val WeatherExpandedContentTopPadding = 76.dp
private val WeatherExpandedPanelBaseHeight = 84.dp
private val WeatherExpandedMetricSpacing = 14.dp
private val WeatherExpandedHintHeight = 28.dp
private val WeatherCollapsedCardHeight = 202.dp
private val QuickTipsCollapsedCardHeight = 216.dp
private val PremiumCardShape = RoundedCornerShape(28.dp)
private val HomeContentShape = RoundedCornerShape(0.dp)

private enum class HomeFilterCategory(
    val label: String,
    val icon: ImageVector,
) {
    OVERALL("All", Icons.Outlined.GridView),
    WEATHER("Weather", Icons.Default.WbSunny),
    PLACES("Nearby", Icons.Default.LocationOn),
    MAP("Map", Icons.Outlined.Map),
}

private data class MapSearchSelection(
    val point: Point,
    val title: String,
)

private enum class RouteSearchField {
    ORIGIN,
    DESTINATION,
}

@Composable
fun HomeScreen(
    homeOverviewRepository: HomeOverviewRepository,
    coolPlaceRepository: CoolPlaceRepository,
    onOpenMap: () -> Unit,
    onOpenProfileCustomisation: () -> Unit,
    onRecommendedPlaceSelected: (CoolPlaceCardData) -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val defaultLat = -37.8136
    val defaultLon = 144.9631
    val homeViewModel: HomeViewModel = viewModel(
        factory = remember(homeOverviewRepository, coolPlaceRepository, context) {
            HomeViewModelFactory(
                context = context,
                homeOverviewRepository = homeOverviewRepository,
                coolPlaceRepository = coolPlaceRepository,
            )
        }
    )
    val scope = rememberCoroutineScope()
    val heatOverviewState = homeViewModel.heatOverviewState
    val coolPlacesState = homeViewModel.coolPlacesState
    val locationPermissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
    var selectedCategory by rememberSaveable { mutableStateOf(HomeFilterCategory.OVERALL.name) }
    val activeCategory = remember(selectedCategory) { HomeFilterCategory.valueOf(selectedCategory) }
    val contentScrollState = rememberScrollState()
    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var tipsCardBounds by remember { mutableStateOf<Rect?>(null) }
    var isTipsOverlayVisible by rememberSaveable { mutableStateOf(false) }
    var isTipsOverlayExpanded by rememberSaveable { mutableStateOf(false) }
    var weatherCardBounds by remember { mutableStateOf<Rect?>(null) }
    var isWeatherOverlayVisible by rememberSaveable { mutableStateOf(false) }
    var isWeatherOverlayExpanded by rememberSaveable { mutableStateOf(false) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.toFloat()
    val collapsedHeroCopyTop = (screenHeight * 0.029f).dp.coerceIn(22.dp, 28.dp)
    val maxHeroHeight = 474.dp
    val minHeroHeight = (screenHeight * 0.275f).dp.coerceIn(212.dp, 232.dp)
    val collapseRangePx = with(density) { (maxHeroHeight - minHeroHeight).toPx() }
    var heroCollapsePx by remember { mutableFloatStateOf(0f) }
    fun updateBottomBarVisibility(scrollDeltaY: Float) {
        when {
            scrollDeltaY < -1f -> onBottomBarVisibilityChanged(false)
            scrollDeltaY > 1f -> onBottomBarVisibilityChanged(true)
        }
    }
    val collapseProgress by remember(collapseRangePx, heroCollapsePx) {
        derivedStateOf {
            if (collapseRangePx <= 0f) 0f else (heroCollapsePx / collapseRangePx).coerceIn(0f, 1f)
        }
    }
    val heroHeight by remember(density, heroCollapsePx) {
        derivedStateOf {
            maxHeroHeight - with(density) { heroCollapsePx.toDp() }
        }
    }
    val stagedCollapseScrollConnection = remember(collapseRangePx, contentScrollState, onBottomBarVisibilityChanged) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                updateBottomBarVisibility(deltaY)

                if (deltaY < 0f && heroCollapsePx < collapseRangePx) {
                    val consumed = min(collapseRangePx - heroCollapsePx, -deltaY)
                    heroCollapsePx += consumed
                    return Offset(0f, -consumed)
                }

                if (deltaY > 0f && contentScrollState.value == 0 && heroCollapsePx > 0f) {
                    val consumed = min(heroCollapsePx, deltaY)
                    heroCollapsePx -= consumed
                    return Offset(0f, consumed)
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                updateBottomBarVisibility(consumed.y.takeIf { it != 0f } ?: available.y)
                if (available.y > 0f && contentScrollState.value == 0 && heroCollapsePx > 0f) {
                    val consumedY = min(heroCollapsePx, available.y)
                    heroCollapsePx -= consumedY
                    return Offset(0f, consumedY)
                }

                return Offset.Zero
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        scope.launch {
            val resolvedCoordinates = if (granted) resolveCurrentCoordinates(context) else null
            homeViewModel.refreshForResolvedCoordinates(
                coordinates = resolvedCoordinates,
                defaultLat = defaultLat,
                defaultLon = defaultLon,
            )
        }
    }

    fun requestLocationAndLoad() {
        scope.launch {
            if (hasHomeLocationPermission(context)) {
                val resolvedCoordinates = resolveCurrentCoordinates(context)
                homeViewModel.forceRefreshCurrentCoordinates(
                    coordinates = resolvedCoordinates,
                    defaultLat = defaultLat,
                    defaultLon = defaultLon,
                )
            } else {
                locationPermissionLauncher.launch(locationPermissions)
            }
        }
    }

    LaunchedEffect(homeOverviewRepository, coolPlaceRepository) {
        onBottomBarVisibilityChanged(true)
        homeViewModel.loadUsingCachedOrDefault(
            defaultLat = defaultLat,
            defaultLon = defaultLon,
            showLoadingState = !homeViewModel.hasVisibleCachedData(),
        )

        if (hasHomeLocationPermission(context)) {
            val resolvedCoordinates = resolveCurrentCoordinates(context)
            homeViewModel.refreshForResolvedCoordinates(
                coordinates = resolvedCoordinates,
                defaultLat = defaultLat,
                defaultLon = defaultLon,
            )
        } else {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    LaunchedEffect(isTipsOverlayVisible, isTipsOverlayExpanded) {
        if (isTipsOverlayVisible && !isTipsOverlayExpanded) {
            delay(TIPS_EXPAND_ANIMATION_MS.toLong())
            isTipsOverlayVisible = false
        }
    }

    LaunchedEffect(isWeatherOverlayVisible, isWeatherOverlayExpanded) {
        if (isWeatherOverlayVisible && !isWeatherOverlayExpanded) {
            delay(WEATHER_EXPAND_ANIMATION_MS.toLong())
            isWeatherOverlayVisible = false
        }
    }

    fun openTipsOverlay() {
        if (tipsCardBounds != null && !isTipsOverlayVisible) {
            isTipsOverlayVisible = true
            isTipsOverlayExpanded = false
            scope.launch {
                delay(16)
                isTipsOverlayExpanded = true
            }
        }
    }

    fun dismissTipsOverlay() {
        isTipsOverlayExpanded = false
    }

    fun openWeatherOverlay() {
        if (weatherCardBounds != null && !isWeatherOverlayVisible) {
            isWeatherOverlayVisible = true
            isWeatherOverlayExpanded = false
            scope.launch {
                delay(16)
                isWeatherOverlayExpanded = true
            }
        }
    }

    fun dismissWeatherOverlay() {
        isWeatherOverlayExpanded = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ShadeMatePalette.AppBackground),
        contentAlignment = Alignment.TopStart,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { rootSize = it }
                .nestedScroll(stagedCollapseScrollConnection)
                .verticalScroll(contentScrollState),
        ) {
            Spacer(modifier = Modifier.height(heroHeight))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = HomeContentShape,
                color = Color.White,
                shadowElevation = 10.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 116.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    when (activeCategory) {
                        HomeFilterCategory.OVERALL -> {
                            RecommendedPlacesSection(
                                state = coolPlacesState,
                                onRetry = ::requestLocationAndLoad,
                                onPlaceSelected = onRecommendedPlaceSelected,
                                animateCards = true,
                                animationBaseIndex = 0,
                            )
                            HeatUvSection(
                                state = heatOverviewState,
                                onRetry = ::requestLocationAndLoad,
                                onWeatherCardClick = ::openWeatherOverlay,
                                onWeatherCardBoundsChanged = { weatherCardBounds = it },
                                weatherCardCollapsedAlpha = if (isWeatherOverlayVisible) 0f else 1f,
                                animateCards = true,
                                animationBaseIndex = 1,
                            )
                            QuickTipsImageCard(
                                state = heatOverviewState,
                                onRetry = ::requestLocationAndLoad,
                                onClick = ::openTipsOverlay,
                                onBoundsChanged = { tipsCardBounds = it },
                                collapsedAlpha = if (isTipsOverlayVisible) 0f else 1f,
                                modifier = Modifier.homeFilterCardEntrance(
                                    index = 3,
                                    enabled = true,
                                ),
                            )
                            MapPreviewSection(
                                onOpenMap = onOpenMap,
                                enableWeatherUpdates = true,
                                animateCard = true,
                                animationIndex = 4,
                            )
                        }

                        HomeFilterCategory.PLACES -> {
                            RecommendedPlacesSection(
                                state = coolPlacesState,
                                onRetry = ::requestLocationAndLoad,
                                onPlaceSelected = onRecommendedPlaceSelected,
                                animateCards = true,
                                animationBaseIndex = 0,
                            )
                        }

                        HomeFilterCategory.WEATHER -> {
                            HeatUvSection(
                                state = heatOverviewState,
                                onRetry = ::requestLocationAndLoad,
                                onWeatherCardClick = ::openWeatherOverlay,
                                onWeatherCardBoundsChanged = { weatherCardBounds = it },
                                weatherCardCollapsedAlpha = if (isWeatherOverlayVisible) 0f else 1f,
                                animateCards = true,
                                animationBaseIndex = 0,
                            )
                            QuickTipsImageCard(
                                state = heatOverviewState,
                                onRetry = ::requestLocationAndLoad,
                                onClick = ::openTipsOverlay,
                                onBoundsChanged = { tipsCardBounds = it },
                                collapsedAlpha = if (isTipsOverlayVisible) 0f else 1f,
                                modifier = Modifier.homeFilterCardEntrance(
                                    index = 2,
                                    enabled = true,
                                ),
                            )
                        }

                        HomeFilterCategory.MAP -> {
                            MapPreviewSection(
                                onOpenMap = onOpenMap,
                                enableWeatherUpdates = true,
                                animateCard = true,
                                animationIndex = 0,
                            )
                        }
                    }
                }
            }
        }

        ImmersiveHomeHero(
            heroHeight = heroHeight,
            collapseProgress = collapseProgress,
            collapsedCopyTop = collapsedHeroCopyTop,
            activeCategory = activeCategory,
            onCategorySelected = { selectedCategory = it.name },
            onOpenProfileCustomisation = onOpenProfileCustomisation,
        )

        if (isTipsOverlayVisible) {
            ExpandedTipsCardOverlay(
                state = heatOverviewState,
                onRetry = ::requestLocationAndLoad,
                sourceBounds = tipsCardBounds,
                rootSize = rootSize,
                expanded = isTipsOverlayExpanded,
                onDismiss = ::dismissTipsOverlay,
            )
        }

        if (isWeatherOverlayVisible) {
            ExpandedWeatherCardOverlay(
                state = heatOverviewState,
                sourceBounds = weatherCardBounds,
                rootSize = rootSize,
                expanded = isWeatherOverlayExpanded,
                onDismiss = ::dismissWeatherOverlay,
            )
        }
    }
}

@Composable
private fun ImmersiveHomeHero(
    heroHeight: androidx.compose.ui.unit.Dp,
    collapseProgress: Float,
    collapsedCopyTop: androidx.compose.ui.unit.Dp,
    activeCategory: HomeFilterCategory,
    onCategorySelected: (HomeFilterCategory) -> Unit,
    onOpenProfileCustomisation: () -> Unit,
) {
    val expandedCopyTop = 192.dp
    val copyTop = lerp(expandedCopyTop, collapsedCopyTop, collapseProgress)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heroHeight)
            .clipToBounds(),
    ) {
        AssetHeroImage(
            drawableName = "hero_top_background",
            fallbackRes = R.drawable.hero_top_background_placeholder,
            modifier = Modifier.fillMaxSize(),
            fitEntireImage = false,
            alignment = Alignment.TopCenter,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF091317).copy(alpha = 0.34f),
                            Color(0xFF112028).copy(alpha = 0.18f),
                            Color(0xFF081114).copy(alpha = 0.74f),
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.42f),
                        ),
                        startY = 0f,
                        endY = 1400f,
                    ),
                ),
        )

        HeroCopyBlock(
            collapseProgress = collapseProgress,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = copyTop,
                ),
        )

        ShadeMateProfileButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 18.dp),
            profileTint = Color.White,
            iconContainerColor = Color.White.copy(alpha = 0.22f),
            iconSize = 27.dp,
            onClick = onOpenProfileCustomisation,
        )

        HomeFilterTabsRow(
            activeCategory = activeCategory,
            onCategorySelected = onCategorySelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 0.dp),
        )
    }
}

@Composable
private fun HeroCopyBlock(
    collapseProgress: Float,
    modifier: Modifier = Modifier,
) {
    val titleSize = (46f - (collapseProgress * 7f)).coerceAtLeast(36f).sp
    val subtitleSize = (18f - (collapseProgress * 2f)).coerceAtLeast(15f).sp
    val subtitleAlpha = (1f - collapseProgress * 0.4f).coerceIn(0.58f, 1f)
    val subtitleBlockIndent = 32.dp - (1.dp * collapseProgress)
    val subtitleIndent = subtitleBlockIndent + (14.dp - (2.dp * collapseProgress))
    val tertiarySubtitleIndent = subtitleBlockIndent + (36.dp - (3.dp * collapseProgress))

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "ShadeMates",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = titleSize,
                lineHeight = titleSize * 0.96f,
            ),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
        )
        Column(
            modifier = Modifier.padding(start = subtitleBlockIndent),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Smarter Routes.",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = subtitleSize,
                    lineHeight = subtitleSize * 1.08f,
                ),
                color = Color.White.copy(alpha = subtitleAlpha),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Cooler Journeys.",
                modifier = Modifier.padding(start = subtitleIndent),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = subtitleSize,
                    lineHeight = subtitleSize * 1.08f,
                ),
                color = Color.White.copy(alpha = subtitleAlpha),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Heat awareness",
                modifier = Modifier.padding(start = tertiarySubtitleIndent),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = subtitleSize,
                    lineHeight = subtitleSize * 1.08f,
                ),
                color = Color.White.copy(alpha = subtitleAlpha),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun HomeFilterTabsRow(
    activeCategory: HomeFilterCategory,
    onCategorySelected: (HomeFilterCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        HomeFilterCategory.entries.forEach { category ->
            val isSelected = category == activeCategory
            val labelColor = if (isSelected) Color(0xFF233248) else Color.White
            val iconSize = if (isSelected) 18.dp else 17.dp

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (isSelected) 72.dp else 56.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                if (isSelected) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp)
                            .height(72.dp),
                        onClick = { onCategorySelected(category) },
                        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.label,
                                tint = labelColor,
                                modifier = Modifier.size(iconSize),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 13.sp,
                                ),
                                color = labelColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { onCategorySelected(category) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.label,
                            tint = labelColor,
                            modifier = Modifier.size(iconSize),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 13.sp,
                            ),
                            color = labelColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds(),
    ) {
        AssetHeroImage(
            drawableName = "hero_top_background",
            fallbackRes = R.drawable.hero_top_background_placeholder,
        )
    }
}

@Composable
private fun AssetHeroImage(
    drawableName: String,
    @DrawableRes fallbackRes: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    fitEntireImage: Boolean = false,
    alignment: Alignment = Alignment.Center,
) {
    DrawableBackgroundImage(
        drawableName = drawableName,
        fallbackRes = fallbackRes,
        contentDescription = "ShadeMate hero image",
        modifier = modifier,
        fitEntireImage = fitEntireImage,
        alignment = alignment,
    )
}

@Composable
private fun DrawableBackgroundImage(
    drawableName: String,
    @DrawableRes fallbackRes: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fitEntireImage: Boolean = false,
    alignment: Alignment = Alignment.Center,
    fixedDecodeSize: IntSize? = null,
    onImageReady: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val resolvedDrawableRes = remember(drawableName, fallbackRes) {
        resolveDrawableResource(context, drawableName, fallbackRes)
    }
    var targetSize by remember { mutableStateOf(IntSize.Zero) }
    val decodeTargetSize = fixedDecodeSize ?: targetSize
    val isXmlResource = remember(resolvedDrawableRes) {
        context.resources.getResourceTypeName(resolvedDrawableRes).equals("xml", ignoreCase = true)
    }
    val sampledBitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = resolvedDrawableRes,
        key2 = decodeTargetSize,
        key3 = fitEntireImage,
    ) {
        if (isXmlResource) {
            value = null
            return@produceState
        }

        val displayMetrics = context.resources.displayMetrics
        val safeTargetWidth = if (decodeTargetSize.width > 0) decodeTargetSize.width else displayMetrics.widthPixels
        val safeTargetHeight = if (decodeTargetSize.height > 0) decodeTargetSize.height else (displayMetrics.heightPixels / 3).coerceAtLeast(1)

        value = withContext(kotlinx.coroutines.Dispatchers.IO) {
            decodeSampledBitmapFromResource(
                context = context,
                drawableRes = resolvedDrawableRes,
                reqWidth = safeTargetWidth,
                reqHeight = safeTargetHeight,
            )
        }
    }
    val isImageReady = sampledBitmap != null || isXmlResource

    LaunchedEffect(isImageReady) {
        onImageReady(isImageReady)
    }

    Box(modifier = modifier.onSizeChanged { targetSize = it }) {
        when {
            sampledBitmap != null -> {
                Image(
                    bitmap = sampledBitmap!!.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = if (fitEntireImage) ContentScale.Fit else ContentScale.Crop,
                    alignment = alignment,
                )
            }

            isXmlResource -> {
                Image(
                    painter = painterResource(id = resolvedDrawableRes),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = if (fitEntireImage) ContentScale.Fit else ContentScale.Crop,
                    alignment = alignment,
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ShadeMatePalette.AppBackground),
                )
            }
        }
    }
}

private fun resolveDrawableResource(
    context: Context,
    drawableName: String,
    @DrawableRes fallbackRes: Int,
): Int {
    val resolved = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    return if (resolved != 0) resolved else fallbackRes
}

private fun decodeSampledBitmapFromResource(
    context: Context,
    @DrawableRes drawableRes: Int,
    reqWidth: Int,
    reqHeight: Int,
) = BitmapFactory.Options().run {
    inJustDecodeBounds = true
    BitmapFactory.decodeResource(context.resources, drawableRes, this)

    inSampleSize = calculateInSampleSize(
        originalWidth = outWidth,
        originalHeight = outHeight,
        reqWidth = reqWidth.coerceAtLeast(1),
        reqHeight = reqHeight.coerceAtLeast(1),
    )
    inJustDecodeBounds = false
    inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
    BitmapFactory.decodeResource(context.resources, drawableRes, this)
}

private fun calculateInSampleSize(
    originalWidth: Int,
    originalHeight: Int,
    reqWidth: Int,
    reqHeight: Int,
): Int {
    var inSampleSize = 1
    if (originalHeight > reqHeight || originalWidth > reqWidth) {
        val widthRatio = (originalWidth.toFloat() / reqWidth.toFloat()).toInt().coerceAtLeast(1)
        val heightRatio = (originalHeight.toFloat() / reqHeight.toFloat()).toInt().coerceAtLeast(1)
        val rawSample = maxOf(widthRatio, heightRatio)
        while (inSampleSize < rawSample) {
            inSampleSize *= 2
        }
    }
    return inSampleSize.coerceAtLeast(1)
}

@Composable
private fun RecommendedPlacesSection(
    state: LoadState<List<CoolPlaceCardData>>,
    onRetry: () -> Unit,
    onPlaceSelected: (CoolPlaceCardData) -> Unit,
    animateCards: Boolean = false,
    animationBaseIndex: Int = 0,
) {
    var selectedCategory by rememberSaveable { mutableStateOf<CoolPlaceCategory?>(CoolPlaceCategory.OUTDOOR) }
    val recommendedPlace = remember(state, selectedCategory) {
        val places = (state as? LoadState.Success)?.data.orEmpty()
        places
            .filter { selectedCategory == null || it.categories.contains(selectedCategory) }
            .minByOrNull { it.distanceMeters }
    }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle("Recommended Place")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            listOf(
                CoolPlaceCategory.OUTDOOR,
                CoolPlaceCategory.PARK,
                CoolPlaceCategory.INDOOR,
                CoolPlaceCategory.AC,
                CoolPlaceCategory.WATER_FOUNTAIN,
                CoolPlaceCategory.OTHER,
            ).forEach { category ->
                FilterChipPill(
                    label = category.label,
                    isSelected = selectedCategory == category,
                    onClick = {
                        selectedCategory = if (selectedCategory == category) null else category
                    },
                )
            }
        }
        when (state) {
            LoadState.Loading -> {
                SectionStatusCard(
                    title = "Loading nearby cool places",
                    message = "Getting the latest recommended place list for the home screen.",
                    loading = true,
                    modifier = Modifier.homeFilterCardEntrance(
                        index = animationBaseIndex,
                        enabled = animateCards,
                    ),
                )
            }

            LoadState.Empty -> EmptyRecommendedPlaceCard(
                modifier = Modifier.homeFilterCardEntrance(
                    index = animationBaseIndex,
                    enabled = animateCards,
                ),
            )

            is LoadState.Error -> {
                SectionStatusCard(
                    title = "Could not load cool places",
                    message = state.message,
                    actionLabel = "Retry",
                    onAction = onRetry,
                    modifier = Modifier.homeFilterCardEntrance(
                        index = animationBaseIndex,
                        enabled = animateCards,
                    ),
                )
            }

            is LoadState.Success -> {
                if (recommendedPlace != null) {
                    Box(
                        modifier = Modifier.homeFilterCardEntrance(
                            index = animationBaseIndex,
                            enabled = animateCards,
                        ),
                    ) {
                        CoolPlaceCard(
                            place = recommendedPlace,
                            onClick = { onPlaceSelected(recommendedPlace) },
                        )
                    }
                } else {
                    EmptyRecommendedPlaceCard(
                        modifier = Modifier.homeFilterCardEntrance(
                            index = animationBaseIndex,
                            enabled = animateCards,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(138.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) ShadeMatePalette.ChipSelected else ShadeMatePalette.ChipBackground,
        border = BorderStroke(1.dp, if (isSelected) ShadeMatePalette.PrimaryGreen else ShadeMatePalette.Border),
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = if (isSelected) ShadeMatePalette.PrimaryGreen else Color(0xFF4E555B),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyRecommendedPlaceCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ShadeMatePalette.CardBackground),
        border = BorderStroke(1.dp, ShadeMatePalette.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "No recommended place found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ShadeMatePalette.PrimaryText,
            )
            Text(
                text = "Try a different category to preview another nearby cool place.",
                style = MaterialTheme.typography.bodyMedium,
                color = ShadeMatePalette.SecondaryText,
            )
        }
    }
}

@Composable
private fun HeatUvSection(
    state: LoadState<HomeHeatOverview>,
    onRetry: () -> Unit,
    onWeatherCardClick: () -> Unit,
    onWeatherCardBoundsChanged: (Rect) -> Unit,
    weatherCardCollapsedAlpha: Float,
    animateCards: Boolean = false,
    animationBaseIndex: Int = 0,
) {
    BoxWithConstraints {
        val compact = maxWidth <= 360.dp
        Column(
            modifier = Modifier.padding(horizontal = if (compact) 18.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("Heat & UV Right Now")
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(if (compact) 22.dp else 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh data",
                            tint = ShadeMatePalette.PrimaryGreen,
                            modifier = Modifier.size(if (compact) 18.dp else 20.dp),
                        )
                    }
                }
                Text(
                    text = "Current conditions and quick guidance before you head out.",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (compact) 12.sp else 13.sp,
                        lineHeight = if (compact) 16.sp else 18.sp,
                    ),
                    color = ShadeMatePalette.SecondaryText,
                    fontWeight = FontWeight.Normal,
                )
            }
        when (state) {
            LoadState.Loading -> {
                SectionStatusCard(
                    title = "Loading heat overview",
                    message = "Fetching UV and suburb temperature summaries.",
                    loading = true,
                    modifier = Modifier.homeFilterCardEntrance(
                        index = animationBaseIndex,
                        enabled = animateCards,
                    ),
                )
            }

            LoadState.Empty -> {
                SectionStatusCard(
                    title = "No heat overview available",
                    message = "The home screen can still open the live map while the overview is unavailable.",
                    modifier = Modifier.homeFilterCardEntrance(
                        index = animationBaseIndex,
                        enabled = animateCards,
                    ),
                )
            }

            is LoadState.Error -> {
                SectionStatusCard(
                    title = "Could not load heat overview",
                    message = state.message,
                    actionLabel = "Retry",
                    onAction = onRetry,
                    modifier = Modifier.homeFilterCardEntrance(
                        index = animationBaseIndex,
                        enabled = animateCards,
                    ),
                )
            }

            is LoadState.Success -> {
                val data = state.data
                Box(
                    modifier = Modifier.homeFilterCardEntrance(
                        index = animationBaseIndex,
                        enabled = animateCards,
                    ),
                ) {
                    CompactUvOverviewCard(data = state.data)
                }

                Box(
                    modifier = Modifier.homeFilterCardEntrance(
                        index = animationBaseIndex + 1,
                        enabled = animateCards,
                    ),
                ) {
                    CurrentLocationTemperatureImageCard(
                        weather = data.currentLocationWeather,
                        onClick = onWeatherCardClick,
                        onBoundsChanged = onWeatherCardBoundsChanged,
                        collapsedAlpha = weatherCardCollapsedAlpha,
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun CardImageBackground(
    drawableName: String,
    @DrawableRes fallbackRes: Int,
    overlayAlpha: Float,
    overlayBrush: Brush,
    modifier: Modifier = Modifier,
    fixedDecodeSize: IntSize? = null,
    onImageReady: (Boolean) -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        DrawableBackgroundImage(
            drawableName = drawableName,
            fallbackRes = fallbackRes,
            modifier = Modifier.fillMaxSize(),
            fixedDecodeSize = fixedDecodeSize,
            onImageReady = onImageReady,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayBrush),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha * 0.36f)),
        )
    }
}

@Composable
private fun CompactUvOverviewCard(
    data: HomeHeatOverview,
) {
    val uvIndex = data.uvIndex ?: 0
    val severityColor = when {
        uvIndex < 3 -> ShadeMatePalette.LowUvGreen
        uvIndex < 8 -> ShadeMatePalette.WarningOrange
        else -> ShadeMatePalette.DangerHeatRed
    }

    BoxWithConstraints {
        val compact = maxWidth <= 360.dp
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 176.dp else 184.dp),
            shape = PremiumCardShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FAF5)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.86f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF8FCF9),
                                Color(0xFFEEF7F1),
                                Color(0xFFE8F4EC),
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(900f, 700f),
                        ),
                    ),
            ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 22.dp, y = (-18).dp)
                    .size(108.dp)
                    .background(Color.White.copy(alpha = 0.34f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-14).dp, y = 16.dp)
                    .size(84.dp)
                    .background(Color.White.copy(alpha = 0.22f), CircleShape),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (compact) 16.dp else 20.dp, vertical = if (compact) 16.dp else 18.dp),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier
                        .width(if (compact) 70.dp else 76.dp)
                        .height(if (compact) 88.dp else 94.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = data.uvIndex?.toString() ?: "--",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = if (compact) 26.sp else 28.sp,
                                lineHeight = if (compact) 28.sp else 30.sp,
                            ),
                            color = ShadeMatePalette.PrimaryText,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                ) {
                    Text(
                        text = "UV Index",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = if (compact) 20.sp else 22.sp,
                            lineHeight = if (compact) 22.sp else 24.sp,
                        ),
                        color = ShadeMatePalette.PrimaryText,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = severityColor.copy(alpha = 0.14f),
                    ) {
                        Text(
                            text = data.uvLabel,
                            modifier = Modifier.padding(horizontal = if (compact) 14.dp else 16.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = if (compact) 13.sp else 14.sp),
                            color = severityColor,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Text(
                        text = "Stay sun safe before you head out.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (compact) 12.sp else 13.sp,
                            lineHeight = if (compact) 16.sp else 18.sp,
                        ),
                        color = ShadeMatePalette.SecondaryText,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun CurrentLocationTemperatureImageCard(
    weather: CurrentLocationWeather?,
    onClick: () -> Unit,
    onBoundsChanged: (Rect) -> Unit,
    collapsedAlpha: Float,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(collapsedAlpha)
            .onGloballyPositioned { coordinates ->
                onBoundsChanged(coordinates.boundsInRoot())
            },
        onClick = onClick,
        shape = PremiumCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        CurrentLocationTemperatureCard(
            weather = weather,
            expanded = false,
            onClose = null,
        )
    }
}

@Composable
private fun OverlayContentRevealBlock(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + slideInVertically(
            animationSpec = tween(240, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 6 },
        ),
        exit = fadeOut(animationSpec = tween(120)),
    ) {
        content()
    }
}

@Composable
private fun ExpandedWeatherCardOverlay(
    state: LoadState<HomeHeatOverview>,
    sourceBounds: Rect?,
    rootSize: IntSize,
    expanded: Boolean,
    onDismiss: () -> Unit,
) {
    if (sourceBounds == null || rootSize == IntSize.Zero) return

    val density = LocalDensity.current
    val screenWidth = with(density) { rootSize.width.toDp() }
    val screenHeight = with(density) { rootSize.height.toDp() }
    val targetWidth = screenWidth - 32.dp
    val weather = (state as? LoadState.Success)?.data?.currentLocationWeather
    val estimatedExpandedHeight = estimateWeatherExpandedHeight(weather)
    val targetHeight = estimatedExpandedHeight
        .coerceAtMost(screenHeight * 0.60f)
        .coerceAtLeast(WeatherExpandedMinHeight)
    val targetX = (screenWidth - targetWidth) / 2f
    val targetY = (screenHeight - targetHeight) / 2f
    val sourceX = with(density) { sourceBounds.left.toDp() }
    val sourceY = with(density) { sourceBounds.top.toDp() }
    val sourceWidth = with(density) { sourceBounds.width.toDp() }
    val sourceHeight = with(density) { sourceBounds.height.toDp() }
    val backgroundDecodeSize = remember(targetWidth, targetHeight, density) {
        IntSize(
            with(density) { targetWidth.toPx().roundToInt() },
            with(density) { targetHeight.toPx().roundToInt() },
        )
    }

    val animatedWidth by animateDpAsState(
        targetValue = if (expanded) targetWidth else sourceWidth,
        animationSpec = tween(durationMillis = WEATHER_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "weather_overlay_width",
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (expanded) targetHeight else sourceHeight,
        animationSpec = tween(durationMillis = WEATHER_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "weather_overlay_height",
    )
    val animatedX by animateDpAsState(
        targetValue = if (expanded) targetX else sourceX,
        animationSpec = tween(durationMillis = WEATHER_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "weather_overlay_x",
    )
    val animatedY by animateDpAsState(
        targetValue = if (expanded) targetY else sourceY,
        animationSpec = tween(durationMillis = WEATHER_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "weather_overlay_y",
    )
    val dimAlpha by animateFloatAsState(
        targetValue = if (expanded) 0.40f else 0f,
        animationSpec = tween(durationMillis = WEATHER_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "weather_overlay_dim",
    )
    val cardElevation by animateDpAsState(
        targetValue = if (expanded) 26.dp else 8.dp,
        animationSpec = tween(durationMillis = WEATHER_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "weather_overlay_elevation",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.976f,
        animationSpec = tween(durationMillis = WEATHER_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "weather_overlay_scale",
    )
    val cardLiftOffset by animateDpAsState(
        targetValue = if (expanded) 0.dp else 12.dp,
        animationSpec = tween(durationMillis = WEATHER_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "weather_overlay_lift",
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.97f,
        animationSpec = tween(durationMillis = WEATHER_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "weather_overlay_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = dimAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        Card(
            modifier = Modifier
                .offset(x = animatedX, y = animatedY)
                .width(animatedWidth)
                .height(animatedHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                    translationY = with(density) { cardLiftOffset.toPx() }
                    alpha = cardAlpha
                },
            shape = PremiumCardShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        ) {
            CurrentLocationTemperatureCard(
                weather = weather,
                expanded = true,
                fixedBackgroundDecodeSize = backgroundDecodeSize,
                onClose = onDismiss,
            )
        }
    }
}

@Composable
private fun CurrentLocationTemperatureCard(
    weather: CurrentLocationWeather?,
    expanded: Boolean,
    fixedBackgroundDecodeSize: IntSize? = null,
    onClose: (() -> Unit)?,
) {
    var isBackgroundReady by remember(expanded) { mutableStateOf(!expanded) }
    var revealStep by remember(expanded) { mutableStateOf(0) }

    LaunchedEffect(expanded, isBackgroundReady) {
        revealStep = 0
        if (expanded && isBackgroundReady) {
            revealStep = 1
            delay(85)
            revealStep = 2
            delay(85)
            revealStep = 3
            delay(85)
            revealStep = 4
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val compact = maxWidth <= 360.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (expanded) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.height(if (compact) 196.dp else WeatherCollapsedCardHeight)
                    }
                ),
        ) {
            CardImageBackground(
                drawableName = "weather_card_background",
                fallbackRes = R.drawable.weather_card_background_placeholder,
                overlayAlpha = WEATHER_CARD_IMAGE_OVERLAY_ALPHA,
                overlayBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF173336).copy(alpha = 0.22f),
                        Color(0xFF244B50).copy(alpha = 0.44f),
                        Color(0xFF0F1C20).copy(alpha = 0.7f),
                    ),
                    start = Offset(960f, 80f),
                    end = Offset(120f, 760f),
                ),
                fixedDecodeSize = fixedBackgroundDecodeSize,
                onImageReady = { isBackgroundReady = it },
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = (-6).dp)
                    .size(if (compact) 60.dp else 68.dp)
                    .background(
                        color = ShadeMatePalette.AccentAqua.copy(alpha = 0.10f),
                        shape = CircleShape,
                    ),
            )
            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF102129).copy(alpha = 0.18f),
                                    Color(0xFF0A1218).copy(alpha = 0.62f),
                                ),
                                startY = 0f,
                                endY = 920f,
                            ),
                        ),
                )
            }

            if (onClose != null) {
                OverlayContentRevealBlock(
                    modifier = Modifier
                        .zIndex(2f)
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp),
                    visible = !expanded || revealStep >= 1,
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.18f), CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close weather details",
                            tint = Color.White,
                        )
                    }
                }
            }

            if (expanded) {
                OverlayContentRevealBlock(
                    modifier = Modifier
                        .zIndex(1f)
                        .align(Alignment.TopStart),
                    visible = revealStep >= 1,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = if (compact) 20.dp else 22.dp, top = if (compact) 18.dp else 20.dp, end = 56.dp),
                        verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 6.dp),
                    ) {
                        Text(
                            text = "Weather / Temperature",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = if (compact) 22.sp else 24.sp,
                                lineHeight = if (compact) 24.sp else 26.sp,
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = weather?.areaName ?: "Current location",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = if (compact) 15.sp else 16.sp),
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                ExpandedCurrentLocationTemperatureContent(
                    weather = weather,
                    revealStep = revealStep,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = if (compact) 18.dp else 20.dp, top = if (compact) 16.dp else 18.dp, end = if (compact) 18.dp else 20.dp, bottom = if (compact) 36.dp else 42.dp),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Weather / Temperature",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = if (compact) 19.sp else 20.sp,
                                lineHeight = if (compact) 21.sp else 22.sp,
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = weather?.areaName ?: "Current location",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = if (compact) 15.sp else 16.sp),
                            color = Color.White.copy(alpha = 0.92f),
                            fontWeight = FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                text = weather?.currentCelsius?.toString() ?: "--",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = if (compact) 58.sp else 64.sp,
                                    lineHeight = if (compact) 56.sp else 62.sp,
                                ),
                                color = Color(0xFFF7FCFB),
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                text = "\u00B0",
                                modifier = Modifier.padding(top = if (compact) 6.dp else 8.dp),
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = if (compact) 22.sp else 24.sp),
                                color = ShadeMatePalette.AccentAqua,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TemperatureExtremeChip(label = "HIGH", value = weather?.highCelsius)
                            TemperatureExtremeChip(label = "LOW", value = weather?.lowCelsius)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .padding(horizontal = if (compact) 2.dp else 0.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(14.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "View more",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = if (compact) 9.sp else 10.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = Color.White.copy(alpha = 0.94f),
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Expand weather details",
                            tint = Color.White,
                            modifier = Modifier
                                .size(if (compact) 12.dp else 13.dp)
                                .graphicsLayer { rotationZ = 90f },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedCurrentLocationTemperatureContent(
    weather: CurrentLocationWeather?,
    revealStep: Int,
) {
    val contentScrollState = rememberScrollState()
    BoxWithConstraints {
        val compact = maxWidth <= 360.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(contentScrollState)
                .padding(horizontal = if (compact) 20.dp else 22.dp, vertical = if (compact) 16.dp else 18.dp)
                .padding(top = if (compact) 72.dp else WeatherExpandedContentTopPadding, bottom = if (compact) 16.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else WeatherExpandedMetricSpacing),
        ) {
        OverlayContentRevealBlock(visible = revealStep >= 2) {
            WeatherMetricPanel(
                title = "Shade Coverage Score",
                value = formatWeatherScore(weather?.shadeCoverageScore),
                accentColor = Color(0xFF7EF4D5),
                description = "A softer score for how much cover is available around you.",
            )
        }
        OverlayContentRevealBlock(visible = revealStep >= 3) {
            WeatherMetricPanel(
                title = "Heat Exposure Score",
                value = formatWeatherScore(weather?.heatExposureScore),
                accentColor = Color(0xFFFFB57A),
                description = "A quick indication of outdoor heat stress right now.",
            )
        }
        OverlayContentRevealBlock(visible = revealStep >= 4) {
            WeatherMetricPanel(
                title = "Feels-like Temperature",
                value = formatExpandedTemperature(weather?.feelsLikeCelsius),
                accentColor = ShadeMatePalette.AccentAqua,
                description = "Live value from OpenWeather current conditions.",
            )
        }
        OverlayContentRevealBlock(visible = revealStep >= 4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Weather hint",
                    tint = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.size(if (compact) 14.dp else 15.dp),
                )
                Text(
                    text = "Based on your nearest outdoor 2 meter radius around your location.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = if (compact) 10.sp else 11.sp,
                        lineHeight = if (compact) 13.sp else 14.sp,
                    ),
                    color = Color.White.copy(alpha = 0.72f),
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                )
            }
        }
    }
    }
}

private fun estimateWeatherExpandedHeight(weather: CurrentLocationWeather?): androidx.compose.ui.unit.Dp {
    val descriptions = listOf(
        "A softer score for how much cover is available around you.",
        "A quick indication of outdoor heat stress right now.",
        "Live value from OpenWeather current conditions.",
    )
    val headerRows = 2
    val areaNameRows = kotlin.math.ceil((weather?.areaName?.length ?: 0) / 26f).toInt().coerceAtLeast(1)
    val descriptionRows = descriptions.sumOf { description ->
        kotlin.math.ceil(description.length / 34f).toInt().coerceAtLeast(1)
    }
    val panelHeight = (WeatherExpandedPanelBaseHeight.value * 3).dp
    val extraDescriptionHeight = (descriptionRows * 8).dp
    val headerHeight = (headerRows * 24 + areaNameRows * 18).dp
    val spacingHeight = (WeatherExpandedMetricSpacing.value * 2).dp
    return WeatherExpandedMinHeight
        .coerceAtLeast(
            headerHeight +
                panelHeight +
                extraDescriptionHeight +
                spacingHeight +
                WeatherExpandedHintHeight +
                WeatherExpandedExtraSpace
        )
}

@Composable
private fun WeatherMetricPanel(
    title: String,
    value: String,
    accentColor: Color,
    description: String,
) {
    BoxWithConstraints {
        val compact = maxWidth <= 360.dp
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (compact) 14.dp else 16.dp, vertical = if (compact) 13.dp else 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 5.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = if (compact) 14.sp else 15.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (compact) 11.sp else 12.sp,
                            lineHeight = if (compact) 15.sp else 16.sp,
                        ),
                        color = Color.White.copy(alpha = 0.72f),
                        fontWeight = FontWeight.Normal,
                        maxLines = 3,
                    )
                }
                Spacer(modifier = Modifier.width(if (compact) 10.dp else 14.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = if (compact) 24.sp else 28.sp,
                        lineHeight = if (compact) 26.sp else 30.sp,
                    ),
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun TemperatureExtremeChip(
    label: String,
    value: Int?,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                color = ShadeMatePalette.AccentAqua,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = formatCurrentCardTemperature(value),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 16.sp),
                color = Color(0xFFF7FCFB),
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun FeaturedCoolPlaceCard(
    place: CoolPlaceCardData,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(286.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = ShadeMatePalette.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CoolPlaceImage(
                place = place,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF18322C).copy(alpha = 0.18f),
                                Color(0xFF152925).copy(alpha = 0.84f),
                            ),
                        ),
                    ),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.78f),
            ) {
                Text(
                    text = place.travelTimeText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = ShadeMatePalette.PrimaryText,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 18.dp, vertical = 18.dp)
                    .padding(end = 72.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                )
                Text(
                    text = place.suburb,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.92f),
                )
                Text(
                    text = place.tagList.take(3).joinToString(" | "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 2,
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp)
                    .size(48.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                onClick = onClick,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open recommended place",
                        tint = ShadeMatePalette.PrimaryGreen,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickTipsCard(
    state: LoadState<HomeHeatOverview>,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ShadeMatePalette.CardBackground),
        border = BorderStroke(1.dp, ShadeMatePalette.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle("Quick Tips for Now")
            when (state) {
                LoadState.Loading -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = ShadeMatePalette.PrimaryGreen,
                        )
                        Text(
                            text = "Loading guidance...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShadeMatePalette.SecondaryText,
                        )
                    }
                }

                LoadState.Empty -> {
                    Text(
                        text = "Tips will appear here when the home overview service returns guidance.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ShadeMatePalette.SecondaryText,
                    )
                }

                is LoadState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ShadeMatePalette.DangerHeatRed,
                    )
                    TextButton(onClick = onRetry) {
                        Text("Retry", color = ShadeMatePalette.PrimaryGreen)
                    }
                }

                is LoadState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.data.quickTips.forEach { tip ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = ShadeMatePalette.AppBackground,
                                border = BorderStroke(1.dp, ShadeMatePalette.Border.copy(alpha = 0.8f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .background(ShadeMatePalette.SecondaryTeal, CircleShape),
                                    )
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = ShadeMatePalette.PrimaryText,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedCoolPlaceLifestyleCard(
    place: CoolPlaceCardData,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(326.dp)
            .clickable(onClick = onClick),
        shape = PremiumCardShape,
        colors = CardDefaults.cardColors(containerColor = ShadeMatePalette.CardBackground),
        border = BorderStroke(1.dp, ShadeMatePalette.Border.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            ) {
                CoolPlaceImage(
                    place = place,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = ShadeMatePalette.PrimaryText,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                    )
                    Text(
                        text = place.suburb,
                        style = MaterialTheme.typography.titleMedium,
                        color = ShadeMatePalette.SecondaryText,
                    )
                    Text(
                        text = place.tagList.take(3).joinToString(" | "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShadeMatePalette.SecondaryText,
                        maxLines = 2,
                    )
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ShadeMatePalette.SelectedNavBackground,
                    ) {
                        Text(
                            text = place.travelTimeText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = ShadeMatePalette.PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = ShadeMatePalette.PrimaryGreen,
                    onClick = onClick,
                    shadowElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open recommended place",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTipsImageCard(
    state: LoadState<HomeHeatOverview>,
    onRetry: () -> Unit,
    onClick: () -> Unit,
    onBoundsChanged: (Rect) -> Unit,
    collapsedAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .alpha(collapsedAlpha)
            .onGloballyPositioned { coordinates ->
                onBoundsChanged(coordinates.boundsInRoot())
            },
        onClick = onClick,
        shape = PremiumCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        TipsCardBody(
            state = state,
            onRetry = onRetry,
            expanded = false,
            onClose = null,
        )
    }
}

@Composable
private fun ExpandedTipsCardOverlay(
    state: LoadState<HomeHeatOverview>,
    onRetry: () -> Unit,
    sourceBounds: Rect?,
    rootSize: IntSize,
    expanded: Boolean,
    onDismiss: () -> Unit,
) {
    if (sourceBounds == null || rootSize == IntSize.Zero) return

    val density = LocalDensity.current
    val screenWidth = with(density) { rootSize.width.toDp() }
    val screenHeight = with(density) { rootSize.height.toDp() }
    val targetWidth = screenWidth - 32.dp
    val maxTargetHeight = screenHeight * 0.6f
    val estimatedExpandedHeight = when (state) {
        is LoadState.Success -> {
            val totalCharacters = state.data.quickTips.sumOf { it.length }
            val estimatedTipRows = (totalCharacters / 34f).coerceAtLeast(state.data.quickTips.size.toFloat())
            val interTipSpacing = TipsExpandedItemSpacing * (state.data.quickTips.size - 1).coerceAtLeast(0)
            TipsExpandedHeroHeight + 112.dp + (estimatedTipRows * 24f).dp + interTipSpacing + TipsExpandedExtraSpace
        }

        is LoadState.Error -> TipsExpandedHeroHeight + 168.dp
        LoadState.Empty -> TipsExpandedHeroHeight + 148.dp
        LoadState.Loading -> TipsExpandedHeroHeight + 132.dp
    }
    val targetHeight = estimatedExpandedHeight
        .coerceAtLeast(TipsExpandedMinHeight)
        .coerceAtMost(maxTargetHeight)
    val targetX = (screenWidth - targetWidth) / 2f
    val targetY = (screenHeight - targetHeight) / 2f
    val sourceX = with(density) { sourceBounds.left.toDp() }
    val sourceY = with(density) { sourceBounds.top.toDp() }
    val sourceWidth = with(density) { sourceBounds.width.toDp() }
    val sourceHeight = with(density) { sourceBounds.height.toDp() }
    val backgroundDecodeSize = remember(targetWidth, targetHeight, density) {
        IntSize(
            with(density) { targetWidth.toPx().roundToInt() },
            with(density) { targetHeight.toPx().roundToInt() },
        )
    }

    val animatedWidth by animateDpAsState(
        targetValue = if (expanded) targetWidth else sourceWidth,
        animationSpec = tween(durationMillis = TIPS_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "tips_overlay_width",
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (expanded) targetHeight else sourceHeight,
        animationSpec = tween(durationMillis = TIPS_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "tips_overlay_height",
    )
    val animatedX by animateDpAsState(
        targetValue = if (expanded) targetX else sourceX,
        animationSpec = tween(durationMillis = TIPS_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "tips_overlay_x",
    )
    val animatedY by animateDpAsState(
        targetValue = if (expanded) targetY else sourceY,
        animationSpec = tween(durationMillis = TIPS_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "tips_overlay_y",
    )
    val dimAlpha by animateFloatAsState(
        targetValue = if (expanded) 0.42f else 0f,
        animationSpec = tween(durationMillis = TIPS_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "tips_overlay_dim",
    )
    val cardElevation by animateDpAsState(
        targetValue = if (expanded) 26.dp else 7.dp,
        animationSpec = tween(durationMillis = TIPS_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "tips_overlay_elevation",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.976f,
        animationSpec = tween(durationMillis = TIPS_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "tips_overlay_scale",
    )
    val cardLiftOffset by animateDpAsState(
        targetValue = if (expanded) 0.dp else 12.dp,
        animationSpec = tween(durationMillis = TIPS_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "tips_overlay_lift",
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.97f,
        animationSpec = tween(durationMillis = TIPS_EXPAND_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "tips_overlay_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = dimAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        Card(
            modifier = Modifier
                .offset(x = animatedX, y = animatedY)
                .width(animatedWidth)
                .height(animatedHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                    translationY = with(density) { cardLiftOffset.toPx() }
                    alpha = cardAlpha
                },
            shape = PremiumCardShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        ) {
            TipsCardBody(
                state = state,
                onRetry = onRetry,
                expanded = true,
                fixedBackgroundDecodeSize = backgroundDecodeSize,
                onClose = onDismiss,
            )
        }
    }
}

@Composable
private fun TipsCardBody(
    state: LoadState<HomeHeatOverview>,
    onRetry: () -> Unit,
    expanded: Boolean,
    fixedBackgroundDecodeSize: IntSize? = null,
    onClose: (() -> Unit)?,
) {
    val contentScrollState = rememberScrollState()
    val expandedTipCount = when (state) {
        is LoadState.Success -> state.data.quickTips.count { it.isNotBlank() }
        is LoadState.Error -> 2
        else -> 1
    }
    val revealTargetStep = 1 + expandedTipCount.coerceAtLeast(1)
    var isBackgroundReady by remember(expanded) { mutableStateOf(!expanded) }
    var revealStep by remember(expanded, state) { mutableStateOf(0) }

    LaunchedEffect(expanded, state, isBackgroundReady) {
        revealStep = 0
        if (expanded && isBackgroundReady) {
            for (step in 1..revealTargetStep) {
                revealStep = step
                if (step < revealTargetStep) {
                    delay(85)
                }
            }
        }
    }

    BoxWithConstraints {
        val compact = maxWidth <= 360.dp
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(),
            ) {
                CardImageBackground(
                    drawableName = "tips_card_background",
                    fallbackRes = R.drawable.tips_card_background_placeholder,
                    overlayAlpha = TIPS_CARD_IMAGE_OVERLAY_ALPHA,
                    overlayBrush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF19312A).copy(alpha = 0.22f),
                            ShadeMatePalette.PrimaryGreen.copy(alpha = 0.30f),
                            Color(0xFF10201B).copy(alpha = 0.54f),
                        ),
                        start = Offset(960f, 80f),
                        end = Offset(80f, 920f),
                    ),
                    fixedDecodeSize = fixedBackgroundDecodeSize,
                    onImageReady = { isBackgroundReady = it },
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF10201B).copy(alpha = 0.22f),
                                    Color(0xFF10201B).copy(alpha = 0.72f),
                                ),
                                startY = 0f,
                                endY = 1400f,
                            ),
                        ),
                )

                OverlayContentRevealBlock(
                    modifier = Modifier
                        .zIndex(1f)
                        .align(Alignment.TopStart),
                    visible = revealStep >= 1,
                ) {
                    Text(
                        text = "Quick Tips",
                        modifier = Modifier
                            .padding(start = if (compact) 20.dp else 22.dp, top = if (compact) 18.dp else 20.dp, end = 56.dp),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = if (compact) 22.sp else 24.sp,
                            lineHeight = if (compact) 24.sp else 26.sp,
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                }

                if (onClose != null) {
                    OverlayContentRevealBlock(
                        modifier = Modifier
                            .zIndex(2f)
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 12.dp),
                        visible = revealStep >= 1,
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.18f), CircleShape),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close quick tips",
                                tint = Color.White,
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(contentScrollState)
                        .padding(horizontal = if (compact) 20.dp else 22.dp, vertical = if (compact) 16.dp else 18.dp)
                        .padding(top = if (compact) 52.dp else TipsExpandedContentTopPadding, bottom = if (compact) 16.dp else 18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TipsCardContent(
                        state = state,
                        onRetry = onRetry,
                        expanded = true,
                        revealStep = revealStep,
                        compact = compact,
                    )
                }
            }
            return@BoxWithConstraints
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 220.dp else QuickTipsCollapsedCardHeight),
        ) {
        CardImageBackground(
            drawableName = "tips_card_background",
            fallbackRes = R.drawable.tips_card_background_placeholder,
            overlayAlpha = TIPS_CARD_IMAGE_OVERLAY_ALPHA,
            overlayBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF19312A).copy(alpha = 0.24f),
                    ShadeMatePalette.PrimaryGreen.copy(alpha = 0.34f),
                    Color(0xFF10201B).copy(alpha = 0.66f),
                ),
                start = Offset(960f, 80f),
                end = Offset(80f, 820f),
            ),
        )

        Column(
            modifier = Modifier.padding(start = if (compact) 18.dp else 20.dp, top = if (compact) 16.dp else 18.dp, end = if (compact) 18.dp else 20.dp, bottom = if (compact) 34.dp else 36.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp),
        ) {
            Text(
                text = "Quick Tips",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = if (compact) 19.sp else 20.sp,
                    lineHeight = if (compact) 21.sp else 22.sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            TipsCardContent(
                state = state,
                onRetry = onRetry,
                expanded = false,
                compact = compact,
            )
        }

        if (!expanded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "View more",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (compact) 9.sp else 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color.White.copy(alpha = 0.94f),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Expand quick tips",
                        tint = Color.White,
                        modifier = Modifier
                            .size(if (compact) 12.dp else 13.dp)
                            .graphicsLayer { rotationZ = 90f },
                    )
                }
            }
        }

        if (expanded && onClose != null) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .zIndex(1f)
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .background(Color.Black.copy(alpha = 0.18f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close quick tips",
                    tint = Color.White,
                )
            }
        }
    }
    }
}

@Composable
private fun TipsCardContent(
    state: LoadState<HomeHeatOverview>,
    onRetry: () -> Unit,
    expanded: Boolean,
    revealStep: Int = Int.MAX_VALUE,
    compact: Boolean = false,
) {
    when (state) {
        LoadState.Loading -> {
            OverlayContentRevealBlock(visible = !expanded || revealStep >= 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = ShadeMatePalette.AccentAqua,
                    )
                    Text(
                        text = "Loading guidance...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = if (expanded) {
                                if (compact) 14.sp else 15.sp
                            } else {
                                if (compact) 11.5.sp else 12.sp
                            }
                        ),
                        color = Color.White.copy(alpha = 0.86f),
                    )
                }
            }
        }

        LoadState.Empty -> {
            OverlayContentRevealBlock(visible = !expanded || revealStep >= 2) {
                Text(
                    text = "Tips will appear here when the home overview service returns guidance.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = if (expanded) {
                            if (compact) 14.sp else 15.sp
                        } else {
                            if (compact) 12.5.sp else 13.sp
                        }
                    ),
                    color = Color.White.copy(alpha = 0.82f),
                )
            }
        }

        is LoadState.Error -> {
            OverlayContentRevealBlock(visible = !expanded || revealStep >= 2) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = if (expanded) {
                            if (compact) 14.sp else 15.sp
                        } else {
                            if (compact) 12.5.sp else 13.sp
                        }
                    ),
                    color = Color.White,
                )
            }
            OverlayContentRevealBlock(visible = !expanded || revealStep >= 3) {
                TextButton(onClick = onRetry) {
                    Text(
                        "Retry",
                        color = ShadeMatePalette.AccentAqua,
                        fontSize = if (expanded) {
                            if (compact) 15.sp else 16.sp
                        } else {
                            if (compact) 13.sp else 14.sp
                        },
                    )
                }
            }
        }

        is LoadState.Success -> {
            val allTips = state.data.quickTips.filter { it.isNotBlank() }
            val visibleTips = if (expanded) allTips else allTips.take(2)
            Column(verticalArrangement = Arrangement.spacedBy(if (expanded) TipsExpandedItemSpacing else if (compact) 6.dp else 8.dp)) {
                visibleTips.forEachIndexed { index, tip ->
                    OverlayContentRevealBlock(visible = !expanded || revealStep >= index + 2) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = if (expanded) {
                                            if (compact) 16.dp else 18.dp
                                        } else {
                                            if (compact) 14.dp else 16.dp
                                        },
                                        vertical = if (expanded) 12.dp else if (compact) 9.dp else 10.dp,
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = if (expanded) 7.dp else 6.dp)
                                        .size(if (expanded) 10.dp else if (compact) 8.dp else 9.dp)
                                        .background(ShadeMatePalette.AccentAqua, CircleShape),
                                )
                                Text(
                                    text = tip,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = if (expanded) {
                                            if (compact) 14.sp else 15.sp
                                        } else {
                                            if (compact) 12.5.sp else 13.sp
                                        },
                                        lineHeight = if (expanded) {
                                            if (compact) 21.sp else 23.sp
                                        } else {
                                            if (compact) 18.sp else 20.sp
                                        },
                                    ),
                                    color = Color.White,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = if (expanded) {
                                        Int.MAX_VALUE
                                    } else if (compact) {
                                        2
                                    } else if (index == 1) {
                                        1
                                    } else {
                                        2
                                    },
                                    overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                if (allTips.isEmpty()) {
                    OverlayContentRevealBlock(visible = !expanded || revealStep >= 2) {
                        Text(
                            text = "Tips will appear here when the home overview service returns guidance.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (expanded) {
                                    if (compact) 14.sp else 15.sp
                                } else {
                                    if (compact) 12.5.sp else 13.sp
                                }
                            ),
                            color = Color.White.copy(alpha = 0.82f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapPreviewSection(
    onOpenMap: () -> Unit,
    enableWeatherUpdates: Boolean,
    animateCard: Boolean = false,
    animationIndex: Int = 0,
) {
    BoxWithConstraints {
        val compact = maxWidth <= 360.dp
        Column(
            modifier = Modifier.padding(horizontal = if (compact) 18.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp),
        ) {
            SectionTitle("Nearby Heat Map")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 296.dp else 310.dp)
                    .homeFilterCardEntrance(
                        index = animationIndex,
                        enabled = animateCard,
                    ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ShadeMatePalette.CardBackground),
            border = BorderStroke(1.dp, ShadeMatePalette.Border),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                InteractiveMapScreen(
                    modifier = Modifier.fillMaxSize(),
                    showControls = false,
                    enableWeatherUpdates = enableWeatherUpdates,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, ShadeMatePalette.PrimaryGreen.copy(alpha = 0.22f)),
                            ),
                        ),
                )
                Button(
                    onClick = onOpenMap,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(if (compact) 12.dp else 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShadeMatePalette.PrimaryGreen),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        "Open Map",
                        color = ShadeMatePalette.CardBackground,
                        fontSize = if (compact) 13.sp else 14.sp,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                        tint = ShadeMatePalette.CardBackground,
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun SectionTitle(text: String) {
    BoxWithConstraints {
        val compact = maxWidth <= 360.dp
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = if (compact) 22.sp else 24.sp,
                lineHeight = if (compact) 26.sp else 30.sp,
            ),
            fontWeight = FontWeight.ExtraBold,
            color = ShadeMatePalette.PrimaryText,
        )
    }
}

private fun formatTemperature(value: Int?): String = value?.let { "${it}\u00B0C" } ?: "--"

@Composable
private fun SectionStatusCard(
    title: String,
    message: String,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ShadeMatePalette.CardBackground),
        border = BorderStroke(1.dp, ShadeMatePalette.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ShadeMatePalette.PrimaryText,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ShadeMatePalette.SecondaryText,
            )
            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = ShadeMatePalette.PrimaryGreen,
                    )
                }

                actionLabel != null && onAction != null -> {
                    TextButton(onClick = onAction) {
                        Text(actionLabel, color = ShadeMatePalette.PrimaryGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.homeFilterCardEntrance(
    index: Int,
    enabled: Boolean,
): Modifier {
    if (!enabled) return this

    var visible by remember(index) { mutableStateOf(false) }

    LaunchedEffect(index) {
        visible = false
        delay(index * 95L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "home_filter_card_alpha",
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "home_filter_card_translation",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.985f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "home_filter_card_scale",
    )

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun MapScreen(
    repository: HeatRepository,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf<LoadState<HeatMapData>>(LoadState.Loading) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            state = LoadState.Loading
            state = try {
                val result = repository.getHeatMapData()
                if (result.zones.isEmpty()) LoadState.Empty else LoadState.Success(result)
            } catch (exception: Exception) {
                LoadState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    LaunchedEffect(repository) { loadData() }

    MapScreenContent(
        state = state,
        onRetry = ::loadData,
        modifier = modifier,
    )
}

@Composable
private fun MapScreenContent(
    state: LoadState<HeatMapData>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var liveHeatZones by remember { mutableStateOf<List<HeatZone>>(emptyList()) }
    var selectedHeatLocation by remember { mutableStateOf<SelectedHeatLocation?>(null) }
    var clearSelectedHeatLocationToken by remember { mutableStateOf(0) }

    LaunchedEffect(state) {
        if (state is LoadState.Loading) {
            liveHeatZones = emptyList()
        }
    }

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        InteractiveMapScreen(
            modifier = Modifier.fillMaxSize(),
            showControls = true,
            isRouteModeActive = false,
            clearSelectedHeatLocationToken = clearSelectedHeatLocationToken,
            onHeatZonesUpdated = { zones ->
                liveHeatZones = zones
            },
            onSelectedHeatLocationChanged = { location ->
                selectedHeatLocation = location
            },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.22f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.08f),
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 108.dp),
        ) {
            selectedHeatLocation?.let { location ->
                DraggableSelectedHeatSheet(
                    location = location,
                    onDismiss = {
                        selectedHeatLocation = null
                        clearSelectedHeatLocationToken++
                    },
                )
            } ?: run {
                when (state) {
                    is LoadState.Loading -> FloatingMessageCard(
                        title = "Loading heat conditions",
                        message = "Fetching the latest suburb temperatures for Melbourne.",
                        loading = true,
                    )
                    is LoadState.Error -> FloatingMessageCard(
                        title = "Could not load heat data",
                        message = state.message,
                        actionLabel = "Retry",
                        onAction = onRetry,
                    )
                    is LoadState.Empty -> FloatingMessageCard(
                        title = "No heat data available",
                        message = "The map is still available, but there are no zone summaries to show right now.",
                    )
                    is LoadState.Success -> FloatingMessageCard(
                        title = "Tap the map to inspect a suburb",
                        message = "Select any area on the heat map to view its information.",
                    )
                }
            }
        }
    }
}

@Composable
private fun MapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<PlaceAutocompleteSuggestion>,
    isSearching: Boolean,
    errorMessage: String?,
    onSuggestionSelected: (PlaceAutocompleteSuggestion) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val shouldShowResults = query.isNotBlank() && (isSearching || suggestions.isNotEmpty() || errorMessage != null)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Where do you want to go?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = {
                        suggestions.firstOrNull()?.let { suggestion ->
                            focusManager.clearFocus()
                            onSuggestionSelected(suggestion)
                        }
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
        }

        if (shouldShowResults) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    when {
                        isSearching -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                                Text(
                                    text = "Searching places...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        errorMessage != null -> {
                            Text(
                                text = errorMessage,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        suggestions.isEmpty() -> {
                            Text(
                                text = "No matching places found.",
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        else -> {
                            suggestions.forEachIndexed { index, suggestion ->
                                SearchSuggestionRow(
                                    suggestion = suggestion,
                                    onClick = {
                                        focusManager.clearFocus()
                                        onSuggestionSelected(suggestion)
                                    },
                                )
                                if (index != suggestions.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 18.dp),
                                        color = Color(0xFFE0E0E0),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionRow(
    suggestion: PlaceAutocompleteSuggestion,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF55575A),
                maxLines = 1,
            )
            suggestion.formattedAddress?.takeIf { it.isNotBlank() }?.let { address ->
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
        formatDistanceLabel(suggestion.distanceMeters)?.let { distanceLabel ->
            Text(
                text = distanceLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun formatDistanceLabel(distanceMeters: Double?): String? {
    distanceMeters ?: return null
    return when {
        distanceMeters >= 1000 -> String.format("%.1f km", distanceMeters / 1000.0)
        distanceMeters >= 100 -> "${distanceMeters.toInt()} m"
        else -> null
    }
}

private fun formatCurrentCardTemperature(value: Int?): String {
    return value?.let { "${it}\u00B0" } ?: "--\u00B0"
}

private fun formatExpandedTemperature(value: Int?): String {
    return value?.let { "${it}\u00B0C" } ?: "--"
}

private fun formatWeatherScore(value: Int?): String {
    return value?.toString() ?: "--"
}

private fun hasHomeLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private suspend fun resolveCurrentCoordinates(context: Context): HomeCoordinates? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val enabledProviders = buildList {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            add(LocationManager.NETWORK_PROVIDER)
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            add(LocationManager.GPS_PROVIDER)
        }
        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            add(LocationManager.PASSIVE_PROVIDER)
        }
    }
    if (enabledProviders.isEmpty()) return null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        enabledProviders.forEach { provider ->
            val currentLocation = withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    locationManager.getCurrentLocation(
                        provider,
                        null,
                        ContextCompat.getMainExecutor(context),
                    ) { location ->
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }
                }
            }
            if (currentLocation != null) {
                return HomeCoordinates(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                )
            }
        }
    }

    return enabledProviders
        .mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
        .maxByOrNull { it.time }
        ?.let { location ->
            HomeCoordinates(
                latitude = location.latitude,
                longitude = location.longitude,
            )
        }
}

private suspend fun loadRouteOptions(
    walkingRouteRepository: WalkingRouteRepository,
    origin: Point,
    destination: Point,
): RouteScreenState {
    val walkingRouteResult = runCatching {
        walkingRouteRepository.getWalkingRouteOptions(origin, destination)
    }
    val recommendedRouteResult = runCatching {
        walkingRouteRepository.getRecommendedWalkingRoute(origin, destination)
    }

    val walkingRoutes = walkingRouteResult.getOrDefault(emptyList())
    val futureRecommendedRoute = recommendedRouteResult.getOrNull()
    if (walkingRoutes.isNotEmpty() || futureRecommendedRoute != null) {
        val coolRouteGeometry = walkingRoutes.firstOrNull() ?: futureRecommendedRoute
        val regularRouteGeometry = selectRegularRouteGeometry(
            coolGeometry = coolRouteGeometry,
            mapboxRoutes = walkingRoutes,
            futureRecommendedRoute = futureRecommendedRoute,
        )
        val routeWeatherSummary = runCatching {
            walkingRouteRepository.getRouteWeatherSummary(origin)
        }.getOrNull()
        return RouteScreenState.Ready(
            RouteLoadResult(
                options = buildRouteOptionsFromGeometries(
                    coolGeometry = coolRouteGeometry,
                    regularGeometry = regularRouteGeometry,
                    weatherSummary = routeWeatherSummary,
                ),
            )
        )
    }

    val errorMessage = walkingRouteResult.exceptionOrNull()?.message
    return RouteScreenState.Error(
        errorMessage ?: "No route alternatives could be generated for this trip."
    )
}

@Composable
private fun DraggableSelectedHeatSheet(
    location: SelectedHeatLocation,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val collapsedPeekHeight = 152.dp
    val collapsedPeekHeightPx = with(density) { collapsedPeekHeight.toPx() }
    var isCollapsed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var sheetHeightPx by remember { mutableFloatStateOf(0f) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var entranceVisible by remember(location.featureId) { mutableStateOf(false) }

    val maxOffsetPx = (sheetHeightPx - collapsedPeekHeightPx).coerceAtLeast(0f)
    val targetOffsetPx = when {
        isDragging -> dragOffsetPx
        isCollapsed -> maxOffsetPx
        else -> 0f
    }
    val animatedOffsetPx by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetOffsetPx,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "heat_summary_sheet_offset",
    )
    val entranceTranslationPx by animateFloatAsState(
        targetValue = if (entranceVisible) 0f else 72f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "selected_heat_sheet_entrance_translation",
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (entranceVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "selected_heat_sheet_entrance_alpha",
    )
    val entranceScale by animateFloatAsState(
        targetValue = if (entranceVisible) 1f else 0.985f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "selected_heat_sheet_entrance_scale",
    )

    LaunchedEffect(location.featureId) {
        isCollapsed = false
        isDragging = false
        dragOffsetPx = 0f
        entranceVisible = false
        delay(40)
        entranceVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(x = 0, y = animatedOffsetPx.roundToInt()) }
            .onSizeChanged { sheetHeightPx = it.height.toFloat() }
            .graphicsLayer {
                alpha = entranceAlpha
                translationY = entranceTranslationPx
                scaleX = entranceScale
                scaleY = entranceScale
            }
            .pointerInput(maxOffsetPx) {
                detectVerticalDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffsetPx = animatedOffsetPx
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetPx = (dragOffsetPx + dragAmount).coerceIn(0f, maxOffsetPx)
                    },
                    onDragEnd = {
                        isDragging = false
                        isCollapsed = dragOffsetPx > (maxOffsetPx * 0.45f)
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                )
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 22.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.96f),
                            Color(0xFFF6FBF5).copy(alpha = 0.92f),
                            Color(0xFFEFF6ED).copy(alpha = 0.9f),
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SheetHandle()
                SelectedHeatSummaryCardContent(
                    location = location,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .height(5.dp)
                .fillMaxWidth(0.14f),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
        ) {}
    }
}

@Composable
private fun SelectedHeatSummaryCardContent(
    location: SelectedHeatLocation,
    onDismiss: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Selected Heat Area",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ShadeMatePalette.PrimaryText,
                )
                Text(
                    text = "Tap another suburb to compare heat conditions around Melbourne.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShadeMatePalette.SecondaryText,
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear selected heat area",
                    tint = ShadeMatePalette.SecondaryText,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SelectedHeatMetricBubble(
                modifier = Modifier.weight(1f),
                value = location.temperatureCelsius?.let { "${it.toInt()}\u00B0C" } ?: "--",
                accentColor = Color(0xFF4B8341),
            )
            SelectedHeatMetricBubble(
                modifier = Modifier.weight(1f),
                value = location.name,
                accentColor = Color(0xFF26442E),
            )
        }
    }
}

@Composable
private fun SelectedHeatMetricBubble(
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val isTemperature = value.contains("°")
        val maxWidthDp = maxWidth
        val fontSize = when {
            isTemperature -> 30.sp
            value.length <= 12 && maxWidthDp >= 150.dp -> 22.sp
            value.length <= 16 && maxWidthDp >= 140.dp -> 20.sp
            value.length <= 20 && maxWidthDp >= 130.dp -> 18.sp
            else -> 16.sp
        }
        val lineHeight = when {
            isTemperature -> 34.sp
            fontSize >= 22.sp -> 26.sp
            fontSize >= 20.sp -> 24.sp
            fontSize >= 18.sp -> 22.sp
            else -> 20.sp
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = Color.White.copy(alpha = 0.74f),
            border = BorderStroke(1.dp, Color(0xFFE3ECE1)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color(0xFFEAF4E7).copy(alpha = 0.72f),
                            ),
                        ),
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                    ),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun FloatingMessageCard(
    title: String,
    message: String,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (loading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (actionLabel != null && onAction != null) {
                TextButton(
                    onClick = onAction,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun ZoneSummaryRow(zone: HeatZone) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = zone.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "${zone.temperatureCelsius}\u00B0C",
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Surface(
            modifier = Modifier.width(96.dp),
            shape = RoundedCornerShape(16.dp),
            color = colorForHeatLevel(zone.level).copy(alpha = 0.16f),
        ) {
            Text(
                text = zone.level.label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = colorForHeatLevel(zone.level),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
