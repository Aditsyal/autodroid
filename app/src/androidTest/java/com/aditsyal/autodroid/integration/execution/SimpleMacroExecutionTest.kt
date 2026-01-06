package com.aditsyal.autodroid.integration.execution

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditsyal.autodroid.data.local.database.AutomationDatabase
import com.aditsyal.autodroid.data.repository.MacroRepositoryImpl
import com.aditsyal.autodroid.domain.usecase.EvaluateConstraintsUseCase
import com.aditsyal.autodroid.domain.usecase.ExecuteActionUseCase
import com.aditsyal.autodroid.domain.usecase.ExecuteMacroUseCase
import com.aditsyal.autodroid.domain.usecase.EvaluateLogicUseCase
import com.aditsyal.autodroid.domain.usecase.GetVariableUseCase
import com.aditsyal.autodroid.domain.usecase.SetVariableUseCase
import com.aditsyal.autodroid.domain.usecase.EvaluateVariableUseCase
import com.aditsyal.autodroid.domain.usecase.executors.*
import com.aditsyal.autodroid.test.DatabaseTest
import com.aditsyal.autodroid.test.fixtures.MacroFixtures
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleMacroExecutionTest : DatabaseTest() {
    
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
    fun `should execute macro with single trigger and single action`() = runBlocking {
        val macro = MacroFixtures.createMacroWithAction(
            name = "Simple Macro",
            actionType = "SHOW_TOAST",
            actionConfig = mapOf("message" to "Test execution")
        )
        
        val macroId = repository.createMacro(macro)
        val result = executeMacroUseCase(macroId)
        
        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Success)
        
        // Verify execution logged
        val logs = database.executionLogDao().getAllExecutionLogs(limit = 10).first()
        assertTrue(logs.isNotEmpty())
        val latestLog = logs.first()
        assertEquals("SUCCESS", latestLog.executionStatus)
        assertEquals(macroId, latestLog.macroId)
    }
    
    @Test
    fun `should verify action executes successfully`() = runBlocking {
        val macro = MacroFixtures.createMacroWithAction(
            name = "Action Test",
            actionType = "DELAY",
            actionConfig = mapOf("delayMs" to 10L)
        )
        
        val macroId = repository.createMacro(macro)
        val result = executeMacroUseCase(macroId)
        
        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Success)
    }
    
    @Test
    fun `should verify execution logged to database`() = runBlocking {
        val macro = MacroFixtures.createSimpleMacro(name = "Logging Test")
        val macroId = repository.createMacro(macro)
        
        executeMacroUseCase(macroId)
        
        val logs = database.executionLogDao().getAllExecutionLogs(limit = 10).first()
        assertTrue(logs.any { it.macroId == macroId })
        
        val macroLog = logs.first { it.macroId == macroId }
        assertEquals("SUCCESS", macroLog.executionStatus)
        assertTrue(macroLog.executionDurationMs >= 0)
    }
    
    @Test
    fun `should verify execution status updated`() = runBlocking {
        val macro = MacroFixtures.createSimpleMacro(name = "Status Test")
        val macroId = repository.createMacro(macro)
        
        val beforeExecution = repository.getMacroById(macroId)
        assertNotNull(beforeExecution)
        assertEquals(0, beforeExecution?.lastExecuted)
        
        executeMacroUseCase(macroId)
        
        val afterExecution = repository.getMacroById(macroId)
        assertNotNull(afterExecution?.lastExecuted)
        assertTrue((afterExecution?.lastExecuted ?: 0) > 0)
    }
}

