package com.example.analytics.sdklayer

import com.example.analytics.event.AnalyticsEvent

interface AnalyticsProvider {
    fun track(event: AnalyticsEvent)
    fun identify(userId: String)
    fun setUserProperty(key: String, value: String)
}