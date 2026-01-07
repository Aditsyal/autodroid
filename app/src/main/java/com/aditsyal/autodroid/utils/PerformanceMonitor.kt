package com.aditsyal.autodroid.utils

import com.aditsyal.autodroid.BuildConfig
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performance monitor for tracking execution times and identifying bottlenecks.
 * Optimized for production use with automatic pruning and memory safety.
 */
@Singleton
class PerformanceMonitor @Inject constructor() {
    // Flag to enable/disable monitoring. Defaults to DEBUG builds.
    private val isEnabled = BuildConfig.DEBUG
    
    private val executionTimes = ConcurrentHashMap<String, MutableList<Long>>()
    private val recentExecutions = ConcurrentHashMap<String, MutableList<Long>>()
    
    // Safety limits for memory protection in production-like environments
    companion object {
        private const val MAX_EXECUTION_RECORDS = 50
        private const val MAX_BURST_RECORDS = 20
        private const val STALE_ENTRY_TIMEOUT_NS = 60_000_000_000L // 60 seconds
    }
    
    fun startExecution(operation: String): String {
        if (!isEnabled) return ""
        
        pruneStaleEntries()
        
        // Ensure we don't exceed max records to prevent memory leaks
        if (executionTimes.size >= MAX_EXECUTION_RECORDS) {
            val oldestKey = executionTimes.keys().nextElement()
            executionTimes.remove(oldestKey)
        }

        val executionId = UUID.randomUUID().toString()
        val now = System.nanoTime()
        executionTimes[executionId] = mutableListOf(now)
        
        trackBurst(operation, now)
        
        Timber.d("Starting performance monitor for: $operation (ID: $executionId)")
        return executionId
    }

    fun trackBurst(operation: String, now: Long) {
        if (!isEnabled) return
        
        val times = recentExecutions.getOrPut(operation) { mutableListOf() }
        val oneSecondAgo = now - 1_000_000_000L
        
        times.removeIf { it < oneSecondAgo }
        
        // Safety cap for burst records
        if (times.size >= MAX_BURST_RECORDS) {
            times.removeAt(0)
        }
        
        times.add(now)
        
        if (times.size > 3) {
            Timber.i("Performance Notice: Frequent execution of $operation (${times.size} times in last 1s)")
        }
    }

    fun findActiveExecutionId(operationPrefix: String): String? {
        if (!isEnabled) return null
        return executionTimes.keys().asSequence().find { 
            it.startsWith(operationPrefix) || it.contains(operationPrefix) 
        }
    }
    
    fun checkpoint(executionId: String, checkpointName: String) {
        if (!isEnabled || executionId.isBlank()) return
        
        executionTimes[executionId]?.add(System.nanoTime())
        Timber.v("Performance checkpoint: $checkpointName (ID: $executionId)")
    }
    
    fun endExecution(executionId: String, operation: String) {
        if (!isEnabled || executionId.isBlank()) return
        
        executionTimes[executionId]?.add(System.nanoTime())
        logExecutionStats(executionId, operation)
        executionTimes.remove(executionId)
    }
    
    private fun logExecutionStats(executionId: String, operation: String) {
        val times = executionTimes[executionId] ?: return
        if (times.size < 2) return
        
        val totalTime = (times.last() - times.first()) / 1_000_000 // ms
        
        // Privacy: Mask specific details in logs if not in debug if necessary, 
        // but here we primarily use it for development.
        Timber.d("Performance: $operation took ${totalTime}ms (ID: $executionId)")
        
        // Thresholds based on operation type
        val threshold = when {
            operation.startsWith("Render_") -> 100 
            operation.contains("Macro") -> 500
            else -> 1000
        }
        
        if (totalTime > threshold) {
            Timber.w("Slow operation detected: $operation took ${totalTime}ms (Threshold: ${threshold}ms)")
        }
    }

    /**
     * Remove entries that were started but never ended (e.g. exceptions)
     */
    private fun pruneStaleEntries() {
        val now = System.nanoTime()
        executionTimes.entries.removeIf { (_, times) ->
            (now - (times.firstOrNull() ?: 0L)) > STALE_ENTRY_TIMEOUT_NS
        }
    }

    /**
     * Clear all monitoring data. Useful for low-memory situations.
     */
    fun clear() {
        executionTimes.clear()
        recentExecutions.clear()
        Timber.d("Performance monitor data cleared")
    }
}
