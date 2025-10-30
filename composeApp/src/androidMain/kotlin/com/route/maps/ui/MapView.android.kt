package com.route.maps.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
    driverLocation: Location?,
    route: Route?,
    routeProgress: Float,
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
        // Current location marker (only show if no driver location)
        if (driverLocation == null) {
            Marker(
                state = MarkerState(position = currentLatLng),
                title = "Current Location",
                snippet = "You are here",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        }

        // Pickup location marker
        pickupLocation?.let { location ->
            Marker(
                state = MarkerState(
                    position = LatLng(location.latitude, location.longitude)
                ),
                title = "Pickup",
                snippet = location.address,
                icon = BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_GREEN
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
                icon = BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_RED
                )
            )
        }

        // Route polylines - covered (gray) and remaining (black)
        route?.let { calculatedRoute ->
            if (calculatedRoute.polyline.isNotEmpty()) {
                val routePoints = calculatedRoute.polyline.map {
                    LatLng(it.latitude, it.longitude)
                }
                
                if (routeProgress > 0f && routeProgress < 1f) {
                    // Calculate split point
                    val splitIndex = (routePoints.size * routeProgress).toInt().coerceIn(1, routePoints.size - 1)
                    
                    // Covered route (gray)
                    if (splitIndex > 0) {
                        Polyline(
                            points = routePoints.subList(0, splitIndex + 1),
                            color = Color.Gray,
                            width = 12f
                        )
                    }
                    
                    // Remaining route (black/dark)
                    if (splitIndex < routePoints.size - 1) {
                        Polyline(
                            points = routePoints.subList(splitIndex, routePoints.size),
                            color = Color(0xFF2C2C2C),
                            width = 12f
                        )
                    }
                } else {
                    // No progress or complete - show full route
                    Polyline(
                        points = routePoints,
                        color = if (routeProgress >= 1f) Color.Gray else Color(0xFF4285F4),
                        width = 12f
                    )
                }
            }
        }
        
        // Driver location marker (bike icon representation)
        driverLocation?.let { location ->
            Marker(
                state = MarkerState(
                    position = LatLng(location.latitude, location.longitude)
                ),
                title = "Driver",
                snippet = "On the way",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                // In production, you would use a custom bike icon here
                // icon = BitmapDescriptorFactory.fromResource(R.drawable.bike_icon)
                rotation = 0f
            )
        }
    }
}
