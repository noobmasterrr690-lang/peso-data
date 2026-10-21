package com.aistudio.peso.ui.theme

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
    primary = PesoVoltGreen,
    onPrimary = Color(0xFF12140E),
    primaryContainer = Color(0xFF28340D),
    onPrimaryContainer = PesoVoltGreen,
    secondary = PesoGold,
    onSecondary = Color(0xFF1D1505),
    secondaryContainer = Color(0xFF382B12),
    onSecondaryContainer = PesoGoldLight,
    tertiary = PesoDarkIncome,
    onTertiary = Color(0xFF03260F),
    background = PesoDarkBg,
    onBackground = PesoDarkText,
    surface = PesoDarkPanel,
    onSurface = PesoDarkText,
    surfaceVariant = PesoDarkPanelVariant,
    onSurfaceVariant = PesoDarkMuted,
    outline = PesoDarkLine,
    error = PesoDarkWarning,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PesoLightGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = Color(0xFF14532D),
    secondary = PesoGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = PesoLightIncome,
    onTertiary = Color.White,
    background = PesoLightBg,
    onBackground = PesoLightText,
    surface = PesoLightPanel,
    onSurface = PesoLightText,
    surfaceVariant = PesoLightPanelVariant,
    onSurfaceVariant = PesoLightMuted,
    outline = PesoLightLine,
    error = PesoLightWarning,
    onError = Color.White
)

@Composable
fun PESOTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
