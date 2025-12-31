package com.aditsyal.autodroid.domain.usecase.executors

import com.aditsyal.autodroid.utils.SoundPlayer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StopSoundExecutorTest {

    @MockK
    lateinit var soundPlayer: SoundPlayer

    private lateinit var executor: StopSoundExecutor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { soundPlayer.stop() } returns Unit
        executor = StopSoundExecutor(soundPlayer)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute stops sound`() = runTest {
        val result = executor.execute(emptyMap())

        assertTrue(result.isSuccess)
        verify { soundPlayer.stop() }
    }
}