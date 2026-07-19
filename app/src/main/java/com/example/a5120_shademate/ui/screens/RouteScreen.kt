package com.example.a5120_shademate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.a5120_shademate.data.CoolPlaceRepository
import com.example.a5120_shademate.data.api.WalkingRouteRepository
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private enum class RouteInputField {
    ORIGIN,
    DESTINATION,
}

private val DefaultRouteOrigin = Point.fromLngLat(144.9631, -37.8136)

@Composable
fun RouteScreen(
    coolPlaceRepository: CoolPlaceRepository,
    initialDestination: CoolPlaceCardData? = null,
    onConsumedDestination: () -> Unit = {},
    onNearbyPlaceSelected: (CoolPlaceCardData) -> Unit = {},
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val walkingRouteRepository = remember(context) { WalkingRouteRepository(context) }
    val placeAutocomplete = remember { PlaceAutocomplete.create(locationProvider = null) }
    val searchScope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<Point?>(null) }
    var routeScreenState by remember {
        mutableStateOf<RouteScreenState>(RouteScreenState.Empty("Set a destination to preview walking route options."))
    }
    var selectedRouteIndex by rememberSaveable { mutableStateOf(0) }
    var routeOriginQuery by rememberSaveable { mutableStateOf("Current location") }
    var routeDestinationQuery by rememberSaveable { mutableStateOf("") }
    var routeOriginPoint by remember { mutableStateOf<Point?>(null) }
    var routeDestinationPoint by remember { mutableStateOf<Point?>(null) }
    var activeRouteField by remember { mutableStateOf<RouteInputField?>(null) }
    var routeSuggestions by remember { mutableStateOf<List<PlaceAutocompleteSuggestion>>(emptyList()) }
    var isRouteSearching by remember { mutableStateOf(false) }
    var routeSearchErrorMessage by remember { mutableStateOf<String?>(null) }
    var suppressNextRouteAutocomplete by remember { mutableStateOf(false) }
    var availableCoolPlaces by remember { mutableStateOf<List<CoolPlaceCardData>>(emptyList()) }
    var nearbyCoolPlaces by remember { mutableStateOf<List<RouteNearbyCoolPlace>>(emptyList()) }
    var selectedNearbyCoolPlace by remember { mutableStateOf<RouteNearbyCoolPlace?>(null) }

    fun resolveOriginForPreview(): Point? {
        return routeOriginPoint ?: if (routeOriginQuery == "Current location") {
            userLocation ?: DefaultRouteOrigin
        } else {
            null
        }
    }

    suspend fun refreshRoutePreview(
        origin: Point,
        destination: Point,
    ) {
        routeScreenState = RouteScreenState.Loading
        routeScreenState = loadRouteOptions(
            walkingRouteRepository = walkingRouteRepository,
            origin = origin,
            destination = destination,
        )
        selectedRouteIndex = 0
    }

    fun dismissRoutePreview() {
        routeScreenState = RouteScreenState.Empty("Set a destination to preview walking route options.")
        selectedRouteIndex = 0
        routeDestinationQuery = ""
        routeDestinationPoint = null
        routeSuggestions = emptyList()
        activeRouteField = null
        isRouteSearching = false
        routeSearchErrorMessage = null
        suppressNextRouteAutocomplete = false
        nearbyCoolPlaces = emptyList()
        selectedNearbyCoolPlace = null
        onBottomBarVisibilityChanged(true)
    }

    LaunchedEffect(Unit) {
        onBottomBarVisibilityChanged(true)
    }

    LaunchedEffect(initialDestination) {
        if (initialDestination != null) {
            val destinationPoint = Point.fromLngLat(initialDestination.longitude, initialDestination.latitude)
            val origin = resolveOriginForPreview() ?: DefaultRouteOrigin

            routeOriginQuery = "Current location"
            routeOriginPoint = origin
            routeDestinationPoint = destinationPoint
            routeDestinationQuery = initialDestination.name
            activeRouteField = null
            routeSuggestions = emptyList()
            routeSearchErrorMessage = null
            suppressNextRouteAutocomplete = true

            searchScope.launch {
                refreshRoutePreview(origin, destinationPoint)
            }

            onConsumedDestination()
        }
    }

    LaunchedEffect(coolPlaceRepository) {
        availableCoolPlaces = runCatching { coolPlaceRepository.getCoolPlaces() }
            .getOrDefault(emptyList())
    }

    LaunchedEffect(activeRouteField, routeOriginQuery, routeDestinationQuery) {
        val activeField = activeRouteField ?: run {
            routeSuggestions = emptyList()
            routeSearchErrorMessage = null
            isRouteSearching = false
            return@LaunchedEffect
        }
        val activeQuery = when (activeField) {
            RouteInputField.ORIGIN -> routeOriginQuery
            RouteInputField.DESTINATION -> routeDestinationQuery
        }.trim()

        if (suppressNextRouteAutocomplete) {
            suppressNextRouteAutocomplete = false
            routeSuggestions = emptyList()
            routeSearchErrorMessage = null
            isRouteSearching = false
            return@LaunchedEffect
        }
        if (activeQuery.length < 2 || activeQuery == "Current location") {
            routeSuggestions = emptyList()
            routeSearchErrorMessage = null
            isRouteSearching = false
            return@LaunchedEffect
        }

        isRouteSearching = true
        routeSearchErrorMessage = null
        delay(350)

        val suggestionsResponse = placeAutocomplete.suggestions(query = activeQuery)
        suggestionsResponse
            .onValue { suggestions ->
                routeSuggestions = suggestions.take(6)
            }
            .onError { error ->
                routeSuggestions = emptyList()
                routeSearchErrorMessage = error.message ?: "Search is unavailable right now."
            }
        isRouteSearching = false
    }

    fun selectRouteSuggestion(suggestion: PlaceAutocompleteSuggestion) {
        val targetField = activeRouteField ?: return
        searchScope.launch {
            isRouteSearching = true
            routeSearchErrorMessage = null

            val selectedResponse = placeAutocomplete.select(suggestion)
            selectedResponse
                .onValue { result ->
                    suppressNextRouteAutocomplete = true
                    when (targetField) {
                        RouteInputField.ORIGIN -> {
                            routeOriginQuery = result.name
                            routeOriginPoint = result.coordinate
                        }

                        RouteInputField.DESTINATION -> {
                            routeDestinationQuery = result.name
                            routeDestinationPoint = result.coordinate
                        }
                    }
                    routeSuggestions = emptyList()
                    activeRouteField = null
                }
                .onError { error ->
                    suppressNextRouteAutocomplete = true
                    suggestion.coordinate?.let { point ->
                        when (targetField) {
                            RouteInputField.ORIGIN -> {
                                routeOriginQuery = suggestion.name
                                routeOriginPoint = point
                            }

                            RouteInputField.DESTINATION -> {
                                routeDestinationQuery = suggestion.name
                                routeDestinationPoint = point
                            }
                        }
                    }
                    routeSuggestions = emptyList()
                    routeSearchErrorMessage = error.message ?: "Couldn't load place details."
                    activeRouteField = null
                }

            val origin = resolveOriginForPreview()
            val destination = routeDestinationPoint
            if (origin != null && destination != null) {
                refreshRoutePreview(origin, destination)
            } else {
                routeScreenState = RouteScreenState.Error("Set both start and destination to preview route options.")
            }

            isRouteSearching = false
        }
    }

    LaunchedEffect(routeScreenState, selectedRouteIndex, availableCoolPlaces, routeDestinationPoint) {
        val readyState = routeScreenState as? RouteScreenState.Ready
        val selectedRoute = readyState?.result?.options?.getOrNull(selectedRouteIndex)
        if (selectedRoute == null) {
            nearbyCoolPlaces = emptyList()
            selectedNearbyCoolPlace = null
            return@LaunchedEffect
        }

        nearbyCoolPlaces = buildNearbyCoolPlaces(
            route = selectedRoute,
            coolPlaces = availableCoolPlaces,
            destinationPoint = routeDestinationPoint,
        )
        selectedNearbyCoolPlace = selectedNearbyCoolPlace?.let { current ->
            nearbyCoolPlaces.firstOrNull { it.place.id == current.place.id }
        }
    }

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        InteractiveMapScreen(
            modifier = Modifier.fillMaxSize(),
            showControls = true,
            enableWeatherUpdates = false,
            isRouteModeActive = true,
            searchTarget = routeDestinationPoint,
            nearbyCoolPlaces = nearbyCoolPlaces,
            routeOptions = if (routeScreenState is RouteScreenState.Ready) {
                (routeScreenState as RouteScreenState.Ready).result.options
            } else {
                emptyList()
            },
            selectedRouteIndex = selectedRouteIndex,
            onUserLocationChanged = { point ->
                userLocation = point
                if (routeOriginQuery == "Current location" && routeOriginPoint == null) {
                    routeOriginPoint = point
                }
            },
            onNearbyCoolPlaceTapped = { selectedNearbyCoolPlace = it },
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

        CoolRouteScreen(
            originQuery = routeOriginQuery,
            destinationQuery = routeDestinationQuery,
            onOriginQueryChange = {
                suppressNextRouteAutocomplete = false
                routeOriginQuery = it
                routeOriginPoint = if (it == "Current location") userLocation else null
                activeRouteField = RouteInputField.ORIGIN
            },
            onDestinationQueryChange = {
                suppressNextRouteAutocomplete = false
                routeDestinationQuery = it
                routeDestinationPoint = null
                activeRouteField = RouteInputField.DESTINATION
            },
            onOriginFieldFocused = { activeRouteField = RouteInputField.ORIGIN },
            onDestinationFieldFocused = { activeRouteField = RouteInputField.DESTINATION },
            suggestions = routeSuggestions,
            isSearching = isRouteSearching,
            errorMessage = routeSearchErrorMessage,
            onSuggestionSelected = ::selectRouteSuggestion,
            routeState = routeScreenState,
            selectedRouteIndex = selectedRouteIndex,
            onSelectedRouteChange = { selectedRouteIndex = it },
            onDismissRoute = ::dismissRoutePreview,
            onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
            onRetry = {
                searchScope.launch {
                    val origin = resolveOriginForPreview()
                    val destination = routeDestinationPoint
                    if (origin != null && destination != null) {
                        refreshRoutePreview(origin, destination)
                    } else {
                        routeScreenState = RouteScreenState.Error("Set both start and destination to preview route options.")
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        )

        selectedNearbyCoolPlace?.let { nearbyPlace ->
            NearbyCoolPlaceRouteCard(
                nearbyPlace = nearbyPlace,
                onOpenPlace = { onNearbyPlaceSelected(nearbyPlace.place) },
                onDismiss = { selectedNearbyCoolPlace = null },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 122.dp),
            )
        }
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

private fun buildNearbyCoolPlaces(
    route: MockRouteOption,
    coolPlaces: List<CoolPlaceCardData>,
    destinationPoint: Point?,
    maxRouteDistanceMeters: Double = 250.0,
    fallbackRouteDistanceMeters: Double = 500.0,
    maxItems: Int = 5,
): List<RouteNearbyCoolPlace> {
    val candidates = coolPlaces.mapNotNull { place ->
        val placePoint = Point.fromLngLat(place.longitude, place.latitude)
        if (destinationPoint != null && haversineDistanceMeters(destinationPoint, placePoint) < 60.0) {
            return@mapNotNull null
        }
        val distanceToRouteMeters = minimumDistanceToRouteMeters(placePoint, route.points)
        RouteNearbyCoolPlace(
            place = place,
            routeDistanceMeters = distanceToRouteMeters.roundToInt(),
        )
    }.sortedBy { it.routeDistanceMeters }

    val nearby = candidates.filter { it.routeDistanceMeters <= maxRouteDistanceMeters }
        .take(maxItems)
    if (nearby.isNotEmpty()) {
        return nearby
    }

    return candidates.filter { it.routeDistanceMeters <= fallbackRouteDistanceMeters }
        .take(maxItems)
}

private fun minimumDistanceToRouteMeters(point: Point, routePoints: List<Point>): Double {
    if (routePoints.isEmpty()) return Double.MAX_VALUE
    if (routePoints.size == 1) return haversineDistanceMeters(point, routePoints.first())

    return routePoints.zipWithNext()
        .minOf { (start, end) -> distanceToSegmentMeters(point, start, end) }
}

private fun distanceToSegmentMeters(point: Point, segmentStart: Point, segmentEnd: Point): Double {
    val referenceLatRad = Math.toRadians(
        (point.latitude() + segmentStart.latitude() + segmentEnd.latitude()) / 3.0
    )
    val pointX = projectLongitudeMeters(point.longitude(), referenceLatRad)
    val pointY = projectLatitudeMeters(point.latitude())
    val startX = projectLongitudeMeters(segmentStart.longitude(), referenceLatRad)
    val startY = projectLatitudeMeters(segmentStart.latitude())
    val endX = projectLongitudeMeters(segmentEnd.longitude(), referenceLatRad)
    val endY = projectLatitudeMeters(segmentEnd.latitude())

    val dx = endX - startX
    val dy = endY - startY
    if (dx == 0.0 && dy == 0.0) {
        return distanceMeters(pointX, pointY, startX, startY)
    }

    val t = (((pointX - startX) * dx) + ((pointY - startY) * dy)) / (dx * dx + dy * dy)
    val clampedT = t.coerceIn(0.0, 1.0)
    val projectionX = startX + clampedT * dx
    val projectionY = startY + clampedT * dy
    return distanceMeters(pointX, pointY, projectionX, projectionY)
}

private fun haversineDistanceMeters(start: Point, end: Point): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = Math.toRadians(end.latitude() - start.latitude())
    val dLng = Math.toRadians(end.longitude() - start.longitude())
    val startLat = Math.toRadians(start.latitude())
    val endLat = Math.toRadians(end.latitude())

    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(startLat) * cos(endLat) * sin(dLng / 2) * sin(dLng / 2)
    return earthRadiusMeters * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun projectLongitudeMeters(longitude: Double, referenceLatRad: Double): Double {
    return longitude * 111_320.0 * cos(referenceLatRad)
}

private fun projectLatitudeMeters(latitude: Double): Double {
    return latitude * 110_540.0
}

private fun distanceMeters(x1: Double, y1: Double, x2: Double, y2: Double): Double {
    val dx = x2 - x1
    val dy = y2 - y1
    return sqrt(dx * dx + dy * dy)
}
