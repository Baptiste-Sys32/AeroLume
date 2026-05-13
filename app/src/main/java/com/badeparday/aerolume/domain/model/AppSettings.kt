package com.badeparday.aerolume.domain.model

enum class MotionIntensity {
    Reduced,
    Standard,
    Expressive,
}

enum class UnitSystem {
    Metric,
    Imperial,
}

enum class MapAppearance {
    NightVector,
    NightHybrid,
}

data class AppSettings(
    val demoMode: Boolean = true,
    val motionIntensity: MotionIntensity = MotionIntensity.Standard,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val mapAppearance: MapAppearance = MapAppearance.NightVector,
)
