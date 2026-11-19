package com.example.testmandiri

import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingSource.LoadParams
import com.example.testmandiri.data.Page
import com.example.testmandiri.data.TmdbPagingSource
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TmdbPagingSourceTest {
    @Test
    fun skipsDuplicateMoviesAndStopsAtTheLastPage() = runTest {
        val pages = listOf(listOf(1, 2), listOf(2), listOf(2, 3))
        val source = TmdbPagingSource({ id: Int -> id }) { page ->
            Page(page, pages.size, pages[page - 1])
        }

        val first = source.load(LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
            as LoadResult.Page
        val last = source.load(LoadParams.Append(first.nextKey!!, 20, false)) as LoadResult.Page

        assertEquals(listOf(1, 2), first.data)
        assertEquals(listOf(3), last.data)
        assertNull(last.nextKey)
    }

    @Test
    fun failedAppendCanRetryTheSamePage() = runTest {
        var fail = true
        val source = TmdbPagingSource({ id: Int -> id }) { page ->
            if (page == 2 && fail) throw IOException("Offline")
            Page(page, 2, listOf(page))
        }
        source.load(LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val append = LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false)

        assertTrue(source.load(append) is LoadResult.Error)
        fail = false
        val last = source.load(append) as LoadResult.Page
        assertEquals(listOf(2), last.data)
        assertNull(last.nextKey)
    }
}
