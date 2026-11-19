package com.example.testmandiri

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.testmandiri.data.FavoriteDatabase
import com.example.testmandiri.data.Movie
import com.example.testmandiri.data.RoomFavoriteMovies
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoritesPersistenceTest {
    @Test
    fun favoritesSurviveDatabaseReopenAndCanBeRemoved() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "favorites-test-${UUID.randomUUID()}.db"
        val movie = Movie(42, "Example film", null, null, 8.0, 1)

        try {
            val first = Room.databaseBuilder(context, FavoriteDatabase::class.java, databaseName).build()
            try {
                val favorites = RoomFavoriteMovies(first.favoriteMovieDao())
                favorites.setSaved(movie, true)
                favorites.setSaved(movie, true)
            } finally {
                first.close()
            }

            val reopened = Room.databaseBuilder(context, FavoriteDatabase::class.java, databaseName).build()
            try {
                val favorites = RoomFavoriteMovies(reopened.favoriteMovieDao())
                assertEquals(listOf(movie), favorites.observeMovies().first())
                favorites.setSaved(movie, false)
                assertEquals(emptyList<Movie>(), favorites.observeMovies().first())
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
        Unit
    }
}
