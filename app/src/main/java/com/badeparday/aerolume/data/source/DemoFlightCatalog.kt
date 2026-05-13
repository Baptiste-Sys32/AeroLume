package com.badeparday.aerolume.data.source

import com.badeparday.aerolume.domain.model.Aircraft
import com.badeparday.aerolume.domain.model.Airport
import com.badeparday.aerolume.domain.model.RoutePoint

internal data class DemoFlightSeed(
    val id: String,
    val flightNumber: String,
    val airline: String,
    val aircraft: Aircraft,
    val origin: Airport,
    val destination: Airport,
    val route: List<RoutePoint>,
    val initialProgress: Float,
    val progressStep: Float,
    val blockMinutes: Int,
    val delayMinutes: Int,
    val cruiseAltitudeFeet: Int,
    val cruiseSpeedKnots: Int,
    val accentColorHex: Long,
)

internal object DemoFlightCatalog {
    private val cdg = airport("CDG", "Paris", "Charles de Gaulle", "Europe/Paris", 49.0097, 2.5479)
    private val lhr = airport("LHR", "London", "Heathrow", "Europe/London", 51.4700, -0.4543)
    private val jfk = airport("JFK", "New York", "John F. Kennedy", "America/New_York", 40.6413, -73.7781)
    private val ams = airport("AMS", "Amsterdam", "Schiphol", "Europe/Amsterdam", 52.3105, 4.7683)
    private val nce = airport("NCE", "Nice", "Cote d'Azur", "Europe/Paris", 43.6653, 7.2150)
    private val mad = airport("MAD", "Madrid", "Adolfo Suarez", "Europe/Madrid", 40.4983, -3.5676)
    private val fco = airport("FCO", "Rome", "Fiumicino", "Europe/Rome", 41.8003, 12.2389)
    private val ath = airport("ATH", "Athens", "Eleftherios Venizelos", "Europe/Athens", 37.9364, 23.9475)
    private val ist = airport("IST", "Istanbul", "Istanbul Airport", "Europe/Istanbul", 41.2753, 28.7519)
    private val gva = airport("GVA", "Geneva", "Cointrin", "Europe/Zurich", 46.2381, 6.1089)
    private val ory = airport("ORY", "Paris", "Orly", "Europe/Paris", 48.7262, 2.3652)

    fun seeds(): List<DemoFlightSeed> =
        listOf(
            DemoFlightSeed(
                id = "af011",
                flightNumber = "AF011",
                airline = "Air France",
                aircraft = aircraft("af-359", "F-HUVB", "A359", "Airbus A350-900", "Air France", "AFR011"),
                origin = cdg,
                destination = jfk,
                route = route(
                    cdg,
                    point(48.2, -4.6, "Brittany"),
                    point(47.4, -24.8, "North Atlantic"),
                    point(45.8, -48.0, "Oceanic"),
                    point(44.8, -63.4, "Halifax"),
                    jfk,
                ),
                initialProgress = 0.34f,
                progressStep = 0.0036f,
                blockMinutes = 455,
                delayMinutes = 4,
                cruiseAltitudeFeet = 37_000,
                cruiseSpeedKnots = 474,
                accentColorHex = 0xFF8BC8FF,
            ),
            DemoFlightSeed(
                id = "dl229",
                flightNumber = "DL229",
                airline = "Delta Air Lines",
                aircraft = aircraft("dl-339", "N421DZ", "A339", "Airbus A330-900neo", "Delta Air Lines", "DAL229"),
                origin = jfk,
                destination = cdg,
                route = route(
                    jfk,
                    point(44.6, -62.2, "Nova Scotia"),
                    point(49.4, -34.8, "North Atlantic"),
                    point(52.2, -13.6, "Ireland"),
                    point(50.4, -1.4, "Channel"),
                    cdg,
                ),
                initialProgress = 0.71f,
                progressStep = 0.0039f,
                blockMinutes = 425,
                delayMinutes = 0,
                cruiseAltitudeFeet = 36_000,
                cruiseSpeedKnots = 468,
                accentColorHex = 0xFF85D6B6,
            ),
            DemoFlightSeed(
                id = "ba317",
                flightNumber = "BA317",
                airline = "British Airways",
                aircraft = aircraft("ba-320", "G-EUYR", "A320", "Airbus A320-200", "British Airways", "BAW317"),
                origin = lhr,
                destination = cdg,
                route = route(
                    lhr,
                    point(51.0, 1.0, "Dover"),
                    point(50.8, 2.3, "Calais"),
                    point(49.7, 2.6, "Paris FIR"),
                    cdg,
                ),
                initialProgress = 0.82f,
                progressStep = 0.0064f,
                blockMinutes = 78,
                delayMinutes = 0,
                cruiseAltitudeFeet = 18_000,
                cruiseSpeedKnots = 302,
                accentColorHex = 0xFF89E5F7,
            ),
            DemoFlightSeed(
                id = "kl1457",
                flightNumber = "KL1457",
                airline = "KLM",
                aircraft = aircraft("kl-738", "PH-BGX", "B738", "Boeing 737-800", "KLM", "KLM1457"),
                origin = ams,
                destination = nce,
                route = route(
                    ams,
                    point(50.9, 4.4, "Brussels"),
                    point(47.3, 5.1, "Dijon"),
                    point(45.7, 4.8, "Lyon"),
                    nce,
                ),
                initialProgress = 0.56f,
                progressStep = 0.0055f,
                blockMinutes = 118,
                delayMinutes = 12,
                cruiseAltitudeFeet = 33_000,
                cruiseSpeedKnots = 438,
                accentColorHex = 0xFF7AAFFF,
            ),
            DemoFlightSeed(
                id = "lx1832",
                flightNumber = "LX1832",
                airline = "SWISS",
                aircraft = aircraft("lx-223", "HB-JCH", "BCS3", "Airbus A220-300", "SWISS", "SWR1832"),
                origin = gva,
                destination = ath,
                route = route(
                    gva,
                    point(45.4, 9.2, "Milan"),
                    point(43.7, 14.4, "Adriatic"),
                    point(41.7, 20.0, "Albania"),
                    ath,
                ),
                initialProgress = 0.46f,
                progressStep = 0.0048f,
                blockMinutes = 155,
                delayMinutes = 0,
                cruiseAltitudeFeet = 35_000,
                cruiseSpeedKnots = 432,
                accentColorHex = 0xFFB4F0DC,
            ),
            DemoFlightSeed(
                id = "tk1823",
                flightNumber = "TK1823",
                airline = "Turkish Airlines",
                aircraft = aircraft("tk-321", "TC-LSP", "A21N", "Airbus A321neo", "Turkish Airlines", "THY1823"),
                origin = ist,
                destination = cdg,
                route = route(
                    ist,
                    point(42.7, 23.3, "Sofia"),
                    point(48.2, 16.3, "Vienna"),
                    point(48.1, 11.5, "Munich"),
                    point(49.3, 3.6, "Reims"),
                    cdg,
                ),
                initialProgress = 0.67f,
                progressStep = 0.0045f,
                blockMinutes = 215,
                delayMinutes = 18,
                cruiseAltitudeFeet = 34_000,
                cruiseSpeedKnots = 451,
                accentColorHex = 0xFFFFBC74,
            ),
            DemoFlightSeed(
                id = "ib525",
                flightNumber = "IB525",
                airline = "Iberia",
                aircraft = aircraft("ib-320", "EC-NFN", "A320", "Airbus A320neo", "Iberia", "IBE525"),
                origin = mad,
                destination = ory,
                route = route(
                    mad,
                    point(42.4, -3.7, "Burgos"),
                    point(44.8, -0.6, "Bordeaux"),
                    point(47.9, 1.9, "Orleans"),
                    ory,
                ),
                initialProgress = 0.24f,
                progressStep = 0.0052f,
                blockMinutes = 110,
                delayMinutes = 0,
                cruiseAltitudeFeet = 31_000,
                cruiseSpeedKnots = 421,
                accentColorHex = 0xFF9BBEFF,
            ),
            DemoFlightSeed(
                id = "az349",
                flightNumber = "AZ349",
                airline = "ITA Airways",
                aircraft = aircraft("az-320", "EI-DTG", "A320", "Airbus A320-200", "ITA Airways", "ITY349"),
                origin = fco,
                destination = lhr,
                route = route(
                    fco,
                    point(43.8, 11.2, "Florence"),
                    point(45.1, 7.6, "Turin"),
                    point(46.2, 6.1, "Geneva"),
                    point(49.1, 2.5, "Paris FIR"),
                    lhr,
                ),
                initialProgress = 0.74f,
                progressStep = 0.0041f,
                blockMinutes = 145,
                delayMinutes = 26,
                cruiseAltitudeFeet = 30_000,
                cruiseSpeedKnots = 402,
                accentColorHex = 0xFFFFA96C,
            ),
        )

    fun defaultSavedIds(): Set<String> = setOf("af011", "dl229", "ba317", "tk1823")

    private fun airport(
        code: String,
        city: String,
        name: String,
        timeZoneId: String,
        latitude: Double,
        longitude: Double,
    ) = Airport(code, city, name, timeZoneId, latitude, longitude)

    private fun aircraft(
        id: String,
        registration: String,
        typeCode: String,
        typeName: String,
        airline: String,
        callsign: String,
    ) = Aircraft(id, registration, typeCode, typeName, airline, callsign)

    private fun point(latitude: Double, longitude: Double, label: String? = null) =
        RoutePoint(latitude, longitude, label)

    private fun route(vararg points: Any): List<RoutePoint> =
        points.map { point ->
            when (point) {
                is Airport -> RoutePoint(point.latitude, point.longitude, point.code)
                is RoutePoint -> point
                else -> error("Unsupported route point: $point")
            }
        }
}
