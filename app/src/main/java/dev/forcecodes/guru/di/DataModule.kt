package dev.forcecodes.guru.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides

@Module
object DataModule {

    @Provides
    fun providesFirebaseAuth() = FirebaseAuth.getInstance()
}