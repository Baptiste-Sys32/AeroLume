package com.badeparday.aerolume.domain.repository

import com.badeparday.aerolume.domain.model.AppSettings
import com.badeparday.aerolume.domain.model.MapAppearance
import com.badeparday.aerolume.domain.model.MotionIntensity
import com.badeparday.aerolume.domain.model.UnitSystem
import kotlinx.coroutines.flow.StateFlow

interface AppSettingsRepository {
    val settings: StateFlow<AppSettings>

    suspend fun setDemoMode(enabled: Boolean)

    suspend fun setMotionIntensity(intensity: MotionIntensity)

    suspend fun setUnitSystem(unitSystem: UnitSystem)

    suspend fun setMapAppearance(mapAppearance: MapAppearance)
}
