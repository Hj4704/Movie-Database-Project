package com.example.movie_database2

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object API {
    private const val BASE = "https://api.themoviedb.org/3/movie/now_playing"

    private var apiKey: String? = null
    private var bearerToken: String? = null

    fun configure(apiKey: String?, bearerToken: String?) {
        this.apiKey = apiKey
        this.bearerToken = bearerToken
    }
    fun fetchMovies(page: Int): List<Movie> {
        val urlStr = buildString {
            append(BASE)
            append("?page=").append(page)
            if (!apiKey.isNullOrBlank()) {
                append("&api_key=").append(apiKey)
            }
        }

        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            doInput = true
            if (!bearerToken.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $bearerToken")
            }
        }

        conn.inputStream.use { stream ->
            val response = BufferedReader(InputStreamReader(stream)).readText()
            return parseMovies(response)
        }
    }

    private fun parseMovies(jsonText: String): List<Movie> {
        val movies = mutableListOf<Movie>()
        val root = JSONObject(jsonText)
        if (!root.has("results")) return movies

        val arr = root.getJSONArray("results")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)

            if (obj.optString("original_language", "") != "en") continue

            val id = obj.optInt("id")
            val title = obj.optString("title", "Untitled")
            val overview = obj.optString("overview", "")
            val releaseDateRaw = obj.optString("release_date", "")
            val releaseDate = releaseDateRaw.takeIf { it.isNotBlank() }
            val voteAverage = obj.optDouble("vote_average", 0.0)
            val voteCount = obj.optInt("vote_count", 0)
            val posterPath = obj.optString("poster_path", null)
            val language = obj.optString("original_language", "")

            movies.add(
                Movie(
                    id = id,
                    title = title,
                    voteAverage = voteAverage,
                    voteCount = voteCount,
                    posterPath = posterPath,
                    releaseDate = releaseDate,
                    overview = overview,
                    liked = false
                )
            )

        }
        return movies
    }
}