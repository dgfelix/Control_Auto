package br.com.sensorauto.domain.usecase

import android.view.Surface
import br.com.sensorauto.domain.model.Orientation
import kotlin.math.atan2
import kotlin.math.sqrt

class ComputeOrientationUseCase {
    operator fun invoke(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        orientation: Orientation,
        displayRotation: Int = Surface.ROTATION_0,
    ): Pair<Float, Float> {
        val (x, y) = when (displayRotation) {
            Surface.ROTATION_90 -> Pair(-accelY, accelX)
            Surface.ROTATION_180 -> Pair(-accelX, -accelY)
            Surface.ROTATION_270 -> Pair(accelY, -accelX)
            else -> Pair(accelX, accelY)
        }

        val radToDeg = 180.0f / Math.PI.toFloat()
        return when (orientation) {
            Orientation.HORIZONTAL -> {
                val roll = atan2(y, sqrt((x * x) + (accelZ * accelZ))) * radToDeg
                val pitch = atan2(-x, sqrt((y * y) + (accelZ * accelZ))) * radToDeg
                Pair(pitch, roll)
            }
            Orientation.VERTICAL -> {
                val roll = atan2(x, sqrt((y * y) + (accelZ * accelZ))) * radToDeg
                val pitch = atan2(-y, sqrt((x * x) + (accelZ * accelZ))) * radToDeg
                Pair(pitch, roll)
            }
        }
    }
}
