package com.example.testmandiri.data

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesModule {
    @Binds
    abstract fun bindFavorites(repository: RoomFavoriteMovies): FavoriteMovies

    companion object {
        @Provides
        @Singleton
        fun provideFavoriteDao(@ApplicationContext context: Context): FavoriteMovieDao =
            Room.databaseBuilder(context, FavoriteDatabase::class.java, "favorites.db")
                .build()
                .favoriteMovieDao()
    }
}
