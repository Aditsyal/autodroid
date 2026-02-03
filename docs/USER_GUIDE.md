# AutoDroid User Guide

Welcome to AutoDroid! This guide will help you create powerful automations to streamline your Android experience.

## Table of Contents

- [Introduction](#introduction)
- [Getting Started](#getting-started)
- [Creating Your First Macro](#creating-your-first-macro)
- [Understanding Triggers](#understanding-triggers)
- [Adding Actions](#adding-actions)
- [Using Constraints](#using-constraints)
- [Working with Variables](#working-with-variables)
- [Logic Control](#logic-control)
- [Using Templates](#using-templates)
- [Managing Macros](#managing-macros)
- [Execution History](#execution-history)
- [Advanced Features](#advanced-features)
- [Tips and Best Practices](#tips-and-best-practices)
- [Troubleshooting](#troubleshooting)

## Introduction

AutoDroid is an automation app that allows you to create rules (called "macros") that automatically perform actions based on triggers you specify. Each macro consists of:

- **Trigger(s)**: Events that start the automation (time, location, device state, etc.)
- **Actions**: What the automation does (change settings, launch apps, send messages, etc.)
- **Constraints**: Optional conditions that must be met for the macro to run
- **Variables**: Store and use data across actions

## Getting Started

### Initial Setup

1. **Download and Install** AutoDroid from the Google Play Store (or build from source)

2. **Grant Permissions**: When you first open the app, grant the requested permissions:
   - Location (for location-based triggers)
   - Notifications (for automation feedback)
   - SMS/Phone (if using communication features)
   - Accessibility (for UI automation)

3. **Enable Accessibility Service**:
   - Go to **Settings > Accessibility > AutoDroid**
   - Enable the AutoDroid accessibility service
   - Grant all requested permissions
   - This is required for many automation features

4. **Disable Battery Optimization** (important for reliable execution):
   - Go to **Settings > Apps > AutoDroid > Battery**
   - Set to **Don't optimize** or **Unrestricted**

5. **Enable Location Services** (if using location-based triggers):
   - Go to **Settings > Location**
   - Enable location services
   - Set accuracy to "High accuracy"

### Navigating the App

- **Macros Screen**: View and manage all your automations
- **Macro Editor**: Create or edit individual macros
- **Templates Library**: Browse and use pre-configured templates
- **Settings**: Configure app preferences
- **Execution History**: View past macro executions
- **Conflict Detection**: Check for macro conflicts

## Creating Your First Macro

Let's create a simple macro to get started:

### Example: Automatic Wi-Fi at Home

1. Open the app and tap the **+** button on the Macros screen
2. **Name** your macro: "Home WiFi"
3. **Add a Trigger**:
   - Tap "Add Trigger"
   - Select "Location"
   - Choose "Enter Location"
   - Set your home location (or use current location)
   - Set radius (e.g., 100 meters)
4. **Add an Action**:
   - Tap "Add Action"
   - Select "System Settings"
   - Choose "Enable WiFi"
5. **Save** the macro
6. **Enable** the macro using the toggle

Now whenever you arrive home, Wi-Fi will automatically enable!

## Navigation and Gestures

AutoDroid supports modern Android navigation patterns for a smooth experience:

### Back Navigation

- **Back Button**: Tap the back arrow in the top-left corner of screens to return to the previous screen
- **Predictive Back Gesture** (Android 13+): Swipe from the left or right edge of the screen to see a preview of where you'll go before completing the gesture
  - This provides visual feedback showing the destination screen before you commit to the navigation
  - If you cancel the gesture (don't swipe far enough), you'll stay on the current screen

### Screen Navigation

- **Macros List**: Main screen showing all your automation rules
- **Macro Details**: View and edit individual macros
- **Macro Editor**: Create or modify automation rules
- **Settings**: Configure app preferences and options
- **Execution History**: View past automation runs
- **Templates**: Use pre-built automation templates
- **Import/Export**: Share macros with others or backup your automations

## Understanding Triggers

Triggers are the events that start your macros. AutoDroid supports 30+ trigger types:

### Time-Based Triggers

- **Specific Time**: Macro runs at a specific time (e.g., 8:00 AM)
- **Time Interval**: Macro runs every X minutes/hours
- **Day of Week**: Macro runs on specific days (e.g., Monday-Friday)
- **Date Range**: Macro runs within a date range

**Example Use Cases**:

- "Good morning" routine at 7:00 AM
- "Work mode" at 9:00 AM on weekdays
- "Sleep mode" at 10:00 PM

### Location-Based Triggers

- **Enter Location**: When you enter a geofence
- **Exit Location**: When you leave a geofence
- **Inside/Outside**: While inside or outside a geofence

**Example Use Cases**:

- Enable Wi-Fi when you arrive home
- Enable Bluetooth when you leave home
- Change settings based on location (work, home, gym)

### Device State Triggers

- **Screen On/Off**: When screen turns on or off
- **Device Locked/Unlocked**: When device is locked or unlocked
- **Charging Connected**: When you plug in charger
- **Battery Level**: When battery reaches a level

**Example Use Cases**:

- Dim brightness when screen turns on at night
- Enable airplane mode when battery < 20%
- Disable screen timeout while charging

### Connectivity Triggers

- **WiFi Connected**: When connected to WiFi
- **WiFi Disconnected**: When WiFi disconnects
- **WiFi SSID Connected**: When connected to specific WiFi network
- **Bluetooth Connected**: When Bluetooth device connects
- **Mobile Data Enabled**: When mobile data turns on

**Example Use Cases**:

- Sync apps when on WiFi
- Enable Bluetooth when in car
- Auto-reply messages when not on WiFi

### App Event Triggers

- **App Launched**: When an app is opened
- **App Closed**: When an app is closed
- **Notification Received**: When a specific notification arrives

**Example Use Cases**:

- Change settings when launching specific apps
- Log app usage
- Take action on specific notifications

### Communication Triggers

- **Call Received**: When receiving a call
- **Call Ended**: When a call ends
- **SMS Received**: When receiving an SMS
- **Missed Call**: When missing a call

**Example Use Cases**:

- Auto-reply to missed calls
- Log incoming calls
- Take action on specific SMS senders

### Sensor Triggers

- **Shake**: When device is shaken
- **Proximity**: When proximity sensor detects object
- **Light Level**: When ambient light changes
- **Orientation Change**: When device rotates

### Calendar Triggers

- **Event Started**: When a calendar event begins
- **Event Ended**: When a calendar event ends
- **Event Keyword**: When an event with specific text in title/description is detected

**Example Use Cases**:

- Enable "Meeting Mode" when a work calendar event starts
- Silence phone during specific appointments
- Log work hours based on calendar entries

### Audio Profile Triggers

- **Ringtone Mode**: When switched between Silent, Vibrate, or Normal
- **Volume Level**: When volume on a specific stream (Media, Ring, Alarm) crosses a threshold

**Example Use Cases**:

- Enable Bluetooth when volume is set to maximum
- Send a "Phone Silenced" notification to specific contacts
- Adjust brightness when entering vibrate mode

## Adding Actions

Actions are what your macros do when triggered. AutoDroid supports 35+ action types:

### System Settings

- **Enable/Disable WiFi**: Toggle WiFi
- **Enable/Disable Bluetooth**: Toggle Bluetooth
- **Set Brightness**: Adjust screen brightness (0-100)
- **Set Screen Timeout**: Change how long screen stays on
- **Toggle Airplane Mode**: Enable/disable airplane mode
- **Toggle GPS**: Enable/disable location services
- **Volume Control**: Adjust volume levels

### Device Control

- **Lock Screen**: Lock the device programmatically (requires Device Admin)
- **Unlock Screen**: Wake up the device and dismiss non-secure lock screen
- **Sleep Device**: Turn off the screen immediately
- **Vibrate**: Trigger haptic patterns
- **Enable/Disable Do Not Disturb**: Toggle DND mode with priority support

### Communication

- **Send SMS**: Send an SMS message
- **Send Email**: Open email composer
- **Make Call**: Dial a phone number
- **Speak Text**: Use text-to-speech to read text aloud

### App Control

- **Launch App**: Open a specific app
- **Close App**: Force-stop an app
- **Clear App Cache**: Clear an app's cache
- **Open URL**: Open a URL in browser

### Media

- **Play Sound**: Play a system sound or audio file
- **Stop Sound**: Stop media playback
- **Change Wallpaper**: Set the device wallpaper

### Notifications

- **Show Notification**: Display a notification
- **Show Toast**: Display a temporary message

### Automation

- **Delay**: Wait X seconds before next action
- **Set Variable**: Store a value in a variable
- **HTTP Request (Webhook)**: Send GET/POST/PUT/DELETE requests to any URL
- **Log Event**: Log to execution history

### Logic Control

- **If/Else Condition**: Execute actions based on conditions
- **While Loop**: Repeat actions while condition is true
- **For Loop**: Repeat actions for a specified count
- **Break**: Exit a loop
- **Continue**: Skip to next iteration

## Using Constraints

Constraints are optional conditions that must be met for your macro to run, even when the trigger fires. This allows you to create more sophisticated automations.

### Constraint Types

- **Time Range**: Macro only runs during specific hours
- **Day of Week**: Macro only runs on specific days
- **Exclude Weekends**: Skip weekends
- **Specific Date**: Macro only runs on a specific date
- **Battery Level**: Macro only runs when battery is within a range
- **Charging Status**: Macro only runs when charging or not charging
- **Screen State**: Macro only runs when screen is on/off
- **Device Locked**: Macro only runs when device is locked/unlocked
- **WiFi Connected**: Macro only runs when WiFi is connected
- **WiFi SSID**: Macro only runs when connected to specific network
- **Mobile Data Active**: Macro only runs when mobile data is on
- **Bluetooth Connected**: Macro only runs when Bluetooth is connected
- **Bluetooth Device**: Macro only runs when connected to specific device
- **Inside Geofence**: Macro only runs when inside a location
- **Outside Geofence**: Macro only runs when outside a location
- **App Running**: Macro only runs when a specific app is running
- **Headphones Connected**: Macro only runs when headphones are connected
- **Do Not Disturb Enabled**: Macro only runs when DND is on

### Example: Using Constraints

Create a macro that:

- **Trigger**: Time at 8:00 AM
- **Constraint**: Day of Week is Monday-Friday
- **Constraint**: Battery level > 20%
- **Action**: Enable work mode

This macro will only run on weekdays when your battery is sufficient.

## Working with Variables

Variables allow you to store and use data across actions. AutoDroid supports:

- **Local Variables**: Scoped to a single macro execution
- **Global Variables**: Persist across macro executions

### Variable Operations

- **Set Variable**: Create or update a variable
- **Get Variable**: Use a variable's value in actions
- **Add**: Add to a numeric variable
- **Subtract**: Subtract from a numeric variable
- **Multiply**: Multiply a numeric variable
- **Divide**: Divide a numeric variable
- **Append**: Append text to a string variable
- **Substring**: Extract part of a string variable

### Using Variables in Actions

Variables can be used in action configurations using placeholders:

- `{variableName}`: Use a local variable
- `{globalVariableName}`: Use a global variable

**Example**:

1. Set variable `{userName}` to "John"
2. Send SMS with message: "Hello {userName}!"
3. Result: SMS sent with "Hello John!"

### Example: Counter

Create a macro that counts how many times it runs:

1. **Trigger**: Shake device
2. **Action**: Set variable `{count}` to `{count} + 1`
3. **Action**: Show toast with "Shake count: {count}"

Each time you shake the device, the counter increments!

## Logic Control

Logic control actions allow you to create complex automation workflows:

### If/Else Conditions

Execute different actions based on conditions:

```
IF {batteryLevel} < 20
  THEN: Show toast "Low battery!"
  ELSE: Show toast "Battery OK"
```

### While Loops

Repeat actions while a condition is true:

```
WHILE {count} < 5
  DO: Set variable {count} to {count} + 1
```

### For Loops

Repeat actions a specified number of times:

```
FOR 3 iterations
  DO: Vibrate device
END
```

### Break and Continue

- **Break**: Exit the current loop
- **Continue**: Skip to the next iteration

**Example**: Loop through items and skip specific ones

```
FOR 10 iterations
  IF {count} = 5
    DO: Continue (skip iteration 5)
  DO: Show toast "Iteration {count}"
END
```

## Using Templates

Templates are pre-configured macros that you can use as-is or customize. AutoDroid includes 10+ templates:

### Available Templates

1. **Morning Routine**: Automate your morning tasks
2. **Work Mode**: Configure settings for work
3. **Sleep Mode**: Configure settings for sleep
4. **Driving Mode**: Configure settings while driving
5. **Meeting Mode**: Configure settings for meetings
6. **Battery Saver**: Activate when battery is low
7. **Home Mode**: Configure settings at home
8. **Travel Mode**: Configure settings while traveling
9. **Quiet Hours**: Silence notifications during specific hours
10. **Emergency Mode**: Quick access to emergency features

### Using Templates

1. Go to **Templates Library** screen
2. Browse available templates
3. Tap a template to view details
4. Tap **"Use Template"** to create a macro from it
5. Customize the macro as needed
6. Save and enable the macro

## Managing Macros

### Viewing Macros

- See all macros on the **Macros** screen
- Each macro card shows:
  - Name
  - Trigger types
  - Number of actions
  - Enable/disable status
  - Last execution time

### Editing Macros

1. Tap a macro on the Macros screen
2. Modify any aspect:

- Name
- Triggers
- Actions
- Constraints
- Variables

3. Save changes

### Enabling/Disabling Macros

- Use the toggle on each macro card
- Disabled macros won't execute
- Keep macros disabled to save battery

### Deleting Macros

1. Long-press a macro
2. Tap **Delete**
3. Confirm deletion

### Importing/Exporting Macros

- **Export**: Share macros with others or backup your automations to JSON or XML files.
- **Import**: Load macros from file with intelligent conflict detection.
  - **Overwrite**: Replace existing macro with the same name.
  - **Skip**: Keep the existing macro and ignore the imported one.
  - **Rename**: Import the macro with a new name to avoid conflicts.
- Useful for:
  - Backing up your automations to cloud storage.
  - Sharing powerful workflows with the community.
  - Migrating your setup to a new device seamlessly.

## Execution History

View past macro executions to:

- See what executed and when
- Debug issues
- Monitor automation performance
- Filter by status (success, failure, skipped)
- Search by macro name

### Accessing Execution History

1. Go to **Execution History** screen
2. View list of past executions
3. Tap an entry for details
4. Use filters to find specific executions
5. Search by macro name

### Understanding Execution Status

- **Success**: Macro executed successfully
- **Failure**: Macro failed (check error message)
- **Skipped**: Constraints not met

### Dynamic Theming

AutoDroid supports **Material You** dynamic color on Android 12+ devices:

- **System Theme**: App colors adapt to your device wallpaper
- **Dark Mode**: Supports both standard dark theme and high-contrast **AMOLED Black** mode
- **Dynamic Color Toggle**: Enable or disable wallpaper-based theming in Settings

### Sidebar Launcher

Quickly access your favorite macros from any app:

1. Enable the **Sidebar Launcher** in Settings
2. Swipe from the configured edge of the screen to open the sidebar
3. Tap a macro to execute it manually
4. Long-press to reposition the sidebar trigger

### Dry-Run Preview

Not sure what a macro will do? Use **Dry-Run Preview**:

1. Open a macro in the editor
2. Tap the **Preview** button
3. See exactly which constraints pass, which actions will run, and the estimated battery impact
4. Fix any issues before enabling the macro for real

### Variable Management

Manage all your global variables in one place:

- Go to **Settings > Variable Management**
- View, edit, or delete global variables
- Track where each variable is being used across your macros

### Notification Feedback

Configure which actions show notifications:

- Success notifications
- Failure notifications
- Detailed error messages
- Customize notification sound

### Battery Optimization

AutoDroid is designed for minimal battery impact:

- Efficient trigger detection
- Smart background execution
- Optimized database queries
- Battery usage typically < 5% per hour

## Tips and Best Practices

### Creating Effective Macros

1. **Start Simple**: Begin with basic macros, then add complexity
2. **Test Thoroughly**: Test macros before relying on them
3. **Use Constraints**: Add constraints to prevent unwanted executions
4. **Monitor Execution**: Check execution history regularly
5. **Disable Unused Macros**: Turn off macros you don't need

### Organizing Macros

- Use descriptive names (e.g., "Morning Routine" not "Macro 1")
- Group related macros (e.g., "Work Mode", "Work Email", "Work Calendar")
- Add descriptions to complex macros
- Use consistent naming conventions

### Performance Tips

- Avoid too many active macros (impact on battery)
- Use time-based triggers instead of continuous monitoring where possible
- Increase geofence radius to reduce location updates
- Keep actions focused and efficient
- Use delays sparingly

### Security Tips

- Review permissions for sensitive actions
- Be careful with SMS and call actions
- Don't share sensitive macros publicly
- Regularly review execution history
- Keep app updated

### Backup Your Macros

- Export macros regularly
- Keep backups in safe location
- Version important macros
- Share backups with yourself via cloud storage

## Troubleshooting

### Macro Not Executing

1. Check if macro is enabled
2. Verify trigger is firing (check logs)
3. Check constraints are satisfied
4. Verify permissions are granted
5. Check execution history for errors

### Trigger Not Firing

1. Restart the app
2. Check if accessibility service is enabled
3. Verify permissions are granted
4. Disable battery optimization
5. Check device logs for errors

### Actions Not Executing

1. Verify action configuration is correct
2. Check action permissions
3. Test action in isolation
4. Check execution logs for errors
5. Ensure constraints are met

### Battery Drain

1. Reduce number of active macros
2. Use efficient trigger types
3. Increase geofence radius
4. Disable unused macros
5. Check battery usage in device settings

For more troubleshooting tips, see [Troubleshooting Guide](TROUBLESHOOTING.md).

## Need Help?

- **Check Documentation**: Browse the `/docs` folder
- **View Execution History**: Look for error messages
- **Contact Support**: Report issues on GitHub

Happy automating! 🚀
