package com.example.a5120_shademate.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a5120_shademate.data.CoolPlaceRepository
import com.example.a5120_shademate.data.HomeOverviewRepository
import com.example.a5120_shademate.model.HomeHeatOverview
import com.example.a5120_shademate.model.LoadState
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class HomeCoordinates(
    val latitude: Double,
    val longitude: Double,
)

class HomeViewModel(
    context: Context,
    private val homeOverviewRepository: HomeOverviewRepository,
    private val coolPlaceRepository: CoolPlaceRepository,
) : ViewModel() {

    private val appContext = context.applicationContext
    private var activeLoadRequestId by mutableIntStateOf(0)

    var heatOverviewState by mutableStateOf<LoadState<HomeHeatOverview>>(LoadState.Loading)
        private set

    var coolPlacesState by mutableStateOf<LoadState<List<CoolPlaceCardData>>>(LoadState.Loading)
        private set

    var currentCoordinates by mutableStateOf(readCachedHomeCoordinates(appContext))
        private set

    fun hasVisibleCachedData(): Boolean {
        return heatOverviewState !is LoadState.Loading || coolPlacesState !is LoadState.Loading
    }

    suspend fun loadUsingCachedOrDefault(
        defaultLat: Double,
        defaultLon: Double,
        showLoadingState: Boolean,
    ) {
        val initialCoordinates = currentCoordinates ?: HomeCoordinates(defaultLat, defaultLon)
        if (currentCoordinates == null) {
            currentCoordinates = initialCoordinates
        }
        loadData(
            lat = initialCoordinates.latitude,
            lon = initialCoordinates.longitude,
            showLoadingState = showLoadingState,
        )
    }

    suspend fun refreshForResolvedCoordinates(
        coordinates: HomeCoordinates?,
        defaultLat: Double,
        defaultLon: Double,
    ) {
        if (coordinates == null) {
            if (!hasVisibleCachedData()) {
                loadUsingCachedOrDefault(
                    defaultLat = defaultLat,
                    defaultLon = defaultLon,
                    showLoadingState = true,
                )
            }
            return
        }

        val coordinatesChanged = currentCoordinates?.isSameLocation(coordinates) != true
        cacheCoordinates(coordinates)
        if (coordinatesChanged || !hasVisibleCachedData()) {
            loadData(
                lat = coordinates.latitude,
                lon = coordinates.longitude,
                showLoadingState = false,
            )
        }
    }

    suspend fun forceRefreshCurrentCoordinates(
        coordinates: HomeCoordinates?,
        defaultLat: Double,
        defaultLon: Double,
    ) {
        coordinates?.let(::cacheCoordinates)
        loadData(
            lat = coordinates?.latitude ?: defaultLat,
            lon = coordinates?.longitude ?: defaultLon,
            showLoadingState = !hasVisibleCachedData(),
        )
    }

    private suspend fun loadData(
        lat: Double,
        lon: Double,
        showLoadingState: Boolean,
    ) {
        val requestId = ++activeLoadRequestId
        if (showLoadingState) {
            heatOverviewState = LoadState.Loading
            coolPlacesState = LoadState.Loading
        }

        supervisorScope {
            launch {
                val heatResult = runCatching { homeOverviewRepository.getHomeHeatOverview(lat, lon) }
                if (requestId != activeLoadRequestId) return@launch

                heatOverviewState = heatResult.fold(
                    onSuccess = { data: HomeHeatOverview ->
                        if (data.areaTemperatures.isEmpty() &&
                            data.quickTips.isEmpty() &&
                            data.uvIndex == null &&
                            data.currentLocationWeather == null
                        ) {
                            LoadState.Empty
                        } else {
                            LoadState.Success(data)
                        }
                    },
                    onFailure = { throwable ->
                        LoadState.Error(throwable.message ?: "Couldn't load home heat overview.")
                    },
                )
            }

            launch {
                val placesResult = runCatching { coolPlaceRepository.getCoolPlaces(lat = lat, lon = lon) }
                if (requestId != activeLoadRequestId) return@launch

                coolPlacesState = placesResult.fold(
                    onSuccess = { data: List<CoolPlaceCardData> ->
                        if (data.isEmpty()) LoadState.Empty else LoadState.Success(data)
                    },
                    onFailure = { throwable ->
                        LoadState.Error(throwable.message ?: "Couldn't load cool places.")
                    },
                )
            }
        }
    }

    private fun cacheCoordinates(coordinates: HomeCoordinates) {
        currentCoordinates = coordinates
        writeCachedHomeCoordinates(appContext, coordinates)
    }

    private fun readCachedHomeCoordinates(context: Context): HomeCoordinates? {
        val prefs = context.getSharedPreferences(HOME_LOCATION_CACHE_PREFS, Context.MODE_PRIVATE)
        val lat = prefs.getString(HOME_LOCATION_CACHE_LAT, null)?.toDoubleOrNull()
        val lon = prefs.getString(HOME_LOCATION_CACHE_LON, null)?.toDoubleOrNull()
        return if (lat != null && lon != null) {
            HomeCoordinates(latitude = lat, longitude = lon)
        } else {
            null
        }
    }

    private fun writeCachedHomeCoordinates(context: Context, coordinates: HomeCoordinates) {
        context.getSharedPreferences(HOME_LOCATION_CACHE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(HOME_LOCATION_CACHE_LAT, coordinates.latitude.toString())
            .putString(HOME_LOCATION_CACHE_LON, coordinates.longitude.toString())
            .apply()
    }

    private fun HomeCoordinates.isSameLocation(
        other: HomeCoordinates,
        tolerance: Double = 0.0001,
    ): Boolean {
        return kotlin.math.abs(latitude - other.latitude) < tolerance &&
            kotlin.math.abs(longitude - other.longitude) < tolerance
    }

    private companion object {
        const val HOME_LOCATION_CACHE_PREFS = "home_location_cache"
        const val HOME_LOCATION_CACHE_LAT = "home_location_cache_lat"
        const val HOME_LOCATION_CACHE_LON = "home_location_cache_lon"
    }
}

class HomeViewModelFactory(
    private val context: Context,
    private val homeOverviewRepository: HomeOverviewRepository,
    private val coolPlaceRepository: CoolPlaceRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                context = context,
                homeOverviewRepository = homeOverviewRepository,
                coolPlaceRepository = coolPlaceRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
