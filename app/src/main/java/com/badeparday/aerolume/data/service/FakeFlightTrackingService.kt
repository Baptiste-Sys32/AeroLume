package com.badeparday.aerolume.data.service

import com.badeparday.aerolume.data.source.DemoFlightCatalog
import com.badeparday.aerolume.data.source.DemoFlightSeed
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.model.FlightStatus
import com.badeparday.aerolume.domain.model.Position
import com.badeparday.aerolume.domain.model.RoutePoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class FakeFlightTrackingService(
    private val externalScope: CoroutineScope,
    private val clock: Clock = Clock.systemUTC(),
) : FlightTrackingService {

    private data class SimulationState(
        val seed: DemoFlightSeed,
        val progress: Float,
        val breadcrumb: List<RoutePoint>,
    )

    private val simulatorScope = CoroutineScope(externalScope.coroutineContext + SupervisorJob() + Dispatchers.Default)

    private var states: List<SimulationState> =
        DemoFlightCatalog.seeds().map { seed ->
            val currentPoint = interpolateOnRoute(seed.route, seed.initialProgress)
            SimulationState(
                seed = seed,
                progress = seed.initialProgress,
                breadcrumb = listOf(currentPoint),
            )
        }

    private val _flights = MutableStateFlow(states.map { it.toFlight(clock.instant()) })
    override val flights: StateFlow<List<Flight>> = _flights.asStateFlow()

    init {
        simulatorScope.launch {
            while (isActive) {
                delay(1_150)
                val now = clock.instant()
                states = states.map(::advance)
                _flights.value = states.map { it.toFlight(now) }
            }
        }
    }

    private fun advance(state: SimulationState): SimulationState {
        val nextProgress = (state.progress + state.seed.progressStep).let { progress ->
            when {
                progress >= 0.992f -> 0.082f + (progress - 0.992f).absoluteValue
                else -> progress
            }
        }
        val currentPoint = interpolateOnRoute(state.seed.route, nextProgress)
        return state.copy(
            progress = nextProgress,
            breadcrumb = (state.breadcrumb + currentPoint).takeLast(18),
        )
    }

    private fun SimulationState.toFlight(now: Instant): Flight {
        val departureLeadSeconds = (seed.blockMinutes * 60 * progress).roundToInt().toLong()
        val scheduledDeparture = now.minusSeconds(departureLeadSeconds)
        val scheduledArrival = scheduledDeparture.plusSeconds(seed.blockMinutes * 60L)
        val estimatedDeparture = scheduledDeparture.plusSeconds(seed.delayMinutes * 60L)
        val estimatedArrival = scheduledArrival.plusSeconds(seed.delayMinutes * 60L)
        val heading = bearingOnRoute(seed.route, progress)
        val altitude = altitudeFor(seed, progress)
        val speed = speedFor(seed, progress)

        return Flight(
            id = seed.id,
            flightNumber = seed.flightNumber,
            airline = seed.airline,
            aircraft = seed.aircraft,
            origin = seed.origin,
            destination = seed.destination,
            scheduledDeparture = scheduledDeparture,
            estimatedDeparture = estimatedDeparture,
            scheduledArrival = scheduledArrival,
            estimatedArrival = estimatedArrival,
            status = statusFor(seed, progress),
            position = Position(
                latitude = interpolateOnRoute(seed.route, progress).latitude,
                longitude = interpolateOnRoute(seed.route, progress).longitude,
                altitudeFeet = altitude,
                speedKnots = speed,
                headingDegrees = heading,
                updatedAt = now,
            ),
            route = seed.route,
            breadcrumb = breadcrumb,
            progress = progress,
            accentColorHex = seed.accentColorHex,
            saved = false,
        )
    }

    private fun altitudeFor(seed: DemoFlightSeed, progress: Float): Int =
        when {
            progress < 0.08f -> lerpInt(3_000, 16_000, progress / 0.08f)
            progress < 0.18f -> lerpInt(16_000, seed.cruiseAltitudeFeet, (progress - 0.08f) / 0.10f)
            progress < 0.76f -> seed.cruiseAltitudeFeet + ((progress * 8_000).roundToInt() % 1_200) - 600
            progress < 0.92f -> lerpInt(seed.cruiseAltitudeFeet, 10_000, (progress - 0.76f) / 0.16f)
            else -> lerpInt(10_000, 2_800, (progress - 0.92f) / 0.08f)
        }

    private fun speedFor(seed: DemoFlightSeed, progress: Float): Int =
        when {
            progress < 0.08f -> lerpInt(180, 290, progress / 0.08f)
            progress < 0.18f -> lerpInt(290, seed.cruiseSpeedKnots, (progress - 0.08f) / 0.10f)
            progress < 0.76f -> seed.cruiseSpeedKnots + ((progress * 1_000).roundToInt() % 14) - 7
            progress < 0.92f -> lerpInt(seed.cruiseSpeedKnots, 265, (progress - 0.76f) / 0.16f)
            else -> lerpInt(265, 178, (progress - 0.92f) / 0.08f)
        }

    private fun statusFor(seed: DemoFlightSeed, progress: Float): FlightStatus =
        when {
            seed.delayMinutes >= 22 && progress < 0.12f -> FlightStatus.Delayed
            progress < 0.08f -> FlightStatus.Boarding
            progress < 0.18f -> FlightStatus.Climbing
            progress < 0.76f -> FlightStatus.Cruising
            progress < 0.92f -> FlightStatus.Descending
            progress < 0.985f -> FlightStatus.Final
            else -> FlightStatus.Landed
        }
}
