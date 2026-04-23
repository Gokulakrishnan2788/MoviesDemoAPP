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
    single { AdobeAnalyticsProviderProvider() }

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
