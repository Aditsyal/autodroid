package com.aditsyal.autodroid.utils

import timber.log.Timber

object MemoryMonitor {
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
        val stats = getMemoryStats()
        Timber.d("$tag - Memory: ${stats.usedMemory / 1024 / 1024}MB / ${stats.maxMemory / 1024 / 1024}MB")
    }
    
    data class MemoryStats(
        val usedMemory: Long,
        val freeMemory: Long,
        val maxMemory: Long,
        val heapSize: Long
    )
}


