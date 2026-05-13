package com.badeparday.aerolume.ui.screen.radar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.badeparday.aerolume.BuildConfig
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.ui.components.ExpandableDetailSheet
import com.badeparday.aerolume.ui.components.RadarTopBar
import com.badeparday.aerolume.ui.map.FlightMapCanvas
import com.badeparday.aerolume.ui.theme.Ink900
import com.badeparday.aerolume.ui.theme.Ink950
import com.badeparday.aerolume.ui.util.springFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    uiState: RadarUiState,
    focusFlightId: String?,
    contentPadding: PaddingValues,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var selectedFlightId by rememberSaveable { mutableStateOf<String?>(focusFlightId) }
    val selectedFlight = remember(uiState.flights, selectedFlightId) {
        uiState.flights.firstOrNull { it.id == selectedFlightId }
    }
    val sheetState =
        rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false,
        )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val expanded = sheetState.currentValue == SheetValue.Expanded || sheetState.targetValue == SheetValue.Expanded
    val targetPadding =
        when {
            selectedFlight == null -> contentPadding.calculateBottomPadding() + 72.dp
            expanded -> contentPadding.calculateBottomPadding() + 304.dp
            else -> contentPadding.calculateBottomPadding() + 146.dp
        }
    val mapBottomPadding by animateDpAsState(
        targetValue = targetPadding,
        animationSpec = springFor(uiState.settings.motionIntensity),
        label = "mapBottomPadding",
    )
    val trafficSummary = "${uiState.flights.size} aircraft • world map"

    LaunchedEffect(focusFlightId) {
        if (!focusFlightId.isNullOrBlank()) {
            selectedFlightId = focusFlightId
        }
    }

    LaunchedEffect(selectedFlight?.id) {
        if (selectedFlight == null) {
            sheetState.hide()
        } else if (sheetState.currentValue == SheetValue.Hidden) {
            sheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (selectedFlight == null) 0.dp else 208.dp,
        sheetSwipeEnabled = selectedFlight != null,
        containerColor = Ink950,
        sheetContainerColor = Ink900,
        sheetContentColor = contentColorFor(Ink900),
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 0.dp,
        sheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        sheetContent = {
            if (selectedFlight != null) {
                ExpandableDetailSheet(
                    flight = selectedFlight,
                    unitSystem = uiState.settings.unitSystem,
                    onToggleSaved = { onToggleSaved(selectedFlight.id) },
                    onOpenDetails = { onOpenDetails(selectedFlight.id) },
                )
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        },
        sheetDragHandle = {
            if (selectedFlight != null) {
                Canvas(
                    modifier =
                        Modifier
                            .padding(top = 8.dp, bottom = 4.dp)
                            .size(width = 40.dp, height = 8.dp),
                ) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.12f),
                        topLeft = Offset(size.width * 0.14f, size.height * 0.40f),
                        size = Size(size.width * 0.72f, size.height * 0.20f),
                        cornerRadius = CornerRadius(999f, 999f),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            RadarSurface(
                uiState = uiState,
                selectedFlight = selectedFlight,
                bottomPadding = mapBottomPadding,
                onClearSelection = { selectedFlightId = null },
                onSelectFlight = { flight ->
                    selectedFlightId = flight.id
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            )

            RadarMapVignette()

            RadarTopBar(
                trafficSummary = trafficSummary,
                onSearchClick = onOpenSearch,
                onSettingsClick = onOpenSettings,
                modifier =
                    Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun RadarMapVignette() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.28f),
                                Color.Transparent,
                                Ink950.copy(alpha = 0.62f),
                            ),
                        ),
                ),
    )
}

@Composable
private fun RadarSurface(
    uiState: RadarUiState,
    selectedFlight: Flight?,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onClearSelection: () -> Unit,
    onSelectFlight: (Flight) -> Unit,
) {
    FlightMapCanvas(
        flights = uiState.flights,
        settings = uiState.settings,
        selectedFlight = selectedFlight,
        selectedAirport = null,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 104.dp, bottom = bottomPadding, start = 8.dp, end = 8.dp),
        onClearSelection = onClearSelection,
        onSelectFlight = onSelectFlight,
        onSelectRoute = onSelectFlight,
    )
}
