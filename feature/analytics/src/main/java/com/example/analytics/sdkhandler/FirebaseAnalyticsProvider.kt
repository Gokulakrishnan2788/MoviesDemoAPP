package com.example.analytics.sdkhandler

import android.os.Bundle
import com.example.analytics.event.AnalyticsEvent
import com.example.analytics.sdklayer.AnalyticsProvider

class FirebaseAnalyticsProvider(
    private val firebase: com.google.firebase.analytics.FirebaseAnalytics
) : AnalyticsProvider {

    override fun track(event: AnalyticsEvent) {
        val bundle = Bundle().apply {
            event.params.forEach { (k, v) ->
                putString(k, v.toString())
            }
        }
        firebase.logEvent(event.eventName, bundle)
    }

    override fun identify(userId: String) {
        firebase.setUserId(userId)
    }

    override fun setUserProperty(key: String, value: String) {
        firebase.setUserProperty(key, value)
    }
}