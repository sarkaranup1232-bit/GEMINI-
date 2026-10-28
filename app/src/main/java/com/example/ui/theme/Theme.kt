package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val NexusDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = NexusBlack,
    primaryContainer = NeonBlue.copy(alpha = 0.2f),
    onPrimaryContainer = NeonCyan,
    secondary = NeonPurple,
    onSecondary = NexusTextPrimary,
    secondaryContainer = NeonPurple.copy(alpha = 0.2f),
    onSecondaryContainer = NexusTextPrimary,
    tertiary = NeonPink,
    onTertiary = NexusTextPrimary,
    background = NexusBlack,
    onBackground = NexusTextPrimary,
    surface = NexusDarkSurface,
    onSurface = NexusTextPrimary,
    surfaceVariant = NexusCardBg,
    onSurfaceVariant = NexusTextSecondary,
    outline = NexusCardBorder
)

val NexusShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp) // Exact 24px/dp corners requested
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NexusBlack.toArgb()
            window.navigationBarColor = NexusBlack.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = NexusDarkColorScheme,
        typography = Typography,
        shapes = NexusShapes,
        content = content
    )
}
