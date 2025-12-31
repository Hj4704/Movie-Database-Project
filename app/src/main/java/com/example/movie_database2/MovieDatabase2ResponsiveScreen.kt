package com.example.movie_database2

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage

@Composable
fun MovieDatabase2ResponsiveScreen(
    viewModel: MovieDatabase2ViewModel,
    listOffsetX: Dp = 0.dp,
    listOffsetY: Dp = 0.dp,
    detailOffsetX: Dp = 0.dp,
    detailOffsetY: Dp = 0.dp
) {
    val state by viewModel.uiState
    val context = LocalContext.current

    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingPhotoUri
        val movie = state.selectedMovie
        if (success && uri != null && movie != null) {
            viewModel.onPhotoCaptured(movie.id, uri.toString())
        }
    }

    when {
        state.selectedMovie != null -> {
            val movie = state.selectedMovie!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .offset(detailOffsetX, detailOffsetY)
            ) {
                Text(
                    text = "Movie Database",
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                MovieDetailScreen(
                    movie = movie,
                    onBack = { viewModel.onBackFromDetail() },
                    onToggleLike = { viewModel.toggleLike(movie.id) },
                    onTakePhoto = {
                        val contentValues = ContentValues().apply {
                            put(
                                MediaStore.Images.Media.DISPLAY_NAME,
                                "movie_${movie.id}_${System.currentTimeMillis()}.jpg"
                            )
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(
                                    MediaStore.Images.Media.RELATIVE_PATH,
                                    "Pictures/MovieDatabase2"
                                )
                            }
                        }

                        val resolver = context.contentResolver
                        val uri = resolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )

                        if (uri != null) {
                            pendingPhotoUri = uri
                            takePictureLauncher.launch(uri)
                        }
                    },
                    onViewPhoto = {
                        movie.photoUri?.let { uriString ->
                            val uri = Uri.parse(uriString)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(listOffsetX, listOffsetY)
            ) {
                Text(
                    text = "Movie Database",
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                MovieTopBar(
                    state = state,
                    onToggleLayout = { viewModel.toggleLayout() },
                    onSortChange = { viewModel.changeSort(it) },
                    onFilterToggle = { viewModel.toggleFilter() }
                )

                MovieListScreen(
                    state = state,
                    onMovieClick = { viewModel.onMovieClicked(it) },
                    onRetry = { viewModel.retry() },
                    offsetX = 0.dp,
                    offsetY = 0.dp
                )
            }
        }
    }
}

@Composable
fun MovieTopBar(
    state: MovieUiState,
    onToggleLayout: () -> Unit,
    onSortChange: (SortOption) -> Unit,
    onFilterToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onToggleLayout,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            )
        ) {
            val text = if (state.layoutMode == LayoutMode.LIST) "Grid" else "List"
            Text(text, fontSize = 12.sp)
        }

        OutlinedButton(
            onClick = { onSortChange(SortOption.TITLE) },
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            )
        ) {
            Text("Title", fontSize = 12.sp)
        }

        OutlinedButton(
            onClick = { onSortChange(SortOption.RELEASE_DATE) },
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            )
        ) {
            Text("Date", fontSize = 12.sp)
        }

        OutlinedButton(
            onClick = { onSortChange(SortOption.USER_RATING) },
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            )
        ) {
            Text("Rating", fontSize = 12.sp)
        }

        OutlinedButton(
            onClick = onFilterToggle,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            )
        ) {
            val filterText = if (state.filterOption == FilterOption.ALL) "All" else "Liked"
            Text("Filter: $filterText", fontSize = 12.sp)
        }
    }
}

@Composable
fun MovieListScreen(
    state: MovieUiState,
    onMovieClick: (Movie) -> Unit,
    onRetry: () -> Unit,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset(offsetX, offsetY)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        state.errorMessage?.let { msg ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Button(onClick = onRetry) {
                    Text("Refresh")
                }
            }
        }

        if (state.layoutMode == LayoutMode.LIST) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.movies) { movie ->
                    MovieListRow(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.movies) { movie ->
                    MoviePosterGridItem(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
    }
}

@Composable
fun MovieListRow(
    movie: Movie,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        MoviePosterThumbnail(
            posterPath = movie.posterPath,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Rating: ${"%.1f".format(movie.voteAverage)} / 10",
                style = MaterialTheme.typography.bodyMedium
            )
            movie.releaseDate?.let {
                Text(
                    text = "Released: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MoviePosterGridItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MoviePosterThumbnail(
            posterPath = movie.posterPath,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        )
    }
}

@Composable
fun MoviePosterThumbnail(
    posterPath: String?,
    modifier: Modifier = Modifier
) {
    val url = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    AsyncImage(
        model = url,
        contentDescription = "Movie poster",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun MovieDetailScreen(
    movie: Movie,
    onBack: () -> Unit,
    onToggleLike: () -> Unit,
    onTakePhoto: () -> Unit,
    onViewPhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Back", fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = onTakePhoto,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Take picture", fontSize = 14.sp)
            }

            if (movie.photoUri != null) {
                OutlinedButton(
                    onClick = onViewPhoto,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("View picture", fontSize = 14.sp)
                }
            }

            OutlinedButton(
                onClick = onToggleLike,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val label = if (movie.liked) "Unlike" else "Like"
                Text(label, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        MoviePosterThumbnail(
            posterPath = movie.posterPath,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .aspectRatio(2f / 3f)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(8.dp))

        Text(movie.title, style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(4.dp))

        Text("Rating: ${"%.1f".format(movie.voteAverage)} / 10")
        Text("Votes: ${movie.voteCount}")
        movie.releaseDate?.let {
            Text("Release date: $it")
        }

        Spacer(Modifier.height(8.dp))

        if (movie.overview.isNotBlank()) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(movie.overview)
        }
    }
}
