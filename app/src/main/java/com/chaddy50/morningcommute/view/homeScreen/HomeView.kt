package com.chaddy50.morningcommute.view.homeScreen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chaddy50.morningcommute.viewModel.Forecast

@Composable
fun HomeView(
    morningCommuteForecast: List<Forecast>,
    nextDWestboundDeparture: String,
    modifier: Modifier = Modifier,
) {
    var temperatureAt0600 = 0.0
    if (morningCommuteForecast.size > 0) {
        temperatureAt0600 = morningCommuteForecast[0].temperature
    }

    Text(
        text = "Temperature: $temperatureAt0600",
        modifier = modifier
    )

    Text(
        text = "Next D Westbound Departure: $nextDWestboundDeparture",
        modifier = modifier
    )
}