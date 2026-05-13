package com.badeparday.aerolume.data.repository

import com.badeparday.aerolume.domain.model.AppSettings
import com.badeparday.aerolume.domain.model.MapAppearance
import com.badeparday.aerolume.domain.model.MotionIntensity
import com.badeparday.aerolume.domain.model.UnitSystem
import com.badeparday.aerolume.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryAppSettingsRepository : AppSettingsRepository {
    private val lock = Mutex()
    private val mutableSettings = MutableStateFlow(AppSettings())

    override val settings: StateFlow<AppSettings> = mutableSettings.asStateFlow()

    override suspend fun setDemoMode(enabled: Boolean) {
        update { it.copy(demoMode = enabled) }
    }

    override suspend fun setMotionIntensity(intensity: MotionIntensity) {
        update { it.copy(motionIntensity = intensity) }
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        update { it.copy(unitSystem = unitSystem) }
    }

    override suspend fun setMapAppearance(mapAppearance: MapAppearance) {
        update { it.copy(mapAppearance = mapAppearance) }
    }

    private suspend fun update(transform: (AppSettings) -> AppSettings) {
        lock.withLock {
            mutableSettings.value = transform(mutableSettings.value)
        }
    }
}
