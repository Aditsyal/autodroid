# Database Documentation

## Table of Contents

- [Overview](#overview)
- [Schema](#schema)
- [Entities](#entities)
- [Data Access Objects](#data-access-objects-daos)
- [Relationships](#relationships)
- [Indexing Strategy](#indexing-strategy)
- [Migrations](#migrations)
- [Queries](#queries)
- [Best Practices](#best-practices)
- [Performance Optimations](#performance-optimations)
- [Testing](#testing)
- [Common Issues](#common-issues)

## Overview

AutoDroid uses **Room Database** as its local storage solution. Room provides an abstraction layer over SQLite, allowing compile-time verification of SQL queries and mapping to Kotlin objects.

### Why Room?

- **Type Safety**: Compile-time verification of SQL queries
- **Zero Boilerplate**: Reduces SQLite boilerplate code
- **Observable Queries**: Built-in Flow/LiveData support
- **Migration Support**: Handles schema changes gracefully
- **Testing**: Easy to test with in-memory databases

### Database Configuration

```kotlin
@Database(
    entities = [
        MacroEntity::class,
        TriggerEntity::class,
        ActionEntity::class,
        ConstraintEntity::class,
        VariableEntity::class,
        ExecutionLogEntity::class,
        LogicBlockEntity::class,
        TemplateEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AutomationDatabase : RoomDatabase()
```

## Schema

### Entity Relationship Diagram

```
┌─────────────────┐
│    MacroEntity  │
│    (macros)     │
│                 │
│  - id (PK)      │◄────────────────┐
│  - name         │                 │
│  - enabled      │                 │ FK              │ FK
│  - createdAt    │                 │                  │
│  - updatedAt    │                 │                  │
└─────────────────┘                 │                  │
        │                            │                  │
        │ FK                         │ FK               │
        ▼                            ▼                  ▼
┌─────────────────┐         ┌───────────────────┐         ┌─────────────────┐
│  TriggerEntity  │         │   ActionEntity     │         │ConstraintEntity│
│   (triggers)    │         │    (actions)      │         │ (constraints) │
│                 │         │                    │         │                │
│  - id (PK)      │         │  - id (PK)        │         │  - id (PK)     │
│  - macroId (FK) │◄────────│  - macroId (FK)   │◄────────│  - macroId (FK) │
│  - type         │         │  - type            │         │  - type         │
│  - config       │         │  - config          │         │  - config       │
└─────────────────┘         └───────────────────┘         └─────────────────┘
        │                            │                  │
        │ FK                         │ FK               │
        ▼                            ▼                  ▼
┌─────────────────┐         ┌───────────────────┐         ┌─────────────────┐
│ VariableEntity │         │ ExecutionLogEntity │         │ LogicBlockEntity│
│  (variables)   │         │ (execution_logs)  │         │ (logic_blocks) │
│                 │         │                    │         │                │
│  - id (PK)      │         │  - id (PK)        │         │  - id (PK)     │
│  - macroId (FK) │◄────────│  - macroId (FK)   │◄────────│  - macroId (FK) │
│  - name         │         │  - status          │         │  - type         │
│  - value        │         │  - executedAt      │         │  - config       │
│  - scope        │         │  - durationMs      │         │  - parentId      │
└─────────────────┘         └───────────────────┘         └─────────────────┘
                                                   │
                                                   │ FK (optional)
                                                   ▼
                                          ┌─────────────────┐
                                          │ TemplateEntity  │
                                          │  (templates)   │
                                          │                │
                                          │  - id (PK)      │
                                          │  - name         │
                                          │  - description  │
                                          │  - macroConfig  │
                                          └─────────────────┘
```

## Entities

### MacroEntity

Stores macro definitions.

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

**Indexes**:

- `enabled`: For quickly querying enabled macros
- `name`: For searching by name

---

### TriggerEntity

Stores trigger configurations.

```kotlin
@Entity(tableName = "triggers", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
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

**Indexes**:

- `macro_id`: For loading triggers for a macro
- `type`: For filtering by trigger type

---

### ActionEntity

Stores action configurations.

```kotlin
@Entity(tableName = "actions", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
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
    val config: String  // JSON string

    @ColumnInfo(name = "execution_order")
    val executionOrder: Int = 0,

    @ColumnInfo(name = "delay_after")
    val delayAfter: Long = 0
)
```

**Indexes**:

- `macro_id` + `execution_order`: For loading actions in order

---

### ConstraintEntity

Stores constraint configurations.

```kotlin
@Entity(tableName = "constraints", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
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

**Indexes**:

- `macro_id`: For loading constraints for a macro
- `type`: For filtering by constraint type

---

### VariableEntity

Stores variables (local and global).

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

**Indexes**:

- `macro_id`: For loading local variables for a macro
- `name` + `scope`: For quick variable lookup

---

### ExecutionLogEntity

Stores execution history.

```kotlin
@Entity(tableName = "execution_logs", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )
])
data class ExecutionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "macro_id", index = true)
    val macroId: Long,

    @ColumnInfo(name = "status", index = true)
    val status: String,  // "SUCCESS", "FAILURE", "SKIPPED"

    @ColumnInfo(name = "executed_at", index = true)
    val executedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long = 0,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null
)
```

**Indexes**:

- `macro_id`: For loading execution history for a macro
- `executed_at`: For time-based queries
- `status`: For filtering by status

---

### LogicBlockEntity

Stores logic control blocks (if/else, loops).

```kotlin
@Entity(tableName = "logic_blocks", foreignKeys = [
    ForeignKey(
        entity = MacroEntity::class,
        parentColumns = ["id"],
        childColumns = ["macro_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )
])
data class LogicBlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "macro_id", index = true)
    val macroId: Long,

    @ColumnInfo(name = "type")
    val type: String,  // "IF", "WHILE", "FOR"

    @ColumnInfo(name = "config")
    val config: String,  // JSON string

    @ColumnInfo(name = "parent_id", index = true)
    val parentId: Long? = null  // For nesting
)
```

**Indexes**:

- `macro_id`: For loading logic blocks for a macro
- `parent_id`: For nested logic blocks

---

### TemplateEntity

Stores pre-configured macro templates.

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
    val macroConfig: String  // JSON of complete macro configuration
)
```

## Data Access Objects (DAOs)

### MacroDao

```kotlin
@Dao
interface MacroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(macro: MacroEntity): Long

    // ... other methods

    /**
     * Synchronous version for file export/import operations
     */
    @Query("SELECT * FROM macros")
    fun getAllMacrosSync(): List<MacroEntity>

    @Query("SELECT * FROM macros WHERE name = :name")
    fun getMacroByNameSync(name: String): MacroEntity?
}
```

**Usage**:

```kotlin
// Insert
val id = macroDao.insert(macroEntity)

// Observe
macroDao.getAllEnabledMacros().collect { macros ->
    // Update UI
}

// Update
macroDao.updateEnabled(macroId, false)
```

---

### TriggerDao

```kotlin
@Dao
interface TriggerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trigger: TriggerEntity): Long

    @Update
    suspend fun update(trigger: TriggerEntity)

    @Delete
    suspend fun delete(trigger: TriggerEntity)

    @Query("SELECT * FROM triggers WHERE macro_id = :macroId")
    fun getTriggersByMacroId(macroId: Long): Flow<List<TriggerEntity>>

    @Query("SELECT * FROM triggers WHERE id = :id")
    suspend fun getTriggerById(id: Long): TriggerEntity?

    @Query("DELETE FROM triggers WHERE macro_id = :macroId")
    suspend fun deleteByMacroId(macroId: Long)

    @Query("DELETE FROM triggers WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

---

### ActionDao

```kotlin
@Dao
interface ActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: ActionEntity): Long

    @Update
    suspend fun update(action: ActionEntity)

    @Delete
    suspend fun delete(action: ActionEntity)

    @Query("SELECT * FROM actions WHERE macro_id = :macroId ORDER BY execution_order")
    fun getActionsByMacroId(macroId: Long): Flow<List<ActionEntity>>

    @Query("SELECT * FROM actions WHERE id = :id")
    suspend fun getActionById(id: Long): ActionEntity?

    @Query("DELETE FROM actions WHERE macro_id = :macroId")
    suspend fun deleteByMacroId(macroId: Long)
}
```

---

### ConstraintDao

```kotlin
@Dao
interface ConstraintDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(constraint: ConstraintEntity): Long

    @Update
    suspend fun update(constraint: ConstraintEntity)

    @Delete
    suspend fun delete(constraint: ConstraintEntity)

    @Query("SELECT * FROM constraints WHERE macro_id = :macroId")
    fun getConstraintsByMacroId(macroId: Long): Flow<List<ConstraintEntity>>

    @Query("SELECT * FROM constraints WHERE id = :id")
    suspend fun getConstraintById(id: Long): ConstraintEntity?

    @Query("DELETE FROM constraints WHERE macro_id = :macroId")
    suspend fun deleteByMacroId(macroId: Long)
}
```

---

### VariableDao

```kotlin
@Dao
interface VariableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(variable: VariableEntity): Long

    // ... other methods

    @Query("SELECT * FROM variables WHERE name = :name AND scope = 'GLOBAL'")
    fun getGlobalVariableByNameSync(name: String): VariableEntity?
}
```

---

### ExecutionLogDao

```kotlin
@Dao
interface ExecutionLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ExecutionLogEntity): Long

    @Query("SELECT * FROM execution_logs WHERE macro_id = :macroId ORDER BY executed_at DESC LIMIT 100")
    fun getExecutionLogsByMacroId(macroId: Long): Flow<List<ExecutionLogEntity>>

    @Query("SELECT * FROM execution_logs WHERE status = :status ORDER BY executed_at DESC LIMIT 50")
    fun getExecutionLogsByStatus(status: String): Flow<List<ExecutionLogEntity>>

    @Query("SELECT * FROM execution_logs WHERE executed_at > :timestamp ORDER BY executed_at DESC")
    fun getRecentExecutionLogs(timestamp: Long): Flow<List<ExecutionLogEntity>>

    @Query("DELETE FROM execution_logs WHERE executed_at < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)

    @Query("SELECT COUNT(*) FROM execution_logs")
    suspend fun getExecutionCount(): Int
}
```

---

### TemplateDao

```kotlin
@Dao
interface TemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: TemplateEntity): Long

    @Query("SELECT * FROM templates")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): TemplateEntity?

    @Query("DELETE FROM templates")
    suspend fun deleteAllTemplates()

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

## Relationships

### One-to-Many: Macro to Triggers

One macro can have multiple triggers:

```kotlin
// Loading triggers for a macro
macroDao.getMacroById(macroId).collect { macro ->
    val triggers = triggerDao.getTriggersByMacroId(macro.id)
    // triggers can be empty or have multiple entries
}
```

### One-to-Many: Macro to Actions

One macro can have multiple actions in execution order:

```kotlin
// Loading actions for a macro in order
macroDao.getMacroById(macroId).collect { macro ->
    val actions = actionDao.getActionsByMacroId(macro.id)
    // Actions are ordered by execution_order field
}
```

### One-to-Many: Macro to Constraints

One macro can have multiple constraints:

```kotlin
// Loading constraints for a macro
macroDao.getMacroById(macroId).collect { macro ->
    val constraints = constraintDao.getConstraintsByMacroId(macro.id)
    // All constraints must be satisfied for macro to run
}
```

### Many-to-One: Variables to Macro

Variables can belong to a macro (LOCAL) or be global:

```kotlin
// Load both local and global variables
val localVars = variableDao.getLocalVariables(macroId)
val globalVars = variableDao.getGlobalVariables()

// Combined list
val allVars = localVars + globalVars
```

### Composite Relationships

Loading complete macro with all details:

```kotlin
data class MacroWithDetails(
    @Embedded val macro: MacroEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "macro_id",
        entity = TriggerEntity::class
    )
    val triggers: List<TriggerEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "macro_id",
        entity = ActionEntity::class
    )
    val actions: List<ActionEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "macro_id",
        entity = ConstraintEntity::class
    )
    val constraints: List<ConstraintEntity>
)

@Query("SELECT * FROM macros WHERE id = :id")
fun getMacroWithDetails(id: Long): Flow<MacroWithDetails>
```

## Indexing Strategy

### Primary Key Indexes

All entities have indexed primary keys automatically created by Room:

```kotlin
@PrimaryKey(autoGenerate = true)
val id: Long
```

### Foreign Key Indexes

Foreign keys create indexes automatically:

```kotlin
@ForeignKey(
    entity = MacroEntity::class,
    parentColumns = ["id"],
    childColumns = ["macro_id"]
)
val macroId: Long  // Automatically indexed
```

### Custom Indexes

Additional indexes for query optimization:

```kotlin
@Index(value = ["macro_id", "type"])
val type: String

@Index(value = ["macro_id", "execution_order"])
val executionOrder: Int
```

### Index Usage Examples

**Query**: Get all enabled macros

```kotlin
@Query("SELECT * FROM macros WHERE enabled = 1")
// Uses index: enabled
```

**Query**: Get triggers for specific macro

```kotlin
@Query("SELECT * FROM triggers WHERE macro_id = :macroId")
// Uses index: macro_id
```

**Query**: Get recent executions

```kotlin
@Query("SELECT * FROM execution_logs WHERE executed_at > :timestamp ORDER BY executed_at DESC")
// Uses index: executed_at
```

### Index Strategy

1. **Index columns used in WHERE clauses**
2. **Index columns used in JOIN conditions**
3. **Index columns used in ORDER BY**
4. **Avoid over-indexing** (impacts write performance)
5. **Use composite indexes** for multi-column queries

## Migrations

### Migration Structure

```kotlin
companion object {
    private const val DATABASE_NAME = "automation_database.db"

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Migration logic here
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Migration logic here
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Migration logic here
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Migration logic here
        }
    }

    fun getDatabase(context: Context): AutomationDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AutomationDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5
            )
            .build()
    }
}
```

### Migration Examples

#### Adding a New Column

```kotlin
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new column
        database.execSQL(
            "ALTER TABLE macros ADD COLUMN description TEXT"
        )
    }
}
```

#### Changing Column Type

```kotlin
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create new table with new schema
        database.execSQL(
            """
            CREATE TABLE macros_new (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                enabled INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // 2. Copy data from old table
        database.execSQL(
            """
            INSERT INTO macros_new (id, name, enabled, created_at)
            SELECT id, name, enabled, created_at FROM macros
            """.trimIndent()
        )

        // 3. Drop old table
        database.execSQL("DROP TABLE macros")

        // 4. Rename new table to old table
        database.execSQL("ALTER TABLE macros_new RENAME TO macros")
    }
}
```

#### Adding a New Table

```kotlin
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create new table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS execution_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                macro_id INTEGER NOT NULL,
                status TEXT NOT NULL,
                executed_at INTEGER NOT NULL,
                FOREIGN KEY (macro_id) REFERENCES macros(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }
}
```

### Best Practices for Migrations

1. **Test Migrations**: Always test on both old and new schemas
2. **Use Transactions**: Wrap multiple operations in transactions
3. **Handle Data Loss**: Provide fallback for missing data
4. **Preserve User Data**: Never delete user data in migrations
5. **Version Carefully**: Increment version number only when schema changes

## Queries

### Complex Query Examples

#### Join Multiple Tables

```kotlin
data class MacroWithExecutionCount(
    @Embedded val macro: MacroEntity,
    @ColumnInfo(name = "execution_count")
    val executionCount: Int
)

@Query("""
    SELECT
        m.*,
        COUNT(el.id) as execution_count
    FROM macros m
    LEFT JOIN execution_logs el ON m.id = el.macro_id
    WHERE m.id = :id
    GROUP BY m.id
""")
fun getMacroWithExecutionCount(id: Long): Flow<MacroWithExecutionCount>
```

#### Aggregation Queries

```kotlin
@Query("""
    SELECT
        type,
        COUNT(*) as count
    FROM triggers
    WHERE macro_id IN (
        SELECT id FROM macros WHERE enabled = 1
    )
    GROUP BY type
""")
fun getTriggerTypeStatistics(): Flow<List<TriggerTypeStatistic>>
```

#### Date Range Queries

```kotlin
@Query("""
    SELECT * FROM execution_logs
    WHERE executed_at >= :startTime
    AND executed_at <= :endTime
    ORDER BY executed_at DESC
""")
fun getExecutionLogsInRange(
    startTime: Long,
    endTime: Long
): Flow<List<ExecutionLogEntity>>
```

#### Full-Text Search

```kotlin
@Query("""
    SELECT * FROM macros
    WHERE name LIKE '%' || :query || '%'
    COLLATE NOCASE
    ORDER BY name
""")
fun searchMacros(query: String): Flow<List<MacroEntity>>
```

### Query Performance Tips

1. **Use indexes**: Ensure WHERE and JOIN columns are indexed
2. **Limit results**: Use LIMIT for queries that don't need all results
3. **Avoid SELECT \***: Specify only needed columns
4. **Use transactions**: Group multiple write operations
5. **Batch operations**: Use batch inserts/updates where possible

## Best Practices

### 1. Type Safety

```kotlin
// Good: Use Room types
@Entity
data class User(
    @PrimaryKey val id: Long,
    val name: String
)

// Bad: Use raw queries
database.execSQL("INSERT INTO users VALUES (1, 'John')")
```

### 2. Use Flow for Reactive Updates

```kotlin
// Good: Observe changes
macroDao.getAllMacros().collect { macros ->
    updateUI(macros)
}

// Bad: Query once and cache
val macros = macroDao.getAllMacros() // Not reactive
```

### 3. Handle Nulls Properly

```kotlin
// Good: Handle null values
val macro = macroDao.getMacroById(id)
if (macro != null) {
    // Handle macro
}

// Bad: Force non-null
val macro = macroDao.getMacroById(id)!! // Can crash
```

### 4. Use Transactions for Multiple Writes

```kotlin
@Transaction
suspend fun insertMultipleMacros(macros: List<MacroEntity>) {
    macros.forEach { insert(it) }
}
```

### 5. Use Indexes Wisely

```kotlin
// Good: Index columns used in WHERE clauses
@Index(value = ["macro_id", "status"])
val macroId: Long

// Bad: Index columns never used in queries
@Index("created_at")  // Rarely queried
```

## Performance Optimations

### 1. Enable Query Logging

```kotlin
Room.databaseBuilder(context, AutomationDatabase::class.java)
    .setQueryCallback(QueryExecutor { sqlQuery, bindArgs ->
        Log.d("RoomSQL", "Query: $sqlQuery, Args: $bindArgs")
    })
    .build()
```

### 2. Use Efficient Data Types

```kotlin
// Good: Use appropriate types
val count: Int  // Small number

// Bad: Use String for numbers
val count: String  // Overhead
```

### 3. Avoid N+1 Queries

```kotlin
// Bad: Query in loop
for (macroId in macroIds) {
    val macro = macroDao.getMacroById(macroId)
}

// Good: Single query with IN clause
@Query("SELECT * FROM macros WHERE id IN (:ids)")
fun getMacrosByIds(ids: List<Long>): Flow<List<MacroEntity>>
```

### 4. Pagination for Large Datasets

```kotlin
// Good: Limit and offset
@Query("""
    SELECT * FROM execution_logs
    ORDER BY executed_at DESC
    LIMIT :limit OFFSET :offset
""")
fun getExecutionLogsPaginated(limit: Int, offset: Int): Flow<List<ExecutionLogEntity>>
```

### 5. Database Cleaning

```kotlin
// Clean old logs periodically
@Query("""
    DELETE FROM execution_logs
    WHERE executed_at < :threshold
""")
suspend fun deleteOldLogs(threshold: Long)

// Call weekly (in WorkManager)
val oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
executionLogDao.deleteOldLogs(oneWeekAgo)
```

## Testing

### In-Memory Database for Tests

```kotlin
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MacroDaoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var database: AutomationDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext()
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }
}
```

### Testing DAO Operations

```kotlin
@Test
fun `insert and retrieve macro`() {
    // Given
    val entity = MacroEntity(name = "Test Macro", enabled = true)

    // When
    val id = database.macroDao().insert(entity)
    val result = database.macroDao().getMacroById(id)

    // Then
    assertNotNull(result)
    assertEquals("Test Macro", result?.name)
}
```

### Testing Migrations

```kotlin
@Test
fun `migration from 1 to 2 works`() {
    val database = Room.inMemoryDatabaseBuilder(context)
        .addMigrations(MIGRATION_1_2)
        .build()

    // Create schema version 1
    createSchema1(database)

    // Migrate
    database.openHelper().onUpgrade(database.openHelper, 1, 2)

    // Verify schema version 2
    val expectedColumns = listOf("id", "name", "description")
    val actualColumns = getColumnNames(database, "macros")
    assertEquals(expectedColumns, actualColumns)
}
```

## Common Issues

### Foreign Key Constraint Violation

**Error**: `SQLiteConstraintException: FOREIGN KEY constraint failed`

**Causes**:

- Inserting row with non-existent foreign key
- Deleting parent row without handling children

**Solutions**:

1. Verify parent exists before inserting children
2. Use proper foreign key cascade operations
3. Handle database exceptions gracefully

### Unique Constraint Violation

**Error**: `SQLiteConstraintException: UNIQUE constraint failed`

**Causes**:

- Inserting duplicate primary key
- Inserting duplicate in indexed column without conflict strategy

**Solutions**:

1. Use appropriate conflict strategy: `OnConflictStrategy.REPLACE`
2. Check for existence before inserting
3. Handle constraint exceptions

### Database Locked

**Error**: `android.database.sqlite.SQLiteDatabaseLockedException`

**Causes**:

- Long-running transaction
- Concurrent writes
- Database file corruption

**Solutions**:

1. Keep transactions short
2. Use proper coroutine dispatchers
3. Close database connections properly
4. Handle lock exceptions with retry logic

### Slow Queries

**Symptoms**: Queries taking too long

**Causes**:

- Missing indexes
- Full table scans
- N+1 queries
- Large result sets

**Solutions**:

1. Add missing indexes
2. Use LIMIT to reduce result set size
3. Optimize queries with JOINs
4. Use batch operations

### Migration Failed

**Error**: Migration fails to apply

**Causes**:

- Incompatible schema changes
- Data loss in migration
- Bugs in migration logic

**Solutions**:

1. Test migrations thoroughly
2. Provide fallback for failed migrations
3. Export user data before migration
4. Handle migration exceptions with error reporting

## See Also

- **[Architecture Documentation](ARCHITECTURE.md)**: Database architecture
- **[Testing Guide](TESTING.md)**: Testing database code
- **[Repository Pattern](../data/repository/MacroRepositoryImpl.kt)**: Implementation examples

---

**Happy querying!** 🗄️
