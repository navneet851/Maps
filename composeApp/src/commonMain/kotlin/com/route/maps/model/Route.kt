package com.route.maps.model

import kotlinx.serialization.Serializable

/**
 * Represents a route between two locations
 */
@Serializable
data class Route(
    val origin: Location,
    val destination: Location,
    val polyline: List<Location> = emptyList(),
    val distance: String = "",
    val duration: String = "",
    val distanceMeters: Int = 0,
    val durationSeconds: Int = 0
)
