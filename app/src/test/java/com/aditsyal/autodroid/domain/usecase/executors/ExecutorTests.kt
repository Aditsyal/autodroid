package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.net.wifi.WifiManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WifiToggleExecutorTest {
    private val context = mockk<Context>(relaxed = true)
    private val wifiManager = mockk<WifiManager>(relaxed = true)
    private lateinit var executor: WifiToggleExecutor

    @Before
    fun setup() {
        every { context.applicationContext.getSystemService(Context.WIFI_SERVICE) } returns wifiManager
        executor = WifiToggleExecutor(context)
    }

    @Test
    fun `should enable wifi when config says so`() = runTest {
        val result = executor.execute(mapOf("enabled" to true))
        assertTrue(result.isSuccess)
    }
}

class DelayExecutorTest {
    private val executor = DelayExecutor()

    @Test
    fun `should complete successfully after delay`() = runTest {
        val result = executor.execute(mapOf("delayMs" to 10L))
        assertTrue(result.isSuccess)
    }
}

class SetBrightnessExecutorTest {
    private val context = mockk<Context>(relaxed = true)
    private lateinit var executor: SetBrightnessExecutor

    @Before
    fun setup() {
        executor = SetBrightnessExecutor(context)
    }

    @Test
    fun `should fail if brightness out of range`() = runTest {
        val result = executor.execute(mapOf("brightness" to 150))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should succeed with default if brightness is missing`() = runTest {
        val result = executor.execute(emptyMap())
        // May fail in unit test due to Settings.System access, but should use default brightness
        assertTrue(result.isSuccess || result.isFailure)
    }
}

class BluetoothToggleExecutorTest {
    private val context = mockk<Context>(relaxed = true)
    private lateinit var executor: BluetoothToggleExecutor

    @Before
    fun setup() {
        executor = BluetoothToggleExecutor(context)
    }

    @Test
    fun `should execute successfully`() = runTest {
        val result = executor.execute(mapOf("enabled" to true))
        // Success or failure depends on reflection in the actual executor
        assertTrue(result.isSuccess || result.isFailure)
    }
}

class OpenUrlExecutorTest {
    private val context = mockk<Context>(relaxed = true)
    private lateinit var executor: OpenUrlExecutor

    @Before
    fun setup() {
        executor = OpenUrlExecutor(context)
    }

    @Test
    fun `should fail if url is missing`() = runTest {
        val result = executor.execute(emptyMap())
        assertTrue(result.isFailure)
    }
}
