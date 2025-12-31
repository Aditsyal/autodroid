package com.aditsyal.autodroid.domain.usecase.executors

import com.aditsyal.autodroid.utils.SoundPlayer
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlaySoundExecutorTest {

    @MockK
    lateinit var soundPlayer: SoundPlayer

    private lateinit var executor: PlaySoundExecutor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { soundPlayer.playSound(any(), any()) } just Unit
        executor = PlaySoundExecutor(soundPlayer)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute plays default sound`() = runTest {
        val result = executor.execute(emptyMap())

        assertTrue(result.isSuccess)
        verify { soundPlayer.playSound("DEFAULT", null) }
    }

    @Test
    fun `execute plays custom sound with uri`() = runTest {
        val config = mapOf("soundType" to "NOTIFICATION", "uri" to "content://test")

        val result = executor.execute(config)

        assertTrue(result.isSuccess)
        verify { soundPlayer.playSound("NOTIFICATION", any()) }
    }
}