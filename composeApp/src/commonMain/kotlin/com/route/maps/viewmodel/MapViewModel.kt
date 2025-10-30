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
class MapViewModel : ViewModel() {
    
    private val directionsService = DirectionsService()
    
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    private val _pickupLocation = MutableStateFlow<Location?>(null)
    val pickupLocation: StateFlow<Location?> = _pickupLocation.asStateFlow()
    
    private val _destinationLocation = MutableStateFlow<Location?>(null)
    val destinationLocation: StateFlow<Location?> = _destinationLocation.asStateFlow()
    
    private val _currentLocation = MutableStateFlow(Location.DEFAULT)
    val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
        }
    }

    /**
     * Cancels the current navigation
     */
    fun cancelNavigation() {
        _navigationState.value = NavigationState.Idle
        _pickupLocation.value = null
        _destinationLocation.value = null
    }

    /**
     * Resets the navigation state
     */
    fun reset() {
        _navigationState.value = NavigationState.Idle
        _pickupLocation.value = null
        _destinationLocation.value = null
    }

    override fun onCleared() {
        super.onCleared()
        directionsService.close()
    }
}
