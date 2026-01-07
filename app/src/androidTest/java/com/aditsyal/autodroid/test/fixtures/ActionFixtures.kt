package com.aditsyal.autodroid.test.fixtures

import com.aditsyal.autodroid.data.models.ActionDTO

object ActionFixtures {
    
    fun createToastAction(
        message: String = "Test message",
        executionOrder: Int = 1
    ): ActionDTO {
        return ActionDTO(
            actionType = "SHOW_TOAST",
            actionConfig = mapOf("message" to message),
            executionOrder = executionOrder
        )
    }
    
    fun createWifiToggleAction(
        enabled: Boolean = true,
        executionOrder: Int = 1
    ): ActionDTO {
        return ActionDTO(
            actionType = "WIFI_TOGGLE",
            actionConfig = mapOf("enabled" to enabled),
            executionOrder = executionOrder
        )
    }
    
    fun createBrightnessAction(
        brightness: Int = 50,
        executionOrder: Int = 1
    ): ActionDTO {
        return ActionDTO(
            actionType = "SET_BRIGHTNESS",
            actionConfig = mapOf("brightness" to brightness),
            executionOrder = executionOrder
        )
    }
    
    fun createDelayAction(
        delayMs: Long = 1000L,
        executionOrder: Int = 1
    ): ActionDTO {
        return ActionDTO(
            actionType = "DELAY",
            actionConfig = mapOf("delayMs" to delayMs),
            executionOrder = executionOrder,
            delayAfter = 0
        )
    }
    
    fun createNotificationAction(
        title: String = "Test Title",
        message: String = "Test Message",
        executionOrder: Int = 1
    ): ActionDTO {
        return ActionDTO(
            actionType = "NOTIFICATION",
            actionConfig = mapOf(
                "title" to title,
                "message" to message
            ),
            executionOrder = executionOrder
        )
    }
    
    fun createVariableSetAction(
        variableName: String = "testVar",
        value: String = "testValue",
        scope: String = "GLOBAL",
        executionOrder: Int = 1
    ): ActionDTO {
        return ActionDTO(
            actionType = "SET_VARIABLE",
            actionConfig = mapOf(
                "variableName" to variableName,
                "value" to value,
                "scope" to scope
            ),
            executionOrder = executionOrder
        )
    }
}


