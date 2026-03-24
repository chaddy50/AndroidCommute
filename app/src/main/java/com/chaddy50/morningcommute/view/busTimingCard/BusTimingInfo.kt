package com.chaddy50.morningcommute.view.busTimingCard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chaddy50.morningcommute.model.CommuteStatus

@Composable
fun BusTimingInfo(status: CommuteStatus, title: String) {
    Text(title, fontWeight = FontWeight.Bold)

    when (status) {
        is CommuteStatus.RideToEnd -> {
            StatusChip("Stay On Bus", Color(0xFF43A047))
            TransferDetails(
                bus1DepartureTime = status.bus1DepartureTime,
                arrivalAtTransferStop = status.bus1ArrivalAtStopB,
                transferStopLabel = "Junction at Park & Ride",
                bus2DepartureTime = status.bus2DepartureFromStopB,
            )
        }

        is CommuteStatus.ExitEarly -> {
            StatusChip("Get Off Early", Color(0xFFFB8C00))
            TransferDetails(
                bus1DepartureTime = status.bus1DepartureTime,
                arrivalAtTransferStop = status.bus1ArrivalAtStopA,
                transferStopLabel = "Watts at South High Point",
                bus2DepartureTime = status.bus2DepartureFromStopA,
            )
        }

        is CommuteStatus.Missed -> {
            StatusChip("Missed", MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Catch the next bus or find another way to get to work.")
        }

        is CommuteStatus.Error -> {
            StatusChip("Error", MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Could not fetch bus timings. Try again in a minute.")
        }
    }
}
