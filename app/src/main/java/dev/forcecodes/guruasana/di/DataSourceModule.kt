package dev.forcecodes.guruasana.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.guruasana.data.auth.UserAuthDataSourceImpl
import dev.forcecodes.guruasana.data.auth.UserDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindsAuthStateDataSource(dataSource: UserAuthDataSourceImpl): UserDataSource
}