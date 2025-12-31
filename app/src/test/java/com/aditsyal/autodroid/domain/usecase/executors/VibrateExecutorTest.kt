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
        mockkStatic(Build.VERSION::class)
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.O // API 26+
        every { context.applicationContext } returns context
        every { context.getSystemService(Context.VIBRATOR_SERVICE) } returns vibrator
        every { vibrator.vibrate(any<VibrationEffect>()) } returns Unit
        executor = VibrateExecutor(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute vibrates with default duration on API 26+`() = runTest {
        val result = executor.execute(emptyMap())

        assertTrue(result.isSuccess)
        verify { vibrator.vibrate(any<VibrationEffect>()) }
    }

    @Test
    fun `execute vibrates with custom duration`() = runTest {
        val config = mapOf("duration" to "2000")

        val result = executor.execute(config)

        assertTrue(result.isSuccess)
        verify { vibrator.vibrate(any<VibrationEffect>()) }
    }
}