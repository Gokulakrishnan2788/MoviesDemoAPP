package com.example.analytics.handler

import com.example.analytics.engine.AnalyticsEngine
import com.example.analytics.event.AnalyticsEvent

class AnalyticsHandler(
    private val analyticsEngine: AnalyticsEngine
) {

    fun handle(event: AnalyticsEvent?) {
        event?.let {
            analyticsEngine.track(it)
        }
    }
}