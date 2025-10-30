package com.route.maps.service

import com.route.maps.model.Location
import com.route.maps.model.Route
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Service for interacting with Google Directions API
 * Note: In production, API key should be securely stored and not hardcoded
 */
class DirectionsService(private val apiKey: String = "") {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Fetches route between two locations
     */
    suspend fun getRoute(origin: Location, destination: Location): Result<Route> {
        return try {
            if (apiKey.isEmpty()) {
                // Return mock route for demo purposes when API key is not provided
                Result.success(createMockRoute(origin, destination))
            } else {
                val originStr = "${origin.latitude},${origin.longitude}"
                val destinationStr = "${destination.latitude},${destination.longitude}"
                
                val response: DirectionsResponse = client.get("https://maps.googleapis.com/maps/api/directions/json") {
                    parameter("origin", originStr)
                    parameter("destination", destinationStr)
                    parameter("key", apiKey)
                }.body()

                if (response.status == "OK" && response.routes.isNotEmpty()) {
                    val route = response.routes[0]
                    val leg = route.legs[0]
                    
                    val polylinePoints = decodePolyline(route.overviewPolyline.points)
                    
                    Result.success(
                        Route(
                            origin = origin,
                            destination = destination,
                            polyline = polylinePoints,
                            distance = leg.distance.text,
                            duration = leg.duration.text,
                            distanceMeters = leg.distance.value,
                            durationSeconds = leg.duration.value
                        )
                    )
                } else {
                    Result.failure(Exception("Failed to fetch route: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            // Fallback to mock route on error
            Result.success(createMockRoute(origin, destination))
        }
    }

    /**
     * Creates a mock route for demonstration purposes
     */
    private fun createMockRoute(origin: Location, destination: Location): Route {
        // Create a simple straight line between origin and destination
        val polyline = listOf(origin, destination)
        
        // Calculate approximate distance (simple straight line)
        val distance = calculateDistance(origin, destination)
        val distanceKm = distance / 1000.0
        val estimatedTimeMinutes = (distanceKm / 30.0 * 60).toInt() // Assuming 30 km/h average
        
        return Route(
            origin = origin,
            destination = destination,
            polyline = polyline,
            distance = String.format("%.1f km", distanceKm),
            duration = "$estimatedTimeMinutes min",
            distanceMeters = distance.toInt(),
            durationSeconds = estimatedTimeMinutes * 60
        )
    }

    /**
     * Calculates distance between two locations in meters using Haversine formula
     */
    private fun calculateDistance(loc1: Location, loc2: Location): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
        val lat1 = Math.toRadians(loc1.latitude)
        val lat2 = Math.toRadians(loc2.latitude)

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2) *
                kotlin.math.cos(lat1) * kotlin.math.cos(lat2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Decodes Google's encoded polyline format
     */
    private fun decodePolyline(encoded: String): List<Location> {
        val poly = mutableListOf<Location>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(Location(lat / 1E5, lng / 1E5))
        }

        return poly
    }

    fun close() {
        client.close()
    }
}

// Data classes for Google Directions API response
@Serializable
private data class DirectionsResponse(
    val routes: List<DirectionRoute>,
    val status: String
)

@Serializable
private data class DirectionRoute(
    val legs: List<RouteLeg>,
    @Serializable(with = OverviewPolylineSerializer::class)
    val overviewPolyline: OverviewPolyline
)

@Serializable
private data class RouteLeg(
    val distance: Distance,
    val duration: Duration
)

@Serializable
private data class Distance(
    val text: String,
    val value: Int
)

@Serializable
private data class Duration(
    val text: String,
    val value: Int
)

@Serializable
private data class OverviewPolyline(
    val points: String
)

// Custom serializer for nested structure
private object OverviewPolylineSerializer : kotlinx.serialization.KSerializer<OverviewPolyline> {
    override val descriptor = kotlinx.serialization.descriptors.buildClassSerialDescriptor("OverviewPolyline")
    
    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: OverviewPolyline) {
        encoder.encodeString(value.points)
    }
    
    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): OverviewPolyline {
        return OverviewPolyline(decoder.decodeString())
    }
}
