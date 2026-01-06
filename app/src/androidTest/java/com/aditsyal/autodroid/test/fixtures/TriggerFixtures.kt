package com.aditsyal.autodroid.test.fixtures

import com.aditsyal.autodroid.data.models.TriggerDTO

object TriggerFixtures {
    
    fun createTimeTrigger(
        macroId: Long = 0,
        time: String = "12:00"
    ): TriggerDTO {
        return TriggerDTO(
            macroId = macroId,
            triggerType = "TIME",
            triggerConfig = mapOf(
                "subType" to "SPECIFIC_TIME",
                "time" to time
            )
        )
    }
    
    fun createBatteryTrigger(
        macroId: Long = 0,
        level: Int = 20,
        operator: String = "less_than"
    ): TriggerDTO {
        return TriggerDTO(
            macroId = macroId,
            triggerType = "BATTERY",
            triggerConfig = mapOf(
                "level" to mapOf(
                    "operator" to operator,
                    "value" to level
                )
            )
        )
    }
    
    fun createLocationTrigger(
        macroId: Long = 0,
        latitude: Double = 37.7749,
        longitude: Double = -122.4194,
        radius: Float = 100f,
        transitionType: String = "ENTER"
    ): TriggerDTO {
        return TriggerDTO(
            macroId = macroId,
            triggerType = "LOCATION",
            triggerConfig = mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "radius" to radius,
                "transitionType" to transitionType
            )
        )
    }
    
    fun createDeviceStateTrigger(
        macroId: Long = 0,
        event: String = "SCREEN_ON"
    ): TriggerDTO {
        return TriggerDTO(
            macroId = macroId,
            triggerType = "DEVICE_STATE",
            triggerConfig = mapOf("event" to event)
        )
    }
    
    fun createConnectivityTrigger(
        macroId: Long = 0,
        event: String = "WIFI_CONNECTED"
    ): TriggerDTO {
        return TriggerDTO(
            macroId = macroId,
            triggerType = "CONNECTIVITY",
            triggerConfig = mapOf("event" to event)
        )
    }
}

