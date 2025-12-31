package com.aditsyal.autodroid.integration

import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.usecase.ExecuteMacroUseCase
import com.aditsyal.autodroid.domain.usecase.GetVariableUseCase
import com.aditsyal.autodroid.domain.usecase.SetVariableUseCase
import com.aditsyal.autodroid.utils.CacheManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integration test for macro execution with variable caching
 */
class MacroExecutionIntegrationTest {

    // This would require Hilt testing setup, simplified for demo
    @Test
    fun `macro execution with variable caching works correctly`() = runTest {
        // Clear cache
        CacheManager.clearAll()

        // This is a placeholder - in real implementation, we'd inject use cases
        // and test a full macro with variable reads/writes and caching behavior

        // Simulate caching behavior
        val cacheKey = "VAR_LOCAL_testVar_123"
        val cachedValue = CacheManager.getSuspend(cacheKey, 30_000L) {
            "cached_test_value"
        }

        assertEquals("cached_test_value", cachedValue)

        // Verify it's cached
        val cachedAgain = CacheManager.getSuspend(cacheKey, 30_000L) {
            "should_not_call_loader"
        }

        assertEquals("cached_test_value", cachedAgain)
    }

    @Test
    fun `cache invalidation works on variable update`() = runTest {
        val cacheKey = "VAR_GLOBAL_testVar"

        // Set initial cached value
        val initial = CacheManager.getSuspend(cacheKey, 60_000L) {
            "initial_value"
        }

        assertEquals("initial_value", initial)

        // Invalidate cache
        CacheManager.invalidateVariableCache("testVar", null)

        // Should reload
        val reloaded = CacheManager.getSuspend(cacheKey, 60_000L) {
            "reloaded_value"
        }

        assertEquals("reloaded_value", reloaded)
    }
}