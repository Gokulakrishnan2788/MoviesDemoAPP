package com.example.analytics.engine

import com.example.analytics.event.AnalyticsEvent
import com.example.analytics.event.Provider
import com.example.analytics.sdkhandler.AdobeAnalyticsProviderProvider
import com.example.analytics.sdkhandler.FirebaseAnalyticsProvider
import com.example.analytics.sdklayer.AnalyticsProvider
import com.example.analytics.security.AnalyticsInterceptor

class AnalyticsEngine(
    private val providers: List<AnalyticsProvider>,
    private val interceptor: AnalyticsInterceptor
) {
    fun track(event: AnalyticsEvent) {
        when (event.provider) {
            Provider.FIREBASE -> providers.filterIsInstance<FirebaseAnalyticsProvider>()
                .forEach { firebase ->
                    val safeEvent = interceptor.process(event) ?: return
                    firebase.track(safeEvent)
                }

            Provider.ABODE -> providers.filterIsInstance<AdobeAnalyticsProviderProvider>()
                .forEach { adobe ->
                    val safeEvent = interceptor.process(event) ?: return
                    adobe.track(safeEvent)
                }

            Provider.ALL -> {
                val safeEvent = interceptor.process(event) ?: return
                providers.forEach {
                    it.track(safeEvent)
                }
            }

            Provider.NONE -> {

            }
        }
    }
}