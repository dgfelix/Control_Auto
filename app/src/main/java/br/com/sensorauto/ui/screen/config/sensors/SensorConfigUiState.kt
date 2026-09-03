package br.com.sensorauto.ui.screen.config.sensors

import br.com.sensorauto.domain.model.SensorType

data class SensorConfigUiState(
    val sensors: List<SensorConfigItem> = emptyList(),
    val isSaving: Boolean = false
)

data class SensorConfigItem(
    val type: SensorType,
    val label: String,
    val description: String,
    val isAvailable: Boolean,
    val isEnabled: Boolean,
    val frequencyHz: Int,
    val freqRange: IntRange,
    val isIntervalSensor: Boolean = false
)
