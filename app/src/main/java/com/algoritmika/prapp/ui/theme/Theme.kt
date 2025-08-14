package com.algoritmika.prapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = AccentYellow,
    tertiary = AccentRed,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkCard,
    onSurface = Color.White,
    scrim = DarkBackground.copy(alpha = 0.6f)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = AccentYellow,
    tertiary = AccentRed,
    background = BeigeBackground,
    onBackground = DarkText,
    surface = Color.White,
    onSurface = DarkText,
    scrim = DarkBackground.copy(alpha = 0.6f)
)

@Composable
fun PrAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}