package com.badeparday.aerolume.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.model.UnitSystem
import com.badeparday.aerolume.ui.preview.PreviewFixtures
import com.badeparday.aerolume.ui.theme.AerolumeTheme
import com.badeparday.aerolume.ui.util.formatAirportTime
import com.badeparday.aerolume.ui.util.formatHeading
import com.badeparday.aerolume.ui.util.formatSpeed

@Composable
fun SavedFlightCard(
    flight: Flight,
    unitSystem: UnitSystem,
    modifier: Modifier = Modifier,
) {
    GlassPanel(
        modifier = modifier,
        padding = 20.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TRACKED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = flight.flightNumber,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${flight.airline} • ${flight.aircraft.typeCode}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FlightStatusChip(status = flight.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${flight.origin.code} → ${flight.destination.code}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${formatAirportTime(flight.estimatedDeparture, flight.origin)}  •  ${formatAirportTime(flight.estimatedArrival, flight.destination)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = formatSpeed(flight.position.speedKnots, unitSystem),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            RouteTimeline(flight = flight, modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "HDG ${formatHeading(flight.position.headingDegrees)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "LIVE TRACK",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SavedFlightCardPreview() {
    AerolumeTheme {
        SavedFlightCard(
            flight = PreviewFixtures.flight,
            unitSystem = UnitSystem.Imperial,
            modifier = Modifier.padding(16.dp),
        )
    }
}
