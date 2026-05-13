package com.badeparday.aerolume.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badeparday.aerolume.data.AppContainer
import com.badeparday.aerolume.domain.model.Airport
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.model.MapAppearance
import com.badeparday.aerolume.ui.adaptive.AerolumeWindowState
import com.badeparday.aerolume.ui.components.FlightStatusChip
import com.badeparday.aerolume.ui.components.FloatingInspectorPanel
import com.badeparday.aerolume.ui.components.GlassPanel
import com.badeparday.aerolume.ui.components.MapToolRail
import com.badeparday.aerolume.ui.components.MetricPill
import com.badeparday.aerolume.ui.components.RouteTimeline
import com.badeparday.aerolume.ui.components.ToolRailMode
import com.badeparday.aerolume.ui.screen.radar.RadarUiState
import com.badeparday.aerolume.ui.screen.radar.RadarViewModel
import com.badeparday.aerolume.ui.screen.saved.SavedFlightsUiState
import com.badeparday.aerolume.ui.screen.saved.SavedFlightsViewModel
import com.badeparday.aerolume.ui.screen.search.SearchUiState
import com.badeparday.aerolume.ui.screen.search.SearchViewModel
import com.badeparday.aerolume.ui.screen.settings.SettingsUiState
import com.badeparday.aerolume.ui.screen.settings.SettingsViewModel
import com.badeparday.aerolume.ui.screen.workspace.TabletWorkspaceMap
import com.badeparday.aerolume.ui.theme.AerolumeThemeTokens
import com.badeparday.aerolume.ui.theme.Ink950
import com.badeparday.aerolume.ui.util.formatAirportDay
import com.badeparday.aerolume.ui.util.formatAirportTime
import com.badeparday.aerolume.ui.util.formatAltitude
import com.badeparday.aerolume.ui.util.formatBlockTime
import com.badeparday.aerolume.ui.util.formatDelay
import com.badeparday.aerolume.ui.util.formatHeading
import com.badeparday.aerolume.ui.util.formatRelativeProgress
import com.badeparday.aerolume.ui.util.formatSpeed
import com.badeparday.aerolume.ui.util.statusLabel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private enum class WorkspacePane {
    Overview,
    Flight,
    Airport,
    Route,
    Search,
    Saved,
    Settings,
}

@Composable
fun TabletWorkspaceRoot(
    container: AppContainer,
    windowState: AerolumeWindowState,
) {
    val radarViewModel: RadarViewModel = viewModel(factory = RadarViewModel.factory(container))
    val radarState by radarViewModel.uiState.collectAsStateWithLifecycle()
    val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModel.factory(container))
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val savedViewModel: SavedFlightsViewModel = viewModel(factory = SavedFlightsViewModel.factory(container))
    val savedState by savedViewModel.uiState.collectAsStateWithLifecycle()
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    var pane by rememberSaveable { mutableStateOf(WorkspacePane.Overview) }
    var selectedFlightId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedAirportCode by rememberSaveable { mutableStateOf<String?>(null) }

    val airports = remember(radarState.flights) {
        radarState.flights
            .flatMap { listOf(it.origin, it.destination) }
            .distinctBy { it.code }
            .sortedBy { it.code }
    }
    val selectedFlight = remember(radarState.flights, selectedFlightId) {
        radarState.flights.firstOrNull { it.id == selectedFlightId }
    }
    val selectedAirport = remember(airports, selectedAirportCode) {
        airports.firstOrNull { it.code == selectedAirportCode }
    }
    val resolvedPane =
        when {
            selectedFlight == null && (pane == WorkspacePane.Flight || pane == WorkspacePane.Route) -> WorkspacePane.Overview
            selectedAirport == null && pane == WorkspacePane.Airport -> WorkspacePane.Overview
            else -> pane
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Ink950),
    ) {
        TabletWorkspaceMap(
            flights = radarState.flights,
            settings = radarState.settings,
            selectedFlight = selectedFlight,
            selectedAirport = selectedAirport,
            leftOverlayWidth = windowState.inspectorWidth,
            modifier = Modifier.fillMaxSize(),
            onClearSelection = {
                if (resolvedPane == WorkspacePane.Flight || resolvedPane == WorkspacePane.Airport || resolvedPane == WorkspacePane.Route) {
                    selectedFlightId = null
                    selectedAirportCode = null
                    pane = WorkspacePane.Overview
                }
            },
            onSelectFlight = { flight ->
                selectedFlightId = flight.id
                selectedAirportCode = null
                pane = WorkspacePane.Flight
            },
            onSelectAirport = { airport ->
                selectedAirportCode = airport.code
                pane = WorkspacePane.Airport
            },
            onSelectRoute = { flight ->
                selectedFlightId = flight.id
                selectedAirportCode = null
                pane = WorkspacePane.Route
            },
        )

        FloatingInspectorPanel(
            title = inspectorTitle(pane = resolvedPane, flight = selectedFlight, airport = selectedAirport),
            subtitle = inspectorSubtitle(pane = resolvedPane),
            width = windowState.inspectorWidth,
            modifier =
                Modifier
                    .statusBarsPadding()
                    .padding(start = 20.dp, top = 14.dp, bottom = 20.dp)
                    .fillMaxHeight(0.92f),
            actions = {
                if (selectedFlight != null && (resolvedPane == WorkspacePane.Flight || resolvedPane == WorkspacePane.Route)) {
                    CompactInspectorAction(
                        label = if (selectedFlight.saved) "UNSAVE" else "SAVE",
                        onClick = { radarViewModel.toggleSaved(selectedFlight.id) },
                    )
                }
            },
        ) {
            when (resolvedPane) {
                WorkspacePane.Overview ->
                    OverviewInspector(
                        radarState = radarState,
                        airports = airports,
                        onSelectFlight = {
                            selectedFlightId = it.id
                            pane = WorkspacePane.Flight
                        },
                        onSelectAirport = {
                            selectedAirportCode = it.code
                            pane = WorkspacePane.Airport
                        },
                    )

                WorkspacePane.Flight ->
                    selectedFlight?.let { flight ->
                        FlightInspector(
                            flight = flight,
                            radarState = radarState,
                            onToggleSaved = { radarViewModel.toggleSaved(flight.id) },
                            onOpenAirport = { airport ->
                                selectedAirportCode = airport.code
                                pane = WorkspacePane.Airport
                            },
                            onOpenRoute = { pane = WorkspacePane.Route },
                        )
                    }

                WorkspacePane.Airport ->
                    selectedAirport?.let { airport ->
                        AirportInspector(
                            airport = airport,
                            flights = radarState.flights,
                            onOpenFlight = { flight ->
                                selectedFlightId = flight.id
                                pane = WorkspacePane.Flight
                            },
                        )
                    }

                WorkspacePane.Route ->
                    selectedFlight?.let { flight ->
                        RouteInspector(
                            flight = flight,
                            onOpenAirport = { airport ->
                                selectedAirportCode = airport.code
                                pane = WorkspacePane.Airport
                            },
                        )
                    }

                WorkspacePane.Search ->
                    SearchInspector(
                        uiState = searchState,
                        onQueryChange = searchViewModel::onQueryChanged,
                        onUseRecent = searchViewModel::useRecentQuery,
                        onTrackFlight = { flight ->
                            searchViewModel.rememberSelection(flight.id)
                            selectedFlightId = flight.id
                            pane = WorkspacePane.Flight
                        },
                    )

                WorkspacePane.Saved ->
                    SavedInspector(
                        uiState = savedState,
                        onOpenFlight = { flight ->
                            selectedFlightId = flight.id
                            pane = WorkspacePane.Flight
                        },
                        onToggleSaved = savedViewModel::toggleSaved,
                    )

                WorkspacePane.Settings ->
                    SettingsInspector(
                        uiState = settingsState,
                        onSetDemoMode = settingsViewModel::setDemoMode,
                        onSetMotionIntensity = settingsViewModel::setMotionIntensity,
                        onSetUnitSystem = settingsViewModel::setUnitSystem,
                        onSetMapAppearance = settingsViewModel::setMapAppearance,
                    )
            }
        }

        MapToolRail(
            selectedMode =
                when (resolvedPane) {
                    WorkspacePane.Search -> ToolRailMode.Search
                    WorkspacePane.Saved -> ToolRailMode.Saved
                    WorkspacePane.Settings -> ToolRailMode.Settings
                    else -> ToolRailMode.Radar
                },
            hybridEnabled = radarState.settings.mapAppearance == MapAppearance.NightHybrid,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(end = windowState.toolRailInset, top = 20.dp, bottom = 20.dp),
            onShowRadar = {
                selectedFlightId = null
                selectedAirportCode = null
                pane = WorkspacePane.Overview
            },
            onShowSearch = { pane = WorkspacePane.Search },
            onShowSaved = { pane = WorkspacePane.Saved },
            onToggleLayers = {
                settingsViewModel.setMapAppearance(
                    if (radarState.settings.mapAppearance == MapAppearance.NightVector) {
                        MapAppearance.NightHybrid
                    } else {
                        MapAppearance.NightVector
                    },
                )
            },
            onShowSettings = { pane = WorkspacePane.Settings },
        )
    }
}

@Composable
private fun OverviewInspector(
    radarState: RadarUiState,
    airports: List<Airport>,
    onSelectFlight: (Flight) -> Unit,
    onSelectAirport: (Airport) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricPill(label = "Aircraft", value = radarState.flights.size.toString(), modifier = Modifier.weight(1f))
            MetricPill(
                label = "Saved",
                value = radarState.flights.count { it.saved }.toString(),
                modifier = Modifier.weight(1f),
            )
            MetricPill(label = "Airports", value = airports.size.toString(), modifier = Modifier.weight(1f))
        }

        InspectorModule(title = "Tracked now") {
            radarState.flights.sortedBy { it.flightNumber }.take(5).forEach { flight ->
                InspectorFlightRow(
                    flight = flight,
                    onClick = { onSelectFlight(flight) },
                )
            }
        }

        InspectorModule(title = "Airport watch") {
            airports.take(6).forEach { airport ->
                InspectorAirportRow(
                    airport = airport,
                    flights = radarState.flights,
                    onClick = { onSelectAirport(airport) },
                )
            }
        }
    }
}

@Composable
private fun FlightInspector(
    flight: Flight,
    radarState: RadarUiState,
    onToggleSaved: () -> Unit,
    onOpenAirport: (Airport) -> Unit,
    onOpenRoute: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = flight.flightNumber,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${flight.airline} • ${flight.aircraft.typeCode} • ${flight.aircraft.registration}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FlightStatusChip(status = flight.status)
        }

        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            padding = 14.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = flight.origin.code,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = flight.origin.city,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "DIRECT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = flight.destination.code,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = flight.destination.city,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "DEPARTURE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatAirportTime(flight.estimatedDeparture, flight.origin),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "ARRIVAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatAirportTime(flight.estimatedArrival, flight.destination),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                RouteTimeline(flight = flight)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricPill(
                label = "Altitude",
                value = formatAltitude(flight.position.altitudeFeet, radarState.settings.unitSystem),
                modifier = Modifier.weight(1f),
            )
            MetricPill(
                label = "Speed",
                value = formatSpeed(flight.position.speedKnots, radarState.settings.unitSystem),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricPill(label = "Heading", value = formatHeading(flight.position.headingDegrees), modifier = Modifier.weight(1f))
            MetricPill(label = "Delay", value = formatDelay(flight), modifier = Modifier.weight(1f))
        }

        InspectorModule(title = "Operation") {
            InspectorKeyValue("Status", statusLabel(flight.status))
            InspectorKeyValue("Block time", formatBlockTime(flight))
            InspectorKeyValue("Callsign", flight.aircraft.callsign)
            InspectorKeyValue("Aircraft", flight.aircraft.typeName)
        }

        InspectorModule(title = "Airports") {
            InspectorAirportLink(label = "Origin", airport = flight.origin, onClick = { onOpenAirport(flight.origin) })
            InspectorAirportLink(label = "Destination", airport = flight.destination, onClick = { onOpenAirport(flight.destination) })
            CompactInspectorAction(
                label = "ROUTE PROFILE",
                onClick = onOpenRoute,
            )
            CompactInspectorAction(
                label = if (flight.saved) "REMOVE SAVE" else "SAVE FLIGHT",
                onClick = onToggleSaved,
            )
        }
    }
}

@Composable
private fun AirportInspector(
    airport: Airport,
    flights: List<Flight>,
    onOpenFlight: (Flight) -> Unit,
) {
    val snapshot = remember(airport, flights) { buildAirportSnapshot(airport, flights) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            padding = 14.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = airport.code,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = airport.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${airport.city} • ${formatAirportDay(snapshot.referenceFlightTime, airport)} • ${formatAirportTime(snapshot.referenceFlightTime, airport)} local",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AirportSummaryStat(label = "On time", value = "${snapshot.onTimeScore}%", modifier = Modifier.weight(1f))
                    AirportSummaryStat(label = "Active", value = snapshot.activeFlights.toString(), modifier = Modifier.weight(1f))
                    AirportSummaryStat(label = "Peak", value = "${snapshot.peakDepartureRate}/hr", modifier = Modifier.weight(1f))
                }
            }
        }

        InspectorModule(title = "Operational status") {
            Text(
                text = snapshot.statusNote.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${snapshot.activeFlights} active movements • ${snapshot.arrivals.size} arrivals tracked in the current mock set",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AirportStatusGrid(
                cells =
                    listOf(
                        "Flow" to snapshot.flowLabel,
                        "Runway mode" to snapshot.runwayMode,
                        "Surface wind" to snapshot.windSummary,
                        "Terminal load" to "${snapshot.terminalLoad}%",
                    ),
            )
        }

        InspectorModule(title = "Departures per hour") {
            AirportDeparturesChart(bins = snapshot.departuresPerHour)
        }

        InspectorModule(title = "Departures list") {
            snapshot.departures.forEach { flight ->
                AirportDepartureRow(
                    airport = airport,
                    flight = flight,
                    onClick = { onOpenFlight(flight) },
                )
            }
            if (snapshot.departures.isEmpty()) {
                EmptyModuleText("No mock departures in the current radar set.")
            }
        }

        InspectorModule(title = "Performance summary") {
            AirportPerformanceGrid(metrics = snapshot.performanceSummary)
        }
    }
}

@Composable
private fun AirportSummaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = AerolumeThemeTokens.colors
    GlassPanel(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        padding = 10.dp,
        containerColor = colors.panelMuted,
        borderColor = colors.border.copy(alpha = 0.9f),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AirportStatusGrid(
    cells: List<Pair<String, String>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.chunked(2).forEach { rowCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowCells.forEach { (label, value) ->
                    AirportDataCell(
                        label = label,
                        value = value,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowCells.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AirportDataCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = AerolumeThemeTokens.colors
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(10.dp))
                .background(colors.panelMuted)
                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AirportDeparturesChart(
    bins: List<AirportTrafficBin>,
) {
    val colors = AerolumeThemeTokens.colors
    val maxValue = bins.maxOfOrNull { it.departures }?.coerceAtLeast(1) ?: 1

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "LOCAL DEPARTURES WINDOW",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bins.forEach { bin ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = bin.departures.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (bin.highlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(78.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.panelMuted)
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                .padding(4.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight((bin.departures.toFloat() / maxValue.toFloat()).coerceIn(0.14f, 1f))
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (bin.highlighted) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.62f)
                                        } else {
                                            Color.White.copy(alpha = 0.14f)
                                        },
                                    ),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (bin.highlighted) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                        Text(
                            text = bin.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AirportDepartureRow(
    airport: Airport,
    flight: Flight,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.width(72.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = formatAirportTime(flight.estimatedDeparture, airport),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = formatDelay(flight),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "${flight.flightNumber} • ${flight.destination.code}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${flight.airline} • ${flight.destination.city}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = statusLabel(flight.status).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AirportPerformanceGrid(
    metrics: List<AirportPerformanceMetric>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowMetrics.forEach { metric ->
                    AirportDataCell(
                        label = metric.label,
                        value = metric.value,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowMetrics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RouteInspector(
    flight: Flight,
    onOpenAirport: (Airport) -> Unit,
) {
    val waypoints = flight.route.mapNotNull { it.label }.distinct()
    val totalDistanceKm = remember(flight.route) { estimateRouteDistanceKilometers(flight.route) }
    val completedDistanceKm = (totalDistanceKm * flight.progress).roundToInt()
    val remainingDistanceKm = (totalDistanceKm - completedDistanceKm).coerceAtLeast(0)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            padding = 14.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${flight.origin.code} → ${flight.destination.code}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${formatRelativeProgress(flight.progress)} complete • ${completedDistanceKm} km flown • ${remainingDistanceKm} km remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RouteTimeline(flight = flight)
            }
        }

        InspectorModule(title = "Route profile") {
            InspectorKeyValue("Block time", formatBlockTime(flight))
            InspectorKeyValue("Current phase", statusLabel(flight.status))
            InspectorKeyValue("Heading", formatHeading(flight.position.headingDegrees))
            InspectorKeyValue("Cruise track", "${flight.position.speedKnots} kt")
        }

        InspectorModule(title = "Waypoints") {
            waypoints.forEach { label ->
                InspectorKeyValue(label, "Crossing")
            }
        }

        InspectorModule(title = "Airport pair") {
            InspectorAirportLink(label = "Origin", airport = flight.origin, onClick = { onOpenAirport(flight.origin) })
            InspectorAirportLink(label = "Destination", airport = flight.destination, onClick = { onOpenAirport(flight.destination) })
        }
    }
}

@Composable
private fun SearchInspector(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onUseRecent: (String) -> Unit,
    onTrackFlight: (Flight) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TabletSearchField(
            value = uiState.query,
            onValueChange = onQueryChange,
        )

        if (uiState.recentQueries.isNotEmpty()) {
            InspectorModule(title = "Recent") {
                uiState.recentQueries.forEach { query ->
                    CompactInspectorAction(
                        label = query,
                        onClick = { onUseRecent(query) },
                    )
                }
            }
        }

        InspectorModule(title = if (uiState.query.isBlank()) "Highlights" else "Results") {
            uiState.results.forEach { flight ->
                InspectorFlightRow(
                    flight = flight,
                    onClick = { onTrackFlight(flight) },
                )
            }
        }
    }
}

@Composable
private fun SavedInspector(
    uiState: SavedFlightsUiState,
    onOpenFlight: (Flight) -> Unit,
    onToggleSaved: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiState.flights.isEmpty()) {
            InspectorModule(title = "Tracked list") {
                EmptyModuleText("No saved flights yet. Save aircraft from the map to pin them here.")
            }
        } else {
            uiState.flights.forEach { flight ->
                InspectorModule(title = flight.flightNumber) {
                    InspectorFlightRow(
                        flight = flight,
                        onClick = { onOpenFlight(flight) },
                    )
                    CompactInspectorAction(
                        label = "REMOVE SAVE",
                        onClick = { onToggleSaved(flight.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsInspector(
    uiState: SettingsUiState,
    onSetDemoMode: (Boolean) -> Unit,
    onSetMotionIntensity: (com.badeparday.aerolume.domain.model.MotionIntensity) -> Unit,
    onSetUnitSystem: (com.badeparday.aerolume.domain.model.UnitSystem) -> Unit,
    onSetMapAppearance: (MapAppearance) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InspectorModule(title = "Workspace") {
            CompactInspectorAction(
                label = if (uiState.settings.demoMode) "DEMO MODE ON" else "DEMO MODE OFF",
                onClick = { onSetDemoMode(!uiState.settings.demoMode) },
            )
            CompactInspectorAction(
                label = "MOTION ${uiState.settings.motionIntensity.name.uppercase()}",
                onClick = {
                    val next =
                        when (uiState.settings.motionIntensity) {
                            com.badeparday.aerolume.domain.model.MotionIntensity.Reduced -> com.badeparday.aerolume.domain.model.MotionIntensity.Standard
                            com.badeparday.aerolume.domain.model.MotionIntensity.Standard -> com.badeparday.aerolume.domain.model.MotionIntensity.Expressive
                            com.badeparday.aerolume.domain.model.MotionIntensity.Expressive -> com.badeparday.aerolume.domain.model.MotionIntensity.Reduced
                        }
                    onSetMotionIntensity(next)
                },
            )
            CompactInspectorAction(
                label = "UNITS ${uiState.settings.unitSystem.name.uppercase()}",
                onClick = {
                    val next =
                        when (uiState.settings.unitSystem) {
                            com.badeparday.aerolume.domain.model.UnitSystem.Metric -> com.badeparday.aerolume.domain.model.UnitSystem.Imperial
                            com.badeparday.aerolume.domain.model.UnitSystem.Imperial -> com.badeparday.aerolume.domain.model.UnitSystem.Metric
                        }
                    onSetUnitSystem(next)
                },
            )
            CompactInspectorAction(
                label = "MAP ${uiState.settings.mapAppearance.name.uppercase()}",
                onClick = {
                    onSetMapAppearance(
                        if (uiState.settings.mapAppearance == MapAppearance.NightVector) {
                            MapAppearance.NightHybrid
                        } else {
                            MapAppearance.NightVector
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun InspectorModule(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        padding = 14.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        })
    }
}

@Composable
private fun InspectorFlightRow(
    flight: Flight,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = flight.flightNumber,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${flight.origin.code} → ${flight.destination.code} • ${flight.airline}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FlightStatusChip(status = flight.status)
    }
}

@Composable
private fun InspectorAirportRow(
    airport: Airport,
    flights: List<Flight>,
    onClick: () -> Unit,
) {
    val departures = remember(airport, flights) { flights.count { it.origin.code == airport.code } }
    val arrivals = remember(airport, flights) { flights.count { it.destination.code == airport.code } }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = airport.code,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${airport.city} • $departures dep / $arrivals arr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        androidx.compose.material3.Icon(
            imageVector = Icons.Rounded.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InspectorAirportLink(
    label: String,
    airport: Airport,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${airport.code} • ${airport.name}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = airport.city,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InspectorKeyValue(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CompactInspectorAction(
    label: String,
    onClick: () -> Unit,
) {
    GlassPanel(
        modifier =
            Modifier
                .wrapContentHeight()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        padding = 10.dp,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TabletSearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        padding = 14.dp,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions =
                androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                ),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isBlank()) {
                        Text(
                            text = "AF011, CDG, JFK, Paris to New York",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun EmptyModuleText(
    value: String,
) {
    Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private data class AirportSnapshot(
    val onTimeScore: Int,
    val activeFlights: Int,
    val flowLabel: String,
    val runwayMode: String,
    val terminalLoad: Int,
    val windSummary: String,
    val statusNote: String,
    val peakDepartureRate: Int,
    val departuresPerHour: List<AirportTrafficBin>,
    val performanceSummary: List<AirportPerformanceMetric>,
    val departures: List<Flight>,
    val arrivals: List<Flight>,
    val referenceFlightTime: java.time.Instant,
)

private data class AirportTrafficBin(
    val label: String,
    val departures: Int,
    val highlighted: Boolean,
)

private data class AirportPerformanceMetric(
    val label: String,
    val value: String,
)

private fun buildAirportSnapshot(
    airport: Airport,
    flights: List<Flight>,
): AirportSnapshot {
    val departures = flights.filter { it.origin.code == airport.code }.sortedBy { it.estimatedDeparture }.take(5)
    val arrivals = flights.filter { it.destination.code == airport.code }.sortedBy { it.estimatedArrival }.take(5)
    val delayedCount = (departures + arrivals).count { formatDelay(it) != "On time" }
    val scoreBase = 82 + ((airport.code.hashCode().absoluteValue + flights.size) % 13)
    val terminalLoad = 54 + ((airport.city.hashCode().absoluteValue + delayedCount) % 34)
    val runwayMode = listOf("27/09 west flow", "18/36 mixed", "north departures", "single arrival bank")[airport.code.hashCode().absoluteValue % 4]
    val windDirection = 90 + (airport.code.hashCode().absoluteValue % 180)
    val windSpeed = 6 + (airport.city.hashCode().absoluteValue % 22)
    val flowLabel =
        when {
            delayedCount >= 3 -> "Flow constrained"
            delayedCount >= 1 -> "Minor holding"
            else -> "Normal flow"
        }
    val referenceTime =
        (departures.firstOrNull()?.estimatedDeparture ?: arrivals.firstOrNull()?.estimatedArrival)
            ?: java.time.Instant.now()
    val zoneId = ZoneId.of(airport.timeZoneId)
    val localReferenceHour = referenceTime.atZone(zoneId).truncatedTo(ChronoUnit.HOURS)
    val departureFormatter = DateTimeFormatter.ofPattern("HH")
    val departuresPerHour =
        (-1L..4L).mapIndexed { index, offset ->
            val bucketTime = localReferenceHour.plusHours(offset)
            val actualCount =
                departures.count {
                    it.estimatedDeparture.atZone(zoneId).truncatedTo(ChronoUnit.HOURS) == bucketTime
                }
            val baseline = 2 + ((airport.code.hashCode().absoluteValue + (index * 7)) % 5)
            val count = maxOf(actualCount, baseline + if (offset == 0L || offset == 1L) departures.size / 2 else 0)
            AirportTrafficBin(
                label = bucketTime.format(departureFormatter),
                departures = count,
                highlighted = offset == 0L,
            )
        }
    val peakDepartureRate = departuresPerHour.maxOfOrNull { it.departures } ?: (departures.size + 2)
    val taxiOutMinutes = 9 + ((airport.code.hashCode().absoluteValue + delayedCount) % 14)
    val runwayUtilization = 61 + ((airport.name.hashCode().absoluteValue + flights.size) % 27)
    val gateTurns = 34 + ((airport.city.hashCode().absoluteValue + departures.size) % 28)
    val statusNote =
        when {
            delayedCount >= 3 -> "Departure sequencing active"
            delayedCount >= 1 -> "Minor queue on the active bank"
            else -> "Departure bank stable"
        }
    val performanceSummary =
        listOf(
            AirportPerformanceMetric(label = "Peak dep", value = "${peakDepartureRate}/hr"),
            AirportPerformanceMetric(label = "Taxi-out", value = "${taxiOutMinutes} min"),
            AirportPerformanceMetric(label = "Runway use", value = "${runwayUtilization}%"),
            AirportPerformanceMetric(label = "Gate turns", value = "${gateTurns} min"),
        )
    return AirportSnapshot(
        onTimeScore = scoreBase,
        activeFlights = departures.size + arrivals.size,
        flowLabel = flowLabel,
        runwayMode = runwayMode,
        terminalLoad = terminalLoad,
        windSummary = "${windDirection}° / ${windSpeed} kt",
        statusNote = statusNote,
        peakDepartureRate = peakDepartureRate,
        departuresPerHour = departuresPerHour,
        performanceSummary = performanceSummary,
        departures = departures,
        arrivals = arrivals,
        referenceFlightTime = referenceTime,
    )
}

private fun estimateRouteDistanceKilometers(route: List<com.badeparday.aerolume.domain.model.RoutePoint>): Int {
    if (route.size < 2) return 0
    var total = 0.0
    route.zipWithNext().forEach { (start, end) ->
        val startLat = Math.toRadians(start.latitude)
        val endLat = Math.toRadians(end.latitude)
        val deltaLat = Math.toRadians(end.latitude - start.latitude)
        val deltaLon = Math.toRadians(end.longitude - start.longitude)
        val a =
            (kotlin.math.sin(deltaLat / 2) * kotlin.math.sin(deltaLat / 2)) +
                kotlin.math.cos(startLat) * kotlin.math.cos(endLat) *
                (kotlin.math.sin(deltaLon / 2) * kotlin.math.sin(deltaLon / 2))
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        total += 6_371.0 * c
    }
    return total.roundToInt()
}

private fun inspectorTitle(
    pane: WorkspacePane,
    flight: Flight?,
    airport: Airport?,
): String =
    when (pane) {
        WorkspacePane.Overview -> "Radar Workspace"
        WorkspacePane.Flight -> flight?.flightNumber ?: "Flight"
        WorkspacePane.Airport -> airport?.code ?: "Airport"
        WorkspacePane.Route -> flight?.let { "${it.origin.code} to ${it.destination.code}" } ?: "Route"
        WorkspacePane.Search -> "Search"
        WorkspacePane.Saved -> "Saved Flights"
        WorkspacePane.Settings -> "Workspace"
    }

private fun inspectorSubtitle(
    pane: WorkspacePane,
): String =
    when (pane) {
        WorkspacePane.Overview -> "Live airspace"
        WorkspacePane.Flight -> "Flight detail"
        WorkspacePane.Airport -> "Airport detail"
        WorkspacePane.Route -> "Route profile"
        WorkspacePane.Search -> "Track flights"
        WorkspacePane.Saved -> "Tracked list"
        WorkspacePane.Settings -> "Preferences"
    }
