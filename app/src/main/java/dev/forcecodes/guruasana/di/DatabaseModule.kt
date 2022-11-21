package dev.forcecodes.guruasana.di

import android.content.Context
import androidx.room.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.guruasana.data.db.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun instance(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "Guru.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun customizePosesDao(appDatabase: AppDatabase) = appDatabase.customizePosesDao()

    @Singleton
    @Provides
    fun poseProcessDao(appDatabase: AppDatabase) = appDatabase.postProcessMetricsDao()
}