package com.example.testmandiri

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.testmandiri.data.Genre
import com.example.testmandiri.ui.GenreScreen
import com.example.testmandiri.ui.LoadState
import com.example.testmandiri.ui.MovieGuide
import com.example.testmandiri.ui.MovieViewModel
import com.example.testmandiri.ui.theme.MovieGuideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lightBars = SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = 0x99000000.toInt())
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        setContent {
            MovieGuideTheme {
                MovieGuide(viewModel)
            }
        }
    }
}

@Preview(name = "Genre - Light", widthDp = 360, heightDp = 640)
@Composable
private fun GenreScreenPreview() {
    MovieGuideTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            GenreScreen(
                genres = LoadState.Ready(
                    listOf(
                        Genre(28, "Action"),
                        Genre(12, "Adventure"),
                        Genre(35, "Comedy"),
                        Genre(18, "Drama"),
                        Genre(14, "Fantasy"),
                        Genre(878, "Science Fiction")
                    )
                ),
                openGenre = {},
                retry = {}
            )
        }
    }
}
