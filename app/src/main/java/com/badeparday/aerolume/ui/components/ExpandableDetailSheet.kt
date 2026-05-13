package com.badeparday.aerolume.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.model.UnitSystem
import com.badeparday.aerolume.ui.util.formatAltitude
import com.badeparday.aerolume.ui.util.formatBlockTime
import com.badeparday.aerolume.ui.util.formatDelay
import com.badeparday.aerolume.ui.util.formatHeading
import com.badeparday.aerolume.ui.util.formatSpeed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandableDetailSheet(
    flight: Flight,
    unitSystem: UnitSystem,
    modifier: Modifier = Modifier,
    onToggleSaved: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FlightHeroCard(
            flight = flight,
            onToggleSaved = onToggleSaved,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2,
        ) {
            MetricPill(label = "Altitude", value = formatAltitude(flight.position.altitudeFeet, unitSystem), modifier = Modifier.fillMaxWidth(0.48f))
            MetricPill(label = "Speed", value = formatSpeed(flight.position.speedKnots, unitSystem), modifier = Modifier.fillMaxWidth(0.48f))
            MetricPill(label = "Heading", value = formatHeading(flight.position.headingDegrees), modifier = Modifier.fillMaxWidth(0.48f))
            MetricPill(label = "Block", value = formatBlockTime(flight), modifier = Modifier.fillMaxWidth(0.48f))
        }

        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            padding = 18.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "TIMELINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${flight.origin.code} to ${flight.destination.code} • ${formatDelay(flight)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${flight.aircraft.typeName} • ${flight.aircraft.registration} • ${flight.aircraft.callsign}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RouteTimeline(flight = flight)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassPanel(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable(onClick = onToggleSaved),
                        padding = 14.dp,
                    ) {
                        Text(
                            text = if (flight.saved) "REMOVE SAVE" else "SAVE FLIGHT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .padding(vertical = 2.dp),
                        )
                    }
                    GlassPanel(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable(onClick = onOpenDetails),
                        padding = 14.dp,
                    ) {
                        Text(
                            text = "OPEN FULL DETAIL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
