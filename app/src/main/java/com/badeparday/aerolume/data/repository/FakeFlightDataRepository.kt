package com.badeparday.aerolume.data.repository

import com.badeparday.aerolume.data.service.FlightTrackingService
import com.badeparday.aerolume.data.source.DemoFlightCatalog
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.repository.FlightDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeFlightDataRepository(
    trackingService: FlightTrackingService,
    scope: CoroutineScope,
) : FlightDataRepository {

    private val lock = Mutex()
    private val savedIds = kotlinx.coroutines.flow.MutableStateFlow(DemoFlightCatalog.defaultSavedIds())

    override val flights: StateFlow<List<Flight>> =
        combine(trackingService.flights, savedIds) { flights, ids ->
            flights.map { it.copy(saved = it.id in ids) }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun observeSavedFlights(): Flow<List<Flight>> =
        flights.map { current -> current.filter(Flight::saved) }

    override fun observeFlight(flightId: String): Flow<Flight?> =
        flights.map { current -> current.firstOrNull { it.id == flightId } }

    override suspend fun searchFlights(query: String): List<Flight> {
        val needle = query.trim().lowercase()
        if (needle.isBlank()) return flights.value.sortedBy { it.flightNumber }

        return flights.value.filter { flight ->
            listOf(
                flight.flightNumber,
                flight.airline,
                flight.aircraft.typeCode,
                flight.aircraft.typeName,
                flight.origin.code,
                flight.origin.city,
                flight.destination.code,
                flight.destination.city,
                "${flight.origin.code} ${flight.destination.code}",
                "${flight.origin.city} ${flight.destination.city}",
            ).any { candidate -> candidate.lowercase().contains(needle) }
        }.sortedBy { it.flightNumber }
    }

    override suspend fun toggleSaved(flightId: String) {
        lock.withLock {
            savedIds.value = savedIds.value.toMutableSet().apply {
                if (!add(flightId)) remove(flightId)
            }
        }
    }
}
