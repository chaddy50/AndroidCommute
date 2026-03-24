package com.chaddy50.morningcommute.model

import java.time.ZonedDateTime

sealed class CommuteStatus {

    data class RideToEnd(
        val bus1DepartureTime: ZonedDateTime,
        val bus1ArrivalAtStopB: ZonedDateTime,
        val bus2DepartureFromStopB: ZonedDateTime,
        val bufferMinutes: Long,
    ) : CommuteStatus()

    data class ExitEarly(
        val bus1DepartureTime: ZonedDateTime,
        val bus1ArrivalAtStopA: ZonedDateTime,
        val bus2DepartureFromStopA: ZonedDateTime,
        val bufferMinutes: Long,
    ) : CommuteStatus()

    data object Missed : CommuteStatus()

    data object Error : CommuteStatus()
}
