package com.chaddy50.morningcommute.view.homeScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.chaddy50.morningcommute.view.Screen
import com.chaddy50.morningcommute.viewModel.MorningCommuteViewModel

object HomeScreen : Screen {
    override val route = "home"

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun Content(
        viewModel: MorningCommuteViewModel
    ) {
        val morningCommuteForecast = viewModel.morningCommuteForecast.collectAsState()
        val numberOfNearbyRoutes = viewModel.nextDWestboundDeparture.collectAsState()

        HomeView(
            morningCommuteForecast.value,
            numberOfNearbyRoutes.value
        )
    }
}