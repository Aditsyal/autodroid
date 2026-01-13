package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DryRunUseCaseTest {

    private lateinit var dryRunUseCase: DryRunUseCase
    private lateinit var evaluateConstraintsUseCase: EvaluateConstraintsUseCase

    @Before
    fun setup() {
        evaluateConstraintsUseCase = mockk()
        dryRunUseCase = DryRunUseCase(evaluateConstraintsUseCase)
    }

    @Test
    fun `simulateMacro returns correct step count for simple macro`() = runTest {
        // Given
        val macro = createSimpleMacro()

        // When
        val result = dryRunUseCase.simulateMacro(macro).first()

        // Then
        assertEquals(macro, result.macro)
        assertEquals(3, result.steps.size) // 1 trigger + 1 constraint + 1 action
        assertTrue(result.totalEstimatedDuration > 0.toDuration(DurationUnit.MILLISECONDS))
        assertTrue(result.totalBatteryImpact >= 0.0)
        assertTrue(result.overallSuccessProbability > 0.0)
    }

    @Test
    fun `simulateMacro includes trigger evaluation step`() = runTest {
        // Given
        val macro = createMacroWithTimeTrigger()

        // When
        val result = dryRunUseCase.simulateMacro(macro).first()

        // Then
        val triggerStep = result.steps.first()
        assertEquals(DryRunUseCase.StepType.TRIGGER_CHECK, triggerStep.type)
        assertTrue(triggerStep.title.contains("Time Trigger"))
        assertTrue(triggerStep.successProbability > 0.95) // Time triggers should be very reliable
    }

    @Test
    fun `simulateMacro includes constraint evaluation step`() = runTest {
        // Given
        val macro = createMacroWithBatteryConstraint()

        // When
        val result = dryRunUseCase.simulateMacro(macro).first()

        // Then
        val constraintStep = result.steps.find { it.type == DryRunUseCase.StepType.CONSTRAINT_EVALUATION }
        assertTrue(constraintStep != null)
        assertTrue(constraintStep!!.title.contains("Battery Level"))
    }

    @Test
    fun `simulateMacro includes action execution step with correct battery impact`() = runTest {
        // Given
        val macro = createMacroWithLaunchAppAction()

        // When
        val result = dryRunUseCase.simulateMacro(macro).first()

        // Then
        val actionStep = result.steps.find { it.type == DryRunUseCase.StepType.ACTION_EXECUTION }
        assertTrue(actionStep != null)
        assertTrue(actionStep!!.title.contains("Launch App"))
        assertTrue(actionStep.batteryImpact > 0.004) // App launch should have significant battery impact
    }

    // Helper functions to create test data

    private fun createSimpleMacro(): MacroDTO {
        return MacroDTO(
            id = 1,
            name = "Test Macro",
            enabled = true,
            triggers = listOf(createTimeTrigger()),
            actions = listOf(createNotificationAction()),
            constraints = listOf(createBatteryConstraint()),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createMacroWithTimeTrigger(): MacroDTO {
        return MacroDTO(
            id = 1,
            name = "Time Trigger Macro",
            enabled = true,
            triggers = listOf(createTimeTrigger()),
            actions = listOf(createNotificationAction()),
            constraints = emptyList(),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createMacroWithBatteryConstraint(): MacroDTO {
        return MacroDTO(
            id = 1,
            name = "Battery Constraint Macro",
            enabled = true,
            triggers = listOf(createTimeTrigger()),
            actions = listOf(createNotificationAction()),
            constraints = listOf(createBatteryConstraint()),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createMacroWithLaunchAppAction(): MacroDTO {
        return MacroDTO(
            id = 1,
            name = "App Launch Macro",
            enabled = true,
            triggers = listOf(createTimeTrigger()),
            actions = listOf(createLaunchAppAction()),
            constraints = emptyList(),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createMacroWithMultipleActions(): MacroDTO {
        return MacroDTO(
            id = 1,
            name = "Multi Action Macro",
            enabled = true,
            triggers = listOf(createTimeTrigger()),
            actions = listOf(
                createNotificationAction(),
                createSetVolumeAction(),
                createHttpRequestAction()
            ),
            constraints = emptyList(),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createMacroWithDelayedAction(): MacroDTO {
        val action = createNotificationAction().copy(delayAfter = 2000)
        return MacroDTO(
            id = 1,
            name = "Delayed Action Macro",
            enabled = true,
            triggers = listOf(createTimeTrigger()),
            actions = listOf(action),
            constraints = emptyList(),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createMacroWithImpossibleConstraint(): MacroDTO {
        return MacroDTO(
            id = 1,
            name = "Impossible Constraint Macro",
            enabled = true,
            triggers = listOf(createTimeTrigger()),
            actions = listOf(createNotificationAction()),
            constraints = listOf(createImpossibleConstraint()),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createTimeTrigger(): TriggerDTO {
        return TriggerDTO(
            id = 1,
            macroId = 1,
            triggerType = "TIME",
            triggerConfig = mapOf("time" to "09:00")
        )
    }

    private fun createBatteryConstraint(): ConstraintDTO {
        return ConstraintDTO(
            id = 1,
            constraintType = "BATTERY_LEVEL",
            constraintConfig = mapOf("minLevel" to 20)
        )
    }

    private fun createImpossibleConstraint(): ConstraintDTO {
        // This represents a constraint that would be very difficult to satisfy
        return ConstraintDTO(
            id = 1,
            constraintType = "CUSTOM_IMPOSSIBLE",
            constraintConfig = emptyMap()
        )
    }

    private fun createNotificationAction(): ActionDTO {
        return ActionDTO(
            id = 1,
            actionType = "SEND_NOTIFICATION",
            actionConfig = mapOf("title" to "Test", "message" to "Hello"),
            executionOrder = 0,
            delayAfter = 0
        )
    }

    private fun createLaunchAppAction(): ActionDTO {
        return ActionDTO(
            id = 1,
            actionType = "LAUNCH_APP",
            actionConfig = mapOf("appName" to "Calculator"),
            executionOrder = 0,
            delayAfter = 0
        )
    }

    private fun createSetVolumeAction(): ActionDTO {
        return ActionDTO(
            id = 1,
            actionType = "SET_VOLUME",
            actionConfig = mapOf("level" to 50),
            executionOrder = 1,
            delayAfter = 0
        )
    }

    private fun createHttpRequestAction(): ActionDTO {
        return ActionDTO(
            id = 1,
            actionType = "HTTP_REQUEST",
            actionConfig = mapOf("url" to "https://example.com", "method" to "GET"),
            executionOrder = 2,
            delayAfter = 0
        )
    }
}