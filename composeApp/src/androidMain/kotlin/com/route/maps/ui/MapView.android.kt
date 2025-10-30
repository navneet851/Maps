package com.route.maps.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.route.maps.model.Location
import com.route.maps.model.Route

/**
 * Android implementation using Google Maps Compose
 */
@Composable
actual fun MapView(
    currentLocation: Location,
    pickupLocation: Location?,
    destinationLocation: Location?,
    route: Route?,
    onMapClick: (Location) -> Unit,
    modifier: Modifier
) {
    val currentLatLng = remember(currentLocation) {
        LatLng(currentLocation.latitude, currentLocation.longitude)
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
    }

    // Update camera when location changes
    LaunchedEffect(currentLocation) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            LatLng(currentLocation.latitude, currentLocation.longitude),
            15f
        )
    }

    // Update camera to show entire route when calculated
    LaunchedEffect(route) {
        route?.let {
            if (it.polyline.isNotEmpty()) {
                val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                it.polyline.forEach { location ->
                    bounds.include(LatLng(location.latitude, location.longitude))
                }
                try {
                    cameraPositionState.animate(
                        com.google.maps.android.compose.CameraUpdateFactory.newLatLngBounds(
                            bounds.build(),
                            100
                        )
                    )
                } catch (_: Exception) {
                    // Handle case where bounds are invalid
                }
            }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            compassEnabled = true
        ),
        onMapClick = { latLng ->
            onMapClick(Location(latLng.latitude, latLng.longitude))
        }
    ) {
        // Current location marker
        Marker(
            state = MarkerState(position = currentLatLng),
            title = "Current Location",
            snippet = "You are here"
        )

        // Pickup location marker
        pickupLocation?.let { location ->
            Marker(
                state = MarkerState(
                    position = LatLng(location.latitude, location.longitude)
                ),
                title = "Pickup",
                snippet = location.address,
                icon = com.google.maps.android.compose.BitmapDescriptorFactory.defaultMarker(
                    com.google.maps.android.compose.BitmapDescriptorFactory.HUE_GREEN
                )
            )
        }

        // Destination marker
        destinationLocation?.let { location ->
            Marker(
                state = MarkerState(
                    position = LatLng(location.latitude, location.longitude)
                ),
                title = "Destination",
                snippet = location.address,
                icon = com.google.maps.android.compose.BitmapDescriptorFactory.defaultMarker(
                    com.google.maps.android.compose.BitmapDescriptorFactory.HUE_RED
                )
            )
        }

        // Route polyline
        route?.let { calculatedRoute ->
            if (calculatedRoute.polyline.isNotEmpty()) {
                Polyline(
                    points = calculatedRoute.polyline.map {
                        LatLng(it.latitude, it.longitude)
                    },
                    color = androidx.compose.ui.graphics.Color(0xFF4285F4),
                    width = 10f
                )
            }
        }
    }
}
