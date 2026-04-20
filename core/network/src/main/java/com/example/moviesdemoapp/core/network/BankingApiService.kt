package com.example.moviesdemoapp.core.network

import retrofit2.http.GET

// Retrofit service for banking endpoints.
// All /banking/* paths are intercepted by MockInterceptor and served from
// assets/mock/banking/{endpoint}.json - no real network call is made.
interface BankingApiService {

    @GET("/banking/home")
    suspend fun getBankingHome(): BankingHomeDto
}
