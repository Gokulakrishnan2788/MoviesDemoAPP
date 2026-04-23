package com.example.analytics

import com.example.analytics.di.analyticsModule
import com.example.analytics.engine.AnalyticsEngine
import com.example.analytics.event.AnalyticsEvent
import com.example.analytics.event.Provider
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

/**
 * Unit tests for AnalyticsEngine dependency injection
 */
class AnalyticsEngineInjectionTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(analyticsModule)
    }

    @Test
    fun `analytics engine should be injectable with all dependencies`() {
        // Given: AnalyticsEngine is injected via Koin
        val analyticsEngine: AnalyticsEngine = get()

        // Then: AnalyticsEngine should not be null and have required dependencies
        assert(analyticsEngine != null) { "AnalyticsEngine should be injectable" }

        // Test that we can create and track an event
        val event = AnalyticsEvent(
            eventName = "test_event",
            provider = Provider.ALL,
            params = mapOf("test_param" to "test_value")
        )

        // This should not throw an exception
        analyticsEngine.track(event)
    }

    @Test
    fun `analytics engine should handle different providers correctly`() {
        val analyticsEngine: AnalyticsEngine = get()

        // Test Firebase provider
        val firebaseEvent = AnalyticsEvent(
            eventName = "firebase_test",
            provider = Provider.FIREBASE,
            params = mapOf("source" to "test")
        )
        analyticsEngine.track(firebaseEvent)

        // Test Adobe provider
        val adobeEvent = AnalyticsEvent(
            eventName = "adobe_test",
            provider = Provider.ABODE,
            params = mapOf("source" to "test")
        )
        analyticsEngine.track(adobeEvent)

        // Test all providers
        val allEvent = AnalyticsEvent(
            eventName = "all_providers_test",
            provider = Provider.ALL,
            params = mapOf("source" to "test")
        )
        analyticsEngine.track(allEvent)
    }
}
