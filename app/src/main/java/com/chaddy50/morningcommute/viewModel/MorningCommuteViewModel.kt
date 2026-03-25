package com.chaddy50.morningcommute.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.morningcommute.api.TransferStop
import com.chaddy50.morningcommute.api.WeatherAtTime
import com.chaddy50.morningcommute.api.getCommuteStatus
import com.chaddy50.morningcommute.api.getWeatherAtTimeForLocation
import com.chaddy50.morningcommute.model.CommuteStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

//#region Constants
const val LATITUDE = 43.05
const val LONGITUDE = -89.46
const val ROUTE_D = "MMTWI:244383"
const val ROUTE_55 = "MMTWI:31664"

// Morning boarding stop
const val TOKAY_AT_SOUTH_SEGOE_WESTBOUND = "MMTWI:30205"

// Morning early transfer (Watts at South High Point)
const val WATTS_AT_SOUTH_HIGH_POINT_MORNING_EXIT = "MMTWI:31018"    // Bus 1 (Route D) stops here — westbound
const val WATTS_AT_SOUTH_HIGH_POINT_MORNING_BOARDING = "MMTWI:23270" // Bus 2 (Route 55) stop — southbound boarding

// Morning preferred transfer (Junction Park & Ride)
const val JUNCTION_PARK_AND_RIDE_MORNING_EXIT = "MMTWI:32273"       // Bus 1 (Route D) terminates here
const val JUNCTION_PARK_AND_RIDE_MORNING_BOARDING = "MMTWI:32273"   // Bus 2 (Route 55) — southbound boarding

// Evening boarding stop
const val NORTHERN_LIGHTS_EPIC_STAFF_C = "MMTWI:23278"

// Evening early transfer (Watts at South High Point)
const val WATTS_AT_SOUTH_HIGH_POINT_EVENING_EXIT = "MMTWI:29656"    // Bus 1 (Route 55) stops here — northbound
const val WATTS_AT_SOUTH_HIGH_POINT_EVENING_BOARDING = "MMTWI:31113" // Bus 2 (Route D) stop — eastbound boarding

// Evening preferred transfer (Junction Park & Ride)
const val JUNCTION_PARK_AND_RIDE_EVENING_EXIT = "MMTWI:32269"       // Bus 1 (Route 55) terminates here
const val JUNCTION_PARK_AND_RIDE_EVENING_BOARDING = "MMTWI:32269"   // Bus 2 (Route D) — eastbound boarding
//#endregion

class MorningCommuteViewModel : ViewModel() {
    val commuteDate: LocalDate
    private val morningCommuteTime: ZonedDateTime
    private val eveningCommuteTime: ZonedDateTime
    private var lastFetchOfBusData: LocalDateTime? = null

    //#region Properties
    private val _morningCommuteWeather = MutableStateFlow<WeatherAtTime?>(null)
    val morningCommuteWeather: StateFlow<WeatherAtTime?> = _morningCommuteWeather.asStateFlow()

    private val _eveningCommuteWeather = MutableStateFlow<WeatherAtTime?>(null)
    val eveningCommuteWeather: StateFlow<WeatherAtTime?> = _eveningCommuteWeather.asStateFlow()

    private val _morningCommuteStatus = MutableStateFlow<CommuteStatus?>(null)
    val morningCommuteStatus: StateFlow<CommuteStatus?> = _morningCommuteStatus.asStateFlow()

    private val _eveningCommuteStatus = MutableStateFlow<CommuteStatus?>(null)
    val eveningCommuteStatus: StateFlow<CommuteStatus?> = _eveningCommuteStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    //#endregion

    init {
        val targetDate = run {
            val now = LocalDateTime.now()
            val cutoffTime = LocalTime.of(17, 30)
            if (now.toLocalTime().isAfter(cutoffTime)) LocalDate.now().plusDays(1) else LocalDate.now()
        }
        commuteDate = targetDate
        morningCommuteTime = targetDate.atTime(7, 30).atZone(ZoneId.systemDefault())
        eveningCommuteTime = targetDate.atTime(16, 45).atZone(ZoneId.systemDefault())

        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val weatherJob = launch { fetchWeather() }
                val busJob = launch { fetchBusTimings() }
                joinAll(weatherJob, busJob)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun fetchWeather() {
        _morningCommuteWeather.value = getWeatherAtTimeForLocation(
            LATITUDE,
            LONGITUDE,
            morningCommuteTime.truncatedTo(ChronoUnit.HOURS),
        )

        _eveningCommuteWeather.value = getWeatherAtTimeForLocation(
            LATITUDE,
            LONGITUDE,
            eveningCommuteTime.truncatedTo(ChronoUnit.HOURS)
        )
    }

    private suspend fun fetchBusTimings() {
        val lastFetch = lastFetchOfBusData
        if (lastFetch != null && LocalDateTime.now().isBefore(lastFetch.plusMinutes(1))) {
            return
        }

        val isEveningCommuteTimeWindow = LocalTime.now().isAfter(LocalTime.of(9, 0))
            && LocalTime.now().isBefore(LocalTime.of(18, 0))

        if (isEveningCommuteTimeWindow) {
            _eveningCommuteStatus.value = getCommuteStatus(
                bus1RouteId = ROUTE_55,
                bus1DirectionHeadsign = "Northbound",
                bus2RouteId = ROUTE_D,
                bus2DirectionHeadsign = "Eastbound",
                boardingStopId = NORTHERN_LIGHTS_EPIC_STAFF_C,
                earlyTransfer = TransferStop(
                    exitStopId = WATTS_AT_SOUTH_HIGH_POINT_EVENING_EXIT,
                    boardingStopId = WATTS_AT_SOUTH_HIGH_POINT_EVENING_BOARDING,
                ),
                preferredTransfer = TransferStop(
                    exitStopId = JUNCTION_PARK_AND_RIDE_EVENING_EXIT,
                    boardingStopId = JUNCTION_PARK_AND_RIDE_EVENING_BOARDING,
                ),
                desiredDepartureTime = eveningCommuteTime,
            )
        } else {
            _morningCommuteStatus.value = getCommuteStatus(
                bus1RouteId = ROUTE_D,
                bus1DirectionHeadsign = "Westbound",
                bus2RouteId = ROUTE_55,
                bus2DirectionHeadsign = "Southbound",
                boardingStopId = TOKAY_AT_SOUTH_SEGOE_WESTBOUND,
                earlyTransfer = TransferStop(
                    exitStopId = WATTS_AT_SOUTH_HIGH_POINT_MORNING_EXIT,
                    boardingStopId = WATTS_AT_SOUTH_HIGH_POINT_MORNING_BOARDING,
                ),
                preferredTransfer = TransferStop(
                    exitStopId = JUNCTION_PARK_AND_RIDE_MORNING_EXIT,
                    boardingStopId = JUNCTION_PARK_AND_RIDE_MORNING_BOARDING,
                ),
                desiredDepartureTime = morningCommuteTime,
            )
        }
        lastFetchOfBusData = LocalDateTime.now()
    }
}
