# Trigger Development Guide

## Table of Contents

- [Overview](#overview)
- [Trigger Provider Interface](#trigger-provider-interface)
- [Trigger Registration Flow](#trigger-registration-flow)
- [Complete Trigger Reference](#complete-trigger-reference)
  - [Time-Based Triggers](#time-based-triggers)
  - [Location-Based Triggers](#location-based-triggers)
  - [Sensor-Based Triggers](#sensor-based-triggers)
  - [Device State Triggers](#device-state-triggers)
  - [Connectivity Triggers](#connectivity-triggers)
- [App Event Triggers](#app-event-triggers)
- [Communication Triggers](#communication-triggers)
- [Calendar Triggers](#calendar-triggers)
- [Audio Profile Triggers](#audio-profile-triggers)
- [Device Lock Triggers](#device-lock-triggers)
- [Adding a New Trigger](#adding-a-new-trigger)

- [Trigger Event Data](#trigger-event-data)
- [Performance Considerations](#performance-considerations)
- [Testing Triggers](#testing-triggers)
- [Best Practices](#best-practices)
- [Common Issues](#common-issues)

## Overview

Triggers are events that cause a macro to execute. AutoDroid supports 30+ trigger types organized into categories.

Triggers can:

- Detect specific times or intervals
- Monitor device location (geofencing)
- Respond to sensor changes (shake, proximity, light)
- React to device state changes (screen, charging, battery)
- Monitor connectivity (WiFi, Bluetooth, mobile data)
- Track app lifecycle (launch, close, install)
- Respond to communication events (calls, SMS)

## Trigger Provider Interface

All trigger providers implement the `TriggerProvider` interface:

```kotlin
interface TriggerProvider {
    /**
     * Unique identifier for this trigger type
     */
    val type: String

    /**
     * Register a new trigger to listen for events
     */
    suspend fun registerTrigger(trigger: TriggerDTO)

    /**
     * Unregister a specific trigger
     */
    suspend fun unregisterTrigger(triggerId: Long)

    /**
     * Unregister all triggers and clean up resources
     */
    suspend fun clearTriggers()
}
```

## Trigger Registration Flow

```
┌─────────────────────────────────────────────────────────────┐
│ 1. User Creates Macro                                       │
│    Macro saved to database via repository                   │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. InitializeTriggersUseCase                               │
│    Loads all enabled macros from database              │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. TriggerManager                                         │
│    Receives all triggers from InitializeTriggersUseCase      │
│    Distributes to appropriate TriggerProviders            │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Register with Provider                                  │
│    Each trigger registered with appropriate           │
│    TriggerProvider (TimeTriggerProvider, etc.)          │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Provider Listens                                       │
│    Provider sets up listeners:                             │
│    - BroadcastReceiver (for system events)                │
│    - SensorManager (for sensor events)                     │
│    - AlarmManager (for time events)                        │
│    - LocationManager (for geofence events)                 │
│    - AccessibilityService (for app events)                 │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. Event Detected                                        │
│    Provider detects event and calls                     │
│    CheckTriggersUseCase with event data                    │
└─────────────────────────────────────────────────────────────┘
```

## Complete Trigger Reference

### Time-Based Triggers

**Provider**: `TimeTriggerProvider`

#### TIME

Trigger at a specific time of day.

**Configuration**:

```kotlin
mapOf(
    "time" to "08:00"  // HH:mm format (24-hour)
)
```

**Examples**:

```kotlin
// Morning routine at 7:00 AM
TriggerDTO(
    triggerType = "TIME",
    triggerConfig = mapOf("time" to "07:00")
)

// Evening routine at 6:00 PM
TriggerDTO(
    triggerType = "TIME",
    triggerConfig = mapOf("time" to "18:00")
)
```

**Use Cases**:

- Morning routines (enable WiFi, launch apps)
- Evening routines (dim screen, enable DND)
- Specific time actions

**Implementation**: Uses `AlarmManager` with `setExactAndAllowWhileIdle()`

---

#### TIME_INTERVAL

Trigger at regular intervals.

**Configuration**:

```kotlin
mapOf(
    "intervalMinutes" to 30,  // Interval in minutes
    "intervalHours" to 0        // Interval in hours (optional)
)
```

**Examples**:

```kotlin
// Every 30 minutes
TriggerDTO(
    triggerType = "TIME_INTERVAL",
    triggerConfig = mapOf("intervalMinutes" to 30)
)

// Every 2 hours
TriggerDTO(
    triggerType = "TIME_INTERVAL",
    triggerConfig = mapOf("intervalHours" to 2)
)

// Every 1 hour 30 minutes (90 minutes)
TriggerDTO(
    triggerType = "TIME_INTERVAL",
    triggerConfig = mapOf("intervalMinutes" to 90)
)
```

**Use Cases**:

- Regular data sync
- Periodic status checks
- Repeating actions

**Implementation**: Uses `AlarmManager` with `setInexactRepeating()`

---

#### DAY_OF_WEEK

Trigger on specific days of the week.

**Configuration**:

```kotlin
mapOf(
    "time" to "09:00",  // Optional: specific time
    "days" to listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
)
```

**Day Values**:

- `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`
- `SATURDAY`, `SUNDAY`
- Or use `WEEKDAY`, `WEEKEND`

**Examples**:

```kotlin
// Every weekday at 9:00 AM
TriggerDTO(
    triggerType = "DAY_OF_WEEK",
    triggerConfig = mapOf(
        "time" to "09:00",
        "days" to listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
    )
)

// Every weekend at 10:00 AM
TriggerDTO(
    triggerType = "DAY_OF_WEEK",
    triggerConfig = mapOf(
        "time" to "10:00",
        "days" to listOf("SATURDAY", "SUNDAY")
    )
)
```

**Use Cases**:

- Workday automations
- Weekend routines
- Different behaviors for different days

---

### Location-Based Triggers

**Provider**: `LocationTriggerProvider`

#### LOCATION (Geofence)

Trigger when entering or leaving a specific location.

**Configuration**:

```kotlin
mapOf(
    "latitude" to 37.7749,
    "longitude" to -122.4194,
    "radius" to 100.0,           // Radius in meters
    "transitionType" to "ENTER",  // "ENTER" or "EXIT"
    "dwellTime" to 30000         // Optional: dwell time in ms
)
```

**Transition Types**:

- `ENTER`: Trigger when entering geofence
- `EXIT`: Trigger when leaving geofence
- `DWELL`: Trigger after staying inside for X ms

**Examples**:

```kotlin
// When entering home geofence (radius 100m)
TriggerDTO(
    triggerType = "LOCATION",
    triggerConfig = mapOf(
        "latitude" to 37.7749,
        "longitude" to -122.4194,
        "radius" to 100.0,
        "transitionType" to "ENTER"
    )
)

// When leaving work geofence (radius 50m)
TriggerDTO(
    triggerType = "LOCATION",
    triggerConfig = mapOf(
        "latitude" to 37.7849,
        "longitude" to -122.4094,
        "radius" to 50.0,
        "transitionType" to "EXIT"
    )
)
```

**Use Cases**:

- Enable WiFi when arriving home
- Disable Bluetooth when leaving home
- Work mode when at office

**Implementation**: Uses `GeofencingClient` from Play Services Location
**Permissions**: `ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`

**Performance Notes**:

- Larger radius = fewer location updates = better battery
- Smaller radius = more precise = more battery usage

---

### Sensor-Based Triggers

**Provider**: `SensorTriggerProvider`

#### SHAKE

Trigger when device is shaken.

**Configuration**:

```kotlin
mapOf(
    "threshold" to 15.0,     // Acceleration threshold (m/s²)
    "duration" to 500,        // Shake duration (ms)
    "delay" to 1000           // Delay between shakes (ms)
)
```

**Examples**:

```kotlin
// Detect medium shake
TriggerDTO(
    triggerType = "SHAKE",
    triggerConfig = mapOf(
        "threshold" to 15.0,
        "duration" to 500,
        "delay" to 1000
    )
)

// Detect light shake
TriggerDTO(
    triggerType = "SHAKE",
    triggerConfig = mapOf(
        "threshold" to 10.0,
        "duration" to 200,
        "delay" to 500
    )
)
```

**Use Cases**:

- Flashlight toggle
- Quick action trigger
- Gesture-based commands

**Implementation**: Uses `SensorManager` with `TYPE_ACCELEROMETER`

---

#### PROXIMITY

Trigger when proximity sensor detects object close to device.

**Configuration**:

```kotlin
mapOf(
    "threshold" to 5.0  // Distance in cm
)
```

**Examples**:

```kotlin
// Trigger when object within 5cm
TriggerDTO(
    triggerType = "PROXIMITY",
    triggerConfig = mapOf("threshold" to 5.0)
)

// Trigger when object within 2cm
TriggerDTO(
    triggerType = "PROXIMITY",
    triggerConfig = mapOf("threshold" to 2.0)
)
```

**Use Cases**:

- Pocket detection
- Face detection
- Smart actions based on device position

**Implementation**: Uses `SensorManager` with `TYPE_PROXIMITY`

---

#### LIGHT_LEVEL

Trigger when ambient light level reaches threshold.

**Configuration**:

```kotlin
mapOf(
    "threshold" to 50,      // Lux value (0-65535)
    "operator" to "GREATER_THAN",  // "GREATER_THAN", "LESS_THAN", "EQUAL"
    "hysteresis" to 5.0     // Optional: hysteresis value
)
```

**Examples**:

```kotlin
// When light exceeds 50 lux
TriggerDTO(
    triggerType = "LIGHT_LEVEL",
    triggerConfig = mapOf(
        "threshold" to 50,
        "operator" to "GREATER_THAN"
    )
)

// When light drops below 10 lux (dark)
TriggerDTO(
    triggerType = "LIGHT_LEVEL",
    triggerConfig = mapOf(
        "threshold" to 10,
        "operator" to "LESS_THAN"
    )
)
```

**Use Cases**:

- Auto-brightness control
- Night mode detection
- Reading mode detection

**Implementation**: Uses `SensorManager` with `TYPE_LIGHT`

---

#### ORIENTATION_CHANGE

Trigger when device orientation changes.

**Configuration**:

```kotlin
mapOf(
    "orientation" to "PORTRAIT"  // Optional: filter specific orientation
)
```

**Orientation Values**:

- `PORTRAIT`: Upright
- `LANDSCAPE`: Sideways
- `REVERSE_PORTRAIT`: Upside down
- `REVERSE_LANDSCAPE`: Other way sideways
- Or omit to trigger on any change

**Examples**:

```kotlin
// Any orientation change
TriggerDTO(
    triggerType = "ORIENTATION_CHANGE",
    triggerConfig = mapOf()
)

// Only when portrait
TriggerDTO(
    triggerType = "ORIENTATION_CHANGE",
    triggerConfig = mapOf("orientation" to "PORTRAIT")
)
```

**Use Cases**:

- Auto-rotate actions
- Media control based on orientation
- Gesture detection

**Implementation**: Uses `SensorManager` with `TYPE_ORIENTATION`

---

### Device State Triggers

**Provider**: `DeviceStateTriggerProvider`

#### SCREEN_ON

Trigger when screen turns on.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "SCREEN_ON",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Show notifications when using phone
- Enable settings when screen on
- Start specific actions on wake

**Implementation**: Uses `BroadcastReceiver` for `ACTION_SCREEN_ON`

---

#### SCREEN_OFF

Trigger when screen turns off.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "SCREEN_OFF",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Disable services when screen off
- Enable battery saving
- Start background tasks

**Implementation**: Uses `BroadcastReceiver` for `ACTION_SCREEN_OFF`

---

#### DEVICE_LOCKED

Trigger when device is locked.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "DEVICE_LOCKED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Privacy mode
- Disable sensitive features
- Security measures

**Implementation**: Uses `KeyguardManager`

---

#### DEVICE_UNLOCKED

Trigger when device is unlocked.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "DEVICE_UNLOCKED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Restore settings
- Enable features
- Personalized experience

**Implementation**: Uses `KeyguardManager`

---

#### CHARGING_CONNECTED

Trigger when charger is connected.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "CHARGING_CONNECTED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Enable power-intensive tasks
- Sync data
- Update apps

**Implementation**: Uses `BroadcastReceiver` for `ACTION_POWER_CONNECTED`

---

#### CHARGING_DISCONNECTED

Trigger when charger is disconnected.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "CHARGING_DISCONNECTED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Enable battery saving
- Disable power-intensive features
- Show battery warning

**Implementation**: Uses `BroadcastReceiver` for `ACTION_POWER_DISCONNECTED`

---

#### BATTERY_LEVEL

Trigger when battery reaches specific level.

**Configuration**:

```kotlin
mapOf(
    "level" to 20,           // Battery level (0-100)
    "operator" to "LESS_THAN",  // "EQUAL", "LESS_THAN", "GREATER_THAN"
    "hysteresis" to 5       // Optional: hysteresis value to prevent rapid re-triggering
)
```

**Examples**:

```kotlin
// When battery drops below 20%
TriggerDTO(
    triggerType = "BATTERY_LEVEL",
    triggerConfig = mapOf(
        "level" to 20,
        "operator" to "LESS_THAN"
    )
)

// When battery reaches 80%
TriggerDTO(
    triggerType = "BATTERY_LEVEL",
    triggerConfig = mapOf(
        "level" to 80,
        "operator" to "EQUAL"
    )
)
```

**Use Cases**:

- Low battery warnings
- Enable battery saver
- Charge notifications

**Implementation**: Uses `BroadcastReceiver` for `ACTION_BATTERY_CHANGED`

---

### Connectivity Triggers

**Provider**: `ConnectivityTriggerProvider`

#### WIFI_CONNECTED

Trigger when WiFi is connected.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "WIFI_CONNECTED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Sync when on WiFi
- Update apps
- Enable features

**Implementation**: Uses `BroadcastReceiver` for `NETWORK_STATE_CHANGED_ACTION`

---

#### WIFI_DISCONNECTED

Trigger when WiFi is disconnected.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "WIFI_DISCONNECTED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Switch to mobile data
- Show WiFi disconnected alert
- Disable WiFi-specific features

**Implementation**: Uses `BroadcastReceiver` for `NETWORK_STATE_CHANGED_ACTION`

---

#### WIFI_SSID_CONNECTED

Trigger when connected to specific WiFi network.

**Configuration**:

```kotlin
mapOf(
    "ssid" to "HomeWiFi"  // WiFi network name
)
```

**Examples**:

```kotlin
// When connected to home WiFi
TriggerDTO(
    triggerType = "WIFI_SSID_CONNECTED",
    triggerConfig = mapOf("ssid" to "HomeWiFi")
)

// When connected to office WiFi
TriggerDTO(
    triggerType = "WIFI_SSID_CONNECTED",
    triggerConfig = mapOf("ssid" to "OfficeWiFi")
)
```

**Use Cases**:

- Location-based settings via WiFi
- Work mode automation
- Home mode automation

**Implementation**: Uses `WifiManager`

---

#### BLUETOOTH_CONNECTED

Trigger when Bluetooth is connected.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "BLUETOOTH_CONNECTED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Car mode
- Headphone detection
- Audio routing

**Implementation**: Uses `BroadcastReceiver` for `ACTION_CONNECTION_STATE_CHANGED`

**Permissions**: `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`

---

#### BLUETOOTH_DISCONNECTED

Trigger when Bluetooth is disconnected.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "BLUETOOTH_DISCONNECTED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Exit car mode
- Disable Bluetooth features
- Switch audio output

**Implementation**: Uses `BroadcastReceiver` for `ACTION_CONNECTION_STATE_CHANGED`

---

#### BLUETOOTH_DEVICE_CONNECTED

Trigger when specific Bluetooth device connects.

**Configuration**:

```kotlin
mapOf(
    "deviceName" to "Car Audio"  // Device name (exact or partial match)
)
```

**Examples**:

```kotlin
// When car audio connects
TriggerDTO(
    triggerType = "BLUETOOTH_DEVICE_CONNECTED",
    triggerConfig = mapOf("deviceName" to "Car Audio")
)

// When headphones connect
TriggerDTO(
    triggerType = "BLUETOOTH_DEVICE_CONNECTED",
    triggerConfig = mapOf("deviceName" to "Sony WH-1000XM2")
)
```

**Use Cases**:

- Car mode
- Headphone mode
- Device-specific settings

**Implementation**: Uses `BluetoothAdapter`

---

#### MOBILE_DATA_ENABLED

Trigger when mobile data is enabled.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "MOBILE_DATA_ENABLED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Show mobile data on warning
- Sync when mobile data on
- Different behavior on mobile data

**Implementation**: Uses `ConnectivityManager`

**Permissions**: `ACCESS_NETWORK_STATE`, `CHANGE_NETWORK_STATE`

---

#### MOBILE_DATA_DISABLED

Trigger when mobile data is disabled.

**Configuration**:

```kotlin
mapOf()  // No configuration needed
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "MOBILE_DATA_DISABLED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Show mobile data off warning
- Disable mobile-specific features
- Switch to WiFi preferences

**Implementation**: Uses `ConnectivityManager`

**Permissions**: `ACCESS_NETWORK_STATE`, `CHANGE_NETWORK_STATE`

---

### App Event Triggers

**Provider**: `AppEventTriggerProvider`

#### APP_LAUNCHED

Trigger when an app is launched.

**Configuration**:

```kotlin
mapOf(
    "packageName" to "com.whatsapp"  // App package name
)
```

**Examples**:

```kotlin
// When WhatsApp opens
TriggerDTO(
    triggerType = "APP_LAUNCHED",
    triggerConfig = mapOf("packageName" to "com.whatsapp")
)

// When Facebook opens
TriggerDTO(
    triggerType = "APP_LAUNCHED",
    triggerConfig = mapOf("packageName" to "com.facebook.katana")
)
```

**Use Cases**:

- App-specific settings
- App launch counters
- Context-aware actions

**Implementation**: Uses `AccessibilityService` or `UsageStatsManager`

---

#### APP_CLOSED

Trigger when an app is closed.

**Configuration**:

```kotlin
mapOf(
    "packageName" to "com.example.app"
)
```

**Examples**:

```kotlin
TriggerDTO(
    triggerType = "APP_CLOSED",
    triggerConfig = mapOf("packageName" to "com.example.app")
)
```

**Use Cases**:

- Clean up after app closes
- Reset settings
- Track app usage

**Implementation**: Uses `AccessibilityService` or `UsageStatsManager`

---

#### APP_INSTALLED

Trigger when an app is installed.

**Configuration**:

```kotlin
mapOf(
    "packageName" to "com.example.app"  // Optional: filter by package
)
```

**Examples**:

```kotlin
// Any app installed
TriggerDTO(
    triggerType = "APP_INSTALLED",
    triggerConfig = mapOf()
)

// Specific app installed
TriggerDTO(
    triggerType = "APP_INSTALLED",
    triggerConfig = mapOf("packageName" to "com.example.app")
)
```

**Use Cases**:

- Welcome new apps
- Configure newly installed apps
- Log installations

**Implementation**: Uses `BroadcastReceiver` for `ACTION_PACKAGE_ADDED`

---

#### APP_UNINSTALLED

Trigger when an app is uninstalled.

**Configuration**:

```kotlin
mapOf(
    "packageName" to "com.example.app"  // Optional: filter by package
)
```

**Examples**:

```kotlin
// Any app uninstalled
TriggerDTO(
    triggerType = "APP_UNINSTALLED",
    triggerConfig = mapOf()
)

// Specific app uninstalled
TriggerDTO(
    triggerType = "APP_UNINSTALLED",
    triggerConfig = mapOf("packageName" to "com.example.app")
)
```

**Use Cases**:

- Clean up after uninstall
- Update app lists
- Log removals

**Implementation**: Uses `BroadcastReceiver` for `ACTION_PACKAGE_REMOVED`

---

#### NOTIFICATION_RECEIVED

Trigger when a specific notification is received.

**Configuration**:

```kotlin
mapOf(
    "packageName" to "com.example.app",  // Optional: filter by app
    "titleContains" to "Important"        // Optional: filter by title
    "textContains" to "Urgent"          // Optional: filter by text
)
```

**Examples**:

```kotlin
// Any notification
TriggerDTO(
    triggerType = "NOTIFICATION_RECEIVED",
    triggerConfig = mapOf()
)

// From specific app
TriggerDTO(
    triggerType = "NOTIFICATION_RECEIVED",
    triggerConfig = mapOf("packageName" to "com.whatsapp")
)
```

**Use Cases**:

- Auto-reply to messages
- Log important notifications
- Notification-based actions

**Implementation**: Uses `AccessibilityService` with `NotificationListener`

**Permissions**: Requires accessibility service

---

### Communication Triggers

**Provider**: `CommunicationTriggerProvider`

#### CALL_RECEIVED

Trigger when receiving a phone call.

**Configuration**:

```kotlin
mapOf(
    "phoneNumber" to "1234567890"  // Optional: filter by number
)
```

**Examples**:

```kotlin
// Any incoming call
TriggerDTO(
    triggerType = "CALL_RECEIVED",
    triggerConfig = mapOf()
)

// Specific number
TriggerDTO(
    triggerType = "CALL_RECEIVED",
    triggerConfig = mapOf("phoneNumber" to "1234567890")
)
```

**Use Cases**:

- Auto-answer logic
- Call loggers
- Privacy mode

**Implementation**: Uses `BroadcastReceiver` for `ACTION_PHONE_STATE_CHANGED`

**Permissions**: `READ_PHONE_STATE`, `READ_CALL_LOG`

---

#### CALL_ENDED

Trigger when a phone call ends.

**Configuration**:

```kotlin
mapOf(
    "phoneNumber" to "1234567890"  // Optional: filter by number
)
```

**Examples**:

```kotlin
// Any call ended
TriggerDTO(
    triggerType = "CALL_ENDED",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Call logging
- Post-call actions
- Meeting mode

**Implementation**: Uses `BroadcastReceiver` for `ACTION_PHONE_STATE_CHANGED`

**Permissions**: `READ_PHONE_STATE`, `READ_CALL_LOG`

---

#### SMS_RECEIVED

Trigger when receiving an SMS message.

**Configuration**:

```kotlin
mapOf(
    "phoneNumber" to "1234567890",  // Optional: filter by number
    "messageContains" to "urgent"       // Optional: filter by content
)
```

**Examples**:

```kotlin
// Any SMS
TriggerDTO(
    triggerType = "SMS_RECEIVED",
    triggerConfig = mapOf()
)

// From specific number
TriggerDTO(
    triggerType = "SMS_RECEIVED",
    triggerConfig = mapOf("phoneNumber" to "1234567890")
)
```

**Use Cases**:

- Auto-reply messages
- SMS logging
- Keyword triggers

**Implementation**: Uses `BroadcastReceiver` for `ACTION_RECEIVE_SMS`

**Permissions**: `RECEIVE_SMS`, `READ_SMS`

---

#### MISSED_CALL

Trigger when missing a phone call.

**Configuration**:

```kotlin
mapOf(
    "phoneNumber" to "1234567890"  // Optional: filter by number
)
```

**Examples**:

```kotlin
// Any missed call
TriggerDTO(
    triggerType = "MISSED_CALL",
    triggerConfig = mapOf()
)
```

**Use Cases**:

- Missed call notifications
- Auto-reply to missed calls
- Call back logic

**Implementation**: Uses `BroadcastReceiver` for `ACTION_PHONE_STATE_CHANGED`

**Permissions**: `READ_PHONE_STATE`, `READ_CALL_LOG`

---

### Calendar Triggers

**Provider**: `CalendarTriggerProvider`

#### CALENDAR_EVENT_STARTED

Trigger when a calendar event begins.

**Configuration**:

```kotlin
mapOf(
    "calendarId" to 1,          // Optional: filter by calendar
    "titleContains" to "Work",  // Optional: filter by title
    "descriptionContains" to "" // Optional: filter by description
)
```

**Implementation**: Uses `ContentObserver` on `CalendarContract` and `AlarmManager` for scheduling.

---

### Audio Profile Triggers

**Provider**: `AudioTriggerProvider`

#### RINGTONE_MODE_CHANGED

Trigger when ringtone mode (Silent, Vibrate, Normal) changes.

**Configuration**:

```kotlin
mapOf(
    "mode" to "SILENT" // "SILENT", "VIBRATE", "NORMAL"
)
```

**Implementation**: Uses `BroadcastReceiver` for `RINGER_MODE_CHANGED_ACTION`.

---

### Device Lock Triggers

**Provider**: `DeviceStateTriggerProvider`

#### DEVICE_LOCKED_SECURE

Trigger when device is locked with a secure method (PIN, Pattern, Biometric).

**Implementation**: Uses `KeyguardManager.isDeviceLocked()`.

---

## Adding a New Trigger

### Step 1: Create Provider Class

Create a new file in `automation/trigger/providers/`:

```kotlin
package com.aditsyal.autodroid.automation.trigger.providers

import android.content.Context
import com.aditsyal.autodroid.domain.models.TriggerDTO
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider {

    override val type: String = "MY_TRIGGER_TYPE"

    private val activeTriggers = mutableMapOf<Long, TriggerDTO>()

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        activeTriggers[trigger.id] = trigger
        try {
            setupListeners(trigger)
            Timber.i("Trigger registered: ${trigger.id} of type $type")
        } catch (e: Exception) {
            Timber.e(e, "Failed to register trigger: ${trigger.id}")
            throw e
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        activeTriggers.remove(triggerId)
        if (activeTriggers.isEmpty()) {
            cleanupListeners()
        }
        Timber.i("Trigger unregistered: $triggerId")
    }

    override suspend fun clearTriggers() {
        activeTriggers.clear()
        cleanupListeners()
        Timber.i("All triggers cleared for type: $type")
    }

    private fun setupListeners(trigger: TriggerDTO) {
        // Set up BroadcastReceiver, SensorManager, etc.
        // When event occurs, call checkTriggersUseCase()
    }

    private fun onEventDetected(triggerId: Long, eventData: Map<String, Any> = mapOf()) {
        // Notify CheckTriggersUseCase
        try {
            checkTriggersUseCase(
                type = type,
                eventData = eventData + mapOf("fired_trigger_id" to triggerId)
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to check triggers for event")
        }
    }

    private fun cleanupListeners() {
        // Unregister all listeners
    }
}
```

### Step 2: Register in Hilt Module

Add to `di/TriggerModule.kt`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class TriggerModule {

    @Binds
    @IntoSet
    abstract fun bindMyTriggerProvider(
        provider: MyTriggerProvider
    ): TriggerProvider
}
```

### Step 3: Add to Trigger Picker

Add option to `presentation/components/TriggerPickerDialog.kt`:

```kotlin
TriggerOption(
    name = "My Trigger",
    type = "MY_TRIGGER_TYPE",
    config = mapOf("configKey" to "configValue"),
    category = "My Category"  // Optional: for grouping
)
```

### Step 4: Write Tests

Create test file:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyTriggerProviderTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var provider: MyTriggerProvider

    @Before
    fun setup() {
        provider = MyTriggerProvider(
            ApplicationProvider.getApplicationContext(),
            mockCheckTriggersUseCase()
        )
    }

    @Test
    fun `register trigger sets up listeners correctly`() = runTest {
        // Given
        val trigger = TriggerDTO(
            triggerType = "MY_TRIGGER_TYPE",
            triggerConfig = mapOf("configKey" to "configValue")
        )

        // When
        runBlocking {
            provider.registerTrigger(trigger)
        }

        // Then
        verify(mockCheckTriggersUseCase).invoke(any(), any())
    }
}
```

## Trigger Event Data

When a trigger fires, it provides event data:

### Event Data Structure

```kotlin
mapOf(
    "fired_trigger_id" to trigger.id,           // ID of trigger that fired
    "event_type" to "EVENT_NAME",                // Specific event name
    "timestamp" to System.currentTimeMillis(),     // When event occurred
    "custom_data" to "value"                  // Any custom data
)
```

### Using Event Data in Actions

```kotlin
// Trigger event includes timestamp
mapOf(
    "event_time" to "{event_timestamp}",
    "triggered_by" to "{event_type}"
)
```

### Custom Event Data

```kotlin
// In trigger provider
onEventDetected(
    triggerId = trigger.id,
    eventData = mapOf(
        "location" to currentLocation.toString(),
        "battery_level" to batteryLevel.toString()
    )
)

// Can be used in actions
mapOf(
    "message" to "Triggered at {location}, battery: {battery_level}%"
)
```

## Performance Considerations

### Efficient Listener Management

```kotlin
// Good: Register once, handle multiple triggers
override suspend fun registerTrigger(trigger: TriggerDTO) {
    activeTriggers[trigger.id] = trigger
    if (activeTriggers.size == 1) {
        // Register listener only once
        registerGlobalListener()
    }
}

// Bad: Register new listener for each trigger
override suspend fun registerTrigger(trigger: TriggerDTO) {
    registerListenerForTrigger(trigger)  // Creates many listeners
}
```

### Event Debouncing

```kotlin
private var lastEventTime = 0L
private val DEBOUNCE_MS = 300L

private fun onEvent(event: Event) {
    val now = System.currentTimeMillis()
    if (now - lastEventTime < DEBOUNCE_MS) {
        return  // Ignore too frequent events
    }
    lastEventTime = now
    handleEvent(event)
}
```

### Resource Cleanup

```kotlin
override suspend fun unregisterTrigger(triggerId: Long) {
    activeTriggers.remove(triggerId)
    if (activeTriggers.isEmpty()) {
        cleanupListeners()  // Only cleanup when no more triggers
    }
}
```

### Battery Optimization

1. **Use BroadcastReceivers** for system events instead of polling
2. **Use SensorManager** efficiently (unregister when not needed)
3. **Use AlarmManager** instead of recurring timers
4. **Batch operations** where possible
5. **Use efficient geofence radii** (larger = fewer updates)

## Testing Triggers

### Unit Testing

```kotlin
@Test
fun `trigger fires correctly when event occurs`() = runTest {
    // Given
    val trigger = TriggerDTO(
        triggerType = "MY_TRIGGER_TYPE",
        triggerConfig = mapOf()
    )

    // When
    runBlocking {
        provider.registerTrigger(trigger)
        simulateEvent()
    }

    // Then
    verify(mockCheckTriggersUseCase).invoke(
        type = "MY_TRIGGER_TYPE",
        eventData = any()
    )
}
```

### Integration Testing

1. Create a test macro with your trigger
2. Enable the macro
3. Trigger the event manually or wait for it
4. Check execution logs to verify trigger fired
5. Verify actions executed correctly

### Manual Testing

1. Enable debug logging
2. Create macro with your trigger
3. Enable macro
4. Trigger event:
   - Time triggers: Wait for time or change device time
   - Location triggers: Move to location
   - Sensor triggers: Shake device, etc.
   - App triggers: Launch/close app
5. Check Logcat for trigger detection
6. Check execution history

## Best Practices

### 1. Error Handling

```kotlin
private fun onEvent(event: Event) {
    try {
        processEvent(event)
    } catch (e: SecurityException) {
        Timber.e(e, "Permission denied")
        cleanup()
    } catch (e: Exception) {
        Timber.e(e, "Trigger processing failed")
        // Don't crash, handle gracefully
    }
}
```

### 2. Permission Checks

```kotlin
private fun hasPermission(): Boolean {
    return ContextCompat.checkSelfPermission(context, PERMISSION) ==
            PackageManager.PERMISSION_GRANTED
}

override suspend fun registerTrigger(trigger: TriggerDTO) {
    if (!hasPermission()) {
        Timber.w("Permission not granted: $PERMISSION")
        return
    }
    // Proceed with registration
}
```

### 3. Logging

```kotlin
Timber.d("Trigger provider starting: $type")
Timber.i("Trigger registered: ${trigger.id}")
Timber.i("Trigger fired: ${trigger.id} at ${timestamp}")
Timber.w("Trigger failed to fire: ${trigger.id}, error: ${error}")
```

### 4. Resource Management

```kotlin
override suspend fun clearTriggers() {
    try {
        cleanupListeners()
        activeTriggers.clear()
        Timber.i("All triggers cleaned up for $type")
    } catch (e: Exception) {
        Timber.e(e, "Failed to clean up triggers")
    }
}
```

### 5. Thread Safety

```kotlin
private val mutex = Mutex()

override suspend fun registerTrigger(trigger: TriggerDTO) = mutex.withLock {
    activeTriggers[trigger.id] = trigger
    setupListeners()
}
```

### 6. Lifecycle Awareness

```kotlin
override suspend fun clearTriggers() {
    // Run in coroutine with proper lifecycle handling
    withContext(Dispatchers.IO) {
        cleanupListeners()
        activeTriggers.clear()
    }
}
```

## Common Issues

### Trigger Not Firing

**Possible Causes**:

- Macro not enabled
- Trigger not registered
- Permissions not granted
- Event not occurring
- Configuration incorrect

**Solutions**:

1. Check if macro is enabled
2. Verify trigger is registered (check logs)
3. Check permissions are granted
4. Verify trigger configuration is correct
5. Test if event is actually occurring

### Battery Drain

**Possible Causes**:

- Too many active triggers
- Continuous sensor monitoring
- Frequent location updates
- Polling instead of event-driven

**Solutions**:

1. Reduce number of active macros
2. Increase geofence radius to reduce location updates
3. Use event-driven approach instead of polling
4. Unregister triggers when not needed
5. Check battery usage in device settings

### Permission Errors

**Possible Causes**:

- Permission not in AndroidManifest
- Runtime permission not granted
- Android version restrictions
- OEM restrictions

**Solutions**:

1. Add permissions to AndroidManifest
2. Request runtime permissions before creating trigger
3. Handle SecurityException gracefully
4. Check device compatibility

### Multiple Triggers Firing

**Possible Causes**:

- Triggers not debounced
- Event occurring multiple times
- Multiple triggers registered for same event

**Solutions**:

1. Implement debouncing (300ms delay)
2. Check if trigger already registered
3. Use hysteresis for sensor triggers
4. Check for duplicate trigger registrations

## See Also

- **[Actions Guide](ACTIONS.md)**: Working with actions
- **[Constraints Guide](CONSTRAINTS.md)**: Working with constraints
- **[Architecture Documentation](ARCHITECTURE.md)**: Understanding the architecture
- **[Services Documentation](SERVICES.md)**: Android services integration
- **[Troubleshooting Guide](TROUBLESHOOTING.md)**: Common issues and solutions

---

**Ready to create powerful triggers?** Start implementing your trigger provider! 🚀
