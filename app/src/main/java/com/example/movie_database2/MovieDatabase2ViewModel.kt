package com.example.movie_database2

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.movieDataStore by preferencesDataStore(name = "movie_prefs")

enum class LayoutMode {
    LIST,
    GRID
}

enum class SortOption {
    TITLE,
    RELEASE_DATE,
    USER_RATING
}

enum class FilterOption {
    ALL,
    LIKED
}

data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val releaseDate: String?,
    val overview: String,
    val liked: Boolean = false,
    val photoUri: String? = null
)

data class MovieUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    val movies: List<Movie> = emptyList(),

    val layoutMode: LayoutMode = LayoutMode.LIST,
    val sortOption: SortOption = SortOption.TITLE,
    val filterOption: FilterOption = FilterOption.ALL,

    val selectedMovie: Movie? = null
)

class MovieDatabase2ViewModel(application: Application) : AndroidViewModel(application) {

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    var uiState = mutableStateOf(MovieUiState())
        private set

    private var pagesToLoad: List<Int> = listOf(1, 2, 3)
    private var apiKey: String? = null
    private var bearerToken: String? = null

    private var allMoviesRaw: List<Movie> = emptyList()

    private val likedMovieIds = mutableSetOf<Int>()
    private val dataStore = application.movieDataStore
    private val likedKey = stringSetPreferencesKey("liked_movie_ids")

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val stored = prefs[likedKey] ?: emptySet()
            likedMovieIds.clear()
            likedMovieIds.addAll(stored.mapNotNull { it.toIntOrNull() })
            recomputeVisibleMovies(updateSelected = true)
        }
    }

    fun setTmdbAuth(apiKey: String?, bearerToken: String?) {
        this.apiKey = apiKey
        this.bearerToken = bearerToken
        API.configure(apiKey = apiKey, bearerToken = bearerToken)

        if (allMoviesRaw.isEmpty()) {
            fetchSelectedPages()
        } else {
            recomputeVisibleMovies(updateSelected = true)
        }
    }

    fun setPagesToLoad(pages: List<Int>) {
        this.pagesToLoad = pages
        fetchSelectedPages()
    }

    fun retry() {
        fetchSelectedPages()
    }

    fun toggleLayout() {
        val newMode = if (uiState.value.layoutMode == LayoutMode.LIST) {
            LayoutMode.GRID
        } else {
            LayoutMode.LIST
        }
        uiState.value = uiState.value.copy(layoutMode = newMode)
    }

    fun changeSort(option: SortOption) {
        uiState.value = uiState.value.copy(sortOption = option)
        recomputeVisibleMovies(updateSelected = true)
    }

    fun toggleFilter() {
        val newFilter = if (uiState.value.filterOption == FilterOption.ALL) {
            FilterOption.LIKED
        } else {
            FilterOption.ALL
        }
        uiState.value = uiState.value.copy(filterOption = newFilter)
        recomputeVisibleMovies(updateSelected = true)
    }

    fun onMovieClicked(movie: Movie) {
        uiState.value = uiState.value.copy(selectedMovie = movie)
    }

    fun onBackFromDetail() {
        uiState.value = uiState.value.copy(selectedMovie = null)
    }

    fun toggleLike(movieId: Int) {
        if (likedMovieIds.contains(movieId)) {
            likedMovieIds.remove(movieId)
        } else {
            likedMovieIds.add(movieId)
        }
        persistLikedIds()
        recomputeVisibleMovies(updateSelected = true)
    }

    fun onPhotoCaptured(movieId: Int, uriString: String) {
        val updatedAll = allMoviesRaw.map { movie ->
            if (movie.id == movieId) {
                movie.copy(photoUri = uriString)
            } else {
                movie
            }
        }
        allMoviesRaw = updatedAll

        recomputeVisibleMovies(updateSelected = true)
    }

    private fun persistLikedIds() {
        val idsAsStrings = likedMovieIds.map { it.toString() }.toSet()
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[likedKey] = idsAsStrings
            }
        }
    }
    private fun fetchSelectedPages() {
        if (apiKey.isNullOrBlank() && bearerToken.isNullOrBlank()) {
            uiState.value = MovieUiState(
                isLoading = false,
                errorMessage = "TMDB auth missing"
            )
            return
        }
        val previousSelectedId = uiState.value.selectedMovie?.id

        uiState.value = uiState.value.copy(isLoading = true, errorMessage = null)

        io.execute {
            try {
                val previousPhotoMap: Map<Int, String?> =
                    allMoviesRaw.associateBy({ it.id }, { it.photoUri })

                val collected = mutableListOf<Movie>()
                for (p in pagesToLoad) {
                    collected.addAll(API.fetchMovies(page = p))
                }

                val merged = collected.map { movie ->
                    val oldPhoto = previousPhotoMap[movie.id]
                    if (oldPhoto != null) {
                        movie.copy(photoUri = oldPhoto)
                    } else {
                        movie
                    }
                }

                allMoviesRaw = merged

                main.post {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )

                    val currentState = uiState.value
                    val selectedMovie = previousSelectedId?.let { id ->
                        allMoviesRaw.firstOrNull { it.id == id }
                    }
                    uiState.value = currentState.copy(selectedMovie = selectedMovie)

                    recomputeVisibleMovies(updateSelected = true)
                }
            } catch (e: Exception) {
                main.post {
                    uiState.value = MovieUiState(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load movies",
                        movies = emptyList(),
                        selectedMovie = null
                    )
                }
            }
        }
    }

    private fun recomputeVisibleMovies(updateSelected: Boolean) {
        val state = uiState.value

        val withLikes = allMoviesRaw.map { movie ->
            movie.copy(liked = likedMovieIds.contains(movie.id))
        }

        val filtered = if (state.filterOption == FilterOption.LIKED) {
            withLikes.filter { it.liked }
        } else {
            withLikes
        }

        val sorted = when (state.sortOption) {
            SortOption.TITLE ->
                filtered.sortedBy { it.title.lowercase() }

            SortOption.RELEASE_DATE ->
                filtered.sortedByDescending { it.releaseDate ?: "" }

            SortOption.USER_RATING ->
                filtered.sortedByDescending { it.voteAverage }
        }

        var newSelected = state.selectedMovie
        if (updateSelected && newSelected != null) {
            newSelected = withLikes.firstOrNull { it.id == newSelected.id }
        }

        uiState.value = state.copy(
            movies = sorted,
            selectedMovie = newSelected
        )
    }
}
