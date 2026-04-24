package com.example.analytics.security

import com.example.analytics.event.AnalyticsEvent

class AnalyticsInterceptor {

    private val blockedKeys = listOf("pan", "aadhaar", "account_number")

    fun process(event: AnalyticsEvent): AnalyticsEvent? {

        val filteredParams = event.params.filterKeys { key ->
            key.lowercase() !in blockedKeys
        }

        // Optional: whitelist events
        if (!isAllowedEvent(event.eventName)) return null

        return event.copy(params = filteredParams)
    }

    private fun isAllowedEvent(name: String): Boolean {
        return name.startsWith("app_") || name.startsWith("user_")
    }
}