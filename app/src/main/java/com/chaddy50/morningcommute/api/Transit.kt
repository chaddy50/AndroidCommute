package com.chaddy50.morningcommute.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

//#region Constants
const val API_KEY = "***REMOVED***"
const val ROUTE_D = "MMTWI:244383"
const val ROUTE_55 = "MMTWI:31664"
const val TOKAY_AT_SOUTH_SEGOE_EASTBOUND = "MMTWI:30303"
const val TOKAY_AT_SOUTH_SEGOE_WESTBOUND = "MMTWI:30205"
const val JUNCTION_PARK_AND_RIDE = "MMTWI:32273"
//#endregion

//#region API
val TransitAPI = Retrofit.Builder()
    .baseUrl("https://external.transitapp.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .client(createOkHttpClient())
    .build()
    .create(TransitService::class.java)

interface TransitService {
    @GET("v3/public/stop_departures")
    suspend fun getDeparturesForRoute(
        @Query("global_stop_id") globalStopID: String,
        @Query("should_update_realtime") shouldUpdateRealtime: Boolean = true,
        @Query("time") time: Long,
    ): Response<StopDeparturesResponse>

    @GET("v3/public/trip_details")
    suspend fun getTripDetails(
        @Query("trip_search_key") tripId: String,
    ) : Response<TripDetailsResponse>
}

private fun createOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("apiKey", API_KEY)
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .build()
}
//#endregion

//#region Data Classes
data class StopDeparturesResponse(
    @SerializedName("route_departures")
    val stopDepartures: List<StopDeparture>
)

data class TripDetailsResponse(
    @SerializedName("schedule_items")
    val scheduleItems: List<ScheduleItem>
)

data class StopDeparture(
    @SerializedName("global_route_id")
    val globalRouteId: String,
    val itineraries: List<Itinerary>,
)

data class Itinerary(
    @SerializedName("direction_id")
    val directionId: Int,
    @SerializedName("direction_headsign")
    val directionHeadsign: String,
    @SerializedName("schedule_items")
    val scheduleItems: List<ScheduleItem>,
)

data class ScheduleItem(
    @SerializedName("departure_time")
    val departureTime: Long,
    @SerializedName("arrival_time")
    val arrivalTime: Long,
    @SerializedName("is_cancelled")
    val isCancelled: Boolean,
    @SerializedName("is_real_time")
    val isRealTime: Boolean,
    @SerializedName("scheduled_departure_time")
    val scheduledDepartureTime: Long?,
    @SerializedName("scheduled_arrival_time")
    val scheduledArrivalTime: Long?,
    @SerializedName("rt_trip_id")
    val tripId: String,
    @SerializedName("trip_search_key")
    val tripSearchKey: String,
    @SerializedName("stop")
    val stop: Stop
)

data class Stop(
    @SerializedName("global_stop_id") val globalStopId: String?,
    @SerializedName("location_type") val locationType: Int?,
    @SerializedName("stop_lat") val latitude: Double?,
    @SerializedName("stop_lon") val longitude: Double?,
    @SerializedName("stop_name") val name: String?,
    @SerializedName("departure_time") val departureTime: Long?,
    @SerializedName("arrival_time") val arrivalTime: Long?,
)
//#endregion