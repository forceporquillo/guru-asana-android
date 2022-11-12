package dev.forcecodes.guruasana

import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.poseprocessor.PoseProcessorMultiDexApplication

@HiltAndroidApp
class GuruApplication : PoseProcessorMultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Logger.plant(Logger.DebugTree())
        }
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.45)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("svg_pose_image_cache"))
                    .maxSizePercent(0.04)
                    .build()
            }
            .build()

        YogaPosesFactory.singleton(this).map {
            it.englishName
        }.forEach {
            println(it)
        }
    }
}