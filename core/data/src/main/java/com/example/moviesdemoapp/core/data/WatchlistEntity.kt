package com.example.moviesdemoapp.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val imdbID: String,
    val title: String,
    val posterUrl: String,
    val rating: String,
    val year: String,
    val genre: String = "",
    val addedAt: Long = System.currentTimeMillis(),
)
