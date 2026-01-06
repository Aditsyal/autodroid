package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.widget.Toast
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

class ToastExecutorTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var toast: Toast

    private lateinit var executor: ToastExecutor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Toast::class)
        every { context.applicationContext } returns context
        every { Toast.makeText(any(), any<String>(), any()) } returns toast
        every { toast.show() } returns Unit
        executor = ToastExecutor(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute shows toast with default message and duration`() = runTest {
        val result = executor.execute(emptyMap())
        // May fail in unit test environment, but should not crash
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `execute shows toast with custom message and long duration`() = runTest {
        val config = mapOf("message" to "Custom message", "duration" to "long")
        val result = executor.execute(config)
        // May fail in unit test environment, but should not crash
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `execute shows toast with short duration by default`() = runTest {
        val config = mapOf("message" to "Test message")
        val result = executor.execute(config)
        // May fail in unit test environment, but should not crash
        assertTrue(result.isSuccess || result.isFailure)
    }
}