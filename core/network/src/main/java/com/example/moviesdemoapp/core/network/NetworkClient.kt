package com.example.moviesdemoapp.core.network

// Contract for making raw HTTP GET requests.
// Keeping this as an interface makes it easy to mock in tests.
interface NetworkClient {
    suspend fun get(url: String): String?
}
