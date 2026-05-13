package com.badeparday.aerolume.domain.model

data class Airport(
    val code: String,
    val city: String,
    val name: String,
    val timeZoneId: String,
    val latitude: Double,
    val longitude: Double,
)
