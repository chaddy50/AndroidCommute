package com.chaddy50.morningcommute.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

//#region Constants
const val API_KEY = "***REMOVED***"
//#endregion

//#region Public functions
suspend fun getTripLeg(
    routeId: String,
    sourceStopId: String,
    destinationStopId: String,
    desiredDepartureTime: ZonedDateTime,
    directionHeadsign: String,
): TripLeg? {
    val busScheduleItem = getScheduleItemForStopAtTime(
        routeId,
        sourceStopId,
        desiredDepartureTime.minusMinutes(5),
        directionHeadsign,
    )
    if (busScheduleItem == null) return null

    val tripDetails = getTripDetails(
        busScheduleItem,
        destinationStopId
    )
    if (tripDetails == null) return null

    return TripLeg(
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(busScheduleItem.departureTime), ZoneId.systemDefault()),
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(tripDetails.departureTime), ZoneId.systemDefault())
    )
}
//#endregion

//#region API
val TransitAPI: TransitService = Retrofit.Builder()
    .baseUrl("https://external.transitapp.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .client(createOkHttpClient())
    .build()
    .create()

interface TransitService {
    @GET("v3/public/stop_departures")
    suspend fun getDeparturesForStop(
        @Query("global_stop_id") globalStopId: String,
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

data class TripLeg(
    val departureTime: ZonedDateTime,
    val arrivalTime: ZonedDateTime,
)
//#endregion

//#region Private functions
private suspend fun getScheduleItemForStopAtTime(
    routeId: String,
    stopId: String,
    time: ZonedDateTime,
    directionHeadsign: String,
) : ScheduleItem? {
    return try {
        val response = TransitAPI.getDeparturesForStop(
            stopId,
            true,
            time.toEpochSecond()
        )
        if (!response.isSuccessful) return null

        val departuresForRoute = response.body() ?: return null
        findNextDepartureForRoute(
            departuresForRoute.stopDepartures,
            routeId,
            directionHeadsign,
        )
    } catch (_: Exception) {
        return null
    }
}

private fun findNextDepartureForRoute(
    stopDepartures: List<StopDeparture>,
    routeId: String,
    directionHeadsign: String,
): ScheduleItem? {
    try {
        val nextDeparture = stopDepartures.first { it.globalRouteId == routeId }
        val itinerary = nextDeparture.itineraries.first { it.directionHeadsign == directionHeadsign }
        return itinerary.scheduleItems[0]
    } catch(_: NoSuchElementException) {
        return null
    }
}

private suspend fun getTripDetails(
    busScheduleItem: ScheduleItem,
    destinationStopId: String
): ScheduleItem? {
    return try {
        val response = TransitAPI.getTripDetails(busScheduleItem.tripSearchKey)
        if (!response.isSuccessful) return null

        val tripDetails = response.body() ?: return null
        tripDetails.scheduleItems.first { it.stop.globalStopId == destinationStopId }

    } catch (_: Exception) {
        return null
    }
}
//#endregion