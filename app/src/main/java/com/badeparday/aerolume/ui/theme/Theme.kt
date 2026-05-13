package com.badeparday.aerolume.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val AerolumeColorScheme =
    darkColorScheme(
        primary = AviationBlue,
        onPrimary = Ink950,
        secondary = Mist,
        tertiary = SoftGreen,
        background = Ink950,
        onBackground = Frost,
        surface = Slate850,
        onSurface = Frost,
        surfaceVariant = Slate800,
        onSurfaceVariant = Mist,
        outline = Color.White.copy(alpha = 0.12f),
        error = RoseAlert,
    )

private val AerolumeShapes =
    Shapes(
        extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    )

internal val LocalExtendedColors = staticCompositionLocalOf { DefaultExtendedColors }

@Composable
fun AerolumeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Color.Transparent.toArgb()
                } else {
                    Ink950.toArgb()
                }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides AerolumeSpacing(),
        LocalExtendedColors provides DefaultExtendedColors,
    ) {
        MaterialTheme(
            colorScheme = AerolumeColorScheme,
            typography = AerolumeTypography,
            shapes = AerolumeShapes,
            content = content,
        )
    }
}

object AerolumeThemeTokens {
    val spacing: AerolumeSpacing
        @Composable get() = LocalSpacing.current

    val colors: AerolumeExtendedColors
        @Composable get() = LocalExtendedColors.current
}
