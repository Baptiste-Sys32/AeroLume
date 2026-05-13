package com.badeparday.aerolume.ui.util

import com.badeparday.aerolume.domain.model.Airport
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.model.FlightStatus
import com.badeparday.aerolume.domain.model.UnitSystem
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
private val DayFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.US)

fun formatAirportTime(instant: Instant, airport: Airport): String =
    TimeFormatter.format(instant.atZone(ZoneId.of(airport.timeZoneId)))

fun formatAirportDay(instant: Instant, airport: Airport): String =
    DayFormatter.format(instant.atZone(ZoneId.of(airport.timeZoneId)))

fun formatBlockTime(flight: Flight): String {
    val duration = Duration.between(flight.estimatedDeparture, flight.estimatedArrival).abs()
    return "${duration.toHoursPart()}h ${duration.toMinutesPart().toString().padStart(2, '0')}m"
}

fun formatDelay(flight: Flight): String {
    val delayMinutes = Duration.between(flight.scheduledArrival, flight.estimatedArrival).toMinutes()
    return when {
        delayMinutes > 0 -> "+${delayMinutes}m"
        delayMinutes < 0 -> "${delayMinutes}m"
        else -> "On time"
    }
}

fun formatAltitude(feet: Int, unitSystem: UnitSystem): String =
    when (unitSystem) {
        UnitSystem.Imperial -> "${feet / 100}00 ft"
        UnitSystem.Metric -> "${(feet * 0.3048).roundToInt().toString().padStart(5, ' ')} m".trim()
    }

fun formatSpeed(knots: Int, unitSystem: UnitSystem): String =
    when (unitSystem) {
        UnitSystem.Imperial -> "$knots kt"
        UnitSystem.Metric -> "${(knots * 1.852).roundToInt()} km/h"
    }

fun formatHeading(headingDegrees: Float): String = "${headingDegrees.roundToInt()}°"

fun statusLabel(status: FlightStatus): String =
    when (status) {
        FlightStatus.Boarding -> "Boarding"
        FlightStatus.Climbing -> "Climb"
        FlightStatus.Cruising -> "Cruise"
        FlightStatus.Descending -> "Descent"
        FlightStatus.Final -> "Final"
        FlightStatus.Delayed -> "Delayed"
        FlightStatus.Landed -> "Landed"
    }

fun formatRelativeProgress(progress: Float): String = "${(progress * 100).roundToInt()}%"
