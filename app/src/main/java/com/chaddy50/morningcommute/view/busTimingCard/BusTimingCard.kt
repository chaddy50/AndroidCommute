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
import com.chaddy50.morningcommute.api.TripLeg
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun BusTimingCard(
    morningBusFirstLeg: TripLeg?,
    morningBusSecondLeg: TripLeg?,
    eveningBusFirstLeg: TripLeg?,
    eveningBusSecondLeg: TripLeg?,
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
        if (morningBusFirstLeg != null && morningBusSecondLeg != null) {
            BusTimingInfo(morningBusFirstLeg, morningBusSecondLeg, "Morning Bus")
        } else if (eveningBusFirstLeg != null && eveningBusSecondLeg != null) {
            BusTimingInfo(eveningBusFirstLeg, eveningBusSecondLeg, "Evening Bus")
        } else {
            Text("No bus information available")
        }
    }
}

//#region Previews
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NoBusInformationPreview() {
    BusTimingCard(
        null,
        null,
        null,
        null,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun OnTimePreview() {
    BusTimingCard(
        TripLeg(
            ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 0), ZoneId.systemDefault()),
            ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 25), ZoneId.systemDefault())
        ),
        TripLeg(
            ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 30), ZoneId.systemDefault()),
            ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 50), ZoneId.systemDefault())
        ),
        null,
        null,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun MissedPreview() {
    BusTimingCard(
        TripLeg(
            ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 0), ZoneId.systemDefault()),
            ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 25), ZoneId.systemDefault())
        ),
        TripLeg(
            ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 20), ZoneId.systemDefault()),
            ZonedDateTime.of(LocalDateTime.of(2026, 1, 31, 8, 50), ZoneId.systemDefault())
        ),
        null,
        null,
    )
}
//#endregion