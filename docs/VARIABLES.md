# Variables Documentation

## Table of Contents

- [Overview](#overview)
- [Variable Types](#variable-types)
- [Variable Operations](#variable-operations)
- [Variable Storage](#variable-storage)
- [Variable Resolution](#variable-resolution)
- [Using Variables in Actions](#using-variables-in-actions)
- [Using Variables in Constraints](#using-variables-in-constraints)
- [Variable Scope](#variable-scope)
- [Variable Lifecycle](#variable-lifecycle)
- [Variable Management UI](#variable-management-ui)
- [Best Practices](#best-practices)
- [Common Issues](#common-issues)

## Overview

Variables allow AutoDroid macros to store and manipulate data dynamically during execution. They enable:

- **Dynamic Actions**: Actions can use variable values
- **Data Passing**: Share data between actions
- **State Tracking**: Maintain execution state
- **User Input**: Incorporate user-provided data
- **Calculations**: Perform arithmetic and string operations

### Variable Features

- **Local Variables**: Scoped to single macro execution
- **Global Variables**: Persist across macro executions
- **Arithmetic Operations**: Add, subtract, multiply, divide
- **String Operations**: Append, substring
- **Variable Resolution**: Automatic placeholder replacement in actions
- **Type Conversion**: Implicit type handling (string, number, boolean)

## Variable Types

### Local Variables

**Scope**: Single macro execution
**Lifetime**: Only during that macro's execution
**Storage**: Stored with `macro_id` in database

```kotlin
VariableDTO(
    variableName = "counter",
    variableValue = "0",
    scope = "LOCAL",
    macroId = 1L  // Belongs to specific macro
)
```

**Use Cases**:

- Counter for loops
- Temporary data storage during macro execution
- Macro-specific state management

---

### Global Variables

**Scope**: Across all macro executions
**Lifetime**: Until explicitly changed or deleted
**Storage**: Stored with `macro_id = null` in database

```kotlin
VariableDTO(
    variableName = "userName",
    variableValue = "John",
    scope = "GLOBAL",
    macroId = null  // No macro association
)
```

**Use Cases**:

- User preferences (name, email, etc.)
- Shared configuration values
- Cross-macro data sharing
- Persistent counters

## Variable Operations

### SET

Set a variable to a specific value.

```kotlin
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "operation" to "SET",
        "name" to "counter",
        "value" to "10",
        "scope" to "LOCAL"
    ),
    executionOrder = 0
)
```

**Examples**:

```kotlin
// Set string value
SET: {message} = "Hello"

// Set number value
SET: {count} = 0

// Set boolean value
SET: {flag} = true
```

---

### ADD

Add a numeric value to a variable.

```kotlin
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "operation" to "ADD",
        "name" to "counter",
        "value" to "5"
    ),
    executionOrder = 1
)
```

**Examples**:

```kotlin
// Increment counter
SET: {counter} = {counter} + 1

// Add to accumulated value
SET: {total} = {total} + 10

// Add multiple values
SET: {sum} = {value1} + {value2} + {value3}
```

---

### SUBTRACT

Subtract a numeric value from a variable.

```kotlin
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "operation" to "SUBTRACT",
        "name" to "counter",
        "value" to "1"
    ),
    executionOrder = 1
)
```

**Examples**:

```kotlin
// Decrement counter
SET: {counter} = {counter} - 1

// Subtract from accumulated value
SET: {remaining} = {total} - {used}

// Subtract multiple values
SET: {result} = {value} - {sub1} - {sub2}
```

---

### MULTIPLY

Multiply a variable by a numeric value.

```kotlin
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "operation" to "MULTIPLY",
        "name" to "total",
        "value" to "2"
    ),
    executionOrder = 1
)
```

**Examples**:

```kotlin
// Double value
SET: {total} = {total} * 2

// Multiply by percentage
SET: {discount} = {price} * 0.10

// Scale value
SET: {scaled} = {value} * 100
```

---

### DIVIDE

Divide a variable by a numeric value.

```kotlin
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "operation" to "DIVIDE",
        "name" to "total",
        "value" to "10"
    ),
    executionOrder = 1
)
```

**Examples**:

```kotlin
// Calculate average
SET: {average} = {total} / {count}

// Calculate percentage
SET: {percent} = {value} / 100

// Split evenly
SET: {share} = {total} / 3
```

---

### APPEND

Append text to a string variable.

```kotlin
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "operation" to "APPEND",
        "name" to "message",
        "value" to " World"
    ),
    executionOrder = 1
)
```

**Examples**:

```kotlin
// Append text
SET: {message} = {message} + " World"
// Result: "Hello World"

// Build string incrementally
SET: {result} = {result} + ", " + {item}

// Append with variable
SET: {text} = {prefix} + " - " + {variable}
```

---

### SUBSTRING

Extract a substring from a string variable.

```kotlin
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "operation" to "SUBSTRING",
        "name" to "extracted",
        "value" to "Hello World",
        "startIndex" to 0,
        "endIndex" to 5
    ),
    executionOrder = 0
)
```

**Result**: "Hello"

**Examples**:

```kotlin
// Extract first 5 characters
SET: {short} = SUBSTRING({longText}, 0, 5)

// Extract last 10 characters
SET: {end} = SUBSTRING({text}, {length} - 10, {length})

// Extract between positions
SET: {middle} = SUBSTRING({text}, 5, 10)
```

## Variable Storage

### Database Schema

```kotlin
@Entity(tableName = "variables", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
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

### DTO to Entity Mapping

```kotlin
// Convert DTO to Entity
fun VariableDTO.toEntity(): VariableEntity {
    return VariableEntity(
        name = variableName,
        value = variableValue,
        scope = scope,
        macroId = macroId
    )
}

// Convert Entity to DTO
fun VariableEntity.toDTO(): VariableDTO {
    return VariableDTO(
        variableName = name,
        variableValue = value,
        scope = scope,
        macroId = macroId
    )
}
```

### Variable DAO Operations

```kotlin
@Dao
interface VariableDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(variable: VariableEntity): Long

    // Update
    @Update
    suspend fun update(variable: VariableEntity)

    // Get variables
    @Query("SELECT * FROM variables WHERE macro_id = :macroId AND scope = 'LOCAL'")
    suspend fun getLocalVariables(macroId: Long): List<VariableEntity>

    @Query("SELECT * FROM variables WHERE macro_id IS NULL AND scope = 'GLOBAL'")
    suspend fun getGlobalVariables(): List<VariableEntity>

    @Query("SELECT * FROM variables WHERE name = :name AND scope = :scope")
    suspend fun getVariableByNameAndScope(name: String, scope: String): VariableEntity?

    @Query("SELECT * FROM variables WHERE name = :name")
    suspend fun getVariableByName(name: String): VariableEntity?

    // Delete
    @Query("DELETE FROM variables WHERE macro_id = :macroId AND scope = 'LOCAL'")
    suspend fun deleteLocalVariables(macroId: Long)

    @Query("DELETE FROM variables WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

## Variable Resolution

### Placeholder Syntax

Variables are referenced in action configurations using placeholders:

- **Local Variables**: `{variableName}`
- **Global Variables**: `{globalVariableName}`

**Examples**:

```kotlin
// Local variable reference
mapOf("message" to "Hello {userName}!")

// Global variable reference
mapOf("url" to "https://example.com/api?token={apiToken}")

// Mixed references
mapOf("text" to "From {globalUser}: {localMessage}")
```

### Resolution Process

```
┌─────────────────────────────────────────────────────┐
│ 1. Action Configuration                                      │
│    Contains placeholders: {variableName}             │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│ 2. Variable Resolution                                     │
│    GetVariableUseCase retrieves variable values        │
│    - Load from database                                 │
│    - Handle missing variables                          │
│    - Type conversion (string, number, boolean)              │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│ 3. Placeholder Replacement                                 │
│    Replace all {variableName} with actual values      │
│    - Global: {globalVar} → "actual value"            │
│    - Local: {localVar} → "actual value"                │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│ 4. Action Execution                                        │
│    Action executes with resolved values                     │
└─────────────────────────────────────────────────────┘
```

### Resolution Example

```kotlin
// Configuration
val config = mapOf(
    "message" to "Hello {userName}! Your balance is {balance} dollars."
)

// Database values
val userName = VariableEntity(name = "userName", value = "John")
val balance = VariableEntity(name = "balance", value = "42")

// After resolution
val resolved = "Hello John! Your balance is 42 dollars."
```

### Variable Value Types

Variables are stored as strings but interpreted based on context:

```kotlin
// String value
VariableEntity(name = "name", value = "John")
// Resolution: "John" (string)

// Number value
VariableEntity(name = "count", value = "42")
// Resolution: 42 (number) for arithmetic, "42" (string) for concatenation)

// Boolean value
VariableEntity(name = "enabled", value = "true")
// Resolution: true (boolean)
```

## Using Variables in Actions

### Simple Variable Usage

```kotlin
// Action 1: Set variable
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "name" to "userName",
        "value" to "John"
    ),
    executionOrder = 0
)

// Action 2: Use variable in message
ActionDTO(
    actionType = "SEND_SMS",
    actionConfig = mapOf(
        "phoneNumber" to "1234567890",
        "message" to "Hello {userName}, this is AutoDroid!"
    )
)
```

**Result**: SMS sent with "Hello John, this is AutoDroid!"

---

### Arithmetic in Actions

```kotlin
// Action 1: Initialize counter
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf("name" to "count", "value" to "0"),
    executionOrder = 0
)

// Action 2: Increment counter in loop
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "name" to "count",
        "value" to "{count} + 1",
        "operation" to "ADD"
    ),
    executionOrder = 1
)

// Action 3: Use counter in message
ActionDTO(
    actionType = "SHOW_TOAST",
    actionConfig = mapOf("message" to "Iteration: {count}"),
    executionOrder = 2
)
```

---

### String Operations

```kotlin
// Action 1: Start with prefix
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "name" to "message",
        "value" to "Processing: "
    ),
    executionOrder = 0
)

// Action 2: Append item
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "name" to "message",
        "value" to "{item}",
        "operation" to "APPEND"
    ),
    executionOrder = 1
)

// Action 3: Show final message
ActionDTO(
    actionType = "SHOW_TOAST",
    actionConfig = mapOf("message" to "{message}"),
    executionOrder = 2
)
```

---

### Complex Expressions

```kotlin
// Action 1: Build complex string
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "name" to "report",
        "value" to "Items: {count}, Total: ${count} * {price}"
    ),
    executionOrder = 0
)

// Action 2: Use in email
ActionDTO(
    actionType = "SEND_EMAIL",
    actionConfig = mapOf(
        "body" to "{report}"
    )
)
```

---

### Variable in Conditions

```kotlin
// Action 1: Compare variable to value
ActionDTO(
    actionType = "IF_CONDITION",
    actionConfig = mapOf(
        "condition" to mapOf(
            "leftOperand" to "{counter}",
            "operator" to ">",
            "rightOperand" to "10"
        ),
        "trueActions" to listOf(
            ActionDTO(
                actionType = "SHOW_TOAST",
                actionConfig = mapOf("message" to "Counter exceeded!")
            )
        )
    ),
    executionOrder = 0
)
```

## Using Variables in Constraints

### Variable-Based Constraints

```kotlin
// Constraint: Only execute when battery is below threshold
ConstraintDTO(
    constraintType = "BATTERY_LEVEL",
    constraintConfig = mapOf(
        "minLevel" to "{batteryThreshold}",
        "maxLevel" to "100"
    )
)

// Set threshold dynamically
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "name" to "batteryThreshold",
        "value" to "20"
    )
)
```

---

### Dynamic Constraints

```kotlin
// Constraint: Time range with variable times
ConstraintDTO(
    constraintType = "TIME_RANGE",
    constraintConfig = mapOf(
        "startTime" to "{workStartTime}",
        "endTime" to "{workEndTime}"
    )
)

// Set work hours based on day
ActionDTO(
    actionType = "IF_CONDITION",
    actionConfig = mapOf(
        "condition" to mapOf(
            "leftOperand" to "{isWeekday}",
            "operator" to "==",
            "rightOperand" to "true"
        ),
        "trueActions" to listOf(
            ActionDTO(
                actionType = "SET_VARIABLE",
                actionConfig = mapOf(
                    "name" to "workStartTime",
                    "value" to "09:00"
                )
            )
        )
    )
)
```

## Variable Scope

### Local Variable Scope

- **Lifetime**: Single macro execution
- **Access**: Only within that macro
- **Cleanup**: Deleted after macro completes
- **Visibility**: Not accessible by other macros

**Example**:

```kotlin
// Macro 1 sets {counter} to 0
// Macro 2 sets {counter} to 0 (different variable)
// Each macro maintains its own local variable
```

---

### Global Variable Scope

- **Lifetime**: Until changed/deleted
- **Access**: All macros
- **Persistence**: Stored in database permanently
- **Visibility**: Accessible by any macro

**Example**:

```kotlin
// Global variable set once
SET_GLOBAL: {userName} = "John"

// Used by multiple macros
// Macro 1: "Hello {userName}"
// Macro 2: "Goodbye {userName}"
```

---

### Scope Lookup Order

When resolving `{variableName}`:

1. **First**: Check for local variable with current `macroId`
2. **Second**: If not found locally, check for global variable
3. **Third**: If not found, use empty string or throw error

```kotlin
fun resolveVariable(
    name: String,
    macroId: Long?
): String {
    // 1. Check local variable first
    val localVar = getVariable(name, "LOCAL", macroId)
    if (localVar != null) {
        return localVar.value
    }

    // 2. Check global variable
    val globalVar = getVariable(name, "GLOBAL", null)
    if (globalVar != null) {
        return globalVar.value
    }

    // 3. Variable not found
    Timber.w("Variable not found: $name")
    return ""
}
```

### Shadowing

Local variables can "shadow" global variables of the same name:

```kotlin
// Global: {counter} = 100
// Local in Macro 1: {counter} = 0  // Shadows global
// Resolution: Uses local (0), not global (100)
```

**Best Practice**: Avoid variable name shadowing for clarity.

## Variable Lifecycle

### Creation

```kotlin
// Via SET_VARIABLE action
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "name" to "newVar",
        "value" to "initial",
        "scope" to "LOCAL"
    )
)
```

### Update

```kotlin
// Via SET_VARIABLE with operation
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "name" to "existingVar",
        "value" to "5",  // For SET operation
        // or: "value" to "{existingVar} + 1"  // For ADD operation
        "operation" to "SET"  // or "ADD"
    )
)
```

### Deletion

**Local Variables**:

- Automatically deleted when macro completes
- Deleted when macro is disabled/deleted

**Global Variables**:

- Persist until explicitly deleted
- Must be deleted manually via action or UI

```kotlin
// Delete global variable (if supported by action type)
ActionDTO(
    actionType = "DELETE_VARIABLE",  // Hypothetical action
    actionConfig = mapOf(
        "name" to "oldGlobalVar",
        "scope" to "GLOBAL"
    )
)
```

## Variable Management UI

AutoDroid provides a centralized interface for managing global variables, accessible via **Settings > Variable Management**.

### Key Features

- **Global View**: See all persistent global variables in one list.
- **CRUD Operations**: Create, Read, Update, and Delete global variables without needing to run a macro.
- **Usage Tracking**: Identify which macros are currently referencing a specific global variable.
- **Type Awareness**: Visual indicators for String, Number, and Boolean variable types.

### Managing Variables

1. **Adding**: Tap the **+** button, provide a unique name and initial value.
2. **Editing**: Tap an existing variable to modify its value.
3. **Deleting**: Swipe left on a variable or use the delete icon to remove it. **Warning**: This may break macros that depend on this variable.
4. **Search**: Quickly find variables by name or value using the search bar.

## Best Practices

### 1. Naming Conventions

```kotlin
// Good: Descriptive, clear names
{userName}         // User's name
{retryCount}       // Number of retries
{lastExecutionTime} // When last executed

// Bad: Vague, ambiguous names
{var1}            // What is this?
{data}             // What data?
{temp}             // Temporary what?
```

### 2. Variable Initialization

```kotlin
// Good: Initialize before use
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf("name" to "counter", "value" to "0"),
    executionOrder = 0
)

// Later in macro
ActionDTO(
    actionType = "SHOW_TOAST",
    actionConfig = mapOf("message" to "Count: {counter}"),
    executionOrder = 5
)

// Bad: Use undefined variable
ActionDTO(
    actionType = "SHOW_TOAST",
    actionConfig = mapOf("message" to "Count: {undefinedVar}")
)
```

### 3. Type Safety

```kotlin
// Good: Explicit type handling
val number = variableValue.toIntOrNull() ?: 0
val string = variableValue
val boolean = variableValue.toBooleanStrict() ?: false

// Bad: Assume string is number
val number = variableValue.toInt()  // Crashes if not numeric
```

### 4. Variable Documentation

```kotlin
// Add description to variable name
val macroName = "Data Processing Macro"
val variables = mapOf(
    "inputFile" to "path/to/input.txt",
    "outputFile" to "path/to/output.txt",
    "processedCount" to "Number of records processed"
)
```

### 5. Variable Validation

```kotlin
// Check if variable exists before using
val variable = getVariable("counter")
if (variable == null) {
    Timber.w("Variable 'counter' not initialized")
    // Handle gracefully
}

// Validate numeric operations
val value = getVariable("count")?.value?.toIntOrNull()
if (value == null) {
    Timber.e("Variable 'count' is not numeric")
    // Handle error
}
```

### 6. Scope Management

```kotlin
// Good: Use local variables when possible
// Temporary counter within macro
SET_LOCAL: {iterationCount} = 0

// Only use global when needed
// User preferences
SET_GLOBAL: {userEmail} = "user@example.com"
```

### 7. Performance Considerations

```kotlin
// Good: Minimize database queries
// Cache variables in memory during macro execution
val variables = mutableMapOf<String, String>()
loadLocalVariables(macroId).forEach {
    variables[it.name] = it.value
}

// Bad: Query database for each variable usage
val value = getVariable("counter")  // Database query
```

### 8. Error Handling

```kotlin
// Good: Handle missing variables gracefully
val value = getVariable("missingVar")?.value ?: "default"
Timber.d("Using default value for missing variable")

// Bad: Crash on missing variable
val value = getVariable("missingVar")!!.value  // Crashes
```

## Common Issues

### Variable Not Found

**Possible Causes**:

- Typo in variable name
- Variable not initialized
- Wrong scope (local vs global)
- Variable deleted before use

**Solutions**:

1. Check variable name spelling
2. Ensure variable is set before use
3. Verify variable scope (LOCAL vs GLOBAL)
4. Check execution logs for resolution errors

### Type Mismatch

**Possible Causes**:

- String variable used in arithmetic
- Number variable used as string without conversion
- Boolean variable used incorrectly

**Solutions**:

1. Convert types explicitly when needed
2. Use string values for concatenation
3. Use numeric values for arithmetic
4. Handle type conversion errors

### Variable Not Updating

**Possible Causes**:

- Variable shadowing
- Wrong variable name used
- SET operation failed silently
- Database write failure

**Solutions**:

1. Check for variable name conflicts
2. Verify SET operation completed
3. Check logs for errors
4. Test variable operations in isolation

### Local Variable Persistence

**Possible Causes**:

- Local variable persists across macro executions
- Variable not cleaned up after execution
- Database deletion failed

**Solutions**:

1. Verify local variables are deleted after macro
2. Check deletion logic in ExecuteMacroUseCase
3. Test macro execution multiple times
4. Clear variables at macro start

### Global Variable Conflicts

**Possible Causes**:

- Multiple macros setting same global variable
- Race conditions in global variable updates
- Unexpected overwrites

**Solutions**:

1. Use specific global variable names
2. Document global variable usage
3. Consider using local variables instead
4. Add conflict detection

### Arithmetic Errors

**Possible Causes**:

- Division by zero
- Integer overflow
- Type conversion failures

**Solutions**:

1. Validate denominator before division
2. Use appropriate numeric types
3. Handle conversion errors gracefully
4. Use safe arithmetic operations

### String Operation Errors

**Possible Causes**:

- Substring out of bounds
- Empty string operations
- Invalid substring indices

**Solutions**:

1. Validate indices before substring operation
2. Handle empty strings gracefully
3. Use safe substring operations
4. Check string length before accessing indices

### Memory Issues

**Possible Causes**:

- Too many variables stored
- Large variable values
- Variable leaks

**Solutions**:

1. Clean up local variables after execution
2. Use reasonable variable value sizes
3. Delete unused global variables
4. Monitor variable storage usage

## Advanced Patterns

### Counter Pattern

```kotlin
// Initialize
SET_LOCAL: {count} = 0

// Increment in loop
FOR: 10 iterations
  DO: SET_LOCAL: {count} = {count} + 1
END

// Use counter
SHOW_TOAST: "Loop ran {count} times"
```

### Accumulator Pattern

```kotlin
// Initialize
SET_LOCAL: {sum} = 0

// Accumulate values
FOR: 5 iterations
  DO: SET_LOCAL: {sum} = {sum} + {item}
END

// Use accumulator
SHOW_TOAST: "Total: {sum}"
```

### Flag Pattern

```kotlin
// Initialize flags
SET_LOCAL: {errorFound} = false
SET_LOCAL: {processingComplete} = false

// Set flags during execution
IF: {error} occurs
  DO: SET_LOCAL: {errorFound} = true
END

// Use flags
IF: {errorFound} == true
  DO: SHOW_TOAST: "Error occurred!"
END
```

### State Machine Pattern

```kotlin
// Define states
SET_LOCAL: {state} = "idle"

// Transition states
IF: {trigger} == "start"
  DO: SET_LOCAL: {state} = "active"
END

// Act based on state
IF: {state} == "active"
  DO: [perform actions]
END
```

## See Also

- **[Actions Guide](ACTIONS.md)**: Using variables in actions
- **[Constraints Guide](CONSTRAINTS.md)**: Using variables in constraints
- **[Logic Control in Actions](ACTIONS.md#logic-control)**: Variable-based logic
- **[Architecture Documentation](ARCHITECTURE.md)**: Database and variable storage

---

**Happy variableizing!** 📊
