package com.example.testmandiri.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.testmandiri.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieGuide(viewModel: MovieViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val genre = state.genre
    val movie = state.movie
    val canGoBack = movie != null || genre != null || state.showingFavorites
    var showCredits by rememberSaveable { mutableStateOf(false) }
    val movieList = key(genre?.id) { rememberLazyListState() }

    BackHandler(enabled = canGoBack, onBack = viewModel::back)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            movie != null -> "Movie details"
                            genre != null -> genre.name
                            state.showingFavorites -> "Favorites"
                            else -> "Movie Guide"
                        },
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (canGoBack) {
                        TextButton(onClick = viewModel::back) { Text("‹ Back") }
                    }
                },
                actions = {
                    if (!canGoBack) {
                        TextButton(onClick = viewModel::openFavorites) { Text("Favorites") }
                        TextButton(onClick = { showCredits = true }) { Text("About") }
                    }
                }
            )
        }
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets)) {
            when {
                movie != null -> key(movie.id) {
                    MovieDetailScreen(
                        movie = movie,
                        details = state.details,
                        reviews = viewModel.reviews.collectAsLazyPagingItems(),
                        favorite = (favorites as? LoadState.Ready)?.value?.any { it.id == movie.id },
                        savingFavorite = state.savingFavorite,
                        favoriteError = state.favoriteError,
                        setFavorite = viewModel::setFavorite,
                        retryDetails = viewModel::loadDetails
                    )
                }
                genre != null -> MovieListScreen(
                    movies = viewModel.movies.collectAsLazyPagingItems(),
                    listState = movieList,
                    openMovie = viewModel::openMovie
                )
                state.showingFavorites -> FavoritesScreen(favorites, viewModel::openMovie)
                else -> GenreScreen(
                    genres = state.genres,
                    openGenre = viewModel::openGenre,
                    retry = viewModel::loadGenres
                )
            }
        }
    }

    if (showCredits) {
        AlertDialog(
            onDismissRequest = { showCredits = false },
            title = { Text("About Movie Guide") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(
                        painterResource(R.drawable.tmdb_logo),
                        contentDescription = "The Movie Database",
                        modifier = Modifier.width(132.dp)
                    )
                    Text("This product uses the TMDB API but is not endorsed or certified by TMDB.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCredits = false }) { Text("Close") }
            }
        )
    }
}

@Composable
internal fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun ScreenMessage(message: String, retry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Message(message, retry)
    }
}

@Composable
internal fun Message(message: String, retry: (() -> Unit)? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (retry != null) TextButton(onClick = retry) { Text("Try again") }
    }
}
