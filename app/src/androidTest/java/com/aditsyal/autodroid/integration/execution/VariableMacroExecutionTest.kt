package com.aditsyal.autodroid.integration.execution

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.repository.MacroRepositoryImpl
import com.aditsyal.autodroid.domain.usecase.*
import com.aditsyal.autodroid.domain.usecase.executors.*
import com.aditsyal.autodroid.test.DatabaseTest
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
class VariableMacroExecutionTest : DatabaseTest() {
    
    private lateinit var repository: MacroRepositoryImpl
    private lateinit var executeMacroUseCase: ExecuteMacroUseCase
    private lateinit var getVariableUseCase: GetVariableUseCase
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
        
        getVariableUseCase = GetVariableUseCase(database.variableDao())
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
    fun `should set variable in action`() = runBlocking {
        val macro = MacroDTO(
            name = "Set Variable Macro",
            actions = listOf(
                ActionDTO(
                    actionType = "SET_VARIABLE",
                    actionConfig = mapOf(
                        "variableName" to "testVar",
                        "value" to "testValue",
                        "scope" to "GLOBAL"
                    ),
                    executionOrder = 1
                )
            )
        )
        
        val macroId = repository.createMacro(macro)
        executeMacroUseCase(macroId)
        
        val variable = getVariableUseCase("testVar", null)
        assertNotNull(variable)
        assertEquals("testValue", variable?.value)
        assertEquals("GLOBAL", variable?.scope)
    }
    
    @Test
    fun `should read variable in subsequent action`() = runBlocking {
        val macro = MacroDTO(
            name = "Read Variable Macro",
            actions = listOf(
                ActionDTO(
                    actionType = "SET_VARIABLE",
                    actionConfig = mapOf(
                        "variableName" to "counter",
                        "value" to "5",
                        "scope" to "GLOBAL",
                        "type" to "NUMBER"
                    ),
                    executionOrder = 1
                ),
                ActionDTO(
                    actionType = "SHOW_TOAST",
                    actionConfig = mapOf(
                        "message" to "\${counter}"
                    ),
                    executionOrder = 2
                )
            )
        )
        
        val macroId = repository.createMacro(macro)
        val result = executeMacroUseCase(macroId)
        
        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Success)
        
        val variable = getVariableUseCase("counter", null)
        assertEquals("5", variable?.value)
    }
    
    @Test
    fun `should handle global vs local variable scope`() = runBlocking {
        val macro1 = MacroDTO(
            name = "Global Variable Macro",
            actions = listOf(
                ActionDTO(
                    actionType = "SET_VARIABLE",
                    actionConfig = mapOf(
                        "variableName" to "shared",
                        "value" to "global",
                        "scope" to "GLOBAL"
                    ),
                    executionOrder = 1
                )
            )
        )
        
        val macro2 = MacroDTO(
            name = "Local Variable Macro",
            actions = listOf(
                ActionDTO(
                    actionType = "SET_VARIABLE",
                    actionConfig = mapOf(
                        "variableName" to "shared",
                        "value" to "local",
                        "scope" to "LOCAL"
                    ),
                    executionOrder = 1
                )
            )
        )
        
        val macroId1 = repository.createMacro(macro1)
        val macroId2 = repository.createMacro(macro2)
        
        executeMacroUseCase(macroId1)
        executeMacroUseCase(macroId2)
        
        val globalVar = getVariableUseCase("shared", null)
        val localVar = getVariableUseCase("shared", macroId2)
        
        assertEquals("global", globalVar?.value)
        assertEquals("local", localVar?.value)
    }
}

