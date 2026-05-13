package com.badeparday.aerolume.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.ui.preview.PreviewFixtures
import com.badeparday.aerolume.ui.theme.AerolumeTheme
import com.badeparday.aerolume.ui.util.formatAirportTime
import com.badeparday.aerolume.ui.util.flightAccentColor

@Composable
fun RouteTimeline(
    flight: Flight,
    modifier: Modifier = Modifier,
) {
    val accent = flightAccentColor(flight.accentColorHex)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(26.dp),
        ) {
            val lineY = size.height / 2f
            val start = size.width * 0.04f
            val end = size.width * 0.96f
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = androidx.compose.ui.geometry.Offset(start, lineY),
                end = androidx.compose.ui.geometry.Offset(end, lineY),
                strokeWidth = 5f,
            )
            drawLine(
                brush =
                    Brush.horizontalGradient(
                        listOf(
                            accent.copy(alpha = 0.28f),
                            accent.copy(alpha = 0.60f),
                        ),
                    ),
                start = androidx.compose.ui.geometry.Offset(start, lineY),
                end = androidx.compose.ui.geometry.Offset(start + ((end - start) * flight.progress), lineY),
                strokeWidth = 5f,
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.16f),
                radius = 7f,
                center = androidx.compose.ui.geometry.Offset(start, lineY),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.16f),
                radius = 7f,
                center = androidx.compose.ui.geometry.Offset(end, lineY),
            )
            drawCircle(
                color = accent.copy(alpha = 0.14f),
                radius = 20f,
                center = androidx.compose.ui.geometry.Offset(start + ((end - start) * flight.progress), lineY),
            )
            drawCircle(
                color = accent,
                radius = 8f,
                center = androidx.compose.ui.geometry.Offset(start + ((end - start) * flight.progress), lineY),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = flight.origin.code,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatAirportTime(flight.estimatedDeparture, flight.origin),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "EN ROUTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${(flight.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = accent,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = flight.destination.code,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatAirportTime(flight.estimatedArrival, flight.destination),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun RouteTimelinePreview() {
    AerolumeTheme {
        GlassPanel(modifier = Modifier.padding(16.dp), padding = 18.dp) {
            RouteTimeline(flight = PreviewFixtures.flight)
        }
    }
}
