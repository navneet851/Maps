package com.route.maps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.route.maps.model.Location
import com.route.maps.model.Route
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.*

/**
 * iOS implementation using MapKit
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView(
    currentLocation: Location,
    pickupLocation: Location?,
    destinationLocation: Location?,
    route: Route?,
    onMapClick: (Location) -> Unit,
    modifier: Modifier
) {
    UIKitView(
        factory = {
            val mapView = MKMapView()
            mapView.showsUserLocation = true
            
            // Set initial region
            val center = CLLocationCoordinate2DMake(
                currentLocation.latitude,
                currentLocation.longitude
            )
            val span = MKCoordinateSpanMake(0.05, 0.05)
            val region = MKCoordinateRegionMake(center, span)
            mapView.setRegion(region, animated = false)
            
            mapView
        },
        update = { mapView ->
            // Remove existing annotations and overlays
            mapView.removeAnnotations(mapView.annotations ?: emptyList())
            mapView.removeOverlays(mapView.overlays ?: emptyList())
            
            // Add pickup location marker
            pickupLocation?.let { location ->
                val annotation = MKPointAnnotation()
                annotation.coordinate = CLLocationCoordinate2DMake(
                    location.latitude,
                    location.longitude
                )
                annotation.title = "Pickup"
                annotation.subtitle = location.address
                mapView.addAnnotation(annotation)
            }
            
            // Add destination marker
            destinationLocation?.let { location ->
                val annotation = MKPointAnnotation()
                annotation.coordinate = CLLocationCoordinate2DMake(
                    location.latitude,
                    location.longitude
                )
                annotation.title = "Destination"
                annotation.subtitle = location.address
                mapView.addAnnotation(annotation)
            }
            
            // Draw route polyline
            route?.let { calculatedRoute ->
                if (calculatedRoute.polyline.isNotEmpty()) {
                    val coordinates = calculatedRoute.polyline.map {
                        CLLocationCoordinate2DMake(it.latitude, it.longitude)
                    }.toTypedArray()
                    
                    val polyline = MKPolyline.polylineWithCoordinates(
                        coordinates.refTo(0),
                        coordinates.size.toULong()
                    )
                    mapView.addOverlay(polyline)
                    
                    // Adjust map to show entire route
                    mapView.setVisibleMapRect(
                        polyline.boundingMapRect,
                        edgePadding = platform.UIKit.UIEdgeInsetsMake(50.0, 50.0, 50.0, 50.0),
                        animated = true
                    )
                }
            }
        },
        modifier = modifier
    )
}
