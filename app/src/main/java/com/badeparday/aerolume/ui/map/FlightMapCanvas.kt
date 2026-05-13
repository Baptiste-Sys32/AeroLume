package com.badeparday.aerolume.ui.map

import android.content.Context
import android.graphics.PointF
import android.os.Bundle
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.badeparday.aerolume.domain.model.Airport
import com.badeparday.aerolume.domain.model.AppSettings
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.model.MapAppearance
import com.badeparday.aerolume.domain.model.MotionIntensity
import com.badeparday.aerolume.ui.util.accentHexString
import com.badeparday.aerolume.ui.util.createAircraftMarkerBitmap
import com.badeparday.aerolume.ui.util.densifyRouteForMap
import com.badeparday.aerolume.ui.util.toLatLng
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdate
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.eq
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.backgroundColor
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillExtrusionColor
import org.maplibre.android.style.layers.PropertyFactory.fillExtrusionOpacity
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconOpacity
import org.maplibre.android.style.layers.PropertyFactory.iconRotate
import org.maplibre.android.style.layers.PropertyFactory.iconRotationAlignment
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.rasterOpacity
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloWidth
import org.maplibre.android.style.layers.PropertyFactory.textOffset
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.PropertyFactory.visibility
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val OpenFreeMapStyleUrl = "https://tiles.openfreemap.org/styles/liberty"
private const val FlightsSourceId = "aerolume-flights"
private const val RoutesSourceId = "aerolume-routes"
private const val BreadcrumbSourceId = "aerolume-breadcrumb"
private const val AirportsSourceId = "aerolume-airports"
private const val AirportContextSourceId = "aerolume-airport-context"
private const val AircraftMarkerDefaultImageId = "aerolume-aircraft-default"
private const val AircraftMarkerSelectedImageId = "aerolume-aircraft-selected"
private const val RouteLayerId = "aerolume-route-default"
private const val RouteSelectedBaseLayerId = "aerolume-route-selected-base"
private const val RouteSelectedLayerId = "aerolume-route-selected"
private const val BreadcrumbLayerId = "aerolume-breadcrumb-layer"
private const val AirportLayerId = "aerolume-airport-default"
private const val AirportSelectedLayerId = "aerolume-airport-selected"
private const val AirportContextGuideLayerId = "aerolume-airport-context-guide"
private const val AirportContextRingLayerId = "aerolume-airport-context-ring"
private const val AircraftHaloLayerId = "aerolume-aircraft-halo"
private const val AircraftLayerId = "aerolume-aircraft-default-layer"
private const val AircraftSelectedLayerId = "aerolume-aircraft-selected-layer"
private const val AircraftSelectedLabelLayerId = "aerolume-aircraft-selected-label"
private const val GridSourceId = "aerolume-grid"
private const val GridMinorLayerId = "aerolume-grid-minor"
private const val GridMajorLayerId = "aerolume-grid-major"

private val HiddenBaseLayerIds =
    listOf(
        "park",
        "park_outline",
        "landuse_residential",
        "landcover_wood",
        "landcover_grass",
        "landcover_ice",
        "landcover_wetland",
        "landuse_pitch",
        "landuse_track",
        "landuse_cemetery",
        "landuse_hospital",
        "landuse_school",
        "landcover_sand",
        "road_area_pattern",
        "waterway_line_label",
        "water_name_point_label",
        "water_name_line_label",
        "airport",
    )

private val HiddenBasePrefixes =
    listOf(
        "road_",
        "tunnel_",
        "bridge_",
        "poi_",
        "highway-name",
        "highway-shield",
        "road_shield",
        "road_one_way",
    )

@Composable
fun FlightMapCanvas(
    flights: List<Flight>,
    settings: AppSettings,
    selectedFlight: Flight?,
    selectedAirport: Airport?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showAirports: Boolean = false,
    onClearSelection: () -> Unit,
    onSelectFlight: (Flight) -> Unit,
    onSelectAirport: (Airport) -> Unit = {},
    onSelectRoute: (Flight) -> Unit = {},
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val currentSelectedFlightState = rememberUpdatedState(selectedFlight)
    val currentFlightsState = rememberUpdatedState(flights)
    val currentOnClearSelectionState = rememberUpdatedState(onClearSelection)
    val currentOnSelectFlightState = rememberUpdatedState(onSelectFlight)
    val currentOnSelectAirportState = rememberUpdatedState(onSelectAirport)
    val currentOnSelectRouteState = rememberUpdatedState(onSelectRoute)
    val mapView = rememberMapViewWithLifecycle()
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var previousFocusKey by remember { mutableStateOf<String?>(null) }
    val focusKey =
        when {
            selectedFlight != null -> "flight:${selectedFlight.id}"
            selectedAirport != null -> "airport:${selectedAirport.code}"
            else -> "overview"
        }
    val renderedFlights = flights.map { rememberRenderedFlight(flight = it, motionIntensity = settings.motionIntensity) }
    val overviewCount = if (focusKey == "overview") flights.size else 0
    val paddingPx =
        with(density) {
            MapPaddingPx(
                left = contentPadding.calculateStartPadding(layoutDirection).toPx().toInt(),
                top = contentPadding.calculateTopPadding().toPx().toInt(),
                right = contentPadding.calculateEndPadding(layoutDirection).toPx().toInt(),
                bottom = contentPadding.calculateBottomPadding().toPx().toInt(),
            )
        }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    mapLibreMap = map
                    map.uiSettings.apply {
                        isCompassEnabled = false
                        isRotateGesturesEnabled = true
                        isTiltGesturesEnabled = true
                        isLogoEnabled = true
                        isAttributionEnabled = true
                    }
                    map.setStyle(Style.Builder().fromUri(OpenFreeMapStyleUrl)) { style ->
                        configureBaseStyle(
                            style = style,
                            mapAppearance = settings.mapAppearance,
                            airportFocus = selectedAirport != null,
                        )
                        ensureFlightLayers(style, context)
                        updateFlightSources(
                            style = style,
                            flights = renderedFlights,
                            selectedFlight = selectedFlight,
                            selectedAirport = selectedAirport,
                            showAirports = showAirports,
                        )
                    }
                    map.addOnMapClickListener { latLng ->
                        handleMapClick(
                            map = map,
                            latLng = latLng,
                            flights = currentFlightsState.value,
                            selectedFlight = currentSelectedFlightState.value,
                            onClearSelection = currentOnClearSelectionState.value,
                            onSelectFlight = currentOnSelectFlightState.value,
                            onSelectAirport = currentOnSelectAirportState.value,
                            onSelectRoute = currentOnSelectRouteState.value,
                        )
                    }
                }
            }
        },
        update = {
            mapLibreMap?.style?.let { style ->
                configureBaseStyle(
                    style = style,
                    mapAppearance = settings.mapAppearance,
                    airportFocus = selectedAirport != null,
                )
                ensureFlightLayers(style, context)
                updateFlightSources(
                    style = style,
                    flights = renderedFlights,
                    selectedFlight = selectedFlight,
                    selectedAirport = selectedAirport,
                    showAirports = showAirports,
                )
            }
        },
    )

    LaunchedEffect(mapLibreMap, focusKey, overviewCount, paddingPx, settings.motionIntensity) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val lastFocusKey = previousFocusKey
        map.setPadding(
            paddingPx.left,
            paddingPx.top,
            paddingPx.right,
            paddingPx.bottom,
        )
        animateCameraToFocus(
            map = map,
            flights = flights,
            selectedFlight = selectedFlight,
            selectedAirport = selectedAirport,
            padding = paddingPx,
            motionIntensity = settings.motionIntensity,
            previousFocusKey = lastFocusKey,
        )
        previousFocusKey = focusKey
    }
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView =
        remember {
            MapView(context).apply {
                onCreate(Bundle())
            }
        }

    DisposableEffect(lifecycle, mapView) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> Unit
                }
            }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onStop()
        }
    }

    return mapView
}

@Composable
private fun rememberRenderedFlight(
    flight: Flight,
    motionIntensity: MotionIntensity,
): RenderedFlight {
    val duration =
        when (motionIntensity) {
            MotionIntensity.Reduced -> 0
            MotionIntensity.Standard -> 980
            MotionIntensity.Expressive -> 1_160
        }

    val latitude by animateFloatAsState(
        targetValue = flight.position.latitude.toFloat(),
        animationSpec = tween(durationMillis = duration, easing = LinearEasing),
        label = "flight-lat-${flight.id}",
    )
    val longitude by animateFloatAsState(
        targetValue = flight.position.longitude.toFloat(),
        animationSpec = tween(durationMillis = duration, easing = LinearEasing),
        label = "flight-lon-${flight.id}",
    )
    val heading by animateFloatAsState(
        targetValue = flight.position.headingDegrees,
        animationSpec = tween(durationMillis = (duration * 0.78f).toInt().coerceAtLeast(0)),
        label = "flight-heading-${flight.id}",
    )

    return remember(flight, latitude, longitude, heading) {
        RenderedFlight(
            flight = flight,
            latitude = latitude.toDouble(),
            longitude = longitude.toDouble(),
            headingDegrees = heading,
        )
    }
}

private fun configureBaseStyle(
    style: Style,
    mapAppearance: MapAppearance,
    airportFocus: Boolean,
) {
    style.getLayer("background")?.setProperties(
        backgroundColor("#010102"),
    )
    style.getLayer("natural_earth")?.setProperties(
        rasterOpacity(if (mapAppearance == MapAppearance.NightHybrid) 0.09f else 0.035f),
    )
    style.getLayer("water")?.setProperties(
        fillColor("#05080B"),
    )
    style.getLayer("waterway_river")?.setProperties(
        lineColor("#0D1318"),
    )
    style.getLayer("waterway_other")?.setProperties(
        lineColor("#0A1015"),
    )
    style.getLayer("aeroway_fill")?.setProperties(
        fillColor(if (airportFocus) "#18191A" else "#171A1D"),
        fillOpacity(if (airportFocus) 0.84f else 0.72f),
    )
    style.getLayer("aeroway_runway")?.setProperties(
        lineColor(if (airportFocus) "#56595C" else "#3A3E42"),
        lineWidth(if (airportFocus) 2.15f else 1.65f),
    )
    style.getLayer("aeroway_taxiway")?.setProperties(
        lineColor(if (airportFocus) "#34373A" else "#272C30"),
        lineWidth(if (airportFocus) 1.28f else 0.95f),
    )
    style.getLayer("boundary_2")?.setProperties(
        lineColor(if (airportFocus) "rgba(118, 120, 124, 0.16)" else "rgba(118, 120, 124, 0.26)"),
    )
    style.getLayer("boundary_3")?.setProperties(
        lineColor(if (airportFocus) "rgba(96, 98, 102, 0.10)" else "rgba(96, 98, 102, 0.18)"),
    )

    HiddenBaseLayerIds.forEach { id ->
        style.getLayer(id)?.setProperties(visibility(Property.NONE))
    }

    style.layers.forEach { layer ->
        if (HiddenBasePrefixes.any { prefix -> layer.id.startsWith(prefix) }) {
            layer.setProperties(visibility(Property.NONE))
        }
    }

    listOf(
        "label_town",
        "label_village",
        "label_other",
    ).forEach { id ->
        style.getLayer(id)?.setProperties(visibility(Property.NONE))
    }

    listOf(
        "label_country_1",
        "label_country_2",
        "label_country_3",
    ).forEach { id ->
        style.getLayer(id)?.setProperties(
            textColor("rgba(218, 220, 222, 0.44)"),
            textHaloColor("rgba(0, 0, 0, 0.92)"),
            textHaloWidth(0.85f),
        )
    }

    listOf(
        "label_city",
        "label_city_capital",
        "label_state",
    ).forEach { id ->
        if (airportFocus) {
            style.getLayer(id)?.setProperties(visibility(Property.NONE))
        } else {
            style.getLayer(id)?.setProperties(
                textColor("rgba(176, 179, 182, 0.34)"),
                textHaloColor("rgba(0, 0, 0, 0.94)"),
                textHaloWidth(0.7f),
            )
        }
    }

    if (mapAppearance == MapAppearance.NightHybrid) {
        style.getLayer("building")?.setProperties(
            fillColor("#15181B"),
            fillOpacity(if (airportFocus) 0.26f else 0.42f),
        )
        style.getLayer("building-3d")?.setProperties(
            visibility(Property.VISIBLE),
            fillExtrusionColor("#181B1E"),
            fillExtrusionOpacity(if (airportFocus) 0.26f else 0.42f),
        )
    } else {
        style.getLayer("building")?.setProperties(visibility(Property.NONE))
        style.getLayer("building-3d")?.setProperties(visibility(Property.NONE))
    }
}

private fun ensureFlightLayers(
    style: Style,
    context: Context,
) {
    if (style.getImage(AircraftMarkerDefaultImageId) == null) {
        style.addImage(
            AircraftMarkerDefaultImageId,
            createAircraftMarkerBitmap(context = context, selected = false),
        )
    }
    if (style.getImage(AircraftMarkerSelectedImageId) == null) {
        style.addImage(
            AircraftMarkerSelectedImageId,
            createAircraftMarkerBitmap(context = context, selected = true),
        )
    }

    if (style.getSource(FlightsSourceId) == null) {
        style.addSource(GeoJsonSource(FlightsSourceId, emptyFeatureCollection()))
    }
    if (style.getSource(RoutesSourceId) == null) {
        style.addSource(GeoJsonSource(RoutesSourceId, emptyFeatureCollection()))
    }
    if (style.getSource(BreadcrumbSourceId) == null) {
        style.addSource(GeoJsonSource(BreadcrumbSourceId, emptyFeatureCollection()))
    }
    if (style.getSource(AirportsSourceId) == null) {
        style.addSource(GeoJsonSource(AirportsSourceId, emptyFeatureCollection()))
    }
    if (style.getSource(AirportContextSourceId) == null) {
        style.addSource(GeoJsonSource(AirportContextSourceId, emptyFeatureCollection()))
    }
    if (style.getSource(GridSourceId) == null) {
        style.addSource(GeoJsonSource(GridSourceId, buildGridFeatureCollection()))
    }

    if (style.getLayer(GridMinorLayerId) == null) {
        style.addLayer(
            LineLayer(GridMinorLayerId, GridSourceId).withProperties(
                lineColor("rgba(255,255,255,0.028)"),
                lineWidth(0.7f),
                lineOpacity(0.86f),
                lineJoin(Property.LINE_JOIN_ROUND),
            ).withFilter(eq(get("major"), literal(false))),
        )
    }
    if (style.getLayer(GridMajorLayerId) == null) {
        style.addLayer(
            LineLayer(GridMajorLayerId, GridSourceId).withProperties(
                lineColor("rgba(255,255,255,0.052)"),
                lineWidth(0.95f),
                lineOpacity(0.94f),
                lineJoin(Property.LINE_JOIN_ROUND),
            ).withFilter(eq(get("major"), literal(true))),
        )
    }

    if (style.getLayer(RouteLayerId) == null) {
        style.addLayer(
            LineLayer(RouteLayerId, RoutesSourceId).withProperties(
                lineColor("rgba(255,255,255,0.075)"),
                lineWidth(1.15f),
                lineOpacity(0.72f),
                lineJoin(Property.LINE_JOIN_ROUND),
            ).withFilter(eq(get("selected"), literal(false))),
        )
    }
    if (style.getLayer(RouteSelectedBaseLayerId) == null) {
        style.addLayer(
            LineLayer(RouteSelectedBaseLayerId, RoutesSourceId).withProperties(
                lineColor("rgba(255,255,255,0.14)"),
                lineWidth(4.4f),
                lineOpacity(0.46f),
                lineJoin(Property.LINE_JOIN_ROUND),
            ).withFilter(eq(get("selected"), literal(true))),
        )
    }
    if (style.getLayer(RouteSelectedLayerId) == null) {
        style.addLayer(
            LineLayer(RouteSelectedLayerId, RoutesSourceId).withProperties(
                lineColor(get("accent")),
                lineWidth(2.55f),
                lineOpacity(0.88f),
                lineJoin(Property.LINE_JOIN_ROUND),
            ).withFilter(eq(get("selected"), literal(true))),
        )
    }
    if (style.getLayer(BreadcrumbLayerId) == null) {
        style.addLayer(
            LineLayer(BreadcrumbLayerId, BreadcrumbSourceId).withProperties(
                lineColor(get("accent")),
                lineWidth(2.0f),
                lineOpacity(0.96f),
                lineJoin(Property.LINE_JOIN_ROUND),
            ),
        )
    }
    if (style.getLayer(AirportLayerId) == null) {
        style.addLayer(
            CircleLayer(AirportLayerId, AirportsSourceId).withProperties(
                circleRadius(3.6f),
                circleColor("rgba(240,242,244,0.16)"),
                circleStrokeColor("rgba(240,242,244,0.28)"),
                circleStrokeWidth(0.8f),
                circleStrokeOpacity(1f),
                circleOpacity(1f),
            ).withFilter(eq(get("selected"), literal(false))),
        )
    }
    if (style.getLayer(AirportSelectedLayerId) == null) {
        style.addLayer(
            CircleLayer(AirportSelectedLayerId, AirportsSourceId).withProperties(
                circleRadius(5.1f),
                circleColor("rgba(240,242,244,0.28)"),
                circleStrokeColor("rgba(240,242,244,0.76)"),
                circleStrokeWidth(1.1f),
                circleStrokeOpacity(1f),
                circleOpacity(1f),
            ).withFilter(eq(get("selected"), literal(true))),
        )
    }
    if (style.getLayer(AirportContextGuideLayerId) == null) {
        style.addLayer(
            LineLayer(AirportContextGuideLayerId, AirportContextSourceId).withProperties(
                lineColor("rgba(244,245,247,0.12)"),
                lineWidth(0.85f),
                lineOpacity(0.92f),
                lineJoin(Property.LINE_JOIN_ROUND),
            ).withFilter(eq(get("kind"), literal("guide"))),
        )
    }
    if (style.getLayer(AirportContextRingLayerId) == null) {
        style.addLayer(
            LineLayer(AirportContextRingLayerId, AirportContextSourceId).withProperties(
                lineColor("rgba(244,245,247,0.18)"),
                lineWidth(1.1f),
                lineOpacity(0.94f),
                lineJoin(Property.LINE_JOIN_ROUND),
            ).withFilter(eq(get("kind"), literal("ring"))),
        )
    }
    if (style.getLayer(AircraftHaloLayerId) == null) {
        style.addLayer(
            CircleLayer(AircraftHaloLayerId, FlightsSourceId).withProperties(
                circleRadius(10.0f),
                circleColor(get("accent")),
                circleOpacity(0.08f),
                circleStrokeColor(get("accent")),
                circleStrokeWidth(0.9f),
                circleStrokeOpacity(0.18f),
            ).withFilter(eq(get("selected"), literal(true))),
        )
    }
    if (style.getLayer(AircraftLayerId) == null) {
        style.addLayer(
            SymbolLayer(AircraftLayerId, FlightsSourceId).withProperties(
                iconImage(AircraftMarkerDefaultImageId),
                iconSize(1f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
                iconRotate(get("heading")),
                iconOpacity(0.92f),
            ).withFilter(eq(get("selected"), literal(false))),
        )
    }
    if (style.getLayer(AircraftSelectedLayerId) == null) {
        style.addLayer(
            SymbolLayer(AircraftSelectedLayerId, FlightsSourceId).withProperties(
                iconImage(AircraftMarkerSelectedImageId),
                iconSize(1.05f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
                iconRotate(get("heading")),
                iconOpacity(1f),
            ).withFilter(eq(get("selected"), literal(true))),
        )
    }
    if (style.getLayer(AircraftSelectedLabelLayerId) == null) {
        style.addLayer(
            SymbolLayer(AircraftSelectedLabelLayerId, FlightsSourceId).withProperties(
                textField(get("flightNumber")),
                textSize(11f),
                textOffset(arrayOf(0f, 1.18f)),
                textColor("rgba(225,227,229,0.72)"),
                textHaloColor("rgba(0,0,0,0.92)"),
                textHaloWidth(0.8f),
                iconOpacity(0f),
            ).withFilter(eq(get("selected"), literal(true))),
        )
    }
}

private fun updateFlightSources(
    style: Style,
    flights: List<RenderedFlight>,
    selectedFlight: Flight?,
    selectedAirport: Airport?,
    showAirports: Boolean,
) {
    val selectedFlightId = selectedFlight?.id
    val routeFeatures =
        flights.map { renderedFlight ->
            val flight = renderedFlight.flight
            Feature.fromGeometry(
                LineString.fromLngLats(densifyRouteForMap(flight.route)),
            ).apply {
                addStringProperty("flightId", flight.id)
                addStringProperty("accent", accentHexString(flight.accentColorHex))
                addBooleanProperty("selected", flight.id == selectedFlightId)
            }
        }

    val breadcrumbFeatures =
        selectedFlight
            ?.takeIf { it.breadcrumb.size > 1 }
            ?.let { flight ->
                listOf(
                    Feature.fromGeometry(
                        LineString.fromLngLats(densifyRouteForMap(flight.breadcrumb)),
                    ).apply {
                        addStringProperty("flightId", flight.id)
                        addStringProperty("accent", accentHexString(flight.accentColorHex))
                    },
                )
            } ?: emptyList()

    val flightFeatures =
        flights.map { renderedFlight ->
            val flight = renderedFlight.flight
            Feature.fromGeometry(
                Point.fromLngLat(
                    renderedFlight.longitude,
                    renderedFlight.latitude,
                ),
            ).apply {
                addStringProperty("flightId", flight.id)
                addStringProperty("flightNumber", flight.flightNumber)
                addBooleanProperty("selected", flight.id == selectedFlightId)
                addNumberProperty("heading", renderedFlight.headingDegrees.toDouble())
                addStringProperty("accent", accentHexString(flight.accentColorHex))
            }
        }

    val airportsToRender =
        when {
            showAirports ->
                flights
                    .flatMap { listOf(it.flight.origin, it.flight.destination) }
                    .distinctBy { it.code }

            selectedFlight != null -> listOf(selectedFlight.origin, selectedFlight.destination)
            selectedAirport != null -> listOf(selectedAirport)
            else -> emptyList()
        }

    val airportFeatures =
        airportsToRender.map { airport ->
            Feature.fromGeometry(
                Point.fromLngLat(airport.longitude, airport.latitude),
            ).apply {
                addStringProperty("airportCode", airport.code)
                addBooleanProperty("selected", airport.code == selectedAirport?.code)
            }
        }
    val airportContextFeatures = buildAirportContextFeatures(selectedAirport)

    style.getSourceAs<GeoJsonSource>(RoutesSourceId)?.setGeoJson(FeatureCollection.fromFeatures(routeFeatures))
    style.getSourceAs<GeoJsonSource>(BreadcrumbSourceId)?.setGeoJson(FeatureCollection.fromFeatures(breadcrumbFeatures))
    style.getSourceAs<GeoJsonSource>(FlightsSourceId)?.setGeoJson(FeatureCollection.fromFeatures(flightFeatures))
    style.getSourceAs<GeoJsonSource>(AirportsSourceId)?.setGeoJson(FeatureCollection.fromFeatures(airportFeatures))
    style.getSourceAs<GeoJsonSource>(AirportContextSourceId)?.setGeoJson(FeatureCollection.fromFeatures(airportContextFeatures))
}

private fun handleMapClick(
    map: MapLibreMap,
    latLng: LatLng,
    flights: List<Flight>,
    selectedFlight: Flight?,
    onClearSelection: () -> Unit,
    onSelectFlight: (Flight) -> Unit,
    onSelectAirport: (Airport) -> Unit,
    onSelectRoute: (Flight) -> Unit,
) : Boolean {
    val screenPoint = map.projection.toScreenLocation(latLng)

    val flightFeature =
        map.queryRenderedFeatures(
            PointF(screenPoint.x.toFloat(), screenPoint.y.toFloat()),
            AircraftSelectedLayerId,
            AircraftLayerId,
        ).firstOrNull()
    if (flightFeature != null) {
        val flightId = flightFeature.getStringProperty("flightId")
        val selected = selectedFlight?.id == flightId
        if (selected) {
            selectedFlight?.let(onSelectRoute)
        } else {
            flights.firstOrNull { it.id == flightId }?.let(onSelectFlight)
        }
        return true
    }

    val airportFeature =
        map.queryRenderedFeatures(
            PointF(screenPoint.x.toFloat(), screenPoint.y.toFloat()),
            AirportSelectedLayerId,
            AirportLayerId,
        ).firstOrNull()
    if (airportFeature != null) {
        val code = airportFeature.getStringProperty("airportCode")
        flights
            .flatMap { listOf(it.origin, it.destination) }
            .distinctBy { it.code }
            .firstOrNull { it.code == code }
            ?.let(onSelectAirport)
        return true
    }

    onClearSelection()
    return true
}

private suspend fun animateCameraToFocus(
    map: MapLibreMap,
    flights: List<Flight>,
    selectedFlight: Flight?,
    selectedAirport: Airport?,
    padding: MapPaddingPx,
    motionIntensity: MotionIntensity,
    previousFocusKey: String?,
) : Unit {
    map.cancelTransitions()
    when {
        selectedFlight != null ->
            animateToSelectedFlight(
                map = map,
                flight = selectedFlight,
                padding = padding,
                motionIntensity = motionIntensity,
                previousFocusKey = previousFocusKey,
            )

        selectedAirport != null ->
            animateToSelectedAirport(
                map = map,
                airport = selectedAirport,
                padding = padding,
                motionIntensity = motionIntensity,
                previousFocusKey = previousFocusKey,
            )

        flights.isNotEmpty() ->
            animateToOverview(
                map = map,
                flights = flights,
                padding = padding,
                motionIntensity = motionIntensity,
            )
    }
}

private suspend fun animateToSelectedFlight(
    map: MapLibreMap,
    flight: Flight,
    padding: MapPaddingPx,
    motionIntensity: MotionIntensity,
    previousFocusKey: String?,
) {
    val routeBounds = buildFlightRouteBounds(flight) ?: return
    val presentationBounds = buildFlightPresentationBounds(flight) ?: routeBounds
    val routePadding = padding.adjusted(top = 40, right = 18, bottom = 84)
    val presentationPadding = padding.adjusted(top = 64, right = 22, bottom = 132)
    val routeContextCamera =
        map.getCameraForLatLngBounds(
            routeBounds,
            routePadding.toArray(),
            0.0,
            0.0,
        ) ?: CameraPosition.Builder().target(flight.position.toLatLng()).zoom(4.8).build()
    val usePerspective = shouldUsePerspective(flight)
    val presentationBearing = if (usePerspective) calculateBearing(flight.origin.toLatLng(), flight.destination.toLatLng()) else 0.0
    val presentationTilt =
        when (motionIntensity) {
            MotionIntensity.Reduced -> 0.0
            MotionIntensity.Standard -> if (usePerspective) 12.0 else 0.0
            MotionIntensity.Expressive -> if (usePerspective) 17.0 else 8.0
        }
    val presentationCamera =
        map.getCameraForLatLngBounds(
            presentationBounds,
            presentationPadding.toArray(),
            presentationBearing,
            presentationTilt,
        ) ?: CameraPosition.Builder().target(flight.position.toLatLng()).zoom(6.2).bearing(presentationBearing).tilt(presentationTilt).build()
    val routeContextDuration =
        when (motionIntensity) {
            MotionIntensity.Reduced -> 0
            MotionIntensity.Standard -> if (previousFocusKey == "overview") 760 else 560
            MotionIntensity.Expressive -> if (previousFocusKey == "overview") 980 else 720
        }
    val presentationDuration =
        when (motionIntensity) {
            MotionIntensity.Reduced -> 0
            MotionIntensity.Standard -> 680
            MotionIntensity.Expressive -> 880
        }
    val shouldStage = motionIntensity != MotionIntensity.Reduced && previousFocusKey != null && previousFocusKey != "flight:${flight.id}"

    if (shouldStage) {
        map.awaitCamera(
            update = CameraUpdateFactory.newCameraPosition(routeContextCamera),
            durationMillis = routeContextDuration,
            useEase = true,
        )
    }

    map.awaitCamera(
        update = CameraUpdateFactory.newCameraPosition(presentationCamera),
        durationMillis = presentationDuration,
        useEase = false,
    )
}

private suspend fun animateToSelectedAirport(
    map: MapLibreMap,
    airport: Airport,
    padding: MapPaddingPx,
    motionIntensity: MotionIntensity,
    previousFocusKey: String?,
) {
    val regionBounds = latLngBoundsOf(buildAirportRangeRing(airport, radiusMeters = 18_500.0))
    val contextPadding = padding.adjusted(top = 44, right = 18, bottom = 92)
    val contextDuration =
        when (motionIntensity) {
            MotionIntensity.Reduced -> 0
            MotionIntensity.Standard -> if (previousFocusKey == "overview") 720 else 560
            MotionIntensity.Expressive -> if (previousFocusKey == "overview") 920 else 680
        }
    val detailDuration =
        when (motionIntensity) {
            MotionIntensity.Reduced -> 0
            MotionIntensity.Standard -> 620
            MotionIntensity.Expressive -> 820
        }

    if (regionBounds != null && motionIntensity != MotionIntensity.Reduced) {
        val regionCamera =
            map.getCameraForLatLngBounds(
                regionBounds,
                contextPadding.toArray(),
                0.0,
                0.0,
            ) ?: CameraPosition.Builder().target(airport.toLatLng()).zoom(8.8).build()
        map.awaitCamera(
            update = CameraUpdateFactory.newCameraPosition(regionCamera),
            durationMillis = contextDuration,
            useEase = true,
        )
    }

    val airportCamera =
        CameraPosition.Builder()
            .target(airport.toLatLng())
            .zoom(
                when (motionIntensity) {
                    MotionIntensity.Reduced -> 10.6
                    MotionIntensity.Standard -> 10.9
                    MotionIntensity.Expressive -> 11.2
                },
            ).tilt(
                when (motionIntensity) {
                    MotionIntensity.Reduced -> 0.0
                    MotionIntensity.Standard -> 8.0
                    MotionIntensity.Expressive -> 12.0
                },
            ).bearing(0.0)
            .build()
    map.awaitCamera(
        update = CameraUpdateFactory.newCameraPosition(airportCamera),
        durationMillis = detailDuration,
        useEase = false,
    )
}

private suspend fun animateToOverview(
    map: MapLibreMap,
    flights: List<Flight>,
    padding: MapPaddingPx,
    motionIntensity: MotionIntensity,
) {
    val overviewBounds =
        latLngBoundsOf(
            flights.flatMap { flight ->
                buildList {
                    add(flight.origin.toLatLng())
                    add(flight.destination.toLatLng())
                    add(flight.position.toLatLng())
                }
            },
        ) ?: return
    val overviewDuration =
        when (motionIntensity) {
            MotionIntensity.Reduced -> 0
            MotionIntensity.Standard -> 760
            MotionIntensity.Expressive -> 920
        }
    val overviewPadding = padding.adjusted(top = 26, right = 18, bottom = 42)
    val overviewCamera =
        map.getCameraForLatLngBounds(
            overviewBounds,
            overviewPadding.toArray(),
            0.0,
            0.0,
        ) ?: CameraPosition.Builder().target(flights.first().position.toLatLng()).zoom(2.3).build()
    map.awaitCamera(
        update = CameraUpdateFactory.newCameraPosition(overviewCamera),
        durationMillis = overviewDuration,
        useEase = true,
    )
}

private fun buildGridFeatureCollection(): FeatureCollection {
    val features = mutableListOf<Feature>()

    (-180..180 step 15).forEach { longitude ->
        val points =
            (-75..75 step 5).map { latitude ->
                Point.fromLngLat(longitude.toDouble(), latitude.toDouble())
            }
        features +=
            Feature.fromGeometry(LineString.fromLngLats(points)).apply {
                addBooleanProperty("major", longitude % 30 == 0 || longitude == 0)
            }
    }

    (-60..60 step 15).forEach { latitude ->
        val points =
            (-180..180 step 5).map { longitude ->
                Point.fromLngLat(longitude.toDouble(), latitude.toDouble())
            }
        features +=
            Feature.fromGeometry(LineString.fromLngLats(points)).apply {
                addBooleanProperty("major", latitude % 30 == 0 || latitude == 0)
            }
    }

    return FeatureCollection.fromFeatures(features)
}

private fun emptyFeatureCollection(): FeatureCollection = FeatureCollection.fromFeatures(emptyArray())

private fun buildAirportContextFeatures(
    selectedAirport: Airport?,
): List<Feature> {
    val airport = selectedAirport ?: return emptyList()
    val ringFeatures =
        listOf(
            buildAirportRangeRing(airport, radiusMeters = 9_260.0),
            buildAirportRangeRing(airport, radiusMeters = 18_520.0),
        ).map { ring ->
            Feature.fromGeometry(LineString.fromLngLats(ring.map { Point.fromLngLat(it.longitude, it.latitude) })).apply {
                addStringProperty("kind", "ring")
            }
        }
    val guideFeatures =
        listOf(
            buildAirportGuideLine(airport, bearingDegrees = 0.0, halfLengthMeters = 19_000.0),
            buildAirportGuideLine(airport, bearingDegrees = 90.0, halfLengthMeters = 19_000.0),
        ).map { guide ->
            Feature.fromGeometry(LineString.fromLngLats(guide.map { Point.fromLngLat(it.longitude, it.latitude) })).apply {
                addStringProperty("kind", "guide")
            }
        }
    return ringFeatures + guideFeatures
}

private fun buildAirportRangeRing(
    airport: Airport,
    radiusMeters: Double,
): List<LatLng> {
    val center = airport.toLatLng()
    return (0..72).map { index ->
        destinationPoint(
            origin = center,
            bearingDegrees = index * 5.0,
            distanceMeters = radiusMeters,
        )
    }
}

private fun buildAirportGuideLine(
    airport: Airport,
    bearingDegrees: Double,
    halfLengthMeters: Double,
): List<LatLng> {
    val center = airport.toLatLng()
    return listOf(
        destinationPoint(center, bearingDegrees + 180.0, halfLengthMeters),
        center,
        destinationPoint(center, bearingDegrees, halfLengthMeters),
    )
}

private fun buildFlightRouteBounds(flight: Flight): LatLngBounds? =
    latLngBoundsOf(
        buildList {
            add(flight.origin.toLatLng())
            add(flight.destination.toLatLng())
            add(flight.position.toLatLng())
            flight.route.forEach { add(it.toLatLng()) }
        },
    )

private fun buildFlightPresentationBounds(flight: Flight): LatLngBounds? {
    val route = flight.route
    if (route.isEmpty()) {
        return latLngBoundsOf(listOf(flight.origin.toLatLng(), flight.position.toLatLng(), flight.destination.toLatLng()))
    }

    val anchorIndex = (flight.progress * route.lastIndex.coerceAtLeast(0)).roundToInt().coerceIn(0, route.lastIndex)
    val stride = (route.lastIndex / 5).coerceAtLeast(1)
    val contextPoints =
        buildList {
            add(flight.position.toLatLng())
            listOf(
                (anchorIndex - stride).coerceAtLeast(0),
                anchorIndex,
                (anchorIndex + stride).coerceAtMost(route.lastIndex),
                (anchorIndex + (stride * 2)).coerceAtMost(route.lastIndex),
            ).distinct().forEach { add(route[it].toLatLng()) }
            if (flight.progress < 0.14f) add(flight.origin.toLatLng())
            if (flight.progress > 0.86f) add(flight.destination.toLatLng())
        }
    return latLngBoundsOf(contextPoints)
}

private fun latLngBoundsOf(points: List<LatLng>): LatLngBounds? {
    val uniquePoints = points.distinctBy { "${it.latitude}:${it.longitude}" }
    if (uniquePoints.size < 2) return null
    return LatLngBounds.Builder().apply {
        uniquePoints.forEach(::include)
    }.build()
}

private fun shouldUsePerspective(flight: Flight): Boolean {
    val distanceMeters = greatCircleDistanceMeters(flight.origin.toLatLng(), flight.destination.toLatLng())
    return distanceMeters >= 450_000.0
}

private fun calculateBearing(
    start: LatLng,
    end: LatLng,
): Double {
    val startLat = Math.toRadians(start.latitude)
    val endLat = Math.toRadians(end.latitude)
    val deltaLon = Math.toRadians(end.longitude - start.longitude)
    val y = sin(deltaLon) * cos(endLat)
    val x = (cos(startLat) * sin(endLat)) - (sin(startLat) * cos(endLat) * cos(deltaLon))
    val bearing = Math.toDegrees(atan2(y, x))
    return (bearing + 360.0) % 360.0
}

private fun destinationPoint(
    origin: LatLng,
    bearingDegrees: Double,
    distanceMeters: Double,
): LatLng {
    val radius = 6_371_000.0
    val angularDistance = distanceMeters / radius
    val bearing = Math.toRadians(bearingDegrees)
    val startLat = Math.toRadians(origin.latitude)
    val startLon = Math.toRadians(origin.longitude)
    val targetLat =
        asinSafe(
            (sin(startLat) * cos(angularDistance)) +
                (cos(startLat) * sin(angularDistance) * cos(bearing)),
        )
    val targetLon =
        startLon + atan2(
            sin(bearing) * sin(angularDistance) * cos(startLat),
            cos(angularDistance) - (sin(startLat) * sin(targetLat)),
        )
    return LatLng(
        Math.toDegrees(targetLat),
        normalizeLongitude(Math.toDegrees(targetLon)),
    )
}

private fun asinSafe(value: Double): Double = kotlin.math.asin(value.coerceIn(-1.0, 1.0))

private fun normalizeLongitude(value: Double): Double =
    when {
        value > 180.0 -> value - 360.0
        value < -180.0 -> value + 360.0
        else -> value
    }

private fun greatCircleDistanceMeters(
    start: LatLng,
    end: LatLng,
): Double {
    val deltaLat = Math.toRadians(end.latitude - start.latitude)
    val deltaLon = Math.toRadians(end.longitude - start.longitude)
    val startLat = Math.toRadians(start.latitude)
    val endLat = Math.toRadians(end.latitude)
    val a =
        (sin(deltaLat / 2) * sin(deltaLat / 2)) +
            (cos(startLat) * cos(endLat) * sin(deltaLon / 2) * sin(deltaLon / 2))
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return 6_371_000.0 * c
}

private suspend fun MapLibreMap.awaitCamera(
    update: CameraUpdate,
    durationMillis: Int,
    useEase: Boolean,
) {
    if (durationMillis <= 0) {
        moveCamera(update)
        return
    }
    suspendCancellableCoroutine { continuation ->
        val callback =
            object : MapLibreMap.CancelableCallback {
                override fun onFinish() {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onCancel() {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        if (useEase) {
            easeCamera(update, durationMillis, callback)
        } else {
            animateCamera(update, durationMillis, callback)
        }
        continuation.invokeOnCancellation { cancelTransitions() }
    }
}

private data class RenderedFlight(
    val flight: Flight,
    val latitude: Double,
    val longitude: Double,
    val headingDegrees: Float,
)

private data class MapPaddingPx(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    fun adjusted(
        left: Int = 0,
        top: Int = 0,
        right: Int = 0,
        bottom: Int = 0,
    ): MapPaddingPx =
        MapPaddingPx(
            left = this.left + left,
            top = this.top + top,
            right = this.right + right,
            bottom = this.bottom + bottom,
        )

    fun toArray(): IntArray = intArrayOf(left, top, right, bottom)
}
