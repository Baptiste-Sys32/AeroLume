package com.badeparday.aerolume.data.service

import com.badeparday.aerolume.domain.model.Flight
import kotlinx.coroutines.flow.StateFlow

interface FlightTrackingService {
    val flights: StateFlow<List<Flight>>
}
