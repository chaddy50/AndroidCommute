package com.chaddy50.morningcommute.view.homeScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.morningcommute.view.Screen
import com.chaddy50.morningcommute.viewModel.MorningCommuteViewModel

object HomeScreen : Screen {
    override val route = "home"

    @Composable
    override fun Content(viewModel: MorningCommuteViewModel) {
        val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
        val morningCommuteStatus by viewModel.morningCommuteStatus.collectAsStateWithLifecycle()
        val eveningCommuteStatus by viewModel.eveningCommuteStatus.collectAsStateWithLifecycle()
        val morningCommuteWeather by viewModel.morningCommuteWeather.collectAsStateWithLifecycle()
        val eveningCommuteWeather by viewModel.eveningCommuteWeather.collectAsStateWithLifecycle()

        HomeView(
            isRefreshing = isRefreshing,
            morningCommuteStatus = morningCommuteStatus,
            eveningCommuteStatus = eveningCommuteStatus,
            morningCommuteWeather = morningCommuteWeather,
            eveningCommuteWeather = eveningCommuteWeather,
            refreshData = viewModel::refreshData,
        )
    }
}
