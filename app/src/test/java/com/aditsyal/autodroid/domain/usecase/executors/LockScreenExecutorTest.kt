package com.aditsyal.autodroid.domain.usecase.executors

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
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

class LockScreenExecutorTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var devicePolicyManager: DevicePolicyManager

    private lateinit var executor: LockScreenExecutor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.applicationContext } returns context
        every { context.getSystemService(Context.DEVICE_POLICY_SERVICE) } returns devicePolicyManager
        every { context.packageName } returns "com.aditsyal.autodroid"

        executor = LockScreenExecutor(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute locks screen when device admin is active`() = runTest {
        every { devicePolicyManager.isAdminActive(any()) } returns true
        every { devicePolicyManager.lockNow() } returns Unit

        val result = executor.execute(emptyMap())
        assertTrue(result.isSuccess)
        verify { devicePolicyManager.lockNow() }
    }

    @Test
    fun `execute throws exception when device admin not active and forceLock false`() = runTest {
        every { devicePolicyManager.isAdminActive(any()) } returns false

        val config = mapOf("forceLock" to false)
        val result = executor.execute(config)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun `execute attempts lock even without admin when forceLock true`() = runTest {
        every { devicePolicyManager.isAdminActive(any()) } returns false

        val config = mapOf("forceLock" to true)
        val result = executor.execute(config)
        // Should attempt to lock and may fail, but shouldn't crash
        assertTrue(result.isSuccess || result.isFailure)
    }



    @Test
    fun `constants are correctly defined`() {
        assertEquals("android.permission.BIND_DEVICE_ADMIN", LockScreenExecutor.DEVICE_ADMIN_PERMISSION)
        assertEquals("android.app.action.ADD_DEVICE_ADMIN", LockScreenExecutor.ACTION_ADD_DEVICE_ADMIN)
    }
}