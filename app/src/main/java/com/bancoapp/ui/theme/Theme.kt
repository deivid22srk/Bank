package com.bancoapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
