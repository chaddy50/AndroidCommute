package com.chaddy50.morningcommute.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.morningcommute.api.JUNCTION_PARK_AND_RIDE
import com.chaddy50.morningcommute.api.ROUTE_55
import com.chaddy50.morningcommute.api.ROUTE_D
import com.chaddy50.morningcommute.api.ScheduleItem
import com.chaddy50.morningcommute.api.StopDeparture
import com.chaddy50.morningcommute.api.TOKAY_AT_SOUTH_SEGOE_WESTBOUND
import com.chaddy50.morningcommute.api.TransitAPI
import com.chaddy50.morningcommute.api.WeatherAtTime
import com.chaddy50.morningcommute.api.getWeatherAtTimeForLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

const val latitude = 43.05
const val longitude = -89.46

class MorningCommuteViewModel : ViewModel() {

    //#region Properties
    private lateinit var _morningCommuteTime: ZonedDateTime
    private lateinit var _eveningCommuteTime: ZonedDateTime

    private val _morningCommuteWeather = MutableStateFlow<WeatherAtTime?>(null)
    val morningCommuteWeather = _morningCommuteWeather

    private val _eveningCommuteWeather = MutableStateFlow<WeatherAtTime?>(null)
    val eveningCommuteWeather = _eveningCommuteWeather

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
            val now = LocalDateTime.now()
            val cutoffTime = LocalTime.of(17, 30)

            var targetDate = LocalDate.now()
            if (now.toLocalTime().isAfter(cutoffTime)) {
                // If we're done commuting for today, show tomorrow instead
                targetDate = targetDate.plusDays(1)
            }

            _morningCommuteTime = targetDate.atTime(7, 25).atZone(ZoneId.systemDefault())
            _eveningCommuteTime = targetDate.atTime(16, 30).atZone(ZoneId.systemDefault())

            refreshWeather()
            refreshBusTimings()
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _morningCommuteWeather.value = getWeatherAtTimeForLocation(
                latitude,
                longitude,
                _morningCommuteTime.truncatedTo(ChronoUnit.HOURS),
            )
            _eveningCommuteWeather.value = getWeatherAtTimeForLocation(
                latitude,
                longitude,
                _eveningCommuteTime.truncatedTo(ChronoUnit.HOURS)
            )
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
                _morningCommuteTime.toEpochSecond()
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
                _morningCommuteTime.plusMinutes(25).toEpochSecond()
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
}