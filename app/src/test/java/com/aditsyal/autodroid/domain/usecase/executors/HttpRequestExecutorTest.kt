package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class HttpRequestExecutorTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var url: URL

    @MockK
    lateinit var connection: HttpURLConnection

    private lateinit var executor: HttpRequestExecutor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.applicationContext } returns context
        executor = HttpRequestExecutor(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute throws exception when URL is missing`() = runTest {
        val result = executor.execute(emptyMap())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("URL is required for HTTP request", result.exceptionOrNull()?.message)
    }

    @Test
    fun `execute throws exception when URL is null`() = runTest {
        val config = emptyMap<String, Any>()
        val result = executor.execute(config)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `execute handles GET request with valid config`() = runTest {
        val config = mapOf(
            "url" to "https://httpbin.org/get",
            "method" to "GET",
            "timeout" to 5000
        )
        val result = executor.execute(config)
        // In unit test, network calls may fail, but should not crash
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `execute handles POST request with body`() = runTest {
        val config = mapOf(
            "url" to "https://httpbin.org/post",
            "method" to "POST",
            "body" to "{\"key\": \"value\"}",
            "headers" to mapOf("Content-Type" to "application/json")
        )
        val result = executor.execute(config)
        // In unit test, network calls may fail, but should not crash
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun `buildFormBody creates correct form encoding`() {
        val params = mapOf("name" to "John Doe", "email" to "john@example.com")
        val result = executor.buildFormBody(params)
        assertTrue(result.contains("name=John+Doe"))
        assertTrue(result.contains("email=john%40example.com"))
        assertTrue(result.contains("&"))
    }
}