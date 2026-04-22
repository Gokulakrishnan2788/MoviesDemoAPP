# DeepLink Feature Documentation

## Overview

The DeepLink feature enables the MoviesDemoAPP to handle external deep links, allowing users to navigate directly to specific screens within the app from external sources such as web browsers, other apps, or push notifications. This feature uses Server-Driven UI (SDUI) architecture to dynamically render screens based on configuration loaded from a backend.

## Architecture

### Core Components

#### 1. DeepLinkActivity
- **Purpose**: Entry point for handling deep link intents
- **Location**: `app/src/main/java/com/example/moviesdemoapp/app/DeepLinkActivity.kt`
- **Responsibilities**:
  - Parse incoming deep link URIs
  - Extract screen name and parameters
  - Initialize the UI with DeepLinkScreen
  - Handle navigation to MainActivity when internal navigation occurs

#### 2. DeepLinkScreen (Composable)
- **Purpose**: Main UI component for rendering deep link screens
- **Location**: `feature/deeplink/src/main/java/com/example/deeplink/DeepLinkScreen.kt`
- **Features**:
  - Uses SDUI renderer for dynamic screen rendering
  - Supports both NavController-based and callback-based navigation
  - Handles loading states and error conditions

#### 3. DeepLinkScreenViewModel
- **Purpose**: Business logic and state management
- **Location**: `feature/deeplink/src/main/java/com/example/deeplink/DeepLinkScreenViewModel.kt`
- **Responsibilities**:
  - Load screen configurations from ScreenRepository
  - Execute data sources for dynamic content
  - Handle user actions and navigation effects
  - Manage loading and error states

#### 4. Model Classes
- **Location**: `feature/deeplink/src/main/java/com/example/deeplink/model/DeepLinkingContract.kt`
- **Components**:
  - `DeepLinkPageState`: UI state containing screen model, loading status, and data
  - `DeepLinkPageIntent`: User actions and system events
  - `DeepLinkPageEffect`: Side effects like navigation

## Implementation Details

### Deep Link URI Structure

The app supports deep links with the following format:
```
https://myapp.com/screen/{screenName}?symbol={parameter}
```

**Examples:**
- `https://myapp.com/screen/banking` - Opens banking screen
- `https://myapp.com/screen/movies?symbol=tt0111161` - Opens movies screen with specific symbol

### AndroidManifest Configuration

```xml
<activity android:name=".app.DeepLinkActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="myapp.com"
            android:pathPrefix="/screen" />
    </intent-filter>
</activity>
```

### Navigation Flow

1. **External Link Clicked** → Android system launches DeepLinkActivity
2. **DeepLinkActivity** → Parses URI and initializes DeepLinkScreen
3. **DeepLinkScreen** → Loads screen configuration via ViewModel
4. **User Interaction** → Triggers navigation effect
5. **DeepLinkActivity** → Starts MainActivity with route parameter
6. **MainActivity** → Navigates to appropriate screen and graph

### State Management

The feature uses MVI (Model-View-Intent) architecture:

```kotlin
// State
data class DeepLinkPageState(
    val screenModel: ScreenModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val dataMap: Map<String, String> = emptyMap(),
    val listData: Map<String, List<Map<String, String>>> = emptyMap(),
)

// Intents
sealed interface DeepLinkPageIntent {
    data object LoadPersonalDetailMainPage : DeepLinkPageIntent
    data class LoadOtherMainPage(val pageDetail: String) : DeepLinkPageIntent
    data class OnAction(val actionId: String, val params: Map<String, String>) : DeepLinkPageIntent
}

// Effects
sealed interface DeepLinkPageEffect {
    data class Navigate(val route: String) : DeepLinkPageEffect
}
```

## Usage Examples

### Testing Deep Links

#### ADB Command
```bash
# Open banking screen
adb shell am start -a android.intent.action.VIEW -d "https://myapp.com/screen/banking"

# Open movies screen with parameter
adb shell am start -a android.intent.action.VIEW -d "https://myapp.com/screen/movies?symbol=tt0111161"
```

#### From Browser
Simply navigate to the deep link URL in a browser or click a link containing the deep link URL.

#### Programmatic Launch
```kotlin
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myapp.com/screen/banking"))
startActivity(intent)
```

### Supported Screen Types

The DeepLink feature can render any screen defined in the SDUI configuration system, including:
- Banking screens (personal details, address, financial info, etc.)
- Movie screens (list, details, search)
- Any custom screen defined in the backend configuration

## Pros and Cons

### Advantages

#### ✅ **Dynamic Content**
- Screens are rendered based on server configuration
- No app updates required for UI changes
- A/B testing capabilities
- Remote content updates

#### ✅ **Seamless Integration**
- Works with existing navigation system
- Maintains app state and back stack
- Consistent user experience

#### ✅ **Flexible Routing**
- Supports parameterized deep links
- Handles complex navigation flows
- Extensible for new screen types

#### ✅ **Robust Architecture**
- Uses proven MVI pattern
- Proper error handling and loading states
- Dependency injection with Hilt
- Lifecycle-aware components

#### ✅ **Developer Experience**
- Type-safe navigation
- Clear separation of concerns
- Easy to test and maintain
- Reusable components

### Disadvantages

#### ❌ **Complexity**
- Additional architectural layers
- Learning curve for SDUI concepts
- More components to maintain

#### ❌ **Performance Overhead**
- Additional network requests for screen configs
- Runtime screen parsing and rendering
- Potential delays in screen loading

#### ❌ **Debugging Challenges**
- Dynamic content makes debugging harder
- Server-dependent behavior
- Cache invalidation issues

#### ❌ **Limited Offline Support**
- Requires network connectivity for initial load
- Cached content may become stale
- No fallback for server unavailability

## Potential Errors and Troubleshooting

### 1. Hilt Dependency Injection Errors

**Error:**
```
java.lang.IllegalStateException: Given component holder class ... does not implement interface dagger.hilt.internal.GeneratedComponent
```

**Cause:** Missing `@AndroidEntryPoint` annotation on Activity.

**Solution:**
```kotlin
@AndroidEntryPoint
class DeepLinkActivity : ComponentActivity() {
    // ...
}
```

### 2. Navigation Context Issues

**Error:** Crash when trying to use NavController in standalone Activity.

**Cause:** DeepLinkActivity doesn't have navigation context.

**Solution:** Use callback-based navigation instead of NavController:
```kotlin
DeepLinkScreen(
    onNavigate = { route ->
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("route", route)
        }
        startActivity(intent)
        finish()
    }
)
```

### 3. Screen Configuration Not Found

**Error:** "Screen config not found" displayed to user.

**Cause:** Screen name doesn't match any configured screen in the backend.

**Solution:**
- Verify screen name in deep link matches backend configuration
- Check network connectivity
- Ensure ScreenRepository is properly configured

### 4. Data Source Execution Failures

**Error:** "Failed to load" with network or parsing errors.

**Cause:** Issues with data source execution or network problems.

**Solution:**
- Check network connectivity
- Verify DataSourceExecutor configuration
- Ensure proper error handling in ViewModel

### 5. Deep Link Not Recognized

**Error:** Deep link opens browser instead of app.

**Cause:** Incorrect AndroidManifest configuration or app not installed.

**Solution:**
- Verify intent-filter configuration
- Test with `adb shell am start` command
- Check if app is properly installed

### 6. Navigation Graph Mismatch

**Error:** Navigation to route fails in MainActivity.

**Cause:** Route doesn't exist in navigation graph or wrong graph selected.

**Solution:**
- Verify route constants in Routes.kt
- Check navigation graph setup in ArchitectNavHost
- Ensure proper graph selection logic in MainActivity

### 7. Memory Leaks

**Error:** ViewModel not properly cleaned up.

**Cause:** Improper lifecycle management or retained references.

**Solution:**
- Use `collectAsStateWithLifecycle()` for state collection
- Properly cancel coroutines in ViewModel
- Avoid storing context references in ViewModel

### 8. Race Conditions

**Error:** Inconsistent state or multiple navigation calls.

**Cause:** Concurrent state updates or effect emissions.

**Solution:**
- Use single source of truth for state
- Properly handle LaunchedEffect keys
- Use `collectLatest` for effect collection

## Best Practices

### 1. Error Handling
```kotlin
// Always provide user-friendly error messages
runCatching { executeDataSource.execute(dataSource) }
    .onSuccess { /* handle success */ }
    .onFailure { error ->
        setState { copy(isLoading = false, error = error.message ?: "Unknown error") }
    }
```

### 2. Loading States
```kotlin
// Show loading indicators during async operations
setState { copy(isLoading = true, error = null) }
// ... async operation ...
setState { copy(isLoading = false) }
```

### 3. Navigation Safety
```kotlin
// Validate routes before navigation
params["route"]?.let { route ->
    if (isValidRoute(route)) {
        setEffect(DeepLinkPageEffect.Navigate(route))
    }
}
```

### 4. URI Validation
```kotlin
// Validate deep link parameters
val screenName = uri?.lastPathSegment?.takeIf { it.isNotBlank() } ?: "home"
val symbol = uri?.getQueryParameter("symbol")?.takeIf { it.isNotBlank() }
```

### 5. Testing
```kotlin
// Test deep links programmatically
@Test
fun testDeepLinkNavigation() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myapp.com/screen/banking"))
    // Verify intent handling
}
```

## Security Considerations

### 1. Input Validation
- Validate all URI parameters
- Sanitize screen names and symbols
- Prevent injection attacks

### 2. Network Security
- Use HTTPS for deep links
- Validate server certificates
- Implement proper SSL pinning

### 3. Data Protection
- Don't expose sensitive data in deep link parameters
- Use secure parameter passing
- Implement proper authentication checks

## Performance Optimization

### 1. Caching
- Cache screen configurations locally
- Implement intelligent cache invalidation
- Use appropriate cache expiration policies

### 2. Lazy Loading
- Load screen configurations on-demand
- Implement progressive loading for large screens
- Use pagination for list data

### 3. Background Processing
- Perform network operations on background threads
- Use appropriate coroutine contexts
- Avoid blocking UI thread

## Monitoring and Analytics

### 1. Deep Link Tracking
```kotlin
// Track deep link usage
FirebaseAnalytics.getInstance(context).logEvent("deep_link_opened") {
    param("screen", screenName)
    param("source", "external")
}
```

### 2. Error Monitoring
```kotlin
// Report deep link errors
FirebaseCrashlytics.getInstance().recordException(Exception("Deep link failed: $error"))
```

### 3. Performance Metrics
```kotlin
// Track loading times
val startTime = System.currentTimeMillis()
// ... loading logic ...
val loadTime = System.currentTimeMillis() - startTime
// Report to analytics
```

## Future Enhancements

### 1. Advanced Routing
- Support for nested routes
- Query parameter mapping
- Dynamic route generation

### 2. Offline Support
- Offline screen caching
- Fallback UI for network failures
- Sync mechanisms for offline actions

### 3. Personalization
- User-specific deep link handling
- Contextual content loading
- Personalized screen configurations

### 4. Analytics Integration
- Comprehensive tracking
- Conversion funnel analysis
- A/B testing framework

## Conclusion

The DeepLink feature provides a robust, scalable solution for handling external navigation in the MoviesDemoAPP. While it introduces some complexity, the benefits of dynamic content delivery and seamless integration outweigh the challenges when properly implemented and maintained.

The key to success lies in thorough testing, proper error handling, and continuous monitoring of the feature's performance and reliability.</content>
<parameter name="filePath">C:\Users\a.ex.srivastava\AndroidStudioProjects\MoviesDemoAPP\DEEPLINK_FEATURE_DOCUMENTATION.md
