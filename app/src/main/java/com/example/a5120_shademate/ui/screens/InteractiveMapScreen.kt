package com.example.a5120_shademate.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Color as AndroidColor
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a5120_shademate.data.local.AppDatabase
import com.example.a5120_shademate.ui.viewmodel.MapViewModel
import com.example.a5120_shademate.ui.viewmodel.MapViewModelFactory
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.*
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.IconRotationAlignment
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.SymbolPlacement
import com.mapbox.maps.extension.style.layers.properties.generated.SymbolZOrder
import com.mapbox.maps.extension.style.types.StyleTransition
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.delay

private const val SEARCH_TARGET_SOURCE_ID = "search-target-source"
private const val SEARCH_TARGET_IMAGE_ID = "search-target-pin-image"
private const val SEARCH_TARGET_LAYER_ID = "search-target-symbol-layer"
private const val ROUTE_COOL_PLACE_SOURCE_ID = "route-cool-place-source"
private const val ROUTE_COOL_PLACE_IMAGE_ID = "route-cool-place-pin-image"
private const val ROUTE_COOL_PLACE_LAYER_ID = "route-cool-place-symbol-layer"
private const val SELECTED_HEAT_SOURCE_ID = "selected-heat-source"
private const val SELECTED_HEAT_IMAGE_ID = "selected-heat-pin-image"
private const val SELECTED_HEAT_LAYER_ID = "selected-heat-symbol-layer"
private const val ROUTE_OPTIONS_SOURCE_ID = "route-options-source"
private const val ROUTE_OPTIONS_CASING_LAYER_ID = "route-options-casing-layer"
private const val ROUTE_OPTIONS_LAYER_ID = "route-options-layer"
private const val ROUTE_OPTIONS_COOLEST_SOURCE_ID = "route-options-coolest-source"
private const val ROUTE_OPTIONS_COOLEST_CASING_LAYER_ID = "route-options-coolest-casing-layer"
private const val ROUTE_OPTIONS_COOLEST_LAYER_ID = "route-options-coolest-layer"
private const val ROUTE_OPTIONS_REGULAR_SOURCE_ID = "route-options-regular-source"
private const val ROUTE_OPTIONS_REGULAR_CASING_LAYER_ID = "route-options-regular-casing-layer"
private const val ROUTE_OPTIONS_REGULAR_LAYER_ID = "route-options-regular-layer"
private const val SELECTED_ROUTE_SOURCE_ID = "selected-route-source"
private const val SELECTED_ROUTE_CASING_LAYER_ID = "selected-route-casing-layer"
private const val SELECTED_ROUTE_LAYER_ID = "selected-route-layer"
private const val SHADED_ROUTE_SOURCE_ID = "shaded-route-source"
private const val SHADED_ROUTE_LAYER_ID = "shaded-route-layer"
private const val ROUTE_OPTIONS_COOLEST_ARROW_LAYER_ID = "route-options-coolest-arrow-layer"
private const val ROUTE_OPTIONS_REGULAR_ARROW_LAYER_ID = "route-options-regular-arrow-layer"
private const val SELECTED_ROUTE_ARROW_LAYER_ID = "selected-route-arrow-layer"
private const val COOL_ROUTE_SELECTED_ARROW_IMAGE_ID = "cool-route-selected-arrow-image"
private const val COOL_ROUTE_UNSELECTED_ARROW_IMAGE_ID = "cool-route-unselected-arrow-image"
private const val REGULAR_ROUTE_SELECTED_ARROW_IMAGE_ID = "regular-route-selected-arrow-image"
private const val REGULAR_ROUTE_UNSELECTED_ARROW_IMAGE_ID = "regular-route-unselected-arrow-image"
private const val LGA_LINE_LAYER_ID = "lga-line-layer"
private const val ROUTE_STYLE_TRANSITION_MS = 260L
private const val ROUTE_FEATURE_TYPE_KEY = "route_type"
private const val ROUTE_FEATURE_ID_KEY = "route_id"
private const val COOL_ROUTE_TYPE = "COOL_ROUTE"
private const val REGULAR_ROUTE_TYPE = "REGULAR_ROUTE"
private val RouteStyleTransition = StyleTransition.Builder()
    .duration(ROUTE_STYLE_TRANSITION_MS)
    .delay(0L)
    .build()

private enum class RouteVisualType {
    COOL_ROUTE,
    REGULAR_ROUTE,
}

// Route styling is centralized here so visual tuning stays separate from route loading logic.
private data class RouteStyle(
    val mainColor: String,
    val outlineColor: String,
    val mainWidth: Double,
    val outlineWidth: Double,
    val opacity: Double,
    val outlineOpacity: Double,
    val zIndex: Int,
    val arrowImageId: String,
    val arrowSize: Double,
    val arrowOpacity: Double,
    val arrowSpacing: Double,
)

private val coolRouteSelectedStyle = RouteStyle(
    mainColor = "#1FAE9B",
    outlineColor = "#A7E8DC",
    mainWidth = 9.0,
    outlineWidth = 13.0,
    opacity = 1.0,
    outlineOpacity = 0.56,
    zIndex = 30,
    arrowImageId = COOL_ROUTE_SELECTED_ARROW_IMAGE_ID,
    arrowSize = 1.06,
    arrowOpacity = 1.0,
    arrowSpacing = 122.0,
)

private val coolRouteUnselectedStyle = RouteStyle(
    mainColor = "#83D6C8",
    outlineColor = "#D8F4EF",
    mainWidth = 5.0,
    outlineWidth = 8.2,
    opacity = 0.60,
    outlineOpacity = 0.24,
    zIndex = 10,
    arrowImageId = COOL_ROUTE_UNSELECTED_ARROW_IMAGE_ID,
    arrowSize = 0.76,
    arrowOpacity = 0.62,
    arrowSpacing = 164.0,
)

private val regularRouteSelectedStyle = RouteStyle(
    mainColor = "#FF8A4C",
    outlineColor = "#FFD6BF",
    mainWidth = 9.0,
    outlineWidth = 13.0,
    opacity = 1.0,
    outlineOpacity = 0.50,
    zIndex = 30,
    arrowImageId = REGULAR_ROUTE_SELECTED_ARROW_IMAGE_ID,
    arrowSize = 1.06,
    arrowOpacity = 1.0,
    arrowSpacing = 122.0,
)

private val regularRouteUnselectedStyle = RouteStyle(
    mainColor = "#F2B18E",
    outlineColor = "#F8E1D4",
    mainWidth = 5.0,
    outlineWidth = 8.2,
    opacity = 0.58,
    outlineOpacity = 0.26,
    zIndex = 10,
    arrowImageId = REGULAR_ROUTE_UNSELECTED_ARROW_IMAGE_ID,
    arrowSize = 0.76,
    arrowOpacity = 0.60,
    arrowSpacing = 164.0,
)

data class SelectedHeatLocation(
    val featureId: String,
    val name: String,
    val temperatureCelsius: Double?,
    val point: Point,
)

@Composable
fun InteractiveMapScreen(
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    enableWeatherUpdates: Boolean = true,
    isRouteModeActive: Boolean = false,
    searchTarget: Point? = null,
    nearbyCoolPlaces: List<RouteNearbyCoolPlace> = emptyList(),
    routeOptions: List<MockRouteOption> = emptyList(),
    selectedRouteIndex: Int = 0,
    clearSelectedHeatLocationToken: Int = 0,
    onUserLocationChanged: (Point?) -> Unit = {},
    onNearbyCoolPlaceTapped: (RouteNearbyCoolPlace?) -> Unit = {},
    onHeatZonesUpdated: (List<com.example.a5120_shademate.model.HeatZone>) -> Unit = {},
    onSelectedHeatLocationChanged: (SelectedHeatLocation?) -> Unit = {},
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val density = LocalDensity.current
    val viewModel: MapViewModel = viewModel(factory = MapViewModelFactory(db))
    val state by viewModel.state.collectAsState()
    val activeTemperatureCache by viewModel.getActiveViewModel().temperatureCache.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val routeViewportPadding = remember(density) {
        EdgeInsets(
            with(density) { 168.dp.toPx().toDouble() },
            with(density) { 28.dp.toPx().toDouble() },
            with(density) { 320.dp.toPx().toDouble() },
            with(density) { 28.dp.toPx().toDouble() },
        )
    }
    var currentUserLocation by remember { mutableStateOf<Point?>(null) }
    var hasAutoCenteredOnUser by remember { mutableStateOf(false) }
    var selectedHeatLocation by remember { mutableStateOf<SelectedHeatLocation?>(null) }
    val shouldRenderHeatOverlays = enableWeatherUpdates && !isRouteModeActive

    val suburbLayerId = "sal-2021-aust-gda2020-shp-ad1f0o"
    val suburbLineLayerId = "suburb-line-layer"
    val suburbLabelLayerId = "suburb-label-layer"
    val streetHeatmapLayerId = "gisfeedback-ad251p26"

    val mapView = remember {
        MapView(context).apply {
            val customStyleUri = "mapbox://styles/dt0324/cmnyokpca002c01shh1zv24s3"
            mapboxMap.loadStyle(customStyleUri) { style ->
                viewModel.suburbVm.setupLayer(style)
                viewModel.lgaVm.setupLayer(style)
                setupEnhancedLayers(
                    style,
                    suburbLayerId,
                    suburbLineLayerId,
                    suburbLabelLayerId,
                    streetHeatmapLayerId
                )
                updateHeatLayersForMode(
                    style = style,
                    state = state,
                    isRouteModeActive = isRouteModeActive,
                    lgaLayerId = viewModel.lgaVm.layerId,
                    lgaLineLayerId = LGA_LINE_LAYER_ID,
                    suburbLayerId = suburbLayerId,
                    suburbLineLayerId = suburbLineLayerId,
                    suburbLabelLayerId = suburbLabelLayerId,
                    streetHeatmapLayerId = streetHeatmapLayerId,
                )
                updateRouteOverlays(style, routeOptions, selectedRouteIndex)
                updateRouteCoolPlaceMarkers(style, nearbyCoolPlaces)
                updateSearchTargetMarker(style, searchTarget)
                updateSelectedHeatMarker(style, selectedHeatLocation?.point)
            }
            mapboxMap.setCamera(
                CameraOptions.Builder().center(Point.fromLngLat(144.9631, -37.8136)).zoom(12.0).build()
            )
            mapboxMap.subscribeCameraChanged {
                viewModel.onZoomChanged(mapboxMap.cameraState.zoom.toFloat())
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            enableLocation(mapView, primaryColor)
        }
    }

    val indicatorPositionChangedListener = remember(mapView) {
        OnIndicatorPositionChangedListener { point ->
            currentUserLocation = point
            onUserLocationChanged(point)
            if (!hasAutoCenteredOnUser) {
                centerCameraOnUser(mapView, point)
                hasAutoCenteredOnUser = true
            }
        }
    }

    val performQuery: () -> Unit = {
        if (shouldRenderHeatOverlays && mapView.width > 0) {
            val activeVm = viewModel.getActiveViewModel()
            val targetLayerId = activeVm.layerId
            val box = ScreenBox(
                ScreenCoordinate(0.0, 0.0),
                ScreenCoordinate(mapView.width.toDouble(), mapView.height.toDouble())
            )

            mapView.mapboxMap.getStyle { style ->
                updateHeatLayersForMode(
                    style = style,
                    state = state,
                    isRouteModeActive = isRouteModeActive,
                    lgaLayerId = viewModel.lgaVm.layerId,
                    lgaLineLayerId = LGA_LINE_LAYER_ID,
                    suburbLayerId = viewModel.suburbVm.layerId,
                    suburbLineLayerId = suburbLineLayerId,
                    suburbLabelLayerId = suburbLabelLayerId,
                    streetHeatmapLayerId = streetHeatmapLayerId,
                )
            }

            Log.d("MapUpdate", "Performing query for layer: $targetLayerId")
            mapView.mapboxMap.queryRenderedFeatures(
                RenderedQueryGeometry(box),
                RenderedQueryOptions(listOf(targetLayerId), null)
            ) { expected ->
                if (expected.isValue) {
                    val features = expected.value ?: emptyList()
                    Log.d("MapUpdate", "Found ${features.size} features")

                    val dataToUpdate = activeVm.extractDataFromFeatures(features)

                    if (dataToUpdate.isNotEmpty()) {
                        val sampleFeature = features.firstOrNull()
                        val src = sampleFeature?.queriedFeature?.source ?: "composite"
                        val srcLayer = sampleFeature?.queriedFeature?.sourceLayer ?: ""

                        activeVm.updateWeatherData(src, srcLayer, dataToUpdate, mapView.mapboxMap) {
                            mapView.mapboxMap.getStyle { style ->
                                val layer = style.getLayer(targetLayerId) as? FillLayer
                                val expression = activeVm.getColorExpression()
                                layer?.fillColor(expression)
                                Log.d("MapUpdate", "Applied color expression to $targetLayerId")
                            }
                            onHeatZonesUpdated(activeVm.getTopHotZones(3))
                        }
                    }
                } else {
                    Log.e("MapUpdate", "Query failed: ${expected.error}")
                }
            }
        }
    }

    val currentPerformQuery by rememberUpdatedState(performQuery)

    LaunchedEffect(mapView.mapboxMap) {
        var lastUpdate = 0L
        mapView.mapboxMap.subscribeCameraChanged {
            val now = System.currentTimeMillis()
            if (now - lastUpdate > 1500) {
                currentPerformQuery()
                lastUpdate = now
            }
        }
        mapView.mapboxMap.subscribeMapIdle {
            currentPerformQuery()
        }
        delay(1000)
        currentPerformQuery()
    }

    LaunchedEffect(state.isLgaMode) {
        currentPerformQuery()
    }

    LaunchedEffect(isRouteModeActive) {
        mapView.mapboxMap.getStyle { style ->
            updateHeatLayersForMode(
                style = style,
                state = state,
                isRouteModeActive = isRouteModeActive,
                lgaLayerId = viewModel.lgaVm.layerId,
                lgaLineLayerId = LGA_LINE_LAYER_ID,
                suburbLayerId = suburbLayerId,
                suburbLineLayerId = suburbLineLayerId,
                suburbLabelLayerId = suburbLabelLayerId,
                streetHeatmapLayerId = streetHeatmapLayerId,
            )
        }
        if (shouldRenderHeatOverlays) {
            currentPerformQuery()
        }
    }

    LaunchedEffect(state.isLgaMode, onHeatZonesUpdated) {
        val cachedZones = viewModel.getActiveViewModel().getTopHotZones(3)
        if (cachedZones.isNotEmpty()) {
            onHeatZonesUpdated(cachedZones)
        }
    }

    LaunchedEffect(searchTarget) {
        mapView.mapboxMap.getStyle { style ->
            updateSearchTargetMarker(style, searchTarget)
        }
        searchTarget?.let { point ->
            hasAutoCenteredOnUser = true
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(point)
                    .zoom(15.0)
                    .build()
            )
        }
    }

    LaunchedEffect(routeOptions, selectedRouteIndex) {
        mapView.mapboxMap.getStyle { style ->
            updateRouteOverlays(style, routeOptions, selectedRouteIndex)
        }
        val selectedRoute = routeOptions.getOrNull(selectedRouteIndex)
            ?: routeOptions.firstOrNull()
        if (selectedRoute != null && selectedRoute.points.isNotEmpty()) {
            hasAutoCenteredOnUser = true
            mapView.mapboxMap.setCamera(
                mapView.mapboxMap.cameraForCoordinates(
                    selectedRoute.points,
                    routeViewportPadding,
                    null,
                    null,
                )
            )
        }
    }

    LaunchedEffect(nearbyCoolPlaces) {
        mapView.mapboxMap.getStyle { style ->
            updateRouteCoolPlaceMarkers(style, nearbyCoolPlaces)
        }
        if (nearbyCoolPlaces.isEmpty()) {
            onNearbyCoolPlaceTapped(null)
        }
    }

    val heatTapListener = remember(mapView, shouldRenderHeatOverlays, isRouteModeActive) {
        OnMapClickListener { point ->
            if (!shouldRenderHeatOverlays || isRouteModeActive) {
                false
            } else {
                val isHeatSelectionEnabled = !viewModel.state.value.isLgaMode
                if (!isHeatSelectionEnabled) {
                    selectedHeatLocation = null
                    mapView.mapboxMap.getStyle { style ->
                        updateSelectedHeatMarker(style, null)
                    }
                    false
                } else {
                    val activeVm = viewModel.getActiveViewModel()
                    val screenPoint = mapView.mapboxMap.pixelForCoordinate(point)
                    mapView.mapboxMap.queryRenderedFeatures(
                        RenderedQueryGeometry(screenPoint),
                        RenderedQueryOptions(listOf(activeVm.layerId), null)
                    ) { expected ->
                        if (expected.isValue) {
                            val selectedFeature = expected.value
                                ?.firstOrNull { it.layers.contains(activeVm.layerId) }
                                ?.queriedFeature
                                ?.feature
                            val featureId = selectedFeature?.let(activeVm::getFeatureId)
                            if (selectedFeature != null && featureId != null) {
                                val areaName = selectedFeature.getProperty(
                                    if (activeVm.isLga) "LGA_NAME25" else "SAL_NAME21"
                                )?.asString ?: featureId
                                val temperature = activeVm.temperatureCache.value[featureId]
                                selectedHeatLocation = SelectedHeatLocation(
                                    featureId = featureId,
                                    name = areaName,
                                    temperatureCelsius = temperature,
                                    point = point,
                                )
                                mapView.mapboxMap.getStyle { style ->
                                    updateSelectedHeatMarker(style, point)
                                }
                            } else {
                                selectedHeatLocation = null
                                mapView.mapboxMap.getStyle { style ->
                                    updateSelectedHeatMarker(style, null)
                                }
                            }
                        } else {
                            selectedHeatLocation = null
                            mapView.mapboxMap.getStyle { style ->
                                updateSelectedHeatMarker(style, null)
                            }
                        }
                    }
                    true
                }
            }
        }
    }

    val routeCoolPlaceTapListener = remember(mapView, isRouteModeActive, nearbyCoolPlaces) {
        OnMapClickListener { point ->
            if (!isRouteModeActive || nearbyCoolPlaces.isEmpty()) {
                false
            } else {
                val screenPoint = mapView.mapboxMap.pixelForCoordinate(point)
                mapView.mapboxMap.queryRenderedFeatures(
                    RenderedQueryGeometry(screenPoint),
                    RenderedQueryOptions(listOf(ROUTE_COOL_PLACE_LAYER_ID), null)
                ) { expected ->
                    if (expected.isValue) {
                        val placeId = expected.value
                            ?.firstOrNull { it.layers.contains(ROUTE_COOL_PLACE_LAYER_ID) }
                            ?.queriedFeature
                            ?.feature
                            ?.getProperty("place_id")
                            ?.asString
                        val selectedPlace = nearbyCoolPlaces.firstOrNull { it.place.id == placeId }
                        onNearbyCoolPlaceTapped(selectedPlace)
                    } else {
                        onNearbyCoolPlaceTapped(null)
                    }
                }
                true
            }
        }
    }

    DisposableEffect(mapView, indicatorPositionChangedListener) {
        mapView.location.addOnIndicatorPositionChangedListener(indicatorPositionChangedListener)
        onDispose {
            mapView.location.removeOnIndicatorPositionChangedListener(indicatorPositionChangedListener)
        }
    }

    DisposableEffect(mapView, heatTapListener) {
        mapView.gestures.addOnMapClickListener(heatTapListener)
        onDispose {
            mapView.gestures.removeOnMapClickListener(heatTapListener)
        }
    }

    DisposableEffect(mapView, routeCoolPlaceTapListener) {
        mapView.gestures.addOnMapClickListener(routeCoolPlaceTapListener)
        onDispose {
            mapView.gestures.removeOnMapClickListener(routeCoolPlaceTapListener)
        }
    }

    LaunchedEffect(Unit) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissions.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            enableLocation(mapView, primaryColor)
        } else {
            locationPermissionLauncher.launch(permissions)
        }
    }

    LaunchedEffect(selectedHeatLocation) {
        mapView.mapboxMap.getStyle { style ->
            updateSelectedHeatMarker(style, selectedHeatLocation?.point)
        }
        onSelectedHeatLocationChanged(selectedHeatLocation)
    }

    LaunchedEffect(activeTemperatureCache) {
        val currentSelection = selectedHeatLocation ?: return@LaunchedEffect
        val refreshedTemperature = activeTemperatureCache[currentSelection.featureId]
        if (refreshedTemperature != currentSelection.temperatureCelsius) {
            selectedHeatLocation = currentSelection.copy(temperatureCelsius = refreshedTemperature)
        }
    }

    LaunchedEffect(state.isLgaMode, isRouteModeActive) {
        if (state.isLgaMode || isRouteModeActive) {
            selectedHeatLocation = null
            mapView.mapboxMap.getStyle { style ->
                updateSelectedHeatMarker(style, null)
            }
        }
    }

    LaunchedEffect(clearSelectedHeatLocationToken) {
        if (clearSelectedHeatLocationToken != 0) {
            selectedHeatLocation = null
            mapView.mapboxMap.getStyle { style ->
                updateSelectedHeatMarker(style, null)
            }
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        if (showControls) {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                color = ComposeColor.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                val label = when {
                    isRouteModeActive -> "Route Preview"
                    state.isLgaMode -> "LGA Overview"
                    else -> "Suburb Details"
                }
                Text(
                    label,
                    color = ComposeColor.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (currentUserLocation != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 128.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = ComposeColor.White.copy(alpha = 0.96f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                IconButton(
                    onClick = {
                        currentUserLocation?.let { centerCameraOnUser(mapView, it) }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Center on current location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun setupEnhancedLayers(
    style: Style,
    suburbId: String,
    lineId: String,
    labelId: String,
    streetId: String
) {
    val fillLayer = style.getLayer(suburbId) as? FillLayer ?: return
    val sourceId = fillLayer.sourceId
    val sourceLayerId = fillLayer.sourceLayer ?: return

    fillLayer.fillColor(
        Expression.interpolate(
            Expression.linear(),
            Expression.coalesce(
                Expression.featureState(Expression.literal("temp")),
                Expression.literal(-1.0)
            ),
            Expression.literal(-1.0), Expression.color(AndroidColor.WHITE), // Not loaded yet
            Expression.literal(10.0), Expression.color(AndroidColor.BLUE),  // Initial placeholder
            Expression.literal(40.0), Expression.color(AndroidColor.RED)
        )
    )
    fillLayer.fillOpacity(
        Expression.interpolate(
            Expression.linear(),
            Expression.zoom(),
            Expression.literal(12.5),
            Expression.literal(0.6),
            Expression.literal(13.0),
            Expression.literal(0.0)
        )
    )

    if (style.getLayer(lineId) == null) {
        val lineLayer = LineLayer(lineId, sourceId)
        lineLayer.sourceLayer(sourceLayerId)
        lineLayer.lineColor(AndroidColor.BLACK)
        lineLayer.lineWidth(0.8)
        lineLayer.lineOpacity(
            Expression.interpolate(
                Expression.linear(),
                Expression.zoom(),
                Expression.literal(12.5),
                Expression.literal(0.4),
                Expression.literal(13.0),
                Expression.literal(0.0)
            )
        )
        style.addLayer(lineLayer)
    }

    if (style.getLayer(labelId) == null) {
        val symbolLayer = SymbolLayer(labelId, sourceId)
        symbolLayer.sourceLayer(sourceLayerId)

        symbolLayer.textField(
            Expression.concat(
                Expression.coalesce(
                    Expression.toString(Expression.number(Expression.featureState(Expression.literal("temp")))),
                    Expression.literal("--")
                ),
                Expression.literal("°C")
            )
        )

        symbolLayer.textSize(14.0)
        symbolLayer.textColor(AndroidColor.BLACK)
        symbolLayer.textHaloColor(AndroidColor.WHITE)
        symbolLayer.textHaloWidth(1.5)
        symbolLayer.textAllowOverlap(true)
        symbolLayer.textIgnorePlacement(true)
        symbolLayer.symbolZOrder(SymbolZOrder.AUTO)

        style.addLayer(symbolLayer)
    }

    val streetLayer = style.getLayer(streetId)
    when (streetLayer) {
        is FillLayer -> streetLayer.fillOpacity(
            Expression.interpolate(
                Expression.linear(),
                Expression.zoom(),
                Expression.literal(12.5),
                Expression.literal(0.0),
                Expression.literal(13.0),
                Expression.literal(0.8)
            )
        )
        is RasterLayer -> streetLayer.rasterOpacity(
            Expression.interpolate(
                Expression.linear(),
                Expression.zoom(),
                Expression.literal(12.5),
                Expression.literal(0.0),
                Expression.literal(13.0),
                Expression.literal(1.0)
            )
        )
        is HeatmapLayer -> streetLayer.heatmapOpacity(
            Expression.interpolate(
                Expression.linear(),
                Expression.zoom(),
                Expression.literal(12.5),
                Expression.literal(0.0),
                Expression.literal(13.0),
                Expression.literal(1.0)
            )
        )
    }

    ensureRouteLayers(style)
    ensureRouteCoolPlaceLayers(style)
    ensureSearchTargetLayers(style)
}

private fun updateHeatLayersForMode(
    style: Style,
    state: com.example.a5120_shademate.ui.viewmodel.MapState,
    isRouteModeActive: Boolean,
    lgaLayerId: String,
    lgaLineLayerId: String,
    suburbLayerId: String,
    suburbLineLayerId: String,
    suburbLabelLayerId: String,
    streetHeatmapLayerId: String,
) {
    val lgaOpacity = if (isRouteModeActive || state.currentZoom >= 10f) 0.0 else 0.3
    val suburbOpacity = if (isRouteModeActive || state.currentZoom < 10f) 0.0 else 0.5
    val buildingOpacity = 1.0
    val suburbLabelOpacity = if (isRouteModeActive) 0.0 else 1.0

    (style.getLayer(lgaLayerId) as? FillLayer)?.fillOpacity(lgaOpacity)
    (style.getLayer(lgaLineLayerId) as? LineLayer)?.lineOpacity(
        if (isRouteModeActive) 0.0 else if (state.currentZoom < 10f) 0.8 else 0.0
    )
    (style.getLayer(suburbLayerId) as? FillLayer)?.fillOpacity(suburbOpacity)
    (style.getLayer(suburbLineLayerId) as? LineLayer)?.lineOpacity(
        if (isRouteModeActive) 0.0 else if (state.currentZoom >= 10f) 0.4 else 0.0
    )
    (style.getLayer(suburbLabelLayerId) as? SymbolLayer)?.textOpacity(suburbLabelOpacity)
    (style.getLayer("building") as? FillLayer)?.fillOpacity(buildingOpacity)

    when (val streetLayer = style.getLayer(streetHeatmapLayerId)) {
        is FillLayer -> {
            val opacity = if (isRouteModeActive) {
                Expression.literal(0.0)
            } else {
                Expression.interpolate(
                    Expression.linear(),
                    Expression.zoom(),
                    Expression.literal(12.5),
                    Expression.literal(0.0),
                    Expression.literal(13.0),
                    Expression.literal(0.8)
                )
            }
            streetLayer.fillOpacity(opacity)
        }
        is RasterLayer -> {
            val opacity = if (isRouteModeActive) {
                Expression.literal(0.0)
            } else {
                Expression.interpolate(
                    Expression.linear(),
                    Expression.zoom(),
                    Expression.literal(12.5),
                    Expression.literal(0.0),
                    Expression.literal(13.0),
                    Expression.literal(1.0)
                )
            }
            streetLayer.rasterOpacity(opacity)
        }
        is HeatmapLayer -> {
            val opacity = if (isRouteModeActive) {
                Expression.literal(0.0)
            } else {
                Expression.interpolate(
                    Expression.linear(),
                    Expression.zoom(),
                    Expression.literal(12.5),
                    Expression.literal(0.0),
                    Expression.literal(13.0),
                    Expression.literal(1.0)
                )
            }
            streetLayer.heatmapOpacity(opacity)
        }
    }
}

private fun routeVisualType(route: MockRouteOption?): RouteVisualType {
    return if (route?.isCoolest == true) RouteVisualType.COOL_ROUTE else RouteVisualType.REGULAR_ROUTE
}

private fun routeVisualStyle(
    routeType: RouteVisualType,
    isSelected: Boolean,
): RouteStyle {
    return when (routeType) {
        RouteVisualType.COOL_ROUTE -> if (isSelected) coolRouteSelectedStyle else coolRouteUnselectedStyle
        RouteVisualType.REGULAR_ROUTE -> if (isSelected) regularRouteSelectedStyle else regularRouteUnselectedStyle
    }
}

private fun LineLayer.applyRouteLineDefaults(): LineLayer {
    return lineCap(LineCap.ROUND)
        .lineJoin(LineJoin.ROUND)
        .lineColorTransition(RouteStyleTransition)
        .lineWidthTransition(RouteStyleTransition)
        .lineOpacityTransition(RouteStyleTransition)
        .lineBlurTransition(RouteStyleTransition)
}

private fun LineLayer.applyRouteVisualStyle(
    routeStyle: RouteStyle,
    isOutline: Boolean,
): LineLayer {
    return lineColor(if (isOutline) routeStyle.outlineColor else routeStyle.mainColor)
        .lineWidth(if (isOutline) routeStyle.outlineWidth else routeStyle.mainWidth)
        .lineOpacity(if (isOutline) routeStyle.outlineOpacity else routeStyle.opacity)
        .lineBlur(if (isOutline) 0.15 else 0.0)
        .lineSortKey(routeStyle.zIndex.toDouble())
}

private fun SymbolLayer.applyRouteArrowDefaults(): SymbolLayer {
    return symbolPlacement(SymbolPlacement.LINE)
        .iconAnchor(IconAnchor.CENTER)
        .iconRotationAlignment(IconRotationAlignment.MAP)
        .iconKeepUpright(false)
        .iconAllowOverlap(true)
        .iconIgnorePlacement(true)
        .iconOpacityTransition(RouteStyleTransition)
        .iconImageCrossFadeTransition(RouteStyleTransition)
}

private fun SymbolLayer.applyRouteArrowStyle(routeStyle: RouteStyle): SymbolLayer {
    return iconImage(routeStyle.arrowImageId)
        .iconSize(routeStyle.arrowSize)
        .iconOpacity(routeStyle.arrowOpacity)
        .symbolSpacing(routeStyle.arrowSpacing)
}

private fun ensureRouteArrowImages(style: Style) {
    if (style.getStyleImage(COOL_ROUTE_SELECTED_ARROW_IMAGE_ID) == null) {
        style.addImage(
            COOL_ROUTE_SELECTED_ARROW_IMAGE_ID,
            createRouteArrowBitmap(
                arrowColor = "#EFFFFA",
                contrastColor = "#167D6D",
                contrastAlpha = 132,
            )
        )
    }
    if (style.getStyleImage(COOL_ROUTE_UNSELECTED_ARROW_IMAGE_ID) == null) {
        style.addImage(
            COOL_ROUTE_UNSELECTED_ARROW_IMAGE_ID,
            createRouteArrowBitmap(
                arrowColor = "#F4FFFC",
                contrastColor = "#269B86",
                contrastAlpha = 84,
            )
        )
    }
    if (style.getStyleImage(REGULAR_ROUTE_SELECTED_ARROW_IMAGE_ID) == null) {
        style.addImage(
            REGULAR_ROUTE_SELECTED_ARROW_IMAGE_ID,
            createRouteArrowBitmap(
                arrowColor = "#FFF4EC",
                contrastColor = "#B9633C",
                contrastAlpha = 124,
            )
        )
    }
    if (style.getStyleImage(REGULAR_ROUTE_UNSELECTED_ARROW_IMAGE_ID) == null) {
        style.addImage(
            REGULAR_ROUTE_UNSELECTED_ARROW_IMAGE_ID,
            createRouteArrowBitmap(
                arrowColor = "#FFF8F2",
                contrastColor = "#D28B66",
                contrastAlpha = 82,
            )
        )
    }
}

private fun ensureRouteLayers(style: Style) {
    val coolUnselectedStyle = routeVisualStyle(RouteVisualType.COOL_ROUTE, isSelected = false)
    val regularUnselectedStyle = routeVisualStyle(RouteVisualType.REGULAR_ROUTE, isSelected = false)
    val selectedCoolStyle = routeVisualStyle(RouteVisualType.COOL_ROUTE, isSelected = true)

    if (style.getSourceAs<GeoJsonSource>(ROUTE_OPTIONS_SOURCE_ID) == null) {
        style.addSource(
            geoJsonSource(ROUTE_OPTIONS_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
            }
        )
    }

    if (style.getSourceAs<GeoJsonSource>(ROUTE_OPTIONS_COOLEST_SOURCE_ID) == null) {
        style.addSource(
            geoJsonSource(ROUTE_OPTIONS_COOLEST_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
            }
        )
    }

    if (style.getSourceAs<GeoJsonSource>(ROUTE_OPTIONS_REGULAR_SOURCE_ID) == null) {
        style.addSource(
            geoJsonSource(ROUTE_OPTIONS_REGULAR_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
            }
        )
    }

    if (style.getSourceAs<GeoJsonSource>(SELECTED_ROUTE_SOURCE_ID) == null) {
        style.addSource(
            geoJsonSource(SELECTED_ROUTE_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
            }
        )
    }

    if (style.getSourceAs<GeoJsonSource>(SHADED_ROUTE_SOURCE_ID) == null) {
        style.addSource(
            geoJsonSource(SHADED_ROUTE_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
            }
        )
    }

    if (style.getLayer(ROUTE_OPTIONS_CASING_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(ROUTE_OPTIONS_CASING_LAYER_ID, ROUTE_OPTIONS_SOURCE_ID)
                .applyRouteLineDefaults()
                .lineOpacity(0.0)
        )
    }

    if (style.getLayer(ROUTE_OPTIONS_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(ROUTE_OPTIONS_LAYER_ID, ROUTE_OPTIONS_SOURCE_ID)
                .applyRouteLineDefaults()
                .lineOpacity(0.0)
        )
    }

    if (style.getLayer(ROUTE_OPTIONS_COOLEST_CASING_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(ROUTE_OPTIONS_COOLEST_CASING_LAYER_ID, ROUTE_OPTIONS_COOLEST_SOURCE_ID)
                .applyRouteLineDefaults()
                .applyRouteVisualStyle(coolUnselectedStyle, isOutline = true)
        )
    }

    if (style.getLayer(ROUTE_OPTIONS_COOLEST_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(ROUTE_OPTIONS_COOLEST_LAYER_ID, ROUTE_OPTIONS_COOLEST_SOURCE_ID)
                .applyRouteLineDefaults()
                .applyRouteVisualStyle(coolUnselectedStyle, isOutline = false)
        )
    }

    ensureRouteArrowImages(style)

    if (style.getLayer(ROUTE_OPTIONS_COOLEST_ARROW_LAYER_ID) == null) {
        style.addLayer(
            SymbolLayer(ROUTE_OPTIONS_COOLEST_ARROW_LAYER_ID, ROUTE_OPTIONS_COOLEST_SOURCE_ID)
                .applyRouteArrowDefaults()
                .applyRouteArrowStyle(coolUnselectedStyle)
        )
    }

    if (style.getLayer(ROUTE_OPTIONS_REGULAR_CASING_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(ROUTE_OPTIONS_REGULAR_CASING_LAYER_ID, ROUTE_OPTIONS_REGULAR_SOURCE_ID)
                .applyRouteLineDefaults()
                .applyRouteVisualStyle(regularUnselectedStyle, isOutline = true)
        )
    }

    if (style.getLayer(ROUTE_OPTIONS_REGULAR_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(ROUTE_OPTIONS_REGULAR_LAYER_ID, ROUTE_OPTIONS_REGULAR_SOURCE_ID)
                .applyRouteLineDefaults()
                .applyRouteVisualStyle(regularUnselectedStyle, isOutline = false)
        )
    }

    if (style.getLayer(ROUTE_OPTIONS_REGULAR_ARROW_LAYER_ID) == null) {
        style.addLayer(
            SymbolLayer(ROUTE_OPTIONS_REGULAR_ARROW_LAYER_ID, ROUTE_OPTIONS_REGULAR_SOURCE_ID)
                .applyRouteArrowDefaults()
                .applyRouteArrowStyle(regularUnselectedStyle)
        )
    }

    if (style.getLayer(SELECTED_ROUTE_CASING_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(SELECTED_ROUTE_CASING_LAYER_ID, SELECTED_ROUTE_SOURCE_ID)
                .applyRouteLineDefaults()
                .applyRouteVisualStyle(selectedCoolStyle, isOutline = true)
        )
    }

    if (style.getLayer(SELECTED_ROUTE_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(SELECTED_ROUTE_LAYER_ID, SELECTED_ROUTE_SOURCE_ID)
                .applyRouteLineDefaults()
                .applyRouteVisualStyle(selectedCoolStyle, isOutline = false)
        )
    }

    if (style.getLayer(SHADED_ROUTE_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(SHADED_ROUTE_LAYER_ID, SHADED_ROUTE_SOURCE_ID)
                .applyRouteLineDefaults()
                .lineColor("#E9FFF6")
                .lineWidth(3.8)
                .lineOpacity(0.66)
                .lineBlur(0.25)
        )
    }

    if (style.getLayer(SELECTED_ROUTE_ARROW_LAYER_ID) == null) {
        style.addLayer(
            SymbolLayer(SELECTED_ROUTE_ARROW_LAYER_ID, SELECTED_ROUTE_SOURCE_ID)
                .applyRouteArrowDefaults()
                .applyRouteArrowStyle(selectedCoolStyle)
        )
    }
}

private fun updateRouteOverlays(
    style: Style,
    routeOptions: List<MockRouteOption>,
    selectedRouteIndex: Int,
) {
    ensureRouteLayers(style)

    val allRoutesSource = style.getSourceAs<GeoJsonSource>(ROUTE_OPTIONS_SOURCE_ID) ?: return
    val coolestRoutesSource = style.getSourceAs<GeoJsonSource>(ROUTE_OPTIONS_COOLEST_SOURCE_ID) ?: return
    val regularRoutesSource = style.getSourceAs<GeoJsonSource>(ROUTE_OPTIONS_REGULAR_SOURCE_ID) ?: return
    val selectedRouteSource = style.getSourceAs<GeoJsonSource>(SELECTED_ROUTE_SOURCE_ID) ?: return
    val shadedRouteSource = style.getSourceAs<GeoJsonSource>(SHADED_ROUTE_SOURCE_ID) ?: return

    if (routeOptions.isEmpty()) {
        allRoutesSource.featureCollection(FeatureCollection.fromFeatures(emptyArray()))
        coolestRoutesSource.featureCollection(FeatureCollection.fromFeatures(emptyArray()))
        regularRoutesSource.featureCollection(FeatureCollection.fromFeatures(emptyArray()))
        selectedRouteSource.featureCollection(FeatureCollection.fromFeatures(emptyArray()))
        shadedRouteSource.featureCollection(FeatureCollection.fromFeatures(emptyArray()))
        return
    }

    val coolUnselectedStyle = routeVisualStyle(RouteVisualType.COOL_ROUTE, isSelected = false)
    val regularUnselectedStyle = routeVisualStyle(RouteVisualType.REGULAR_ROUTE, isSelected = false)
    (style.getLayer(ROUTE_OPTIONS_COOLEST_CASING_LAYER_ID) as? LineLayer)
        ?.applyRouteVisualStyle(coolUnselectedStyle, isOutline = true)
    (style.getLayer(ROUTE_OPTIONS_COOLEST_LAYER_ID) as? LineLayer)
        ?.applyRouteVisualStyle(coolUnselectedStyle, isOutline = false)
    (style.getLayer(ROUTE_OPTIONS_REGULAR_CASING_LAYER_ID) as? LineLayer)
        ?.applyRouteVisualStyle(regularUnselectedStyle, isOutline = true)
    (style.getLayer(ROUTE_OPTIONS_REGULAR_LAYER_ID) as? LineLayer)
        ?.applyRouteVisualStyle(regularUnselectedStyle, isOutline = false)

    val selectedRoute = routeOptions.getOrNull(selectedRouteIndex)
    val alternativeCoolRoutes = routeOptions.filterIndexed { index, route ->
        index != selectedRouteIndex && route.isCoolest
    }
    val alternativeRegularRoutes = routeOptions.filterIndexed { index, route ->
        index != selectedRouteIndex && !route.isCoolest
    }

    val routeFeatures = routeOptions.map { route -> buildRouteFeature(route) }
    allRoutesSource.featureCollection(FeatureCollection.fromFeatures(routeFeatures))
    coolestRoutesSource.featureCollection(
        FeatureCollection.fromFeatures(
            alternativeCoolRoutes.map { route -> buildRouteFeature(route) }
        )
    )
    regularRoutesSource.featureCollection(
        FeatureCollection.fromFeatures(
            alternativeRegularRoutes.map { route -> buildRouteFeature(route) }
        )
    )

    val selectedFeatures = if (selectedRoute != null) {
        arrayOf(buildRouteFeature(selectedRoute))
    } else {
        emptyArray()
    }
    selectedRouteSource.featureCollection(FeatureCollection.fromFeatures(selectedFeatures))

    val selectedStyle = routeVisualStyle(routeVisualType(selectedRoute), isSelected = true)
    (style.getLayer(SELECTED_ROUTE_CASING_LAYER_ID) as? LineLayer)
        ?.applyRouteVisualStyle(selectedStyle, isOutline = true)
    (style.getLayer(SELECTED_ROUTE_LAYER_ID) as? LineLayer)
        ?.applyRouteVisualStyle(selectedStyle, isOutline = false)
    (style.getLayer(ROUTE_OPTIONS_COOLEST_ARROW_LAYER_ID) as? SymbolLayer)
        ?.applyRouteArrowStyle(coolUnselectedStyle)
    (style.getLayer(ROUTE_OPTIONS_REGULAR_ARROW_LAYER_ID) as? SymbolLayer)
        ?.applyRouteArrowStyle(regularUnselectedStyle)
    (style.getLayer(SELECTED_ROUTE_ARROW_LAYER_ID) as? SymbolLayer)
        ?.applyRouteArrowStyle(selectedStyle)

    val shadedFeatures = selectedRoute?.let { route ->
        if (route.shadeAvailable == true || route.shadedSegments.isNotEmpty()) buildShadedRouteFeatures(route) else emptyArray()
    } ?: emptyArray()
    shadedRouteSource.featureCollection(FeatureCollection.fromFeatures(shadedFeatures))
}

private fun buildRouteFeature(route: MockRouteOption): Feature {
    return Feature.fromGeometry(LineString.fromLngLats(route.points)).apply {
        addStringProperty(ROUTE_FEATURE_ID_KEY, route.id)
        addStringProperty(ROUTE_FEATURE_TYPE_KEY, if (route.isCoolest) COOL_ROUTE_TYPE else REGULAR_ROUTE_TYPE)
    }
}

private fun ensureSearchTargetLayers(style: Style) {
    if (style.getSourceAs<GeoJsonSource>(SEARCH_TARGET_SOURCE_ID) == null) {
        style.addSource(
            geoJsonSource(SEARCH_TARGET_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
            }
        )
    }

    if (style.getStyleImage(SEARCH_TARGET_IMAGE_ID) == null) {
        style.addImage(SEARCH_TARGET_IMAGE_ID, createSearchTargetPinBitmap())
    }

    if (style.getLayer(SEARCH_TARGET_LAYER_ID) == null) {
        style.addLayer(
            SymbolLayer(SEARCH_TARGET_LAYER_ID, SEARCH_TARGET_SOURCE_ID)
                .iconImage(SEARCH_TARGET_IMAGE_ID)
                .iconAnchor(IconAnchor.BOTTOM)
                .iconAllowOverlap(true)
                .iconIgnorePlacement(true)
        )
    }
}

private fun ensureRouteCoolPlaceLayers(style: Style) {
    if (style.getSourceAs<GeoJsonSource>(ROUTE_COOL_PLACE_SOURCE_ID) == null) {
        style.addSource(
            geoJsonSource(ROUTE_COOL_PLACE_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
            }
        )
    }

    if (style.getStyleImage(ROUTE_COOL_PLACE_IMAGE_ID) == null) {
        style.addImage(ROUTE_COOL_PLACE_IMAGE_ID, createRouteCoolPlacePinBitmap())
    }

    if (style.getLayer(ROUTE_COOL_PLACE_LAYER_ID) == null) {
        style.addLayer(
            SymbolLayer(ROUTE_COOL_PLACE_LAYER_ID, ROUTE_COOL_PLACE_SOURCE_ID)
                .iconImage(ROUTE_COOL_PLACE_IMAGE_ID)
                .iconAnchor(IconAnchor.BOTTOM)
                .iconAllowOverlap(true)
                .iconIgnorePlacement(true)
        )
    }
}

private fun updateRouteCoolPlaceMarkers(
    style: Style,
    nearbyCoolPlaces: List<RouteNearbyCoolPlace>,
) {
    ensureRouteCoolPlaceLayers(style)
    val source = style.getSourceAs<GeoJsonSource>(ROUTE_COOL_PLACE_SOURCE_ID) ?: return
    val features = nearbyCoolPlaces.map { routePlace ->
        Feature.fromGeometry(
            Point.fromLngLat(routePlace.place.longitude, routePlace.place.latitude)
        ).apply {
            addStringProperty("place_id", routePlace.place.id)
        }
    }.toTypedArray()
    source.featureCollection(FeatureCollection.fromFeatures(features))
}

private fun updateSearchTargetMarker(style: Style, searchTarget: Point?) {
    ensureSearchTargetLayers(style)
    val source = style.getSourceAs<GeoJsonSource>(SEARCH_TARGET_SOURCE_ID) ?: return
    val collection = if (searchTarget != null) {
        FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(searchTarget)))
    } else {
        FeatureCollection.fromFeatures(emptyArray())
    }
    source.featureCollection(collection)
}

private fun ensureSelectedHeatLayers(style: Style) {
    if (style.getSourceAs<GeoJsonSource>(SELECTED_HEAT_SOURCE_ID) == null) {
        style.addSource(
            geoJsonSource(SELECTED_HEAT_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
            }
        )
    }

    if (style.getStyleImage(SELECTED_HEAT_IMAGE_ID) == null) {
        style.addImage(SELECTED_HEAT_IMAGE_ID, createSelectedHeatPinBitmap())
    }

    if (style.getLayer(SELECTED_HEAT_LAYER_ID) == null) {
        style.addLayer(
            SymbolLayer(SELECTED_HEAT_LAYER_ID, SELECTED_HEAT_SOURCE_ID)
                .iconImage(SELECTED_HEAT_IMAGE_ID)
                .iconAnchor(IconAnchor.BOTTOM)
                .iconAllowOverlap(true)
                .iconIgnorePlacement(true)
        )
    }
}

private fun updateSelectedHeatMarker(style: Style, point: Point?) {
    ensureSelectedHeatLayers(style)
    val source = style.getSourceAs<GeoJsonSource>(SELECTED_HEAT_SOURCE_ID) ?: return
    val collection = if (point != null) {
        FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(point)))
    } else {
        FeatureCollection.fromFeatures(emptyArray())
    }
    source.featureCollection(collection)
}

private fun createSearchTargetPinBitmap(): Bitmap {
    val width = 84
    val height = 108
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(216, 67, 58)
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    val innerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }

    val circleRect = RectF(12f, 8f, 72f, 68f)
    val path = Path().apply {
        addOval(circleRect, Path.Direction.CW)
        moveTo(42f, 98f)
        lineTo(24f, 54f)
        lineTo(60f, 54f)
        close()
    }

    canvas.drawPath(path, fillPaint)
    canvas.drawPath(path, strokePaint)
    canvas.drawCircle(42f, 38f, 11f, innerDotPaint)

    return bitmap
}

private fun createRouteCoolPlacePinBitmap(): Bitmap {
    val width = 70
    val height = 94
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(71, 122, 49)
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    val innerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }

    val circleRect = RectF(10f, 8f, 60f, 58f)
    val path = Path().apply {
        addOval(circleRect, Path.Direction.CW)
        moveTo(35f, 84f)
        lineTo(21f, 45f)
        lineTo(49f, 45f)
        close()
    }

    canvas.drawPath(path, fillPaint)
    canvas.drawPath(path, strokePaint)
    canvas.drawCircle(35f, 33f, 8f, innerDotPaint)

    return bitmap
}

private fun createSelectedHeatPinBitmap(): Bitmap {
    val width = 76
    val height = 98
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(255, 152, 0)
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    val innerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }

    val circleRect = RectF(11f, 8f, 65f, 62f)
    val path = Path().apply {
        addOval(circleRect, Path.Direction.CW)
        moveTo(38f, 88f)
        lineTo(23f, 49f)
        lineTo(53f, 49f)
        close()
    }

    canvas.drawPath(path, fillPaint)
    canvas.drawPath(path, strokePaint)
    canvas.drawCircle(38f, 35f, 9f, innerDotPaint)
    return bitmap
}

private fun createRouteArrowBitmap(
    arrowColor: String,
    contrastColor: String,
    contrastAlpha: Int,
): Bitmap {
    val width = 68
    val height = 26
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val roundedChevronCorners = CornerPathEffect(2.2f)

    val contrastBaseColor = AndroidColor.parseColor(contrastColor)
    val contrastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(
            contrastAlpha.coerceIn(0, 255),
            AndroidColor.red(contrastBaseColor),
            AndroidColor.green(contrastBaseColor),
            AndroidColor.blue(contrastBaseColor),
        )
        style = Paint.Style.STROKE
        strokeWidth = 2.4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        pathEffect = roundedChevronCorners
    }
    val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor(arrowColor)
        style = Paint.Style.FILL
        pathEffect = roundedChevronCorners
    }

    fun drawFilledChevron(
        startX: Float,
        topY: Float,
        bottomY: Float,
        width: Float,
        shoulderWidth: Float,
        notchDepth: Float,
    ) {
        val centerY = height / 2f
        val path = Path().apply {
            moveTo(startX, topY)
            lineTo(startX + shoulderWidth, topY)
            lineTo(startX + width, centerY)
            lineTo(startX + shoulderWidth, bottomY)
            lineTo(startX, bottomY)
            lineTo(startX + notchDepth, centerY)
            close()
        }
        canvas.drawPath(path, contrastPaint)
        canvas.drawPath(path, arrowPaint)
    }

    drawFilledChevron(startX = 7.0f, topY = 7.0f, bottomY = 19.0f, width = 11.6f, shoulderWidth = 5.0f, notchDepth = 3.2f)
    drawFilledChevron(startX = 19.3f, topY = 5.6f, bottomY = 20.4f, width = 13.8f, shoulderWidth = 6.1f, notchDepth = 3.8f)
    drawFilledChevron(startX = 33.6f, topY = 4.2f, bottomY = 21.8f, width = 16.2f, shoulderWidth = 7.2f, notchDepth = 4.4f)
    return bitmap
}

private fun buildShadedRouteFeatures(route: MockRouteOption): Array<Feature> {
    if (route.shadedSegments.isNotEmpty()) {
        return route.shadedSegments.mapNotNull { segment ->
            if (segment.size >= 2) Feature.fromGeometry(LineString.fromLngLats(segment)) else null
        }.toTypedArray()
    }

    return emptyArray()
}

private fun enableLocation(mapView: MapView, puckColor: Int) {
    mapView.location.apply {
        enabled = true
        pulsingEnabled = true
        pulsingColor = puckColor
        locationPuck = LocationPuck2D()
    }
}

private fun centerCameraOnUser(mapView: MapView, point: Point) {
    val targetZoom = maxOf(mapView.mapboxMap.cameraState.zoom, 12.0)
    mapView.mapboxMap.setCamera(
        CameraOptions.Builder()
            .center(point)
            .zoom(targetZoom)
            .build()
    )
}

@Composable
private fun SelectedHeatLocationCard(
    location: SelectedHeatLocation,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ComposeColor.White.copy(alpha = 0.97f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
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
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Lat ${"%.5f".format(location.point.latitude())}, Lng ${"%.5f".format(location.point.longitude())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close selected suburb details",
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = location.temperatureCelsius?.let { "${it.toInt()}°C" } ?: "Temperature unavailable",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
