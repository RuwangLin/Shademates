package com.example.a5120_shademate.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a5120_shademate.data.api.AppApiClient
import com.example.a5120_shademate.data.api.HeatMapApiService
import com.example.a5120_shademate.data.api.HeatMapRequest
import com.example.a5120_shademate.data.api.HeatMapResponseBody
import com.example.a5120_shademate.data.api.WeatherManager
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class MapViewModel(
    val suburbVm: SuburbMapViewModel,
    val lgaVm: LgaMapViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private val heatMapApi = AppApiClient.retrofit.create(HeatMapApiService::class.java)
    private var heatMapJob: Job? = null

    fun onZoomChanged(zoom: Float) {
        val isLga = zoom < 10f

        _state.update {
            it.copy(
                currentZoom = zoom,
                isLgaMode = isLga
            )
        }

        if (zoom < 13f && _state.value.heatMapUrl != null) {
            _state.update { it.copy(heatMapUrl = null, heatMapBbox = null) }
        }
    }

    fun fetchHeatMapIfNeeded(bbox: List<Double>) {
        if (_state.value.currentZoom < 13f) return

        // Avoid redundant requests if bbox is very similar or we already have it
        // For simplicity, we'll use a debounce and check if we already have a URL

        heatMapJob?.cancel()
        heatMapJob = viewModelScope.launch {
            delay(1000) // Debounce

            try {
                // 使用系统默认时区获取当前时间
                val calendar = Calendar.getInstance()
                val month = calendar.get(Calendar.MONTH) + 1
                val hour = calendar.get(Calendar.HOUR_OF_DAY)

                // 打印详细日志以排查时区问题
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                Log.d("MapViewModel", "HeatMap Request -> Local Time: ${sdf.format(calendar.time)}, Hour: $hour, Zone: ${calendar.timeZone.id}")

                val response = heatMapApi.getHeatMap(
                    HeatMapRequest(bbox = bbox, currentMonth = month, currentHour = hour)
                )

                if (response.statusCode == 200) {
                    val body = Gson().fromJson(response.body, HeatMapResponseBody::class.java)
                    _state.update {
                        it.copy(
                            heatMapUrl = body.s3Url,
                            heatMapBbox = bbox,
                            heatMapSuburb = body.suburb
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Failed to fetch heatmap", e)
            }
        }
    }

    fun toggleHeatMapOverlay() {
        _state.update { it.copy(isHeatMapOverlayActive = !it.isHeatMapOverlayActive) }
    }

    fun getActiveViewModel(): BaseMapViewModel = if (_state.value.isLgaMode) lgaVm else suburbVm
}
