package com.chaddy50.morningcommute.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.morningcommute.api.TripLeg
import com.chaddy50.morningcommute.api.WeatherAtTime
import com.chaddy50.morningcommute.api.getTripLeg
import com.chaddy50.morningcommute.api.getWeatherAtTimeForLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

//#region Constants
const val latitude = 43.05
const val longitude = -89.46
const val ROUTE_D = "MMTWI:244383"
const val ROUTE_55 = "MMTWI:31664"
const val TOKAY_AT_SOUTH_SEGOE_WESTBOUND = "MMTWI:30205"
const val TOKAY_AT_SOUTH_SEGOE_EASTBOUND = "MMTWI:31128"
const val JUNCTION_PARK_AND_RIDE_MORNING = "MMTWI:32273"
const val JUNCTION_PARK_AND_RIDE_EVENING = "MMTWI:32269"
const val NORTHERN_LIGHTS_EPIC_STAFF_C = "MMTWI:23278"
//#endregion

class MorningCommuteViewModel : ViewModel() {
    private lateinit var morningCommuteTime: ZonedDateTime
    private lateinit var eveningCommuteTime: ZonedDateTime
    private var lastFetchOfBusData: LocalDateTime? = null

    //#region Properties
    private val _morningCommuteWeather = MutableStateFlow<WeatherAtTime?>(null)
    val morningCommuteWeather = _morningCommuteWeather

    private val _eveningCommuteWeather = MutableStateFlow<WeatherAtTime?>(null)
    val eveningCommuteWeather = _eveningCommuteWeather

    private val _morningBusFirstLeg = MutableStateFlow<TripLeg?>(null)
    val morningBusFirstLeg = _morningBusFirstLeg

    private val _morningBusSecondLeg = MutableStateFlow<TripLeg?>(null)
    val morningBusSecondLeg = _morningBusSecondLeg

    private val _eveningBusFirstLeg = MutableStateFlow<TripLeg?>(null)
    val eveningBusFirstLeg = _eveningBusFirstLeg

    private val _eveningBusSecondLeg = MutableStateFlow<TripLeg?>(null)
    val eveningBusSecondLeg = _eveningBusSecondLeg

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing
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

            morningCommuteTime = targetDate.atTime(7, 30).atZone(ZoneId.systemDefault())
            eveningCommuteTime = targetDate.atTime(16, 30).atZone(ZoneId.systemDefault())

            refreshData()
        }
    }

    suspend fun refreshData() {
        _isRefreshing.value = true
        try {
            val weatherFetchJob = viewModelScope.launch { fetchWeather() }
            val busTimingsFetchJob = viewModelScope.launch { fetchBusTimings() }
            joinAll(weatherFetchJob, busTimingsFetchJob)
        } finally {
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchWeather() {
        _morningCommuteWeather.value = getWeatherAtTimeForLocation(
            latitude,
            longitude,
            morningCommuteTime.truncatedTo(ChronoUnit.HOURS),
        )

        _eveningCommuteWeather.value = getWeatherAtTimeForLocation(
            latitude,
            longitude,
            eveningCommuteTime.truncatedTo(ChronoUnit.HOURS)
        )
    }

    private suspend fun fetchBusTimings() {
        if (lastFetchOfBusData != null && LocalDateTime.now().isBefore(lastFetchOfBusData?.plusMinutes(1))) {
            return
        }

        val isEveningCommuteTimeWindow = LocalTime.now().isAfter(LocalTime.of(9,0)) && LocalTime.now().isBefore(LocalTime.of(18,0))

        if (isEveningCommuteTimeWindow) {
            _eveningBusFirstLeg.value = getTripLeg(
                ROUTE_55,
                NORTHERN_LIGHTS_EPIC_STAFF_C,
                JUNCTION_PARK_AND_RIDE_EVENING,
                eveningCommuteTime,
                "Northbound"
            )

            _eveningBusSecondLeg.value = getTripLeg(
                ROUTE_D,
                JUNCTION_PARK_AND_RIDE_EVENING,
                TOKAY_AT_SOUTH_SEGOE_EASTBOUND,
                eveningCommuteTime.plusMinutes(20),
                "Eastbound"
            )
        }
        else {
            _morningBusFirstLeg.value = getTripLeg(
                ROUTE_D,
                TOKAY_AT_SOUTH_SEGOE_WESTBOUND,
                JUNCTION_PARK_AND_RIDE_MORNING,
                morningCommuteTime,
                "Westbound"
            )

            _morningBusSecondLeg.value = getTripLeg(
                ROUTE_55,
                JUNCTION_PARK_AND_RIDE_MORNING,
                NORTHERN_LIGHTS_EPIC_STAFF_C,
                morningCommuteTime.plusMinutes(25),
                "Southbound"
            )
        }
        lastFetchOfBusData = LocalDateTime.now()
    }
}