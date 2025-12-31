package com.example.movie_database2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val releaseDate: String?,
    val overview: String,
    val liked: Boolean,
    val photoUri: String?
)