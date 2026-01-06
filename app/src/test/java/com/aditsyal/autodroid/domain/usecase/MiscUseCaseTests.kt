package com.aditsyal.autodroid.domain.usecase

import android.content.Context
import com.aditsyal.autodroid.data.models.ConflictDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TemplateDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.automation.trigger.TriggerManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
    import kotlinx.coroutines.flow.flowOf
    import kotlinx.coroutines.test.runTest
    import org.junit.Assert.assertEquals
    import org.junit.Before
    import org.junit.Test

    class ConflictDetectorUseCaseTest {
        private val repository = mockk<MacroRepository>()
        private lateinit var useCase: ConflictDetectorUseCase

        @Before
        fun setup() {
            useCase = ConflictDetectorUseCase(repository)
        }

        @Test
        fun `should return conflicts from repository`() = runTest {
            val conflicts = listOf(ConflictDTO(1L, "Macro 1", 2))
            every { repository.getMacroConflicts() } returns flowOf(conflicts)
            
            useCase().collect { result ->
                assertEquals(conflicts, result)
            }
        }
    }

    class EvaluateLogicUseCaseTest {
        private val getVariableUseCase = mockk<GetVariableUseCase>()
        private lateinit var useCase: EvaluateLogicUseCase

        @Before
        fun setup() {
            useCase = EvaluateLogicUseCase(getVariableUseCase)
        }

        @Test
        fun `should evaluate simple equality condition`() = runTest {
            coEvery { getVariableUseCase("test", any()) } returns mockk { every { value } returns "10" }
            
            val condition = mapOf(
                "leftOperand" to "{test}",
                "operator" to "==",
                "rightOperand" to "10",
                "useVariables" to true
            )
            
            val result = useCase.evaluateCondition(condition, 1L)
            assertEquals(true, result)
        }

        @Test
        fun `should evaluate numeric comparison`() = runTest {
            coEvery { getVariableUseCase("count", any()) } returns mockk { every { value } returns "5" }
            
            val condition = mapOf(
                "leftOperand" to "{count}",
                "operator" to ">",
                "rightOperand" to "3",
                "useVariables" to true
            )
            
            val result = useCase.evaluateCondition(condition, 1L)
            assertEquals(true, result)
        }

        @Test
        fun `should return false when condition not met`() = runTest {
            coEvery { getVariableUseCase("count", any()) } returns mockk { every { value } returns "1" }
            
            val condition = mapOf(
                "leftOperand" to "{count}",
                "operator" to ">",
                "rightOperand" to "3",
                "useVariables" to true
            )
            
            val result = useCase.evaluateCondition(condition, 1L)
            assertEquals(false, result)
        }
    }

    class InitializeTriggersUseCaseTest {
        private val context = mockk<Context>(relaxed = true)
        private val repository = mockk<MacroRepository>()
        private val triggerManager = mockk<TriggerManager>(relaxed = true)
        private lateinit var useCase: InitializeTriggersUseCase

        @Before
        fun setup() {
            useCase = InitializeTriggersUseCase(context, repository, triggerManager)
        }

        @Test
        fun `should initialize all enabled triggers`() = runTest {
            val triggers = listOf(mockk<com.aditsyal.autodroid.data.models.TriggerDTO>(relaxed = true))
            coEvery { repository.getAllEnabledTriggers() } returns triggers
            
            useCase()
            
            coVerify { triggerManager.registerTrigger(any()) }
        }
    }

    class InitializeDefaultTemplatesUseCaseTest {
        private val templateDao = mockk<com.aditsyal.autodroid.data.local.dao.TemplateDao>()
        private lateinit var useCase: InitializeDefaultTemplatesUseCase

        @Before
        fun setup() {
            useCase = InitializeDefaultTemplatesUseCase(templateDao)
        }

        @Test
        fun `should insert templates if none exist`() = runTest {
            every { templateDao.getAllTemplates() } returns flowOf(emptyList())
            coEvery { templateDao.insertTemplate(any()) } returns 1L
            
            useCase()
            
            coVerify(atLeast = 1) { templateDao.insertTemplate(any()) }
        }
    }
