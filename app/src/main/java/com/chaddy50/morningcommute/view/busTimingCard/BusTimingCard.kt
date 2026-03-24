package com.chaddy50.morningcommute.view.busTimingCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chaddy50.morningcommute.model.CommuteStatus
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun BusTimingCard(
    morningCommuteStatus: CommuteStatus?,
    eveningCommuteStatus: CommuteStatus?,
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (morningCommuteStatus != null) {
            BusTimingInfo(morningCommuteStatus, "Morning Bus")
        } else if (eveningCommuteStatus != null) {
            BusTimingInfo(eveningCommuteStatus, "Evening Bus")
        } else {
            Text("No bus information available")
        }
    }
}

//#region Previews
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NoBusInformationPreview() {
    BusTimingCard(null, null)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RideToEndPreview() {
    BusTimingCard(
        morningCommuteStatus = CommuteStatus.RideToEnd(
            bus1DepartureTime = ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 7, 42), ZoneId.systemDefault()),
            bus1ArrivalAtStopB = ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 5), ZoneId.systemDefault()),
            bus2DepartureFromStopB = ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 12), ZoneId.systemDefault()),
            bufferMinutes = 7,
        ),
        eveningCommuteStatus = null,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ExitEarlyPreview() {
    BusTimingCard(
        morningCommuteStatus = CommuteStatus.ExitEarly(
            bus1DepartureTime = ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 7, 42), ZoneId.systemDefault()),
            bus1ArrivalAtStopA = ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 7, 58), ZoneId.systemDefault()),
            bus2DepartureFromStopA = ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 1), ZoneId.systemDefault()),
            bufferMinutes = 3,
        ),
        eveningCommuteStatus = null,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun MissedPreview() {
    BusTimingCard(
        morningCommuteStatus = CommuteStatus.Missed,
        eveningCommuteStatus = null,
    )
}
//#endregion
