package com.route.maps.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.route.maps.model.Location
import com.route.maps.model.Route

/**
 * Platform-specific map view composable
 * Implemented differently for Android and iOS
 */
@Composable
expect fun MapView(
    currentLocation: Location,
    pickupLocation: Location?,
    destinationLocation: Location?,
    driverLocation: Location?,
    route: Route?,
    routeProgress: Float,
    onMapClick: (Location) -> Unit,
    modifier: Modifier = Modifier
)
