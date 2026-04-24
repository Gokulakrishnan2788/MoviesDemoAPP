package com.example.moviesdemoapp.app

import android.app.Application
import com.example.analytics.di.analyticsModule
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/** Application class. Enables both Hilt and Koin dependency injection for the entire app. */
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin alongside Hilt for analytics module
        startKoin {
            androidContext(this@App)
            modules(analyticsModule)
        }
    }
}
