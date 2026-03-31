@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package khom.pavlo.aitripplanner.ui.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.TravelTheme
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLLocationManager
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@Composable
actual fun PlatformPlaceMap(
    latitude: Double,
    longitude: Double,
    label: String,
    modifier: Modifier,
) {
    val strings = appStrings()
    val locationManager = remember { CLLocationManager() }
    val mapView = remember {
        MKMapView().apply {
            scrollEnabled = true
            zoomEnabled = true
            pitchEnabled = false
            rotateEnabled = false
        }
    }

    Box(modifier = modifier) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { currentMapView ->
                val coordinate = CLLocationCoordinate2DMake(latitude, longitude)
                currentMapView.removeAnnotations(currentMapView.annotations)
                val annotation = MKPointAnnotation().apply {
                    setCoordinate(coordinate)
                    setTitle(label)
                }
                currentMapView.addAnnotation(annotation)
                currentMapView.setRegion(
                    region = MKCoordinateRegionMakeWithDistance(coordinate, 800.0, 800.0),
                    animated = false,
                )
            },
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MapControlButton(onClick = { zoomMap(mapView, 0.55) }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = strings.zoomInAction,
                )
            }
            MapControlButton(onClick = { zoomMap(mapView, 1.8) }) {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = strings.zoomOutAction,
                )
            }
            MapControlButton(
                onClick = { focusOnUserLocation(mapView, locationManager) },
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

private fun zoomMap(
    mapView: MKMapView,
    scaleFactor: Double,
) {
    val region = mapView.region.useContents { this }
    mapView.setRegion(
        region = MKCoordinateRegionMake(
            centerCoordinate = mapView.centerCoordinate,
            span = MKCoordinateSpanMake(
                latitudeDelta = (region.span.latitudeDelta * scaleFactor).coerceIn(0.002, 80.0),
                longitudeDelta = (region.span.longitudeDelta * scaleFactor).coerceIn(0.002, 80.0),
            ),
        ),
        animated = true,
    )
}

private fun focusOnUserLocation(
    mapView: MKMapView,
    locationManager: CLLocationManager,
) {
    locationManager.requestWhenInUseAuthorization()
    mapView.showsUserLocation = true
    val userLocation = mapView.userLocation.location ?: return
    mapView.setRegion(
        region = MKCoordinateRegionMakeWithDistance(userLocation.coordinate, 900.0, 900.0),
        animated = true,
    )
}
