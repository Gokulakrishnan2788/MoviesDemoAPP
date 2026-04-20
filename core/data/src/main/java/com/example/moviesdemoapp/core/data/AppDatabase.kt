package com.example.moviesdemoapp.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

/** Room database for MovieApp local persistence. */
@Database(
    entities = [WatchlistEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    /** Returns the [WatchlistDao] for watchlist CRUD operations. */
    abstract fun watchlistDao(): WatchlistDao
}
