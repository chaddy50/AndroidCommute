package com.chaddy50.morningcommute.api

import com.chaddy50.morningcommute.model.CommuteStatus
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
import java.time.temporal.ChronoUnit

//#region Constants
const val API_KEY = "***REMOVED***"
const val TRANSFER_BUFFER_MINUTES = 3L
//#endregion

//#region Public functions

/**
 * Represents a transfer point where Bus 1 is exited and Bus 2 is boarded.
 * These may be different stops (e.g. opposite sides of the street).
 *
 * @param exitStopId The stop where Bus 1 arrives — used to look up Bus 1's arrival time via trip details.
 * @param boardingStopId The stop where Bus 2 departs — used to query Bus 2's departure time.
 */
data class TransferStop(
    val exitStopId: String,
    val boardingStopId: String,
)

suspend fun getCommuteStatus(
    bus1RouteId: String,
    bus1DirectionHeadsign: String,
    bus2RouteId: String,
    bus2DirectionHeadsign: String,
    boardingStopId: String,
    earlyTransfer: TransferStop,
    preferredTransfer: TransferStop,
    desiredDepartureTime: ZonedDateTime,
): CommuteStatus {
    // Call 1: get Bus 1 departure and trip key (must complete before calls 2/3/4)
    val bus1Item = getScheduleItemForStopAtTime(
        bus1RouteId,
        boardingStopId,
        desiredDepartureTime.minusMinutes(5),
        bus1DirectionHeadsign,
    ) ?: return CommuteStatus.Error

    val bus1DepartureTime = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(bus1Item.departureTime),
        ZoneId.systemDefault()
    )

    // Calls 2, 3, 4 in parallel:
    // - Trip details to find Bus 1's arrival at both exit stops
    // - Bus 2 departures from both boarding stops (which may differ from the exit stops)
    val (bus1Stops, bus2AtEarlyTransfer, bus2AtPreferredTransfer) = coroutineScope {
        val d1 = async { getStopsFromTripDetails(bus1Item.tripSearchKey, earlyTransfer.exitStopId, preferredTransfer.exitStopId) }
        val d2 = async { getScheduleItemForStopAtTime(bus2RouteId, earlyTransfer.boardingStopId, desiredDepartureTime, bus2DirectionHeadsign) }
        val d3 = async { getScheduleItemForStopAtTime(bus2RouteId, preferredTransfer.boardingStopId, desiredDepartureTime, bus2DirectionHeadsign) }
        Triple(d1.await(), d2.await(), d3.await())
    }

    bus1Stops ?: return CommuteStatus.Error

    return computeCommuteStatus(bus1DepartureTime, bus1Stops, bus2AtEarlyTransfer, bus2AtPreferredTransfer)
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

private data class Bus1StopArrivals(
    val arrivalAtEarlyTransferStop: ZonedDateTime,
    val arrivalAtPreferredTransferStop: ZonedDateTime,
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
        null
    }
}

private fun findNextDepartureForRoute(
    stopDepartures: List<StopDeparture>,
    routeId: String,
    directionHeadsign: String,
): ScheduleItem? {
    val nextDeparture = stopDepartures.firstOrNull { it.globalRouteId == routeId } ?: return null
    val itinerary = nextDeparture.itineraries.firstOrNull { it.directionHeadsign == directionHeadsign } ?: return null
    return itinerary.scheduleItems.firstOrNull()
}

private suspend fun getStopsFromTripDetails(
    tripSearchKey: String,
    earlyTransferExitStopId: String,
    preferredTransferExitStopId: String,
): Bus1StopArrivals? {
    return try {
        val response = TransitAPI.getTripDetails(tripSearchKey)
        if (!response.isSuccessful) return null
        val scheduleItems = response.body()?.scheduleItems ?: return null

        val earlyTransferItem = scheduleItems.firstOrNull { it.stop.globalStopId == earlyTransferExitStopId } ?: return null
        val preferredTransferItem = scheduleItems.firstOrNull { it.stop.globalStopId == preferredTransferExitStopId } ?: return null

        Bus1StopArrivals(
            arrivalAtEarlyTransferStop = ZonedDateTime.ofInstant(Instant.ofEpochSecond(earlyTransferItem.departureTime), ZoneId.systemDefault()),
            arrivalAtPreferredTransferStop = ZonedDateTime.ofInstant(Instant.ofEpochSecond(preferredTransferItem.departureTime), ZoneId.systemDefault()),
        )
    } catch (_: Exception) {
        null
    }
}

private fun computeCommuteStatus(
    bus1DepartureTime: ZonedDateTime,
    bus1Stops: Bus1StopArrivals,
    bus2AtEarlyTransfer: ScheduleItem?,
    bus2AtPreferredTransfer: ScheduleItem?,
): CommuteStatus {
    val bus2DeparturePreferred = bus2AtPreferredTransfer?.let {
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(it.departureTime), ZoneId.systemDefault())
    }
    val bus2DepartureEarly = bus2AtEarlyTransfer?.let {
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(it.departureTime), ZoneId.systemDefault())
    }

    val preferredBufferMinutes = if (bus2DeparturePreferred != null) {
        ChronoUnit.MINUTES.between(bus1Stops.arrivalAtPreferredTransferStop, bus2DeparturePreferred)
    } else null

    val earlyBufferMinutes = if (bus2DepartureEarly != null) {
        ChronoUnit.MINUTES.between(bus1Stops.arrivalAtEarlyTransferStop, bus2DepartureEarly)
    } else null

    if (preferredBufferMinutes != null && preferredBufferMinutes >= TRANSFER_BUFFER_MINUTES) {
        return CommuteStatus.RideToEnd(
            bus1DepartureTime = bus1DepartureTime,
            bus1ArrivalAtStopB = bus1Stops.arrivalAtPreferredTransferStop,
            bus2DepartureFromStopB = bus2DeparturePreferred!!,
            bufferMinutes = preferredBufferMinutes,
        )
    }

    if (earlyBufferMinutes != null && earlyBufferMinutes >= TRANSFER_BUFFER_MINUTES) {
        return CommuteStatus.ExitEarly(
            bus1DepartureTime = bus1DepartureTime,
            bus1ArrivalAtStopA = bus1Stops.arrivalAtEarlyTransferStop,
            bus2DepartureFromStopA = bus2DepartureEarly!!,
            bufferMinutes = earlyBufferMinutes,
        )
    }

    return CommuteStatus.Missed
}
//#endregion