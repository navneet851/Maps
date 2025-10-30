# 🔑 WHERE TO PUT API KEYS - Quick Reference

This is a **quick reference guide** for adding API keys to your Maps application. For detailed information, see [API_KEYS.md](API_KEYS.md).

---

## 📱 ANDROID

### Google Maps API Key (for Map Display)

**File**: `composeApp/src/androidMain/AndroidManifest.xml`

**Line 17** - Replace `YOUR_GOOGLE_MAPS_API_KEY` with your actual API key:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="PUT_YOUR_ANDROID_API_KEY_HERE" />
```

### Google Directions API Key (for Route Calculation)

**File**: `composeApp/src/commonMain/kotlin/com/route/maps/App.kt`

**Line ~20** - Pass your API key to the ViewModel:

```kotlin
val viewModel: MapViewModel = viewModel { 
    MapViewModel(apiKey = "PUT_YOUR_DIRECTIONS_API_KEY_HERE") 
}
```

**Then update** `composeApp/src/commonMain/kotlin/com/route/maps/viewmodel/MapViewModel.kt`

**Line 17** - Update constructor to accept API key:

```kotlin
class MapViewModel(apiKey: String = "") : ViewModel() {
    private val directionsService = DirectionsService(apiKey)
    // ... rest of code
}
```

---

## �� iOS

### Apple Maps (MapKit) - ✅ NO API KEY NEEDED!

iOS uses Apple's MapKit which requires **no API key**. It works out of the box!

### Google Directions API Key (for Route Calculation)

**Same as Android** - Update the App.kt and MapViewModel.kt files as shown above.

---

## 🎯 Getting Your API Keys

### Step 1: Go to Google Cloud Console
Visit: https://console.cloud.google.com/

### Step 2: Enable APIs
Enable these APIs in your project:
- ✅ Maps SDK for Android
- ✅ Directions API
- ⚠️ Maps SDK for iOS (optional - only if you want Google Maps instead of Apple Maps)

### Step 3: Create API Keys
1. Go to "Credentials" in Google Cloud Console
2. Click "Create Credentials" → "API Key"
3. Restrict your keys:
   - **Android key**: Restrict by package name + SHA-1
   - **Directions key**: Restrict by API (Directions API only)

### Step 4: Add to Your Code
Follow the instructions above to add keys to the correct files.

---

## 🚀 Quick Start Without API Keys

The app works in **demo mode** without any API keys:

✅ Maps display (basic features)
✅ Location selection
✅ UI interactions
✅ Mock route calculations

To enable **real routing**, add the Directions API key as shown above.

---

## ⚠️ SECURITY WARNING

**NEVER commit API keys to Git!**

The files that might contain API keys are already in `.gitignore`:
- `local.properties`
- `**/config.xml`
- `**/Config.plist`

---

## 📚 For More Information

See the comprehensive guide in [API_KEYS.md](API_KEYS.md) for:
- Detailed setup instructions
- Production configuration best practices
- Environment-specific configuration
- Troubleshooting guide
- Security recommendations

---

## 🔍 Summary Table

| What | Where | Required? |
|------|-------|-----------|
| Android Maps | `AndroidManifest.xml` line 17 | No (works without) |
| iOS Maps | N/A (uses MapKit) | No |
| Directions API | `App.kt` + `MapViewModel.kt` | Yes (for real routes) |

---

**Need help?** Check [API_KEYS.md](API_KEYS.md) or [README.md](README.md)
