# Koin Dependency Injection Setup for AnalyticsEngine

This document explains how to inject the `AnalyticsEngine` class and its dependencies using Koin alongside Hilt.

## Overview

The `AnalyticsEngine` requires two dependencies:
- `providers: List<AnalyticsProvider>` - List of analytics providers (Firebase, Adobe, etc.)
- `interceptor: AnalyticsInterceptor` - Security interceptor for filtering sensitive data

## Setup Steps

### 1. Add Koin Dependencies

Add Koin to your module's `build.gradle.kts`:

```kotlin
dependencies {
    // Koin dependency injection
    implementation("io.insert-koin:koin-android:3.5.6")
}
```

### 2. Create Koin Module

Create `AnalyticsModule.kt` in your DI package:

```kotlin
package com.example.analytics.di

import com.example.analytics.engine.AnalyticsEngine
import com.example.analytics.handler.AnalyticsHandler
import com.example.analytics.sdkhandler.AdobeAnalyticsProviderProvider
import com.example.analytics.sdkhandler.FirebaseAnalyticsProvider
import com.example.analytics.sdklayer.AnalyticsProvider
import com.example.analytics.security.AnalyticsInterceptor
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val analyticsModule = module {

    // Firebase Analytics instance
    single { FirebaseAnalytics.getInstance(androidContext()) }

    // Analytics Providers
    single { FirebaseAnalyticsProvider(get()) }
    single { AdobeAnalyticsProviderProvider(get()) }

    // List of all providers
    single<List<AnalyticsProvider>> {
        listOf(
            get<FirebaseAnalyticsProvider>(),
            get<AdobeAnalyticsProviderProvider>()
        )
    }

    // Analytics Interceptor
    single { AnalyticsInterceptor() }

    // Analytics Engine - the main class you want to inject
    single {
        AnalyticsEngine(
            providers = get(),
            interceptor = get()
        )
    }

    // Analytics Handler
    single {
        AnalyticsHandler(
            analyticsEngine = get()
        )
    }
}
```

### 3. Initialize Koin alongside Hilt

Update your `Application` class (which already uses `@HiltAndroidApp`):

```kotlin
@HiltAndroidApp
class MovieApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin alongside Hilt for analytics module
        startKoin {
            androidContext(this@MovieApp)
            modules(analyticsModule)
        }
    }
}
```

## Usage Examples

### Inject in Activity

```kotlin
class MainActivity : ComponentActivity() {

    // Inject AnalyticsEngine
    private val analyticsEngine: AnalyticsEngine by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Track an event
        val event = AnalyticsEvent(
            name = "app_launch",
            provider = Provider.ALL,
            params = mapOf("screen" to "main")
        )

        analyticsEngine.track(event)
    }
}
```

### Inject in ViewModel

```kotlin
class AnalyticsViewModel : ViewModel() {

    // Inject AnalyticsEngine
    private val analyticsEngine: AnalyticsEngine by inject()

    fun trackUserAction(action: String) {
        val event = AnalyticsEvent(
            name = "user_action",
            provider = Provider.FIREBASE,
            params = mapOf("action" to action)
        )

        analyticsEngine.track(event)
    }
}
```

### Inject in Repository/Service

```kotlin
class UserRepository(
    private val analyticsEngine: AnalyticsEngine = get()
) {

    fun loginUser(userId: String) {
        // Login logic...

        val event = AnalyticsEvent(
            name = "user_login",
            provider = Provider.ALL,
            params = mapOf("user_id" to userId)
        )

        analyticsEngine.track(event)
    }
}
```

## Provider-Specific Tracking

```kotlin
// Track only to Firebase
analyticsEngine.track(AnalyticsEvent(
    name = "screen_view",
    provider = Provider.FIREBASE,
    params = mapOf("screen_name" to "home")
))

// Track only to Adobe
analyticsEngine.track(AnalyticsEvent(
    name = "purchase",
    provider = Provider.ABODE,
    params = mapOf("product_id" to "123")
))

// Track to all providers
analyticsEngine.track(AnalyticsEvent(
    name = "app_crash",
    provider = Provider.ALL,
    params = mapOf("error_message" to "NullPointerException")
))
```

## Testing with Koin

```kotlin
class AnalyticsEngineInjectionTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(analyticsModule)
    }

    @Test
    fun `analytics engine should be injectable with all dependencies`() {
        val analyticsEngine: AnalyticsEngine = get()
        
        assert(analyticsEngine != null) { "AnalyticsEngine should be injectable" }
        
        val event = AnalyticsEvent(
            name = "test_event",
            provider = Provider.ALL,
            params = mapOf("test_param" to "test_value")
        )
        
        analyticsEngine.track(event)
    }
}
```

## Integration with SDUI Renderer

The `AnalyticsEngine` is now properly integrated with the SDUI rendering system:

```kotlin
// In SDUIRenderer.kt - AnalyticsEngine is injected via Koin
val analyticsEngine = remember {
    GlobalContext.get().get<AnalyticsEngine>()
}
val components = remember { SDUIComponentsDispatcher(resolver, analyticsEngine) }
```

This ensures that:
- Analytics tracking works within SDUI components
- Dependencies are properly resolved at runtime
- The hybrid Hilt + Koin approach works seamlessly

## Benefits of This Setup

1. **Hybrid Approach**: Uses Hilt for main app DI and Koin for analytics module
2. **Clean Architecture**: Dependencies are clearly defined and injected
3. **Testability**: Easy to mock dependencies in tests  
4. **Flexibility**: Add/remove providers without changing business logic
5. **Security**: Built-in data filtering through AnalyticsInterceptor
6. **Type Safety**: Compile-time dependency resolution
7. **Scoping**: Proper lifecycle management of dependencies

## Important Notes

- The main application continues to use Hilt for dependency injection
- Koin is only used for the analytics module to avoid conflicts
- Both DI frameworks can coexist peacefully in the same application
- AnalyticsEngine is now fully injectable with Koin and can be used anywhere in the app
