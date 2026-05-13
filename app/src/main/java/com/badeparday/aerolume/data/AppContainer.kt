package com.badeparday.aerolume.data

import com.badeparday.aerolume.data.repository.FakeFlightDataRepository
import com.badeparday.aerolume.data.repository.InMemoryAppSettingsRepository
import com.badeparday.aerolume.data.service.FakeFlightTrackingService
import com.badeparday.aerolume.domain.repository.AppSettingsRepository
import com.badeparday.aerolume.domain.repository.FlightDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val trackingService = FakeFlightTrackingService(appScope)

    val settingsRepository: AppSettingsRepository = InMemoryAppSettingsRepository()
    val flightDataRepository: FlightDataRepository = FakeFlightDataRepository(trackingService, appScope)
}
