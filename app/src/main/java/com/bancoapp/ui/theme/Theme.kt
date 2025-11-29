package com.bancoapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF018786),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF03DAC5),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF018786),
    onTertiaryContainer = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color.White,
    outline = Color(0xFF444444)
)

private val LightColorScheme = lightColorScheme(
    primary = PastelPurple,
    onPrimary = TextDark,
    primaryContainer = DarkPastelPurple,
    onPrimaryContainer = TextDark,
    secondary = PastelBlue,
    onSecondary = TextDark,
    secondaryContainer = DarkPastelBlue,
    onSecondaryContainer = TextDark,
    tertiary = PastelPink,
    onTertiary = TextDark,
    tertiaryContainer = DarkPastelPink,
    onTertiaryContainer = TextDark,
    error = Color(0xFFFFCDD2),
    onError = TextDark,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    surfaceVariant = PastelPeach,
    onSurfaceVariant = TextLight,
    outline = TextLight
)

@Composable
fun BancoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
