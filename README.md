# Maps - Compose Multiplatform Navigation App

A Rapido-like maps navigation application built with Kotlin Multiplatform, featuring route planning and turn-by-turn navigation for both Android and iOS.

## Features

- 🗺️ **Interactive Maps**: Google Maps on Android, Apple MapKit on iOS
- 📍 **Location Selection**: Intuitive pickup and destination selection
- 🛣️ **Route Calculation**: Real-time route calculation with distance and time
- 🧭 **Navigation**: Turn-by-turn navigation interface
- 🎨 **Rapido-like UI**: Clean, modern interface inspired by Rapido
- 📱 **Cross-platform**: Shared business logic, native platform features
- ⚡ **Efficient**: Well-structured, error-prone architecture using MVVM

## Architecture

The project follows Clean Architecture principles with MVVM pattern:

```
composeApp/
├── commonMain/           # Shared code
│   ├── model/           # Data models (Location, Route, NavigationState)
│   ├── viewmodel/       # Business logic (MapViewModel)
│   ├── service/         # API services (DirectionsService)
│   └── ui/              # Shared UI components
├── androidMain/         # Android-specific code
│   └── ui/             # Google Maps implementation
└── iosMain/            # iOS-specific code
    └── ui/             # MapKit implementation
```

### Key Components

1. **Models**: Serializable data classes for Location, Route, and NavigationState
2. **ViewModel**: `MapViewModel` manages state and coordinates between UI and services
3. **Services**: `DirectionsService` handles route calculation with Google Directions API
4. **UI Components**: 
   - `NavigationBottomSheet`: Rapido-inspired bottom sheet for route planning
   - `MapView`: Platform-specific map implementations (expect/actual)

## Technology Stack

- **Kotlin Multiplatform**: Shared business logic
- **Compose Multiplatform**: UI framework
- **Google Maps SDK**: Android maps
- **MapKit**: iOS maps
- **Ktor**: HTTP client for API calls
- **Kotlin Coroutines**: Asynchronous programming
- **StateFlow**: Reactive state management

## Getting Started

### Prerequisites

- **Android Studio**: Latest stable version
- **Xcode**: 15.0 or later (for iOS development)
- **JDK**: 17 or later
- **Google Maps API Key**: (Optional for demo, required for production)

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/navneet851/Maps.git
cd Maps
```

2. **Run without API keys** (Demo mode)
   - The app works out of the box with mock routing
   - Maps display with basic features (no API key required)

3. **Add API keys for production** (See [API_KEYS.md](API_KEYS.md))
   - Get keys from [Google Cloud Console](https://console.cloud.google.com/)
   - Follow the detailed guide in `API_KEYS.md`

### Build and Run Android Application

Build from terminal:
```bash
./gradlew :composeApp:assembleDebug
```

Or use Android Studio:
1. Open the project in Android Studio
2. Select "composeApp" configuration
3. Click Run

### Build and Run iOS Application

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select a simulator or device
3. Click Run

Or from terminal:
```bash
cd iosApp
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug
```

## Usage

### Basic Flow

1. **Launch the app** - Map displays with your current location
2. **Tap the map button (🗺️)** - Start route planning
3. **Select pickup location** - Tap "Pickup Location" or click on map
4. **Select destination** - Tap "Destination" or click on map
5. **View route** - See distance, duration, and route on map
6. **Start navigation** - Tap "Start Navigation" to begin

### Demo Mode (Without API Keys)

- ✅ View maps on both platforms
- ✅ Select locations by tapping map
- ✅ See UI for route planning
- ✅ Get mock route calculations (straight line)
- ❌ Real route calculations require API key

### With API Keys

- ✅ All demo features
- ✅ Real route calculation via Google Directions API
- ✅ Accurate distance and time estimates
- ✅ Proper route polylines following roads

## Project Structure

This is a Kotlin Multiplatform project targeting Android and iOS.

* `/composeApp` - Shared code across platforms
  * `commonMain/kotlin` - Code common for all targets
  * `androidMain/kotlin` - Android-specific implementations
  * `iosMain/kotlin` - iOS-specific implementations

* `/iosApp` - iOS application entry point
  * Contains SwiftUI wrapper for Compose content
  * Platform-specific configurations

## Configuration Files

- `gradle/libs.versions.toml` - Dependency versions
- `composeApp/build.gradle.kts` - Module configuration
- `API_KEYS.md` - Detailed API key setup guide
- `.gitignore` - Excludes sensitive files from version control

## Best Practices Implemented

✅ **Separation of Concerns**: Clear separation between UI, business logic, and data layers
✅ **Platform-specific Code**: Using expect/actual for platform implementations
✅ **State Management**: Reactive state management with StateFlow
✅ **Error Handling**: Comprehensive error handling with fallbacks
✅ **Security**: API keys excluded from version control
✅ **Code Reusability**: Maximum code sharing between platforms
✅ **Clean Architecture**: MVVM pattern with clear dependencies
✅ **Type Safety**: Leveraging Kotlin's type system
✅ **Null Safety**: Proper handling of nullable values
✅ **Coroutines**: Non-blocking async operations

## Screenshots

### Android
- Interactive Google Maps with markers
- Bottom sheet navigation UI
- Route visualization with polylines

### iOS  
- Native MapKit integration
- Seamless SwiftUI integration
- Same UI/UX as Android

## API Documentation

See [API_KEYS.md](API_KEYS.md) for detailed information about:
- Obtaining Google Maps API keys
- Configuring keys for Android and iOS
- Security best practices
- Environment-specific configuration
- Troubleshooting

## Dependencies

### Common
- Compose Multiplatform
- Kotlin Coroutines
- Kotlinx Serialization
- Ktor Client
- Lifecycle ViewModel

### Android
- Google Maps Compose
- Play Services Maps
- Play Services Location
- Ktor Client Android

### iOS
- Ktor Client Darwin
- MapKit (system framework)

## Contributing

This project demonstrates best practices for:
- Kotlin Multiplatform development
- Compose Multiplatform UI
- Platform-specific implementations
- Maps integration
- State management
- Clean architecture

## License

This project is available for educational and demonstration purposes.

## Learn More

- [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Google Maps Platform](https://developers.google.com/maps)
- [MapKit (Apple)](https://developer.apple.com/documentation/mapkit)

## Support

For issues, questions, or contributions, please open an issue on GitHub.

---

Built with ❤️ using Kotlin Multiplatform and Compose Multiplatform
