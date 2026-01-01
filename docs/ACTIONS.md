# Action Development Guide

## Table of Contents

- [Overview](#overview)
- [Action Execution Flow](#action-execution-flow)
- [Complete Action Reference](#complete-action-reference)
  - [System Settings](#system-settings)
  - [Device Control](#device-control)
  - [Communication](#communication)
  - [App Control](#app-control)
  - [Media](#media)
  - [Notifications](#notifications)
  - [Automation](#automation)
  - [Logic Control](#logic-control)
- [Adding a New Action](#adding-a-new-action)
- [Variable Support](#variable-support)
- [Action Configuration](#action-configuration)
- [Action Chaining](#action-chaining)
- [Error Handling](#error-handling)
- [Permission Handling](#permission-handling)
- [Android Version Compatibility](#android-version-compatibility)
- [Performance Considerations](#performance-considerations)
- [Testing Actions](#testing-actions)
- [Best Practices](#best-practices)
- [Common Issues](#common-issues)

## Overview

Actions are operations that execute when a macro's trigger fires and constraints are satisfied. AutoDroid supports 35+ action types organized into categories.

Actions can:

- Change system settings (WiFi, Bluetooth, brightness)
- Control device (lock, vibrate, sleep)
- Communicate (SMS, email, calls, TTS)
- Control apps (launch, close, clear cache)
- Play media (sounds, wallpaper)
- Show notifications
- Store variables
- Implement logic (if/else, loops)

## Action Execution Flow

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Trigger Fires                                           │
│    Trigger provider detects event                          │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Constraints Evaluated                                   │
│    All constraints must be satisfied                    │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. ExecuteMacroUseCase Starts                             │
│    Loads macro with all actions                        │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. For Each Action (in execution order)                    │
│    a. Resolve variable placeholders                       │
│    b. Evaluate logic (if/else, loops)                  │
│    c. Execute action via ExecuteActionUseCase             │
│    d. Log result (success/failure)                       │
│    e. Apply delayAfter if configured                     │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Action Execution                                            │
│    a. ExecuteActionUseCase selects executor               │
│    b. Executor performs the operation                    │
│    c. Result returned to ExecuteMacroUseCase           │
└─────────────────────────────────────────────────────────────┘
```

## Complete Action Reference

### System Settings

#### WIFI_TOGGLE

Enable or disable WiFi.

**Configuration**:

```kotlin
mapOf("enabled" to true)  // true = enable, false = disable
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "WIFI_TOGGLE",
    actionConfig = mapOf("enabled" to true),
    executionOrder = 0
)
```

**Use Cases**:

- Enable WiFi at home
- Disable WiFi when leaving location
- Toggle WiFi for meetings

**Executor**: `WifiToggleExecutor`

---

#### BLUETOOTH_TOGGLE

Enable or disable Bluetooth.

**Configuration**:

```kotlin
mapOf("enabled" to true)  // true = enable, false = disable
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "BLUETOOTH_TOGGLE",
    actionConfig = mapOf("enabled" to true),
    executionOrder = 0
)
```

**Use Cases**:

- Enable Bluetooth when leaving home
- Disable Bluetooth during meetings
- Toggle for car mode

**Executor**: `BluetoothToggleExecutor`

---

#### SET_BRIGHTNESS

Set screen brightness level.

**Configuration**:

```kotlin
mapOf(
    "brightness" to 80,  // 0-100 (percentage)
    "auto" to false     // Optional: use auto-brightness
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "SET_BRIGHTNESS",
    actionConfig = mapOf("brightness" to 80),
    executionOrder = 0
)
```

**Use Cases**:

- Dim brightness at night
- Increase brightness in sunlight
- Adjust based on time of day

**Executor**: `SetBrightnessExecutor`

---

#### SET_SCREEN_TIMEOUT

Set screen timeout duration.

**Configuration**:

```kotlin
mapOf(
    "timeoutSeconds" to 300  // 15-3600 seconds
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "SET_SCREEN_TIMEOUT",
    actionConfig = mapOf("timeoutSeconds" to 300),
    executionOrder = 0
)
```

**Use Cases**:

- Longer timeout while reading
- Shorter timeout for battery saving
- Different timeouts based on app

---

#### TOGGLE_AIRPLANE_MODE

Enable or disable airplane mode.

**Configuration**:

```kotlin
mapOf("enabled" to true)  // true = enable, false = disable
```

**Use Cases**:

- Nighttime airplane mode
- Meeting airplane mode
- Emergency airplane mode

**Note**: Requires system-level permission on most devices.

---

#### TOGGLE_GPS

Enable or disable location services.

**Configuration**:

```kotlin
mapOf("enabled" to true)  // true = enable, false = disable
```

**Use Cases**:

- Disable GPS when at home
- Enable GPS when leaving home
- Toggle based on app usage

**Note**: Requires system-level permission on most devices.

---

#### VOLUME_CONTROL

Adjust volume for specific stream.

**Configuration**:

```kotlin
mapOf(
    "streamType" to "MUSIC",  // Options: MUSIC, NOTIFICATION, RING, ALARM
    "volume" to 50,            // 0-100 (percentage)
    "adjust" to "SET"          // Options: SET, UP, DOWN, MUTE, UNMUTE
    "step" to 10               // For UP/DOWN: step size (optional)
)
```

**Example**:

```kotlin
// Action 1: Set music volume
ActionDTO(
    actionType = "VOLUME_CONTROL",
    actionConfig = mapOf(
        "streamType" to "MUSIC",
        "volume" to 80
    ),
    executionOrder = 0
)

// Action 2: Lower ring volume
ActionDTO(
    actionType = "VOLUME_CONTROL",
    actionConfig = mapOf(
        "streamType" to "RING",
        "adjust" to "DOWN",
        "step" to 20
    ),
    executionOrder = 1
)
```

**Use Cases**:

- Set volume based on location
- Mute for meetings
- Adjust based on time of day

**Executor**: `VolumeControlExecutor`

---

### Device Control

#### LOCK_SCREEN

Lock the device screen.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "LOCK_SCREEN",
    actionConfig = mapOf(),
    executionOrder = 0
)
```

**Use Cases**:

- Auto-lock after tasks
- Security measure
- Privacy protection

**Note**: Requires `BIND_DEVICE_ADMIN` permission.

---

#### SLEEP_DEVICE

Turn off the device screen.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "SLEEP_DEVICE",
    actionConfig = mapOf(),
    executionOrder = 0
)
```

**Use Cases**:

- Nighttime sleep mode
- Automatic screen off
- Battery saving

**Note**: Requires system-level permission.

---

#### VIBRATE

Vibrate the device.

**Configuration**:

```kotlin
mapOf(
    "durationMs" to 500,        // Duration in milliseconds
    "pattern" to listOf(100, 100)  // Optional: vibration pattern
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "VIBRATE",
    actionConfig = mapOf(
        "durationMs" to 500,
        "pattern" to listOf(100, 100, 100)
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Notification vibrate
- Alarm replacement
- Confirmation feedback

**Executor**: `VibrateExecutor`

---

#### ENABLE_DO_NOT_DISTURB

Enable Do Not Disturb mode.

**Configuration**:

```kotlin
mapOf(
    "priority" to "ALARMS"  // Options: ALL, ALARMS, NONE
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "ENABLE_DO_NOT_DISTURB",
    actionConfig = mapOf("priority" to "ALARMS"),
    executionOrder = 0
)
```

**Use Cases**:

- Meeting mode
- Sleep mode
- Focus time

---

#### DISABLE_DO_NOT_DISTURB

Disable Do Not Disturb mode.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "DISABLE_DO_NOT_DISTURB",
    actionConfig = mapOf(),
    executionOrder = 0
)
```

**Use Cases**:

- End of meeting
- End of sleep mode
- End of focus time

---

### Communication

#### SEND_SMS

Send an SMS message.

**Configuration**:

```kotlin
mapOf(
    "phoneNumber" to "1234567890",
    "message" to "Hello from AutoDroid!"
)
```

**With Variables**:

```kotlin
mapOf(
    "phoneNumber" to "{emergencyContact}",
    "message" to "Location: {currentLocation}, Battery: {batteryLevel}%"
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "SEND_SMS",
    actionConfig = mapOf(
        "phoneNumber" to "1234567890",
        "message" to "I'm leaving work now"
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Send location to family
- Auto-reply messages
- Emergency notifications

**Executor**: `SendSmsExecutor`

**Permissions**: `SEND_SMS`

---

#### SEND_EMAIL

Open email composer with pre-filled content.

**Configuration**:

```kotlin
mapOf(
    "to" to "recipient@example.com",
    "subject" to "Auto-generated email",
    "body" to "Email body text"
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "SEND_EMAIL",
    actionConfig = mapOf(
        "to" to "boss@company.com",
        "subject" to "Leaving early today",
        "body" to "I have a dentist appointment."
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Quick email templates
- Send reports
- Communication shortcuts

---

#### MAKE_CALL

Dial a phone number.

**Configuration**:

```kotlin
mapOf(
    "phoneNumber" to "1234567890"
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "MAKE_CALL",
    actionConfig = mapOf("phoneNumber" to "1234567890"),
    executionOrder = 0
)
```

**Use Cases**:

- Emergency calls
- Quick dial shortcuts
- Auto-call family

**Permissions**: `CALL_PHONE`

---

#### SPEAK_TEXT

Use text-to-speech to read text aloud.

**Configuration**:

```kotlin
mapOf(
    "text" to "Hello! This is AutoDroid speaking.",
    "language" to "en-US",  // Optional: language code
    "pitch" to 1.0f,        // Optional: speech pitch (0.5-2.0)
    "speed" to 1.0f         // Optional: speech rate (0.5-2.0)
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "SPEAK_TEXT",
    actionConfig = mapOf(
        "text" to "Good morning! Time to start your day."
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Morning greeting
- Read notifications
- Voice feedback

---

### App Control

#### LAUNCH_APP

Launch an app by package name.

**Configuration**:

```kotlin
mapOf(
    "packageName" to "com.whatsapp",
    "activityName" to ".Main"  // Optional: specific activity
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "LAUNCH_APP",
    actionConfig = mapOf("packageName" to "com.whatsapp"),
    executionOrder = 0
)
```

**Use Cases**:

- Launch specific app on trigger
- App routines
- Quick app shortcuts

**Executor**: `LaunchAppExecutor`

---

#### CLOSE_APP

Force-stop an app.

**Configuration**:

```kotlin
mapOf(
    "packageName" to "com.example.app"
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "CLOSE_APP",
    actionConfig = mapOf("packageName" to "com.facebook.katana"),
    executionOrder = 0
)
```

**Use Cases**:

- Close apps when battery low
- Meeting mode (close distractions)
- Privacy protection

**Permissions**: `KILL_BACKGROUND_PROCESSES`

---

#### CLEAR_APP_CACHE

Clear an app's cache.

**Configuration**:

```kotlin
mapOf(
    "packageName" to "com.example.app"
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "CLEAR_APP_CACHE",
    actionConfig = mapOf("packageName" to "com.android.chrome"),
    executionOrder = 0
)
```

**Use Cases**:

- Regular cache cleanup
- Free up storage
- Performance optimization

---

#### OPEN_URL

Open a URL in browser.

**Configuration**:

```kotlin
mapOf(
    "url" to "https://example.com"
)
```

**With Variables**:

```kotlin
mapOf(
    "url" to "https://maps.google.com/?q={address}"
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "OPEN_URL",
    actionConfig = mapOf("url" to "https://example.com/dashboard"),
    executionOrder = 0
)
```

**Use Cases**:

- Open dashboards
- Quick links
- Web shortcuts

**Executor**: `OpenUrlExecutor`

---

### Media

#### PLAY_SOUND

Play a system sound or audio file.

**Configuration**:

```kotlin
mapOf(
    "soundType" to "SYSTEM",  // Options: SYSTEM, FILE, URI
    "soundName" to "NOTIFICATION_DEFAULT",  // For SYSTEM
    "filePath" to "/sdcard/sounds/alert.mp3",  // For FILE
    "uri" to "content://...",  // For URI
    "volume" to 1.0f  // Optional: 0.0-1.0
)
```

**System Sound Options**:

- `NOTIFICATION_DEFAULT`
- `ALARM_ALERT`
- `RINGTONE_DEFAULT`
- `MUSIC_DEFAULT`

**Example**:

```kotlin
// Action 1: Play system sound
ActionDTO(
    actionType = "PLAY_SOUND",
    actionConfig = mapOf(
        "soundType" to "SYSTEM",
        "soundName" to "NOTIFICATION_DEFAULT"
    ),
    executionOrder = 0
)

// Action 2: Play custom sound
ActionDTO(
    actionType = "PLAY_SOUND",
    actionConfig = mapOf(
        "soundType" to "FILE",
        "filePath" to "/sdcard/Music/alert.mp3",
        "volume" to 0.8f
    ),
    executionOrder = 1
)
```

**Use Cases**:

- Notification sounds
- Alarm replacement
- Audio feedback

**Executor**: `PlaySoundExecutor`

---

#### STOP_SOUND

Stop all media playback.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "STOP_SOUND",
    actionConfig = mapOf(),
    executionOrder = 0
)
```

**Use Cases**:

- Stop music when leaving car
- Meeting mode
- Privacy mode

**Executor**: `StopSoundExecutor`

---

#### CHANGE_WALLPAPER

Set the device wallpaper.

**Configuration**:

```kotlin
mapOf(
    "source" to "FILE",  // Options: FILE, URI
    "filePath" to "/sdcard/Pictures/wallpaper.jpg",  // For FILE
    "uri" to "content://...",  // For URI
    "homeScreen" to true,   // Set home screen wallpaper
    "lockScreen" to true    // Set lock screen wallpaper
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "CHANGE_WALLPAPER",
    actionConfig = mapOf(
        "source" to "FILE",
        "filePath" to "/sdcard/Pictures/morning.jpg"
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Daytime wallpaper
- Nighttime wallpaper
- Location-based wallpaper

**Permissions**: `SET_WALLPAPER`

---

### Notifications

#### NOTIFICATION

Show a notification.

**Configuration**:

```kotlin
mapOf(
    "title" to "Notification Title",
    "message" to "Notification message text",
    "channelId" to "automation_channel",  // Optional
    "icon" to "ic_launcher",  // Optional
    "autoCancel" to true,  // Optional
    "ongoing" to false,  // Optional
    "largeIcon" to "ic_large"  // Optional
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "NOTIFICATION",
    actionConfig = mapOf(
        "title" to "Automation Executed",
        "message" to "Your 'Home WiFi' macro ran successfully."
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Confirmation notifications
- Information display
- Reminders

**Executor**: `NotificationExecutor`

---

#### SHOW_TOAST

Show a temporary toast message.

**Configuration**:

```kotlin
mapOf(
    "message" to "Toast message",
    "duration" to "SHORT"  // Options: SHORT (2s), LONG (3.5s)
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "SHOW_TOAST",
    actionConfig = mapOf(
        "message" to "WiFi Enabled!",
        "duration" to "SHORT"
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Quick feedback
- Debugging
- Status updates

**Executor**: `ToastExecutor`

---

### Automation

#### DELAY

Wait a specified duration before next action.

**Configuration**:

```kotlin
mapOf(
    "durationMs" to 5000  // Duration in milliseconds
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "DELAY",
    actionConfig = mapOf("durationMs" to 5000),
    executionOrder = 0
)

// Action 2 (executes after 5 seconds)
ActionDTO(
    actionType = "LAUNCH_APP",
    actionConfig = mapOf("packageName" to "com.music.app"),
    executionOrder = 1
)
```

**Use Cases**:

- Wait between actions
- Allow time for operations
- Sequencing events

**Executor**: `DelayExecutor`

---

#### SET_VARIABLE

Store a value in a variable.

**Configuration**:

```kotlin
mapOf(
    "name" to "myVariable",
    "value" to "Hello World",
    "scope" to "LOCAL",  // Options: LOCAL, GLOBAL
    "operation" to "SET"  // Options: SET, ADD, SUBTRACT, MULTIPLY, DIVIDE, APPEND, SUBSTRING
)
```

**Operation Examples**:

**SET**:

```kotlin
mapOf("operation" to "SET", "name" to "counter", "value" to "0")
```

**ADD**:

```kotlin
mapOf("operation" to "ADD", "name" to "counter", "value" to "1")
```

**APPEND**:

```kotlin
mapOf("operation" to "APPEND", "name" to "message", "value" to " World")
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "operation" to "SET",
        "name" to "counter",
        "value" to "0",
        "scope" to "LOCAL"
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Store data between actions
- Build strings dynamically
- Maintain state

**See Also**: [Variables Guide](VARIABLES.md)

---

#### HTTP_REQUEST

Send an HTTP request.

**Configuration**:

```kotlin
mapOf(
    "url" to "https://api.example.com/endpoint",
    "method" to "GET",  // Options: GET, POST, PUT, DELETE
    "headers" to mapOf("Authorization" to "Bearer token"),
    "body" to "{\"key\":\"value\"}",  // For POST/PUT
    "timeout" to 30000  // Timeout in milliseconds
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "HTTP_REQUEST",
    actionConfig = mapOf(
        "url" to "https://api.example.com/log",
        "method" to "POST",
        "body" to "{\"event\":\"triggered\",\"location\":\"{currentLocation}\"}"
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Webhook triggers
- API integrations
- Home automation bridges

---

#### LOG_EVENT

Log an event to execution history.

**Configuration**:

```kotlin
mapOf(
    "message" to "Custom log message",
    "level" to "INFO"  // Options: DEBUG, INFO, WARN, ERROR
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "LOG_EVENT",
    actionConfig = mapOf(
        "message" to "Macro reached step 3",
        "level" to "DEBUG"
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Debugging macros
- Tracking execution
- Diagnostics

---

### Logic Control

#### IF_CONDITION

Conditional execution based on conditions.

**Configuration**:

```kotlin
mapOf(
    "condition" to mapOf(
        "leftOperand" to "{batteryLevel}",
        "operator" to "<",  // Options: ==, !=, >, <, >=, <=, CONTAINS
        "rightOperand" to "20"
    ),
    "trueActions" to listOf(/* ActionDTOs */),  // Actions if condition true
    "falseActions" to listOf(/* ActionDTOs */)  // Actions if condition false
)
```

**Example**:

```kotlin
// Action 1: If battery is low
ActionDTO(
    actionType = "IF_CONDITION",
    actionConfig = mapOf(
        "condition" to mapOf(
            "leftOperand" to "{batteryLevel}",
            "operator" to "<",
            "rightOperand" to "20"
        ),
        "trueActions" to listOf(
            ActionDTO(
                actionType = "SHOW_TOAST",
                actionConfig = mapOf("message" to "Low battery!"),
                executionOrder = 0
            )
        )
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Conditional actions
- Branching logic
- Smart responses

---

#### WHILE_LOOP

Repeat actions while condition is true.

**Configuration**:

```kotlin
mapOf(
    "condition" to mapOf(
        "leftOperand" to "{counter}",
        "operator" to "<",
        "rightOperand" to "10"
    ),
    "actions" to listOf(/* ActionDTOs */)  // Actions to repeat
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "WHILE_LOOP",
    actionConfig = mapOf(
        "condition" to mapOf(
            "leftOperand" to "{counter}",
            "operator" to "<",
            "rightOperand" to "5"
        ),
        "actions" to listOf(
            ActionDTO(
                actionType = "VIBRATE",
                actionConfig = mapOf("durationMs" to 200),
                executionOrder = 0
            ),
            ActionDTO(
                actionType = "SET_VARIABLE",
                actionConfig = mapOf(
                    "operation" to "ADD",
                    "name" to "counter",
                    "value" to "1"
                ),
                executionOrder = 1
            )
        )
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Repeat until condition met
- Dynamic iteration count
- Wait loops

---

#### FOR_LOOP

Repeat actions a specified number of times.

**Configuration**:

```kotlin
mapOf(
    "iterations" to 5,
    "variableName" to "index",  // Optional: variable for loop counter
    "startValue" to 0,  // Optional
    "actions" to listOf(/* ActionDTOs */)  // Actions to repeat
)
```

**Example**:

```kotlin
// Action 1
ActionDTO(
    actionType = "FOR_LOOP",
    actionConfig = mapOf(
        "iterations" to 3,
        "variableName" to "i",
        "startValue" to 0,
        "actions" to listOf(
            ActionDTO(
                actionType = "VIBRATE",
                actionConfig = mapOf("durationMs" to 200),
                executionOrder = 0
            )
        )
    ),
    executionOrder = 0
)
```

**Use Cases**:

- Fixed iteration count
- Repeat patterns
- Batch operations

---

#### BREAK

Exit current loop.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Example**:

```kotlin
// Inside a loop action
ActionDTO(
    actionType = "BREAK",
    actionConfig = mapOf(),
    executionOrder = 0
)
```

**Use Cases**:

- Early loop exit
- Conditional break
- Error handling

---

#### CONTINUE

Skip to next iteration of loop.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Example**:

```kotlin
// Inside a loop action
ActionDTO(
    actionType = "CONTINUE",
    actionConfig = mapOf(),
    executionOrder = 0
)
```

**Use Cases**:

- Skip specific iterations
- Conditional continue
- Filtering in loops

---

## Adding a New Action

### Step 1: Create Action Executor

Create a new file in `domain/usecase/executors/`:

```kotlin
package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyActionExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override val actionType: String = "MY_ACTION_TYPE"

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return try {
            // Get configuration values
            val param1 = config["param1"]?.toString() ?: ""
            val param2 = config["param2"]?.toString()?.toIntOrNull() ?: 0

            // Perform action
            performMyAction(param1, param2)

            Timber.i("Action MY_ACTION_TYPE executed successfully")
            Result.success(Unit)
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied for MY_ACTION_TYPE")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute MY_ACTION_TYPE")
            Result.failure(e)
        }
    }

    private fun performMyAction(param1: String, param2: Int) {
        // Implementation here
        // ...
    }
}
```

### Step 2: Register in ExecuteActionUseCase

In `domain/usecase/ExecuteActionUseCase.kt`:

```kotlin
// Add to executor set
@Inject Set<@JvmSuppressWildcards ActionExecutor> executors

// In execute() method, add to when statement:
"MY_ACTION_TYPE" -> executeWithType(executors, actionType, actionConfig)
```

### Step 3: Add to Action Picker

In `presentation/components/ActionPickerDialog.kt`:

```kotlin
ActionOption(
    name = "My Action",
    type = "MY_ACTION_TYPE",
    config = mapOf("param1" to "value1", "param2" to 42),
    category = "System Settings"  // Optional category
)
```

### Step 4: Add Permissions (if needed)

In `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.MY_PERMISSION" />
```

### Step 5: Write Tests

Create test file in `test/java/com/aditsyal/autodroid/domain/usecase/executors/`:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyActionExecutorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var executor: MyActionExecutor

    @Before
    fun setup() {
        executor = MyActionExecutor(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `execute returns success when action succeeds`() = runTest {
        // Given
        val config = mapOf("param1" to "test", "param2" to 42)

        // When
        val result = executor.execute(config)

        // Then
        assertTrue(result.isSuccess)
    }
}
```

## Variable Support

Actions support variable placeholders in their configuration:

### Variable Resolution

Variables are resolved before action execution:

```kotlin
// In action config
mapOf("message" to "Hello {userName}!")

// After variable resolution
mapOf("message" to "Hello John!")
```

### Variable Scopes

- **Local Variables**: `{variableName}` - Scoped to current macro execution
- **Global Variables**: `{globalVariableName}` - Persist across macro executions

### Complex Expressions

Variables can be used in expressions:

```kotlin
mapOf(
    "message" to "Counter is {counter}, battery is {batteryLevel}%"
)
```

### Variable Operations

Within actions, variables can be modified:

```kotlin
// Use SET_VARIABLE action before
ActionDTO(
    actionType = "SET_VARIABLE",
    actionConfig = mapOf(
        "operation" to "ADD",
        "name" to "counter",
        "value" to "1"
    ),
    executionOrder = 0
)

// Use variable in next action
ActionDTO(
    actionType = "SHOW_TOAST",
    actionConfig = mapOf("message" to "Count: {counter}"),
    executionOrder = 1
)
```

**See Also**: [Variables Guide](VARIABLES.md) for complete variable documentation.

## Action Configuration

Actions receive configuration via `actionConfig` map:

### ActionDTO Structure

```kotlin
data class ActionDTO(
    val id: Long = 0,
    val macroId: Long,
    val actionType: String,
    val actionConfig: Map<String, Any>,
    val executionOrder: Int,
    val delayAfter: Long = 0  // Delay after this action (ms)
)
```

### Config Pattern Examples

**Boolean Toggle**:

```kotlin
mapOf("enabled" to true)
```

**Numeric Value**:

```kotlin
mapOf("brightness" to 80)  // 0-100
mapOf("volume" to 50)  // 0-100
mapOf("durationMs" to 5000)
```

**String Value**:

```kotlin
mapOf("message" to "Hello World")
mapOf("url" to "https://example.com")
mapOf("packageName" to "com.whatsapp")
```

**List Value**:

```kotlin
mapOf("days" to listOf("MONDAY", "TUESDAY"))
mapOf("pattern" to listOf(100, 100, 100))
```

**Nested Config**:

```kotlin
mapOf(
    "condition" to mapOf(
        "leftOperand" to "{counter}",
        "operator" to ">",
        "rightOperand" to "10"
    )
)
```

**Config with Variables**:

```kotlin
mapOf(
    "message" to "Hello {userName}, your balance is {balance}",
    "url" to "https://maps.google.com/?q={address}"
)
```

## Action Chaining

Actions are executed in order based on `executionOrder` field:

### Sequential Execution

```kotlin
// Action 1: Execute first
ActionDTO(
    actionType = "WIFI_TOGGLE",
    actionConfig = mapOf("enabled" to true),
    executionOrder = 0
)

// Action 2: Execute after action 1
ActionDTO(
    actionType = "DELAY",
    actionConfig = mapOf("durationMs" to 2000),
    executionOrder = 1
)

// Action 3: Execute after action 2
ActionDTO(
    actionType = "LAUNCH_APP",
    actionConfig = mapOf("packageName" to "com.music.app"),
    executionOrder = 2
)
```

### Using Delays

```kotlin
// Action 1: Execute immediately
ActionDTO(
    actionType = "SHOW_TOAST",
    actionConfig = mapOf("message" to "Starting..."),
    executionOrder = 0,
    delayAfter = 0
)

// Action 2: Wait 2 seconds after action 1
ActionDTO(
    actionType = "WIFI_TOGGLE",
    actionConfig = mapOf("enabled" to true),
    executionOrder = 1,
    delayAfter = 2000
)

// Action 3: Wait 3 seconds after action 2
ActionDTO(
    actionType = "LAUNCH_APP",
    actionConfig = mapOf("packageName" to "com.app"),
    executionOrder = 2,
    delayAfter = 3000
)
```

### Logic-Based Chaining

```kotlin
// Action 1: Check condition
ActionDTO(
    actionType = "IF_CONDITION",
    actionConfig = mapOf(
        "condition" to mapOf(
            "leftOperand" to "{batteryLevel}",
            "operator" to "<",
            "rightOperand" to "20"
        ),
        "trueActions" to listOf(
            ActionDTO(
                actionType = "SHOW_TOAST",
                actionConfig = mapOf("message" to "Low battery!"),
                executionOrder = 0
            )
        ),
        "falseActions" to listOf(
            ActionDTO(
                actionType = "SHOW_TOAST",
                actionConfig = mapOf("message" to "Battery OK"),
                executionOrder = 0
            )
        )
    ),
    executionOrder = 0
)
```

## Error Handling

All actions should handle errors gracefully:

### Error Handling Pattern

```kotlin
override suspend fun execute(config: Map<String, Any>): Result<Unit> {
    return try {
        // Validate input
        val param = config["param"]?.toString()
        if (param == null) {
            return Result.failure(IllegalArgumentException("param is required"))
        }

        // Perform action
        performAction(param)

        // Return success
        Result.success(Unit)
    } catch (e: SecurityException) {
        // Permission denied - log but don't crash
        Timber.e(e, "Permission denied")
        Result.failure(e)
    } catch (e: IllegalArgumentException) {
        // Invalid input - log error
        Timber.e(e, "Invalid configuration")
        Result.failure(e)
    } catch (e: UnsupportedOperationException) {
        // API not available on this Android version
        Timber.w(e, "Action not supported on this device")
        Result.failure(e)
    } catch (e: Exception) {
        // Other errors - log and re-throw
        Timber.e(e, "Action failed")
        Result.failure(e)
    }
}
```

### Error Types

- **SecurityException**: Permission denied
- **IllegalArgumentException**: Invalid configuration
- **UnsupportedOperationException**: Feature not supported
- **Exception**: General error

### Logging

Use Timber for structured logging:

```kotlin
Timber.d("Action executed: $actionType")
Timber.i("Action completed successfully")
Timber.w("Action warning: $message")
Timber.e(exception, "Action failed")
```

## Permission Handling

Check permissions before executing sensitive actions:

### Permission Check Pattern

```kotlin
private fun hasPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) ==
        PackageManager.PERMISSION_GRANTED
}

override suspend fun execute(config: Map<String, Any>): Result<Unit> {
    if (!hasPermission(context, Manifest.permission.MY_PERMISSION)) {
        Timber.w("Permission not granted: MY_PERMISSION")
        return Result.failure(SecurityException("Permission required"))
    }

    // Perform action
    return performAction(config)
}
```

### Runtime Permission Requests

For dangerous permissions, request at runtime:

```kotlin
// In ViewModel or UI
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        // Permission granted, execute action
    } else {
        // Show explanation or error
    }
}

// Request permission
launcher.launch(Manifest.permission.MY_PERMISSION)
```

### AndroidManifest Permissions

Add to `AndroidManifest.xml`:

```xml
<!-- Permissions -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

## Android Version Compatibility

Handle API level differences:

### API Level Checking

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Use new API (Android 12+)
    notificationManager.createNotificationChannel(channel)
} else {
    // Use deprecated API or handle differently
}
```

### New vs Deprecated APIs

```kotlin
// Android 13+ (API 33+)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    setPerAppLanguage(context, locale)
} else {
    // Android 12 and below
    updateConfiguration(context, config)
}
```

### Feature Detection

```kotlin
// Check if feature is available
val hasTelephony = packageManager.hasSystemFeature(
    PackageManager.FEATURE_TELEPHONY
)

if (hasTelephony) {
    // Use phone features
}
```

### Version-Specific Actions

```kotlin
// For newer Android versions
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    // Use modern notification builder
    val notification = Notification.Builder(context, CHANNEL_ID)
        .build()
}
```

## Performance Considerations

### Efficient Execution

1. **Avoid Blocking**: Use coroutines for async operations
2. **Minimize System Calls**: Batch operations where possible
3. **Cache Results**: Cache values that don't change frequently
4. **Optimize Queries**: Use indexed database queries

### Battery Efficiency

1. **Short Actions**: Keep actions fast (under 1 second)
2. **Avoid Polling**: Use event-driven approaches
3. **Minimize Wake Locks**: Only use when necessary
4. **Background-Friendly**: Use WorkManager for background tasks

### Memory Management

1. **Clean Up Resources**: Close cursors, streams, etc.
2. **Avoid Leaks**: Use weak references where appropriate
3. **Limit Scope**: Keep objects scoped appropriately
4. **Use Lazy Initialization**: Initialize objects only when needed

## Testing Actions

### Unit Testing

```kotlin
@Test
fun `execute wifi toggle returns success`() = runTest {
    // Given
    val config = mapOf("enabled" to true)
    val executor = WifiToggleExecutor(context)

    // When
    val result = executor.execute(config)

    // Then
    assertTrue(result.isSuccess)
    verify(wifiManager).setWifiEnabled(true)
}
```

### Integration Testing

1. Create a test macro with your action
2. Enable the macro
3. Trigger the macro
4. Verify action executed correctly
5. Check execution logs

### Manual Testing

1. Create macro with your action
2. Set a simple trigger (e.g., time trigger)
3. Enable macro
4. Wait for trigger or manually test
5. Verify result:
   - Did action execute?
   - Was timing correct?
   - Were variables resolved?
   - Check logs for errors

## Best Practices

### 1. Validate Input

```kotlin
private fun validateConfig(config: Map<String, Any>): Result<Unit> {
    val requiredParams = listOf("param1", "param2")
    for (param in requiredParams) {
        if (!config.containsKey(param)) {
            return Result.failure(IllegalArgumentException("$param is required"))
        }
    }
    return Result.success(Unit)
}
```

### 2. Handle Nulls Safely

```kotlin
val param = config["param"]?.toString() ?: defaultValue
val number = config["number"]?.toString()?.toIntOrNull() ?: 0
```

### 3. Provide User Feedback

```kotlin
// Show notification for important actions
ActionDTO(
    actionType = "NOTIFICATION",
    actionConfig = mapOf(
        "title" to "Action Executed",
        "message" to "WiFi Enabled"
    ),
    executionOrder = 0
)
```

### 4. Log Actions

```kotlin
Timber.i("Executing action: $actionType with config: $config")
Timber.d("Action completed successfully in ${duration}ms")
```

### 5. Use Coroutines

```kotlin
override suspend fun execute(config: Map<String, Any>): Result<Unit> = withContext(Dispatchers.IO) {
    // Perform IO operation
    val result = performIOOperation(config)
    Result.success(result)
}
```

### 6. Write Tests

```kotlin
@Test
fun `action handles errors gracefully`() = runTest {
    // Test error scenarios
    val result = executor.execute(invalidConfig)
    assertFalse(result.isSuccess)
}
```

## Common Issues

### Action Not Executing

**Possible Causes**:

- Constraints not satisfied
- Macro disabled
- Previous action failed
- Execution order incorrect

**Solutions**:

1. Check execution logs for errors
2. Verify macro is enabled
3. Check constraints are met
4. Review execution order

### Permission Errors

**Possible Causes**:

- Permission not granted
- Permission revoked by user
- Android version restrictions

**Solutions**:

1. Request permissions before creating macro
2. Check AndroidManifest.xml
3. Handle SecurityException gracefully
4. Show user-friendly error messages

### Variable Not Resolved

**Possible Causes**:

- Variable name spelling
- Scope mismatch (LOCAL vs GLOBAL)
- Variable not set before use

**Solutions**:

1. Check variable name spelling
2. Verify variable scope
3. Set variable before using it
4. Check execution logs for resolution errors

### Performance Issues

**Possible Causes**:

- Action takes too long
- Blocking main thread
- Frequent system calls

**Solutions**:

1. Use coroutines for async operations
2. Minimize system API calls
3. Cache results where possible
4. Profile action execution

### Action Not Supported

**Possible Causes**:

- Feature not available on device
- Android version too old
- OEM restrictions

**Solutions**:

1. Check API level requirements
2. Handle UnsupportedOperationException
3. Provide fallback behavior
4. Document device limitations

## See Also

- **[Triggers Guide](TRIGGERS.md)**: Working with triggers
- **[Constraints Guide](CONSTRAINTS.md)**: Working with constraints
- **[Variables Guide](VARIABLES.md)**: Variable system and operations
- **[Architecture Documentation](ARCHITECTURE.md)**: Understanding the architecture
- **[Testing Guide](TESTING.md)**: Testing strategies
- **[Troubleshooting Guide](TROUBLESHOOTING.md)**: Common issues and solutions

---

**Ready to create powerful automations?** Start implementing your action! 🚀
