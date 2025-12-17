package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.usecase.CreateMacroUseCase
import com.aditsyal.autodroid.domain.usecase.GetMacroByIdUseCase
import com.aditsyal.autodroid.domain.usecase.UpdateMacroUseCase
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MacroEditorViewModelTest {

    private lateinit var viewModel: MacroEditorViewModel
    private val getMacroByIdUseCase = mockk<GetMacroByIdUseCase>()
    private val createMacroUseCase = mockk<CreateMacroUseCase>()
    private val updateMacroUseCase = mockk<UpdateMacroUseCase>()
    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = MacroEditorViewModel(
            savedStateHandle,
            getMacroByIdUseCase,
            createMacroUseCase,
            updateMacroUseCase
        )
    }

    @Test
    fun `addTrigger should update uiState with new trigger`() {
        val trigger = TriggerDTO(triggerType = "SYSTEM_EVENT", triggerConfig = mapOf("event" to "TEST"))
        viewModel.addTrigger(trigger)

        val currentState = viewModel.uiState.value
        assertEquals(1, currentState.currentMacro?.triggers?.size)
        assertEquals(trigger, currentState.currentMacro?.triggers?.first())
    }

    @Test
    fun `addAction should update uiState with new action and correct order`() {
        val action1 = ActionDTO(actionType = "TEST_ACTION_1", executionOrder = 0)
        val action2 = ActionDTO(actionType = "TEST_ACTION_2", executionOrder = 0)

        viewModel.addAction(action1)
        viewModel.addAction(action2)

        val currentState = viewModel.uiState.value
        assertEquals(2, currentState.currentMacro?.actions?.size)
        assertEquals(0, currentState.currentMacro?.actions?.get(0)?.executionOrder)
        assertEquals(1, currentState.currentMacro?.actions?.get(1)?.executionOrder)
    }

    @Test
    fun `removeTrigger should update uiState by removing trigger`() {
        val trigger = TriggerDTO(triggerType = "SYSTEM_EVENT", triggerConfig = mapOf("event" to "TEST"))
        viewModel.addTrigger(trigger)
        viewModel.removeTrigger(trigger)

        val currentState = viewModel.uiState.value
        assertEquals(0, currentState.currentMacro?.triggers?.size)
    }
}
