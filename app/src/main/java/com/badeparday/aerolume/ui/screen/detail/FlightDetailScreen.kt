package com.badeparday.aerolume.ui.screen.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.ui.components.FlightHeroCard
import com.badeparday.aerolume.ui.components.GlassPanel
import com.badeparday.aerolume.ui.components.MetricPill
import com.badeparday.aerolume.ui.components.RouteTimeline
import com.badeparday.aerolume.ui.util.formatAltitude
import com.badeparday.aerolume.ui.util.formatBlockTime
import com.badeparday.aerolume.ui.util.formatHeading
import com.badeparday.aerolume.ui.util.formatSpeed

@Composable
fun FlightDetailScreen(
    uiState: FlightDetailUiState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = 16.dp,
                top = 18.dp,
                end = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 36.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GlassPanel(
                modifier =
                    Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .clickable { onBack() },
                padding = 18.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Back to radar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val flight = uiState.flight
        if (flight == null) {
            item {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 24.dp,
                ) {
                    Text(
                        text = "Loading flight details…",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        } else {
            item {
                FlightHeroCard(
                    flight = flight,
                    modifier = Modifier.fillMaxWidth(),
                    onToggleSaved = onToggleSaved,
                )
            }

            item {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2,
                ) {
                    MetricPill(label = "Altitude", value = formatAltitude(flight.position.altitudeFeet, uiState.settings.unitSystem), modifier = Modifier.fillMaxWidth(0.48f))
                    MetricPill(label = "Speed", value = formatSpeed(flight.position.speedKnots, uiState.settings.unitSystem), modifier = Modifier.fillMaxWidth(0.48f))
                    MetricPill(label = "Heading", value = formatHeading(flight.position.headingDegrees), modifier = Modifier.fillMaxWidth(0.48f))
                    MetricPill(label = "Block", value = formatBlockTime(flight), modifier = Modifier.fillMaxWidth(0.48f))
                }
            }

            item {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 18.dp,
                ) {
                    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "ROUTE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${flight.origin.name} → ${flight.destination.name}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${flight.aircraft.typeName} • ${flight.aircraft.registration} • ${flight.aircraft.callsign}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        RouteTimeline(flight = flight)
                    }
                }
            }
        }
    }
}
