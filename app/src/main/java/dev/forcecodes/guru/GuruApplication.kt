package dev.forcecodes.guru

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.forcecodes.guru.logger.Logger

@HiltAndroidApp
class GuruApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Logger.plant(Logger.DebugTree())
        }
    }
}