package com.badeparday.aerolume.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AerolumeLayoutMode {
    Phone,
    TabletPortrait,
    TabletLandscape,
}

data class AerolumeWindowState(
    val mode: AerolumeLayoutMode,
    val inspectorWidth: Dp,
    val toolRailInset: Dp,
) {
    val isTablet: Boolean
        get() = mode != AerolumeLayoutMode.Phone

    val isLandscapeTablet: Boolean
        get() = mode == AerolumeLayoutMode.TabletLandscape
}

fun rememberAerolumeWindowState(windowSizeClass: WindowSizeClass): AerolumeWindowState {
    val mode =
        when {
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded -> AerolumeLayoutMode.TabletLandscape
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium &&
                windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact -> {
                AerolumeLayoutMode.TabletPortrait
            }
            else -> AerolumeLayoutMode.Phone
        }

    val inspectorWidth =
        when (mode) {
            AerolumeLayoutMode.Phone -> 0.dp
            AerolumeLayoutMode.TabletPortrait -> 364.dp
            AerolumeLayoutMode.TabletLandscape -> 404.dp
        }

    return AerolumeWindowState(
        mode = mode,
        inspectorWidth = inspectorWidth,
        toolRailInset = if (mode == AerolumeLayoutMode.Phone) 0.dp else 20.dp,
    )
}
