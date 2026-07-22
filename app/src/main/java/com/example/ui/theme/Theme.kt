package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GuitarDarkColorScheme = darkColorScheme(
    primary = AmberGoldPrimary,
    onPrimary = Color(0xFF231200),
    primaryContainer = Color(0xFF422300),
    onPrimaryContainer = Color(0xFFFFDDB3),
    secondary = CyanAccent,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFFB6F0FF),
    tertiary = PurpleAccent,
    onTertiary = Color(0xFF38004B),
    tertiaryContainer = Color(0xFF531168),
    onTertiaryContainer = Color(0xFFF9D8FF),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = DarkSurfaceVariant,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = Color(0xFF44444C),
    outlineVariant = Color(0xFF2E2E34),
    error = RedError,
    onError = Color.White
)

@Composable
fun FretboardMasteryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GuitarDarkColorScheme,
        typography = Typography,
        content = content
    )
}

