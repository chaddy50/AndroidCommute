package com.chaddy50.morningcommute.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

//#region API
val OpenMeteoAPI = Retrofit.Builder()
    .baseUrl("https://api.open-meteo.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(OpenMeteoService::class.java)

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String,
        @Query("timezone") timezone: String = "auto",
        @Query("temperature_unit") temperatureUnit: String = "fahrenheit"
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
    val temperature_2m: String
    // add other units as needed
)

data class Hourly(
    val time: List<String>,
    val temperature_2m: List<Double>
    // add other weather variables as needed
)
//#endregion