# Services Documentation

## Table of Contents

- [Overview](#overview)
- [Foreground Service](#foreground-service)
- [Accessibility Service](#accessibility-service)
- [WorkManager Worker](#workmanager-worker)
- [Broadcast Receivers](#broadcast-receivers)
- [Service Lifecycle](#service-lifecycle)
- [Inter-Service Communication](#inter-service-communication)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

## Overview

AutoDroid uses several Android services and background components to ensure reliable automation execution across different app states and device conditions.

### Services Overview

```
┌────────────────────────────────────────────────────────┐
│                   AutoDroid Services                │
│                                                         │
│  ┌─────────────┐    ┌──────────────┐   │
│  │ Foreground   │    │ Accessibility  │   │
│  │ Service      │    │ Service      │   │
│  └─────────────┘    └──────────────┘   │
│                         ↓                    │
│  ┌─────────────────────────────────────┐ │
│  │     Broadcast Receivers           │ │
│  │  • BootReceiver                │ │
│  │  • DeviceStateReceiver          │ │
│  │  • TriggerAlarmReceiver         │ │
│  │  • GeofenceBroadcastReceiver    │ │
│  └─────────────────────────────────────┘ │
│                         ↓                    │
│  ┌─────────────────────────────────────┐ │
│  │     WorkManager Workers            │ │
│  │  • MacroTriggerWorker           │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

## Foreground Service

**Location**: `app/src/main/java/com/aditsyal/autodroid/services/foreground/AutomationForegroundService.kt`

**Purpose**: Ensures reliable background execution of macros, prevents system from killing the app.

### Service Configuration

```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".services.foreground.AutomationForegroundService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="Automation monitoring service" />
</service>
```

**Permissions Required**:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
```

### Service Implementation

```kotlin
@HiltAndroidApp
class AutomationForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_ID = "automation_service"
        const val NOTIFICATION_ID = 1

        fun startService(context: Context) {
            val intent = Intent(context, AutomationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AutomationForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        Timber.i("ForegroundService started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle incoming commands
        when (intent?.action) {
            "ACTION_START_MONITORING" -> startMonitoring()
            "ACTION_STOP_MONITORING" -> stopMonitoring()
            else -> Timber.w("Unknown action: ${intent?.action}")
        }
        return START_STICKY  // Keep service running
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopForeground(true)
        Timber.i("ForegroundService stopped")
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoDroid")
            .setContentText("Automation service running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Automation Service",
                NotificationManagerCompat.IMPORTANCE_LOW
            ).apply {
                description = "Keeps AutoDroid running for reliable automation"
                enableLights(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startMonitoring() {
        Timber.i("Starting automation monitoring")
        // Start any ongoing monitoring tasks
    }

    private fun stopMonitoring() {
        Timber.i("Stopping automation monitoring")
        // Stop any ongoing monitoring tasks
    }
}
```

### Starting the Service

```kotlin
// From Application or ViewModel
fun startAutomationService(context: Context) {
    AutomationForegroundService.startService(context)
    Timber.d("AutomationForegroundService started")
}

// Stop when no macros are enabled
fun stopAutomationService(context: Context) {
    AutomationForegroundService.stopService(context)
    Timber.d("AutomationForegroundService stopped")
}
```

### Service States

1. **Created**: Service initialized, notification created
2. **Running**: Service is active in foreground
3. **Stopped**: Service is being destroyed
4. **Destroyed**: Service cleanup, notifications removed

## Accessibility Service

**Location**: `app/src/main/java/com/aditsyal/autodroid/services/accessibility/AutomationAccessibilityService.kt`

**Purpose**: Provides UI automation capabilities, detects app events, and performs UI interactions across apps.

### Service Configuration

```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".services.accessibility.AutomationAccessibilityService"
    android:enabled="false"
    android:exported="true"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>

    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

**Config File**:

```xml
<!-- res/xml/accessibility_service_config.xml -->
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android">
    <accessibility-event-type android:package android:eventTypes="all" />
    <accessibility-event-type android:eventTypes="all" />
    <accessibility-event-type android:notificationStateChanged="false" />
    <accessibility-event-type android:contentChangeTypes="all" />
    <feedback-type android:feedbackAudio="false" android:feedbackGeneric="false" />
    <canRetrieveWindowContent android:true" />
    <canPerformGestures android:true" />
</accessibility-service>
```

### Service Implementation

```kotlin
@HiltAndroidApp
class AutomationAccessibilityService : AccessibilityService() {

    private var callback: AccessibilityService.Callback? = null

    companion object {
        private var instance: AutomationAccessibilityService? = null

        fun getInstance(): AutomationAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Timber.i("AccessibilityService connected")

        callback = object : AccessibilityService.Callback() {
            override fun onAccessibilityEvent(event: AccessibilityEvent) {
                handleAccessibilityEvent(event)
            }

            override fun onInterrupt() {
                Timber.w("AccessibilityService interrupted")
            }
        }
        serviceInfo?.eventDispatcher?.addCallback(callback)
    }

    override fun onServiceDisconnected() {
        super.onServiceDisconnected()
        instance = null
        Timber.i("AccessibilityService disconnected")
        serviceInfo?.eventDispatcher?.removeCallback(callback)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> handleWindowChanged(event)
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> handleContentChanged(event)
            AccessibilityEvent.TYPE_GESTURE_DETECTION_START -> handleGesture(event)
            else -> handleOtherEvent(event)
        }
    }

    private fun handleWindowChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        Timber.d("Window changed: $packageName")

        // Notify TriggerManager about app launch/close
        TriggerManager.onAppEvent(
            type = if (event.contentChangeTypes?.contains(TYPE_WINDOWS_CHANGED)) {
                "APP_LAUNCHED"
            } else {
                "APP_CLOSED"
            },
            packageName = packageName
        )
    }

    private fun handleContentChanged(event: AccessibilityEvent) {
        val content = event.text?.toString()
        Timber.d("Content changed: $content")
        // Can be used for text-based automation
    }

    private fun handleGesture(event: AccessibilityEvent) {
        val gestureName = event.eventName
        Timber.d("Gesture detected: $gestureName")
        // Can trigger macros based on gestures
    }

    private fun performGesture(event: AccessibilityEvent): Boolean {
        // Perform action based on event
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun performClick(x: Int, y: Int): Boolean {
        val node = findNodeAtPosition(x, y)
        return node?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
    }
}
```

### Event Detection

The accessibility service detects various event types:

**Window Events**:

- App launched (`TYPE_WINDOW_STATE_CHANGED`)
- App closed (`TYPE_WINDOWS_CHANGED`)

**Content Events**:

- Text content changed
- Content scroll events

**Gesture Events**:

- Swipe gestures
- Tap events

**Focus Events**:

- Accessibility focus changes
- User interaction

### Accessibility Events for Triggers

```kotlin
// In TriggerManager
fun onAppEvent(packageName: String) {
    // Check for app launch/close triggers
    val matchingTriggers = triggerDao.getTriggersByType("APP_LAUNCHED")
    matchingTriggers.forEach { trigger ->
        if (shouldTrigger(trigger, packageName)) {
            checkTriggersUseCase(
                type = "APP_LAUNCHED",
                eventData = mapOf("packageName" to packageName)
            )
        }
    }
}
```

### Best Practices for Accessibility Service

1. **Disable when not needed**: Users can disable accessibility service
2. **Debounce events**: Prevent spamming the system (300ms debounce)
3. **Handle permissions gracefully**: Some actions may not be allowed
4. **Log events**: Use Timber for debugging
5. **Optimize node finding**: Cache root nodes for better performance
6. **Handle service interruptions**: Gracefully handle disconnections

## WorkManager Worker

**Location**: `app/src/main/java/com/aditsyal/autodroid/workers/MacroTriggerWorker.kt`

**Purpose**: Performs periodic background checks and executes macros at scheduled times when device is in Doze mode.

### Worker Configuration

```kotlin
@HiltWorker
class MacroTriggerWorker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase,
    private val executeMacroUseCase: ExecuteMacroUseCase
) : CoroutineWorker(context, workerParams, workerRunnerFactory) {

    companion object {
        const val WORK_NAME = "macro_trigger_worker"
        const val TAG = "MacroTriggerWorker"

        fun schedulePeriodicCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<MacroTriggerWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                workRequest
            )
        }
    }
}
```

### Worker Implementation

```kotlin
override suspend fun doWork(): Result {
    return try {
        Timber.i("MacroTriggerWorker executing")

        // 1. Check for pending time-based triggers
        val pendingTriggers = checkTriggersUseCase.getPendingTimeTriggers()

        // 2. Execute any due triggers
        pendingTriggers.forEach { trigger ->
            val shouldExecute = evaluateConstraints(trigger)
            if (shouldExecute) {
                executeMacroUseCase(trigger.macroId)
            }
        }

        Timber.i("MacroTriggerWorker completed")
        Result.success()
    } catch (e: Exception) {
        Timber.e(e, "MacroTriggerWorker failed")
        Result.failure(e)
    }
}

private fun evaluateConstraints(trigger: TriggerDTO): Boolean {
    // Check if all constraints are satisfied
    return true  // Simplified for example
}
```

### Scheduling the Worker

```kotlin
// From ViewModel or Application
fun scheduleTriggerWorker(context: Context) {
    MacroTriggerWorker.schedulePeriodicCheck(context)
    Timber.d("TriggerWorker scheduled")
}
```

### Worker Constraints

- **Network Required**: Only run when device has network
- **Battery Not Low**: Only run when battery is sufficient
- **Device Idle**: Only run when device is idle
- **Storage Not Low**: Only run when storage is available

## Broadcast Receivers

### BootReceiver

**Location**: `app/src/main/java/com/aditsyal/autodroid/receivers/BootReceiver.kt`

**Purpose**: Re-initializes all triggers when device boots up.

```kotlin
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var initializeTriggersUseCase: InitializeTriggersUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.i("Device booted, initializing triggers")

            // Re-initialize all triggers
            runBlocking {
                initializeTriggersUseCase()
            }

            // Start foreground service
            AutomationForegroundService.startService(context)
        }
    }
}
```

### DeviceStateReceiver

**Purpose**: Handles system state changes like low storage, airplane mode.

```kotlin
class DeviceStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_DEVICE_STORAGE_LOW -> handleStorageLow(context)
            Intent.ACTION_AIRPLANE_MODE_CHANGED -> handleAirplaneMode(context, intent)
            else -> Timber.d("Unhandled device state: ${intent.action}")
        }
    }

    private fun handleStorageLow(context: Context) {
        Timber.w("Device storage low")
        // Notify user, maybe disable some features
    }

    private fun handleAirplaneMode(context: Context, intent: Intent) {
        val isAirplaneModeOn = intent.getBooleanExtra("state", false)
        Timber.d("Airplane mode: ${if (isAirplaneModeOn) "ON" else "OFF"}")
        // Adjust behavior based on airplane mode
    }
}
```

### TriggerAlarmReceiver

**Purpose**: Receives and handles time-based trigger alarms.

```kotlin
class TriggerAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var checkTriggersUseCase: CheckTriggersUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val triggerId = intent.getLongExtra("trigger_id", -1L)
        val triggerType = intent.getStringExtra("trigger_type") ?: ""

        Timber.i("Alarm received: triggerId=$triggerId, type=$triggerType")

        // Check and execute triggers
        checkTriggersUseCase(
            type = triggerType,
            eventData = mapOf("fired_trigger_id" to triggerId)
        )
    }
}
```

### GeofenceBroadcastReceiver

**Purpose**: Receives geofence transition events from Play Services.

```kotlin
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var checkTriggersUseCase: CheckTriggersUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        Timber.i("Geofence event: ${geofencingEvent.transitionType}")

        // Check for matching location triggers
        checkTriggersUseCase(
            type = "LOCATION",
            eventData = mapOf(
                "transition_type" to geofencingEvent.transitionType,
                "location" to "${geofencingEvent.latitude},${geofencingEvent.longitude}"
            )
        )
    }
}
```

## Service Lifecycle

### Foreground Service Lifecycle

```
┌─────────────┐
│ onCreate()  │
│   ↓         │
│ Create channel│
│ Start foreground│
│ └───────────┘
      ↓
┌─────────────┐
│onCommand()  │
│   ↓         │
│Handle intent │
│Return START_STICKY│
│ └───────────┘
      ↓
┌─────────────┐
│ onDestroy()  │
│   ↓         │
│Stop foreground│
│Cancel coroutines│
│ └───────────┘
```

### Accessibility Service Lifecycle

```
┌─────────────────┐
│onServiceConnected()│
│   ↓            │
│Add callback    │
│Store instance   │
│ └──────────────┘
       ↓
┌─────────────────┐
│onAccessibilityEvent()│
│   ↓            │
│Handle event    │
│Notify triggers │
│ └──────────────┘
       ↓
┌─────────────────┐
│onServiceDisconnected()│
│   ↓            │
│Remove callback │
│Clear instance   │
│ └──────────────┘
```

### Worker Lifecycle

```
┌─────────────┐
│ doWork()   │
│   ↓        │
│Execute tasks│
│Return result│
│ └───────────┘
```

## Inter-Service Communication

### Accessing Foreground Service from App

```kotlin
// Check if service is running
fun isForegroundServiceRunning(context: Context): Boolean {
    val manager = ContextCompat.getSystemService(
        context, ActivityManager::class
    ) as ActivityManager

    manager.getRunningServices(Int.MAX_VALUE)?.any {
        it.service.className == AutomationForegroundService::class.java.name
    } ?: false
}
```

### Communicating with Accessibility Service

```kotlin
// From any part of app
val accessibilityService = AutomationAccessibilityService.getInstance()
accessibilityService?.performAction(AccessibilityService.GLOBAL_ACTION_BACK)
```

### Sending Commands to Services

```kotlin
// Send command to foreground service
fun sendCommandToForegroundService(context: Context, action: String) {
    val intent = Intent(context, AutomationForegroundService::class.java).apply {
        this.action = action
    }
    context.startService(intent)
}
```

## Best Practices

### 1. Service Lifecycle Management

```kotlin
// Good: Use proper lifecycle methods
override fun onCreate() {
    super.onCreate()
    // Initialize resources
}

override fun onDestroy() {
    super.onDestroy()
    // Clean up resources
    serviceScope.cancel()
}

// Bad: Don't hold references to destroyed services
```

### 2. Foreground Service Notifications

```kotlin
// Good: Provide user-friendly notification
val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setContentTitle("AutoDroid")
    .setContentText("Monitoring automation")
    .setSmallIcon(R.drawable.ic_notification)
    .setOngoing(true)
    .setPriority(NotificationCompat.PRIORITY_LOW)
    .build()

// Bad: No notification or user can't disable service
```

### 3. Accessibility Service Permissions

```kotlin
// Good: Check if service is enabled before using
if (AutomationAccessibilityService.getInstance() == null) {
    // Show prompt to enable accessibility service
    return
}

// Bad: Assume service is enabled
```

### 4. Worker Constraints

```kotlin
// Good: Use appropriate constraints
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .setRequiresDeviceIdle(false)
    .build()

// Bad: No constraints (runs even when offline)
```

### 5. Error Handling in Services

```kotlin
// Good: Wrap operations in try-catch
override fun onAccessibilityEvent(event: AccessibilityEvent) {
    try {
        processEvent(event)
    } catch (e: SecurityException) {
        Timber.e(e, "Permission denied for accessibility action")
    } catch (e: Exception) {
        Timber.e(e, "Failed to process accessibility event")
    }
}

// Bad: Let exceptions crash the service
```

### 6. Battery Optimization

```kotlin
// Good: Use WorkManager for background tasks
WorkManager.getInstance(context).enqueue(workerRequest)

// Bad: Use long-running services that get killed
```

### 7. Coroutines Management

```kotlin
// Good: Use CoroutineScope with proper cancellation
private val serviceScope = CoroutineScope(SupervisorJob())

override fun onDestroy() {
    serviceScope.cancel()  // Cancel all coroutines
}

// Bad: Use GlobalScope or don't cancel coroutines
```

### 8. Notification Channels (Android O+)

```kotlin
// Good: Create notification channel before showing notifications
private fun createNotificationChannel() {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Service Channel",
        NotificationManager.IMPORTANCE_LOW
    )
    notificationManager.createNotificationChannel(channel)
}

// Bad: Don't create channel (crash on Android O+)
```

## Troubleshooting

### Service Not Starting

**Possible Causes**:

- Service not exported properly
- Wrong service class name
- Missing permissions
- Service disabled

**Solutions**:

1. Check AndroidManifest.xml configuration
2. Verify service class exists
3. Check permissions are declared
4. Ensure service is enabled

### Foreground Service Killed by System

**Possible Causes**:

- Doze mode aggressive
- Battery optimization enabled
- Memory pressure
- System resource constraints

**Solutions**:

1. Request battery optimization exemption
2. Use WorkManager for critical tasks
3. Implement service restart on crash
4. Monitor service lifecycle

### Accessibility Service Not Enabled

**Possible Causes**:

- User disabled in system settings
- Service not properly configured
- Compatibility issues
- Previous crash

**Solutions**:

1. Guide user to enable service
2. Check accessibility service config
3. Verify service implementation
4. Check for compatibility issues

### Worker Not Executing

**Possible Causes**:

- Constraints not met
- Worker cancelled
- Duplicate work enqueued
- App killed during execution

**Solutions**:

1. Check worker constraints
2. Verify work is enqueued
3. Check WorkManager logs
4. Ensure unique work names

### Geofence Events Not Firing

**Possible Causes**:

- Location permission not granted
- Location services disabled
- Geofence too small
- GPS disabled

**Solutions**:

1. Grant location permissions
2. Enable location services
3. Increase geofence radius
4. Verify GPS is enabled

### Service Crashes

**Possible Causes**:

- Null pointer exceptions
- Missing dependencies
- Thread issues
- Resource leaks

**Solutions**:

1. Check Logcat for crash logs
2. Verify all dependencies are injected
3. Use proper coroutine dispatchers
4. Test service in isolation

### High Battery Usage

**Possible Causes**:

- Too frequent polling
- Inefficient event handling
- Not using proper background modes
- Sensor listeners always active

**Solutions**:

1. Increase polling intervals
2. Debounce events
3. Use efficient listeners
4. Unregister unused listeners
5. Use WorkManager for periodic tasks

### Notification Issues

**Symptoms**:

- Notifications not showing
- Notifications not updating
- Notifications can't be dismissed
- Wrong notification style

**Solutions**:

1. Check notification channel is created
2. Verify notification importance level
3. Test on different Android versions
4. Ensure notification ID is unique
5. Check notification permissions

## See Also

- **[Trigger Guide](TRIGGERS.md)**: How triggers work with services
- **[Architecture Documentation](ARCHITECTURE.md)**: System architecture
- **[Testing Guide](TESTING.md)**: Testing services
- **[Performance Documentation](PERFORMANCE.md)**: Performance optimization

---

**Happy service management!** ⚙️
