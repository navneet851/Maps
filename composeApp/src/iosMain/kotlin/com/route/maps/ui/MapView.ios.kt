package com.route.maps.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.route.maps.model.Location
import com.route.maps.model.Route
import kotlinx.cinterop.*
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.*
import platform.UIKit.*
import platform.darwin.NSObject

/**
 * iOS implementation using MapKit
 */
@OptIn(ExperimentalForeignApi::class)
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
    // Create delegate instance that will be reused
    val mapDelegate = remember { MapViewDelegate(routeProgress) }
    
    UIKitView(
        factory = {
            val mapView = MKMapView()
            mapView.showsUserLocation = false
            mapView.delegate = mapDelegate
            
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
            // Update delegate's route progress
            mapDelegate.routeProgress = routeProgress
            
            // Remove existing annotations and overlays
            mapView.annotations?.let { annotations ->
                mapView.removeAnnotations(annotations as List<*>)
            }
            mapView.overlays?.let { overlays ->
                mapView.removeOverlays(overlays as List<*>)
            }
            
            // Add current location marker (only if no driver)
            if (driverLocation == null) {
                val annotation = MKPointAnnotation()
                annotation.coordinate = CLLocationCoordinate2DMake(
                    currentLocation.latitude,
                    currentLocation.longitude
                )
                annotation.title = "Current Location"
                mapView.addAnnotation(annotation)
            }
            
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
            
            // Add driver location marker (bike/vehicle icon)
            driverLocation?.let { location ->
                val annotation = MKPointAnnotation()
                annotation.coordinate = CLLocationCoordinate2DMake(
                    location.latitude,
                    location.longitude
                )
                annotation.title = "Driver"
                annotation.subtitle = "On the way"
                mapView.addAnnotation(annotation)
            }
            
            // Draw route polylines
            route?.let { calculatedRoute ->
                if (calculatedRoute.polyline.isNotEmpty()) {
                    val routeCoordinates = calculatedRoute.polyline.map {
                        CLLocationCoordinate2DMake(it.latitude, it.longitude)
                    }
                    
                    if (routeProgress > 0f && routeProgress < 1f) {
                        // Split route into covered and remaining
                        val splitIndex = (routeCoordinates.size * routeProgress).toInt()
                            .coerceIn(1, routeCoordinates.size - 1)
                        
                        // Covered route (gray) - use tag to identify
                        if (splitIndex > 0) {
                            val coveredCoords = routeCoordinates.subList(0, splitIndex + 1).toTypedArray()
                            val coveredPolyline = MKPolyline.polylineWithCoordinates(
                                coveredCoords.refTo(0),
                                coveredCoords.size.toULong()
                            )
                            mapView.addOverlay(coveredPolyline)
                        }
                        
                        // Remaining route (black)
                        if (splitIndex < routeCoordinates.size - 1) {
                            val remainingCoords = routeCoordinates.subList(splitIndex, routeCoordinates.size).toTypedArray()
                            val remainingPolyline = MKPolyline.polylineWithCoordinates(
                                remainingCoords.refTo(0),
                                remainingCoords.size.toULong()
                            )
                            mapView.addOverlay(remainingPolyline)
                        }
                    } else {
                        // Full route
                        val coords = routeCoordinates.toTypedArray()
                        val polyline = MKPolyline.polylineWithCoordinates(
                            coords.refTo(0),
                            coords.size.toULong()
                        )
                        mapView.addOverlay(polyline)
                    }
                    
                    // Adjust map to show entire route
                    if (routeCoordinates.isNotEmpty()) {
                        val firstPolyline = MKPolyline.polylineWithCoordinates(
                            routeCoordinates.toTypedArray().refTo(0),
                            routeCoordinates.size.toULong()
                        )
                        mapView.setVisibleMapRect(
                            firstPolyline.boundingMapRect,
                            edgePadding = UIEdgeInsetsMake(50.0, 50.0, 50.0, 50.0),
                            animated = true
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}

/**
 * MapView delegate to handle polyline rendering
 */
@OptIn(ExperimentalForeignApi::class)
private class MapViewDelegate(var routeProgress: Float) : NSObject(), MKMapViewDelegateProtocol {
    private var overlayIndex = 0
    
    override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
        val overlay = rendererForOverlay
        
        if (overlay is MKPolyline) {
            val renderer = MKPolylineRenderer(overlay)
            
            // Determine color based on order (first overlay is covered, second is remaining)
            // This is a simple approach; in production you might use custom overlay types
            if (routeProgress > 0f && routeProgress < 1f) {
                // Alternate between gray (covered) and black (remaining)
                val color = if (overlayIndex % 2 == 0) {
                    UIColor.grayColor // Covered
                } else {
                    UIColor.blackColor // Remaining
                }
                overlayIndex++
                renderer.strokeColor = color
            } else {
                // Single route - blue or gray based on completion
                renderer.strokeColor = if (routeProgress >= 1f) {
                    UIColor.grayColor
                } else {
                    UIColor.systemBlueColor
                }
            }
            
            renderer.lineWidth = 5.0
            return renderer
        }
        
        return MKOverlayRenderer(rendererForOverlay)
    }
    
    override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
        val annotation = viewForAnnotation
        
        // Don't customize user location annotation
        if (annotation is MKUserLocation) {
            return null
        }
        
        val identifier = "CustomPin"
        var annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(identifier)
        
        if (annotationView == null) {
            annotationView = MKMarkerAnnotationView(annotation, identifier)
            annotationView.canShowCallout = true
        } else {
            annotationView.annotation = annotation
        }
        
        // Customize marker colors based on title
        (annotationView as? MKMarkerAnnotationView)?.let { markerView ->
            when (annotation.title) {
                "Pickup" -> markerView.markerTintColor = UIColor.greenColor
                "Destination" -> markerView.markerTintColor = UIColor.redColor
                "Driver" -> {
                    markerView.markerTintColor = UIColor.orangeColor
                    markerView.glyphText = "🏍️" // Bike emoji for driver
                }
                else -> markerView.markerTintColor = UIColor.blueColor
            }
        }
        
        return annotationView
    }
}
