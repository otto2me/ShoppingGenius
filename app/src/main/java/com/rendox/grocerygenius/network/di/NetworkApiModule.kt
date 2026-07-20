package com.rendox.grocerygenius.network.di

import com.rendox.grocerygenius.BuildConfig
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.logging.HttpLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkApiModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()
}