package com.example.expensetracker.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTime: Long = 0

    companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 1.5f
        private const val SHAKE_SLOP_TIME_MS = 500
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
            Log.d("ShakeDetector", "Shake detection started")
        } ?: run {
            Log.e("ShakeDetector", "Accelerometer not available")
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d("ShakeDetector", "Shake detection stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            Log.d("ShakeDetector", "gForce = $gForce")
            val currentTime = System.currentTimeMillis()

            if (lastShakeTime + SHAKE_SLOP_TIME_MS < currentTime) {
                lastShakeTime = currentTime
                Log.d("ShakeDetector", "Shake detected! G-Force: $gForce")
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}