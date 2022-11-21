package dev.forcecodes.guruasana.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.forcecodes.guruasana.model.CustomizePose
import dev.forcecodes.guruasana.model.PostProcessMetrics

@Database(
    entities = [CustomizePose::class, PostProcessMetrics::class],
    exportSchema = false,
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customizePosesDao(): CustomizePosesDao
    abstract fun postProcessMetricsDao(): PostProcessMetricsDao
}