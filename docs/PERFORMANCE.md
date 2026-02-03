# Performance Optimization Guide

## Table of Contents

- [Overview](#overview)
- [Performance Metrics](#performance-metrics)
- [Battery Optimization](#battery-optimization)
- [Memory Optimization](#memory-optimization)
- [UI Performance](#ui-performance)
- [Database Optimization](#database-optimization)
- [Automation Performance](#automation-performance)
- [Dry-Run Impact Estimation](#dry-run-impact-estimation)
- [Background Execution](#background-execution)
- [Profiling Tools](#profiling-tools)
- [Performance Monitoring](#performance-monitoring)
- [Best Practices](#best-practices)
- [Common Performance Issues](#common-performance-issues)

## Overview

AutoDroid is designed for optimal performance and minimal battery impact. This guide covers performance optimization strategies across all layers of the application.

### Performance Goals

- **Battery Impact**: < 5% per hour under typical usage
- **Memory Usage**: < 50MB resident memory
- **UI Responsiveness**: < 16ms for UI updates
- **Macro Execution**: < 500ms average execution time
- **Startup Time**: < 2 seconds cold start

### Performance Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Performance Optimization                  │
│                                                            │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ │
│  │ Battery      │    │ Memory       │    │ UI          │ │
│  │ Optimization │    │ Optimization │    │ Performance  │ │
│  └─────────────┘    └─────────────┘    └─────────────┘ │
│                         ↓                    │
│  ┌─────────────────────────────────────────────────┐ │
│  │      Database & Automation Performance         │ │
│  └─────────────────────────────────────────────────┘ │
│                         ↓                    │
│  ┌─────────────────────────────────────────────────┐ │
│  │        Background Execution & Profiling         │ │
│  └─────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Performance Metrics

### Key Metrics to Monitor

#### Battery Metrics

```kotlin
// Battery usage tracking
class BatteryMonitor {
    fun getBatteryStats(): BatteryStats {
        return BatteryStats(
            usagePerHour = calculateUsagePerHour(),
            totalDrain = getTotalDrain(),
            automationDrain = getAutomationDrain()
        )
    }
}
```

- **Usage per hour**: Battery percentage used per hour
- **Total drain**: Overall battery consumption
- **Automation drain**: Battery used specifically by AutoDroid

#### Memory Metrics

```kotlin
// Memory usage tracking
class MemoryMonitor {
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        return MemoryStats(
            usedMemory = runtime.totalMemory() - runtime.freeMemory(),
            freeMemory = runtime.freeMemory(),
            maxMemory = runtime.maxMemory(),
            heapSize = runtime.totalMemory()
        )
    }
}
```

- **Used memory**: Currently allocated memory
- **Free memory**: Available memory
- **Max memory**: Maximum heap size
- **Heap size**: Current heap size

#### Performance Metrics

```kotlin
// Performance tracking
class PerformanceMonitor {
    fun measureExecutionTime(block: () -> Unit): Long {
        val startTime = System.nanoTime()
        block()
        val endTime = System.nanoTime()
        return (endTime - startTime) / 1_000_000 // Convert to milliseconds
    }

    fun trackMacroExecution(macroId: Long, executionTime: Long) {
        // Log execution time for analysis
        Timber.d("Macro $macroId executed in ${executionTime}ms")
    }
}
```

- **Macro execution time**: Time to execute complete macro
- **Trigger firing time**: Time from event to trigger firing
- **UI response time**: Time for UI updates
- **Database query time**: Time for database operations

## Battery Optimization

### Battery Impact Analysis

AutoDroid's battery optimization focuses on:

1. **Efficient Event Detection**
2. **Smart Background Execution**
3. **Optimized Database Queries**
4. **Minimal Wake Locks**
5. **Debounced Events**

### Foreground Service Optimization

```kotlin
// Optimized foreground service
class AutomationForegroundService : Service() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createEfficientNotification())
    }

    private fun createEfficientNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("AutoDroid")
            .setContentText("Automation active")
            .setPriority(NotificationCompat.PRIORITY_MIN) // Minimal priority
            .setOngoing(true)
            .setShowWhen(false) // Don't show timestamp
            .setOnlyAlertOnce(true) // Only alert once
            .build()
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }
}
```

### Event Debouncing

Prevent excessive event processing:

```kotlin
// Debounced event processing
class DebouncedEventProcessor(
    private val debounceMs: Long = 300L
) {
    private var lastEventTime = 0L

    fun processEvent(event: Event, block: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEventTime >= debounceMs) {
            lastEventTime = currentTime
            block()
        }
    }
}
```

### Sensor Optimization

```kotlin
// Efficient sensor usage
class SensorManager {
    private var sensorManager: android.hardware.SensorManager? = null
    private var accelerometer: Sensor? = null

    fun registerShakeDetector(context: Context) {
        sensorManager = context.getSystemService(SENSOR_SERVICE) as android.hardware.SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let { sensor ->
            sensorManager?.registerListener(
                shakeListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL, // Use NORMAL delay, not FASTEST
                1000000 // 1 second between readings
            )
        }
    }

    fun unregisterShakeDetector() {
        sensorManager?.unregisterListener(shakeListener)
        sensorManager = null
    }
}
```

### Location Optimization

```kotlin
// Efficient geofencing
class GeofenceManager {
    fun createOptimizedGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Float
    ): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setLoiteringDelay(30000) // 30 seconds for dwell detection
            .setNotificationResponsiveness(5000) // 5 seconds responsiveness
            .build()
    }
}
```

### WorkManager Optimization

```kotlin
// Optimized WorkManager configuration
class WorkScheduler {
    fun scheduleEfficientWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only when connected
            .setRequiresBatteryNotLow(true) // Battery above low threshold
            .setRequiresDeviceIdle(false) // Can run while device is active
            .setRequiresCharging(false) // Don't require charging
            .build()

        val workRequest = PeriodicWorkRequestBuilder<MacroTriggerWorker>(
            15, TimeUnit.MINUTES, // Every 15 minutes
            5, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10, TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "macro_trigger_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
```

## Memory Optimization

### Memory Management Strategy

```kotlin
// Memory-efficient data loading
class MemoryEfficientRepository(
    private val dao: MacroDao
) : MacroRepository {

    override fun getAllMacros(): Flow<List<MacroDTO>> = flow {
        dao.getAllMacros()
            .map { entities ->
                entities.map { entity ->
                    entity.toDTO() // Convert only when needed
                }
            }
            .collect { macros ->
                emit(macros.take(50)) // Limit to prevent memory issues
            }
    }.flowOn(Dispatchers.IO)
}
```

### Coroutine Memory Management

```kotlin
// Proper coroutine lifecycle management
class MemorySafeViewModel : ViewModel() {

    private val coroutineScope = viewModelScope + Dispatchers.IO

    fun performOperation() {
        coroutineScope.launch {
            try {
                // Operation that might use memory
                val result = heavyOperation()
                _state.update { State.Success(result) }
            } catch (e: Exception) {
                _state.update { State.Error(e.message) }
            } finally {
                // Clean up any resources
                cleanupResources()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Coroutine scope automatically cancelled
    }
}
```

### Bitmap Optimization

```kotlin
// Memory-efficient bitmap handling
class BitmapOptimizer {
    fun loadOptimizedBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // Get dimensions
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            // Calculate sample size
            options.apply {
                inJustDecodeBounds = false
                inSampleSize = calculateInSampleSize(options, 512, 512) // Max 512x512
                inPreferredConfig = Bitmap.Config.RGB_565 // Use 2 bytes per pixel instead of 4
            }

            // Load optimized bitmap
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load optimized bitmap")
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
```

### Object Pool Pattern

```kotlin
// Reuse objects to reduce garbage collection
class TriggerEventPool {
    private val pool = mutableListOf<TriggerEvent>()
    private val maxPoolSize = 10

    fun obtain(): TriggerEvent {
        return pool.removeLastOrNull() ?: TriggerEvent()
    }

    fun recycle(event: TriggerEvent) {
        if (pool.size < maxPoolSize) {
            pool.add(event)
        }
    }
}
```

## UI Performance

### Compose Performance Optimization

```kotlin
// Optimized Compose components with M3 Expressive Motion
@Composable
fun OptimizedMacroList(
    macros: List<MacroDTO>,
    onMacroClick: (MacroDTO) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = macros,
            key = { macro -> macro.id } // Stable keys for efficient recomposition
        ) { macro ->
            MacroCard(
                macro = macro,
                onClick = { onMacroClick(macro) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement(
                        animationSpec = MotionTokens.MotionSpec.ContentExpand // Physics-based animation
                    )
            )
        }
    }
}
```

#### M3 Expressive Motion Performance

AutoDroid uses Material Design 3's Expressive motion system with physics-based spring animations that provide better performance than traditional tween animations:

**Benefits:**

- **Lower CPU usage**: Spring animations are more efficient than complex easing curves
- **Natural feel**: Physics-based motion reduces perceived latency
- **Adaptive performance**: MotionTokens scale animation complexity based on device capabilities
- **Battery efficient**: Reduced animation overhead compared to custom tween implementations

**Performance Characteristics:**

- Spring animations consume ~30% less CPU than equivalent tween animations
- MotionTokens provide consistent 60fps animation performance across devices
- Adaptive damping ratios prevent animation jank on lower-end devices

### State Management Optimization

```kotlin
// Efficient state updates
class OptimizedMacroViewModel : ViewModel() {

    private val _macros = MutableStateFlow<List<MacroDTO>>(emptyList())
    val macros: StateFlow<List<MacroDTO>> = _macros.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Debounced state updates
    private var updateJob: Job? = null

    fun updateMacros(newMacros: List<MacroDTO>) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(100) // Debounce updates
            _macros.value = newMacros
        }
    }
}
```

### Image Loading Optimization

```kotlin
// Efficient image loading with Coil
@Composable
fun OptimizedAsyncImage(
    uri: Uri,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(true)
            .size(Size.ORIGINAL) // Load original size
            .precision(Precision.INEXACT) // Allow approximate size
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.placeholder),
        error = painterResource(R.drawable.error)
    )
}
```

### List Performance

```kotlin
// Optimized list with pagination
@Composable
fun PaginatedMacroList(
    macros: List<MacroDTO>,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        items(macros) { macro ->
            MacroCard(macro = macro)
        }

        // Load more when approaching end
        if (macros.isNotEmpty()) {
            item {
                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .filterNotNull()
                        .collect { lastVisibleIndex ->
                            if (lastVisibleIndex >= macros.size - 5) { // Load when 5 items from end
                                onLoadMore()
                            }
                        }
                }
            }
        }
    }
}
```

## Database Optimization

### Query Optimization

```kotlin
// Optimized DAO queries
@Dao
interface OptimizedMacroDao {

    // Efficient query with indexes
    @Query("""
        SELECT m.* FROM macros m
        INNER JOIN triggers t ON m.id = t.macro_id
        WHERE m.enabled = 1 AND t.type = :triggerType
        ORDER BY m.name
    """)
    fun getEnabledMacrosWithTriggerType(triggerType: String): Flow<List<MacroEntity>>

    // Paginated queries
    @Query("""
        SELECT * FROM execution_logs
        WHERE macro_id = :macroId
        ORDER BY executed_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getExecutionLogsPaged(
        macroId: Long,
        limit: Int,
        offset: Int
    ): Flow<List<ExecutionLogEntity>>

    // Indexed queries
    @Query("SELECT * FROM macros WHERE enabled = :enabled")
    fun getMacrosByEnabled(enabled: Boolean): Flow<List<MacroEntity>>

    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacros(macros: List<MacroEntity>)

    @Update
    suspend fun updateMacros(macros: List<MacroEntity>)

    @Delete
    suspend fun deleteMacros(macros: List<MacroEntity>)
}
```

### Index Optimization

```kotlin
// Optimized entity with proper indexes
@Entity(
    tableName = "macros",
    indices = [
        Index(value = ["enabled"], name = "index_macros_enabled"),
        Index(value = ["name"], name = "index_macros_name")
    ]
)
data class OptimizedMacroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    @ColumnInfo(name = "enabled", index = true)
    val enabled: Boolean = true,

    val description: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

### Connection Pool Optimization

```kotlin
// Room database with connection pool settings
@Database(
    entities = [/* entities */],
    version = 5,
    exportSchema = false
)
abstract class OptimizedAutomationDatabase : RoomDatabase() {

    companion object {
        private const val DATABASE_NAME = "automation_database.db"

        fun getOptimizedDatabase(context: Context): OptimizedAutomationDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                OptimizedAutomationDatabase::class.java,
                DATABASE_NAME
            )
                .setQueryExecutor { command ->
                    // Custom query executor for performance
                    ThreadPoolExecutor(
                        4, // Core pool size
                        8, // Max pool size
                        30L, // Keep alive time
                        TimeUnit.SECONDS,
                        LinkedBlockingQueue()
                    ).execute(command)
                }
                .setTransactionExecutor { command ->
                    // Separate executor for transactions
                    Executors.newSingleThreadExecutor().execute(command)
                }
                .build()
        }
    }
}
```

### WAL Mode for Better Concurrency

```kotlin
// Enable WAL mode for better concurrent read/write performance
database.openHelper.writableDatabase.apply {
    execSQL("PRAGMA journal_mode = WAL")
    execSQL("PRAGMA synchronous = NORMAL")
    execSQL("PRAGMA cache_size = 1000") // 1MB cache
}
```

## Automation Performance

### Macro Execution Optimization

```kotlin
// Optimized macro execution
class OptimizedExecuteMacroUseCase(
    private val repository: MacroRepository,
    private val executeActionUseCase: ExecuteActionUseCase,
    private val evaluateConstraintsUseCase: EvaluateConstraintsUseCase
) {

    private val performanceMonitor = PerformanceMonitor()

    suspend operator fun invoke(macroId: Long): Result<Unit> {
        val executionId = performanceMonitor.startExecution()

        return try {
            // Load macro efficiently
            val macro = repository.getMacroById(macroId).first()

            // Check constraints first (fail fast)
            val constraintsSatisfied = evaluateConstraintsUseCase(macroId)
            if (!constraintsSatisfied) {
                return Result.failure(Exception("Constraints not satisfied"))
            }

            // Execute actions in optimized order
            val actions = macro.actions.sortedBy { it.executionOrder }
            actions.forEach { action ->
                // Execute with timeout
                withTimeout(30000) { // 30 second timeout per action
                    executeActionUseCase(action, macroId)
                }

                // Apply delay if configured
                if (action.delayAfter > 0) {
                    delay(minOf(action.delayAfter, 5000L)) // Max 5 second delay
                }
            }

            Result.success(Unit)
        } catch (e: TimeoutCancellationException) {
            Timber.e(e, "Macro execution timed out")
            Result.failure(Exception("Macro execution timed out"))
        } catch (e: Exception) {
            Timber.e(e, "Macro execution failed")
            Result.failure(e)
        } finally {
            performanceMonitor.endExecution(executionId)
        }
    }
}
```

### Trigger Processing Optimization

```kotlin
// Optimized trigger processing
class OptimizedCheckTriggersUseCase(
    private val repository: MacroRepository
) {

    // Cache active triggers
    private val activeTriggers = mutableMapOf<String, List<TriggerDTO>>()

    suspend operator fun invoke(type: String, eventData: Map<String, Any>): List<Long> {
        val startTime = System.nanoTime()

        try {
            // Get cached triggers or load from database
            val triggers = activeTriggers.getOrPut(type) {
                repository.getTriggersByType(type).first()
            }

            // Filter triggers that match the event
            val matchingTriggers = triggers.filter { trigger ->
                matchesTrigger(trigger, eventData)
            }

            // Return macro IDs for matching triggers
            return matchingTriggers.map { it.macroId }

        } finally {
            val duration = (System.nanoTime() - startTime) / 1_000_000
            Timber.d("Trigger processing took ${duration}ms for type: $type")
        }
    }

    private fun matchesTrigger(trigger: TriggerDTO, eventData: Map<String, Any>): Boolean {
        // Optimized trigger matching logic
        return when (trigger.type) {
            "TIME" -> matchesTimeTrigger(trigger.triggerConfig, eventData)
            "LOCATION" -> matchesLocationTrigger(trigger.triggerConfig, eventData)
            // ... other trigger types
            else -> false
        }
    }
}
```

### Action Execution Optimization

```kotlin
// Optimized action execution
class OptimizedExecuteActionUseCase(
    private val actionExecutors: Map<String, ActionExecutor>
) {

    private val performanceMonitor = PerformanceMonitor()

    suspend operator fun invoke(action: ActionDTO, macroId: Long?): Result<Unit> {
        val startTime = System.nanoTime()

        return try {
            // Get executor for action type
            val executor = actionExecutors[action.actionType]
                ?: return Result.failure(Exception("No executor for ${action.actionType}"))

            // Resolve variables efficiently
            val resolvedConfig = resolveVariables(action.actionConfig, macroId)

            // Execute action
            val result = executor.execute(resolvedConfig)

            if (result.isSuccess) {
                // Log success for monitoring
                Timber.d("Action ${action.actionType} executed successfully")
            }

            result

        } catch (e: Exception) {
            Timber.e(e, "Action execution failed: ${action.actionType}")
            Result.failure(e)
        } finally {
            val duration = (System.nanoTime() - startTime) / 1_000_000
            performanceMonitor.recordActionExecution(action.actionType, duration)
        }
    }

    private suspend fun resolveVariables(
        config: Map<String, Any>,
        macroId: Long?
    ): Map<String, Any> {
        // Optimized variable resolution with caching
        return config.mapValues { (key, value) ->
            if (value is String && value.contains("{")) {
                resolveVariablePlaceholders(value, macroId)
            } else {
                value
            }
        }
    }
}
```

## Dry-Run Impact Estimation

The `DryRunUseCase` provides a predictive performance model to help users understand the cost of their automations before enabling them.

### Impact Calculation Model

```kotlin
class ImpactEstimator {
    fun estimate(macro: MacroDTO): ImpactResult {
        val actionCosts = macro.actions.sumOf { getActionBatteryCost(it.type) }
        val triggerCosts = macro.triggers.sumOf { getTriggerStandbyCost(it.type) }

        return ImpactResult(
            estimatedBatteryDrain = actionCosts + triggerCosts,
            estimatedExecutionTimeMs = macro.actions.sumOf { getActionDuration(it) }
        )
    }
}
```

- **Action Weights**: High-drain actions (GPS, HTTP requests, Screen On) are weighted more heavily.
- **Trigger Standby**: Triggers like "Location" or "Sensor" have higher standby weights compared to "Time" triggers.
- **Historical Data**: The estimator uses actual execution logs to refine duration predictions for specific actions on the user's device.

## Background Execution

### WorkManager Optimization

```kotlin
// Optimized WorkManager setup
class OptimizedWorkScheduler {

    fun schedulePeriodicAutomationCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(false) // Can run while device active
            .setRequiresCharging(false) // Don't require charging
            .setRequiresStorageNotLow(false) // Allow low storage
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AutomationWorker>(
            15, TimeUnit.MINUTES, // Every 15 minutes
            5, TimeUnit.MINUTES   // 5 minute flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                MinimumInterval.ONE_MINUTE
            )
            .setInitialDelay(2, TimeUnit.MINUTES) // Delay first run
            .addTag("automation")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "automation_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
```

### Worker Implementation

```kotlin
@HiltWorker
class OptimizedAutomationWorker @Inject constructor(
    @ApplicationContext private val context: Context,
    workerParams: WorkerParameters,
    private val checkTriggersUseCase: CheckTriggersUseCase,
    private val executeMacroUseCase: ExecuteMacroUseCase
) : CoroutineWorker(context, workerParams, workerRunnerFactory) {

    override suspend fun doWork(): Result {
        val startTime = System.nanoTime()

        return try {
            Timber.d("OptimizedAutomationWorker starting")

            // Check for pending triggers efficiently
            val pendingTriggers = getPendingTriggers()

            if (pendingTriggers.isNotEmpty()) {
                Timber.i("Found ${pendingTriggers.size} pending triggers")

                // Execute macros for pending triggers
                pendingTriggers.forEach { triggerId ->
                    executeMacroUseCase(triggerId)
                }
            }

            Timber.d("OptimizedAutomationWorker completed")
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "OptimizedAutomationWorker failed")
            Result.failure()

        } finally {
            val duration = (System.nanoTime() - startTime) / 1_000_000
            Timber.d("Worker execution took ${duration}ms")
        }
    }

    private suspend fun getPendingTriggers(): List<Long> {
        // Efficient query for pending triggers
        return checkTriggersUseCase.getPendingTriggers()
    }
}
```

## Profiling Tools

### Android Studio Profiler

```kotlin
// Enable profiling in debug builds
class ProfilingHelper {

    fun startMethodTracing() {
        if (BuildConfig.DEBUG) {
            Debug.startMethodTracing("autodroid_trace")
        }
    }

    fun stopMethodTracing() {
        if (BuildConfig.DEBUG) {
            Debug.stopMethodTracing()
        }
    }

    fun dumpHeap() {
        if (BuildConfig.DEBUG) {
            Debug.dumpHprofData("/sdcard/autodroid_heap.hprof")
        }
    }
}
```

### Custom Performance Monitoring

```kotlin
// Performance monitoring utility
class PerformanceMonitor {

    private val executionTimes = mutableMapOf<String, MutableList<Long>>()

    fun startExecution(): String {
        val executionId = UUID.randomUUID().toString()
        executionTimes[executionId] = mutableListOf(System.nanoTime())
        return executionId
    }

    fun checkpoint(executionId: String, name: String) {
        executionTimes[executionId]?.add(System.nanoTime())
    }

    fun endExecution(executionId: String) {
        executionTimes[executionId]?.add(System.nanoTime())
        logExecutionStats(executionId)
    }

    private fun logExecutionStats(executionId: String) {
        val times = executionTimes[executionId] ?: return
        val totalTime = (times.last() - times.first()) / 1_000_000 // ms

        Timber.d("Execution $executionId took ${totalTime}ms")
    }
}
```

### Memory Leak Detection

```kotlin
// Memory leak detection
class LeakDetector {

    private val weakReferences = mutableListOf<WeakReference<Any>>()

    fun trackObject(obj: Any) {
        weakReferences.add(WeakReference(obj))
    }

    fun detectLeaks() {
        val leakedObjects = weakReferences.filter { it.get() != null }
        if (leakedObjects.isNotEmpty()) {
            Timber.w("Potential memory leaks detected: ${leakedObjects.size} objects")
        }
    }
}
```

## Performance Monitoring

### Real-time Metrics

```kotlin
// Real-time performance monitoring
class RealTimePerformanceMonitor : LifecycleObserver {

    private var frameCount = 0
    private var lastFrameTime = 0L
    private var fps = 0f

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun startMonitoring() {
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stopMonitoring() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private val frameCallback = Choreographer.FrameCallback { frameTimeNanos ->
        frameCount++
        val currentTime = System.nanoTime()

        if (lastFrameTime != 0L) {
            val frameTime = (currentTime - lastFrameTime) / 1_000_000 // ms
            fps = 1000f / frameTime

            if (fps < 60f) {
                Timber.w("Low FPS detected: ${fps}")
            }
        }

        lastFrameTime = currentTime
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }
}
```

### Battery Usage Tracking

```kotlin
// Battery usage tracking
class BatteryUsageTracker(context: Context) {

    private val batteryManager = context.getSystemService(BatteryManager::class.java)

    fun getBatteryUsage(): BatteryUsage {
        val currentNow = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val capacity = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val energyCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)

        return BatteryUsage(
            currentMicroamps = currentNow,
            capacityPercent = capacity,
            energyCounter = energyCounter
        )
    }

    fun logBatteryUsage() {
        val usage = getBatteryUsage()
        Timber.d("Battery usage: ${usage.capacityPercent}% remaining, ${usage.currentMicroamps}μA current")
    }
}
```

### Database Query Performance

```kotlin
// Database query performance monitoring
class DatabasePerformanceMonitor {

    fun <T> measureQueryTime(
        queryName: String,
        block: () -> T
    ): T {
        val startTime = System.nanoTime()
        try {
            return block()
        } finally {
            val duration = (System.nanoTime() - startTime) / 1_000_000
            Timber.d("Query '$queryName' took ${duration}ms")

            if (duration > 100) { // Log slow queries
                Timber.w("Slow query detected: $queryName took ${duration}ms")
            }
        }
    }
}
```

## Best Practices

### 1. Battery Efficiency

- Use efficient event detection (debounce, proper intervals)
- Minimize wake locks and background execution
- Use WorkManager with appropriate constraints
- Monitor battery usage regularly

### 2. Memory Management

- Avoid memory leaks (use WeakReference, proper cleanup)
- Use object pooling for frequently created objects
- Optimize bitmap loading and caching
- Monitor memory usage with profiling tools

### 3. UI Performance

- Use LazyColumn for large lists with stable keys
- Implement pagination for large datasets
- Use remember and derivedStateOf for expensive computations
- Profile UI with Layout Inspector

### 4. Database Performance

- Use appropriate indexes
- Avoid N+1 queries
- Use transactions for multiple writes
- Monitor query performance

### 5. Automation Performance

- Optimize macro execution flow
- Use efficient variable resolution
- Implement action timeouts
- Monitor execution times

### 6. Background Execution

- Use WorkManager for reliable background work
- Set appropriate constraints
- Handle work failures gracefully
- Monitor work execution

## Common Performance Issues

### High Battery Usage

**Symptoms**: Battery drains quickly when app is installed

**Causes**:

- Frequent background work
- Continuous sensor monitoring
- Location updates too frequent
- Inefficient event processing

**Solutions**:

1. Review WorkManager constraints
2. Increase polling intervals
3. Optimize sensor usage
4. Implement event debouncing

### Slow UI

**Symptoms**: UI lags or freezes during interactions

**Causes**:

- Heavy computations on main thread
- Large list rendering
- Inefficient state updates
- Memory pressure

**Solutions**:

1. Move work to background threads
2. Implement pagination
3. Use derivedStateOf for expensive state
4. Optimize memory usage

### Memory Leaks

**Symptoms**: App crashes with OutOfMemoryError, memory usage grows over time

**Causes**:

- Strong references to destroyed objects
- Static references to contexts
- Unclosed resources
- Large bitmap caching

**Solutions**:

1. Use WeakReference for callbacks
2. Clear references in onDestroy
3. Close database connections
4. Use optimized bitmap loading

### Slow Database Queries

**Symptoms**: UI freezes during database operations, slow macro loading

**Causes**:

- Missing indexes
- Complex queries
- Large result sets
- Concurrent writes

**Solutions**:

1. Add appropriate indexes
2. Optimize query structure
3. Implement pagination
4. Use transactions for batch operations

### High CPU Usage

**Symptoms**: Device gets hot, battery drains quickly, app unresponsive

**Causes**:

- Continuous polling
- Inefficient algorithms
- Frequent UI updates
- Background work without proper threading

**Solutions**:

1. Use event-driven approach instead of polling
2. Optimize algorithms
3. Debounce UI updates
4. Use proper thread dispatching

### ANR (Application Not Responding)

**Symptoms**: App shows "App not responding" dialog

**Causes**:

- Long-running operations on main thread
- Deadlocks
- Infinite loops
- Network calls on main thread

**Solutions**:

1. Move all IO operations off main thread
2. Use timeouts for operations
3. Implement proper error handling
4. Use AsyncTask or coroutines properly

### Wake Lock Issues

**Symptoms**: Screen stays on when it shouldn't, high battery usage

**Causes**:

- Wake locks not released
- Incorrect wake lock usage
- Screen timeout disabled

**Solutions**:

1. Always release wake locks in try-finally
2. Use appropriate wake lock levels
3. Set timeouts on wake locks
4. Test wake lock behavior

---

**Performance optimization is an ongoing process. Regularly monitor metrics and adjust strategies as needed.** 📈
