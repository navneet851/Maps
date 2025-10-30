package com.route.maps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.route.maps.model.Location
import com.route.maps.model.NavigationState
import com.route.maps.ui.MapView
import com.route.maps.ui.components.NavigationBottomSheet
import com.route.maps.viewmodel.MapViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel: MapViewModel = viewModel { MapViewModel() }
        val navigationState by viewModel.navigationState.collectAsState()
        val pickupLocation by viewModel.pickupLocation.collectAsState()
        val destinationLocation by viewModel.destinationLocation.collectAsState()
        val currentLocation by viewModel.currentLocation.collectAsState()
        val driverLocation by viewModel.driverLocation.collectAsState()
        val routeProgress by viewModel.routeProgress.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        // Simulate driver movement during navigation
        LaunchedEffect(navigationState) {
            if (navigationState is NavigationState.Navigating) {
                val route = (navigationState as NavigationState.Navigating).route
                if (route.polyline.isNotEmpty()) {
                    // Simulate driver moving along route
                    var currentIndex = 0
                    while (currentIndex < route.polyline.size && navigationState is NavigationState.Navigating) {
                        viewModel.updateDriverLocation(route.polyline[currentIndex])
                        delay(2000) // Update every 2 seconds
                        currentIndex++
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Map view
            MapView(
                currentLocation = currentLocation,
                pickupLocation = pickupLocation,
                destinationLocation = destinationLocation,
                driverLocation = driverLocation,
                route = when (navigationState) {
                    is NavigationState.RouteCalculated -> (navigationState as NavigationState.RouteCalculated).route
                    is NavigationState.Navigating -> (navigationState as NavigationState.Navigating).route
                    else -> null
                },
                routeProgress = routeProgress,
                onMapClick = { location ->
                    when (navigationState) {
                        NavigationState.Idle -> {
                            viewModel.setPickupLocation(location)
                        }
                        is NavigationState.SelectingPickup -> {
                            viewModel.setPickupLocation(location)
                        }
                        is NavigationState.SelectingDestination -> {
                            viewModel.setDestinationLocation(location)
                        }
                        else -> {}
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Floating action button to start route planning
            if (navigationState is NavigationState.Idle) {
                FloatingActionButton(
                    onClick = {
                        // Use current location as pickup by default
                        viewModel.setPickupLocation(currentLocation)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("🗺️")
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
            }

            // Bottom sheet for navigation controls
            NavigationBottomSheet(
                navigationState = navigationState,
                pickupLocation = pickupLocation,
                destinationLocation = destinationLocation,
                onPickupClick = {
                    // In a real app, show location picker or search
                    // For demo, we'll use a predefined location
                    viewModel.setPickupLocation(
                        Location(28.6139, 77.2090, "Connaught Place, New Delhi")
                    )
                },
                onDestinationClick = {
                    // In a real app, show location picker or search
                    // For demo, we'll use a predefined location
                    viewModel.setDestinationLocation(
                        Location(28.5355, 77.3910, "Noida City Centre")
                    )
                },
                onStartNavigation = {
                    viewModel.startNavigation()
                },
                onCancel = {
                    viewModel.reset()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
