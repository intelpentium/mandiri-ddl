package com.example.testmandiri.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MovieColors = darkColorScheme(
    primary = Color(0xFFF4B979),
    onPrimary = Color(0xFF352006),
    background = Color(0xFF13151B),
    onBackground = Color(0xFFF2EFEA),
    surface = Color(0xFF13151B),
    onSurface = Color(0xFFF2EFEA),
    surfaceVariant = Color(0xFF272B35),
    onSurfaceVariant = Color(0xFFC7C9D2),
    surfaceContainer = Color(0xFF1D2029),
    surfaceContainerHigh = Color(0xFF252934),
    outlineVariant = Color(0xFF3B4050)
)

@Composable
fun MovieGuideTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MovieColors,
        typography = Typography,
        content = content
    )
}
