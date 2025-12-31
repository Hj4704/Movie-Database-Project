package com.example.movie_database2

fun MovieEntity.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        posterPath = posterPath,
        voteAverage = voteAverage,
        voteCount = voteCount,
        releaseDate = releaseDate,
        overview = overview,
        liked = liked,
        photoUri = photoUri
    )

fun Movie.toEntity(): MovieEntity =
    MovieEntity(
        id = id,
        title = title,
        posterPath = posterPath,
        voteAverage = voteAverage,
        voteCount = voteCount,
        releaseDate = releaseDate,
        overview = overview,
        liked = liked,
        photoUri = photoUri
    )
