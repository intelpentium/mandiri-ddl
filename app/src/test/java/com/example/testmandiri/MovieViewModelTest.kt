package com.example.testmandiri

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
import com.example.testmandiri.ui.LoadState
import com.example.testmandiri.ui.MovieViewModel
import com.example.testmandiri.ui.selectTrailer
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieViewModelTest {
    private val api = FakeTmdbApi()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsGenresAndRetriesMovieDetails() = runTest {
        val viewModel = MovieViewModel(MovieRepository(api), EmptyFavorites())
        advanceUntilIdle()
        assertEquals(LoadState.Ready(listOf(action)), viewModel.state.value.genres)

        viewModel.openGenre(action)
        api.failDetails = true
        viewModel.openMovie(firstMovie)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.details is LoadState.Failed)

        api.failDetails = false
        viewModel.loadDetails()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.details is LoadState.Ready)

        viewModel.back()
        assertEquals(action, viewModel.state.value.genre)
        assertNull(viewModel.state.value.movie)
    }

    @Test
    fun prefersOfficialYoutubeTrailer() {
        val unofficial = Video("YouTube", "Trailer", "abc123DEF45", official = false)
        val official = Video("YouTube", "Trailer", "xyz123DEF45", official = true)
        val teaser = official.copy(type = "Teaser")
        val vimeo = official.copy(site = "Vimeo")

        assertEquals(official, selectTrailer(listOf(teaser, vimeo, unofficial, official)))
        assertEquals(unofficial, selectTrailer(listOf(teaser, unofficial)))
        assertNull(selectTrailer(listOf(teaser, vimeo, official.copy(key = "bad key"))))
    }

    private class FakeTmdbApi : TmdbApi {
        var failDetails = false

        override suspend fun genres() = GenreResponse(listOf(action))

        override suspend fun movies(genreId: Int, page: Int) = Page(page, 1, listOf(firstMovie))

        override suspend fun movie(movieId: Int): MovieDetails {
            if (failDetails) throw IOException("Offline")
            return MovieDetails("First film", "Synopsis", 100, listOf(action), null, null, 8.0, 1, null)
        }

        override suspend fun reviews(movieId: Int, page: Int) = Page<Review>(page, 1, emptyList())
    }

    private class EmptyFavorites : FavoriteMovies {
        override fun observeMovies() = flowOf(emptyList<Movie>())
        override suspend fun setSaved(movie: Movie, saved: Boolean) = Unit
    }

    private companion object {
        val action = Genre(28, "Action")
        val firstMovie = Movie(1, "First film", null, null, 8.0, 1)
    }
}
