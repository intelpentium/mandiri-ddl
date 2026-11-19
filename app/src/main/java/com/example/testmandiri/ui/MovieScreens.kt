package com.example.testmandiri.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState as PagingLoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.example.testmandiri.data.Genre
import com.example.testmandiri.data.Movie
import com.example.testmandiri.data.MovieDetails
import com.example.testmandiri.data.Review
import com.example.testmandiri.data.Video
import com.example.testmandiri.data.tmdbErrorMessage
import com.example.testmandiri.ui.theme.MovieGuideTheme
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
internal fun GenreScreen(
    genres: LoadState<List<Genre>>,
    openGenre: (Genre) -> Unit,
    retry: () -> Unit
) {
    when (genres) {
        LoadState.Loading -> LoadingScreen()
        is LoadState.Failed -> ScreenMessage(genres.message, retry)
        is LoadState.Ready -> {
            if (genres.value.isEmpty()) {
                ScreenMessage("No genres found.")
                return
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(148.dp),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Browse by genre",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(genres.value, key = Genre::id) { genre ->
                    Surface(
                        onClick = { openGenre(genre) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            genre.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun MovieListScreen(
    movies: LazyPagingItems<Movie>,
    listState: LazyListState,
    openMovie: (Movie) -> Unit
) {
    if (movies.itemCount == 0 && movies.loadState.refresh is PagingLoadState.Loading) {
        LoadingScreen()
        return
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(movies.itemCount, key = movies.itemKey(Movie::id)) { index ->
            movies[index]?.let { MovieRow(it, openMovie) }
        }
        item(key = "movie_footer") {
            PagingFooter(movies, "No movies found for this genre.")
        }
    }
}

@Composable
internal fun FavoritesScreen(favorites: LoadState<List<Movie>>, openMovie: (Movie) -> Unit) {
    when (favorites) {
        LoadState.Loading -> LoadingScreen()
        is LoadState.Failed -> ScreenMessage(favorites.message)
        is LoadState.Ready -> {
            if (favorites.value.isEmpty()) {
                ScreenMessage("No favorites yet.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites.value, key = Movie::id) { MovieRow(it, openMovie) }
                }
            }
        }
    }
}

@Composable
private fun MovieRow(movie: Movie, openMovie: (Movie) -> Unit) {
    Surface(
        onClick = { openMovie(movie) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Poster(movie.posterPath, Modifier.size(width = 64.dp, height = 96.dp))
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    movie.releaseDate?.take(4)?.ifBlank { null } ?: "Release date unavailable",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    ratingLabel(movie.rating, movie.voteCount),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
internal fun MovieDetailScreen(
    movie: Movie,
    details: LoadState<MovieDetails>,
    reviews: LazyPagingItems<Review>,
    favorite: Boolean?,
    savingFavorite: Boolean,
    favoriteError: String?,
    setFavorite: (Boolean) -> Unit,
    retryDetails: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "details") {
            val fullMovie = (details as? LoadState.Ready)?.value
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Poster(fullMovie?.posterPath ?: movie.posterPath, Modifier.size(100.dp, 150.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        fullMovie?.title ?: movie.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val metadata = listOfNotNull(
                        (fullMovie?.releaseDate ?: movie.releaseDate)?.takeIf(String::isNotBlank),
                        fullMovie?.runtime?.takeIf { it > 0 }?.let { "$it min" }
                    )
                    if (metadata.isNotEmpty()) {
                        Text(
                            metadata.joinToString(" • "),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        ratingLabel(fullMovie?.rating ?: movie.rating, fullMovie?.voteCount ?: movie.voteCount),
                        color = MaterialTheme.colorScheme.primary
                    )
                    val genreNames = fullMovie?.genres.orEmpty().joinToString(", ") { it.name }
                    if (genreNames.isNotBlank()) {
                        Text(genreNames, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (favorite != null) {
                Button(
                    onClick = { setFavorite(!favorite) },
                    enabled = !savingFavorite,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(if (favorite) "Remove from favorites" else "Add to favorites")
                }
            }
            favoriteError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            when (details) {
                LoadState.Loading -> Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                }
                is LoadState.Failed -> Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                    Message(details.message, retryDetails)
                }
                is LoadState.Ready -> Column(
                    Modifier.padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Synopsis", style = MaterialTheme.typography.titleLarge)
                    Text(
                        details.value.overview?.ifBlank { null } ?: "No synopsis available.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Trailer(details.value.videos?.results.orEmpty())
                }
            }
        }
        item(key = "reviews_heading") {
            HorizontalDivider(Modifier.padding(bottom = 16.dp))
            Text("User reviews", style = MaterialTheme.typography.titleLarge)
        }
        items(reviews.itemCount, key = reviews.itemKey { "review-${it.id}" }) { index ->
            val review = reviews[index] ?: return@items
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(review.author, style = MaterialTheme.typography.titleMedium)
                    Text(review.content, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item(key = "reviews_footer") {
            PagingFooter(reviews, "No reviews yet.")
        }
    }
}

@Composable
private fun Poster(path: String?, modifier: Modifier = Modifier) {
    AsyncImage(
        model = path?.takeIf(String::isNotBlank)?.let { "https://image.tmdb.org/t/p/w342$it" },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun Trailer(videos: List<Video>) {
    val trailer = selectTrailer(videos)
    val uriHandler = LocalUriHandler.current
    var openFailed by remember(trailer?.key) { mutableStateOf(false) }

    if (trailer == null) {
        Text("No YouTube trailer available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        Button(onClick = {
            openFailed = try {
                uriHandler.openUri("https://www.youtube.com/watch?v=${trailer.key}")
                false
            } catch (_: IllegalArgumentException) {
                true
            }
        }) {
            Text("Watch trailer on YouTube")
        }
        if (openFailed) {
            Text("Could not open YouTube.", color = MaterialTheme.colorScheme.error)
        }
    }
}

internal fun selectTrailer(videos: List<Video>): Video? {
    val trailers = videos.filter {
        it.site.equals("YouTube", ignoreCase = true) &&
            it.type.equals("Trailer", ignoreCase = true) &&
            it.key.matches(Regex("[A-Za-z0-9_-]{11}"))
    }
    return trailers.firstOrNull { it.official } ?: trailers.firstOrNull()
}

@Composable
private fun PagingFooter(items: LazyPagingItems<*>, emptyMessage: String) {
    val status = if (items.itemCount == 0) items.loadState.refresh else items.loadState.append
    if (status is PagingLoadState.NotLoading && items.itemCount > 0) return

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            PagingLoadState.Loading -> CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            is PagingLoadState.Error -> Message(status.error.tmdbErrorMessage(), items::retry)
            is PagingLoadState.NotLoading -> Message(emptyMessage)
        }
    }
}

private fun ratingLabel(rating: Double, votes: Int): String =
    if (votes > 0) String.format(Locale.getDefault(), "%.1f / 10", rating) else "Not rated"

private val previewMovie = Movie(
    id = 1,
    title = "The Last Signal",
    releaseDate = "2025-08-15",
    posterPath = null,
    rating = 8.2,
    voteCount = 240
)

@Preview(name = "Movie List", widthDp = 360, heightDp = 640)
@Composable
private fun MovieListPreview() {
    val movies = remember {
        MutableStateFlow(
            PagingData.from(listOf(previewMovie, previewMovie.copy(id = 2, title = "Beyond the Horizon")))
        )
    }.collectAsLazyPagingItems()

    MovieScreenPreview {
        MovieListScreen(movies, rememberLazyListState(), openMovie = {})
    }
}

@Preview(name = "Movie Detail and Reviews", widthDp = 360, heightDp = 740)
@Composable
private fun MovieDetailPreview() {
    val reviews = remember {
        MutableStateFlow(
            PagingData.from(listOf(Review("1", "Sam", "The atmosphere stayed with me after the credits.")))
        )
    }.collectAsLazyPagingItems()

    MovieScreenPreview {
        MovieDetailScreen(
            movie = previewMovie,
            details = LoadState.Ready(
                MovieDetails(
                    title = previewMovie.title,
                    overview = "A radio operator follows a mysterious signal to an abandoned research station.",
                    runtime = 108,
                    genres = listOf(Genre(878, "Science Fiction")),
                    releaseDate = previewMovie.releaseDate,
                    posterPath = previewMovie.posterPath,
                    rating = previewMovie.rating,
                    voteCount = previewMovie.voteCount,
                    videos = null
                )
            ),
            reviews = reviews,
            favorite = false,
            savingFavorite = false,
            favoriteError = null,
            setFavorite = {},
            retryDetails = {}
        )
    }
}

@Preview(name = "Favorites", widthDp = 360, heightDp = 640)
@Composable
private fun FavoritesPreview() {
    MovieScreenPreview {
        FavoritesScreen(LoadState.Ready(listOf(previewMovie)), openMovie = {})
    }
}

@Composable
internal fun MovieScreenPreview(content: @Composable () -> Unit) {
    MovieGuideTheme {
        Surface(modifier = Modifier.fillMaxSize(), content = content)
    }
}
