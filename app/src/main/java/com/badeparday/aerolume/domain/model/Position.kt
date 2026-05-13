package com.badeparday.aerolume.domain.model

import java.time.Instant

data class Position(
    val latitude: Double,
    val longitude: Double,
    val altitudeFeet: Int,
    val speedKnots: Int,
    val headingDegrees: Float,
    val updatedAt: Instant,
)
