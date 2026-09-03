package br.com.sensorauto.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateMillis: Long,
    val distanceKm: Float,
    val durationMs: Long,
    val avgSpeedKmh: Float,
    val maxSpeedKmh: Float,
    val dataPoints: Int,
    val deviceId: String
)
