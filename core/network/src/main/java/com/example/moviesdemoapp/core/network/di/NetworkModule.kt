package com.example.moviesdemoapp.core.network.di

import android.content.Context
import com.example.moviesdemoapp.core.network.BankingApi
import com.example.moviesdemoapp.core.network.MockBankingInterceptor
import com.example.moviesdemoapp.core.network.NetworkClient
import com.example.moviesdemoapp.core.network.OkHttpNetworkClient
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

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
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(MockBankingInterceptor(context)) // Added dummy server interceptor
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    fun provideBankingApi(okHttpClient: OkHttpClient, json: Json): BankingApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
           // .baseUrl("https://bd2e759f-9a31-4529-ba92-b69d005fa5bc.mock.pstmn.io/")
            .baseUrl("https://69466fa7-28bb-4c53-9918-e87fecc47f47.mock.pstmn.io/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(BankingApi::class.java)
    }
}
