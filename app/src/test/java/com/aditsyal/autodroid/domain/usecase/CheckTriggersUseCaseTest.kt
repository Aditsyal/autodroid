package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CheckTriggersUseCaseTest {

    private lateinit var useCase: CheckTriggersUseCase
    private val repository = mockk<MacroRepository>()
    private val executeMacroUseCase = mockk<ExecuteMacroUseCase>(relaxed = true)

    @Before
    fun setup() {
        useCase = CheckTriggersUseCase(repository, executeMacroUseCase)
    }

    @Test
    fun `should execute macro when fired trigger ID matches`() = runTest {
        val triggerId = 1L
        val macroId = 10L
        val trigger = TriggerDTO(id = triggerId, macroId = macroId, triggerType = "TEST", triggerConfig = emptyMap())
        
        coEvery { repository.getTriggerById(triggerId) } returns trigger
        
        useCase(triggerType = "TEST", eventData = mapOf("fired_trigger_id" to triggerId))
        
        coVerify { executeMacroUseCase(macroId) }
    }

    @Test
    fun `should execute macro when multiple triggers enabled for type and config matches`() = runTest {
        val macroId = 10L
        val triggers = listOf(
            TriggerDTO(id = 1L, macroId = macroId, triggerType = "BATTERY", triggerConfig = mapOf("level" to mapOf("operator" to "less_than", "value" to 20)))
        )
        
        coEvery { repository.getEnabledTriggersByType("BATTERY") } returns triggers
        
        useCase(triggerType = "BATTERY", eventData = mapOf("level" to 15))
        
        coVerify { executeMacroUseCase(macroId) }
    }

    @Test
    fun `should not execute macro when trigger config does not match event data`() = runTest {
        val macroId = 10L
        val triggers = listOf(
            TriggerDTO(id = 1L, macroId = macroId, triggerType = "BATTERY", triggerConfig = mapOf("level" to mapOf("operator" to "less_than", "value" to 20)))
        )
        
        coEvery { repository.getEnabledTriggersByType("BATTERY") } returns triggers
        
        useCase(triggerType = "BATTERY", eventData = mapOf("level" to 25))
        
        coVerify(exactly = 0) { executeMacroUseCase(any()) }
    }
}

