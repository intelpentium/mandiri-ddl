package com.example.testmandiri.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import javax.inject.Inject

class MovieRepository @Inject constructor(private val api: TmdbApi) {
    suspend fun genres() = api.genres().genres

    fun movies(genreId: Int) = paged(Movie::id) { page -> api.movies(genreId, page) }

    suspend fun movie(movieId: Int) = api.movie(movieId)

    fun reviews(movieId: Int) = paged(Review::id) { page -> api.reviews(movieId, page) }

    private fun <T : Any> paged(itemId: (T) -> Any, loadPage: suspend (Int) -> Page<T>) =
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { TmdbPagingSource(itemId, loadPage) }
        ).flow
}
