package com.example.testmandiri

import com.example.testmandiri.data.MissingTmdbToken
import com.example.testmandiri.data.MovieRepository
import com.example.testmandiri.data.createTmdbApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException

class MovieRepositoryTest {
    @get:Rule
    val server = MockWebServer()

    @Test
    fun discoversMoviesWithGenrePageAndBearerToken() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                    "page": 2,
                    "total_pages": 4,
                    "results": [{
                        "id": 7,
                        "title": "Example film",
                        "poster_path": null,
                        "release_date": "2025-01-01",
                        "vote_average": 7.5,
                        "vote_count": 10
                    }]
                }
                """.trimIndent()
            )
        )
        val api = createTmdbApi("test-token", server.url("/3/").toString())
        val response = api.movies(28, 2)
        val request = server.takeRequest()

        assertEquals("/3/discover/movie", request.requestUrl?.encodedPath)
        assertEquals("28", request.requestUrl?.queryParameter("with_genres"))
        assertEquals("2", request.requestUrl?.queryParameter("page"))
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
        assertEquals(4, response.totalPages)
        assertEquals("2025-01-01", response.results.single().releaseDate)
    }

    @Test
    fun authenticatesWithV3ApiKeyWithoutReplacingGenreOrPage() = runBlocking {
        val key = "0123456789abcdef0123456789abcdef"
        server.enqueue(MockResponse().setBody("""{"page":2,"total_pages":2,"results":[]}"""))
        val api = createTmdbApi(key, server.url("/3/").toString())

        api.movies(28, 2)

        val request = server.takeRequest()
        assertEquals(key, request.requestUrl?.queryParameter("api_key"))
        assertEquals("28", request.requestUrl?.queryParameter("with_genres"))
        assertEquals("2", request.requestUrl?.queryParameter("page"))
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun rejectsMissingTokenBeforeMakingRequest() = runBlocking {
        val repository = MovieRepository(createTmdbApi("", server.url("/3/").toString()))
        val result = runCatching { repository.genres() }

        check(result.exceptionOrNull() is MissingTmdbToken)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun preservesUnauthorizedStatusFromTmdb() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401))
        val repository = MovieRepository(createTmdbApi("bad-token", server.url("/3/").toString()))
        val result = runCatching { repository.genres() }

        assertEquals(401, (result.exceptionOrNull() as? HttpException)?.code())
    }
}
