package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExecuteMacroUseCaseTest {

    @MockK
    lateinit var repository: MacroRepository

    @MockK
    lateinit var evaluateConstraintsUseCase: EvaluateConstraintsUseCase

    @MockK
    lateinit var executeActionUseCase: ExecuteActionUseCase

    @MockK
    lateinit var evaluateLogicUseCase: EvaluateLogicUseCase

    private lateinit var useCase: ExecuteMacroUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ExecuteMacroUseCase(
            repository,
            evaluateConstraintsUseCase,
            executeActionUseCase,
            evaluateLogicUseCase
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns NotFound when macro does not exist`() = runTest {
        coEvery { repository.getMacroById(1) } returns null

        val result = useCase(1, false)

        assertEquals(ExecuteMacroUseCase.ExecutionResult.NotFound, result)
        coVerify(exactly = 0) { repository.logExecution(any()) }
    }

    @Test
    fun `invoke returns Success and logs when execution succeeds`() = runTest {
        val macro = MacroDTO(id = 1, name = "Test Macro")
        coEvery { repository.getMacroById(1) } returns macro
        every { evaluateConstraintsUseCase(any()) } returns true
        coEvery { executeActionUseCase(any()) } just Runs
        coEvery { repository.updateExecutionInfo(any(), any()) } just Runs
        val logSlot = slot<ExecutionLogDTO>()
        coEvery { repository.logExecution(capture(logSlot)) } just Runs

        val result = useCase(1, false)

        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Success)
        
        coVerify { repository.updateExecutionInfo(1, any()) }
        coVerify { repository.logExecution(any()) }
        
        assertEquals("SUCCESS", logSlot.captured.executionStatus)
        assertEquals(1, logSlot.captured.macroId)
    }

    @Test
    fun `invoke returns Failure and logs when execution throws exception`() = runTest {
        val macro = MacroDTO(id = 1, name = "Test Macro")
        coEvery { repository.getMacroById(1) } returns macro
        every { evaluateConstraintsUseCase(any()) } returns true
        
        val exceptionMessage = "Simulated failure"
        coEvery { repository.updateExecutionInfo(any(), any()) } throws RuntimeException(exceptionMessage)
        
        val logSlot = slot<ExecutionLogDTO>()
        coEvery { repository.logExecution(capture(logSlot)) } just Runs

        val result = useCase(1, false)

        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Failure)
        assertEquals(exceptionMessage, (result as ExecuteMacroUseCase.ExecutionResult.Failure).reason)
        
        coVerify { repository.logExecution(any()) }
        assertEquals("FAILURE", logSlot.captured.executionStatus)
        assertEquals(exceptionMessage, logSlot.captured.errorMessage)
    }

    @Test
    fun `invoke returns Skipped when constraints are not satisfied`() = runTest {
        val macro = MacroDTO(id = 1, name = "Test Macro")
        coEvery { repository.getMacroById(1) } returns macro
        every { evaluateConstraintsUseCase(any()) } returns false

        val result = useCase(1, false)

        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Skipped)
        assertEquals("Constraints not satisfied", (result as ExecuteMacroUseCase.ExecutionResult.Skipped).reason)
        coVerify(exactly = 0) { repository.updateExecutionInfo(any(), any()) }
        coVerify(exactly = 0) { executeActionUseCase(any()) }
    }
}
