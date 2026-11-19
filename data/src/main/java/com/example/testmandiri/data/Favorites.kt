package com.example.testmandiri.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FavoriteMovies {
    fun observeMovies(): Flow<List<Movie>>
    suspend fun setSaved(movie: Movie, saved: Boolean)
}

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val releaseDate: String?,
    val posterPath: String?,
    val rating: Double,
    val voteCount: Int,
    val savedAt: Long
)

@Dao
interface FavoriteMovieDao {
    @Query("SELECT * FROM favorite_movies ORDER BY savedAt DESC, id DESC")
    fun observeMovies(): Flow<List<FavoriteMovieEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(movie: FavoriteMovieEntity)

    @Query("DELETE FROM favorite_movies WHERE id = :id")
    suspend fun delete(id: Int)
}

@Database(entities = [FavoriteMovieEntity::class], version = 1)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao
}

class RoomFavoriteMovies @Inject constructor(private val dao: FavoriteMovieDao) : FavoriteMovies {
    override fun observeMovies(): Flow<List<Movie>> = dao.observeMovies().map { favorites ->
        favorites.map { Movie(it.id, it.title, it.releaseDate, it.posterPath, it.rating, it.voteCount) }
    }

    override suspend fun setSaved(movie: Movie, saved: Boolean) {
        if (saved) {
            dao.insert(
                FavoriteMovieEntity(
                    movie.id, movie.title, movie.releaseDate, movie.posterPath,
                    movie.rating, movie.voteCount, System.currentTimeMillis()
                )
            )
        } else {
            dao.delete(movie.id)
        }
    }
}
