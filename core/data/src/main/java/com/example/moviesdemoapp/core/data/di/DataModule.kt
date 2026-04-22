package com.example.moviesdemoapp.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.moviesdemoapp.core.data.local.AppDatabase
import com.example.moviesdemoapp.core.data.local.LocalScreenSource
import com.example.moviesdemoapp.core.data.local.WatchlistDao
import com.example.moviesdemoapp.core.data.remote.SDUIDataRepository
import com.example.moviesdemoapp.core.data.remote.SDUIDataRepositoryImpl
import com.example.moviesdemoapp.core.network.ScreenSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {

    @Binds
    @Singleton
    abstract fun bindSDUIDataRepository(impl: SDUIDataRepositoryImpl): SDUIDataRepository

    /**
     * Swap [LocalScreenSource] → [RemoteScreenSource] here to switch from bundled assets
     * to a live API — no ViewModel or engine code changes required.
     */
    @Binds
    @Singleton
    abstract fun bindScreenSource(impl: LocalScreenSource): ScreenSource
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "movieapp.db").build()

    @Provides
    @Singleton
    fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()
}
