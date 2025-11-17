package com.chaddy50.morningcommute.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.morningcommute.api.JUNCTION_PARK_AND_RIDE
import com.chaddy50.morningcommute.api.OpenMeteoAPI
import com.chaddy50.morningcommute.api.ROUTE_55
import com.chaddy50.morningcommute.api.ROUTE_D
import com.chaddy50.morningcommute.api.ScheduleItem
import com.chaddy50.morningcommute.api.StopDeparture
import com.chaddy50.morningcommute.api.TOKAY_AT_SOUTH_SEGOE_WESTBOUND
import com.chaddy50.morningcommute.api.TransitAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MorningCommuteViewModel : ViewModel() {
    //#region Properties
    private val _temperaturesForWeek = MutableStateFlow<List<Pair<String, Double>>>(emptyList())

    private val _morningCommuteForecast = MutableStateFlow<List<Forecast>>(listOf())
    val morningCommuteForecast = _morningCommuteForecast

    private val _eveningCommuteForecast = MutableStateFlow<List<Forecast>>(listOf())
    val eveningCommuteForecast = _eveningCommuteForecast

    private val _morningBusDeparture = MutableStateFlow<ScheduleItem?>(null)
    private val _morningBusTransferDeparture = MutableStateFlow<ScheduleItem?>(null)

    private val _morningBusDepartureTime = MutableStateFlow<LocalDateTime?>(null)
    val morningBusDepartureTime = _morningBusDepartureTime

    private val _morningBusTransferArrivalTime = MutableStateFlow<LocalDateTime?>(null)
    val morningBusTransferArrivalTime = _morningBusTransferArrivalTime

    private val _morningBusTransferDepartureTime = MutableStateFlow<LocalDateTime?>(null)
    val morningBusTransferDepartureTime = _morningBusTransferDepartureTime
    //#endregion

    init {
        viewModelScope.launch {
            val latitude = 43.05
            val longitude = -89.46

            val tomorrowAt0600 = LocalDate.now().plusDays(1).atTime(6, 0).atZone(ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            val forecastAt0600 = getForecastAtTime(
                latitude,
                longitude,
                formatter.format(tomorrowAt0600),
            )
            //_morningCommuteForecast.value = _morningCommuteForecast.value + forecastAt0600
            refreshBusTimings()
        }
    }

    fun refreshBusTimings() {
        viewModelScope.launch {
            _morningBusDeparture.value = getMorningBusDeparture() ?: _morningBusDeparture.value
            _morningBusTransferDeparture.value = getMorningTransferBusDeparture() ?: _morningBusTransferDeparture.value

            _morningBusDepartureTime.value = getMorningBusDepartureTime()
            _morningBusTransferArrivalTime.value = getMorningBusTransferArrivalTime()
            _morningBusTransferDepartureTime.value = getMorningBusTransferDepartureTime()
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

    private fun getMorningBusDepartureTime(): LocalDateTime? {
        val morningBusDeparture = _morningBusDeparture.value ?: return null

        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(morningBusDeparture.departureTime),
            ZoneId.systemDefault()
        )
    }

    private suspend fun getMorningBusTransferArrivalTime(): LocalDateTime? {
        val morningBusDeparture = _morningBusDeparture.value ?: return null

        return try {
            val response = TransitAPI.getTripDetails(morningBusDeparture.tripSearchKey)
            if (!response.isSuccessful) return null

            val tripDetails = response.body() ?: return null
            val transfer = tripDetails.scheduleItems.first { it.stop.globalStopId == JUNCTION_PARK_AND_RIDE }

            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(transfer.departureTime),
                ZoneId.systemDefault()
            )

        } catch (_: Exception) {
            return null
        }
    }

    private fun getMorningBusTransferDepartureTime(): LocalDateTime? {
        val morningBusDeparture = _morningBusTransferDeparture.value ?: return null

        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(morningBusDeparture.departureTime),
            ZoneId.systemDefault()
        )
    }

    private suspend fun getMorningBusDeparture() : ScheduleItem? {
        return try {
            val response = TransitAPI.getDeparturesForRoute(
                TOKAY_AT_SOUTH_SEGOE_WESTBOUND,
                true,
                getDesiredDepartureTime()
            )
            if (!response.isSuccessful) return null

            val departuresForRoute = response.body() ?: return null
            findNextDepartureForRoute(departuresForRoute.stopDepartures, ROUTE_D)
        } catch (_: Exception) {
            return null
        }
    }

    private suspend fun getMorningTransferBusDeparture() : ScheduleItem? {
        return try {
            val response = TransitAPI.getDeparturesForRoute(
                JUNCTION_PARK_AND_RIDE,
                true,
                getDesiredTransferTime()
            )
            if (!response.isSuccessful) return null

            val departuresForRoute = response.body() ?: return null
            findNextDepartureForRoute(departuresForRoute.stopDepartures, ROUTE_55)
        } catch (_: Exception) {
            return null
        }
    }

    private fun findNextDepartureForRoute(
        stopDepartures: List<StopDeparture>,
        routeId: String
    ): ScheduleItem? {
        try {
            val nextDeparture = stopDepartures.first { it.globalRouteId == routeId }
            return nextDeparture.itineraries[0].scheduleItems[0]
        } catch(_: NoSuchElementException) {
            return null
        }
    }

    private fun getDesiredDepartureTime(): Long {
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault())
        if (now.hour > 9) {
            return now.plusDays(1).withHour(7).withMinute(25).toEpochSecond()
        }
        return now.withHour(7).withMinute(25).toEpochSecond()
    }

    private fun getDesiredTransferTime(): Long {
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault())
        if (now.hour > 9) {
            return now.plusDays(1).withHour(7).withMinute(50).toEpochSecond()
        }
        return now.withHour(7).withMinute(50).toEpochSecond()
    }
}

data class Forecast(
    val temperature: Double
)