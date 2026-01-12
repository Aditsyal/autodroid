package com.aditsyal.autodroid

import android.app.Application
import android.content.Context
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
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
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

        // Setup crash logging with custom exception handler
        setupCrashLogging()

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

    private fun setupCrashLogging() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(CrashLoggingHandler(defaultHandler, this))
        Timber.d("Crash logging handler installed")
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

    /**
     * Custom UncaughtExceptionHandler that logs crashes to file for export.
     */
    private class CrashLoggingHandler(
        private val defaultHandler: Thread.UncaughtExceptionHandler?,
        private val application: Application
    ) : Thread.UncaughtExceptionHandler {

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            try {
                // Log crash to Timber
                Timber.e(throwable, "Uncaught exception in thread ${thread.name}")

                // Write crash details to file
                writeCrashToFile(thread, throwable)

            } catch (e: Exception) {
                Timber.e(e, "Failed to handle uncaught exception")
            } finally {
                // Always call the default handler to ensure the app crashes properly
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        private fun writeCrashToFile(thread: Thread, throwable: Throwable) {
            try {
                val crashDir = File(application.filesDir, "crashes")
                if (!crashDir.exists()) {
                    crashDir.mkdirs()
                }

                // Clean up old crash logs (keep last 10)
                cleanupOldCrashLogs(crashDir)

                val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                    .format(Date())
                val crashFile = File(crashDir, "crash_$timestamp.txt")

                FileOutputStream(crashFile).use { fos ->
                    PrintWriter(fos).use { writer ->
                        writer.println("=== AutoDroid Crash Report ===")
                        writer.println("Timestamp: ${Date()}")
                        writer.println("Thread: ${thread.name} (${thread.id})")
                        writer.println("Android Version: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                        writer.println("Device: ${android.os.Build.MODEL} (${android.os.Build.MANUFACTURER})")
                        writer.println()

                        writer.println("=== Stack Trace ===")
                        val sw = StringWriter()
                        throwable.printStackTrace(PrintWriter(sw))
                        writer.println(sw.toString())

                        writer.println()
                        writer.println("=== System Information ===")
                        writer.println("Available Memory: ${getAvailableMemory()} MB")
                        writer.println("Total Memory: ${getTotalMemory()} MB")
                        writer.println("Free Storage: ${getFreeStorage()} MB")
                    }
                }

                Timber.d("Crash logged to file: ${crashFile.absolutePath}")

            } catch (e: Exception) {
                Timber.e(e, "Failed to write crash to file")
            }
        }

        private fun cleanupOldCrashLogs(crashDir: File) {
            try {
                val crashFiles: Array<File> = crashDir.listFiles { file ->
                    file.name.startsWith("crash_") && file.name.endsWith(".txt")
                } ?: emptyArray()

                val sortedFiles = crashFiles.sortedByDescending { it.lastModified() }

                if (sortedFiles.size > 10) {
                    sortedFiles.drop(10).forEach { file ->
                        try {
                            file.delete()
                            Timber.d("Deleted old crash log: ${file.name}")
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to delete old crash log: ${file.name}")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to cleanup old crash logs")
            }
        }

        private fun getAvailableMemory(): Long {
            return try {
                val mi = android.app.ActivityManager.MemoryInfo()
                val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                activityManager.getMemoryInfo(mi)
                mi.availMem / (1024 * 1024) // Convert to MB
            } catch (e: Exception) {
                -1L
            }
        }

        private fun getTotalMemory(): Long {
            return try {
                val mi = android.app.ActivityManager.MemoryInfo()
                val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                activityManager.getMemoryInfo(mi)
                mi.totalMem / (1024 * 1024) // Convert to MB
            } catch (e: Exception) {
                -1L
            }
        }

        private fun getFreeStorage(): Long {
            return try {
                val stat = android.os.StatFs(application.filesDir.absolutePath)
                val bytesAvailable = stat.availableBytes
                bytesAvailable / (1024 * 1024) // Convert to MB
            } catch (e: Exception) {
                -1L
            }
        }
    }
}
