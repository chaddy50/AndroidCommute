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
import com.chaddy50.morningcommute.api.TripLeg
import com.chaddy50.morningcommute.api.WeatherAtTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun HomeView(
    morningBusFirstLeg: TripLeg?,
    morningBusSecondLeg: TripLeg?,
    morningCommuteWeather: WeatherAtTime?,
    eveningCommuteWeather: WeatherAtTime?,
    refreshBusTimings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Column {
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
            if (morningBusFirstLeg != null && morningBusSecondLeg != null) {

                val isOnTimeForTransfer =
                    morningBusFirstLeg.arrivalTime.isBefore(morningBusSecondLeg.departureTime)
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
                        morningBusFirstLeg.arrivalTime,
                        morningBusSecondLeg.departureTime
                    )

                    Text(
                        text = "$bufferInMinutes minutes buffer",
                        fontSize = 24.sp,
                        modifier = modifier
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Catch bus at ${formatter.format(morningBusFirstLeg.departureTime)}",
                        modifier = modifier
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Transfer by ${formatter.format(morningBusSecondLeg.departureTime)}",
                        modifier = modifier
                    )
                } else {
                    Text(
                        "Catch the next bus or find another way to get to work."
                    )
                }

            } else {
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
        Column {
            if (morningCommuteWeather != null) {
                Text("${morningCommuteWeather.temperature}${morningCommuteWeather.temperatureUnit}")
                Text("${morningCommuteWeather.precipitationProbability}% precipitation")
                Text("Wind ${morningCommuteWeather.windSpeed} ${morningCommuteWeather.windSpeedUnit} ${morningCommuteWeather.windDirectionCardinal}")
            }
            if (eveningCommuteWeather != null) {
                Text("${eveningCommuteWeather.temperature}${eveningCommuteWeather.temperatureUnit}")
                Text("${eveningCommuteWeather.precipitationProbability}% precipitation")
                Text("Wind ${eveningCommuteWeather.windSpeed} ${eveningCommuteWeather.windSpeedUnit} ${eveningCommuteWeather.windDirectionCardinal}")
            }
        }
    }
}

//#region Previews
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun OnTimePreview() {
    HomeView(
        null,
        null,
        null,
        null,
        {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun MissedTransferPreview() {
    HomeView(
        null,
        null,
        null,
        null,
        {},
    )
}
//#endregion