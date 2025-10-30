# Rapido-Like Navigation Features

This document describes the new Rapido-inspired navigation features implemented in the Maps app.

## 🎯 Key Features

### 1. Source and Destination Selection
- **Pickup Location** (Source): Marked with green pin (📍)
- **Destination**: Marked with red pin (🎯)
- Tap locations on map or use bottom sheet to select

### 2. Driver Location Tracking
- **Driver Marker**: Orange pin with bike emoji 🏍️
- **Real-time Updates**: Driver location updates as navigation progresses
- **Auto-simulation**: For demo, driver automatically moves along route

### 3. Route Progress Visualization

#### Before Navigation Starts
- Route shown in **blue** color
- All waypoints visible
- Distance and duration displayed

#### During Navigation
- **Covered Route**: Gray color (#808080)
  - Shows path already traveled
  - Updates as driver moves
- **Remaining Route**: Black/dark color (#2C2C2C)
  - Shows path yet to travel
  - Gets shorter as driver approaches destination

### 4. Navigation Flow

```
1. Idle State
   └─> Tap map button 🗺️
   
2. Select Pickup
   └─> Tap location or use bottom sheet
   
3. Select Destination  
   └─> Tap location or use bottom sheet
   └─> Route automatically calculates
   
4. View Route
   └─> See distance, duration, route preview
   └─> Tap "Start Navigation"
   
5. Active Navigation
   └─> Driver appears at pickup location
   └─> Route splits into covered (gray) + remaining (black)
   └─> Driver moves along route
   └─> Progress updates in real-time
   └─> Tap "End Navigation" to stop
```

## 🎨 Visual Design

### Marker Colors

| Element | Android | iOS | Description |
|---------|---------|-----|-------------|
| Current Location | Blue | Blue | User's current position |
| Pickup | Green | Green | Starting point |
| Destination | Red | Red | End point |
| Driver | Orange | Orange | Driver/vehicle position |

### Route Colors

| State | Color | Hex | Meaning |
|-------|-------|-----|---------|
| Planned | Blue | #4285F4 | Route before starting |
| Covered | Gray | #808080 | Already traveled |
| Remaining | Black | #2C2C2C | Yet to travel |
| Completed | Gray | #808080 | After reaching destination |

## 🚗 Driver Simulation

During navigation, the app simulates driver movement:

1. **Start**: Driver appears at pickup location
2. **Movement**: Driver moves from waypoint to waypoint
3. **Speed**: Updates every 2 seconds (configurable)
4. **Progress**: Route gradually turns from black → gray
5. **End**: Driver reaches destination

### Customization

In `App.kt`, you can adjust the simulation:

```kotlin
LaunchedEffect(navigationState) {
    if (navigationState is NavigationState.Navigating) {
        // ... driver simulation code
        delay(2000) // Change this value to speed up/slow down
    }
}
```

## 📱 Platform Differences

### Android (Google Maps)
- Custom marker icons using `BitmapDescriptorFactory`
- Smooth camera animations
- Polyline width: 12dp
- Marker rotation support (for future compass feature)

### iOS (MapKit)
- Custom marker colors using `MKMarkerAnnotationView`
- Bike emoji (🏍️) as driver glyph
- Polyline width: 5pt
- Native iOS map controls

## 🎭 Real-World Usage

To integrate with real GPS:

### Android

```kotlin
// In your Activity/Composable
val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

fusedLocationClient.lastLocation.addOnSuccessListener { location ->
    location?.let {
        viewModel.updateDriverLocation(
            Location(it.latitude, it.longitude)
        )
    }
}
```

### iOS

```swift
// In your Swift code
let locationManager = CLLocationManager()

func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    if let location = locations.last {
        // Update driver location through ViewModel
    }
}
```

## 📊 Progress Calculation

Route progress is calculated based on:

1. **Total Distance**: From pickup to destination
2. **Distance Traveled**: From pickup to current driver location
3. **Progress**: `distanceTraveled / totalDistance` (0.0 to 1.0)

Formula used (Haversine):
```kotlin
fun calculateDistance(loc1: Location, loc2: Location): Float {
    val earthRadius = 6371000f // meters
    // ... Haversine formula implementation
}
```

## 🎯 Rapido-Like Experience

Our implementation matches Rapido's key features:

- ✅ Clear pickup/destination markers
- ✅ Distinct driver icon (bike emoji)
- ✅ Visual route progress
- ✅ Real-time driver tracking
- ✅ Smooth animations
- ✅ Bottom sheet UI
- ✅ Distance and time estimates
- ✅ Navigation controls

## 🔄 State Management

Navigation states:

1. **Idle**: No active navigation
2. **SelectingPickup**: Waiting for pickup selection
3. **SelectingDestination**: Waiting for destination
4. **RouteCalculated**: Route ready, not started
5. **Navigating**: Active navigation with driver
6. **Error**: Something went wrong

## 💡 Tips

### For Faster Development
- Use predefined locations in demo mode
- Adjust simulation speed for testing
- Use mock routes without API key

### For Production
- Integrate real GPS location services
- Use actual API keys for accurate routes
- Implement background location tracking
- Add notification for navigation updates
- Handle location permissions properly

## 🐛 Known Limitations

1. **Simulation Only**: Current implementation simulates driver movement
2. **Simple Progress**: Uses straight-line distance, not road distance
3. **No Turn-by-Turn**: Voice guidance not implemented
4. **No Rerouting**: If driver goes off-route, no automatic reroute

## 🚀 Future Enhancements

Possible improvements:

- [ ] Real GPS integration
- [ ] Voice turn-by-turn navigation
- [ ] Traffic-aware routing
- [ ] Multiple route options
- [ ] Driver compass heading
- [ ] ETA updates during navigation
- [ ] Speed limit indicators
- [ ] Lane guidance
- [ ] Offline maps

## 📸 Screenshots

### Android
- Pickup (green) and destination (red) markers visible
- Driver (orange) with rotation support
- Route split into gray (covered) and black (remaining)

### iOS
- Native MapKit appearance
- Bike emoji (🏍️) for driver
- Custom marker colors
- Smooth polyline rendering

---

**This implementation provides a complete Rapido-like navigation experience with all the visual features you requested!** 🎉
