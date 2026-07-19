package com.example.a5120_shademate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.a5120_shademate.data.CoolPlaceRepository
import com.example.a5120_shademate.model.LoadState
import com.example.a5120_shademate.ui.components.CoolPlaceCard
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import com.example.a5120_shademate.ui.components.CoolPlaceCategory
import com.example.a5120_shademate.ui.components.EmptyStateView
import com.example.a5120_shademate.ui.components.ErrorStateView
import com.example.a5120_shademate.ui.components.LoadingStateView
import com.example.a5120_shademate.ui.theme.ShadeMatePalette
import com.example.a5120_shademate.ui.viewmodel.HomeCoordinates
import kotlin.coroutines.resume
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull


private const val MIN_WALK_DISTANCE_METERS = 300
private const val MAX_WALK_DISTANCE_METERS = 5000

@Composable
fun CoolPlacesScreen(
    repository: CoolPlaceRepository,
    onPlaceSelected: (CoolPlaceCardData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isFilterScreenVisible by rememberSaveable { mutableStateOf(false) }
    var appliedCategory by remember { mutableStateOf<CoolPlaceCategory?>(null) }
    var appliedDistanceMeters by rememberSaveable { mutableIntStateOf(2500) }
    var draftCategory by remember { mutableStateOf<CoolPlaceCategory?>(null) }
    var draftDistanceMeters by rememberSaveable { mutableIntStateOf(appliedDistanceMeters) }

    var state by remember { mutableStateOf<LoadState<List<CoolPlaceCardData>>>(LoadState.Loading) }
    var isLastPage by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<HomeCoordinates?>(null) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun loadData(isRefresh: Boolean = true) {
        scope.launch {
            if (isRefresh) {
                state = LoadState.Loading
                if (userLocation == null && hasLocationPermission(context)) {
                    userLocation = resolveCurrentCoordinates(context)
                }
            } else {
                isLoadingMore = true
            }

            val currentPlaces = if (isRefresh) emptyList() else (state as? LoadState.Success)?.data.orEmpty()
            val offset = currentPlaces.size
            val limit = 20

            val result = runCatching {
                repository.getCoolPlaces(
                    lat = userLocation?.latitude,
                    lon = userLocation?.longitude,
                    limit = limit,
                    offset = offset
                )
            }

            result.fold(
                onSuccess = { newPlaces ->
                    // Enrich distances if the backend returned 0 but we have user location
                    val enrichedPlaces = userLocation?.let { loc ->
                        newPlaces.map { place ->
                            if (place.distanceMeters <= 0) {
                                val results = FloatArray(1)
                                Location.distanceBetween(
                                    loc.latitude, loc.longitude,
                                    place.latitude, place.longitude,
                                    results
                                )
                                place.copy(distanceMeters = results[0].toInt())
                            } else {
                                place
                            }
                        }
                    } ?: newPlaces

                    val totalPlaces = currentPlaces + enrichedPlaces
                    isLastPage = newPlaces.size < limit
                    state = if (totalPlaces.isEmpty()) LoadState.Empty else LoadState.Success(totalPlaces)
                },
                onFailure = {
                    if (isRefresh) {
                        state = LoadState.Error(it.message ?: "Couldn't load cool places right now.")
                    }
                }
            )
            isLoadingMore = false
        }
    }

    val locationPermissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            scope.launch {
                userLocation = resolveCurrentCoordinates(context)
                loadData(isRefresh = true)
            }
        } else {
            loadData(isRefresh = true)
        }
    }

    LaunchedEffect(repository) {
        if (hasLocationPermission(context)) {
            userLocation = resolveCurrentCoordinates(context)
            loadData(isRefresh = true)
        } else {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            !isLoadingMore && !isLastPage && state is LoadState.Success &&
                lastVisibleItemIndex >= totalItemsCount - 5 && totalItemsCount > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            loadData(isRefresh = false)
        }
    }

    val displayedPlaces = remember(state, appliedCategory, appliedDistanceMeters) {
        val places = (state as? LoadState.Success)?.data.orEmpty()
        places.filter { place ->
            (appliedCategory == null || place.categories.contains(appliedCategory)) &&
                place.distanceMeters <= appliedDistanceMeters
        }
    }


    if (isFilterScreenVisible) {
        CoolPlacesFilterScreen(
            selectedCategory = draftCategory,
            distanceMeters = draftDistanceMeters,
            onCategorySelected = { category ->
                draftCategory = if (draftCategory == category) null else category
            },
            onDistanceChanged = { onDistance ->
                draftDistanceMeters = onDistance
            },
            onBack = {
                draftCategory = appliedCategory
                draftDistanceMeters = appliedDistanceMeters
                isFilterScreenVisible = false
            },
            onApply = {
                appliedCategory = draftCategory
                appliedDistanceMeters = draftDistanceMeters
                isFilterScreenVisible = false
            },
            modifier = modifier,
        )
        return
    }

    Box(
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
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(top = 80.dp, bottom = 116.dp),
        ) {
            when (val currentState = state) {
                LoadState.Loading -> {
                    item {
                        LoadingStateView(
                            title = "Loading cool places",
                            message = "Fetching nearby places that can help users stay cooler.",
                            modifier = Modifier
                                .padding(horizontal = 26.dp)
                                .coolPlacesContentEntrance(
                                    animationKey = "loading",
                                    index = 0,
                                ),
                        )
                    }
                }

                LoadState.Empty -> {
                    item {
                        EmptyStateView(
                            title = "No cool places available",
                            message = "The repository returned no nearby places for this screen yet.",
                            modifier = Modifier
                                .padding(horizontal = 26.dp)
                                .coolPlacesContentEntrance(
                                    animationKey = "empty",
                                    index = 0,
                                ),
                        )
                    }
                }

                is LoadState.Error -> {
                    item {
                        ErrorStateView(
                            title = "Could not load cool places",
                            message = currentState.message,
                            onRetry = { loadData(isRefresh = true) },
                            modifier = Modifier
                                .padding(horizontal = 26.dp)
                                .coolPlacesContentEntrance(
                                    animationKey = "error:${currentState.message}",
                                    index = 0,
                                ),
                        )
                    }
                }

                is LoadState.Success -> {
                    if (displayedPlaces.isEmpty()) {
                        item {
                            EmptyCoolPlacesState(
                                modifier = Modifier
                                    .padding(horizontal = 26.dp)
                                    .coolPlacesContentEntrance(
                                        animationKey = "filtered-empty",
                                        index = 0,
                                    ),
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = displayedPlaces,
                            key = { _, place -> place.id }
                        ) { index, place ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 26.dp, vertical = 10.dp)
                                    .coolPlacesContentEntrance(
                                        animationKey = place.id,
                                        index = index % 20,
                                    )
                            ) {
                                CoolPlaceCard(
                                    place = place,
                                    onClick = { onPlaceSelected(place) },
                                    onGoClick = { onPlaceSelected(place) }
                                )
                            }
                        }
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = ShadeMatePalette.PrimaryGreen,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        FilterButtonRow(
            hasActiveFilter = appliedCategory != null || appliedDistanceMeters < MAX_WALK_DISTANCE_METERS,
            onClick = {
                draftCategory = appliedCategory
                draftDistanceMeters = appliedDistanceMeters
                isFilterScreenVisible = true
            },
            modifier = Modifier.statusBarsPadding()
        )
    }

}

@Composable
private fun FilterButtonRow(
    hasActiveFilter: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(18.dp)
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "cool_places_filter_button_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "cool_places_filter_button_alpha",
    )

    Row(
        modifier = modifier
            .padding(start = 26.dp, end = 26.dp, top = 10.dp, bottom = 18.dp),
    ) {
        Box(
            modifier = Modifier
                .width(144.dp)
                .height(52.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .shadow(
                    elevation = if (hasActiveFilter) 18.dp else 14.dp,
                    shape = shape,
                    ambientColor = ShadeMatePalette.PrimaryGreen.copy(alpha = 0.24f),
                    spotColor = Color.Black.copy(alpha = 0.10f),
                )
                .clip(shape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (hasActiveFilter) {
                            listOf(Color(0xFF6EAA58), Color(0xFF467F40))
                        } else {
                            listOf(Color(0xFF76B260), Color(0xFF4E8B46))
                        },
                    ),
                    shape = shape,
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.24f),
                    shape = shape,
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White,
                )
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.04.sp,
                    ),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun EmptyCoolPlacesState(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FCF9),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.72f)),
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "No cool places match this filter",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.14).sp,
                ),
                color = Color(0xFF55575A),
            )
            Text(
                text = "Try a different category or increase the walking distance.",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = Color(0xFF77797D),
            )
        }
    }
}

@Composable
private fun CoolPlacesFilterScreen(
    selectedCategory: CoolPlaceCategory?,
    distanceMeters: Int,
    onCategorySelected: (CoolPlaceCategory) -> Unit,
    onDistanceChanged: (Int) -> Unit,
    onBack: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var headerVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        headerVisible = true
        delay(110)
        cardVisible = true
        delay(120)
        buttonVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF7F9F6),
                        Color(0xFFFCFCFA),
                        Color.White,
                    ),
                ),
            )
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 138.dp),
    ) {
        FilterScreenHeader(
            onBack = onBack,
            visible = headerVisible,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            FilterMainCard(
                selectedCategory = selectedCategory,
                distanceMeters = distanceMeters,
                onCategorySelected = onCategorySelected,
                onDistanceChanged = onDistanceChanged,
                visible = cardVisible,
            )

            FilterApplyButton(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .iosEntrance(
                        visible = buttonVisible,
                        initialOffsetY = 24f,
                        initialScale = 0.985f,
                    ),
            )
        }
    }
}

@Composable
private fun FilterScreenHeader(
    onBack: () -> Unit,
    visible: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .iosEntrance(
                visible = visible,
                initialOffsetY = 16f,
                initialScale = 0.99f,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterBackButton(onClick = onBack)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Filter",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                ),
                color = ShadeMatePalette.PrimaryText.copy(alpha = 0.94f),
            )
        }
        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun FilterBackButton(
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "filter_back_button_scale",
    )

    Surface(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.78f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.72f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ShadeMatePalette.PrimaryText.copy(alpha = 0.9f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun FilterMainCard(
    selectedCategory: CoolPlaceCategory?,
    distanceMeters: Int,
    onCategorySelected: (CoolPlaceCategory) -> Unit,
    onDistanceChanged: (Int) -> Unit,
    visible: Boolean,
) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .iosEntrance(
                visible = visible,
                initialOffsetY = 28f,
                initialScale = 0.985f,
            )
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = Color.White.copy(alpha = 0.30f),
                spotColor = ShadeMatePalette.PrimaryGreen.copy(alpha = 0.10f),
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FCF9).copy(alpha = 0.92f),
                        Color(0xFFEEF7F1).copy(alpha = 0.95f),
                        Color(0xFFE8F4EC).copy(alpha = 0.98f),
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
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.16).sp,
                    ),
                    color = ShadeMatePalette.PrimaryText.copy(alpha = 0.9f),
                )
                CategoryPicker(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                )
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.62f),
                    thickness = 1.dp,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    text = "How far do you want to walk?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.18).sp,
                    ),
                    color = ShadeMatePalette.PrimaryText.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Slider(
                    value = distanceMeters.toFloat(),
                    onValueChange = { onDistanceChanged(it.toInt()) },
                    valueRange = MIN_WALK_DISTANCE_METERS.toFloat()..MAX_WALK_DISTANCE_METERS.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4E8B46),
                        activeTrackColor = Color(0xFF6DAB5A),
                        inactiveTrackColor = Color(0xFFD9E6DA),
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent,
                    ),
                )
                AnimatedContent(
                    targetState = formatWalkDistance(distanceMeters),
                    label = "walk_distance_value",
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(170)) +
                            slideInVertically(
                                animationSpec = tween(170, easing = FastOutSlowInEasing),
                                initialOffsetY = { it / 4 },
                            )) togetherWith
                            (fadeOut(animationSpec = tween(120)) +
                                slideOutVertically(
                                    animationSpec = tween(140, easing = FastOutSlowInEasing),
                                    targetOffsetY = { -it / 4 },
                                ))
                    },
                ) { distanceText ->
                    Text(
                        text = distanceText,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp,
                            letterSpacing = 0.04.sp,
                        ),
                        color = Color(0xFF4E8B46),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPicker(
    selectedCategory: CoolPlaceCategory?,
    onCategorySelected: (CoolPlaceCategory) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CoolPlaceCategory.entries.forEach { category ->
            CategoryFilterChip(
                label = category.label,
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
            )
        }
    }
}

@Composable
private fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "filter_chip_scale",
    )
    val containerAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.92f,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "filter_chip_alpha",
    )

    Surface(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = containerAlpha
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) ShadeMatePalette.SelectedNavBackground.copy(alpha = 0.98f) else Color.White.copy(alpha = 0.76f),
        border = BorderStroke(
            1.dp,
            if (selected) Color(0xFF8DB97C) else Color.White.copy(alpha = 0.72f),
        ),
        shadowElevation = if (selected) 6.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = ShadeMatePalette.PrimaryGreen,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    letterSpacing = 0.02.sp,
                ),
                color = if (selected) ShadeMatePalette.PrimaryGreen else ShadeMatePalette.PrimaryText.copy(alpha = 0.82f),
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun FilterApplyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(22.dp)
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "filter_apply_button_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "filter_apply_button_alpha",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .shadow(
                elevation = 18.dp,
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
                color = Color.White.copy(alpha = 0.24f),
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Apply Filter",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.02.sp,
            ),
            color = Color.White,
        )
    }
}

@Composable
private fun Modifier.iosEntrance(
    visible: Boolean,
    initialOffsetY: Float,
    initialScale: Float,
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "filter_entrance_alpha",
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else initialOffsetY,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "filter_entrance_translation",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else initialScale,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "filter_entrance_scale",
    )
    return graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
        scaleX = scale
        scaleY = scale
    }
}

@Composable
private fun Modifier.coolPlacesContentEntrance(
    animationKey: Any,
    index: Int,
): Modifier {
    var visible by remember(animationKey) { mutableStateOf(false) }

    LaunchedEffect(animationKey) {
        visible = false
        delay(index * 85L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "cool_places_content_alpha",
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "cool_places_content_translation",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.985f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "cool_places_content_scale",
    )

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
        scaleX = scale
        scaleY = scale
    }
}

private fun formatWalkDistance(distanceMeters: Int): String {
    return if (distanceMeters >= 1000) {
        String.format("%.1f km", distanceMeters / 1000f)
    } else {
        "$distanceMeters m"
    }
}

private fun hasLocationPermission(context: Context): Boolean {
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
