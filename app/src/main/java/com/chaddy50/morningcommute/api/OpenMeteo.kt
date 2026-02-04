package com.chaddy50.morningcommute.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

//#region Public functions
suspend fun getWeatherAtTimeForLocation(
    latitude: Double,
    longitude: Double,
    time: ZonedDateTime,
): WeatherAtTime? {
    return getForecastForLocation(latitude, longitude).getWeatherAt(time)
}
//#endregion

//#region API]
val OpenMeteoAPI: OpenMeteoService = Retrofit.Builder()
    .baseUrl("https://api.open-meteo.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create()

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String,
        @Query("timezone") timezone: String = "auto",
        @Query("temperature_unit") temperatureUnit: String = "fahrenheit",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("precipitation_unit") precipitationUnit: String = "mm"
    ): ForecastResponse
}
//#endregion

//#region Data Classes
data class ForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val hourly_units: HourlyUnits,
    val hourly: Hourly,
)

data class HourlyUnits(
    val time: String,
    val temperature_2m: String,
    val precipitation_probability: String,
    val wind_speed_10m: String,
    val wind_direction_10m: String
)

data class Hourly(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val precipitation_probability: List<Int>,
    val wind_speed_10m: List<Double>,
    val wind_direction_10m: List<Int>
)

data class WeatherAtTime(
    val time: String,
    val temperature: Int,
    val temperatureUnit: String,
    val precipitationProbability: Int,
    val windSpeed: Int,
    val windSpeedUnit: String,
    val windDirection: Int,
    val windDirectionCardinal: String
)
//#endregion

//#region Private functions
private fun ForecastResponse.getWeatherAt(time: ZonedDateTime): WeatherAtTime? {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val timeAsString = formatter.format(time)
    val index = hourly.time.indexOfFirst { it.startsWith(timeAsString) }
    if (index == -1) return null

    return WeatherAtTime(
        time = hourly.time[index],
        temperature = hourly.temperature_2m[index].roundToInt(),
        temperatureUnit = hourly_units.temperature_2m,
        precipitationProbability = hourly.precipitation_probability[index],
        windSpeed = hourly.wind_speed_10m[index].roundToInt(),
        windSpeedUnit = hourly_units.wind_speed_10m,
        windDirection = hourly.wind_direction_10m[index],
        windDirectionCardinal = degreesToCardinal(hourly.wind_direction_10m[index])
    )
}

private val HOURLY_PARAMETERS = listOf(
    "temperature_2m",
    "precipitation_probability",
    "wind_speed_10m",
    "wind_direction_10m"
).joinToString(",")

private suspend fun getForecastForLocation(
    latitude: Double,
    longitude: Double
): ForecastResponse {
    return OpenMeteoAPI.getForecast(
        latitude,
        longitude,
        HOURLY_PARAMETERS
    )
}

private fun degreesToCardinal(degrees: Int): String {
    val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = ((degrees + 22.5) / 45.0).toInt() % 8
    return directions[index]
}
//#endregion