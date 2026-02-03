# Frequently Asked Questions (FAQ)

## General Questions

### What is AutoDroid?

AutoDroid is a powerful automation app for Android that allows you to create rules (macros) to perform actions automatically based on various triggers and conditions.

### Is AutoDroid free?

AutoDroid is open-source and free to use.

### Which Android versions are supported?

AutoDroid supports Android 7.0 (API 24) and higher. Some features may require newer Android versions (e.g., Predictive Back requires Android 13+).

## Automation & Macros

### What is a "Macro"?

A macro is a set of automation rules consisting of one or more triggers, optional constraints, and a sequence of actions.

### Can I have multiple triggers for one macro?

Yes, a macro will execute if ANY of its triggers fire, provided all constraints are satisfied.

### What are constraints?

Constraints are conditions that must be met at the time a trigger fires for the actions to execute. For example, "only run if I'm connected to WiFi".

### How do I use variables?

You can set variables using the "Set Variable" action and reference them in other actions using the `{variableName}` syntax.

## Permissions & Security

### Why does AutoDroid need Accessibility Service?

The Accessibility Service allows AutoDroid to detect UI events (like app launches) and automate UI interactions across different apps.

### Is my data safe?

AutoDroid stores all your macros and execution logs locally on your device. It does not send your data to any external servers.

### Why do I need to disable battery optimization?

Android's battery optimization can kill background services. Disabling it ensures that AutoDroid can reliably detect triggers and execute macros even when the app is not in the foreground.

## Troubleshooting

### Why didn't my macro run?

Check the **Execution History** screen. It will show if a macro was "Skipped" due to constraints or if it "Failed" due to an error.

### My location trigger isn't firing.

Ensure that:

1. Location services are enabled.
2. AutoDroid has "Allow all the time" location permission.
3. Your geofence radius is large enough (at least 100m recommended for reliability).

### The Sidebar Launcher disappeared.

Check if the "Sidebar Launcher" is still enabled in Settings and ensure that the "Display over other apps" permission is granted.

## Advanced

### How do I backup my macros?

Use the **Export** feature in the app to save your macros to a JSON or XML file. You can then **Import** them later or on another device.

### What is "Dry-Run Preview"?

It's a simulation mode that shows you what a macro would do without actually performing the actions. It's great for testing complex logic safely.
