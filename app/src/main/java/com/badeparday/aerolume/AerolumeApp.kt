package com.badeparday.aerolume

import androidx.compose.runtime.Composable
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.badeparday.aerolume.data.AppContainer
import com.badeparday.aerolume.ui.navigation.AerolumeNavHost
import com.badeparday.aerolume.ui.theme.AerolumeTheme

@Composable
fun AerolumeApp(
    container: AppContainer,
    windowSizeClass: WindowSizeClass,
) {
    AerolumeTheme {
        AerolumeNavHost(
            container = container,
            windowSizeClass = windowSizeClass,
        )
    }
}
