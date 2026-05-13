package com.badeparday.aerolume.domain.repository

import com.badeparday.aerolume.domain.model.Flight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FlightDataRepository {
    val flights: StateFlow<List<Flight>>

    fun observeSavedFlights(): Flow<List<Flight>>

    fun observeFlight(flightId: String): Flow<Flight?>

    suspend fun searchFlights(query: String): List<Flight>

    suspend fun toggleSaved(flightId: String)
}
