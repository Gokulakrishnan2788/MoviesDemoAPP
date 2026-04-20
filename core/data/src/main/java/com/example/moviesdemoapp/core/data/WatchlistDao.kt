package com.example.moviesdemoapp.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    suspend fun getAll(): List<WatchlistEntity>

    @Query("SELECT COUNT(*) > 0 FROM watchlist WHERE imdbID = :id")
    suspend fun isInWatchlist(id: String): Boolean

    @Query("SELECT * FROM watchlist WHERE imdbID = :imdbID LIMIT 1")
    suspend fun findById(imdbID: String): WatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    @Delete
    suspend fun delete(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE imdbID = :imdbID")
    suspend fun deleteById(imdbID: String)
}
