package com.chaddy50.morningcommute.view.homeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.morningcommute.view.Screen
import com.chaddy50.morningcommute.viewModel.MorningCommuteViewModel

object HomeScreen : Screen {
    override val route = "home"

    @Composable
    override fun Content(
        viewModel: MorningCommuteViewModel
    ) {
        val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
        val morningBusFirstLeg by viewModel.morningBusFirstLeg.collectAsStateWithLifecycle()
        val morningBusSecondLeg by viewModel.morningBusSecondLeg.collectAsStateWithLifecycle()
        val eveningBusFirstLeg by viewModel.eveningBusFirstLeg.collectAsStateWithLifecycle()
        val eveningBusSecondLeg by viewModel.eveningBusSecondLeg.collectAsStateWithLifecycle()
        val morningCommuteWeather by viewModel.morningCommuteWeather.collectAsStateWithLifecycle()
        val eveningCommuteWeather by viewModel.eveningCommuteWeather.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            HomeView(
                isRefreshing,
                morningBusFirstLeg,
                morningBusSecondLeg,
                eveningBusFirstLeg,
                eveningBusSecondLeg,
                morningCommuteWeather,
                eveningCommuteWeather,
                viewModel::refreshData,
            )
        }
    }
}