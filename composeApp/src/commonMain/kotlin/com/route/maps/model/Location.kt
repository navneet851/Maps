package com.route.maps.model

import kotlinx.serialization.Serializable

/**
 * Represents a geographic location with latitude and longitude
 */
@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
) {
    companion object {
        // Default location (New Delhi, India)
        val DEFAULT = Location(28.6139, 77.2090, "New Delhi, India")
    }
}
