package com.example.analytics.event

data class AnalyticsEvent(
    val provider: Provider = Provider.ALL,
    val eventName: String,
    val actionType:ActionType? = null,
    val params: Map<String, String?> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActionType{
    ACTION, //Used for button clicks or specific events.
    STATE  // Used for screen views or page loads
}


enum class Provider{
    FIREBASE,
    ABODE,
    ALL,
    NONE
}