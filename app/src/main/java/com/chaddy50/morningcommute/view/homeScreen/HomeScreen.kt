package com.chaddy50.morningcommute.view.homeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.chaddy50.morningcommute.view.Screen
import com.chaddy50.morningcommute.viewModel.MorningCommuteViewModel

object HomeScreen : Screen {
    override val route = "home"

    @Composable
    override fun Content(
        viewModel: MorningCommuteViewModel
    ) {
        val morningBusDepartureTime = viewModel.morningBusDepartureTime.collectAsState()
        val morningBusTransferArrivalTime = viewModel.morningBusTransferArrivalTime.collectAsState()
        val morningBusTransferDepartureTime = viewModel.morningBusTransferDepartureTime.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            HomeView(
                morningBusDepartureTime.value,
                morningBusTransferArrivalTime.value,
                morningBusTransferDepartureTime.value,
                viewModel::refreshBusTimings,
            )
        }
    }
}