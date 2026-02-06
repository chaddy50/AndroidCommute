package com.chaddy50.morningcommute.view.homeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.chaddy50.morningcommute.api.TripLeg
import com.chaddy50.morningcommute.api.WeatherAtTime
import com.chaddy50.morningcommute.view.busTimingCard.BusTimingCard
import com.chaddy50.morningcommute.view.weatherCard.WeatherCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    isRefreshing: Boolean,
    morningBusFirstLeg: TripLeg?,
    morningBusSecondLeg: TripLeg?,
    eveningBusFirstLeg: TripLeg?,
    eveningBusSecondLeg: TripLeg?,
    morningCommuteWeather: WeatherAtTime?,
    eveningCommuteWeather: WeatherAtTime?,
    refreshData: suspend () -> Unit,
) {
    val state = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    PullToRefreshBox(
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                refreshData()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherCard(
                    morningCommuteWeather,
                    "Morning",
                    modifier = Modifier.weight(1f)
                )
                WeatherCard(
                    eveningCommuteWeather,
                    "Evening",
                    modifier = Modifier.weight(1f)
                )
            }

            BusTimingCard(
                morningBusFirstLeg,
                morningBusSecondLeg,
                eveningBusFirstLeg,
                eveningBusSecondLeg
            )
        }
    }
}