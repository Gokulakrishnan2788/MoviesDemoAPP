package com.example.analytics.sdkhandler

import com.adobe.marketing.mobile.MobileCore
import com.example.analytics.event.ActionType
import com.example.analytics.event.AnalyticsEvent
import com.example.analytics.sdklayer.AnalyticsProvider

class AdobeAnalyticsProviderProvider : AnalyticsProvider {
    override fun track(event: AnalyticsEvent) {
        val bundle = mutableMapOf<String, String>()
        event.params.forEach { (k, v) ->
            bundle[k] = v.toString()
        }
        when(event.actionType){
            ActionType.ACTION  -> {
                // Used for button clicks or specific events.
                // Adobe Analytics doesn't have a direct logEvent method like Firebase.
                // Instead, you would typically use the trackState or trackAction methods.
                MobileCore.trackAction(event.eventName, bundle)
            }
            else -> {
                // Used for screen views or page loads
                // Adobe Analytics doesn't have a direct logEvent method like Firebase.
                // Instead, you would typically use the trackState or trackAction methods.
                MobileCore.trackState(event.eventName, bundle)
            }

        }

    }

    override fun identify(userId: String) {
        TODO("Not yet implemented")
    }

    override fun setUserProperty(key: String, value: String) {
        TODO("Not yet implemented")
    }

}