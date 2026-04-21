package com.example.moviesdemoapp.engine.sdui.di

import com.example.moviesdemoapp.engine.sdui.SduiComponentProvider
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
abstract class SduiModule {

    /**
     * Declares the base empty Set<SduiComponentProvider> multibinding.
     *
     * This tells Hilt the set exists even when no feature has contributed
     * any custom components yet. Without this, Hilt would throw a
     * "missing binding" error at compile time if the set is empty.
     *
     * Feature modules add their entries via @Provides @IntoSet — they never
     * touch this file.
     */
    @Multibinds
    abstract fun bindComponentProviders(): Set<SduiComponentProvider>
}
