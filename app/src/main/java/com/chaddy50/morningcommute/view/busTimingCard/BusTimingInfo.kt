package com.chaddy50.morningcommute.view.busTimingCard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaddy50.morningcommute.api.TripLeg
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun BusTimingInfo(
    firstLeg: TripLeg?,
    secondLeg: TripLeg?,
    title: String,
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Text(title, fontWeight = FontWeight.Bold)

    if (firstLeg != null && secondLeg != null) {

        val isOnTimeForTransfer = firstLeg.arrivalTime.isBefore(secondLeg.departureTime)
        val label = if (isOnTimeForTransfer) "On Time" else "Missed"
        val color =
            if (isOnTimeForTransfer) Color(0xFF43A047) else MaterialTheme.colorScheme.error

        SuggestionChip(
            onClick = {},
            label = {
                Text(label)
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = color,
                labelColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        if (isOnTimeForTransfer) {
            val bufferInMinutes = ChronoUnit.MINUTES.between(
                firstLeg.arrivalTime,
                secondLeg.departureTime
            )

            Text(
                text = "$bufferInMinutes minutes buffer",
                fontSize = 24.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Catch bus at ${formatter.format(firstLeg.departureTime)}",
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Transfer by ${formatter.format(secondLeg.departureTime)}",
            )
        } else {
            Text(
                "Catch the next bus or find another way to get to work."
            )
        }
    }
}