package com.example.movie_database2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies")
    suspend fun getAll(): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Query("UPDATE movies SET liked = :liked WHERE id = :movieId")
    suspend fun updateLiked(movieId: Int, liked: Boolean)

    @Query("UPDATE movies SET photoUri = :photoUri WHERE id = :movieId")
    suspend fun updatePhoto(movieId: Int, photoUri: String?)

    @Query("DELETE FROM movies")
    suspend fun clearAll()
}