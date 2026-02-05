package com.chaddy50.morningcommute.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.morningcommute.api.JUNCTION_PARK_AND_RIDE
import com.chaddy50.morningcommute.api.NORTHERN_LIGHTS_EPIC_STAFF_C
import com.chaddy50.morningcommute.api.ROUTE_55
import com.chaddy50.morningcommute.api.ROUTE_D
import com.chaddy50.morningcommute.api.TOKAY_AT_SOUTH_SEGOE_WESTBOUND
import com.chaddy50.morningcommute.api.TripLeg
import com.chaddy50.morningcommute.api.WeatherAtTime
import com.chaddy50.morningcommute.api.getTripLeg
import com.chaddy50.morningcommute.api.getWeatherAtTimeForLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

const val latitude = 43.05
const val longitude = -89.46

class MorningCommuteViewModel : ViewModel() {
    private lateinit var morningCommuteTime: ZonedDateTime
    private lateinit var eveningCommuteTime: ZonedDateTime

    //#region Properties
    private val _morningCommuteWeather = MutableStateFlow<WeatherAtTime?>(null)
    val morningCommuteWeather = _morningCommuteWeather

    private val _eveningCommuteWeather = MutableStateFlow<WeatherAtTime?>(null)
    val eveningCommuteWeather = _eveningCommuteWeather

    private val _morningBusFirstLeg = MutableStateFlow<TripLeg?>(null)
    val morningBusFirstLeg = _morningBusFirstLeg

    private val _morningBusSecondLeg = MutableStateFlow<TripLeg?>(null)
    val morningBusSecondLeg = _morningBusSecondLeg
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

            refreshWeather()
            refreshBusTimings()
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
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
    }

    fun refreshBusTimings() {
        viewModelScope.launch {
            _morningBusFirstLeg.value = getTripLeg(
                ROUTE_D,
                TOKAY_AT_SOUTH_SEGOE_WESTBOUND,
                JUNCTION_PARK_AND_RIDE,
                morningCommuteTime
            )

            _morningBusSecondLeg.value = getTripLeg(
                ROUTE_55,
                JUNCTION_PARK_AND_RIDE,
                NORTHERN_LIGHTS_EPIC_STAFF_C,
                morningCommuteTime.plusMinutes(25)
            )
        }
    }
}