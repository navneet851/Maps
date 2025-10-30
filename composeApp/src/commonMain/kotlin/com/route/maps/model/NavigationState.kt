package com.route.maps.model

/**
 * Represents the current state of navigation
 */
sealed class NavigationState {
    object Idle : NavigationState()
    object SelectingPickup : NavigationState()
    object SelectingDestination : NavigationState()
    data class RouteCalculated(val route: Route) : NavigationState()
    data class Navigating(val route: Route) : NavigationState()
    data class Error(val message: String) : NavigationState()
}
