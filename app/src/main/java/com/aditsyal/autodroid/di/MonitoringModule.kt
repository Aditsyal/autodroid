package com.aditsyal.autodroid.di

import com.aditsyal.autodroid.utils.MemoryMonitor
import com.aditsyal.autodroid.utils.PerformanceMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonitoringModule {

    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor()
    }

    @Provides
    @Singleton
    fun provideMemoryMonitor(): MemoryMonitor {
        return MemoryMonitor()
    }
}
