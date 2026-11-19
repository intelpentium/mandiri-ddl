package com.example.testmandiri.data

import java.io.IOException
import retrofit2.HttpException

fun Throwable.tmdbErrorMessage(): String = when (this) {
    is MissingTmdbToken -> "Set TMDB_READ_ACCESS_TOKEN and rebuild the app."
    is HttpException -> when (code()) {
        401, 403 -> "TMDB rejected access. Check the API key or Read Access Token."
        404 -> "TMDB could not find this item."
        429 -> "Too many requests. Wait a moment and try again."
        else -> "TMDB is unavailable right now."
    }
    is IOException -> "Could not connect to TMDB. Check your connection."
    else -> "Could not read data from TMDB."
}
