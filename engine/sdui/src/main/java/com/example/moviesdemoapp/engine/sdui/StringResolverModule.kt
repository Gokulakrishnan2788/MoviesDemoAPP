package com.example.moviesdemoapp.engine.sdui

import android.content.Context
import com.example.moviesdemoapp.core.network.StringResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StringResolverModule {

    @Provides
    @Singleton
    fun provideStringResolver(@ApplicationContext context: Context): StringResolver =
        AndroidStringResolver(context)
}
