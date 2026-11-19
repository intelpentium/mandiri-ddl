package com.example.testmandiri.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.testmandiri.data.FavoriteMovies
import com.example.testmandiri.data.Genre
import com.example.testmandiri.data.Movie
import com.example.testmandiri.data.MovieDetails
import com.example.testmandiri.data.MovieRepository
import com.example.testmandiri.data.tmdbErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Ready<T>(val value: T) : LoadState<T>
    data class Failed(val message: String) : LoadState<Nothing>
}

data class MovieUiState(
    val genres: LoadState<List<Genre>> = LoadState.Loading,
    val genre: Genre? = null,
    val movie: Movie? = null,
    val details: LoadState<MovieDetails> = LoadState.Loading,
    val showingFavorites: Boolean = false,
    val savingFavorite: Boolean = false,
    val favoriteError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val favoriteMovies: FavoriteMovies
) : ViewModel() {
    private val _state = MutableStateFlow(MovieUiState())
    val state = _state.asStateFlow()

    val movies = state.map { it.genre?.id }.distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(PagingData.empty()) else repository.movies(id) }
        .cachedIn(viewModelScope)

    val reviews = state.map { it.movie?.id }.distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(PagingData.empty()) else repository.reviews(id) }
        .cachedIn(viewModelScope)

    val favorites = favoriteMovies.observeMovies()
        .map<List<Movie>, LoadState<List<Movie>>> { LoadState.Ready(it) }
        .catch { emit(LoadState.Failed("Could not read your favorites.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)

    private var genresJob: Job? = null
    private var detailsJob: Job? = null

    init {
        loadGenres()
    }

    fun loadGenres() {
        if (genresJob?.isActive == true) return
        _state.update { it.copy(genres = LoadState.Loading) }
        genresJob = viewModelScope.launch {
            val result = load { repository.genres() }
            _state.update { it.copy(genres = result) }
        }
    }

    fun openGenre(genre: Genre) {
        _state.update { it.copy(genre = genre) }
    }

    fun openFavorites() {
        _state.update { it.copy(showingFavorites = true) }
    }

    fun openMovie(movie: Movie) {
        detailsJob?.cancel()
        _state.update { it.copy(movie = movie, details = LoadState.Loading, favoriteError = null) }
        loadDetails()
    }

    fun loadDetails() {
        val movie = state.value.movie ?: return
        if (detailsJob?.isActive == true) return
        _state.update { it.copy(details = LoadState.Loading) }
        detailsJob = viewModelScope.launch {
            val result = load { repository.movie(movie.id) }
            _state.update { it.copy(details = result) }
        }
    }

    fun setFavorite(saved: Boolean) {
        val movie = state.value.movie ?: return
        if (state.value.savingFavorite) return
        _state.update { it.copy(savingFavorite = true, favoriteError = null) }

        viewModelScope.launch {
            try {
                favoriteMovies.setSaved(movie, saved)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _state.update {
                    if (it.movie?.id == movie.id) it.copy(favoriteError = "Could not update your favorites.") else it
                }
            } finally {
                _state.update { it.copy(savingFavorite = false) }
            }
        }
    }

    fun back() {
        if (state.value.movie != null) {
            detailsJob?.cancel()
            _state.update { it.copy(movie = null, details = LoadState.Loading, favoriteError = null) }
        } else {
            _state.update { it.copy(genre = null, showingFavorites = false) }
        }
    }

    private suspend fun <T> load(request: suspend () -> T): LoadState<T> = try {
        LoadState.Ready(request())
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        LoadState.Failed(error.tmdbErrorMessage())
    }
}
