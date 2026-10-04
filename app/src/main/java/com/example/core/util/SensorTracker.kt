package com.example.core.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Real-time Hardware Sensor Tracker for:
 * 1. Compass Azimuth (Magnetic / Orientation)
 * 2. Elevation Pitch & Roll (Bubble Level & Inclinometer)
 */
class SensorTracker(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val rotationVector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private val _hasSensors = MutableStateFlow(true)
    val hasSensors: StateFlow<Boolean> = _hasSensors.asStateFlow()

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var hasGravity = false
    private var hasGeomagnetic = false

    fun startListening() {
        val hasAny = (accelerometer != null && magneticField != null) || rotationVector != null
        _hasSensors.value = hasAny

        if (rotationVector != null) {
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            magneticField?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            updateAngles(orientationAngles)
            return
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Low-pass filter for smooth readings
            for (i in 0..2) {
                gravity[i] = gravity[i] * 0.8f + event.values[i] * 0.2f
            }
            hasGravity = true
        }

        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            for (i in 0..2) {
                geomagnetic[i] = geomagnetic[i] * 0.8f + event.values[i] * 0.2f
            }
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                updateAngles(orientationAngles)
            }
        }
    }

    private fun updateAngles(angles: FloatArray) {
        var azDeg = Math.toDegrees(angles[0].toDouble()).toFloat()
        if (azDeg < 0) azDeg += 360f

        val pitchDeg = Math.toDegrees(angles[1].toDouble()).toFloat()
        val rollDeg = Math.toDegrees(angles[2].toDouble()).toFloat()

        _azimuth.value = azDeg
        _pitch.value = pitchDeg
        _roll.value = rollDeg
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Calibration notice can be observed if needed
    }
}
