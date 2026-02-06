package com.chaddy50.morningcommute.view.weatherCard

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chaddy50.morningcommute.api.WeatherAtTime

@Composable
fun WeatherCard(
    weatherAtTime: WeatherAtTime?,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        if (weatherAtTime != null) {
            Text("${weatherAtTime.temperature}${weatherAtTime.temperatureUnit}")
            Text("${weatherAtTime.precipitationProbability}% precipitation")
            Text("Wind ${weatherAtTime.windSpeed} ${weatherAtTime.windSpeedUnit} ${weatherAtTime.windDirectionCardinal}")
        } else {
            Text("No weather information available")
        }
    }
}

//#region Previews
@Preview
@Composable
fun NoWeatherInfoPreview() {
    WeatherCard(
        null,
        "Weather"
    )
}

@Preview
@Composable
fun WeatherInfoPreview() {
    WeatherCard(
        WeatherAtTime("08:00", 30, "°F", 30, 20, "km/h", 2, "N"),
        "Weather"
    )
}
//#endregion