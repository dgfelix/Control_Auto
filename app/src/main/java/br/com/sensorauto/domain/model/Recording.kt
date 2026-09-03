package br.com.sensorauto.domain.model

data class Recording(
    val id: Long,
    val name: String,
    val dateMillis: Long,
    val distanceKm: Float,
    val durationMs: Long,
    val avgSpeedKmh: Float,
    val maxSpeedKmh: Float,
    val dataPoints: Int,
    val deviceId: String
)
