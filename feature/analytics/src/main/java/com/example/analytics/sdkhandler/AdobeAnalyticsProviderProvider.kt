package com.example.analytics.sdkhandler

import com.adobe.marketing.mobile.MobileCore
import com.example.analytics.event.ActionType
import com.example.analytics.event.AnalyticsEvent
import com.example.analytics.sdklayer.AnalyticsProvider

class AdobeAnalyticsProviderProvider : AnalyticsProvider {

    // Adobe Analytics doesn't have a direct logEvent method like Firebase.
    // Instead, you would typically use the trackState or trackAction methods.
    override fun track(event: AnalyticsEvent) {
        val bundle = mutableMapOf<String, String>()
        event.params.forEach { (k, v) ->
            bundle[k] = v.toString()
        }
        when(event.actionType){
            ActionType.ACTION  -> {
                // Used for button clicks or specific events.
                MobileCore.trackAction(event.eventName, bundle)
            }
            else -> {
                // Used for screen views or page loads
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