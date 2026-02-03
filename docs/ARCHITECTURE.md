# AutoDroid Architecture Documentation

## Overview

AutoDroid is an Android automation app built with **Clean Architecture** principles, following the **MVVM (Model-View-ViewModel)** pattern. The app allows users to create automation rules (macros) that trigger actions based on various conditions.

### Key Architectural Principles

- **Separation of Concerns**: Each layer has distinct responsibilities
- **Dependency Inversion**: High-level modules don't depend on low-level modules
- **Single Responsibility**: Each class has one reason to change
- **Reactive Programming**: Use of Flow and coroutines for async operations
- **Dependency Injection**: Hilt provides compile-time dependency management

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  (UI: Compose, ViewModels, Navigation, Components)        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                          │
│  (Business Logic: Use Cases, Repository Interfaces)        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                           │
│  (Data Access: Repository Impl, Room Database, Entities)  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   Platform Layer                            │
│  (Android: Services, Receivers, Workers, Accessibility)    │
└─────────────────────────────────────────────────────────────┘
```

### 1. Presentation Layer

**Location**: `app/src/main/java/com/aditsyal/autodroid/presentation/`

**Components**:

- **Screens**: Composable UI screens (located in `presentation/screens/` and `presentation/ui/`)
  - `MacroListScreen`: Main macro list with physics-based card animations
  - `MacroEditorScreen`: Create/edit macros with reactive state
  - `MacroDetailScreen`: View macro details
  - `ExecutionHistoryScreen`: View past executions with action breakdown
  - `TemplateLibraryScreen`: Browse templates with search and recommendations
  - `SettingsScreen`: App settings including AMOLED and Dynamic Color toggles
  - `ConflictDetectionScreen`: View real-time resource contention
  - `VariableManagementScreen`: Manage global variables CRUD
  - `ImportMacrosScreen`: Import macros with conflict resolution strategies
  - `ExportMacrosScreen`: Export macros to JSON/XML
  - `DryRunPreviewScreen`: Step-by-step physics-based execution simulation

- **ViewModels**: State management using StateFlow and coroutines
  - `MacroListViewModel`: Manages macro list state
  - `MacroEditorViewModel`: Manages macro editing
  - `MacroDetailViewModel`: Manages macro detail state
  - `ExecutionHistoryViewModel`: Manages execution history with auto-cleanup logic
  - `SettingsViewModel`: Manages user preferences and theme state
  - `TemplateLibraryViewModel`: Manages templates and recommendations
  - `ConflictDetectorViewModel`: Manages real-time conflict analysis
  - `VariableManagementViewModel`: Manages global variable state
  - `ImportMacrosViewModel`: Manages file-based macro import
  - `ExportMacrosViewModel`: Manages macro serialization and export
  - `DryRunPreviewViewModel`: Manages execution simulation state

- **Components**: Reusable UI components
  - `TriggerPickerDialog`: Trigger selection dialog
  - `ActionPickerDialog`: Action selection dialog
  - `ConstraintPickerDialog`: Constraint selection dialog
  - `MacroCard`: Display macro in list with expressive motion
  - `SidebarView`: Overlay menu for manual execution

- **Navigation**: `NavGraph` for screen navigation using Jetpack Navigation Compose with Predictive Back support

- **Theme**: Material Design 3 theming system with MotionTokens for physics-based spring animations

**Responsibilities**:

- Display UI to users
- Handle user interactions
- Observe and present data from ViewModels
- Navigate between screens

### 2. Domain Layer

**Location**: `app/src/main/java/com/aditsyal/autodroid/domain/`

**Components**:

- **Use Cases**: Business logic operations
  - **Execution Use Cases**:
    - `ExecuteMacroUseCase`: Orchestrates macro execution
    - `ExecuteActionUseCase`: Executes individual actions
    - `CheckTriggersUseCase`: Validates trigger conditions
    - `EvaluateConstraintsUseCase`: Evaluates macro constraints
    - `EvaluateLogicUseCase`: Handles if/else and loop logic

  - **Variable Use Cases**:
    - `GetVariableUseCase`: Retrieve variable value
    - `SetVariableUseCase`: Store variable value
    - `EvaluateVariableUseCase`: Evaluate variable expressions

  - **Macro Management Use Cases**:
    - `GetAllMacrosUseCase`: Retrieve all macros
    - `GetMacroByIdUseCase`: Retrieve single macro
    - `CreateMacroUseCase`: Create new macro
    - `UpdateMacroUseCase`: Update existing macro
    - `DeleteMacroUseCase`: Delete macro
    - `ToggleMacroUseCase`: Enable/disable macro
    - `CreateMacroFromTemplateUseCase`: Create macro from template

  - **Initialization Use Cases**:
    - `InitializeTriggersUseCase`: Register all triggers
    - `InitializeDefaultTemplatesUseCase`: Populate template library

  - **Utility Use Cases**:
    - `CheckPermissionsUseCase`: Check app permissions
    - `ManageBatteryOptimizationUseCase`: Handle battery optimization
    - `ConflictDetectorUseCase`: Detect macro conflicts
    - `DryRunUseCase`: Simulate macro execution without side effects
    - `EstimateImpactUseCase`: Estimate battery and time impact of macros

- **Repository Interface**: `MacroRepository` - Data access abstraction

- **Models**: Data Transfer Objects (DTOs)
  - `MacroDTO`: Macro data model
  - `TriggerDTO`: Trigger data model
  - `ActionDTO`: Action data model
  - `ConstraintDTO`: Constraint data model
  - `VariableDTO`: Variable data model
  - `ExecutionLogDTO`: Execution log model
  - `TemplateDTO`: Template data model

- **Action Executors**: Specialized executors for each action type
  - Located in `domain/usecase/executors/`
  - Each executor implements `ActionExecutor` interface
  - Examples: `WifiToggleExecutor`, `LaunchAppExecutor`, `HttpRequestExecutor`, `TtsExecutor`, `LockScreenExecutor`, `DndExecutor`, `MediaControlExecutor`

**Responsibilities**:

- Contain business logic
- Define use cases
- Abstract data access via repository interfaces
- No dependencies on Android framework (except for domain models)

### 3. Data Layer

**Location**: `app/src/main/java/com/aditsyal/autodroid/data/`

**Components**:

- **Repository Implementation**: `MacroRepositoryImpl` - Implements repository interface

- **Local Database**: Room database
  - `AutomationDatabase`: Main database class
  - **Entities**:
    - `MacroEntity`: Macro table
    - `TriggerEntity`: Trigger table
    - `ActionEntity`: Action table
    - `ConstraintEntity`: Constraint table
    - `VariableEntity`: Variable table
    - `ExecutionLogEntity`: Execution log table
    - `LogicBlockEntity`: Logic control block table
    - `TemplateEntity`: Template table
  - **Data Access Objects (DAOs)**:
    - `MacroDao`: Macro queries
    - `TriggerDao`: Trigger queries
    - `ActionDao`: Action queries
    - `ConstraintDao`: Constraint queries
    - `VariableDao`: Variable queries
    - `ExecutionLogDao`: Execution log queries
    - `TemplateDao`: Template queries

- **Models**: Entity-to-DTO mappers

**Responsibilities**:

- Provide data to domain layer
- Implement repository interfaces
- Handle data storage (Room database)
- No business logic

### 4. Automation Layer

**Location**: `app/src/main/java/com/aditsyal/autodroid/automation/`

**Components**:

- **Trigger Providers**: Implement `TriggerProvider` interface
  - `TimeTriggerProvider`: Time-based triggers (specific time, intervals)
  - `LocationTriggerProvider`: Geofencing triggers (enter/exit locations)
  - `SensorTriggerProvider`: Sensor-based triggers (shake, proximity, light, orientation)
  - `DeviceStateTriggerProvider`: Device state changes (screen, charging, battery)
  - `ConnectivityTriggerProvider`: WiFi, Bluetooth, mobile data events
  - `AppEventTriggerProvider`: App lifecycle events (launch, close, install)
  - `CommunicationTriggerProvider`: Call and SMS events
  - `CalendarTriggerProvider`: Calendar event detection via ContentProvider
  - `AudioTriggerProvider`: Ringtone mode and volume level monitoring

- **Trigger Manager**: `TriggerManager` - Manages all trigger providers

**Responsibilities**:

- Detect system events
- Call `CheckTriggersUseCase` when events occur
- Register/unregister triggers
- Manage trigger lifecycle

### 5. Platform Layer

**Location**: `app/src/main/java/com/aditsyal/autodroid/`

**Components**:

- **Services**:
  - `AutomationAccessibilityService`: Handles accessibility events for UI automation
  - `AutomationForegroundService`: Ensures reliable background execution
  - `SidebarService`: Overlay service for the manual macro launcher

- **Broadcast Receivers**:
  - `BootReceiver`: Re-initializes triggers on device boot
  - `DeviceStateReceiver`: Handles device state changes
  - `TriggerAlarmReceiver`: Handles time-based triggers
  - `GeofenceBroadcastReceiver`: Handles geofence events

- **Workers**:
  - `MacroTriggerWorker`: WorkManager worker for periodic trigger checking

- **Utils**:
  - `PermissionManager`: Manages app permissions
  - `CacheManager`: Manages caching
  - `SoundPlayer`: Plays sounds

**Responsibilities**:

- Interact with Android platform
- Handle system events
- Provide platform-specific functionality
- Ensure background execution

## Data Flow

### Macro Execution Flow

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Trigger Fires                                           │
│    Trigger provider detects event (e.g., time, location)    │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Check Triggers                                          │
│    CheckTriggersUseCase validates trigger conditions         │
│    - Match trigger type                                     │
│    - Verify trigger configuration                            │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Load Macro                                             │
│    Repository loads macro with all details from database    │
│    - Actions                                               │
│    - Constraints                                           │
│    - Variables                                             │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Evaluate Constraints                                     │
│    EvaluateConstraintsUseCase checks if all constraints    │
│    are satisfied (time, battery, device state, etc.)       │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Execute Actions (ExecuteMacroUseCase)                   │
│    For each action in order:                               │
│    a. Evaluate logic blocks (if/else, loops)              │
│    b. Resolve variable placeholders                         │
│    c. Execute action via ExecuteActionUseCase               │
│    d. Apply delays if configured                           │
│    e. Handle errors                                        │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. Log Execution                                          │
│    Execution result logged to database                      │
│    - Success/Failure/Skipped                               │
│    - Execution duration                                    │
│    - Error messages                                        │
└─────────────────────────────────────────────────────────────┘
```

### Trigger Registration Flow

```
┌─────────────────────────────────────────────────────────────┐
│ 1. User Creates Macro                                      │
│    Macro saved to database via repository                   │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Initialize Triggers                                    │
│    InitializeTriggersUseCase loads all enabled macros       │
│    from database                                           │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Register with Providers                                │
│    Each trigger registered with appropriate                 │
│    TriggerProvider via TriggerManager                       │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Provider Listens                                      │
│    Provider sets up listeners:                             │
│    - BroadcastReceiver (for system events)                │
│    - SensorManager (for sensor events)                     │
│    - AlarmManager (for time events)                        │
│    - LocationManager (for geofence events)                 │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Event Detection                                       │
│    Provider detects event and calls                         │
│    CheckTriggersUseCase with event data                    │
└─────────────────────────────────────────────────────────────┘
```

### Database Query Flow

```
┌─────────────────────────────────────────────────────────────┐
│ ViewModel                                                  │
│  - Exposes StateFlow of data                             │
│  - Observes repository data                               │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ Use Case                                                  │
│  - Defines business logic                                 │
│  - Calls repository methods                               │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ Repository                                                │
│  - Implements repository interface                         │
│  - Calls DAO methods                                      │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ DAO                                                       │
│  - Defines SQL queries                                    │
│  - Returns Flow or suspend functions                      │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ Room Database                                             │
│  - Executes queries                                       │
│  - Returns results                                        │
└─────────────────────────────────────────────────────────────┘
```

## Dependency Injection

AutoDroid uses **Hilt** (Dagger) for compile-time dependency injection, providing:

- Compile-time validation
- Zero reflection
- Easy testing
- Clear dependency graph

### Modules

**`DatabaseModule`**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AutomationDatabase
}
```

- Provides Room database instance
- Provides all DAOs
- Configured as singleton

**`UseCaseModule`**

```kotlin
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun provideExecuteMacroUseCase(
        repository: MacroRepository,
        executeActionUseCase: ExecuteActionUseCase,
        // ...
    ): ExecuteMacroUseCase
}
```

- Provides all use cases
- Scoped to ViewModelComponent
- Allows ViewModels to inject use cases

**`TriggerModule`**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class TriggerModule {
    @Binds
    @IntoSet
    abstract fun bindTimeTriggerProvider(
        provider: TimeTriggerProvider
    ): TriggerProvider
}
```

- Binds all trigger providers into a Set
- TriggerManager receives all providers
- Easy to add new trigger types

**`RepositoryModule`**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMacroRepository(
        impl: MacroRepositoryImpl
    ): MacroRepository
}
```

- Binds repository implementations to interfaces
- Configured as singleton

### DI Scopes

- **`@Singleton`**: Application-wide single instance (database, repositories)
- **`@ViewModelScoped`**: One instance per ViewModel (use cases)
- **`@ActivityScoped`**: One instance per activity
- **No scope**: New instance each time injected

### Injection Example

```kotlin
@HiltViewModel
class MacroListViewModel @Inject constructor(
    private val getAllMacrosUseCase: GetAllMacrosUseCase,
    private val toggleMacroUseCase: ToggleMacroUseCase
) : ViewModel()
```

### Benefits

- **Testability**: Easily mock dependencies
- **Maintainability**: Clear dependency graph
- **Reusability**: Single instances shared across app
- **Performance**: Compile-time generation

## Background Execution

- **WorkManager**: Periodic trigger checking (`MacroTriggerWorker`)
- **Foreground Service**: `AutomationForegroundService` ensures reliable background execution
- **Broadcast Receivers**:
  - `BootReceiver`: Re-initializes triggers on device boot
  - `DeviceStateReceiver`: Handles system events
  - `TriggerAlarmReceiver`: Handles time-based triggers
  - `GeofenceBroadcastReceiver`: Handles location triggers

## Database Schema

### Entity Relationships

```
┌─────────────────┐
│    MacroEntity  │
│    (macros)     │
│                 │
│  - id (PK)      │◄────────────────┐
│  - name         │                 │
│  - enabled      │                 │
│  - description  │                 │
└─────────────────┘                 │
        │                            │
        │                            │
        │ FK                         │ FK
        │                            │
        ▼                            │
┌─────────────────┐         ┌───────────────────┐
│  TriggerEntity  │         │   ActionEntity    │
│   (triggers)    │         │    (actions)      │
│                 │         │                   │
│  - id (PK)      │         │  - id (PK)        │
│  - macroId (FK) │         │  - macroId (FK)   │
│  - type         │         │  - type           │
│  - config       │         │  - config         │
└─────────────────┘         │  - executionOrder│
        │                   └───────────────────┘
        │                            │
        │ FK                         │ FK
        │                            │
        ▼                            ▼
┌─────────────────┐         ┌───────────────────┐
│ConstraintEntity │         │ LogicBlockEntity  │
│ (constraints)   │         │  (logic_blocks)  │
│                 │         │                   │
│  - id (PK)      │         │  - id (PK)        │
│  - macroId (FK) │         │  - macroId (FK)   │
│  - type         │         │  - type           │
│  - config       │         │  - config         │
└─────────────────┘         │  - parentId       │
                            └───────────────────┘

┌─────────────────┐         ┌───────────────────┐
│ VariableEntity  │         │ExecutionLogEntity │
│   (variables)   │         │ (execution_logs)  │
│                 │         │                   │
│  - id (PK)      │         │  - id (PK)        │
│  - macroId (FK) │◄────────│  - macroId (FK)   │
│  - name         │  Optional│  - status         │
│  - value        │         │  - executedAt     │
│  - scope        │         │  - durationMs     │
└─────────────────┘         │  - errorMessage  │
                            └───────────────────┘

┌─────────────────┐
│ TemplateEntity │
│  (templates)   │
│                 │
│  - id (PK)      │
│  - name         │
│  - description  │
│  - macroConfig  │ (JSON)
└─────────────────┘
```

### Tables

**`macros`**: Macro definitions

- `id`: Primary key
- `name`: Macro name (unique)
- `enabled`: Boolean (whether macro is active)
- `description`: Optional description
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

**`triggers`**: Trigger configurations (foreign key to macros)

- `id`: Primary key
- `macroId`: Foreign key to macros table
- `type`: Trigger type (TIME, LOCATION, SENSOR, etc.)
- `config`: JSON configuration for trigger
- `createdAt`: Creation timestamp

**`actions`**: Action configurations (foreign key to macros)

- `id`: Primary key
- `macroId`: Foreign key to macros table
- `type`: Action type (WIFI_TOGGLE, LAUNCH_APP, etc.)
- `config`: JSON configuration for action
- `executionOrder`: Execution order (0, 1, 2, ...)
- `delayAfter`: Delay in milliseconds after this action

**`constraints`**: Constraint configurations (foreign key to macros)

- `id`: Primary key
- `macroId`: Foreign key to macros table
- `type`: Constraint type (TIME_RANGE, BATTERY_LEVEL, etc.)
- `config`: JSON configuration for constraint

**`variables`**: Variable storage

- `id`: Primary key
- `macroId`: Foreign key to macros (null for GLOBAL scope)
- `name`: Variable name
- `value`: Variable value (stored as string)
- `scope`: Scope (LOCAL or GLOBAL)

**`execution_logs`**: Execution history

- `id`: Primary key
- `macroId`: Foreign key to macros
- `status`: Execution status (SUCCESS, FAILURE, SKIPPED)
- `executedAt`: Execution timestamp
- `durationMs`: Execution duration in milliseconds
- `errorMessage`: Error message (if any)

**`templates`**: Pre-configured macro templates

- `id`: Primary key
- `name`: Template name
- `description`: Template description
- `macroConfig`: JSON of complete macro configuration

**`logic_blocks`**: Logic control blocks (if/else, loops)

- `id`: Primary key
- `macroId`: Foreign key to macros table
- `type`: Logic block type (IF, WHILE, FOR)
- `config`: JSON configuration (conditions, loop counts)
- `parentId`: Parent logic block (for nesting)

### Indexing Strategy

Tables have indexes on:

- `macros`: `enabled`, `name`
- `triggers`: `macroId`, `type`
- `actions`: `macroId`, `executionOrder`
- `constraints`: `macroId`, `type`
- `execution_logs`: `macroId`, `executedAt`, `status`

Indexes ensure fast queries during macro execution.

## Key Design Patterns

1. **Repository Pattern**: Abstracts data access
   - Domain layer depends on interfaces, not implementations
   - Easy to swap data sources
   - Simplifies testing

2. **Use Case Pattern**: Encapsulates business logic
   - Each use case represents a single business operation
   - Reusable across multiple ViewModels
   - Easy to test in isolation

3. **Observer Pattern**: Flow-based reactive updates
   - ViewModels observe data via Flow
   - UI automatically updates when data changes
   - Lifecycle-aware collection

4. **Strategy Pattern**: Trigger providers implement common interface
   - `TriggerProvider` interface defines contract
   - Each provider implements strategy for specific trigger type
   - Easy to add new trigger types

5. **Factory Pattern**: Trigger provider discovery via Hilt
   - `@IntoSet` annotation collects all providers
   - `TriggerManager` receives all providers
   - New providers automatically included

6. **MVVM Pattern**: Model-View-ViewModel
   - Model: Data and business logic
   - View: Composable UI
   - ViewModel: UI state and business logic coordination

7. **Singleton Pattern**: Single instance across app
   - Database instance
   - Repository implementations
   - Trigger providers

8. **Builder Pattern**: Complex object creation
   - Used in some action and trigger configurations
   - Fluent API for configuration

## Threading Model

### Dispatchers

- **Main Dispatcher (Dispatchers.Main)**
  - UI operations only
  - Compose recomposition
  - UI event handlers

- **IO Dispatcher (Dispatchers.IO)**
  - Database operations
  - File I/O
  - Network requests

- **Default Dispatcher (Dispatchers.Default)**
  - CPU-intensive operations
  - Data transformation
  - Complex calculations

- **Unconfined Dispatcher**
  - Used sparingly
  - When dispatcher shouldn't be switched

### Coroutines Usage

```kotlin
// In ViewModel (Main dispatcher)
class MacroListViewModel @Inject constructor(
    private val getAllMacrosUseCase: GetAllMacrosUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    fun loadMacros() {
        viewModelScope.launch {
            // Runs on Main
            _uiState.value = UiState.Loading
            try {
                // Switches to IO internally
                val macros = getAllMacrosUseCase()
                // Back on Main
                _uiState.value = UiState.Success(macros)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message)
            }
        }
    }
}

// In Use Case
class GetAllMacrosUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(): List<MacroDTO> {
        // Runs on IO dispatcher (defined in repository)
        return repository.getAllMacros()
    }
}

// In Repository
class MacroRepositoryImpl @Inject constructor(
    private val macroDao: MacroDao
) : MacroRepository {
    override fun getAllMacros(): Flow<List<MacroDTO>> = flow {
        // Runs on IO dispatcher
        macroDao.getAllMacros().collect { entities ->
            emit(entities.map { it.toDTO() })
        }
    }
}
```

### Thread Safety

- **ViewModels**: Main thread only
- **Use Cases**: Suspend functions (can run on any dispatcher)
- **Repositories**: IO dispatcher for database operations
- **DAOs**: Room handles threading automatically

### Concurrency Best Practices

1. Use `viewModelScope` for ViewModels (cancels when ViewModel clears)
2. Use `lifecycleScope` for lifecycle-aware operations
3. Never block the main thread
4. Use Flow for reactive streams
5. Handle coroutine exceptions with try-catch or `CoroutineExceptionHandler`

## Error Handling

- **Try-Catch Blocks**: Wrap all critical operations
- **Timber Logging**: Structured logging for debugging
- **Graceful Degradation**: Permission failures logged but don't crash
- **Execution Logs**: Failed executions logged with error messages

## Permissions

The app requires various permissions for different features:

- `ACCESS_FINE_LOCATION`: Geofencing triggers
- `POST_NOTIFICATIONS`: Notification actions
- `SEND_SMS`: SMS actions
- `CALL_PHONE`: Call actions
- `BLUETOOTH_CONNECT`: Bluetooth actions
- `SCHEDULE_EXACT_ALARM`: Time-based triggers
- And more...

See `AndroidManifest.xml` for complete list.
