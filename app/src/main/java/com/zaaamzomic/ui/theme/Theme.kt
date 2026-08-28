package com.zaaamzomic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = Hanko,
    onPrimary = PaperIvory,
    primaryContainer = HankoBg,
    background = PaperIvory,
    onBackground = Sumi,
    surface = SpineMist,
    onSurface = Sumi,
    surfaceVariant = SpineMist2,
    outline = Outline,
    error = Hanko,
)

private val DarkScheme = darkColorScheme(
    primary = Hanko2,
    onPrimary = Sumi,
    primaryContainer = SumiVariant,
    background = Sumi,
    onBackground = PaperIvory,
    surface = SumiSurface,
    onSurface = PaperIvory,
    surfaceVariant = SumiVariant,
    outline = OutlineDark,
    error = Hanko2,
)

@Composable
fun ZomicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = ZomicTypography,
        content = content,
    )
}
