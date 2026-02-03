# API Reference

## Table of Contents

- [Overview](#overview)
- [Domain Layer APIs](#domain-layer-apis)
  - [Use Cases](#use-cases)
  - [Repository Interfaces](#repository-interfaces)
- [Data Layer APIs](#data-layer-apis)
  - [DTOs](#dtos)
  - [Entity Classes](#entity-classes)
- [Presentation Layer APIs](#presentation-layer-apis)
  - [ViewModels](#viewmodels)
  - [UI State Classes](#ui-state-classes)
- [Automation APIs](#automation-apis)
  - [Trigger Providers](#trigger-providers)
  - [Action Executors](#action-executors)
- [Utility APIs](#utility-apis)
  - [Managers](#managers)
  - [Helpers](#helpers)
- [Configuration](#configuration)
- [Error Handling](#error-handling)

## Overview

AutoDroid's API is organized into clear layers following Clean Architecture principles. This reference provides comprehensive documentation for all public APIs.

## Domain Layer APIs

### Use Cases

#### ExecuteMacroUseCase

Executes a complete macro with all its triggers, constraints, and actions.

```kotlin
class ExecuteMacroUseCase @Inject constructor(
    private val repository: MacroRepository,
    private val executeActionUseCase: ExecuteActionUseCase,
    private val evaluateConstraintsUseCase: EvaluateConstraintsUseCase,
    private val evaluateLogicUseCase: EvaluateLogicUseCase
) {
    suspend operator fun invoke(macroId: Long): Result<Unit>
}
```

**Parameters:**

- `macroId`: Long - ID of the macro to execute

**Returns:**

- `Result<Unit>` - Success or failure with error details

**Throws:**

- `Exception` when macro execution fails

**Example:**

```kotlin
val result = executeMacroUseCase(macroId)
if (result.isSuccess) {
    // Macro executed successfully
} else {
    // Handle execution failure
    val error = result.exceptionOrNull()
}
```

#### ExecuteActionUseCase

Executes a single action within a macro context.

```kotlin
class ExecuteActionUseCase @Inject constructor(
    private val actionExecutors: Map<String, ActionExecutor>
) {
    suspend operator fun invoke(action: ActionDTO, macroId: Long?): Result<Unit>
}
```

**Parameters:**

- `action`: ActionDTO - Action to execute
- `macroId`: Long? - Optional macro ID for context

**Returns:**

- `Result<Unit>` - Success or failure

#### CheckTriggersUseCase

Validates and processes trigger events.

```kotlin
class CheckTriggersUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(type: String, eventData: Map<String, Any>): List<Long>
}
```

**Parameters:**

- `type`: String - Trigger type that fired
- `eventData`: Map<String, Any> - Event-specific data

**Returns:**

- `List<Long>` - List of macro IDs that should execute

#### EvaluateConstraintsUseCase

Evaluates all constraints for a macro.

```kotlin
class EvaluateConstraintsUseCase @Inject constructor(
    private val repository: MacroRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(macroId: Long): Boolean
}
```

**Parameters:**

- `macroId`: Long - Macro to evaluate constraints for

**Returns:**

- `Boolean` - True if all constraints pass

#### GetAllMacrosUseCase

Retrieves all macros from the database.

```kotlin
class GetAllMacrosUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(): List<MacroDTO>
}
```

**Returns:**

- `List<MacroDTO>` - All macros

#### GetMacroByIdUseCase

Retrieves a specific macro by ID.

```kotlin
class GetMacroByIdUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(id: Long): MacroDTO?
}
```

**Parameters:**

- `id`: Long - Macro ID

**Returns:**

- `MacroDTO?` - Macro or null if not found

#### CreateMacroUseCase

Creates a new macro.

```kotlin
class CreateMacroUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(macro: MacroDTO): Long
}
```

**Parameters:**

- `macro`: MacroDTO - Macro to create

**Returns:**

- `Long` - ID of created macro

#### UpdateMacroUseCase

Updates an existing macro.

```kotlin
class UpdateMacroUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(macro: MacroDTO)
}
```

**Parameters:**

- `macro`: MacroDTO - Updated macro data

#### DeleteMacroUseCase

Deletes a macro.

```kotlin
class DeleteMacroUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(id: Long)
}
```

**Parameters:**

- `id`: Long - Macro ID to delete

#### ToggleMacroUseCase

Enables or disables a macro.

```kotlin
class ToggleMacroUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(id: Long): MacroDTO
}
```

**Parameters:**

- `id`: Long - Macro ID to toggle

**Returns:**

- `MacroDTO` - Updated macro with new enabled state

#### Variable Management Use Cases

````kotlin
class GetVariableUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(name: String, macroId: Long?): String?
}

class SetVariableUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(
        macroId: Long?,
        name: String,
        value: String,
        scope: String,
        operation: String
    )
}

class EvaluateVariableUseCase @Inject constructor() {
    suspend operator fun invoke(
        expression: String,
        macroId: Long?
    ): String
}

#### Dry-Run Use Cases

```kotlin
class DryRunUseCase @Inject constructor(
    private val repository: MacroRepository,
    private val evaluateConstraintsUseCase: EvaluateConstraintsUseCase,
    private val estimateImpactUseCase: EstimateImpactUseCase
) {
    suspend operator fun invoke(macroId: Long): DryRunResult
}

data class DryRunResult(
    val overallSuccess: Boolean,
    val constraintResults: List<ConstraintResult>,
    val actionSequence: List<ActionDTO>,
    val estimatedImpact: ImpactResult
)
````

### Repository Interfaces

#### MacroRepository

Interface for macro data operations.

```kotlin
interface MacroRepository {
    // CRUD Operations
    suspend fun getAllMacros(): List<MacroDTO>
    suspend fun getMacroById(id: Long): MacroDTO?
    suspend fun createMacro(macro: MacroDTO): Long
    suspend fun updateMacro(macro: MacroDTO)
    suspend fun deleteMacro(id: Long)

    // Trigger Operations
    suspend fun getTriggersByMacroId(macroId: Long): List<TriggerDTO>
    suspend fun createTrigger(trigger: TriggerDTO): Long
    suspend fun updateTrigger(trigger: TriggerDTO)
    suspend fun deleteTrigger(id: Long)

    // Action Operations
    suspend fun getActionsByMacroId(macroId: Long): List<ActionDTO>
    suspend fun createAction(action: ActionDTO): Long
    suspend fun updateAction(action: ActionDTO)
    suspend fun deleteAction(id: Long)

    // Constraint Operations
    suspend fun getConstraintsByMacroId(macroId: Long): List<ConstraintDTO>
    suspend fun createConstraint(constraint: ConstraintDTO): Long
    suspend fun updateConstraint(constraint: ConstraintDTO)
    suspend fun deleteConstraint(id: Long)

    // Variable Operations
    suspend fun getVariablesByMacroId(macroId: Long): List<VariableDTO>
    suspend fun getGlobalVariables(): List<VariableDTO>
    suspend fun createVariable(variable: VariableDTO): Long
    suspend fun updateVariable(variable: VariableDTO)
    suspend fun deleteVariable(id: Long)

    // Execution Operations
    suspend fun logExecution(log: ExecutionLogDTO): Long
    suspend fun getExecutionLogs(macroId: Long): List<ExecutionLogDTO>

    // Template Operations
    suspend fun getAllTemplates(): List<TemplateDTO>
    suspend fun createMacroFromTemplate(templateId: Long): Long
}
```

## Data Layer APIs

### DTOs

#### MacroDTO

Data Transfer Object for macros.

```kotlin
data class MacroDTO(
    val id: Long = 0,
    val name: String,
    val enabled: Boolean = true,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val triggers: List<TriggerDTO> = emptyList(),
    val actions: List<ActionDTO> = emptyList(),
    val constraints: List<ConstraintDTO> = emptyList(),
    val variables: List<VariableDTO> = emptyList()
)
```

**Properties:**

- `id`: Unique identifier
- `name`: Display name
- `enabled`: Whether macro is active
- `description`: Optional description
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp
- `triggers`: Associated triggers
- `actions`: Associated actions
- `constraints`: Associated constraints
- `variables`: Associated variables

#### TriggerDTO

Data Transfer Object for triggers.

```kotlin
data class TriggerDTO(
    val id: Long = 0,
    val macroId: Long,
    val type: String,
    val config: Map<String, Any>,
    val createdAt: Long = System.currentTimeMillis()
)
```

**Properties:**

- `id`: Unique identifier
- `macroId`: Parent macro ID
- `type`: Trigger type (e.g., "TIME", "LOCATION")
- `config`: Configuration map
- `createdAt`: Creation timestamp

#### ActionDTO

Data Transfer Object for actions.

```kotlin
data class ActionDTO(
    val id: Long = 0,
    val macroId: Long,
    val actionType: String,
    val actionConfig: Map<String, Any>,
    val executionOrder: Int = 0,
    val delayAfter: Long = 0
)
```

**Properties:**

- `id`: Unique identifier
- `macroId`: Parent macro ID
- `actionType`: Action type (e.g., "WIFI_TOGGLE")
- `actionConfig`: Configuration map
- `executionOrder`: Order of execution (0, 1, 2, ...)
- `delayAfter`: Delay in milliseconds after this action

#### ConstraintDTO

Data Transfer Object for constraints.

```kotlin
data class ConstraintDTO(
    val id: Long = 0,
    val macroId: Long,
    val type: String,
    val config: Map<String, Any>
)
```

**Properties:**

- `id`: Unique identifier
- `macroId`: Parent macro ID
- `type`: Constraint type (e.g., "TIME_RANGE")
- `config`: Configuration map

#### VariableDTO

Data Transfer Object for variables.

```kotlin
data class VariableDTO(
    val variableName: String,
    val variableValue: String,
    val scope: String, // "LOCAL" or "GLOBAL"
    val macroId: Long? // null for GLOBAL scope
)
```

**Properties:**

- `variableName`: Variable name
- `variableValue`: Variable value
- `scope`: Scope ("LOCAL" or "GLOBAL")
- `macroId`: Parent macro ID (null for global)

#### ExecutionLogDTO

Data Transfer Object for execution logs.

```kotlin
data class ExecutionLogDTO(
    val id: Long = 0,
    val macroId: Long,
    val status: String, // "SUCCESS", "FAILURE", "SKIPPED"
    val executedAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
    val errorMessage: String? = null,
    val macroName: String? = null
)
```

**Properties:**

- `id`: Unique identifier
- `macroId`: Associated macro ID
- `status`: Execution status
- `executedAt`: Execution timestamp
- `durationMs`: Execution duration
- `errorMessage`: Error details if failed
- `macroName`: Macro display name

#### TemplateDTO

Data Transfer Object for templates.

```kotlin
data class TemplateDTO(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val macroConfig: Map<String, Any>
)
```

**Properties:**

- `id`: Unique identifier
- `name`: Template name
- `description`: Template description
- `macroConfig`: Complete macro configuration

### Entity Classes

#### MacroEntity

Database entity for macros.

```kotlin
@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "enabled")
    val enabled: Boolean = true,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### TriggerEntity

```kotlin
@Entity(tableName = "triggers", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE
    )
])
data class TriggerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "macro_id", index = true)
    val macroId: Long,

    @ColumnInfo(name = "type", index = true)
    val type: String,

    @ColumnInfo(name = "config")
    val config: String  // JSON string
)
```

#### ActionEntity

```kotlin
@Entity(tableName = "actions", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE
    )
])
data class ActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "macro_id", index = true)
    val macroId: Long,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "config")
    val config: String,  // JSON string

    @ColumnInfo(name = "execution_order")
    val executionOrder: Int = 0,

    @ColumnInfo(name = "delay_after")
    val delayAfter: Long = 0
)
```

#### ConstraintEntity

```kotlin
@Entity(tableName = "constraints", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE
    )
])
data class ConstraintEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "macro_id", index = true)
    val macroId: Long,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "config")
    val config: String  // JSON string
)
```

#### VariableEntity

```kotlin
@Entity(tableName = "variables", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE
    )
])
data class VariableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "macro_id", index = true)
    val macroId: Long?,  // null for GLOBAL scope

    @ColumnInfo(name = "name", index = true)
    val name: String,

    @ColumnInfo(name = "value")
    val value: String,

    @ColumnInfo(name = "scope", index = true)
    val scope: String  // "LOCAL" or "GLOBAL"
)
```

#### ExecutionLogEntity

```kotlin
@Entity(tableName = "execution_logs", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE
    )
])
data class ExecutionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "macro_id", index = true)
    val macroId: Long,

    @ColumnInfo(name = "status", index = true)
    val status: String,

    @ColumnInfo(name = "executed_at", index = true)
    val executedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long = 0,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null
)
```

#### TemplateEntity

```kotlin
@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "macro_config")
    val macroConfig: String  // JSON string
)
```

## Presentation Layer APIs

### ViewModels

#### MacroListViewModel

Manages macro list state and operations.

```kotlin
@HiltViewModel
class MacroListViewModel @Inject constructor(
    private val getAllMacrosUseCase: GetAllMacrosUseCase,
    private val toggleMacroUseCase: ToggleMacroUseCase,
    private val deleteMacroUseCase: DeleteMacroUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val macros: List<MacroDTO>) : UiState()
        data class Error(val message: String) : UiState()
    }

    val uiState: StateFlow<UiState> = // Implementation

    fun loadMacros()
    fun toggleMacro(id: Long)
    fun deleteMacro(id: Long)
}
```

**Methods:**

- `loadMacros()`: Loads all macros
- `toggleMacro(id: Long)`: Toggles macro enabled state
- `deleteMacro(id: Long)`: Deletes a macro

#### MacroEditorViewModel

Manages macro editing state.

```kotlin
@HiltViewModel
class MacroEditorViewModel @Inject constructor(
    private val getMacroByIdUseCase: GetMacroByIdUseCase,
    private val createMacroUseCase: CreateMacroUseCase,
    private val updateMacroUseCase: UpdateMacroUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Editing(val macro: MacroDTO) : UiState()
        data class Saving(val macro: MacroDTO) : UiState()
        data class Saved(val macroId: Long) : UiState()
        data class Error(val message: String) : UiState()
    }

    val uiState: StateFlow<UiState> = // Implementation

    fun loadMacro(id: Long?)
    fun updateName(name: String)
    fun updateDescription(description: String)
    fun addTrigger(trigger: TriggerDTO)
    fun removeTrigger(triggerId: Long)
    fun addAction(action: ActionDTO)
    fun removeAction(actionId: Long)
    fun addConstraint(constraint: ConstraintDTO)
    fun removeConstraint(constraintId: Long)
    fun saveMacro()
}
```

### UI State Classes

#### MacroListViewModel.UiState

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val macros: List<MacroDTO>) : UiState()
    data class Error(val message: String) : UiState()
}
```

#### MacroEditorViewModel.UiState

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Editing(val macro: MacroDTO) : UiState()
    data class Saving(val macro: MacroDTO) : UiState()
    data class Saved(val macroId: Long) : UiState()
    data class Error(val message: String) : UiState()
}
```

## Automation APIs

### Trigger Providers

#### TriggerProvider Interface

```kotlin
interface TriggerProvider {
    val type: String

    suspend fun registerTrigger(trigger: TriggerDTO)

    suspend fun unregisterTrigger(triggerId: Long)

    suspend fun clearTriggers()
}
```

#### TimeTriggerProvider

```kotlin
@Singleton
class TimeTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider {

    override val type: String = "TIME"

    override suspend fun registerTrigger(trigger: TriggerDTO)
    override suspend fun unregisterTrigger(triggerId: Long)
    override suspend fun clearTriggers()
}
```

#### LocationTriggerProvider

```kotlin
@Singleton
class LocationTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider {

    override val type: String = "LOCATION"

    override suspend fun registerTrigger(trigger: TriggerDTO)
    override suspend fun unregisterTrigger(triggerId: Long)
    override suspend fun clearTriggers()
}
```

### Action Executors

#### ActionExecutor Interface

```kotlin
interface ActionExecutor {
    val actionType: String
    suspend fun execute(config: Map<String, Any>): Result<Unit>
}
```

#### WifiToggleExecutor

```kotlin
@Singleton
class WifiToggleExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override val actionType: String = "WIFI_TOGGLE"

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return try {
            val enabled = config["enabled"] as? Boolean ?: false
            // Implementation
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Utility APIs

### Managers

#### PermissionManager

```kotlin
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasPermission(permission: String): Boolean

    fun requestPermissions(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int
    )

    fun shouldShowRationale(activity: Activity, permission: String): Boolean

    fun getPermissionExplanation(permissions: Array<String>): String
}
```

#### CacheManager

```kotlin
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun <T> get(key: String): T?

    fun <T> put(key: String, value: T, ttlMs: Long = 3600000)

    fun remove(key: String)

    fun clear()

    fun cleanup()
}
```

#### SoundPlayer

```kotlin
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun playSound(soundType: String, customUri: Uri? = null): Result<Unit>

    fun stopSound()

    fun setVolume(volume: Float)

    fun release()
}
```

### Helpers

#### DateTimeHelper

```kotlin
object DateTimeHelper {

    fun formatDate(timestamp: Long): String

    fun formatTime(timestamp: Long): String

    fun formatDuration(ms: Long): String

    fun parseTime(timeString: String): LocalTime?

    fun getCurrentTime(): LocalTime

    fun getCurrentDate(): LocalDate

    fun isTimeInRange(time: LocalTime, start: LocalTime, end: LocalTime): Boolean
}
```

#### ValidationHelper

```kotlin
object ValidationHelper {

    fun isValidMacroName(name: String): Boolean

    fun isValidPhoneNumber(phone: String): Boolean

    fun isValidUrl(url: String): Boolean

    fun isValidEmail(email: String): Boolean

    fun isValidVariableName(name: String): Boolean

    fun sanitizeInput(input: String): String
}
```

## Configuration

### App Configuration

```kotlin
object AppConfig {

    const val DATABASE_NAME = "automation_database.db"
    const val DATABASE_VERSION = 5

    const val MAX_MACRO_NAME_LENGTH = 100
    const val MAX_MACRO_DESCRIPTION_LENGTH = 500
    const val MAX_VARIABLE_NAME_LENGTH = 50
    const val MAX_VARIABLE_VALUE_LENGTH = 1000

    const val EXECUTION_TIMEOUT_MS = 30000L
    const val ACTION_DELAY_MAX_MS = 5000L
    const val TRIGGER_DEBOUNCE_MS = 300L

    const val WORKER_INTERVAL_MINUTES = 15L
    const val WORKER_FLEX_MINUTES = 5L

    const val CACHE_TTL_HOURS = 24L
    const val LOG_RETENTION_DAYS = 30
}
```

### Feature Flags

```kotlin
object FeatureFlags {

    const val ENABLE_DEBUG_LOGGING = BuildConfig.DEBUG
    const val ENABLE_CRASH_REPORTING = !BuildConfig.DEBUG
    const val ENABLE_ANALYTICS = true
    const val ENABLE_DRY_RUN_MODE = true
    const val ENABLE_CONFLICT_DETECTION = true

    // Permission-based features
    val ENABLE_LOCATION_TRIGGERS = PermissionManager.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    val ENABLE_SMS_ACTIONS = PermissionManager.hasPermission(Manifest.permission.SEND_SMS)
    val ENABLE_CALL_ACTIONS = PermissionManager.hasPermission(Manifest.permission.CALL_PHONE)
}
```

### Build Configuration

```kotlin
object BuildConfig {
    const val DEBUG = BuildConfig.DEBUG
    const val VERSION_NAME = BuildConfig.VERSION_NAME
    const val VERSION_CODE = BuildConfig.VERSION_CODE
    const val APPLICATION_ID = BuildConfig.APPLICATION_ID
    const val BUILD_TYPE = BuildConfig.BUILD_TYPE
}
```

## Error Handling

### Result Types

```kotlin
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Failure -> exception
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw exception
    }
}
```

### Error Types

```kotlin
sealed class AutoDroidError : Exception() {
    data class ValidationError(val field: String, val message: String) : AutoDroidError()
    data class PermissionError(val permission: String) : AutoDroidError()
    data class ExecutionError(val macroId: Long, val cause: Throwable) : AutoDroidError()
    data class NetworkError(val url: String, val cause: Throwable) : AutoDroidError()
    data class DatabaseError(val operation: String, val cause: Throwable) : AutoDroidError()
    object UnknownError : AutoDroidError()
}
```

### Error Handling Patterns

```kotlin
// In Use Cases
suspend fun executeAction(action: ActionDTO): Result<Unit> {
    return try {
        // Validate input
        validateAction(action)

        // Execute action
        actionExecutor.execute(action.actionConfig)

        Result.success(Unit)
    } catch (e: SecurityException) {
        Result.failure(AutoDroidError.PermissionError("Required permission not granted"))
    } catch (e: IllegalArgumentException) {
        Result.failure(AutoDroidError.ValidationError("action", e.message ?: "Invalid action"))
    } catch (e: Exception) {
        Result.failure(AutoDroidError.ExecutionError(action.macroId, e))
    }
}
```

### Error Recovery

```kotlin
class ErrorRecoveryManager @Inject constructor() {

    fun handleError(error: AutoDroidError): RecoveryAction {
        return when (error) {
            is AutoDroidError.PermissionError -> RecoveryAction.RequestPermission(error.permission)
            is AutoDroidError.ValidationError -> RecoveryAction.ShowValidationError(error.field, error.message)
            is AutoDroidError.ExecutionError -> RecoveryAction.RetryExecution(error.macroId)
            is AutoDroidError.NetworkError -> RecoveryAction.RetryWithBackoff
            is AutoDroidError.DatabaseError -> RecoveryAction.ShowDatabaseError
            AutoDroidError.UnknownError -> RecoveryAction.ShowGenericError
        }
    }

    sealed class RecoveryAction {
        data class RequestPermission(val permission: String) : RecoveryAction()
        data class ShowValidationError(val field: String, val message: String) : RecoveryAction()
        data class RetryExecution(val macroId: Long) : RecoveryAction()
        object RetryWithBackoff : RecoveryAction()
        object ShowDatabaseError : RecoveryAction()
        object ShowGenericError : RecoveryAction()
    }
}
```

---

**This API reference provides comprehensive documentation for all public interfaces in AutoDroid. Use these APIs to build custom triggers, actions, and integrations.** 🔌
