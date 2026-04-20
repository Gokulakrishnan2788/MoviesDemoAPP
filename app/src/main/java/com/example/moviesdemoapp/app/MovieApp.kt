package com.example.moviesdemoapp.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application class. Enables Hilt dependency injection for the entire app. */
@HiltAndroidApp
class MovieApp : Application()
