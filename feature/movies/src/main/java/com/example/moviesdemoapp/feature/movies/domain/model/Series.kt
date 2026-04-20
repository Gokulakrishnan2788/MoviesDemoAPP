package com.example.moviesdemoapp.feature.movies.domain.model

data class Series(
    val id: String,
    val title: String,
    val year: String,
    val type: String,
    val posterUrl: String,
    val rating: String = "",
    val genre: String = "",
)
