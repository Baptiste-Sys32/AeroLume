package com.badeparday.aerolume.ui.screen.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.domain.model.Airport
import com.badeparday.aerolume.domain.model.AppSettings
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.ui.map.FlightMapCanvas
import com.badeparday.aerolume.ui.theme.Ink950

@Composable
fun TabletWorkspaceMap(
    flights: List<Flight>,
    settings: AppSettings,
    selectedFlight: Flight?,
    selectedAirport: Airport?,
    leftOverlayWidth: Dp,
    modifier: Modifier = Modifier,
    onClearSelection: () -> Unit,
    onSelectFlight: (Flight) -> Unit,
    onSelectAirport: (Airport) -> Unit,
    onSelectRoute: (Flight) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        FlightMapCanvas(
            flights = flights,
            settings = settings,
            selectedFlight = selectedFlight,
            selectedAirport = selectedAirport,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = leftOverlayWidth + 28.dp, top = 24.dp, end = 88.dp, bottom = 24.dp),
            showAirports = true,
            onClearSelection = onClearSelection,
            onSelectFlight = onSelectFlight,
            onSelectAirport = onSelectAirport,
            onSelectRoute = onSelectRoute,
        )

        WorkspaceMapVignette(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun WorkspaceMapVignette(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier.background(
                brush =
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.24f),
                            Color.Transparent,
                            Ink950.copy(alpha = 0.52f),
                        ),
                    ),
            ),
    )
    Box(
        modifier =
            modifier.background(
                brush =
                    Brush.horizontalGradient(
                        listOf(
                            Ink950.copy(alpha = 0.22f),
                            Color.Transparent,
                            Ink950.copy(alpha = 0.16f),
                        ),
                    ),
            ),
    )
}
