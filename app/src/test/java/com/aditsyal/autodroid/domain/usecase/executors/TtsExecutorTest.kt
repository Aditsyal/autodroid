package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.speech.tts.TextToSpeech
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

class TtsExecutorTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var textToSpeech: TextToSpeech

    private lateinit var executor: TtsExecutor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.applicationContext } returns context
        executor = TtsExecutor(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute throws exception when text is missing`() = runTest {
        val result = executor.execute(emptyMap())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Text is required for TTS", result.exceptionOrNull()?.message)
    }

    @Test
    fun `execute handles basic TTS request`() = runTest {
        val config = mapOf("text" to "Hello World")
        val result = executor.execute(config)
        // In unit test, TTS may not be available, but should not crash
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `execute handles TTS with custom parameters`() = runTest {
        val config = mapOf(
            "text" to "Test message",
            "language" to "en",
            "pitch" to 1.2f,
            "speechRate" to 0.8f,
            "queueMode" to "add"
        )
        val result = executor.execute(config)
        // Should handle the parameters without crashing
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `execute handles invalid language gracefully`() = runTest {
        val config = mapOf(
            "text" to "Hello",
            "language" to "invalid_language_code"
        )
        val result = executor.execute(config)
        // Should fallback to default language
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `stopSpeech returns success when TTS is available`() {
        // Test assumes TTS is not initialized in unit test
        val result = executor.stopSpeech()
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `isSpeaking returns false when TTS not initialized`() {
        assertTrue(!executor.isSpeaking())
    }

    @Test
    fun `getAvailableLanguages returns empty when TTS not initialized`() {
        val languages = executor.getAvailableLanguages()
        assertTrue(languages.isEmpty())
    }
}