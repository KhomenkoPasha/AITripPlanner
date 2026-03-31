package khom.pavlo.aitripplanner.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.TravelTheme
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
actual fun PlatformPlaceMap(
    latitude: Double,
    longitude: Double,
    label: String,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val strings = appStrings()
    val point = remember(latitude, longitude) { GeoPoint(latitude, longitude) }
    val renderKey = remember(latitude, longitude, label) { "$latitude:$longitude:$label" }
    val appliedRenderKeyRef = remember {
        object {
            var value: String? = null
        }
    }
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

                if (appliedRenderKeyRef.value != renderKey) {
                    view.overlays.removeAll { overlay -> overlay !== locationOverlay }
                    view.controller.setZoom(15.0)
                    view.controller.setCenter(point)
                    view.overlays += Marker(view).apply {
                        position = point
                        title = label
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    if (hasLocationPermission && !view.overlays.contains(locationOverlay)) {
                        view.overlays += locationOverlay
                    }
                    appliedRenderKeyRef.value = renderKey
                }
                view.invalidate()
            },
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MapControlButton(onClick = { mapView.controller.zoomIn() }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = strings.zoomInAction,
                )
            }
            MapControlButton(onClick = { mapView.controller.zoomOut() }) {
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
        modifier = Modifier.size(42.dp),
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
