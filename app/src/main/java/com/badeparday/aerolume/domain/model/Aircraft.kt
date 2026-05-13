package com.badeparday.aerolume.domain.model

data class Aircraft(
    val id: String,
    val registration: String,
    val typeCode: String,
    val typeName: String,
    val airline: String,
    val callsign: String,
)
