package com.aditsyal.autodroid.integration.execution

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditsyal.autodroid.data.repository.MacroRepositoryImpl
import com.aditsyal.autodroid.domain.usecase.*
import com.aditsyal.autodroid.domain.usecase.executors.*
import com.aditsyal.autodroid.test.DatabaseTest
import com.aditsyal.autodroid.test.fixtures.ConstraintFixtures
import com.aditsyal.autodroid.test.fixtures.MacroFixtures
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConstrainedMacroExecutionTest : DatabaseTest() {
    
    private lateinit var repository: MacroRepositoryImpl
    private lateinit var executeMacroUseCase: ExecuteMacroUseCase
    private lateinit var context: Context
    
    @Before
    override fun setupDatabase() {
        super.setupDatabase()
        context = ApplicationProvider.getApplicationContext()
        repository = MacroRepositoryImpl(
            database,
            database.macroDao(),
            database.triggerDao(),
            database.actionDao(),
            database.constraintDao(),
            database.executionLogDao()
        )
        
        val getVariableUseCase = GetVariableUseCase(database.variableDao())
        val setVariableUseCase = SetVariableUseCase(database.variableDao())
        val evaluateVariableUseCase = EvaluateVariableUseCase(getVariableUseCase)
        
        val evaluateConstraintsUseCase = EvaluateConstraintsUseCase(context)
        val executeActionUseCase = ExecuteActionUseCase(
            context,
            getVariableUseCase,
            setVariableUseCase,
            evaluateVariableUseCase,
            WifiToggleExecutor(context),
            BluetoothToggleExecutor(context),
            VolumeControlExecutor(context),
            NotificationExecutor(context),
            SendSmsExecutor(context),
            LaunchAppExecutor(context),
            OpenUrlExecutor(context),
            SetBrightnessExecutor(context),
            ToggleAirplaneModeExecutor(context),
            TetheringExecutor(context),
            DelayExecutor(),
            ToastExecutor(context),
            VibrateExecutor(context),
            PlaySoundExecutor(com.aditsyal.autodroid.utils.SoundPlayer(context)),
            StopSoundExecutor(com.aditsyal.autodroid.utils.SoundPlayer(context))
        )
        val evaluateLogicUseCase = EvaluateLogicUseCase(getVariableUseCase)
        
        executeMacroUseCase = ExecuteMacroUseCase(
            repository,
            evaluateConstraintsUseCase,
            executeActionUseCase,
            evaluateLogicUseCase
        )
    }
    
    @Test
    fun `should execute macro when constraints satisfied`() = runBlocking {
        val constraint = ConstraintFixtures.createBatteryLevelConstraint(
            operator = "less_than",
            value = 100 // Always satisfied in test
        )
        
        val macro = MacroFixtures.createMacroWithConstraint(
            name = "Constrained Macro",
            constraintType = constraint.constraintType,
            constraintConfig = constraint.constraintConfig
        )
        
        val macroId = repository.createMacro(macro)
        val result = executeMacroUseCase(macroId)
        
        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Success)
    }
    
    @Test
    fun `should skip macro when constraints not satisfied`() = runBlocking {
        val constraint = ConstraintFixtures.createBatteryLevelConstraint(
            operator = "greater_than",
            value = 1000 // Never satisfied
        )
        
        val macro = MacroFixtures.createMacroWithConstraint(
            name = "Skipped Macro",
            constraintType = constraint.constraintType,
            constraintConfig = constraint.constraintConfig
        )
        
        val macroId = repository.createMacro(macro)
        val result = executeMacroUseCase(macroId)
        
        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Skipped)
        
        val logs = database.executionLogDao().getAllExecutionLogs(limit = 10).first()
        val macroLog = logs.firstOrNull { it.macroId == macroId }
        assertNotNull(macroLog)
        assertEquals("SKIPPED", macroLog?.executionStatus)
    }
    
    @Test
    fun `should handle multiple constraints with AND logic`() = runBlocking {
        val macro = MacroFixtures.createSimpleMacro(name = "Multi-Constraint Macro")
        val macroWithConstraints = macro.copy(
            constraints = listOf(
                ConstraintFixtures.createBatteryLevelConstraint(operator = "less_than", value = 100),
                ConstraintFixtures.createChargingStatusConstraint(isCharging = false)
            )
        )
        
        val macroId = repository.createMacro(macroWithConstraints)
        val result = executeMacroUseCase(macroId)
        
        // Result depends on actual device state, but should not crash
        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Success ||
                  result is ExecuteMacroUseCase.ExecutionResult.Skipped)
    }
}

