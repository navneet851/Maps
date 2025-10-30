package com.route.maps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.route.maps.model.Location
import com.route.maps.model.NavigationState
import com.route.maps.model.Route
import com.route.maps.service.DirectionsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing map and navigation state
 */
class MapViewModel(apiKey: String = "") : ViewModel() {
    
    private val directionsService = DirectionsService(apiKey)
    
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    private val _pickupLocation = MutableStateFlow<Location?>(null)
    val pickupLocation: StateFlow<Location?> = _pickupLocation.asStateFlow()
    
    private val _destinationLocation = MutableStateFlow<Location?>(null)
    val destinationLocation: StateFlow<Location?> = _destinationLocation.asStateFlow()
    
    private val _currentLocation = MutableStateFlow(Location.DEFAULT)
    val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()
    
    // Driver location (separate from user current location for Rapido-like experience)
    private val _driverLocation = MutableStateFlow<Location?>(null)
    val driverLocation: StateFlow<Location?> = _driverLocation.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Track how much of the route has been covered (0.0 to 1.0)
    private val _routeProgress = MutableStateFlow(0f)
    val routeProgress: StateFlow<Float> = _routeProgress.asStateFlow()

    /**
     * Sets the pickup location
     */
    fun setPickupLocation(location: Location) {
        _pickupLocation.value = location
        _navigationState.value = NavigationState.SelectingDestination
    }

    /**
     * Sets the destination location and calculates route
     */
    fun setDestinationLocation(location: Location) {
        _destinationLocation.value = location
        calculateRoute()
    }

    /**
     * Updates the current user location
     */
    fun updateCurrentLocation(location: Location) {
        _currentLocation.value = location
    }
    
    /**
     * Updates the driver location during navigation
     */
    fun updateDriverLocation(location: Location) {
        _driverLocation.value = location
        // Update route progress based on driver location
        updateRouteProgress(location)
    }
    
    /**
     * Updates route progress based on driver location
     */
    private fun updateRouteProgress(driverLocation: Location) {
        val state = _navigationState.value
        if (state is NavigationState.Navigating) {
            val route = state.route
            if (route.polyline.isNotEmpty()) {
                // Calculate progress based on distance from start
                val totalDistance = route.distanceMeters.toFloat()
                val startLocation = route.origin
                val distanceTraveled = calculateDistanceMeters(startLocation, driverLocation)
                _routeProgress.value = (distanceTraveled / totalDistance).coerceIn(0f, 1f)
            }
        }
    }
    
    /**
     * Calculate distance in meters between two locations
     */
    private fun calculateDistanceMeters(loc1: Location, loc2: Location): Float {
        val earthRadius = 6371000f // meters
        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
        val lat1 = Math.toRadians(loc1.latitude)
        val lat2 = Math.toRadians(loc2.latitude)

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2) *
                kotlin.math.cos(lat1) * kotlin.math.cos(lat2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return (earthRadius * c).toFloat()
    }

    /**
     * Calculates the route between pickup and destination
     */
    fun calculateRoute() {
        val pickup = _pickupLocation.value ?: _currentLocation.value
        val destination = _destinationLocation.value ?: return
        
        _isLoading.value = true
        
        viewModelScope.launch {
            directionsService.getRoute(pickup, destination).fold(
                onSuccess = { route ->
                    _navigationState.value = NavigationState.RouteCalculated(route)
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _navigationState.value = NavigationState.Error(
                        error.message ?: "Failed to calculate route"
                    )
                    _isLoading.value = false
                }
            )
        }
    }

    /**
     * Starts navigation with the calculated route
     */
    fun startNavigation() {
        val state = _navigationState.value
        if (state is NavigationState.RouteCalculated) {
            _navigationState.value = NavigationState.Navigating(state.route)
            // Initialize driver at pickup location
            _driverLocation.value = _pickupLocation.value ?: _currentLocation.value
            _routeProgress.value = 0f
        }
    }

    /**
     * Cancels the current navigation
     */
    fun cancelNavigation() {
        _navigationState.value = NavigationState.Idle
        _pickupLocation.value = null
        _destinationLocation.value = null
        _driverLocation.value = null
        _routeProgress.value = 0f
    }

    /**
     * Resets the navigation state
     */
    fun reset() {
        _navigationState.value = NavigationState.Idle
        _pickupLocation.value = null
        _destinationLocation.value = null
        _driverLocation.value = null
        _routeProgress.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        directionsService.close()
    }
}
