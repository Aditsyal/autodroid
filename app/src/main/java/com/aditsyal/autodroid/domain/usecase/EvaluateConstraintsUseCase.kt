package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.ConstraintDTO
import javax.inject.Inject
import timber.log.Timber

class EvaluateConstraintsUseCase @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) {
    operator fun invoke(constraints: List<ConstraintDTO>): Boolean {
        if (constraints.isEmpty()) return true
        
        Timber.d("Evaluating ${constraints.size} constraints")
        return constraints.all { isConstraintSatisfied(it) }
    }

    private fun isConstraintSatisfied(constraint: ConstraintDTO): Boolean {
        return when (constraint.constraintType) {
            "AIRPLANE_MODE" -> checkAirplaneMode(constraint.constraintConfig)
            "BATTERY_LEVEL" -> checkBatteryLevel(constraint.constraintConfig)
            else -> {
                Timber.w("Unknown constraint type: ${constraint.constraintType}")
                true // Default to true for unsupported constraints to avoid unintended blocking
            }
        }
    }

    private fun checkAirplaneMode(config: Map<String, Any>): Boolean {
        val expected = config["enabled"] as? Boolean ?: return true
        val actual = android.provider.Settings.Global.getInt(
            context.contentResolver,
            android.provider.Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
        return actual == expected
    }

    private fun checkBatteryLevel(config: Map<String, Any>): Boolean {
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        if (level == -1 || scale == -1) return true
        val batteryPct = (level * 100 / scale.toFloat()).toInt()
        
        return compareValues(batteryPct, config)
    }

    private fun compareValues(actual: Int, config: Map<String, Any>): Boolean {
        val operator = config["operator"]?.toString() ?: "equals"
        val expected = config["value"]?.toString()?.toIntOrNull() ?: return true
        
        return when (operator) {
            "greater_than" -> actual > expected
            "less_than" -> actual < expected
            "equals" -> actual == expected
            "not_equals" -> actual != expected
            else -> actual == expected
        }
    }
}
