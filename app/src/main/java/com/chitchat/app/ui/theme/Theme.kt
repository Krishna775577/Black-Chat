package com.chitchat.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF128C7E),
    onPrimary = Color.White,
    secondary = Color(0xFF25D366),
    onSecondary = Color(0xFF07151C),
    tertiary = Color(0xFF53BDEB),
    background = Color(0xFFF0F2F5),
    onBackground = Color(0xFF111B21),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111B21),
    surfaceVariant = Color(0xFFF7F8FA),
    onSurfaceVariant = Color(0xFF667781),
    outline = Color(0xFFE1E7EA),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF25D366),
    onPrimary = Color(0xFF08130F),
    secondary = Color(0xFF53BDEB),
    onSecondary = Color(0xFF07151C),
    tertiary = Color(0xFF9B8CFF),
    background = Color(0xFF0B141A),
    onBackground = Color(0xFFE7EEF3),
    surface = Color(0xFF111B21),
    onSurface = Color(0xFFE7EEF3),
    surfaceVariant = Color(0xFF202C33),
    onSurfaceVariant = Color(0xFF95A3AB),
    outline = Color(0xFF2B3B44),
)

@Composable
fun ChitChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
