package com.badeparday.aerolume.domain.model

import java.time.Instant

data class Flight(
    val id: String,
    val flightNumber: String,
    val airline: String,
    val aircraft: Aircraft,
    val origin: Airport,
    val destination: Airport,
    val scheduledDeparture: Instant,
    val estimatedDeparture: Instant,
    val scheduledArrival: Instant,
    val estimatedArrival: Instant,
    val status: FlightStatus,
    val position: Position,
    val route: List<RoutePoint>,
    val breadcrumb: List<RoutePoint>,
    val progress: Float,
    val accentColorHex: Long,
    val saved: Boolean,
)
