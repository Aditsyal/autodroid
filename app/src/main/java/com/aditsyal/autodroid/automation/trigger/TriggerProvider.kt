package com.aditsyal.autodroid.automation.trigger

import com.aditsyal.autodroid.data.models.TriggerDTO

/**
 * Interface for components that can monitor for specific trigger conditions.
 * Implementation-specific (e.g., Geofencing for Location, AlarmManager for Time).
 */
interface TriggerProvider {
    /**
     * Unique identifier for the trigger type (e.g., "TIME", "LOCATION").
     */
    val type: String

    /**
     * Start monitoring for a specific trigger.
     */
    suspend fun registerTrigger(trigger: TriggerDTO)

    /**
     * Stop monitoring for a specific trigger.
     */
    suspend fun unregisterTrigger(triggerId: Long)

    /**
     * Clear all triggers managed by this provider.
     */
    suspend fun clearTriggers()
}
