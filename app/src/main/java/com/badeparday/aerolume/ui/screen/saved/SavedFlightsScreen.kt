package com.badeparday.aerolume.ui.screen.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.ui.components.GlassPanel
import com.badeparday.aerolume.ui.components.SavedFlightCard

@Composable
fun SavedFlightsScreen(
    uiState: SavedFlightsUiState,
    contentPadding: PaddingValues,
    onOpenFlight: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = 16.dp,
                top = 18.dp,
                end = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 108.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GlassPanel(
                modifier =
                    Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                padding = 20.dp,
            ) {
                androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SAVED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Tracked flights, quietly live",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        if (uiState.flights.isEmpty()) {
            item {
                EmptySavedState()
            }
        } else {
            items(
                items = uiState.flights,
                key = Flight::id,
            ) { flight ->
                val dismissState =
                    rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                onToggleSaved(flight.id)
                            }
                            true
                        },
                    )
                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundContent = {
                        GlassPanel(
                            modifier = Modifier.fillMaxWidth(),
                            padding = 18.dp,
                        ) {
                            Text(
                                text = "Remove from saved",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 34.dp),
                            )
                        }
                    },
                ) {
                    SavedFlightCard(
                        flight = flight,
                        unitSystem = uiState.settings.unitSystem,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOpenFlight(flight.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySavedState() {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        padding = 24.dp,
    ) {
        androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "No tracked flights yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Save a few aircraft from Radar or Search to build the premium tracking view.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
