package com.example.moviesdemoapp.core.network.di

import com.example.moviesdemoapp.core.network.KotlinxSerializationConverterFactory
import com.example.moviesdemoapp.core.network.NetworkClient
import com.example.moviesdemoapp.core.network.OkHttpNetworkClient
import com.example.moviesdemoapp.core.network.OmdbApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

private const val OMDB_BASE_URL = "https://www.omdbapi.com/"

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindingsModule {

    @Binds
    @Singleton
    abstract fun bindNetworkClient(impl: OkHttpNetworkClient): NetworkClient
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(OMDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(KotlinxSerializationConverterFactory.create(json))
            .build()

    @Provides
    @Singleton
    fun provideOmdbApiService(retrofit: Retrofit): OmdbApiService =
        retrofit.create(OmdbApiService::class.java)
}
