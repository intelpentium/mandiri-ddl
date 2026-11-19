package com.example.testmandiri.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("genre/movie/list")
    suspend fun genres(): GenreResponse

    @GET("discover/movie?include_adult=false&sort_by=popularity.desc")
    suspend fun movies(@Query("with_genres") genreId: Int, @Query("page") page: Int): Page<Movie>

    @GET("movie/{id}?append_to_response=videos")
    suspend fun movie(@Path("id") movieId: Int): MovieDetails

    @GET("movie/{id}/reviews")
    suspend fun reviews(@Path("id") movieId: Int, @Query("page") page: Int): Page<Review>
}

data class GenreResponse(val genres: List<Genre>)

data class Genre(val id: Int, val name: String)

data class Page<T>(
    val page: Int,
    @SerializedName("total_pages") val totalPages: Int,
    val results: List<T>
)

data class Movie(
    val id: Int,
    val title: String,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val rating: Double,
    @SerializedName("vote_count") val voteCount: Int
)

data class MovieDetails(
    val title: String,
    val overview: String?,
    val runtime: Int?,
    val genres: List<Genre>,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val rating: Double,
    @SerializedName("vote_count") val voteCount: Int,
    val videos: VideoResponse?
)

data class VideoResponse(val results: List<Video>)

data class Video(val site: String, val type: String, val key: String, val official: Boolean)

data class Review(val id: String, val author: String, val content: String)
