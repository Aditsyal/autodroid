package com.aditsyal.autodroid.utils

import timber.log.Timber
import java.util.UUID

object PerformanceMonitor {
    private val executionTimes = mutableMapOf<String, MutableList<Long>>()
    
    fun startExecution(operation: String): String {
        val executionId = UUID.randomUUID().toString()
        executionTimes[executionId] = mutableListOf(System.nanoTime())
        Timber.d("Starting performance monitor for: $operation (ID: $executionId)")
        return executionId
    }
    
    fun checkpoint(executionId: String, checkpointName: String) {
        executionTimes[executionId]?.add(System.nanoTime())
        Timber.v("Performance checkpoint: $checkpointName (ID: $executionId)")
    }
    
    fun endExecution(executionId: String, operation: String) {
        executionTimes[executionId]?.add(System.nanoTime())
        logExecutionStats(executionId, operation)
        executionTimes.remove(executionId)
    }
    
    private fun logExecutionStats(executionId: String, operation: String) {
        val times = executionTimes[executionId] ?: return
        if (times.size < 2) return
        
        val totalTime = (times.last() - times.first()) / 1_000_000 // ms
        
        Timber.d("Performance: $operation took ${totalTime}ms (ID: $executionId)")
        
        // Log slow operations
        if (totalTime > 1000) {
            Timber.w("Slow operation detected: $operation took ${totalTime}ms")
        }
    }
}


