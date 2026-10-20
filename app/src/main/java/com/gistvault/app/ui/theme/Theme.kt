package com.gistvault.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalGistVaultColors = staticCompositionLocalOf { LightPalette }

object GistTheme {
    val colors: GistVaultColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGistVaultColors.current
}

@Composable
fun GistVaultAppTheme(
    themeSetting: String = "system",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val palette = when (themeSetting) {
        "light" -> LightPalette
        "dark" -> DarkPalette
        "pink" -> PinkPalette
        else -> if (isSystemDark) DarkPalette else LightPalette
    }

    val materialColors = if (palette.isDark) {
        darkColorScheme(
            primary = palette.primary,
            background = palette.paper,
            surface = palette.card,
            onPrimary = palette.onPrimary,
            onBackground = palette.ink,
            onSurface = palette.ink
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            background = palette.paper,
            surface = palette.card,
            onPrimary = palette.onPrimary,
            onBackground = palette.ink,
            onSurface = palette.ink
        )
    }

    CompositionLocalProvider(LocalGistVaultColors provides palette) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
