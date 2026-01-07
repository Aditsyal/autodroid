package com.aditsyal.autodroid

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.aditsyal.autodroid.domain.usecase.InitializeTriggersUseCase
import com.aditsyal.autodroid.domain.usecase.InitializeDefaultTemplatesUseCase
import com.aditsyal.autodroid.data.local.database.AutomationDatabase
import com.aditsyal.autodroid.utils.PerformanceMonitor
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AutodroidApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var initializeTriggersUseCase: InitializeTriggersUseCase

    @Inject
    lateinit var initializeDefaultTemplatesUseCase: InitializeDefaultTemplatesUseCase

    @Inject
    lateinit var database: AutomationDatabase

    @Inject
    lateinit var performanceMonitor: PerformanceMonitor

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Pre-warm database connection on background thread
        applicationScope.launch {
            try {
                database.queryExecutor.execute {
                    database.openHelper.readableDatabase
                    Timber.d("Database pre-warmed")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to pre-warm database")
            }
        }

        // Initialize Timber for logging
        // Check if app is debuggable instead of using BuildConfig
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize triggers on app start
        applicationScope.launch {
            try {
                Timber.d("Initializing triggers on app start")
                initializeTriggersUseCase()
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize triggers on app start")
            }
        }

        // Initialize default templates
        applicationScope.launch {
            try {
                Timber.d("Initializing default templates")
                initializeDefaultTemplatesUseCase()
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize default templates")
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Clear performance monitoring data when system is low on memory
        if (level >= TRIM_MEMORY_BACKGROUND) {
            performanceMonitor.clear()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
