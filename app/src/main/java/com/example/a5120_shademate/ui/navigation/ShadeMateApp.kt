package com.example.a5120_shademate.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.a5120_shademate.data.CoolPlaceRepository
import com.example.a5120_shademate.data.HeatRepository
import com.example.a5120_shademate.data.HomeOverviewRepository
import com.example.a5120_shademate.data.TemperatureAwareCoolPlaceRepository
import com.example.a5120_shademate.data.api.ApiHeatRepository
import com.example.a5120_shademate.data.api.ApiCoolPlaceRepository
import com.example.a5120_shademate.data.api.CoolPlaceTemperatureResolver
import com.example.a5120_shademate.data.api.ApiHomeOverviewRepository
import com.example.a5120_shademate.data.SampleCoolPlaceRepository
import com.example.a5120_shademate.data.SampleHomeOverviewRepository
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import com.example.a5120_shademate.ui.screens.CoolPlacesScreen
import com.example.a5120_shademate.ui.screens.CoolPlaceDetailScreen
import com.example.a5120_shademate.ui.screens.EducationScreen
import com.example.a5120_shademate.ui.screens.HomeScreen
import com.example.a5120_shademate.ui.screens.MapScreen
import com.example.a5120_shademate.ui.screens.ProfileCustomisationScreen
import com.example.a5120_shademate.ui.screens.RouteScreen
import com.example.a5120_shademate.ui.theme.ShadeMatePalette
import com.example.a5120_shademate.ui.theme.ShadeMateTheme

private enum class AppDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    MAP("HeatMap", Icons.Outlined.Map),
    ROUTE("Route", Icons.Default.Route),
    COOL_PLACES("Cool Places", Icons.Outlined.Eco),
    AWARENESS("Awareness", Icons.Outlined.Info),
}

@Composable
fun ShadeMateApp() {
    var selectedDestination by remember { mutableStateOf(AppDestination.HOME) }
    var selectedPlaceForDetail by remember { mutableStateOf<CoolPlaceCardData?>(null) }
    var pendingRoutePlace by remember { mutableStateOf<CoolPlaceCardData?>(null) }
    var isBottomBarVisible by remember { mutableStateOf(true) }
    var isProfileCustomisationOpen by remember { mutableStateOf(false) }
    var bottomBarHeightPx by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    val repository = remember { ApiHeatRepository() }
    val sampleCoolPlaceRepository = remember { SampleCoolPlaceRepository() }
    val sampleHomeOverviewRepository = remember { SampleHomeOverviewRepository() }
    val db = remember(context) { com.example.a5120_shademate.data.local.AppDatabase.getDatabase(context) }
    // Cool places prefer backend data but stay demo-safe by falling back to sample content.
    val coolPlaceRepository = remember(sampleCoolPlaceRepository, db) {
        TemperatureAwareCoolPlaceRepository(
            delegate = ApiCoolPlaceRepository(fallbackRepository = sampleCoolPlaceRepository),
            temperatureResolver = CoolPlaceTemperatureResolver(weatherDao = db.weatherDao()),
        )
    }
    // Home overview combines backend payloads with cached suburb weather enrichment.
    val homeOverviewRepository = remember(db) {
        ApiHomeOverviewRepository(
            context = context,
            weatherDao = db.weatherDao(),
        )
    }
    LaunchedEffect(selectedDestination, selectedPlaceForDetail) {
        isBottomBarVisible = true
    }
    val bottomBarVisibilityProgress by animateFloatAsState(
        targetValue = if (
            selectedPlaceForDetail == null &&
            !isProfileCustomisationOpen &&
            isBottomBarVisible
        ) {
            1f
        } else {
            0f
        },
        animationSpec = tween(
            durationMillis = 520,
            easing = FastOutSlowInEasing,
        ),
        label = "bottom_bar_visibility_progress",
    )

    Scaffold(
        containerColor = ShadeMatePalette.AppBackground,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (selectedPlaceForDetail == null && !isProfileCustomisationOpen) {
                Card(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = (1f - bottomBarVisibilityProgress) * (bottomBarHeightPx + 64f)
                            alpha = bottomBarVisibilityProgress
                        }
                        .onSizeChanged { bottomBarHeightPx = it.height.toFloat() }
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.84f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        AppDestination.entries.forEach { destination ->
                            BottomBarItem(
                                modifier = Modifier.weight(1f),
                                selected = selectedDestination == destination,
                                onClick = { selectedDestination = destination },
                                icon = destination.icon,
                                label = destination.label,
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ShadeMateAppContent(
                innerPadding = innerPadding,
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it },
                selectedPlaceForDetail = selectedPlaceForDetail,
                onPlaceSelected = { selectedPlaceForDetail = it },
                onPlaceDetailDismissed = { selectedPlaceForDetail = null },
                pendingRoutePlace = pendingRoutePlace,
                onNavigateToPlace = { place ->
                    selectedPlaceForDetail = null
                    pendingRoutePlace = place
                    selectedDestination = AppDestination.ROUTE
                },
                onNavigationConsumed = { pendingRoutePlace = null },
                isProfileCustomisationOpen = isProfileCustomisationOpen,
                onOpenProfileCustomisation = { isProfileCustomisationOpen = true },
                onCloseProfileCustomisation = { isProfileCustomisationOpen = false },
                onBottomBarVisibilityChanged = { isVisible ->
                    isBottomBarVisible = isVisible
                },
                repository = repository,
                coolPlaceRepository = coolPlaceRepository,
                homeOverviewRepository = homeOverviewRepository,
            )
        }
    }
}

@Composable
private fun ShadeMateAppContent(
    innerPadding: PaddingValues,
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    selectedPlaceForDetail: CoolPlaceCardData?,
    onPlaceSelected: (CoolPlaceCardData) -> Unit,
    onPlaceDetailDismissed: () -> Unit,
    pendingRoutePlace: CoolPlaceCardData?,
    onNavigateToPlace: (CoolPlaceCardData) -> Unit,
    onNavigationConsumed: () -> Unit,
    isProfileCustomisationOpen: Boolean,
    onOpenProfileCustomisation: () -> Unit,
    onCloseProfileCustomisation: () -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    repository: HeatRepository,
    coolPlaceRepository: CoolPlaceRepository,
    homeOverviewRepository: HomeOverviewRepository,
) {
    if (isProfileCustomisationOpen) {
        ProfileCustomisationScreen(
            onBack = onCloseProfileCustomisation,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
        return
    }

    if (selectedPlaceForDetail != null) {
        // Reuse the selected card data so the detail screen can open immediately without another fetch.
        CoolPlaceDetailScreen(
            place = selectedPlaceForDetail,
            onBack = onPlaceDetailDismissed,
            onNavigate = onNavigateToPlace,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
        return
    }

    when (selectedDestination) {
        AppDestination.HOME -> {
            HomeScreen(
                homeOverviewRepository = homeOverviewRepository,
                coolPlaceRepository = coolPlaceRepository,
                onOpenMap = { onDestinationSelected(AppDestination.MAP) },
                onOpenProfileCustomisation = onOpenProfileCustomisation,
                onRecommendedPlaceSelected = onPlaceSelected,
                onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
                modifier = Modifier.fillMaxSize(),
            )
        }

        AppDestination.MAP -> {
            MapScreen(
                repository = repository,
                modifier = Modifier.fillMaxSize(),
            )
        }

        AppDestination.ROUTE -> {
            RouteScreen(
                coolPlaceRepository = coolPlaceRepository,
                initialDestination = pendingRoutePlace,
                onConsumedDestination = onNavigationConsumed,
                onNearbyPlaceSelected = onPlaceSelected,
                onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
                modifier = Modifier.fillMaxSize(),
            )
        }

        AppDestination.COOL_PLACES -> {
            CoolPlacesScreen(
                repository = coolPlaceRepository,
                onPlaceSelected = onPlaceSelected,
                modifier = Modifier.fillMaxSize(),
            )
        }

        AppDestination.AWARENESS -> {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                EducationScreen(
                    repository = repository,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(78.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) ShadeMatePalette.SelectedNavBackground.copy(alpha = 0.96f) else Color.Transparent,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 5.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) Color.White else Color.Transparent,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .size(24.dp),
                        tint = if (selected) ShadeMatePalette.PrimaryText else ShadeMatePalette.SecondaryText,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    color = if (selected) ShadeMatePalette.PrimaryGreen else ShadeMatePalette.SecondaryText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 10.sp,
                        fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ShadeMateAppPreview() {
    ShadeMateTheme {
        ShadeMateApp()
    }
}
