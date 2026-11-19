package com.example.testmandiri.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MovieColors = lightColorScheme(
    primary = Color(0xFF8A460F),
    onPrimary = Color.White,
    background = Color(0xFFFFFBF6),
    onBackground = Color(0xFF241C16),
    surface = Color(0xFFFFFBF6),
    onSurface = Color(0xFF241C16),
    surfaceVariant = Color(0xFFF0E3D8),
    onSurfaceVariant = Color(0xFF594B40),
    surfaceContainer = Color(0xFFF7EFE6),
    surfaceContainerHigh = Color(0xFFF1E6DC),
    outlineVariant = Color(0xFFD8C8B9)
)

@Composable
fun MovieGuideTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MovieColors,
        typography = Typography,
        content = content
    )
}
