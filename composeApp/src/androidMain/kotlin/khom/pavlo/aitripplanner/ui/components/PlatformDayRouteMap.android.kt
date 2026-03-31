package khom.pavlo.aitripplanner.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import khom.pavlo.aitripplanner.ui.model.DayRouteStopUiModel
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.TravelTheme
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
actual fun PlatformDayRouteMap(
    stops: List<DayRouteStopUiModel>,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val strings = appStrings()
    val markerFillColor = MaterialTheme.colorScheme.primary.toArgb()
    val markerTextColor = MaterialTheme.colorScheme.onPrimary.toArgb()
    val markerStrokeColor = MaterialTheme.colorScheme.surface.toArgb()
    val routeColor = MaterialTheme.colorScheme.secondary.toArgb()
    val geoPoints = remember(stops) {
        stops.map { stop -> GeoPoint(stop.latitude, stop.longitude) }
    }
    val routeBounds = remember(geoPoints) {
        geoPoints.takeIf { it.size > 1 }?.let(::buildBoundingBox)
    }
    val routeRenderKey = remember(
        stops,
        markerFillColor,
        markerTextColor,
        markerStrokeColor,
        routeColor,
    ) {
        buildString {
            append(markerFillColor)
            append(':')
            append(markerTextColor)
            append(':')
            append(markerStrokeColor)
            append(':')
            append(routeColor)
            stops.forEach { stop ->
                append('|')
                append(stop.id)
                append(':')
                append(stop.numberLabel)
                append(':')
                append(stop.latitude)
                append(',')
                append(stop.longitude)
            }
        }
    }
    val appliedRouteRenderKeyRef = remember {
        object {
            var value: String? = null
        }
    }
    val markerIcons = remember(stops, markerFillColor, markerTextColor, markerStrokeColor) {
        stops.associate { stop ->
            stop.id to BitmapDrawable(
                context.resources,
                createMarkerBitmap(
                    label = stop.numberLabel,
                    fillColor = markerFillColor,
                    textColor = markerTextColor,
                    strokeColor = markerStrokeColor,
                ),
            )
        }
    }
    val mapPaddingPx = remember(context) { (context.resources.displayMetrics.density * 72f).toInt() }
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
        }
    }
    val locationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
    }
    var hasLocationPermission by remember(context) {
        mutableStateOf(context.hasLocationPermission())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        hasLocationPermission = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            context.hasLocationPermission()
        if (hasLocationPermission) {
            focusOnCurrentLocation(mapView, locationOverlay)
        }
    }

    DisposableEffect(mapView, locationOverlay) {
        onDispose {
            locationOverlay.disableMyLocation()
            mapView.onDetach()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                syncLocationOverlay(
                    mapView = view,
                    locationOverlay = locationOverlay,
                    enabled = hasLocationPermission,
                )

                if (appliedRouteRenderKeyRef.value == routeRenderKey) {
                    view.invalidate()
                    return@AndroidView
                }

                view.overlays.removeAll { overlay -> overlay !== locationOverlay }

                if (geoPoints.size > 1) {
                    view.overlays += Polyline().apply {
                        setPoints(geoPoints)
                        outlinePaint.color = routeColor
                        outlinePaint.strokeWidth = 7f
                        isGeodesic = true
                    }
                }

                stops.zip(geoPoints).forEach { (stop, point) ->
                    view.overlays += Marker(view).apply {
                        position = point
                        title = stop.title
                        subDescription = stop.address
                        icon = markerIcons.getValue(stop.id)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                }

                if (hasLocationPermission && !view.overlays.contains(locationOverlay)) {
                    view.overlays += locationOverlay
                }

                when (geoPoints.size) {
                    0 -> Unit
                    1 -> {
                        view.controller.setZoom(15.5)
                        view.controller.setCenter(geoPoints.first())
                    }

                    else -> {
                        view.post {
                            view.zoomToBoundingBox(
                                routeBounds ?: return@post,
                                false,
                                mapPaddingPx,
                            )
                        }
                    }
                }

                appliedRouteRenderKeyRef.value = routeRenderKey
                view.invalidate()
            },
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MapControlButton(
                onClick = { mapView.controller.zoomIn() },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = strings.zoomInAction,
                )
            }
            MapControlButton(
                onClick = { mapView.controller.zoomOut() },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = strings.zoomOutAction,
                )
            }
            MapControlButton(
                onClick = {
                    val activity = context.findActivity()
                    if (context.hasLocationPermission()) {
                        hasLocationPermission = true
                        focusOnCurrentLocation(mapView, locationOverlay)
                    } else if (activity != null) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = strings.myLocationAction,
                )
            }
        }
    }
}

@Composable
private fun MapControlButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(46.dp),
        shape = TravelTheme.corners.medium,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

private fun syncLocationOverlay(
    mapView: MapView,
    locationOverlay: MyLocationNewOverlay,
    enabled: Boolean,
) {
    if (!enabled) {
        locationOverlay.disableMyLocation()
        mapView.overlays.remove(locationOverlay)
        return
    }

    if (!locationOverlay.isMyLocationEnabled) {
        locationOverlay.enableMyLocation()
    }
    if (!mapView.overlays.contains(locationOverlay)) {
        mapView.overlays += locationOverlay
    }
}

private fun focusOnCurrentLocation(
    mapView: MapView,
    locationOverlay: MyLocationNewOverlay,
) {
    syncLocationOverlay(mapView, locationOverlay, enabled = true)

    val currentLocation = locationOverlay.myLocation
    if (currentLocation != null) {
        mapView.post {
            if (mapView.zoomLevelDouble < 15.5) {
                mapView.controller.setZoom(15.5)
            }
            mapView.controller.animateTo(currentLocation)
        }
        return
    }

    locationOverlay.runOnFirstFix {
        val firstFix = locationOverlay.myLocation ?: return@runOnFirstFix
        mapView.post {
            if (mapView.zoomLevelDouble < 15.5) {
                mapView.controller.setZoom(15.5)
            }
            mapView.controller.animateTo(firstFix)
            mapView.invalidate()
        }
    }
}

private fun buildBoundingBox(points: List<GeoPoint>): BoundingBox {
    val maxLat = points.maxOf { it.latitude }
    val minLat = points.minOf { it.latitude }
    val maxLon = points.maxOf { it.longitude }
    val minLon = points.minOf { it.longitude }
    return BoundingBox(maxLat, maxLon, minLat, minLon)
}

private fun createMarkerBitmap(
    label: String,
    fillColor: Int,
    textColor: Int,
    strokeColor: Int,
): Bitmap {
    val size = 112
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val circleRadius = 34f
    val centerX = size / 2f
    val centerY = 38f
    val tailWidth = 20f
    val tailHeight = 24f

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x26000000
    }
    canvas.drawCircle(centerX, centerY + 3f, circleRadius + 4f, shadowPaint)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
        textSize = if (label.length > 1) 30f else 36f
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.DEFAULT_BOLD,
            android.graphics.Typeface.BOLD,
        )
    }

    canvas.drawCircle(centerX, centerY, circleRadius, fillPaint)
    canvas.drawCircle(centerX, centerY, circleRadius, strokePaint)
    canvas.drawRoundRect(
        centerX - tailWidth / 2f,
        centerY + circleRadius - 2f,
        centerX + tailWidth / 2f,
        centerY + circleRadius + tailHeight,
        10f,
        10f,
        fillPaint,
    )
    canvas.drawRoundRect(
        centerX - tailWidth / 2f,
        centerY + circleRadius - 2f,
        centerX + tailWidth / 2f,
        centerY + circleRadius + tailHeight,
        10f,
        10f,
        strokePaint,
    )

    val textBounds = Rect()
    textPaint.getTextBounds(label, 0, label.length, textBounds)
    val textY = centerY + textBounds.height() / 2f
    canvas.drawText(label, centerX, textY, textPaint)

    return bitmap
}

private fun Context.hasLocationPermission(): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    return fineGranted || coarseGranted
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
