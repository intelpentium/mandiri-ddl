package com.example.testmandiri.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException

internal class TmdbPagingSource<T : Any>(
    private val itemId: (T) -> Any,
    private val loadPage: suspend (Int) -> Page<T>
) : PagingSource<Int, T>() {
    private val loadedIds = mutableSetOf<Any>()

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        var pageNumber = params.key ?: 1
        try {
            while (true) {
                val page = loadPage(pageNumber)
                val nextPage = if (page.results.isNotEmpty() && pageNumber < minOf(page.totalPages, 500)) {
                    pageNumber + 1
                } else {
                    null
                }

                val items = page.results.distinctBy(itemId).filter { itemId(it) !in loadedIds }
                if (items.isNotEmpty() || nextPage == null) {
                    loadedIds.addAll(items.map(itemId))
                    return LoadResult.Page(items, prevKey = null, nextKey = nextPage)
                }
                pageNumber = nextPage
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            return LoadResult.Error(error)
        }
    }
}
