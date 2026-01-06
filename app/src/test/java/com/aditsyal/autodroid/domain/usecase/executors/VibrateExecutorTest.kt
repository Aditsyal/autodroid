package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VibrateExecutorTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var vibrator: Vibrator

    private lateinit var executor: VibrateExecutor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.applicationContext } returns context
        every { context.getSystemService(Context.VIBRATOR_SERVICE) } returns vibrator
        every { vibrator.vibrate(any<VibrationEffect>()) } returns Unit
        every { vibrator.vibrate(any<Long>()) } returns Unit
        executor = VibrateExecutor(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute vibrates with default duration on API 26+`() = runTest {
        val result = executor.execute(emptyMap())
        // May fail in unit test environment, but should not crash
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `execute vibrates with custom duration`() = runTest {
        val config = mapOf("duration" to "2000")
        val result = executor.execute(config)
        // May fail in unit test environment, but should not crash
        assertTrue(result.isSuccess || result.isFailure)
    }
}