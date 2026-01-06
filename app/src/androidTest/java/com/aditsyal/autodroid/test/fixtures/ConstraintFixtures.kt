package com.aditsyal.autodroid.test.fixtures

import com.aditsyal.autodroid.data.models.ConstraintDTO

object ConstraintFixtures {
    
    fun createBatteryLevelConstraint(
        operator: String = "greater_than",
        value: Int = 50
    ): ConstraintDTO {
        return ConstraintDTO(
            constraintType = "BATTERY_LEVEL",
            constraintConfig = mapOf(
                "operator" to operator,
                "value" to value
            )
        )
    }
    
    fun createTimeRangeConstraint(
        startTime: String = "09:00",
        endTime: String = "17:00"
    ): ConstraintDTO {
        return ConstraintDTO(
            constraintType = "TIME_RANGE",
            constraintConfig = mapOf(
                "startTime" to startTime,
                "endTime" to endTime
            )
        )
    }
    
    fun createDayOfWeekConstraint(
        days: List<String> = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
    ): ConstraintDTO {
        return ConstraintDTO(
            constraintType = "DAY_OF_WEEK",
            constraintConfig = mapOf("days" to days)
        )
    }
    
    fun createChargingStatusConstraint(
        isCharging: Boolean = true
    ): ConstraintDTO {
        return ConstraintDTO(
            constraintType = "CHARGING_STATUS",
            constraintConfig = mapOf("isCharging" to isCharging)
        )
    }
    
    fun createScreenStateConstraint(
        isOn: Boolean = true
    ): ConstraintDTO {
        return ConstraintDTO(
            constraintType = "SCREEN_STATE",
            constraintConfig = mapOf("isOn" to isOn)
        )
    }
    
    fun createGeofenceConstraint(
        latitude: Double = 37.7749,
        longitude: Double = -122.4194,
        radius: Float = 100f,
        inside: Boolean = true
    ): ConstraintDTO {
        return ConstraintDTO(
            constraintType = if (inside) "INSIDE_GEOFENCE" else "OUTSIDE_GEOFENCE",
            constraintConfig = mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "radius" to radius
            )
        )
    }
}

