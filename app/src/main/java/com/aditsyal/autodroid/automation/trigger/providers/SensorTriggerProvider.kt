package com.aditsyal.autodroid.automation.trigger.providers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.aditsyal.autodroid.automation.trigger.TriggerProvider
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class SensorTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider, SensorEventListener {

    override val type: String = "SENSOR_EVENT"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val activeTriggers = mutableMapOf<Long, TriggerDTO>()
    
    // Shake detection variables
    private var lastAcceleration = SensorManager.GRAVITY_EARTH
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var shakeThreshold = 12f

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        activeTriggers[trigger.id] = trigger
        updateSensorListeners()
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        activeTriggers.remove(triggerId)
        updateSensorListeners()
    }

    override suspend fun clearTriggers() {
        activeTriggers.clear()
        updateSensorListeners()
    }

    private fun updateSensorListeners() {
        sensorManager.unregisterListener(this)
        
        val sensorTypesNeeded = mutableSetOf<Int>()
        activeTriggers.values.forEach { trigger ->
            when (trigger.triggerConfig["sensor"]) {
                "SHAKE" -> sensorTypesNeeded.add(Sensor.TYPE_ACCELEROMETER)
                "PROXIMITY" -> sensorTypesNeeded.add(Sensor.TYPE_PROXIMITY)
            }
        }

        sensorTypesNeeded.forEach { sensorType ->
            val sensor = sensorManager.getDefaultSensor(sensorType)
            if (sensor != null) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
            Sensor.TYPE_PROXIMITY -> handleProximity(event)
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt(x * x + y * y + z * z)
        val delta = currentAcceleration - lastAcceleration
        
        if (delta > shakeThreshold) {
            Timber.d("Shake detected!")
            notifyTriggers("SHAKE")
        }
    }

    private fun handleProximity(event: SensorEvent) {
        val distance = event.values[0]
        val isNear = distance < event.sensor.maximumRange
        val state = if (isNear) "NEAR" else "FAR"
        Timber.d("Proximity changed: $state")
        notifyTriggers("PROXIMITY", mapOf("state" to state))
    }

    private fun notifyTriggers(sensorSubType: String, data: Map<String, Any> = emptyMap()) {
        scope.launch {
            activeTriggers.values.filter { it.triggerConfig["sensor"] == sensorSubType }
                .forEach { trigger ->
                    checkTriggersUseCase(type, data + mapOf("fired_trigger_id" to trigger.id))
                }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
