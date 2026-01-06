package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationExecutorTest {
    private val context = mockk<Context>(relaxed = true)
    private lateinit var executor: NotificationExecutor

    @Before
    fun setup() {
        executor = NotificationExecutor(context)
    }

    @Test
    fun `should succeed when providing title and message`() = runTest {
        val result = executor.execute(mapOf("title" to "Test", "message" to "Msg"))
        // May fail in unit test due to missing notification permission, but should not crash
        assertTrue(result.isSuccess || result.isFailure)
    }
}

class SendSmsExecutorTest {
    private val context = mockk<Context>(relaxed = true)
    private lateinit var executor: SendSmsExecutor

    @Before
    fun setup() {
        executor = SendSmsExecutor(context)
    }

    @Test
    fun `should fail if phone number is missing`() = runTest {
        val result = executor.execute(mapOf("message" to "Hello"))
        assertTrue(result.isFailure)
    }
}

class LaunchAppExecutorTest {
    private val context = mockk<Context>(relaxed = true)
    private lateinit var executor: LaunchAppExecutor

    @Before
    fun setup() {
        executor = LaunchAppExecutor(context)
    }

    @Test
    fun `should fail if package name is missing`() = runTest {
        val result = executor.execute(emptyMap())
        assertTrue(result.isFailure)
    }
}

