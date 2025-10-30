# API Keys Configuration Guide

This guide explains where and how to configure API keys for the Maps application on both Android and iOS platforms.

## Overview

The Maps application uses Google Maps API for displaying maps and calculating routes. You need to obtain API keys from Google Cloud Console to use these services.

## Getting Google Maps API Keys

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the following APIs:
   - **Maps SDK for Android** (for Android app)
   - **Maps SDK for iOS** (for iOS app - optional if using Apple Maps)
   - **Directions API** (for route calculation)
   - **Geocoding API** (optional, for address search)
4. Create credentials:
   - For Android: Create an API Key with Android restrictions
   - For iOS: Create an API Key with iOS restrictions (if using Google Maps)
   - For Directions API: Create an API Key with API restrictions

### Security Best Practices

⚠️ **IMPORTANT**: Never commit API keys to version control!

- Add API key files to `.gitignore`
- Use environment variables for CI/CD
- Restrict API keys by platform and API
- Monitor API usage regularly

## Android Configuration

### Option 1: AndroidManifest.xml (Development/Testing)

**Location**: `composeApp/src/androidMain/AndroidManifest.xml`

Replace `YOUR_GOOGLE_MAPS_API_KEY` with your actual API key:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY" />
```

### Option 2: local.properties (Recommended for Production)

1. **Create/Edit** `local.properties` in the project root:

```properties
MAPS_API_KEY=YOUR_ACTUAL_API_KEY_HERE
```

2. **Update** `composeApp/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        // Read API key from local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }
        
        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("MAPS_API_KEY", "")
    }
}
```

3. **Update** `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

4. **Add to** `.gitignore`:

```
local.properties
```

### For Directions API (Backend)

**Location**: `composeApp/src/commonMain/kotlin/com/route/maps/service/DirectionsService.kt`

When creating the DirectionsService instance, pass your API key:

```kotlin
private val directionsService = DirectionsService(apiKey = "YOUR_DIRECTIONS_API_KEY")
```

**Better approach**: Create a configuration file that reads from resources:

1. Create `composeApp/src/androidMain/res/values/config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_directions_api_key">YOUR_DIRECTIONS_API_KEY</string>
</resources>
```

2. Add to `.gitignore`:
```
**/res/values/config.xml
```

## iOS Configuration

### For MapKit (Apple Maps) - No API Key Needed

iOS uses Apple's MapKit by default, which doesn't require an API key. Location permissions are already configured in `Info.plist`.

### For Google Maps iOS SDK (Optional)

If you want to use Google Maps instead of Apple Maps on iOS:

1. **Install Google Maps iOS SDK** via CocoaPods or SPM

2. **Add to** `iosApp/iosApp/Info.plist`:

```xml
<key>GoogleMapsAPIKey</key>
<string>YOUR_IOS_GOOGLE_MAPS_API_KEY</string>
```

3. **Initialize in** `iosApp/iosApp/iOSApp.swift`:

```swift
import GoogleMaps

@main
struct iOSApp: App {
    init() {
        GMSServices.provideAPIKey("YOUR_IOS_GOOGLE_MAPS_API_KEY")
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### For Directions API

Since Directions API calls are made from shared code, you can:

1. **Create a configuration file** `iosApp/iosApp/Config.plist`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>DirectionsAPIKey</key>
    <string>YOUR_DIRECTIONS_API_KEY</string>
</dict>
</plist>
```

2. **Add to** `.gitignore`:
```
**/Config.plist
```

## Current Implementation (Demo Mode)

The current implementation works **without API keys** for demonstration purposes:

- **Maps**: Uses Google Maps on Android and Apple Maps on iOS (both work without keys for basic features)
- **Directions**: Falls back to mock route calculation when no API key is provided

This allows you to:
- ✅ Test the UI and user flow
- ✅ See map interactions
- ✅ View route visualization
- ❌ Get real route calculations (uses straight-line mock routes)

## Adding Real API Keys

To enable real route calculation:

### Quick Start (Development)

1. Get your Google Directions API key from [Google Cloud Console](https://console.cloud.google.com/)

2. Update `App.kt` to pass the API key:

```kotlin
@Composable
fun App() {
    MaterialTheme {
        // Pass your API key here
        val viewModel: MapViewModel = viewModel { 
            MapViewModel(apiKey = "YOUR_API_KEY_HERE") 
        }
        // ... rest of the code
    }
}
```

3. Update `MapViewModel.kt` constructor:

```kotlin
class MapViewModel(apiKey: String = "") : ViewModel() {
    private val directionsService = DirectionsService(apiKey)
    // ... rest of the code
}
```

### Production Setup

For production, use the build configuration approaches described above to keep API keys secure and out of version control.

## Testing Without API Keys

The app is designed to work in demo mode without API keys:

1. **Android**: Google Maps works for basic display (API key required for advanced features)
2. **iOS**: Apple Maps works completely without any API key
3. **Routing**: Mock routes are generated using straight-line distance calculations

This allows developers to:
- Test the app immediately without setup
- Develop UI and features
- Add real API keys only when needed for production

## Environment-Specific Configuration

### Development
- Use `local.properties` for Android
- Use `Config.plist` for iOS
- Keep keys local, don't commit

### CI/CD
- Use environment variables
- Inject keys during build process
- Example for GitHub Actions:

```yaml
- name: Build Android
  env:
    MAPS_API_KEY: ${{ secrets.MAPS_API_KEY }}
  run: ./gradlew assembleRelease
```

### Production
- Use encrypted key storage
- Implement key rotation strategy
- Monitor API usage and costs

## Troubleshooting

### Android: "Map failed to load"
- Check if API key is correctly set in `AndroidManifest.xml`
- Verify the API key has Android restrictions matching your app's package name and SHA-1
- Ensure "Maps SDK for Android" is enabled in Google Cloud Console

### iOS: Map shows but no locations
- Check `Info.plist` has location permissions
- Request location permissions at runtime
- For Google Maps: Verify API key is set and "Maps SDK for iOS" is enabled

### Directions API not working
- Verify "Directions API" is enabled in Google Cloud Console
- Check API key has proper restrictions
- Monitor quota limits in Google Cloud Console
- Check network connectivity

## Summary

| Platform | Component | API Key Location | Required? |
|----------|-----------|------------------|-----------|
| Android | Maps | `AndroidManifest.xml` or `local.properties` | No (basic features work) |
| Android | Directions | Code configuration | Yes (for real routes) |
| iOS | Maps (MapKit) | Not needed | No |
| iOS | Maps (Google) | `Info.plist` | Yes (if using Google Maps) |
| iOS | Directions | Code configuration | Yes (for real routes) |

## Next Steps

1. ✅ App works in demo mode without keys
2. 📝 Get API keys from Google Cloud Console
3. 🔧 Add keys using methods above
4. 🧪 Test with real routing
5. 🚀 Deploy with proper security measures

For more information, visit:
- [Google Maps Platform Documentation](https://developers.google.com/maps/documentation)
- [Google Cloud Console](https://console.cloud.google.com/)
