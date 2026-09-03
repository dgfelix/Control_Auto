package br.com.sensorauto.ui.screen.start

data class StartUiState(
    val sessionName: String = "",
    val elapsedMs: Long = 0L,
    val distanceKm: Float = 0f,
    val speedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val batteryPercent: Int = 0,
    val accelSamples: List<FloatArray> = emptyList(),
    val gyroSamples: List<FloatArray> = emptyList(),
    val status: StartStatus = StartStatus.IDLE,
    val dataPoints: Int = 0,
    val sessionStartMs: Long = 0L,
    val totalSpeedSum: Float = 0f,
    val speedReadings: Int = 0
)
