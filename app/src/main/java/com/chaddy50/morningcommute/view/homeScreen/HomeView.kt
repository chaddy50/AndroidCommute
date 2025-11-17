package com.chaddy50.morningcommute.view.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun HomeView(
    morningBusDepartureTime: LocalDateTime?,
    morningBusTransferArrivalTime: LocalDateTime?,
    morningBusTransferDepartureTime: LocalDateTime?,
    refreshBusTimings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (morningBusDepartureTime != null
                && morningBusTransferArrivalTime != null
                && morningBusTransferDepartureTime != null
            ) {

            val isOnTimeForTransfer = morningBusTransferArrivalTime.isBefore(morningBusTransferDepartureTime)
            val label = if (isOnTimeForTransfer) "On Time" else "Missed"
            val color = if (isOnTimeForTransfer) Color(0xFF43A047) else MaterialTheme.colorScheme.error

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
                val bufferInMinutes = ChronoUnit.MINUTES.between(morningBusTransferArrivalTime, morningBusTransferDepartureTime)

                Text(
                    text = "$bufferInMinutes minutes buffer",
                    fontSize = 24.sp,
                    modifier = modifier
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Catch bus at ${formatter.format(morningBusDepartureTime)}",
                    modifier = modifier
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Transfer by ${formatter.format(morningBusTransferDepartureTime)}",
                    modifier = modifier
                )
            }
            else {
                Text(
                    "Catch the next bus or find another way to get to work."
                )
            }

        }
        else {
            Text(
                text = "No bus information available",
                modifier = modifier
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        IconButton(onClick = refreshBusTimings) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh"
            )
        }
    }
}

//#region Previews
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun OnTimePreview() {
    HomeView(
        LocalDate.of(2025,11,1).atTime(7,31),
        LocalDate.of(2025,11,1).atTime(7,48),
        LocalDate.of(2025,11,1).atTime(7,52),
        {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun MissedTransferPreview() {
    HomeView(
        LocalDate.of(2025,11,1).atTime(7,38),
        LocalDate.of(2025,11,1).atTime(7,52),
        LocalDate.of(2025, 11,1).atTime(7,52),
        {},
    )
}
//#endregion