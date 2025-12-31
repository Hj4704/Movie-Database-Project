package com.example.movie_database2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val viewModel = ViewModelProvider(this)[MovieDatabase2ViewModel::class.java]

        viewModel.setTmdbAuth(
            apiKey = "e559116823d1df2a6dc33ad8d4e34e47",
            bearerToken = null
        )

        setContent {
            MovieDatabase2ResponsiveScreen(viewModel = viewModel)
        }
    }
}

