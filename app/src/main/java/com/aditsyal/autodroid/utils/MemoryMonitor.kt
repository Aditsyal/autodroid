package com.aditsyal.autodroid.utils

import com.aditsyal.autodroid.BuildConfig
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for monitoring memory usage.
 * Respects production boundaries and provides goal-based alerting.
 */
@Singleton
class MemoryMonitor @Inject constructor() {
    private val isEnabled = BuildConfig.DEBUG
    
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        return MemoryStats(
            usedMemory = runtime.totalMemory() - runtime.freeMemory(),
            freeMemory = runtime.freeMemory(),
            maxMemory = runtime.maxMemory(),
            heapSize = runtime.totalMemory()
        )
    }
    
    fun logMemoryUsage(tag: String) {
        if (!isEnabled) return
        
        val stats = getMemoryStats()
        val usedMb = stats.usedMemory / 1024 / 1024
        val maxMb = stats.maxMemory / 1024 / 1024
        
        Timber.d("$tag - Memory: ${usedMb}MB / ${maxMb}MB")
        
        // Alert if used memory exceeds goal (50MB as per performance report)
        if (usedMb > 50) {
            Timber.w("Memory usage optimization required: ${usedMb}MB (Context: $tag)")
        }
    }
    
    data class MemoryStats(
        val usedMemory: Long,
        val freeMemory: Long,
        val maxMemory: Long,
        val heapSize: Long
    )
}
