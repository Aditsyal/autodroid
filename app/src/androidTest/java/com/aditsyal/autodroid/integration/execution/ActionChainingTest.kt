package com.aditsyal.autodroid.integration.execution

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.repository.MacroRepositoryImpl
import com.aditsyal.autodroid.domain.usecase.*
import com.aditsyal.autodroid.domain.usecase.executors.*
import com.aditsyal.autodroid.test.DatabaseTest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActionChainingTest : DatabaseTest() {
    
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
    fun `should execute multiple actions in sequence`() = runBlocking {
        val macro = MacroDTO(
            name = "Chained Actions",
            actions = listOf(
                ActionDTO(
                    actionType = "SHOW_TOAST",
                    actionConfig = mapOf("message" to "First"),
                    executionOrder = 1
                ),
                ActionDTO(
                    actionType = "SHOW_TOAST",
                    actionConfig = mapOf("message" to "Second"),
                    executionOrder = 2
                ),
                ActionDTO(
                    actionType = "SHOW_TOAST",
                    actionConfig = mapOf("message" to "Third"),
                    executionOrder = 3
                )
            )
        )
        
        val macroId = repository.createMacro(macro)
        val result = executeMacroUseCase(macroId)
        
        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Success)
    }
    
    @Test
    fun `should verify execution order`() = runBlocking {
        val macro = MacroDTO(
            name = "Ordered Actions",
            actions = listOf(
                ActionDTO(
                    actionType = "SET_VARIABLE",
                    actionConfig = mapOf(
                        "variableName" to "order",
                        "value" to "1",
                        "scope" to "GLOBAL"
                    ),
                    executionOrder = 1
                ),
                ActionDTO(
                    actionType = "SET_VARIABLE",
                    actionConfig = mapOf(
                        "variableName" to "order",
                        "value" to "2",
                        "scope" to "GLOBAL"
                    ),
                    executionOrder = 2
                ),
                ActionDTO(
                    actionType = "SET_VARIABLE",
                    actionConfig = mapOf(
                        "variableName" to "order",
                        "value" to "3",
                        "scope" to "GLOBAL"
                    ),
                    executionOrder = 3
                )
            )
        )
        
        val macroId = repository.createMacro(macro)
        executeMacroUseCase(macroId)
        
        val getVariableUseCase = GetVariableUseCase(database.variableDao())
        val finalValue = getVariableUseCase("order", null)
        assertEquals("3", finalValue?.value) // Last action should set final value
    }
    
    @Test
    fun `should handle action delays`() = runBlocking {
        val startTime = System.currentTimeMillis()
        
        val macro = MacroDTO(
            name = "Delayed Actions",
            actions = listOf(
                ActionDTO(
                    actionType = "DELAY",
                    actionConfig = mapOf("delayMs" to 50L),
                    executionOrder = 1,
                    delayAfter = 0
                ),
                ActionDTO(
                    actionType = "SHOW_TOAST",
                    actionConfig = mapOf("message" to "After delay"),
                    executionOrder = 2,
                    delayAfter = 0
                )
            )
        )
        
        val macroId = repository.createMacro(macro)
        executeMacroUseCase(macroId)
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Should take at least 50ms due to delay
        assertTrue(duration >= 50)
    }
    
    @Test
    fun `should continue on action failure`() = runBlocking {
        val macro = MacroDTO(
            name = "Failure Recovery",
            actions = listOf(
                ActionDTO(
                    actionType = "SHOW_TOAST",
                    actionConfig = mapOf("message" to "Before failure"),
                    executionOrder = 1
                ),
                ActionDTO(
                    actionType = "LAUNCH_APP",
                    actionConfig = mapOf("packageName" to "invalid.package.name"),
                    executionOrder = 2
                ),
                ActionDTO(
                    actionType = "SHOW_TOAST",
                    actionConfig = mapOf("message" to "After failure"),
                    executionOrder = 3
                )
            )
        )
        
        val macroId = repository.createMacro(macro)
        val result = executeMacroUseCase(macroId)
        
        // Should complete even if middle action fails
        assertTrue(result is ExecuteMacroUseCase.ExecutionResult.Success)
    }
}

