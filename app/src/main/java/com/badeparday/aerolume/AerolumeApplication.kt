package com.badeparday.aerolume

import android.app.Application
import com.badeparday.aerolume.data.AppContainer
import org.maplibre.android.MapLibre

class AerolumeApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer() }

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }
}
