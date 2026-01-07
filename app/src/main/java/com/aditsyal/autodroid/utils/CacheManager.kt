package com.aditsyal.autodroid.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedHashMap

data class CacheEntry<T>(val value: T, val ts: Long, val ttl: Long)

object CacheManager {
    private const val MAX_CACHE_SIZE = 50
    // Simple in-memory cache with a single map; values are stored as Any
    private val cache = object : LinkedHashMap<String, CacheEntry<Any>>(
        MAX_CACHE_SIZE + 1,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry<Any>>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }
    private val mutex = Mutex()

    suspend fun <T> getSuspend(key: String, ttlMs: Long = 30_000L, loader: suspend () -> T): T {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            val entry = cache[key]
            
            if (entry != null && (now - entry.ts) < entry.ttl) {
                @Suppress("UNCHECKED_CAST")
                entry.value as T
            } else {
                val value = loader()
                cache[key] = CacheEntry(value as Any, now, ttlMs)
                
                // Periodic cleanup of expired entries if cache is getting full
                if (cache.size > MAX_CACHE_SIZE * 0.8) {
                    cleanupExpiredEntries(now)
                }
                
                value
            }
        }
    }

    fun <T> get(key: String, ttlMs: Long = 30_000L, loader: () -> T): T {
        val now = System.currentTimeMillis()
        val entry = cache[key]
        
        if (entry != null && (now - entry.ts) < entry.ttl) {
            @Suppress("UNCHECKED_CAST")
            return entry.value as T
        }
        
        val value = loader()
        cache[key] = CacheEntry(value as Any, now, ttlMs)
        return value
    }

    private fun cleanupExpiredEntries(now: Long) {
        val expiredKeys = cache.entries
            .filter { (now - it.value.ts) >= it.value.ttl }
            .map { it.key }
        expiredKeys.forEach { cache.remove(it) }
    }

    suspend fun invalidate(key: String) {
        mutex.withLock { cache.remove(key) }
    }

    suspend fun invalidateVariableCache(name: String, macroId: Long?) {
        mutex.withLock {
            val localKey = if (macroId != null) "VAR_LOCAL_${name}_${macroId}" else "VAR_LOCAL_${name}_GLOBAL"
            val globalKey = "VAR_GLOBAL_${name}"
            cache.remove(localKey)
            cache.remove(globalKey)
        }
    }

    fun clearAll() {
        cache.clear()
    }
}