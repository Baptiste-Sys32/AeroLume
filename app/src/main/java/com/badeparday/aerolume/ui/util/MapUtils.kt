package com.badeparday.aerolume.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.badeparday.aerolume.data.service.haversineDistanceMeters
import com.badeparday.aerolume.domain.model.Airport
import com.badeparday.aerolume.domain.model.Position
import com.badeparday.aerolume.domain.model.RoutePoint
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Point
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun RoutePoint.toPoint(): Point = Point.fromLngLat(longitude, latitude)

fun Position.toPoint(): Point = Point.fromLngLat(longitude, latitude)

fun Airport.toPoint(): Point = Point.fromLngLat(longitude, latitude)

fun RoutePoint.toLatLng(): LatLng = LatLng(latitude, longitude)

fun Position.toLatLng(): LatLng = LatLng(latitude, longitude)

fun Airport.toLatLng(): LatLng = LatLng(latitude, longitude)

fun flightAccentColor(hex: Long): Color = Color(hex)

fun accentHexString(hex: Long): String = String.format("#%06X", (hex and 0x00FFFFFF))

fun densifyRouteForMap(route: List<RoutePoint>, maxSegmentMeters: Double = 320_000.0): List<Point> {
    if (route.isEmpty()) return emptyList()
    if (route.size == 1) return listOf(route.first().toPoint())

    val densified = mutableListOf<Point>()
    route.zipWithNext().forEachIndexed { segmentIndex, (start, end) ->
        val segmentDistance = haversineDistanceMeters(start, end)
        val steps = maxOf(3, ceil(segmentDistance / maxSegmentMeters).toInt() + 1)
        repeat(steps) { step ->
            if (segmentIndex > 0 && step == 0) return@repeat
            val progress = step.toDouble() / (steps - 1).coerceAtLeast(1)
            val point = interpolateGreatCircle(start, end, progress)
            densified += point.toPoint()
        }
    }
    return densified
}

private fun interpolateGreatCircle(
    start: RoutePoint,
    end: RoutePoint,
    fraction: Double,
): RoutePoint {
    val startLat = Math.toRadians(start.latitude)
    val startLon = Math.toRadians(start.longitude)
    val endLat = Math.toRadians(end.latitude)
    val endLon = Math.toRadians(end.longitude)

    val delta =
        2 * acos(
            (
                sin(startLat) * sin(endLat) +
                    cos(startLat) * cos(endLat) * cos(endLon - startLon)
            ).coerceIn(-1.0, 1.0),
        )

    if (delta == 0.0) return start

    val a = sin((1 - fraction) * delta) / sin(delta)
    val b = sin(fraction * delta) / sin(delta)

    val x = a * cos(startLat) * cos(startLon) + b * cos(endLat) * cos(endLon)
    val y = a * cos(startLat) * sin(startLon) + b * cos(endLat) * sin(endLon)
    val z = a * sin(startLat) + b * sin(endLat)

    val latitude = Math.toDegrees(atan2Safe(z, kotlin.math.sqrt((x * x) + (y * y))))
    val longitude = Math.toDegrees(atan2Safe(y, x))

    return RoutePoint(
        latitude = latitude,
        longitude = longitude,
    )
}

private fun atan2Safe(y: Double, x: Double): Double = kotlin.math.atan2(y, x)

fun createAircraftMarkerBitmap(
    context: Context,
    selected: Boolean,
): Bitmap {
    val colorArgb =
        if (selected) {
            Color(0xFFD6D9DC).toArgb()
        } else {
            Color(0xFFB9BEC2).toArgb()
        }
    val density = context.resources.displayMetrics.density
    val size = ((if (selected) 34 else 28) * density).roundToInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = size / 2f
    val accentRed = android.graphics.Color.red(colorArgb)
    val accentGreen = android.graphics.Color.green(colorArgb)
    val accentBlue = android.graphics.Color.blue(colorArgb)

    val tailPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color =
                if (selected) {
                    android.graphics.Color.argb(124, accentRed, accentGreen, accentBlue)
                } else {
                    android.graphics.Color.argb(82, 164, 166, 169)
                }
            style = Paint.Style.STROKE
            strokeWidth = density * if (selected) 1.45f else 1.1f
            strokeCap = Paint.Cap.ROUND
        }
    val outlinePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(if (selected) 96 else 72, 4, 4, 4)
            style = Paint.Style.STROKE
            strokeWidth = density * if (selected) 2.4f else 2.0f
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    val headPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color =
                if (selected) {
                    android.graphics.Color.argb(208, accentRed, accentGreen, accentBlue)
                } else {
                    android.graphics.Color.argb(164, 206, 208, 210)
                }
            style = Paint.Style.STROKE
            strokeWidth = density * if (selected) 1.7f else 1.35f
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    val nodePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color =
                if (selected) {
                    android.graphics.Color.argb(176, accentRed, accentGreen, accentBlue)
                } else {
                    android.graphics.Color.argb(112, 194, 196, 198)
                }
            style = Paint.Style.FILL
        }
    canvas.drawLine(center, size * 0.38f, center, size * 0.80f, tailPaint)

    val chevron =
        Path().apply {
            moveTo(center - size * 0.13f, size * 0.41f)
            lineTo(center, size * 0.17f)
            lineTo(center + size * 0.13f, size * 0.41f)
        }
    canvas.drawPath(chevron, outlinePaint)
    canvas.drawPath(chevron, headPaint)
    canvas.drawCircle(center, size * 0.44f, size * 0.048f, nodePaint)

    return bitmap
}
