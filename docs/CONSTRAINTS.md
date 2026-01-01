# Constraint Development Guide

## Overview

Constraints are optional conditions that must be met for a macro to run, even when its trigger fires. They allow you to create sophisticated automations that only execute under specific conditions.

## Constraint Evaluation

Constraints are evaluated by `EvaluateConstraintsUseCase`:

```kotlin
suspend operator fun invoke(macroId: Long): Boolean
```

The use case:
1. Loads all constraints for the macro from database
2. Evaluates each constraint
3. Returns `true` if all constraints are satisfied, `false` otherwise

## Constraint Types

### Time Constraints

#### TIME_RANGE

Macro only runs during specific hours.

**Configuration**:
```kotlin
mapOf(
    "startTime" to "08:00",  // HH:mm format
    "endTime" to "18:00"
)
```

**Evaluation**:
```kotlin
val currentTime = LocalTime.now()
val startTime = LocalTime.parse(config["startTime"])
val endTime = LocalTime.parse(config["endTime"])
currentTime in startTime..endTime
```

**Use Cases**:
- Only run macro during work hours
- Execute actions only in the evening
- Daytime vs nighttime automations

#### DAY_OF_WEEK

Macro only runs on specific days.

**Configuration**:
```kotlin
mapOf(
    "days" to listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
)
```

**Evaluation**:
```kotlin
val currentDay = DayOfWeek.now()
val allowedDays = config["days"] as List<String>
currentDay.name in allowedDays
```

**Use Cases**:
- Weekday vs weekend automations
- Specific day routines
- Work week schedules

#### EXCLUDE_WEEKENDS

Macro only runs on weekdays (Monday-Friday).

**Configuration**:
```kotlin
mapOf()  // No additional config needed
```

**Evaluation**:
```kotlin
val dayOfWeek = DayOfWeek.now()
dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
```

**Use Cases**:
- Business hours only
- Workday routines
- Skip weekend notifications

#### SPECIFIC_DATE

Macro only runs on a specific date.

**Configuration**:
```kotlin
mapOf(
    "date" to "2024-12-25"  // yyyy-MM-dd format
)
```

**Evaluation**:
```kotlin
val currentDate = LocalDate.now()
val targetDate = LocalDate.parse(config["date"])
currentDate == targetDate
```

**Use Cases**:
- One-time events
- Holiday automations
- Special occasion routines

### Device State Constraints

#### BATTERY_LEVEL

Macro only runs when battery is within a range.

**Configuration**:
```kotlin
mapOf(
    "minLevel" to 20,  // Optional
    "maxLevel" to 80   // Optional
)
```

**Evaluation**:
```kotlin
val batteryLevel = getBatteryLevel()  // 0-100
val minLevel = config["minLevel"]?.toInt() ?: 0
val maxLevel = config["maxLevel"]?.toInt() ?: 100
batteryLevel in minLevel..maxLevel
```

**Use Cases**:
- Battery saver when low
- Power-intensive tasks when charging
- Different behaviors at different battery levels

#### CHARGING_STATUS

Macro only runs when charging or not charging.

**Configuration**:
```kotlin
mapOf(
    "charging" to true  // true = charging, false = not charging
)
```

**Evaluation**:
```kotlin
val isCharging = getChargingStatus()
val expectedCharging = config["charging"] as Boolean
isCharging == expectedCharging
```

**Use Cases**:
- Sync when charging
- Disable power-intensive features when not charging
- Backup when charging

#### SCREEN_STATE

Macro only runs when screen is on or off.

**Configuration**:
```kotlin
mapOf(
    "screenOn" to true  // true = screen on, false = screen off
)
```

**Evaluation**:
```kotlin
val isScreenOn = getScreenState()
val expectedState = config["screenOn"] as Boolean
isScreenOn == expectedState
```

**Use Cases**:
- Show notifications when screen on
- Background tasks when screen off
- Adjust settings based on screen state

#### DEVICE_LOCKED

Macro only runs when device is locked or unlocked.

**Configuration**:
```kotlin
mapOf(
    "locked" to true  // true = locked, false = unlocked
)
```

**Evaluation**:
```kotlin
val isLocked = getDeviceLockStatus()
val expectedLock = config["locked"] as Boolean
isLocked == expectedLock
```

**Use Cases**:
- Privacy features when locked
- Notifications when unlocked
- Security measures when locked

### Connectivity Constraints

#### WIFI_CONNECTED

Macro only runs when WiFi is connected (any network).

**Configuration**:
```kotlin
mapOf(
    "connected" to true  // true = connected, false = disconnected
)
```

**Evaluation**:
```kotlin
val wifiConnected = isWifiConnected()
val expected = config["connected"] as Boolean
wifiConnected == expected
```

**Use Cases**:
- Sync when on WiFi
- Update apps when on WiFi
- Download large files when on WiFi

#### WIFI_SSID

Macro only runs when connected to specific WiFi network.

**Configuration**:
```kotlin
mapOf(
    "ssid" to "HomeWiFi"  // WiFi network name
)
```

**Evaluation**:
```kotlin
val currentSsid = getCurrentWifiSsid()
val targetSsid = config["ssid"] as String
currentSsid == targetSsid
```

**Use Cases**:
- Home vs work automations
- Location-based settings via WiFi
- Network-specific actions

#### MOBILE_DATA_ACTIVE

Macro only runs when mobile data is on.

**Configuration**:
```kotlin
mapOf(
    "active" to true  // true = active, false = inactive
)
```

**Evaluation**:
```kotlin
val mobileDataActive = isMobileDataActive()
val expected = config["active"] as Boolean
mobileDataActive == expected
```

**Use Cases**:
- Avoid data-intensive tasks when not on WiFi
- Notify when mobile data is on
- Different behavior based on connection type

#### BLUETOOTH_CONNECTED

Macro only runs when Bluetooth is connected (any device).

**Configuration**:
```kotlin
mapOf(
    "connected" to true  // true = connected, false = disconnected
)
```

**Evaluation**:
```kotlin
val bluetoothConnected = isBluetoothConnected()
val expected = config["connected"] as Boolean
bluetoothConnected == expected
```

**Use Cases**:
- Car mode when Bluetooth connected
- Audio routing when headphones connected
- Privacy features when not connected

#### BLUETOOTH_DEVICE

Macro only runs when connected to specific Bluetooth device.

**Configuration**:
```kotlin
mapOf(
    "deviceName" to "Car Audio"
)
```

**Evaluation**:
```kotlin
val connectedDevice = getConnectedBluetoothDevice()
val targetDevice = config["deviceName"] as String
connectedDevice?.name == targetDevice
```

**Use Cases**:
- Car mode with specific car
- Home audio device
- Device-specific settings

### Location Constraints

#### INSIDE_GEOFENCE

Macro only runs when inside a specific geofence.

**Configuration**:
```kotlin
mapOf(
    "latitude" to 37.7749,
    "longitude" to -122.4194,
    "radius" to 100.0  // meters
)
```

**Evaluation**:
```kotlin
val currentLocation = getCurrentLocation()
val targetLocation = Location(config["latitude"], config["longitude"])
val radius = config["radius"] as Float
val distance = currentLocation.distanceTo(targetLocation)
distance <= radius
```

**Use Cases**:
- Home automations
- Work mode
- Location-based settings

#### OUTSIDE_GEOFENCE

Macro only runs when outside a specific geofence.

**Configuration**:
```kotlin
mapOf(
    "latitude" to 37.7749,
    "longitude" to -122.4194,
    "radius" to 100.0  // meters
)
```

**Evaluation**:
```kotlin
val currentLocation = getCurrentLocation()
val targetLocation = Location(config["latitude"], config["longitude"])
val radius = config["radius"] as Float
val distance = currentLocation.distanceTo(targetLocation)
distance > radius
```

**Use Cases**:
- Leave home notifications
- Away from work mode
- Exit location triggers

### Context Constraints

#### APP_RUNNING

Macro only runs when a specific app is running.

**Configuration**:
```kotlin
mapOf(
    "packageName" to "com.example.app"
)
```

**Evaluation**:
```kotlin
val runningApps = getRunningApps()
val targetPackage = config["packageName"] as String
runningApps.any { it.packageName == targetPackage }
```

**Use Cases**:
- App-specific settings
- App launch automations
- Context-aware actions

#### HEADPHONES_CONNECTED

Macro only runs when headphones are connected.

**Configuration**:
```kotlin
mapOf(
    "connected" to true  // true = connected, false = disconnected
)
```

**Evaluation**:
```kotlin
val headphonesConnected = areHeadphonesConnected()
val expected = config["connected"] as Boolean
headphonesConnected == expected
```

**Use Cases**:
- Audio routing
- Media playback control
- Different behavior with/without headphones

#### DO_NOT_DISTURB_ENABLED

Macro only runs when DND is enabled.

**Configuration**:
```kotlin
mapOf(
    "enabled" to true  // true = enabled, false = disabled
)
```

**Evaluation**:
```kotlin
val dndEnabled = isDoNotDisturbEnabled()
val expected = config["enabled"] as Boolean
dndEnabled == expected
```

**Use Cases**:
- Silent mode automations
- Priority mode features
- Different behavior based on DND

## Adding a New Constraint Type

### Step 1: Add to Constraint DTO

In `data/models/ConstraintDTO.kt`:

```kotlin
data class ConstraintDTO(
    val id: Long = 0,
    val macroId: Long,
    val type: String,
    val config: Map<String, Any>
)
```

### Step 2: Implement Evaluation Logic

In `domain/usecase/EvaluateConstraintsUseCase.kt`, add to `when` statement:

```kotlin
"MY_CONSTRAINT_TYPE" -> evaluateMyConstraint(config)
```

Add evaluation method:

```kotlin
private suspend fun evaluateMyConstraint(config: Map<String, Any>): Boolean {
    return try {
        // Get required values from config
        val param1 = config["param1"]?.toString()
        val param2 = config["param2"]?.toString()?.toIntOrNull()

        // Perform evaluation
        // Return true if constraint satisfied, false otherwise
        val result = // ... your evaluation logic

        Timber.d("Constraint MY_CONSTRAINT_TYPE evaluated: $result")
        result
    } catch (e: Exception) {
        Timber.e(e, "Failed to evaluate MY_CONSTRAINT_TYPE")
        false  // Return false on error (constraint not satisfied)
    }
}
```

### Step 3: Add to Constraint Picker

In `presentation/components/ConstraintPickerDialog.kt`:

```kotlin
ConstraintOption(
    name = "My Constraint",
    type = "MY_CONSTRAINT_TYPE",
    config = mapOf("param1" to "value1", "param2" to 42)
)
```

### Step 4: Add Permissions (if needed)

In `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.MY_PERMISSION" />
```

## Constraint Configuration Examples

### Single Constraint

```kotlin
ConstraintDTO(
    id = 1,
    macroId = 1,
    type = "TIME_RANGE",
    config = mapOf(
        "startTime" to "09:00",
        "endTime" to "17:00"
    )
)
```

### Multiple Constraints (AND logic)

```kotlin
val constraints = listOf(
    ConstraintDTO(
        type = "DAY_OF_WEEK",
        config = mapOf("days" to listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"))
    ),
    ConstraintDTO(
        type = "TIME_RANGE",
        config = mapOf("startTime" to "09:00", "endTime" to "17:00")
    ),
    ConstraintDTO(
        type = "WIFI_SSID",
        config = mapOf("ssid" to "OfficeWiFi")
    )
)
// All must be satisfied for macro to run
```

## Best Practices

### Performance

1. **Evaluate Efficiently**: Use efficient checks, avoid heavy computations
2. **Cache When Possible**: Cache values that don't change frequently
3. **Use Broadcast Receivers**: Listen for state changes instead of polling
4. **Minimize System Calls**: Avoid unnecessary system API calls

### User Experience

1. **Clear Messaging**: Explain constraint behavior in UI
2. **Validation**: Validate constraint configuration before saving
3. **Default Values**: Provide sensible defaults
4. **Examples**: Show example configurations

### Error Handling

1. **Graceful Degradation**: Return false on errors (don't crash)
2. **Logging**: Log constraint evaluation failures
3. **User Feedback**: Show constraint evaluation status in execution logs
4. **Fallback Values**: Use fallback values when config is missing

## Testing Constraints

### Unit Testing

```kotlin
@Test
fun `evaluate time range constraint returns true when current time is within range`() = runTest {
    // Given
    val config = mapOf("startTime" to "09:00", "endTime" to "17:00")
    val useCase = EvaluateConstraintsUseCase(/* dependencies */)

    // When
    val result = useCase.evaluateTimeRangeConstraint(config)

    // Then
    assertTrue(result)
}
```

### Integration Testing

Create a test macro with constraints:
1. Add trigger (e.g., time trigger)
2. Add constraints
3. Enable macro
4. Wait for trigger
5. Verify macro executes only when constraints are satisfied

### Manual Testing

1. Create macro with constraints
2. Manually trigger macro
3. Check execution history
4. Verify constraint evaluation
5. Test edge cases (boundary values, etc.)

## Common Issues

### Constraint Always Fails

**Possible Causes**:
- Configuration incorrect
- Permission not granted
- System API returning unexpected values
- Time zone mismatch (for time constraints)

**Solutions**:
1. Check constraint configuration
2. Verify permissions granted
3. Check logs for evaluation results
4. Test with simpler constraints first

### Constraint Always Passes

**Possible Causes**:
- Evaluation logic incorrect
- Config values not being read correctly
- Using wrong comparison operator

**Solutions**:
1. Review evaluation logic
2. Add logging to verify values
3. Test with known failing values

### Performance Issues

**Possible Causes**:
- Too many constraints on a macro
- Expensive constraint evaluations
- Polling instead of event-driven evaluation

**Solutions**:
1. Reduce number of constraints
2. Optimize evaluation logic
3. Use event-driven approach where possible
4. Cache results when appropriate

## Advanced Features

### Dynamic Constraints

Constraints that change based on other factors:

```kotlin
// Example: Battery level constraint with dynamic threshold
val threshold = getAverageBatteryUsage() * 2
val batteryLevel = getCurrentBatteryLevel()
batteryLevel > threshold
```

### Conditional Constraints

Constraints that only apply under certain conditions:

```kotlin
// Only check WiFi constraint if mobile data is off
if (!isMobileDataActive()) {
    return isWifiConnected()
}
return true
```

### Constraint Groups

Organize constraints into groups for complex logic:

```kotlin
data class ConstraintGroup(
    val logic: Logic,  // AND or OR
    val constraints: List<ConstraintDTO>
)
```

## Integration with Triggers

Constraints work with all trigger types:

```kotlin
// Example: Time trigger + constraint
Trigger: Specific time (08:00)
Constraint: Day of week (Monday-Friday)
Result: Macro runs at 8:00 AM on weekdays only

// Example: Location trigger + constraint
Trigger: Enter geofence (home)
Constraint: Time range (17:00-23:00)
Result: Macro runs when entering home between 5 PM and 11 PM

// Example: Sensor trigger + constraint
Trigger: Shake device
Constraint: Device unlocked
Result: Macro runs when shaking device, only if unlocked
```

## Documentation

When adding new constraint types, document:

1. **Purpose**: What does this constraint do?
2. **Configuration**: What parameters does it accept?
3. **Use Cases**: When should this be used?
4. **Examples**: Provide example configurations
5. **Performance Notes**: Any performance considerations?
6. **Limitations**: Any known limitations?

## See Also

- [Trigger Development Guide](TRIGGERS.md): Working with triggers
- [Action Development Guide](ACTIONS.md): Working with actions
- [Architecture Documentation](ARCHITECTURE.md): Understanding the architecture
- [Testing Guide](TESTING.md): Testing constraints
