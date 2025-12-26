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
import kotlin.math.abs
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
    
    // Orientation detection variables
    private var lastOrientation = -1
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        try {
            activeTriggers[trigger.id] = trigger
            updateSensorListeners()
        } catch (e: Exception) {
            Timber.e(e, "Failed to register sensor trigger ${trigger.id}")
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            activeTriggers.remove(triggerId)
            updateSensorListeners()
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister sensor trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        try {
            activeTriggers.clear()
            updateSensorListeners()
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear sensor triggers")
        }
    }

    private fun updateSensorListeners() {
        try {
            sensorManager.unregisterListener(this)
            
            val sensorTypesNeeded = mutableSetOf<Int>()
            activeTriggers.values.forEach { trigger ->
                when (trigger.triggerConfig["sensor"]) {
                    "SHAKE" -> sensorTypesNeeded.add(Sensor.TYPE_ACCELEROMETER)
                    "PROXIMITY" -> sensorTypesNeeded.add(Sensor.TYPE_PROXIMITY)
                    "LIGHT_LEVEL" -> sensorTypesNeeded.add(Sensor.TYPE_LIGHT)
                    "ORIENTATION_CHANGE" -> {
                        sensorTypesNeeded.add(Sensor.TYPE_ACCELEROMETER)
                        sensorTypesNeeded.add(Sensor.TYPE_MAGNETIC_FIELD)
                    }
                }
            }

            sensorTypesNeeded.forEach { sensorType ->
                try {
                    val sensor = sensorManager.getDefaultSensor(sensorType)
                    if (sensor != null) {
                        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                    } else {
                        Timber.w("Sensor type $sensorType not available on this device")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to register sensor listener for type $sensorType")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update sensor listeners")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                handleAccelerometer(event)
                handleOrientationChange(event)
            }
            Sensor.TYPE_PROXIMITY -> handleProximity(event)
            Sensor.TYPE_LIGHT -> handleLightLevel(event)
            Sensor.TYPE_MAGNETIC_FIELD -> handleMagnetometer(event)
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

    private fun handleLightLevel(event: SensorEvent) {
        val lightLevel = event.values[0]
        Timber.d("Light level: $lightLevel lux")
        
        activeTriggers.values
            .filter { it.triggerConfig["sensor"] == "LIGHT_LEVEL" }
            .forEach { trigger ->
                val threshold = trigger.triggerConfig["threshold"]?.toString()?.toFloatOrNull()
                val operator = trigger.triggerConfig["operator"]?.toString() ?: "above"
                
                if (threshold != null) {
                    val shouldTrigger = when (operator.lowercase()) {
                        "above", "greater_than" -> lightLevel > threshold
                        "below", "less_than" -> lightLevel < threshold
                        "equals" -> abs(lightLevel - threshold) < 1.0f
                        else -> false
                    }
                    
                    if (shouldTrigger) {
                        notifyTriggers("LIGHT_LEVEL", mapOf("level" to lightLevel, "threshold" to threshold))
                    }
                }
            }
    }

    private fun handleMagnetometer(event: SensorEvent) {
        System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        handleOrientationChange(null)
    }

    private fun handleOrientationChange(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        }
        
        // Need both accelerometer and magnetometer readings
        if (accelerometerReading.all { it == 0f } || magnetometerReading.all { it == 0f }) {
            return
        }
        
        val success = SensorManager.getRotationMatrix(
            rotationMatrix, null,
            accelerometerReading, magnetometerReading
        )
        
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toInt()
            
            // Determine orientation (0=North, 90=East, 180=South, 270=West)
            val orientation = when {
                azimuth in -45..45 -> "NORTH"
                azimuth in 45..135 -> "EAST"
                azimuth in 135..225 || azimuth in -225..-135 -> "SOUTH"
                else -> "WEST"
            }
            
            if (lastOrientation != azimuth) {
                lastOrientation = azimuth
                Timber.d("Orientation changed: $orientation (azimuth: $azimuth)")
                notifyTriggers("ORIENTATION_CHANGE", mapOf("orientation" to orientation, "azimuth" to azimuth))
            }
        }
    }

    private fun notifyTriggers(sensorSubType: String, data: Map<String, Any> = emptyMap()) {
        scope.launch {
            try {
                activeTriggers.values.filter { it.triggerConfig["sensor"] == sensorSubType }
                    .forEach { trigger ->
                        try {
                            checkTriggersUseCase(type, data + mapOf("fired_trigger_id" to trigger.id))
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to check trigger ${trigger.id} for sensor $sensorSubType")
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to notify triggers for sensor $sensorSubType")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
