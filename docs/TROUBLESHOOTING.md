# Troubleshooting Guide

## Common Issues and Solutions

### Macros Not Executing

**Symptoms**: Macro triggers fire but actions don't execute.

**Possible Causes**:

1. Constraints not satisfied
2. Macro disabled
3. Permission issues
4. Action execution errors

**Solutions**:

1. Check execution logs in Execution History screen
2. Verify all constraints are met
3. Ensure macro is enabled
4. Check permissions for required actions
5. Review error messages in execution logs

### Triggers Not Firing

**Symptoms**: Expected trigger events don't cause macro execution.

**Possible Causes**:

1. Trigger not registered
2. Permission issues
3. Battery optimization
4. Doze mode

**Solutions**:

1. Restart app to re-initialize triggers
2. Check trigger configuration
3. Disable battery optimization for AutoDroid
4. Ensure foreground service is running
5. Check device logs for trigger provider errors

### Battery Drain

**Symptoms**: High battery usage by AutoDroid.

**Possible Causes**:

1. Too many active triggers
2. Continuous sensor monitoring
3. Frequent location updates
4. Background work not optimized

**Solutions**:

1. Reduce number of active macros
2. Use time-based triggers instead of continuous monitoring where possible
3. Increase geofence radius to reduce location updates
4. Check battery usage in Android Settings
5. Ensure battery optimization exemption is requested

### Permission Errors

**Symptoms**: Actions fail with permission errors.

**Possible Causes**:

1. Permission not granted
2. Permission revoked by user
3. Android version restrictions

**Solutions**:

1. Request permissions in Settings screen
2. Re-grant permissions if revoked
3. Check Android version compatibility
4. Some permissions require system-level access (not available to regular apps)

### Variables Not Working

**Symptoms**: Variable placeholders not replaced in actions.

**Possible Causes**:

1. Variable doesn't exist
2. Wrong variable name
3. Scope mismatch (LOCAL vs GLOBAL)
4. Variable not set before use

**Solutions**:

1. Verify variable name spelling
2. Check variable scope
3. Ensure variable is set before action that uses it
4. Check variable value in database

### Logic Control Not Working

**Symptoms**: If/else conditions or loops not executing correctly.

**Possible Causes**:

1. Condition syntax incorrect
2. Variable not resolved in condition
3. Loop index configuration wrong

**Solutions**:

1. Verify condition configuration
2. Check variable values used in conditions
3. Review loop configuration (iterations, end index)
4. Check execution logs for logic evaluation errors

### Templates Not Loading

**Symptoms**: Template library is empty.

**Possible Causes**:

1. Templates not initialized
2. Database migration issue

**Solutions**:

1. Restart app to trigger template initialization
2. Check app logs for initialization errors
3. Clear app data and reinstall (last resort)

### Execution History Not Showing

**Symptoms**: Execution history screen is empty or not updating.

**Possible Causes**:

1. No executions yet
2. Database query issue
3. Filter applied

**Solutions**:

1. Verify macros have executed
2. Check filters in Execution History screen
3. Clear filters to see all logs
4. Check database for execution logs

### Foreground Service Not Running

**Symptoms**: Background execution unreliable.

**Possible Causes**:

1. Service not started
2. Notification channel not created
3. Service killed by system

**Solutions**:

1. Restart app to start service
2. Check notification settings
3. Ensure app is not in battery optimization
4. Check if service is running in notification panel

### Geofencing Not Working

**Symptoms**: Location triggers not firing.

**Possible Causes**:

1. Location permission not granted
2. Background location permission missing
3. GPS disabled
4. Geofence too small

**Solutions**:

1. Grant location permissions (including background)
2. Enable GPS in device settings
3. Increase geofence radius
4. Check location accuracy settings

### Time Triggers Not Accurate

**Symptoms**: Time-based triggers fire at wrong time.

**Possible Causes**:

1. Exact alarm permission not granted
2. Device in Doze mode
3. System time changed

**Solutions**:

1. Grant SCHEDULE_EXACT_ALARM permission
2. Request battery optimization exemption
3. Use setExactAndAllowWhileIdle for critical triggers
4. Re-register triggers after time change

### SMS/Call Actions Not Working

**Symptoms**: SMS sending or call making fails.

**Possible Causes**:

1. Permission not granted
2. Default SMS/call app not set
3. Android version restrictions

**Solutions**:

1. Grant SEND_SMS and CALL_PHONE permissions
2. Set AutoDroid as default SMS app (if required)
3. Check Android version compatibility
4. Some features require system-level permissions

### App Launch/Close Not Working

**Symptoms**: Launch app or close app actions fail.

**Possible Causes**:

1. Package name incorrect
2. App not installed
3. Permission restrictions

**Solutions**:

1. Verify package name is correct
2. Ensure target app is installed
3. Some system apps cannot be controlled
4. Check if app is in background/foreground

### Sidebar Launcher Not Visible

**Symptoms**: Sidebar trigger doesn't appear on screen edge.

**Possible Causes**:

1. Feature disabled in settings
2. Overlay permission not granted
3. Service killed by system

**Solutions**:

1. Enable Sidebar Launcher in Settings
2. Grant "Display over other apps" permission
3. Disable battery optimization for AutoDroid

### Dry-Run Simulation Incorrect

**Symptoms**: Dry-run results don't match actual execution.

**Possible Causes**:

1. State changed between dry-run and trigger
2. Variable resolution difference
3. Permission-dependent checks

**Solutions**:

1. Refresh the dry-run simulation
2. Check if variables are correctly initialized
3. Verify all required permissions are granted

## Getting Help

### Logs

- Check Timber logs in Logcat (filter by "AutoDroid")
- Review execution logs in Execution History screen
- Check device logs for system-level errors

### Debug Mode

- Enable debug logging in app settings
- Use dry-run mode to test macros without executing
- Check execution logs for detailed error messages

### Reporting Issues

1. Note the exact steps to reproduce
2. Check execution logs for error messages
3. Note Android version and device model
4. Include relevant log snippets

## Performance Tips

1. **Limit Active Macros**: Too many active macros can impact performance
2. **Optimize Triggers**: Use efficient trigger types (time-based vs continuous monitoring)
3. **Reduce Constraints**: Fewer constraints = faster evaluation
4. **Variable Usage**: Minimize variable lookups in hot paths
5. **Action Delays**: Use delays sparingly to avoid blocking execution

## Best Practices

1. **Test Before Production**: Always test macros before relying on them
2. **Monitor Execution Logs**: Regularly check execution history
3. **Backup Macros**: Export macros for backup
4. **Update Regularly**: Keep app updated for bug fixes
5. **Report Issues**: Help improve the app by reporting bugs
