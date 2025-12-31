package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.net.wifi.WifiManager
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WifiToggleExecutorTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var wifiManager: WifiManager

    private lateinit var executor: WifiToggleExecutor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.applicationContext } returns context
        every { context.getSystemService(Context.WIFI_SERVICE) } returns wifiManager
        executor = WifiToggleExecutor(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute enables wifi when config enabled true`() = runTest {
        every { wifiManager.isWifiEnabled = any() } just Runs

        val result = executor.execute(mapOf("enabled" to true))

        assertTrue(result.isSuccess)
        verify { wifiManager.isWifiEnabled = true }
    }

    @Test
    fun `execute disables wifi when config enabled false`() = runTest {
        every { wifiManager.isWifiEnabled = any() } just Runs

        val result = executor.execute(mapOf("enabled" to false))

        assertTrue(result.isSuccess)
        verify { wifiManager.isWifiEnabled = false }
    }

    @Test
    fun `execute defaults to enable when enabled not specified`() = runTest {
        every { wifiManager.isWifiEnabled = any() } just Runs

        val result = executor.execute(emptyMap())

        assertTrue(result.isSuccess)
        verify { wifiManager.isWifiEnabled = true }
    }

    @Test
    fun `execute throws exception when WifiManager not available`() = runTest {
        every { context.getSystemService(Context.WIFI_SERVICE) } returns null

        val result = executor.execute(mapOf("enabled" to true))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}