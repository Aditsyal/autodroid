package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalTime
import javax.inject.Inject

/**
 * Use case for detecting conflicts between macros and their components
 */
class ConflictDetectionUseCase @Inject constructor() {

    data class Conflict(
        val type: ConflictType,
        val severity: Severity,
        val description: String,
        val affectedMacros: List<MacroDTO>,
        val recommendation: String
    )

    enum class ConflictType {
        TRIGGER_OVERLAP,
        ACTION_CONFLICT,
        CONSTRAINT_VIOLATION,
        RESOURCE_CONTENTION
    }

    enum class Severity {
        LOW,      // Minor issue, macro will still work
        MEDIUM,   // Potential problem, user should review
        HIGH,     // Significant conflict, may cause issues
        CRITICAL  // Will definitely cause problems
    }

    data class ConflictCheckResult(
        val hasConflicts: Boolean,
        val conflicts: List<Conflict>,
        val canProceed: Boolean
    )

    /**
     * Check for conflicts in a single macro during creation/editing
     */
    fun checkMacroConflicts(macro: MacroDTO, allMacros: List<MacroDTO>): Flow<ConflictCheckResult> = flow {
        val conflicts = mutableListOf<Conflict>()

        // Check trigger overlaps with other macros
        val triggerConflicts = checkTriggerConflicts(macro, allMacros)
        conflicts.addAll(triggerConflicts)

        // Check action conflicts within this macro
        val actionConflicts = checkActionConflicts(macro)
        conflicts.addAll(actionConflicts)

        // Check constraint violations
        val constraintConflicts = checkConstraintConflicts(macro)
        conflicts.addAll(constraintConflicts)

        val hasCriticalConflicts = conflicts.any { it.severity == Severity.CRITICAL }
        val canProceed = !hasCriticalConflicts

        emit(ConflictCheckResult(
            hasConflicts = conflicts.isNotEmpty(),
            conflicts = conflicts,
            canProceed = canProceed
        ))
    }

    /**
     * Check for conflicts between all macros in the system
     */
    fun checkAllMacroConflicts(macros: List<MacroDTO>): Flow<List<Conflict>> = flow {
        val allConflicts = mutableListOf<Conflict>()

        // Check each pair of macros for conflicts
        for (i in macros.indices) {
            for (j in i + 1 until macros.size) {
                val macro1 = macros[i]
                val macro2 = macros[j]

                // Check if both macros are enabled
                if (macro1.enabled && macro2.enabled) {
                    val conflicts = checkMacrosConflict(macro1, macro2)
                    allConflicts.addAll(conflicts)
                }
            }
        }

        emit(allConflicts)
    }

    private fun checkTriggerConflicts(macro: MacroDTO, allMacros: List<MacroDTO>): List<Conflict> {
        val conflicts = mutableListOf<Conflict>()

        for (otherMacro in allMacros) {
            if (otherMacro.id == macro.id || !otherMacro.enabled) continue

            for (trigger in macro.triggers) {
                for (otherTrigger in otherMacro.triggers) {
                    val conflict = checkTriggerPairConflict(trigger, otherTrigger, macro, otherMacro)
                    if (conflict != null) {
                        conflicts.add(conflict)
                    }
                }
            }
        }

        return conflicts
    }

    private fun checkTriggerPairConflict(
        trigger1: TriggerDTO,
        trigger2: TriggerDTO,
        macro1: MacroDTO,
        macro2: MacroDTO
    ): Conflict? {
        // Time-based trigger conflicts
        if (trigger1.triggerType == "TIME" && trigger2.triggerType == "TIME") {
            return checkTimeTriggerConflict(trigger1, trigger2, macro1, macro2)
        }

        // Location-based trigger conflicts
        if (trigger1.triggerType == "LOCATION" && trigger2.triggerType == "LOCATION") {
            return checkLocationTriggerConflict(trigger1, trigger2, macro1, macro2)
        }

        // App event conflicts
        if (trigger1.triggerType == "APP_EVENT" && trigger2.triggerType == "APP_EVENT") {
            return checkAppEventTriggerConflict(trigger1, trigger2, macro1, macro2)
        }

        return null
    }

    private fun checkTimeTriggerConflict(
        trigger1: TriggerDTO,
        trigger2: TriggerDTO,
        macro1: MacroDTO,
        macro2: MacroDTO
    ): Conflict? {
        val time1 = trigger1.triggerConfig["time"] as? String
        val time2 = trigger2.triggerConfig["time"] as? String

        if (time1 != null && time2 != null && time1 == time2) {
            return Conflict(
                type = ConflictType.TRIGGER_OVERLAP,
                severity = Severity.HIGH,
                description = "Both '${macro1.name}' and '${macro2.name}' trigger at the same time ($time1)",
                affectedMacros = listOf(macro1, macro2),
                recommendation = "Consider staggering the trigger times or combining the macros"
            )
        }

        return null
    }

    private fun checkLocationTriggerConflict(
        trigger1: TriggerDTO,
        trigger2: TriggerDTO,
        macro1: MacroDTO,
        macro2: MacroDTO
    ): Conflict? {
        // Simplified location conflict detection
        // In a real implementation, this would check geofence overlap
        val location1 = trigger1.triggerConfig["location"] as? String
        val location2 = trigger2.triggerConfig["location"] as? String

        if (location1 != null && location2 != null && location1 == location2) {
            return Conflict(
                type = ConflictType.TRIGGER_OVERLAP,
                severity = Severity.MEDIUM,
                description = "'${macro1.name}' and '${macro2.name}' may trigger at the same location",
                affectedMacros = listOf(macro1, macro2),
                recommendation = "Consider different location triggers or review macro priorities"
            )
        }

        return null
    }

    private fun checkAppEventTriggerConflict(
        trigger1: TriggerDTO,
        trigger2: TriggerDTO,
        macro1: MacroDTO,
        macro2: MacroDTO
    ): Conflict? {
        val event1 = trigger1.triggerConfig["eventType"] as? String
        val app1 = trigger1.triggerConfig["packageName"] as? String
        val event2 = trigger2.triggerConfig["eventType"] as? String
        val app2 = trigger2.triggerConfig["packageName"] as? String

        if (event1 != null && event2 != null && app1 != null && app2 != null &&
            event1 == event2 && app1 == app2) {
            return Conflict(
                type = ConflictType.TRIGGER_OVERLAP,
                severity = Severity.MEDIUM,
                description = "Both macros trigger on the same app event ($event1 for $app1)",
                affectedMacros = listOf(macro1, macro2),
                recommendation = "Consider if both macros need to respond to this event"
            )
        }

        return null
    }

    private fun checkActionConflicts(macro: MacroDTO): List<Conflict> {
        val conflicts = mutableListOf<Conflict>()

        // Check for multiple actions that modify the same resource
        val brightnessActions = macro.actions.filter { it.actionType == "SET_BRIGHTNESS" }
        if (brightnessActions.size > 1) {
            conflicts.add(Conflict(
                type = ConflictType.ACTION_CONFLICT,
                severity = Severity.MEDIUM,
                description = "Multiple brightness adjustments in '${macro.name}'",
                affectedMacros = listOf(macro),
                recommendation = "Consider keeping only the final brightness setting"
            ))
        }

        val volumeActions = macro.actions.filter { it.actionType == "SET_VOLUME" }
        if (volumeActions.size > 1) {
            conflicts.add(Conflict(
                type = ConflictType.ACTION_CONFLICT,
                severity = Severity.MEDIUM,
                description = "Multiple volume adjustments in '${macro.name}'",
                affectedMacros = listOf(macro),
                recommendation = "Consider keeping only the final volume setting"
            ))
        }

        // Check for conflicting WiFi actions
        val wifiActions = macro.actions.filter {
            it.actionType == "ENABLE_WIFI" || it.actionType == "DISABLE_WIFI"
        }
        if (wifiActions.size > 1) {
            val hasEnable = wifiActions.any { it.actionConfig["enabled"] == true }
            val hasDisable = wifiActions.any { it.actionConfig["enabled"] == false }

            if (hasEnable && hasDisable) {
                conflicts.add(Conflict(
                    type = ConflictType.ACTION_CONFLICT,
                    severity = Severity.HIGH,
                    description = "'${macro.name}' has conflicting WiFi enable/disable actions",
                    affectedMacros = listOf(macro),
                    recommendation = "Remove conflicting WiFi actions or add delays between them"
                ))
            }
        }

        return conflicts
    }

    private fun checkConstraintConflicts(macro: MacroDTO): List<Conflict> {
        val conflicts = mutableListOf<Conflict>()

        // Check for impossible constraint combinations
        val batteryConstraints = macro.constraints.filter { it.constraintType == "BATTERY_LEVEL" }
        if (batteryConstraints.size > 1) {
            val minLevels = batteryConstraints.mapNotNull {
                it.constraintConfig["minLevel"] as? Number
            }.map { it.toDouble() }

            if (minLevels.size > 1 && minLevels.maxOrNull() != minLevels.minOrNull()) {
                conflicts.add(Conflict(
                    type = ConflictType.CONSTRAINT_VIOLATION,
                    severity = Severity.HIGH,
                    description = "'${macro.name}' has conflicting battery level requirements",
                    affectedMacros = listOf(macro),
                    recommendation = "Use a single battery level constraint with the highest requirement"
                ))
            }
        }

        return conflicts
    }

    private fun checkMacrosConflict(macro1: MacroDTO, macro2: MacroDTO): List<Conflict> {
        val conflicts = mutableListOf<Conflict>()

        // Check for resource contention between macros
        val macro1Actions = macro1.actions.map { it.actionType }
        val macro2Actions = macro2.actions.map { it.actionType }

        // Check for simultaneous system setting changes
        val systemActions1 = macro1Actions.filter { isSystemAction(it) }
        val systemActions2 = macro2Actions.filter { isSystemAction(it) }

        if (systemActions1.isNotEmpty() && systemActions2.isNotEmpty()) {
            conflicts.add(Conflict(
                type = ConflictType.RESOURCE_CONTENTION,
                severity = Severity.LOW,
                description = "'${macro1.name}' and '${macro2.name}' both modify system settings",
                affectedMacros = listOf(macro1, macro2),
                recommendation = "Consider adding delays between macro executions"
            ))
        }

        // Check for simultaneous media control
        val mediaActions1 = macro1Actions.filter { isMediaAction(it) }
        val mediaActions2 = macro2Actions.filter { isMediaAction(it) }

        if (mediaActions1.isNotEmpty() && mediaActions2.isNotEmpty()) {
            conflicts.add(Conflict(
                type = ConflictType.RESOURCE_CONTENTION,
                severity = Severity.MEDIUM,
                description = "'${macro1.name}' and '${macro2.name}' both control media playback",
                affectedMacros = listOf(macro1, macro2),
                recommendation = "Media controls may interfere with each other"
            ))
        }

        return conflicts
    }

    private fun isSystemAction(actionType: String): Boolean {
        return actionType in listOf(
            "SET_BRIGHTNESS", "SET_VOLUME", "ENABLE_WIFI",
            "DISABLE_WIFI", "SET_BLUETOOTH", "SET_AIRPLANE_MODE"
        )
    }

    private fun isMediaAction(actionType: String): Boolean {
        return actionType in listOf(
            "PLAY_SOUND", "CONTROL_MEDIA", "START_MUSIC_PLAYER"
        )
    }

    /**
     * Validate a macro before saving (quick check for obvious issues)
     */
    fun validateMacroForSaving(macro: MacroDTO): ConflictCheckResult {
        val conflicts = mutableListOf<Conflict>()

        // Basic validation
        if (macro.triggers.isEmpty()) {
            conflicts.add(Conflict(
                type = ConflictType.CONSTRAINT_VIOLATION,
                severity = Severity.CRITICAL,
                description = "Macro '${macro.name}' has no triggers",
                affectedMacros = listOf(macro),
                recommendation = "Add at least one trigger to make the macro functional"
            ))
        }

        if (macro.actions.isEmpty()) {
            conflicts.add(Conflict(
                type = ConflictType.CONSTRAINT_VIOLATION,
                severity = Severity.CRITICAL,
                description = "Macro '${macro.name}' has no actions",
                affectedMacros = listOf(macro),
                recommendation = "Add at least one action for the macro to do something"
            ))
        }

        return ConflictCheckResult(
            hasConflicts = conflicts.isNotEmpty(),
            conflicts = conflicts,
            canProceed = conflicts.none { it.severity == Severity.CRITICAL }
        )
    }
}