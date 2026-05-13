package com.badeparday.aerolume.data.service

import com.badeparday.aerolume.domain.model.RoutePoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val EarthRadiusMeters = 6_371_000.0

internal fun haversineDistanceMeters(start: RoutePoint, end: RoutePoint): Double {
    val startLat = Math.toRadians(start.latitude)
    val endLat = Math.toRadians(end.latitude)
    val deltaLat = Math.toRadians(end.latitude - start.latitude)
    val deltaLon = Math.toRadians(end.longitude - start.longitude)

    val a =
        sin(deltaLat / 2).pow(2.0) + cos(startLat) * cos(endLat) * sin(deltaLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EarthRadiusMeters * c
}

internal fun interpolateOnRoute(route: List<RoutePoint>, progress: Float): RoutePoint {
    if (route.size == 1) return route.first()
    val clamped = progress.coerceIn(0f, 1f)
    val segments = route.zipWithNext()
    val distances = segments.map { haversineDistanceMeters(it.first, it.second) }
    val totalDistance = distances.sum().takeIf { it > 0.0 } ?: 1.0
    val targetDistance = totalDistance * clamped
    var consumed = 0.0

    segments.forEachIndexed { index, segment ->
        val segmentDistance = distances[index]
        if (consumed + segmentDistance >= targetDistance) {
            val segmentProgress = ((targetDistance - consumed) / segmentDistance).toFloat()
            return lerp(segment.first, segment.second, segmentProgress)
        }
        consumed += segmentDistance
    }

    return route.last()
}

internal fun bearingOnRoute(route: List<RoutePoint>, progress: Float): Float {
    if (route.size < 2) return 0f
    val clamped = progress.coerceIn(0f, 0.999f)
    val current = interpolateOnRoute(route, clamped)
    val ahead = interpolateOnRoute(route, (clamped + 0.012f).coerceAtMost(1f))
    val lat1 = Math.toRadians(current.latitude)
    val lat2 = Math.toRadians(ahead.latitude)
    val deltaLon = Math.toRadians(ahead.longitude - current.longitude)
    val y = sin(deltaLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
    val angle = Math.toDegrees(atan2(y, x))
    return ((angle + 360.0) % 360.0).toFloat()
}

internal fun lerp(start: RoutePoint, end: RoutePoint, progress: Float): RoutePoint =
    RoutePoint(
        latitude = start.latitude + ((end.latitude - start.latitude) * progress),
        longitude = start.longitude + ((end.longitude - start.longitude) * progress),
    )

internal fun lerpInt(start: Int, end: Int, progress: Float): Int =
    (start + ((end - start) * progress)).roundToInt()
