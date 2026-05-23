package com.spendly.financetracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SpendlyGreen,
    onPrimary = Color.White,
    primaryContainer = SpendlyGreenLight,
    onPrimaryContainer = SpendlyGreenDark,
    secondary = SpendlyAmber,
    onSecondary = Color.White,
    error = SpendlyRed,
    errorContainer = SpendlyRedLight,
    onErrorContainer = SpendlyRedDark,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = SpendlyGray50,
    onSurface = SpendlyGray900,
    onSurfaceVariant = SpendlyGray700,
    outline = SpendlyGray300
)

@Composable
fun FinanceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val colorScheme = LightColorScheme

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpendlyTypography,
        content = content
    )
}
