package dev.forcecodes.guru.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.guru.data.auth.UserAuthDataSourceImpl
import dev.forcecodes.guru.data.auth.UserDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindsAuthStateDataSource(dataSource: UserAuthDataSourceImpl): UserDataSource
}