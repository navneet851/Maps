# iOS Build Instructions

This document explains how to fix the "unable to find ComposeApp in ContentView imports" error and run the iOS app.

## The Issue

The iOS app imports the `ComposeApp` framework which is generated from the Kotlin Multiplatform code. This framework needs to be built before the iOS app can run.

## Solution: Build the Framework

### Option 1: Build from Terminal (Recommended)

1. **Navigate to project root**:
```bash
cd /path/to/Maps
```

2. **Build the iOS framework**:
```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

Or for physical device:
```bash
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

3. **Open Xcode**:
```bash
open iosApp/iosApp.xcodeproj
```

4. **Select target and run**:
   - Choose iPhone simulator or device
   - Click Run (▶️)

### Option 2: Build from Android Studio

1. **Sync Gradle**:
   - Open the project in Android Studio
   - Click "Sync Project with Gradle Files"

2. **Run iOS target**:
   - Select "iosApp" from the run configurations dropdown
   - Click Run

### Option 3: Build from Xcode with Script

The framework should build automatically when you run from Xcode, but if it doesn't:

1. **Clean build folder**: Product → Clean Build Folder (⇧⌘K)
2. **Build**: Product → Build (⌘B)
3. **Run**: Product → Run (⌘R)

## Verifying the Build

After building, the framework should be located at:
```
composeApp/build/bin/iosSimulatorArm64/debugFramework/ComposeApp.framework
```

## Common Issues and Solutions

### Issue: "Module 'ComposeApp' not found"

**Solution**: Clean and rebuild the framework:
```bash
./gradlew clean
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Issue: Wrong Architecture

If running on Intel Mac simulator:
```bash
./gradlew :composeApp:linkDebugFrameworkIosX64
```

If running on Apple Silicon Mac simulator:
```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Issue: Framework Not Linking

1. Open `iosApp.xcodeproj` in Xcode
2. Select the "iosApp" target
3. Go to "Build Phases"
4. Verify "Compile Sources" and "Link Binary With Libraries" are configured
5. Check that the framework path is correct in "Framework Search Paths"

## Full Clean and Rebuild

If all else fails:
```bash
# Clean everything
./gradlew clean
rm -rf composeApp/build
rm -rf iosApp/build

# Rebuild framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Open and run in Xcode
open iosApp/iosApp.xcodeproj
```

## Gradle Tasks Reference

- `linkDebugFrameworkIosArm64` - iOS device (physical iPhone/iPad)
- `linkDebugFrameworkIosSimulatorArm64` - iOS Simulator (Apple Silicon Macs)
- `linkDebugFrameworkIosX64` - iOS Simulator (Intel Macs)
- `linkReleaseFrameworkIosArm64` - Release build for device
- `assembleXCFramework` - Build universal framework for all targets

## What Gets Built

The build process:
1. Compiles Kotlin code to native iOS binaries
2. Creates the `ComposeApp.framework`
3. Includes all shared code (ViewModels, Models, Services)
4. Makes Compose UI available to SwiftUI via `MainViewController`

## Running the App

Once built, the iOS app will:
- ✅ Show MapKit maps
- ✅ Display bottom sheet navigation UI
- ✅ Support location selection
- ✅ Show route visualization
- ✅ Display driver location with bike icon
- ✅ Animate route progress (gray covered, black remaining)

## Development Workflow

For continuous development:

1. **Make changes to Kotlin code**
2. **Rebuild framework**: 
   ```bash
   ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```
3. **Xcode will detect changes automatically**
4. **Run in Xcode**

Or use Android Studio's iOS run configuration which handles framework building automatically.

## CI/CD Integration

For CI/CD pipelines:

```yaml
- name: Build iOS Framework
  run: ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

- name: Build iOS App
  run: |
    cd iosApp
    xcodebuild -project iosApp.xcodeproj \
               -scheme iosApp \
               -configuration Debug \
               -sdk iphonesimulator \
               -destination 'platform=iOS Simulator,name=iPhone 15'
```

## Troubleshooting Checklist

- [ ] Gradle sync completed successfully
- [ ] Framework built without errors
- [ ] Framework exists at expected path
- [ ] Xcode project opened and cleaned
- [ ] Correct simulator/device architecture selected
- [ ] No pending Xcode updates required
- [ ] macOS version supports Xcode version

## Additional Resources

- [Kotlin Multiplatform iOS Integration](https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html)
- [Compose Multiplatform iOS Setup](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-create-first-app.html)
- [Xcode Build Settings](https://developer.apple.com/documentation/xcode)

---

**Need Help?** Open an issue on GitHub with:
- Xcode version
- macOS version  
- Full error message
- Build logs from terminal
