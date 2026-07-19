package com.example.a5120_shademate.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import com.example.a5120_shademate.data.api.RouteWeatherSummary
import com.example.a5120_shademate.data.api.WalkingRouteGeometry
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class MapDestinationDetails(
    val name: String,
    val distanceText: String,
    val temperatureText: String?,
)

data class RouteOptionUiModel(
    val id: String,
    val label: String,
    val points: List<Point>,
    val distanceKm: Double,
    val durationMinutes: Int,
    val feelsLikeCelsius: Int? = null,
    val uvLevel: Int? = null,
    val uvExposureDurationMinutes: Int? = null,
    val shadeAvailable: Boolean? = null,
    val shadeCoverageScore: Int? = null, // 统一命名为 Score
    val heatExposureScore: Int? = null, // 新增：热暴露评分
    val isCoolest: Boolean = false,
    val shadedSegments: List<List<Point>> = emptyList(),
)

data class RouteNearbyCoolPlace(
    val place: CoolPlaceCardData,
    val routeDistanceMeters: Int,
) {
    val routeDistanceText: String
        get() = if (routeDistanceMeters >= 1000) {
            String.format("%.1f km off route", routeDistanceMeters / 1000f)
        } else {
            "$routeDistanceMeters m off route"
        }
}

// Nullable route summary fields let the UI show partial backend responses without breaking the route flow.
typealias MockRouteOption = RouteOptionUiModel

data class RouteLoadResult(
    val options: List<MockRouteOption>,
)

fun selectRegularRouteGeometry(
    coolGeometry: WalkingRouteGeometry?,
    mapboxRoutes: List<WalkingRouteGeometry>,
    futureRecommendedRoute: WalkingRouteGeometry?,
): WalkingRouteGeometry? {
    val mapboxAlternative = mapboxRoutes
        .drop(1)
        .firstOrNull { candidate ->
            coolGeometry == null || !candidate.hasSameGeometryAs(coolGeometry)
        }
    if (mapboxAlternative != null) return mapboxAlternative

    // Keep this as the future AI/recommendation integration point. Until that backend is
    // ready, do not let the regular route duplicate the cool route in the comparison UI.
    return futureRecommendedRoute?.takeUnless { recommendedRoute ->
        coolGeometry != null && recommendedRoute.hasSameGeometryAs(coolGeometry)
    }
}

sealed interface RouteScreenState {
    data object Loading : RouteScreenState
    data class Ready(val result: RouteLoadResult) : RouteScreenState
    data class Error(val message: String) : RouteScreenState
    data class Empty(val message: String) : RouteScreenState
}

@Composable
fun DestinationInfoSheet(
    details: MapDestinationDetails,
    onRouteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
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

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = details.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F3530),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DestinationMetaChip(label = details.distanceText)
                    details.temperatureText?.let { DestinationMetaChip(label = it) }
                }
            }

            Button(
                onClick = onRouteClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF477A31)),
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Find Cool Route",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DestinationMetaChip(label: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFE4F3E1),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF477A31),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun CoolRouteScreen(
    originQuery: String,
    destinationQuery: String,
    onOriginQueryChange: (String) -> Unit,
    onDestinationQueryChange: (String) -> Unit,
    onOriginFieldFocused: () -> Unit,
    onDestinationFieldFocused: () -> Unit,
    suggestions: List<PlaceAutocompleteSuggestion>,
    isSearching: Boolean,
    errorMessage: String?,
    onSuggestionSelected: (PlaceAutocompleteSuggestion) -> Unit,
    routeState: RouteScreenState,
    selectedRouteIndex: Int,
    onSelectedRouteChange: (Int) -> Unit,
    onDismissRoute: () -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoutePlannerCard(
                originQuery = originQuery,
                destinationQuery = destinationQuery,
                onOriginQueryChange = onOriginQueryChange,
                onDestinationQueryChange = onDestinationQueryChange,
                onOriginFieldFocused = onOriginFieldFocused,
                onDestinationFieldFocused = onDestinationFieldFocused,
                suggestions = suggestions,
                isSearching = isSearching,
                errorMessage = errorMessage,
                onSuggestionSelected = onSuggestionSelected,
            )
        }

        when (routeState) {
            RouteScreenState.Loading -> {
                RouteStatusSheet(
                    title = "Finding walking routes",
                    message = "Checking walking alternatives between your start and destination.",
                    loading = true,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 108.dp),
                )
            }

            is RouteScreenState.Error -> {
                RouteStatusSheet(
                    title = "Couldn't load routes",
                    message = routeState.message,
                    actionLabel = "Try again",
                    onAction = onRetry,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 108.dp),
                )
            }

            is RouteScreenState.Empty -> Unit

            is RouteScreenState.Ready -> {
                DraggableRouteSheet(
                    routeResult = routeState.result,
                    selectedRouteIndex = selectedRouteIndex,
                    onSelectedRouteChange = onSelectedRouteChange,
                    onDismissRequest = onDismissRoute,
                    onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 108.dp),
                )
            }
        }
    }
}

@Composable
fun NearbyCoolPlaceRouteCard(
    nearbyPlace: RouteNearbyCoolPlace,
    onOpenPlace: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = nearbyPlace.place.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F3530),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = nearbyPlace.place.suburb,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6F7175),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close nearby cool place card",
                        tint = Color(0xFF7A7A7A),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DestinationMetaChip(label = nearbyPlace.routeDistanceText)
                DestinationMetaChip(label = nearbyPlace.place.temperatureText)
            }

            Button(
                onClick = onOpenPlace,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF477A31)),
            ) {
                Text(
                    text = "Open Cool Place",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun RoutePlannerCard(
    originQuery: String,
    destinationQuery: String,
    onOriginQueryChange: (String) -> Unit,
    onDestinationQueryChange: (String) -> Unit,
    onOriginFieldFocused: () -> Unit,
    onDestinationFieldFocused: () -> Unit,
    suggestions: List<PlaceAutocompleteSuggestion>,
    isSearching: Boolean,
    errorMessage: String?,
    onSuggestionSelected: (PlaceAutocompleteSuggestion) -> Unit,
) {
    val shouldShowResults = (originQuery.isNotBlank() || destinationQuery.isNotBlank()) &&
        (isSearching || suggestions.isNotEmpty() || errorMessage != null)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
            border = BorderStroke(1.dp, Color(0xFFA6A6A6)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                RouteInputRow(
                    leadingIcon = Icons.Default.LocationOn,
                    value = originQuery,
                    onValueChange = onOriginQueryChange,
                    onFocused = onOriginFieldFocused,
                    placeholder = "Current location",
                    trailingMic = false,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFD8D8D8)),
                )
                RouteInputRow(
                    leadingIcon = Icons.Default.Search,
                    value = destinationQuery,
                    onValueChange = onDestinationQueryChange,
                    onFocused = onDestinationFieldFocused,
                    placeholder = "Where do you want to go?",
                    trailingMic = true,
                )
            }
        }

        if (shouldShowResults) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    when {
                        isSearching -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Searching places...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6A6A6A),
                                )
                            }
                        }

                        errorMessage != null -> {
                            Text(
                                text = errorMessage,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC44949),
                            )
                        }

                        suggestions.isEmpty() -> {
                            Text(
                                text = "No places found.",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6A6A6A),
                            )
                        }

                        else -> suggestions.forEachIndexed { index, suggestion ->
                            RouteSuggestionRow(
                                suggestion = suggestion,
                                onClick = { onSuggestionSelected(suggestion) },
                            )
                            if (index != suggestions.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp)
                                        .height(1.dp)
                                        .background(Color(0xFFE2E2E2)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteInputRow(
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    onFocused: () -> Unit,
    placeholder: String,
    trailingMic: Boolean,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onFocused()
                }
            },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleSmall.copy(
            color = Color(0xFF55575A),
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp,
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.titleSmall.copy(lineHeight = 20.sp),
                color = Color(0xFF8E8E8E),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color(0xFF7A7A7A),
            )
        },
        trailingIcon = if (trailingMic) {
            {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFF9A9A9A),
                )
            }
        } else {
            null
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color(0xFF55575A),
            unfocusedTextColor = Color(0xFF55575A),
            cursorColor = Color(0xFF477A31),
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search,
        ),
    )
}

@Composable
private fun RouteSuggestionRow(
    suggestion: PlaceAutocompleteSuggestion,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3A3A3A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            suggestion.formattedAddress?.let { address ->
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7A7A7A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        suggestion.distanceMeters?.let { distanceMeters ->
            Text(
                text = if (distanceMeters >= 1000) {
                    String.format("%.1f km", distanceMeters / 1000.0)
                } else {
                    "${distanceMeters.roundToInt()} m"
                },
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF8A8A8A),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RouteStatusSheet(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.84f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.54f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.96f),
                            Color(0xFFF1F7EF).copy(alpha = 0.9f),
                        ),
                    ),
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier
                            .height(5.dp)
                            .fillMaxWidth(0.14f),
                        shape = CircleShape,
                        color = Color(0xFFB8C3BA).copy(alpha = 0.85f),
                    ) {}
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF24342A),
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF59655D),
                )

                when {
                    loading -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF5B8D49),
                            )
                            Text(
                                text = "Loading route preview...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4E7E3E),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    actionLabel != null && onAction != null -> {
                        val actionInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = onAction,
                            modifier = Modifier
                                .height(50.dp)
                                .routePressAnimation(actionInteraction),
                            interactionSource = actionInteraction,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D8F49)),
                        ) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class RouteSheetState {
    Collapsed,
    Medium,
    Expanded,
}

@Composable
private fun DraggableRouteSheet(
    routeResult: RouteLoadResult,
    selectedRouteIndex: Int,
    onSelectedRouteChange: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val routeOptions = routeResult.options
    if (routeOptions.isEmpty()) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        val scrollState = rememberScrollState()
        val selectedRoute = routeOptions.getOrElse(selectedRouteIndex.coerceIn(0, routeOptions.lastIndex)) {
            routeOptions.first()
        }
        val availableHeight = maxHeight
        val sheetHeight = (availableHeight * 0.86f).coerceAtLeast(360.dp)
        val collapsedVisibleHeight = 104.dp
        val mediumVisibleHeight = (availableHeight * 0.66f).coerceAtLeast(360.dp).coerceAtMost(sheetHeight)
        val hiddenOffsetPx = with(density) { (sheetHeight + 40.dp).toPx() }
        val collapsedOffsetPx = with(density) { (sheetHeight - collapsedVisibleHeight).toPx() }
        val mediumOffsetPx = with(density) { (sheetHeight - mediumVisibleHeight).toPx() }

        var sheetState by remember(routeResult) { mutableStateOf(RouteSheetState.Collapsed) }
        var isPresented by remember(routeResult) { mutableStateOf(false) }
        var isClosing by remember(routeResult) { mutableStateOf(false) }
        var isDragging by remember { mutableStateOf(false) }
        var dragOffsetPx by remember(routeResult) { mutableFloatStateOf(hiddenOffsetPx) }
        var visibleBlockCount by remember(routeResult) { mutableStateOf(0) }
        var hasPlayedExpandedEntrance by remember(routeResult) { mutableStateOf(false) }

        fun updateBottomBarVisibility(scrollDeltaY: Float) {
            when {
                !isDragging && sheetState == RouteSheetState.Expanded -> onBottomBarVisibilityChanged(true)
                scrollDeltaY < -1f -> onBottomBarVisibilityChanged(false)
                scrollDeltaY > 1f -> onBottomBarVisibilityChanged(true)
            }
        }

        fun offsetFor(state: RouteSheetState): Float = when (state) {
            RouteSheetState.Collapsed -> collapsedOffsetPx
            RouteSheetState.Medium -> mediumOffsetPx
            RouteSheetState.Expanded -> 0f
        }

        fun nearestState(offsetPx: Float): RouteSheetState {
            val distances = listOf(
                RouteSheetState.Collapsed to kotlin.math.abs(offsetPx - collapsedOffsetPx),
                RouteSheetState.Medium to kotlin.math.abs(offsetPx - mediumOffsetPx),
                RouteSheetState.Expanded to kotlin.math.abs(offsetPx - 0f),
            )
            return distances.minBy { it.second }.first
        }

        val targetOffsetPx = when {
            !isPresented || isClosing -> hiddenOffsetPx
            isDragging -> dragOffsetPx
            else -> offsetFor(sheetState)
        }
        val animatedOffsetPx by animateFloatAsState(
            targetValue = targetOffsetPx,
            animationSpec = if (isDragging) {
                tween(durationMillis = 80)
            } else {
                spring(dampingRatio = 0.88f, stiffness = 520f)
            },
            label = "routeSheetOffset",
        )
        val animatedAlpha by animateFloatAsState(
            targetValue = if (!isPresented || isClosing) 0f else 1f,
            animationSpec = tween(durationMillis = 220),
            label = "routeSheetAlpha",
        )

        val effectiveState = when {
            !isPresented || isClosing -> RouteSheetState.Collapsed
            isDragging -> nearestState(dragOffsetPx)
            else -> sheetState
        }

        fun updateDragOffset(deltaPx: Float) {
            updateBottomBarVisibility(deltaPx)
            dragOffsetPx = (dragOffsetPx + deltaPx).coerceIn(0f, collapsedOffsetPx)
        }

        fun finalizeDrag() {
            isDragging = false
            sheetState = nearestState(dragOffsetPx)
            dragOffsetPx = offsetFor(sheetState)
            onBottomBarVisibilityChanged(true)
        }

        fun currentDragBaseOffset(): Float {
            return if (isDragging) dragOffsetPx else offsetFor(sheetState)
        }

        fun expandSheetToMedium() {
            if (isClosing || isDragging || sheetState != RouteSheetState.Collapsed) return
            sheetState = RouteSheetState.Medium
            dragOffsetPx = offsetFor(RouteSheetState.Medium)
            onBottomBarVisibilityChanged(true)
        }

        val sheetScrollConnection = remember(onBottomBarVisibilityChanged, scrollState, collapsedOffsetPx) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val deltaY = available.y
                    val currentOffsetPx = currentDragBaseOffset()
                    val canExpandSheet = deltaY < 0f && currentOffsetPx > 0f && scrollState.value == 0
                    val canCollapseSheet = deltaY > 0f && currentOffsetPx < collapsedOffsetPx && scrollState.value == 0

                    if (canExpandSheet || canCollapseSheet) {
                        isDragging = true
                        updateBottomBarVisibility(deltaY)
                        val nextOffsetPx = (currentOffsetPx + deltaY).coerceIn(0f, collapsedOffsetPx)
                        val consumedY = nextOffsetPx - currentOffsetPx
                        dragOffsetPx = nextOffsetPx
                        return Offset(0f, consumedY)
                    }

                    updateBottomBarVisibility(available.y)
                    return Offset.Zero
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    updateBottomBarVisibility(consumed.y.takeIf { it != 0f } ?: available.y)
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (isDragging) {
                        finalizeDrag()
                        return available
                    }
                    return Velocity.Zero
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    if (isDragging) {
                        finalizeDrag()
                        return available
                    }
                    return Velocity.Zero
                }
            }
        }

        fun dismissSheet() {
            if (isClosing) return
            scope.launch {
                isClosing = true
                visibleBlockCount = 0
                delay(220)
                onDismissRequest()
            }
        }

        LaunchedEffect(routeResult, hiddenOffsetPx) {
            isPresented = false
            isClosing = false
            visibleBlockCount = 0
            hasPlayedExpandedEntrance = false
            dragOffsetPx = hiddenOffsetPx
            delay(50)
            isPresented = true
            delay(90)
            visibleBlockCount = 1
        }

        LaunchedEffect(routeResult, effectiveState, isPresented, isClosing) {
            if (!isPresented || isClosing) return@LaunchedEffect
            if (effectiveState == RouteSheetState.Collapsed) {
                hasPlayedExpandedEntrance = false
                visibleBlockCount = 1
                return@LaunchedEffect
            }
            if (hasPlayedExpandedEntrance) return@LaunchedEffect

            hasPlayedExpandedEntrance = true
            delay(70)
            visibleBlockCount = 2
            delay(70)
            visibleBlockCount = 3
            delay(70)
            visibleBlockCount = 4
            delay(70)
            visibleBlockCount = 5
            delay(70)
            visibleBlockCount = 6
            delay(70)
            visibleBlockCount = 7
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .offset { IntOffset(x = 0, y = animatedOffsetPx.roundToInt()) }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = effectiveState == RouteSheetState.Collapsed,
                    onClick = ::expandSheetToMedium,
                )
                .graphicsLayer { alpha = animatedAlpha },
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 22.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(sheetScrollConnection)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RouteSheetTopBar(
                        summary = "${selectedRoute.label} · ${selectedRoute.distanceKm.format1()} km · ${selectedRoute.durationMinutes} min",
                        showSummary = effectiveState == RouteSheetState.Collapsed,
                        showExpandHint = effectiveState == RouteSheetState.Collapsed,
                        onDismiss = ::dismissSheet,
                        onDragStart = { isDragging = true },
                        onDrag = ::updateDragOffset,
                        onDragEnd = ::finalizeDrag,
                        onDragCancel = {
                            isDragging = false
                            dragOffsetPx = offsetFor(sheetState)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    StaggeredSheetBlock(
                        visible = visibleBlockCount >= 2 && effectiveState != RouteSheetState.Collapsed,
                    ) {
                        RouteTypeSegmentedControl(
                            routeOptions = routeOptions,
                            selectedRouteIndex = selectedRouteIndex,
                            onSelectedRouteChange = onSelectedRouteChange,
                        )
                    }

                    StaggeredSheetBlock(
                        visible = visibleBlockCount >= 3 && effectiveState != RouteSheetState.Collapsed,
                    ) {
                        AnimatedContent(
                            targetState = selectedRoute.id,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInHorizontally { it / 5 }) togetherWith
                                    (fadeOut(tween(140)) + slideOutHorizontally { -it / 8 })
                            },
                            label = "routeBadgeTransition",
                        ) { routeId ->
                            val route = routeOptions.firstOrNull { it.id == routeId } ?: selectedRoute
                            RouteBadgeRow(
                                isCoolest = route.isCoolest,
                                prefersShade = route.shadeAvailable == true || route.shadedSegments.isNotEmpty(),
                            )
                        }
                    }

                    StaggeredSheetBlock(
                        visible = visibleBlockCount >= 4 && effectiveState != RouteSheetState.Collapsed,
                    ) {
                        AnimatedContent(
                            targetState = selectedRoute.id,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInHorizontally { it / 5 }) togetherWith
                                    (fadeOut(tween(140)) + slideOutHorizontally { -it / 8 })
                            },
                            label = "travelSummaryTransition",
                        ) { routeId ->
                            val route = routeOptions.firstOrNull { it.id == routeId } ?: selectedRoute
                            TravelSummaryCard(route = route)
                        }
                    }

                    StaggeredSheetBlock(
                        visible = visibleBlockCount >= 5 && effectiveState != RouteSheetState.Collapsed,
                    ) {
                        AnimatedContent(
                            targetState = selectedRoute.id,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInHorizontally { it / 5 }) togetherWith
                                    (fadeOut(tween(140)) + slideOutHorizontally { -it / 8 })
                            },
                            label = "temperatureSummaryTransition",
                        ) { routeId ->
                            val route = routeOptions.firstOrNull { it.id == routeId } ?: selectedRoute
                            CurrentConditionsCard(route = route)
                        }
                    }

                    StaggeredSheetBlock(
                        visible = visibleBlockCount >= 6 && effectiveState != RouteSheetState.Collapsed,
                    ) {
                        AnimatedContent(
                            targetState = selectedRoute.id,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInHorizontally { it / 5 }) togetherWith
                                    (fadeOut(tween(140)) + slideOutHorizontally { -it / 8 })
                            },
                            label = "exposureSummaryTransition",
                        ) { routeId ->
                            val route = routeOptions.firstOrNull { it.id == routeId } ?: selectedRoute
                            ExposureSummaryCard(route = route)
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun RouteSheetTopBar(
    summary: String,
    showSummary: Boolean,
    showExpandHint: Boolean,
    onDismiss: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val closeInteraction = remember { MutableInteractionSource() }
    Box(modifier = modifier.heightIn(min = 34.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { onDragStart() },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount)
                        },
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragCancel,
                    )
                },
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .height(5.dp)
                    .width(54.dp),
                shape = CircleShape,
                color = Color(0xFFB4BEB5).copy(alpha = 0.9f),
            ) {}
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(34.dp)
                .routePressAnimation(closeInteraction, pressedScale = 0.95f, pressedAlpha = 0.86f)
                .clickable(
                    interactionSource = closeInteraction,
                    indication = null,
                    onClick = onDismiss,
                ),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.68f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close route result",
                    tint = Color(0xFF617165),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = showSummary,
            enter = fadeIn(tween(180)) + slideInHorizontally(initialOffsetX = { it / 6 }),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(top = 18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF2B3B31),
                    fontWeight = FontWeight.SemiBold,
                )
                if (showExpandHint) {
                    Text(
                        text = "Tap to view more",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                        ),
                        color = Color(0xFF6B746C),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteTypeSegmentedControl(
    routeOptions: List<MockRouteOption>,
    selectedRouteIndex: Int,
    onSelectedRouteChange: (Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        val segmentCount = routeOptions.size.coerceAtLeast(1)
        val animatedIndex by animateFloatAsState(
            targetValue = selectedRouteIndex.coerceIn(0, segmentCount - 1).toFloat(),
            animationSpec = spring(dampingRatio = 0.88f, stiffness = 620f),
            label = "routeSegmentIndex",
        )
        val segmentWidth = maxWidth / segmentCount

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.White.copy(alpha = 0.62f),
                    shape = RoundedCornerShape(20.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .offset(x = segmentWidth * animatedIndex)
                    .width(segmentWidth - 8.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFD7EDD2),
                                Color(0xFFC8E3C2),
                            ),
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ),
            )

            Row(modifier = Modifier.fillMaxSize()) {
                routeOptions.forEachIndexed { index, route ->
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .routePressAnimation(interactionSource)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onSelectedRouteChange(index) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = route.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selectedRouteIndex == index) Color(0xFF26442E) else Color(0xFF6A746D),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteBadgeRow(
    isCoolest: Boolean,
    prefersShade: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isCoolest) {
            RouteInfoBadge(
                label = "Coolest Route",
                background = listOf(Color(0xFFE7F6E2), Color(0xFFD8EFD3)),
                textColor = Color(0xFF3E7B37),
            )
        }

        RouteInfoBadge(
            label = if (prefersShade) "Shade Preferred" else "Regular Route",
            background = if (prefersShade) {
                listOf(Color(0xFFEFF8EE), Color(0xFFE1F1DE))
            } else {
                listOf(Color(0xFFF5F6F4), Color(0xFFEDEFEA))
            },
            textColor = if (prefersShade) Color(0xFF4E7D43) else Color(0xFF6A746D),
        )
    }
}

@Composable
private fun RouteInfoBadge(
    label: String,
    background: List<Color>,
    textColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(background),
                    shape = RoundedCornerShape(18.dp),
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TravelSummaryCard(route: MockRouteOption) {
    RouteGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Travel Summary",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF25372D),
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RoutePrimaryMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    value = "${route.distanceKm.format1()} km",
                    label = "Distance",
                )
                RoutePrimaryMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccessTime,
                    value = "${route.durationMinutes} min",
                    label = "Duration",
                )
            }
        }
    }
}

@Composable
private fun DeprecatedLegacyTemperatureSummaryCard(route: MockRouteOption) {
    CurrentConditionsCard(route)
}

@Composable
private fun DeprecatedTemperatureSummaryCard(route: MockRouteOption) {
    CurrentConditionsCard(route)
}

@Composable
private fun DeprecatedExposureSummaryCard(route: MockRouteOption) {
    ExposureSummaryCard(route)
}

@Composable
private fun CurrentConditionsCard(route: MockRouteOption) {
    SummaryStatCard(
        title = "Current Conditions",
        stats = listOf(
            Triple(formatTemperatureValue(route.feelsLikeCelsius), "Feels-like Temp.", Color(0xFFD66A61)),
            Triple(route.uvLevel?.toString() ?: "N/A", "UV Level", Color(0xFF4E9B61)),
        ),
    )
}

@Composable
private fun ExposureSummaryCard(route: MockRouteOption) {
    SummaryStatCard(
        title = "Exposure Summary",
        stats = listOf(
            Triple(formatShadeCoverage(route), "Shade score", Color(0xFF4D8742)),
            Triple(formatUvExposureDuration(route.uvExposureDurationMinutes), "UV Exposure Duration", Color(0xFF6A746D)),
        ),
    )
}

private fun formatShadeCoverage(route: MockRouteOption): String {
    return route.shadeCoverageScore?.toString() ?: "--"
}

@Composable
private fun SummaryStatCard(
    title: String,
    stats: List<Triple<String, String, Color>>,
) {
    RouteGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF25372D),
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                stats.forEach { (value, label, color) ->
                    RouteSecondaryMetric(
                        modifier = Modifier.weight(1f),
                        value = value,
                        label = label,
                        accentColor = color,
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.72f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.94f),
                            Color(0xFFEAF4E7).copy(alpha = 0.88f),
                        ),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun RoutePrimaryMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.74f),
        border = BorderStroke(1.dp, Color(0xFFDFECDD)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFDCEFD8),
                ) {
                    Box(
                        modifier = Modifier.padding(7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF4B8341),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF6B746C),
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = value,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF203128),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RouteSecondaryMetric(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val compact = maxWidth <= 150.dp
        val horizontalPadding = if (compact) 10.dp else 14.dp
        val verticalPadding = if (compact) 13.dp else 14.dp
        val labelBaseFontSize = when {
            maxWidth <= 110.dp -> 9.sp
            maxWidth <= 130.dp -> 10.sp
            compact -> 11.sp
            else -> MaterialTheme.typography.bodySmall.fontSize
        }
        val labelStyle = if (compact) {
            MaterialTheme.typography.bodySmall.copy(
                fontSize = labelBaseFontSize,
                lineHeight = (labelBaseFontSize.value + 3).sp,
            )
        } else {
            MaterialTheme.typography.bodySmall.copy(fontSize = labelBaseFontSize)
        }
        var labelFontSize by remember(label, maxWidth) { mutableStateOf(labelBaseFontSize) }
        val minLabelFontSize = 8.sp

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color.White.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, Color(0xFFE3ECE1)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = value,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = label,
                    modifier = Modifier.fillMaxWidth(),
                    style = labelStyle.copy(
                        fontSize = labelFontSize,
                        lineHeight = (labelFontSize.value + 3).sp,
                    ),
                    color = Color(0xFF68726A),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    onTextLayout = { result ->
                        if (result.hasVisualOverflow && labelFontSize.value > minLabelFontSize.value) {
                            labelFontSize = (labelFontSize.value - 0.5f)
                                .coerceAtLeast(minLabelFontSize.value)
                                .sp
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun RouteDetailNoteCard(message: String) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFE5F2E1).copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color(0xFFD5E9D0)),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4E654F),
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        )
    }
}

@Composable
private fun StaggeredSheetBlock(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) + slideInHorizontally(
            animationSpec = tween(220),
            initialOffsetX = { it / 5 },
        ),
        exit = fadeOut(animationSpec = tween(120)),
    ) {
        content()
    }
}

private fun Modifier.routePressAnimation(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
    pressedAlpha: Float = 0.92f,
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "pressScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) pressedAlpha else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "pressAlpha",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

fun buildRouteOptionsFromGeometries(
    coolGeometry: WalkingRouteGeometry?,
    regularGeometry: WalkingRouteGeometry?,
    weatherSummary: RouteWeatherSummary? = null,
): List<MockRouteOption> {
    val options = mutableListOf<MockRouteOption>()

    coolGeometry?.let { geometry ->
        val distanceKm = geometry.distanceMeters / 1000.0
        val durationMinutes = (geometry.durationSeconds / 60.0).roundToInt().coerceAtLeast(1)
        val sunRatio = weatherSummary?.sunExposureRatio
        val uvExposureMinutes = sunRatio?.let { (durationMinutes * it).roundToInt() }

        options += MockRouteOption(
            id = "real_route_cool",
            label = "Cool Route",
            points = geometry.points,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            feelsLikeCelsius = weatherSummary?.feelsLikeCelsius,
            uvLevel = weatherSummary?.uvLevel,
            uvExposureDurationMinutes = uvExposureMinutes,
            shadeCoverageScore = weatherSummary?.shadeCoverageScore,
            heatExposureScore = weatherSummary?.heatExposureScore,
            shadeAvailable = true,
            isCoolest = true,
        )
    }

    regularGeometry?.let { geometry ->
        val distanceKm = geometry.distanceMeters / 1000.0
        val durationMinutes = (geometry.durationSeconds / 60.0).roundToInt().coerceAtLeast(1)
        val sunRatio = weatherSummary?.sunExposureRatio
        val uvExposureMinutes = sunRatio?.let { (durationMinutes * it).roundToInt() }

        options += MockRouteOption(
            id = "real_route_regular",
            label = "Regular Route",
            points = geometry.points,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            feelsLikeCelsius = weatherSummary?.feelsLikeCelsius,
            uvLevel = weatherSummary?.uvLevel,
            uvExposureDurationMinutes = uvExposureMinutes,
            shadeCoverageScore = weatherSummary?.shadeCoverageScore,
            heatExposureScore = weatherSummary?.heatExposureScore,
            shadeAvailable = false,
            isCoolest = false,
        )
    }

    return options
}

private fun WalkingRouteGeometry.hasSameGeometryAs(other: WalkingRouteGeometry): Boolean {
    if (points.size != other.points.size) return false
    return points.zip(other.points).all { (first, second) ->
        abs(first.longitude() - second.longitude()) < 0.00001 &&
            abs(first.latitude() - second.latitude()) < 0.00001
    }
}

fun deprecatedLegacyBuildDestinationDetails(
    title: String,
    userLocation: Point?,
    destination: Point,
): MapDestinationDetails {
    val distanceKm = userLocation?.let { haversineDistanceKm(it, destination) } ?: 1.3
    val approxTemp = when {
        distanceKm < 1.5 -> 25
        distanceKm < 4.0 -> 27
        else -> 29
    }
    return MapDestinationDetails(
        name = title,
        distanceText = "${distanceKm.format1()} km",
        temperatureText = "${approxTemp}°C",
    )
}

fun deprecatedBuildDestinationDetails(
    title: String,
    userLocation: Point?,
    destination: Point,
): MapDestinationDetails {
    val distanceKm = userLocation?.let { haversineDistanceKm(it, destination) } ?: 1.3
    val approxTemp = when {
        distanceKm < 1.5 -> 25
        distanceKm < 4.0 -> 27
        else -> 29
    }
    return MapDestinationDetails(
        name = title,
        distanceText = "${distanceKm.format1()} km",
        temperatureText = "${approxTemp}°C",
    )
}

fun legacyBuildDestinationDetails(
    title: String,
    userLocation: Point?,
    destination: Point,
): MapDestinationDetails {
    val distanceKm = userLocation?.let { haversineDistanceKm(it, destination) } ?: 1.3
    return MapDestinationDetails(
        name = title,
        distanceText = "${distanceKm.format1()} km",
        temperatureText = null,
    )
}

fun buildDestinationDetails(
    title: String,
    userLocation: Point?,
    destination: Point,
    temperatureCelsius: Int? = null,
): MapDestinationDetails {
    val distanceKm = userLocation?.let { haversineDistanceKm(it, destination) } ?: 1.3
    return MapDestinationDetails(
        name = title,
        distanceText = "${distanceKm.format1()} km",
        temperatureText = temperatureCelsius?.let { "${it}\u00B0C" },
    )
}

private fun formatTemperatureValue(value: Int?): String = value?.let { "${it}\u00B0C" } ?: "N/A"

private fun formatUvExposureDuration(value: Int?): String = value?.let { "$it min" } ?: "N/A"

private fun haversineDistanceKm(start: Point, end: Point): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(end.latitude() - start.latitude())
    val dLng = Math.toRadians(end.longitude() - start.longitude())
    val startLat = Math.toRadians(start.latitude())
    val endLat = Math.toRadians(end.latitude())

    val a = sin(dLat / 2).pow(2) + sin(dLng / 2).pow(2) * cos(startLat) * cos(endLat)
    val c = 2 * asin(sqrt(a))
    return earthRadiusKm * c
}

private fun Double.format1(): String = String.format("%.1f", this)
