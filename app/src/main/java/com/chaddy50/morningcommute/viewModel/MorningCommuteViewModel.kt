package com.chaddy50.morningcommute.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.morningcommute.api.OpenMeteoAPI
import com.chaddy50.morningcommute.api.ROUTE_D
import com.chaddy50.morningcommute.api.StopDeparture
import com.chaddy50.morningcommute.api.TOKAY_AT_SOUTH_SEGOE_WESTBOUND
import com.chaddy50.morningcommute.api.TransitAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class MorningCommuteViewModel : ViewModel() {
    //#region Properties
    private val _temperaturesForWeek = MutableStateFlow<List<Pair<String, Double>>>(emptyList())

    private val _morningCommuteForecast = MutableStateFlow<List<Forecast>>(listOf())
    val morningCommuteForecast = _morningCommuteForecast

    private val _eveningCommuteForecast = MutableStateFlow<List<Forecast>>(listOf())
    val eveningCommuteForecast = _eveningCommuteForecast

    private val _nextDWestboundDeparture = MutableStateFlow("")
    val nextDWestboundDeparture = _nextDWestboundDeparture
    //#endregion

    init {
        viewModelScope.launch {
            val latitude = 43.05
            val longitude = -89.46

            val forecastAt0600 = getForecastAtTime(
                latitude,
                longitude,
                "2025-11-05T06:00",
            )
            _morningCommuteForecast.value = _morningCommuteForecast.value + forecastAt0600

            _nextDWestboundDeparture.value = getNextDWestboundDeparture()
        }
    }

    private suspend fun getForecastAtTime(
        latitude: Double,
        longitude: Double,
        time: String,
    ): Forecast {
        val response = OpenMeteoAPI.getForecast(
            latitude,
            longitude,
            "temperature_2m,relative_humidity_2m",
        )
        val index = response.hourly.time.indexOf(time)

        return Forecast(response.hourly.temperature_2m[index])
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getNextDWestboundDeparture(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault()) // Or use ZoneId.of("UTC") for UTC time
        val response = TransitAPI.getDeparturesForRoute(TOKAY_AT_SOUTH_SEGOE_WESTBOUND)

        val nextDWestboundDeparture = findNextWestboundDeparture(response.stopDepartures)

        if (nextDWestboundDeparture != null) {
            return formatter.format(
                Instant.ofEpochSecond(
                    nextDWestboundDeparture.itineraries[0].scheduleItems[0].departureTime
                )
            )
        }
        return ""
    }

    private fun findNextWestboundDeparture(stopDepartures: List<StopDeparture>): StopDeparture? {
        try {
            val nextDeparture = stopDepartures.first { it.globalRouteId == ROUTE_D }
            return nextDeparture
        } catch(_: NoSuchElementException) {
            return null
        }

    }
}

data class Forecast(
    val temperature: Double
)