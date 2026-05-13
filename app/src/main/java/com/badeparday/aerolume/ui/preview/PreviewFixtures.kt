package com.badeparday.aerolume.ui.preview

import com.badeparday.aerolume.domain.model.Aircraft
import com.badeparday.aerolume.domain.model.Airport
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.model.FlightStatus
import com.badeparday.aerolume.domain.model.Position
import com.badeparday.aerolume.domain.model.RoutePoint
import java.time.Instant
import java.time.temporal.ChronoUnit

object PreviewFixtures {
    private val cdg =
        Airport(
            code = "CDG",
            city = "Paris",
            name = "Charles de Gaulle",
            timeZoneId = "Europe/Paris",
            latitude = 49.0097,
            longitude = 2.5479,
        )
    private val jfk =
        Airport(
            code = "JFK",
            city = "New York",
            name = "John F. Kennedy",
            timeZoneId = "America/New_York",
            latitude = 40.6413,
            longitude = -73.7781,
        )
    val flight: Flight
        get() {
            val now = Instant.now().truncatedTo(ChronoUnit.MINUTES)
            return Flight(
                id = "preview-af011",
                flightNumber = "AF011",
                airline = "Air France",
                aircraft =
                    Aircraft(
                        id = "preview-aircraft",
                        registration = "F-HUVB",
                        typeCode = "A359",
                        typeName = "Airbus A350-900",
                        airline = "Air France",
                        callsign = "AFR011",
                    ),
                origin = cdg,
                destination = jfk,
                scheduledDeparture = now.minus(3, ChronoUnit.HOURS),
                estimatedDeparture = now.minus(3, ChronoUnit.HOURS).plus(4, ChronoUnit.MINUTES),
                scheduledArrival = now.plus(4, ChronoUnit.HOURS),
                estimatedArrival = now.plus(4, ChronoUnit.HOURS).plus(4, ChronoUnit.MINUTES),
                status = FlightStatus.Cruising,
                position =
                    Position(
                        latitude = 47.2,
                        longitude = -28.4,
                        altitudeFeet = 36_900,
                        speedKnots = 472,
                        headingDegrees = 288f,
                        updatedAt = now,
                    ),
                route =
                    listOf(
                        RoutePoint(49.0097, 2.5479, "CDG"),
                        RoutePoint(48.0, -4.5, "Brittany"),
                        RoutePoint(47.3, -28.4, "Oceanic"),
                        RoutePoint(44.5, -55.0, "Approach"),
                        RoutePoint(40.6413, -73.7781, "JFK"),
                    ),
                breadcrumb =
                    listOf(
                        RoutePoint(47.0, -22.0),
                        RoutePoint(47.1, -25.0),
                        RoutePoint(47.2, -28.4),
                    ),
                progress = 0.42f,
                accentColorHex = 0xFF86C9FF,
                saved = true,
            )
        }
}
