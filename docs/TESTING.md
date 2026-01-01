# Testing Guide

## Table of Contents

- [Overview](#overview)
- [Testing Philosophy](#testing-philosophy)
- [Unit Testing](#unit-testing)
- [Integration Testing](#integration-testing)
- [UI Testing with Compose](#ui-testing-with-compose)
- [Testing Triggers](#testing-triggers)
- [Testing Actions](#testing-actions)
- [Testing Constraints](#testing-constraints)
- [Testing Variables](#testing-variables)
- [Testing Use Cases](#testing-use-cases)
- [Test Coverage](#test-coverage)
- [Mocking Dependencies](#mocking-dependencies)
- [Running Tests](#running-tests)
- [CI/CD Testing](#cicd-testing)
- [Best Practices](#best-practices)
- [Common Issues](#common-issues)

## Overview

AutoDroid follows a comprehensive testing strategy to ensure code quality and reliability. This guide covers testing approaches for different layers of the application.

### Testing Pyramid

```
                    ┌─────────────┐
                    │   E2E Tests │  (Small)
                    │   (Manual)   │
                    └──────┬──────┘
                           │
                ┌────────────────┴─────────────┐
                │    Integration Tests          │  (Medium)
                │    (Android Instrumented)   │
                └──────────────┬────────────┘
                               │
                 ┌─────────────────┴─────────────┐
                 │      Unit Tests              │  (Large)
                 │      (JVM)                  │
                 └──────────────────────────────┘
```

- **E2E Tests**: End-to-end manual testing (smoke tests)
- **Integration Tests**: Android instrumented tests
- **Unit Tests**: JVM tests (fast, isolated)

### Testing Goals

1. **Correctness**: Code does what it's supposed to do
2. **Reliability**: Code works consistently over time
3. **Performance**: Code meets performance requirements
4. **Edge Cases**: Code handles unexpected inputs gracefully
5. **Maintainability**: Code is easy to change without breaking tests

## Testing Philosophy

### Test-Driven Development (TDD)

TDD workflow:

1. Write failing test for new feature
2. Implement minimal code to pass test
3. Refactor for code quality
4. Repeat for next requirement

**Benefits**:

- Ensures test coverage
- Improves code design
- Catches bugs early
- Provides documentation

### Behavior-Driven Testing

Focus on behavior rather than implementation:

```kotlin
// Good: Test behavior
@Test
fun `send sms sends message`() {
    val result = smsService.send(phoneNumber, message)
    assertEquals(Success, result)
}

// Bad: Test implementation details
@Test
fun `send sms calls SmsManager`() {
    smsService.send(phoneNumber, message)
    verify(smsManager).sendText(phoneNumber, message)
}
```

### Test Pyramid

Follow testing pyramid:

- **70% Unit Tests**: Fast, isolated, business logic
- **20% Integration Tests**: Slower, test interactions
- **10% E2E Tests**: Slowest, test complete flows

## Unit Testing

### Project Structure

```
app/src/test/java/com/aditsyal/autodroid/
├── domain/
│   ├── usecase/
│   │   └── executors/
│   └── repository/
├── presentation/
│   └── viewmodels/
├── data/
│   └── models/
└── util/
```

### Testing Use Cases

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ExecuteMacroUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var useCase: ExecuteMacroUseCase

    private val mockRepository = mockk<MacroRepository>()
    private val mockExecuteActionUseCase = mockk<ExecuteActionUseCase>()
    private val mockEvaluateConstraintsUseCase = mockk<EvaluateConstraintsUseCase>()

    @Before
    fun setup() {
        useCase = ExecuteMacroUseCase(
            mockRepository,
            mockExecuteActionUseCase,
            mockEvaluateConstraintsUseCase
        )
    }

    @Test
    fun `execute macro calls repository to load macro`() = runTest {
        // Given
        val macroId = 1L
        val macro = MacroDTO(
            id = macroId,
            name = "Test Macro",
            enabled = true,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )
        coEvery { mockRepository.getMacroById(macroId) } returns macro

        // When
        runBlocking {
            useCase(macroId)
        }

        // Then
        coVerify { mockRepository.getMacroById(macroId) }
    }

    @Test
    fun `execute macro returns success when all actions succeed`() = runTest {
        // Given
        val macroId = 1L
        val action1 = ActionDTO(
            actionType = "SHOW_TOAST",
            actionConfig = mapOf("message" to "Test"),
            executionOrder = 0
        )
        val macro = MacroDTO(
            id = macroId,
            name = "Test Macro",
            enabled = true,
            actions = listOf(action1),
            triggers = emptyList(),
            constraints = emptyList()
        )
        coEvery { mockRepository.getMacroById(macroId) } returns macro
        coEvery { mockEvaluateConstraintsUseCase(macroId) } returns true
        coEvery { mockExecuteActionUseCase(any(), any()) } returns Result.success(Unit)

        // When
        runBlocking {
            val result = useCase(macroId)
        }

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute macro returns failure when constraint not satisfied`() = runTest {
        // Given
        val macroId = 1L
        val macro = MacroDTO(
            id = macroId,
            name = "Test Macro",
            enabled = true,
            actions = emptyList(),
            triggers = emptyList(),
            constraints = emptyList()
        )
        coEvery { mockRepository.getMacroById(macroId) } returns macro
        coEvery { mockEvaluateConstraintsUseCase(macroId) } returns false

        // When
        runBlocking {
            val result = useCase(macroId)
        }

        // Then
        assertTrue(result.isFailure)
        assertEquals("Constraints not satisfied", result.exceptionOrNull()?.message)
    }
}
```

### Testing ViewModels

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MacroListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MacroListViewModel

    private val mockGetAllMacrosUseCase = mockk<GetAllMacrosUseCase>()
    private val mockToggleMacroUseCase = mockk<ToggleMacroUseCase>()

    @Before
    fun setup() {
        viewModel = MacroListViewModel(
            mockGetAllMacrosUseCase,
            mockToggleMacroUseCase
        )
    }

    @Test
    fun `load macros updates ui state`() = runTest {
        // Given
        val macros = listOf(
            MacroDTO(id = 1L, name = "Macro 1", enabled = true),
            MacroDTO(id = 2L, name = "Macro 2", enabled = false)
        )
        coEvery { mockGetAllMacrosUseCase() } returns macros

        // When
        viewModel.loadMacros()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is MacroListViewModel.UiState.Success)
        assertEquals(2, (state as MacroListViewModel.UiState.Success).macros.size)
    }

    @Test
    fun `toggle macro calls use case`() = runTest {
        // Given
        val macroId = 1L
        val macro = MacroDTO(id = macroId, name = "Test", enabled = true)
        coEvery { mockToggleMacroUseCase(macroId) } returns macro.copy(enabled = false)

        // When
        viewModel.toggleMacro(macroId)

        // Then
        coVerify { mockToggleMacroUseCase(macroId) }
    }

    @Test
    fun `error updates ui state correctly`() = runTest {
        // Given
        coEvery { mockGetAllMacrosUseCase() } throws Exception("Network error")

        // When
        viewModel.loadMacros()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is MacroListViewModel.UiState.Error)
        assertEquals("Network error", (state as MacroListViewModel.UiState.Error).message)
    }
}
```

### Testing Data Models

```kotlin
class MacroDTOTest {

    @Test
    fun `macro to entity and back`() {
        // Given
        val dto = MacroDTO(
            id = 1L,
            name = "Test Macro",
            enabled = true,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )

        // When
        val entity = dto.toEntity()
        val resultDto = entity.toDTO()

        // Then
        assertEquals(dto.id, resultDto.id)
        assertEquals(dto.name, resultDto.name)
        assertEquals(dto.enabled, resultDto.enabled)
    }
}
```

## Integration Testing

### Project Structure

```
app/src/androidTest/java/com/aditsyal/autodroid/
├── data/
│   ├── local/
│   │   └── dao/
│   └── repository/
└── presentation/
    └── screens/
```

### Testing Database Operations

```kotlin
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MacroDaoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var database: AutomationDatabase
    private lateinit var macroDao: MacroDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext()
        ).build()
        macroDao = database.macroDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and retrieve macro`() = runTest {
        // Given
        val entity = MacroEntity(
            name = "Test Macro",
            enabled = true
        )

        // When
        macroDao.insert(entity)
        val result = macroDao.getMacroById(entity.id)

        // Then
        assertEquals("Test Macro", result?.name)
        assertTrue(result?.enabled == true)
    }

    @Test
    fun `update macro`() = runTest {
        // Given
        val entity = MacroEntity(
            name = "Original Name",
            enabled = false
        )
        macroDao.insert(entity)

        // When
        entity.name = "Updated Name"
        entity.enabled = true
        macroDao.update(entity)

        // Then
        val result = macroDao.getMacroById(entity.id)
        assertEquals("Updated Name", result?.name)
        assertTrue(result?.enabled == true)
    }

    @Test
    fun `delete macro`() = runTest {
        // Given
        val entity = MacroEntity(
            name = "Test Macro",
            enabled = true
        )
        macroDao.insert(entity)

        // When
        macroDao.delete(entity)

        // Then
        val result = macroDao.getMacroById(entity.id)
        assertNull(result)
    }

    @Test
    fun `get all enabled macros`() = runTest {
        // Given
        val entities = listOf(
            MacroEntity(name = "Macro 1", enabled = true),
            MacroEntity(name = "Macro 2", enabled = true),
            MacroEntity(name = "Macro 3", enabled = false)
        )
        entities.forEach { macroDao.insert(it) }

        // When
        val result = macroDao.getAllEnabledMacros()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.enabled })
    }
}
```

### Testing Repository Implementation

```kotlin
@HiltAndroidTest
class MacroRepositoryImplTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: MacroRepositoryImpl

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `get all macros returns flow from database`() = runTest {
        // Given
        val expectedMacros = listOf(
            MacroDTO(id = 1L, name = "Macro 1"),
            MacroDTO(id = 2L, name = "Macro 2")
        )

        // When
        val result = repository.getAllMacros().first()

        // Then
        assertEquals(expectedMacros.size, result.size)
    }
}
```

## UI Testing with Compose

### Testing Composables

```kotlin
class MacroCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `macro card displays macro name`() {
        // Given
        val macro = MacroDTO(
            id = 1L,
            name = "Test Macro",
            enabled = true,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )

        // When
        composeTestRule.setContent {
            MacroCard(
                macro = macro,
                onClick = {},
                onToggle = {},
                onDelete = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test Macro").assertIsDisplayed()
    }

    @Test
    fun `macro card shows correct icon for enabled macro`() {
        // Given
        val macro = MacroDTO(
            id = 1L,
            name = "Enabled Macro",
            enabled = true,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )

        // When
        composeTestRule.setContent {
            MacroCard(
                macro = macro,
                onClick = {},
                onToggle = {},
                onDelete = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Enabled icon").assertIsDisplayed()
    }

    @Test
    fun `toggle button calls on toggle callback`() {
        // Given
        var toggleCalled = false
        val macro = MacroDTO(
            id = 1L,
            name = "Test",
            enabled = true,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )

        // When
        composeTestRule.setContent {
            MacroCard(
                macro = macro,
                onClick = {},
                onToggle = { toggleCalled = true },
                onDelete = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Toggle").performClick()
        assertTrue(toggleCalled)
    }
}
```

### Testing ViewModels in UI Tests

```kotlin
@HiltAndroidTest
class MacroListScreenTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var viewModel: MacroListViewModel

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `screen displays loading state initially`() {
        // Given & When
        hiltRule.setContent {
            MacroListScreen(
                viewModel = viewModel,
                onMacroClick = {},
                onFabClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }
}
```

## Testing Triggers

### Testing Trigger Provider

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class TimeTriggerProviderTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var provider: TimeTriggerProvider
    private val mockCheckTriggersUseCase = mockk<CheckTriggersUseCase>()
    private val mockAlarmManager = mockk<AlarmManager>()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext()
        provider = TimeTriggerProvider(
            context,
            mockCheckTriggersUseCase
        )
    }

    @Test
    fun `register time trigger sets up alarm`() = runTest {
        // Given
        val trigger = TriggerDTO(
            id = 1L,
            triggerType = "TIME",
            triggerConfig = mapOf("time" to "08:00")
        )

        // When
        runBlocking {
            provider.registerTrigger(trigger)
        }

        // Then
        verify { mockAlarmManager }.setExactAndAllowWhileIdle(
            any(),
            any(),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `unregister time trigger cancels alarm`() = runTest {
        // Given
        val triggerId = 1L
        val pendingIntent = mockk<PendingIntent>()
        coEvery { mockAlarmManager.getService(any(), any()) } returns pendingIntent

        // When
        runBlocking {
            provider.unregisterTrigger(triggerId)
        }

        // Then
        verify { mockAlarmManager }.cancel(pendingIntent)
    }
}
```

### Manual Trigger Testing

1. Create a test macro with the trigger
2. Add a simple action (e.g., show toast)
3. Enable the macro
4. Trigger the event manually:
   - Time triggers: Change device time to trigger time
   - Location triggers: Move to location
   - Sensor triggers: Shake device, etc.
   - App triggers: Launch/close app
5. Verify action executed (check execution logs)
6. Check Logcat for trigger detection logs

## Testing Actions

### Testing Action Executors

```kotlin
class WifiToggleExecutorTest {

    private lateinit var executor: WifiToggleExecutor

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext()
        val mockWifiManager = mockk<WifiManager>()
        executor = WifiToggleExecutor(context, mockWifiManager)
    }

    @Test
    fun `execute enables wifi when config true`() = runTest {
        // Given
        val config = mapOf("enabled" to true)

        // When
        val result = executor.execute(config)

        // Then
        assertTrue(result.isSuccess)
        verify { mockWifiManager }.isWifiEnabled = true
    }

    @Test
    fun `execute disables wifi when config false`() = runTest {
        // Given
        val config = mapOf("enabled" to false)

        // When
        val result = executor.execute(config)

        // Then
        assertTrue(result.isSuccess)
        verify { mockWifiManager }.isWifiEnabled = false
    }

    @Test
    fun `execute returns failure on security exception`() = runTest {
        // Given
        val config = mapOf("enabled" to true)
        val mockWifiManager = mockk<WifiManager> {
            every { isWifiEnabled = any() } throws SecurityException()
        }
        executor = WifiToggleExecutor(
            ApplicationProvider.getApplicationContext(),
            mockWifiManager
        )

        // When
        val result = executor.execute(config)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
}
```

### Manual Action Testing

1. Create a test macro with the action
2. Set a simple trigger (e.g., time trigger in 1 minute)
3. Enable the macro
4. Wait for trigger
5. Verify action executed correctly
6. Check execution logs for errors

## Testing Constraints

### Testing Constraint Evaluation

```kotlin
class EvaluateConstraintsUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var useCase: EvaluateConstraintsUseCase

    private val mockRepository = mockk<MacroRepository>()
    private val mockContext = mockk<Context>()

    @Before
    fun setup() {
        useCase = EvaluateConstraintsUseCase(mockRepository, mockContext)
    }

    @Test
    fun `evaluates time range constraint correctly`() = runTest {
        // Given
        val macroId = 1L
        val constraints = listOf(
            ConstraintDTO(
                constraintType = "TIME_RANGE",
                constraintConfig = mapOf(
                    "startTime" to "09:00",
                    "endTime" to "17:00"
                )
            )
        )
        coEvery { mockRepository.getConstraintsByMacroId(macroId) } returns constraints

        // When
        val now = LocalTime.of(12, 0)  // 12:00 PM
        val result = useCase(macroId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `evaluates battery level constraint correctly`() = runTest {
        // Given
        val macroId = 1L
        val constraints = listOf(
            ConstraintDTO(
                constraintType = "BATTERY_LEVEL",
                constraintConfig = mapOf(
                    "minLevel" to 20,
                    "maxLevel" to 80
                )
            )
        )
        coEvery { mockRepository.getConstraintsByMacroId(macroId) } returns constraints
        every { mockContext.registerReceiver(any(), any(), any(), any()) } returns Intent()

        // Mock battery level to 50
        val mockBatteryManager = mockk<BatteryManager>()
        every { mockBatteryManager.getIntProperty(any(), any()) } returns 50

        // When
        val result = useCase(macroId)

        // Then
        assertTrue(result)  // 50 is within 20-80 range
    }

    @Test
    fun `returns false when constraints not satisfied`() = runTest {
        // Given
        val macroId = 1L
        val constraints = listOf(
            ConstraintDTO(
                constraintType = "DAY_OF_WEEK",
                constraintConfig = mapOf("days" to listOf("MONDAY"))
            )
        )
        coEvery { mockRepository.getConstraintsByMacroId(macroId) } returns constraints

        // When
        val dayOfWeek = DayOfWeek.TUESDAY  // Not Monday
        val result = useCase(macroId)

        // Then
        assertFalse(result)
    }
}
```

## Testing Variables

### Testing Variable Operations

```kotlin
class VariableManagerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var useCase: SetVariableUseCase

    private val mockRepository = mockk<MacroRepository>()

    @Before
    fun setup() {
        useCase = SetVariableUseCase(mockRepository)
    }

    @Test
    fun `set local variable stores value`() = runTest {
        // Given
        val macroId = 1L
        val variable = VariableDTO(
            variableName = "counter",
            variableValue = "10",
            scope = "LOCAL",
            macroId = macroId
        )
        coEvery { mockRepository.setVariable(any()) } returns Unit

        // When
        runBlocking {
            useCase(macroId, "counter", "10", "LOCAL", "SET")
        }

        // Then
        coVerify {
            mockRepository.setVariable(
                match { it.variableName == "counter" }
            )
        }
    }

    @Test
    fun `set global variable stores value`() = runTest {
        // Given
        val variable = VariableDTO(
            variableName = "globalCounter",
            variableValue = "5",
            scope = "GLOBAL",
            macroId = null  // Global variable
        )
        coEvery { mockRepository.setVariable(any()) } returns Unit

        // When
        runBlocking {
            useCase(null, "globalCounter", "5", "GLOBAL", "SET")
        }

        // Then
        coVerify {
            mockRepository.setVariable(
                match { it.macroId == null }
            )
        }
    }

    @Test
    fun `add operation increments value`() = runTest {
        // Given
        val variable = VariableDTO(
            variableName = "counter",
            variableValue = "5",
            scope = "LOCAL",
            macroId = 1L
        )
        coEvery { mockRepository.getVariableByName(any(), any()) } returns variable
        coEvery { mockRepository.setVariable(any()) } returns Unit

        // When
        runBlocking {
            useCase(1L, "counter", "3", "LOCAL", "ADD")  // 5 + 3 = 8
        }

        // Then
        coVerify {
            mockRepository.setVariable(
                match { it.variableValue == "8" }
            )
        }
    }
}
```

## Testing Use Cases

### Testing Macro Execution Flow

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MacroExecutionFlowTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var executeMacroUseCase: ExecuteMacroUseCase

    private val mockRepository = mockk<MacroRepository>()
    private val mockExecuteActionUseCase = mockk<ExecuteActionUseCase>()
    private val mockEvaluateConstraintsUseCase = mockk<EvaluateConstraintsUseCase>()
    private val mockEvaluateLogicUseCase = mockk<EvaluateLogicUseCase>()

    @Before
    fun setup() {
        executeMacroUseCase = ExecuteMacroUseCase(
            mockRepository,
            mockExecuteActionUseCase,
            mockEvaluateConstraintsUseCase,
            mockEvaluateLogicUseCase
        )
    }

    @Test
    fun `executes all actions in order`() = runTest {
        // Given
        val macro = MacroDTO(
            id = 1L,
            name = "Test Macro",
            enabled = true,
            actions = listOf(
                ActionDTO(actionType = "SHOW_TOAST", executionOrder = 0),
                ActionDTO(actionType = "VIBRATE", executionOrder = 1),
                ActionDTO(actionType = "DELAY", executionOrder = 2),
                ActionDTO(actionType = "SHOW_TOAST", executionOrder = 3)
            ),
            triggers = emptyList(),
            constraints = emptyList()
        )
        coEvery { mockRepository.getMacroById(1L) } returns macro
        coEvery { mockEvaluateConstraintsUseCase(1L) } returns true
        coEvery { mockEvaluateLogicUseCase(any(), any()) } returns emptyList()
        coEvery { mockExecuteActionUseCase(any(), any()) } returns Result.success(Unit)

        // When
        runBlocking {
            val result = executeMacroUseCase(1L)
        }

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 4) {
            mockExecuteActionUseCase(any(), any())
        }
    }

    @Test
    fun `skips macro when constraints not met`() = runTest {
        // Given
        val macro = MacroDTO(id = 1L, name = "Test", enabled = true, actions = emptyList(), triggers = emptyList(), constraints = emptyList())
        coEvery { mockRepository.getMacroById(1L) } returns macro
        coEvery { mockEvaluateConstraintsUseCase(1L) } returns false

        // When
        runBlocking {
            val result = executeMacroUseCase(1L)
        }

        // Then
        assertTrue(result.isFailure)
        assertEquals("Constraints not satisfied", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) {
            mockExecuteActionUseCase(any(), any())
        }
    }
}
```

## Test Coverage

### Running Coverage Reports

```bash
# Generate coverage report
./gradlew testDebugUnitTestCoverage

# View report
open app/build/reports/coverage/test/debug/index.html
```

### Coverage Goals

- **Domain Layer**: > 90% coverage
- **Data Layer**: > 85% coverage
- **Presentation Layer**: > 80% coverage
- **Automation Layer**: > 75% coverage

### Improving Coverage

1. Identify uncovered code: Check coverage report
2. Write tests for uncovered branches
3. Test edge cases and error conditions
4. Test integration points

## Mocking Dependencies

### Using Mockk

```kotlin
class MyViewModelTest {

    private val mockUseCase = mockk<GetAllMacrosUseCase>()

    @Before
    fun setup() {
        coEvery { mockUseCase() } returns emptyList()
    }

    @Test
    fun `test something`() = runTest {
        // Test implementation
    }
}
```

### Mocking Coroutines

```kotlin
@Test
fun `suspend function test`() = runTest {
    coEvery { mockUseCase() } returns listOf()
    val result = runBlocking { mockUseCase() }
    assertEquals(1, result.size)
}
```

### Mocking Flows

```kotlin
@Test
fun `flow test`() = runTest {
    val flow = flowOf(1, 2, 3)
    val result = flow.toList()
    assertEquals(3, result.size)
}
```

## Running Tests

### Run All Unit Tests

```bash
./gradlew testDebugUnitTest
```

### Run Specific Test Class

```bash
./gradlew testDebugUnitTest --tests "*.MacroListViewModelTest"
```

### Run Specific Test Method

```bash
./gradlew testDebugUnitTest --tests "*.MacroListViewModelTest.load macros updates ui state"
```

### Run Integration Tests

```bash
# Connect device/emulator first
./gradlew connectedDebugAndroidTest
```

### Run Tests with Code Coverage

```bash
./gradlew testDebugUnitTestCoverage
```

### Run Tests with Logging

```bash
# Run with verbose logging
./gradlew testDebugUnitTest --info
```

### Run Tests in Parallel

```bash
# Faster test execution
./gradlew testDebugUnitTest --parallel
```

## CI/CD Testing

### GitHub Actions

AutoDroid uses CI to automatically run tests:

```yaml
name: CI

on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Cache Gradle packages
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build with Gradle
        run: ./gradlew assembleDebug

      - name: Run tests
        run: ./gradlew testDebugUnitTest

      - name: Run lint
        run: ./gradlew lintDebug

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: app/build/test-results/

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: coverage-report
          path: app/build/reports/coverage/
```

### Pre-commit Hooks

Add pre-commit hooks to run tests before committing:

```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "Running tests..."
./gradlew testDebugUnitTest

if [ $? -ne 0 ]; then
    echo "Tests failed. Commit aborted."
    exit 1
fi

echo "Tests passed. Committing..."
```

## Best Practices

### 1. Arrange-Act-Assert (AAA) Pattern

```kotlin
@Test
fun `test name`() {
    // Arrange: Set up test data
    val input = "test value"
    val expected = "expected result"

    // Act: Execute the function
    val result = functionUnderTest(input)

    // Assert: Verify the result
    assertEquals(expected, result)
}
```

### 2. Test Isolation

Each test should be independent:

- Don't rely on test execution order
- Clean up after each test
- Use `@Before` and `@After` hooks

### 3. Descriptive Test Names

```kotlin
// Good: Describes behavior
@Test
fun `wifi toggle enables wifi when config is true`()

// Bad: Describes implementation
@Test
fun `testWifiToggle`()
```

### 4. One Assert Per Test

```kotlin
// Good: Single assert
@Test
fun `returns success when macro exists`() {
    val result = useCase(1L)
    assertTrue(result.isSuccess)
}

// Bad: Multiple asserts (harder to debug which failed)
@Test
fun `test multiple things`() {
    val result = useCase(1L)
    assertTrue(result.isSuccess)
    assertEquals("Macro", result.data?.name)
    assertEquals(true, result.data?.enabled)
}
```

### 5. Test Edge Cases

```kotlin
@Test
fun `handles null input gracefully`() {
    val result = functionUnderTest(null)
    assertEquals("default value", result)
}

@Test
fun `handles empty input`() {
    val result = functionUnderTest("")
    assertEquals("default value", result)
}

@Test
fun `handles extreme values`() {
    val result = functionUnderTest(Int.MAX_VALUE)
    assertTrue(result.isValid)
}
```

### 6. Use Test Doubles

```kotlin
// Create test-specific implementations
class TestMacroRepository : MacroRepository {
    override fun getAllMacros(): Flow<List<MacroDTO>> {
        return flowOf(testMacros)
    }
}
```

## Common Issues

### Test Failing Intermittently

**Possible Causes**:

- Race conditions
- Timing issues
- Uninitialized state
- Async operations not awaited

**Solutions**:

1. Use `runBlocking` for async tests
2. Add delays where necessary
3. Use `@get:Rule` for proper setup/teardown
4. Avoid shared state between tests

### Tests Running Too Slow

**Possible Causes**:

- Heavy database operations
- Network calls
- Unnecessary delays
- Too many test cases

**Solutions**:

1. Use in-memory databases for tests
2. Mock network calls
3. Use coroutines efficiently
4. Consider test splitting

### OutOfMemoryError in Tests

**Possible Causes**:

- Large test data
- Memory leaks in tests
- Too many tests running at once

**Solutions**:

1. Clear test data after each test
2. Use `@After` to clean up
3. Increase JVM memory: `org.gradle.jvmargs=-Xmx2048m`
4. Run tests in smaller batches

### Mock Verification Failures

**Possible Causes**:

- Mock not configured correctly
- Method called differently than expected
- Argument mismatch

**Solutions**:

1. Use exact matchers: `eq()`, `refEq()`
2. Use `any()` when exact values don't matter
3. Check method signatures
4. Print actual calls: `verify(mock).wasCalled()`

## Test Documentation Template

### Test File Template

```kotlin
package com.aditsyal.autodroid.[package]

import com.aditsyal.autodroid.domain.models.ClassName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class ClassNameTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var classUnderTest: ClassName

    private val mockDependency = mockk<DependencyType>()

    @Before
    fun setup() {
        classUnderTest = ClassName(mockDependency)
    }

    @Test
    fun `brief description of what is being tested`() = runTest {
        // Arrange
        val input = "test input"
        val expected = "expected output"

        // Act
        coEvery { mockDependency.method(any()) } returns expected
        val result = runBlocking { classUnderTest.method(input) }

        // Assert
        assertEquals(expected, result)
        verify { mockDependency }.method(input)
    }
}
```

## See Also

- **[Actions Guide](ACTIONS.md)**: Testing actions
- **[Triggers Guide](TRIGGERS.md)**: Testing triggers
- **[Constraints Guide](CONSTRAINTS.md)**: Testing constraints
- **[Variables Guide](VARIABLES.md)**: Testing variables
- **[Architecture Documentation](ARCHITECTURE.md)**: Understanding architecture

---

**Happy testing!** 🧪 Remember: Untested code is broken code.
