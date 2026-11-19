package com.example.testmandiri

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import com.example.testmandiri.data.FavoriteMovies
import com.example.testmandiri.data.Genre
import com.example.testmandiri.data.GenreResponse
import com.example.testmandiri.data.Movie
import com.example.testmandiri.data.MovieDetails
import com.example.testmandiri.data.MovieRepository
import com.example.testmandiri.data.Page
import com.example.testmandiri.data.Review
import com.example.testmandiri.data.TmdbApi
import com.example.testmandiri.data.Video
import com.example.testmandiri.data.VideoResponse
import com.example.testmandiri.ui.MovieGuide
import com.example.testmandiri.ui.MovieViewModel
import com.example.testmandiri.ui.theme.MovieGuideTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Rule
import org.junit.Test

class MovieGuideTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun browsesGenreAndLoadsMoreMoviesAndReviews() {
        val viewModel = MovieViewModel(MovieRepository(SampleApi()), InMemoryFavorites())
        compose.setContent { MovieGuideTheme { MovieGuide(viewModel) } }

        compose.onNodeWithText("Action").performClick()
        waitForText("Film 1")
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Film 21"))
        compose.onNodeWithText("Film 21").performClick()

        waitForText("Watch trailer on YouTube")
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Reviewer 21"))
        compose.onNodeWithText("Reviewer 21").assertExists()

        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Add to favorites"))
        compose.onNodeWithText("Add to favorites").performClick()
        waitForText("Remove from favorites")
        repeat(2) { compose.onNodeWithText("‹ Back").performClick() }

        compose.onNodeWithText("Favorites").performClick()
        waitForText("Film 21")
        compose.onNodeWithText("Film 21").assertExists()
    }

    private fun waitForText(text: String) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private class InMemoryFavorites : FavoriteMovies {
        private val movies = MutableStateFlow(emptyList<Movie>())

        override fun observeMovies() = movies

        override suspend fun setSaved(movie: Movie, saved: Boolean) {
            movies.update { current ->
                val remaining = current.filterNot { it.id == movie.id }
                if (saved) remaining + movie else remaining
            }
        }
    }

    private class SampleApi : TmdbApi {
        private val genre = Genre(28, "Action")

        override suspend fun genres() = GenreResponse(listOf(genre))

        override suspend fun movies(genreId: Int, page: Int): Page<Movie> {
            val ids = if (page == 1) 1..20 else 21..21
            return Page(page, 2, ids.map { Movie(it, "Film $it", null, null, 8.0, 1) })
        }

        override suspend fun movie(movieId: Int) = MovieDetails(
            title = "Film $movieId",
            overview = "Example synopsis",
            runtime = 120,
            genres = listOf(genre),
            releaseDate = "2025-01-01",
            posterPath = null,
            rating = 8.0,
            voteCount = 1,
            videos = VideoResponse(listOf(Video("YouTube", "Trailer", "abc123DEF45", true)))
        )

        override suspend fun reviews(movieId: Int, page: Int): Page<Review> {
            val ids = if (page == 1) 1..20 else 21..21
            return Page(page, 2, ids.map { Review("$it", "Reviewer $it", "Example review.") })
        }
    }
}
