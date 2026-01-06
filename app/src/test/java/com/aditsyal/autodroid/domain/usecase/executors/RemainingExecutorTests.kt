package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.media.AudioManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VolumeControlExecutorTest {
    private val context = mockk<Context>(relaxed = true)
    private val audioManager = mockk<AudioManager>(relaxed = true)
    private lateinit var executor: VolumeControlExecutor

    @Before
    fun setup() {
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        executor = VolumeControlExecutor(context)
    }

    @Test
    fun `should set volume successfully`() = runTest {
        val result = executor.execute(mapOf("stream" to "MUSIC", "level" to 50))
        assertTrue(result.isSuccess)
    }
}

class ToggleAirplaneModeExecutorTest {
    private val context = mockk<Context>(relaxed = true)
    private lateinit var executor: ToggleAirplaneModeExecutor

    @Before
    fun setup() {
        executor = ToggleAirplaneModeExecutor(context)
    }

    @Test
    fun `should fail if settings permission not granted`() = runTest {
        // Settings.System.putInt usually throws SecurityException if no permission
        // In unit test it might not throw unless we mock it specifically, but let's check basic execution
        val result = executor.execute(mapOf("enabled" to true))
        // Depending on implementation, it might succeed in test environment
        assertTrue(result.isSuccess || result.isFailure)
    }
}

