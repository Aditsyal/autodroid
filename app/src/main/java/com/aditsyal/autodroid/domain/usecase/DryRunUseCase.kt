package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.usecase.executors.ActionExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Use case for simulating macro execution (dry run) without actually executing actions
 * Provides step-by-step preview and impact estimation
 */
class DryRunUseCase @Inject constructor(
    private val evaluateConstraintsUseCase: EvaluateConstraintsUseCase
) {

    private data class StepInfo(
        val title: String,
        val description: String,
        val duration: kotlin.time.Duration,
        val battery: Float,
        val probability: Float,
        val warnings: List<String>
    )

    data class DryRunStep(
        val stepNumber: Int,
        val type: StepType,
        val title: String,
        val description: String,
        val estimatedDuration: Duration,
        val batteryImpact: Float, // percentage points
        val successProbability: Float, // 0.0 to 1.0
        val warnings: List<String> = emptyList(),
        val action: Any? = null // TriggerDTO, ActionDTO, or ConstraintDTO
    )

    enum class StepType {
        TRIGGER_CHECK,
        CONSTRAINT_EVALUATION,
        ACTION_EXECUTION,
        DELAY
    }

    data class DryRunResult(
        val macro: MacroDTO,
        val steps: List<DryRunStep>,
        val totalEstimatedDuration: Duration,
        val totalBatteryImpact: Float,
        val overallSuccessProbability: Float,
        val blockingIssues: List<String>
    )

    /**
     * Simulate macro execution and return detailed step-by-step breakdown
     */
    fun simulateMacro(macro: MacroDTO): Flow<DryRunResult> = flow {
        val steps = mutableListOf<DryRunStep>()
        var stepNumber = 1
        val blockingIssues = mutableListOf<String>()

        // Step 1: Trigger evaluation
        for (trigger in macro.triggers) {
            val triggerStep = createTriggerStep(trigger, stepNumber++)
            steps.add(triggerStep)
        }

        // Step 2: Constraint evaluation
        for (constraint in macro.constraints) {
            val constraintStep = createConstraintStep(constraint, stepNumber++)
            steps.add(constraintStep)

            // Check if constraint would block execution
            if (!wouldConstraintPass(constraint)) {
                blockingIssues.add("Constraint '${constraint.constraintType}' may prevent execution")
            }
        }

        // Step 3: Action execution simulation
        for (action in macro.actions.sortedBy { it.executionOrder }) {
            val actionStep = createActionStep(action, stepNumber++)
            steps.add(actionStep)

            // Add delay step if there's a delay after this action
            if (action.delayAfter > 0) {
                val delayStep = DryRunStep(
                    stepNumber = stepNumber++,
                    type = StepType.DELAY,
                    title = "Wait ${action.delayAfter}ms",
                    description = "Delay after action execution",
                    estimatedDuration = action.delayAfter.toDuration(DurationUnit.MILLISECONDS),
                    batteryImpact = 0.001f, // Minimal battery impact for waiting
                    successProbability = 1.0f,
                    action = action
                )
                steps.add(delayStep)
            }
        }

        val totalDuration = steps.sumOf { it.estimatedDuration.inWholeMilliseconds }.toDuration(DurationUnit.MILLISECONDS)
        val totalBattery = steps.sumOf { it.batteryImpact.toDouble() }.toFloat()
        val successProbability = calculateOverallSuccessProbability(steps)

        val result = DryRunResult(
            macro = macro,
            steps = steps,
            totalEstimatedDuration = totalDuration,
            totalBatteryImpact = totalBattery,
            overallSuccessProbability = successProbability,
            blockingIssues = blockingIssues
        )

        emit(result)
    }

    private fun createTriggerStep(trigger: TriggerDTO, stepNumber: Int): DryRunStep {
        val stepInfo = when (trigger.triggerType) {
            "TIME" -> {
                val timeConfig = trigger.triggerConfig as? Map<*, *>
                val timeString = timeConfig?.get("time") as? String ?: "Unknown"
                StepInfo(
                    title = "Time Trigger",
                    description = "Macro will trigger at $timeString",
                    duration = 10.toDuration(DurationUnit.MILLISECONDS), // Minimal time for alarm setup
                    battery = 0.001f,
                    probability = 0.99f,
                    warnings = emptyList()
                )
            }
            "APP_EVENT" -> {
                val appConfig = trigger.triggerConfig as? Map<*, *>
                val eventType = appConfig?.get("eventType") as? String ?: "Unknown"
                val appName = appConfig?.get("packageName") as? String ?: "Unknown app"
                StepInfo(
                    title = "App Event Trigger",
                    description = "Macro will trigger on $eventType for $appName",
                    duration = 50.toDuration(DurationUnit.MILLISECONDS), // Broadcast receiver setup
                    battery = 0.002f,
                    probability = 0.95f,
                    warnings = listOf("Requires notification access permission")
                )
            }
            "LOCATION" -> {
                StepInfo(
                    title = "Location Trigger",
                    description = "Macro will trigger based on location changes",
                    duration = 100.toDuration(DurationUnit.MILLISECONDS), // GPS/geofence setup
                    battery = 0.01f, // GPS can be battery intensive
                    probability = 0.90f,
                    warnings = listOf("Requires location permission", "May drain battery due to GPS usage")
                )
            }
            else -> {
                StepInfo(
                    title = "${trigger.triggerType} Trigger",
                    description = "Custom trigger configuration",
                    duration = 25.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.001f,
                    probability = 0.95f,
                    warnings = emptyList()
                )
            }
        }

        return DryRunStep(
            stepNumber = stepNumber,
            type = StepType.TRIGGER_CHECK,
            title = stepInfo.title,
            description = stepInfo.description,
            estimatedDuration = stepInfo.duration,
            batteryImpact = stepInfo.battery,
            successProbability = stepInfo.probability,
            warnings = stepInfo.warnings,
            action = trigger
        )
    }

    private fun createConstraintStep(constraint: ConstraintDTO, stepNumber: Int): DryRunStep {
        val stepInfo = when (constraint.constraintType) {
            "BATTERY_LEVEL" -> {
                val batteryConfig = constraint.constraintConfig as? Map<*, *>
                val minLevel = batteryConfig?.get("minLevel") as? Number ?: 0
                StepInfo(
                    title = "Battery Level Check",
                    description = "Requires battery level ≥ ${minLevel}%",
                    duration = 5.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.0001f,
                    probability = 0.98f,
                    warnings = emptyList()
                )
            }
            "WIFI_CONNECTED" -> {
                StepInfo(
                    title = "WiFi Connection Check",
                    description = "Requires active WiFi connection",
                    duration = 10.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.0005f,
                    probability = 0.95f,
                    warnings = listOf("May fail if WiFi is unstable")
                )
            }
            "BLUETOOTH_ENABLED" -> {
                StepInfo(
                    title = "Bluetooth Check",
                    description = "Requires Bluetooth to be enabled",
                    duration = 8.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.0003f,
                    probability = 0.97f,
                    warnings = emptyList()
                )
            }
            else -> {
                StepInfo(
                    title = "${constraint.constraintType} Check",
                    description = "Custom constraint evaluation",
                    duration = 5.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.0002f,
                    probability = 0.95f,
                    warnings = emptyList()
                )
            }
        }

        return DryRunStep(
            stepNumber = stepNumber,
            type = StepType.CONSTRAINT_EVALUATION,
            title = stepInfo.title,
            description = stepInfo.description,
            estimatedDuration = stepInfo.duration,
            batteryImpact = stepInfo.battery,
            successProbability = stepInfo.probability,
            warnings = stepInfo.warnings,
            action = constraint
        )
    }

    private fun createActionStep(action: ActionDTO, stepNumber: Int): DryRunStep {
        val stepInfo = when (action.actionType) {
            "LAUNCH_APP" -> {
                val appConfig = action.actionConfig as? Map<*, *>
                val appName = appConfig?.get("appName") as? String ?: "Unknown app"
                StepInfo(
                    title = "Launch App",
                    description = "Open $appName",
                    duration = 500.toDuration(DurationUnit.MILLISECONDS), // App launch time
                    battery = 0.005f,
                    probability = 0.92f,
                    warnings = listOf("App must be installed", "May require additional permissions")
                )
            }
            "SEND_NOTIFICATION" -> {
                StepInfo(
                    title = "Send Notification",
                    description = "Display notification to user",
                    duration = 50.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.001f,
                    probability = 0.99f,
                    warnings = listOf("Requires notification permission")
                )
            }
            "SET_BRIGHTNESS" -> {
                StepInfo(
                    title = "Adjust Brightness",
                    description = "Change screen brightness level",
                    duration = 100.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.002f,
                    probability = 0.95f,
                    warnings = listOf("Requires system settings permission")
                )
            }
            "PLAY_SOUND" -> {
                StepInfo(
                    title = "Play Sound",
                    description = "Play audio notification",
                    duration = 200.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.003f,
                    probability = 0.97f,
                    warnings = emptyList()
                )
            }
            "HTTP_REQUEST" -> {
                StepInfo(
                    title = "HTTP Request",
                    description = "Make network request",
                    duration = 1000.toDuration(DurationUnit.MILLISECONDS), // Network request
                    battery = 0.01f, // Network operations use battery
                    probability = 0.85f,
                    warnings = listOf("Requires internet connection", "May have network latency")
                )
            }
            "SET_VOLUME" -> {
                StepInfo(
                    title = "Adjust Volume",
                    description = "Change audio volume level",
                    duration = 50.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.001f,
                    probability = 0.98f,
                    warnings = emptyList()
                )
            }
            else -> {
                StepInfo(
                    title = "${action.actionType}",
                    description = "Execute ${action.actionType.lowercase()} action",
                    duration = 100.toDuration(DurationUnit.MILLISECONDS),
                    battery = 0.002f,
                    probability = 0.90f,
                    warnings = listOf("Action may require specific permissions")
                )
            }
        }

        return DryRunStep(
            stepNumber = stepNumber,
            type = StepType.ACTION_EXECUTION,
            title = stepInfo.title,
            description = stepInfo.description,
            estimatedDuration = stepInfo.duration,
            batteryImpact = stepInfo.battery,
            successProbability = stepInfo.probability,
            warnings = stepInfo.warnings,
            action = action
        )
    }

    private fun wouldConstraintPass(constraint: ConstraintDTO): Boolean {
        // This is a simplified check - in real implementation, this would
        // use actual system state or mock data
        return when (constraint.constraintType) {
            "BATTERY_LEVEL" -> true // Assume battery is sufficient in dry run
            "WIFI_CONNECTED" -> true // Assume WiFi is available
            "BLUETOOTH_ENABLED" -> true // Assume Bluetooth is enabled
            else -> true // Assume other constraints pass
        }
    }

    private fun calculateOverallSuccessProbability(steps: List<DryRunStep>): Float {
        if (steps.isEmpty()) return 1.0f

        // Calculate combined probability using product of individual probabilities
        // This gives a realistic assessment of overall success chance
        return steps.fold(1.0f) { acc, step ->
            acc * step.successProbability
        }
    }
}