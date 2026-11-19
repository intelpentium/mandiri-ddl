package com.example.testmandiri.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideTmdbApi(): TmdbApi =
        createTmdbApi(BuildConfig.TMDB_READ_ACCESS_TOKEN, "https://api.themoviedb.org/3/")
}

internal fun createTmdbApi(token: String, baseUrl: String): TmdbApi {
    val isApiKey = token.matches(Regex("[0-9a-fA-F]{32}"))
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            if (token.isBlank()) throw MissingTmdbToken()
            val request = chain.request()
            val authenticated = if (isApiKey) {
                val url = request.url().newBuilder().setQueryParameter("api_key", token).build()
                request.newBuilder().url(url)
            } else {
                request.newBuilder().header("Authorization", "Bearer $token")
            }
            chain.proceed(authenticated.build())
        }
        .build()

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TmdbApi::class.java)
}

class MissingTmdbToken : IOException()
