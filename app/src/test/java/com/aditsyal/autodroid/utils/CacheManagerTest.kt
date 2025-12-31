package com.aditsyal.autodroid.utils

import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class CacheManagerTest {

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        runTest { CacheManager.clearAll() }
    }

    @After
    fun tearDown() {
        unmockkAll()
        runTest { CacheManager.clearAll() }
    }

    @Test
    fun `get caches and returns value within TTL`() = runTest {
        val key = "test_key"
        val loaderCalled = mutableListOf<Boolean>()

        val result1 = CacheManager.get(key, 1000L) {
            loaderCalled.add(true)
            "value1"
        }

        val result2 = CacheManager.get(key, 1000L) {
            loaderCalled.add(true)
            "value2"
        }

        assertEquals("value1", result1)
        assertEquals("value1", result2) // Should be cached
        assertEquals(1, loaderCalled.size) // Loader called only once
    }

    @Test
    fun `getSuspend caches and returns value within TTL`() = runTest {
        val key = "test_suspend_key"
        val loaderCalled = mutableListOf<Boolean>()

        val result1 = CacheManager.getSuspend(key, 1000L) {
            loaderCalled.add(true)
            "suspend_value1"
        }

        val result2 = CacheManager.getSuspend(key, 1000L) {
            loaderCalled.add(true)
            "suspend_value2"
        }

        assertEquals("suspend_value1", result1)
        assertEquals("suspend_value1", result2)
        assertEquals(1, loaderCalled.size)
    }

    @Test
    fun `invalidateVariableCache clears specific variable caches`() = runTest {
        // Set up some cached variables
        CacheManager.get("VAR_LOCAL_test_123", 1000L) { "local_value" }
        CacheManager.get("VAR_GLOBAL_test", 1000L) { "global_value" }

        // Invalidate
        CacheManager.invalidateVariableCache("test", 123L)

        // Should reload
        val loaderCalled = mutableListOf<Boolean>()
        val result = CacheManager.get("VAR_LOCAL_test_123", 1000L) {
            loaderCalled.add(true)
            "new_local_value"
        }

        assertEquals("new_local_value", result)
        assertEquals(1, loaderCalled.size)
    }
}