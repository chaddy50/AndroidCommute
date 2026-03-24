package com.chaddy50.morningcommute.view.busTimingCard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TransferDetails(
    bus1DepartureTime: ZonedDateTime,
    arrivalAtTransferStop: ZonedDateTime,
    transferStopLabel: String,
    bus2DepartureTime: ZonedDateTime,
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        Text("Depart", modifier = Modifier.weight(1f))
        Text(formatter.format(bus1DepartureTime))
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        Text("Arrive at $transferStopLabel", modifier = Modifier.weight(1f))
        Text(formatter.format(arrivalAtTransferStop))
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        Text("Transfer bus departs", modifier = Modifier.weight(1f))
        Text(formatter.format(bus2DepartureTime))
    }
}
