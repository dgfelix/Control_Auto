package br.com.sensorauto.ui.screen.config.sensors

import androidx.annotation.StringRes
import br.com.sensorauto.domain.model.SensorType

data class SensorConfigUiState(
    val sensors: List<SensorConfigItem> = emptyList(),
    val isSaving: Boolean = false
)

data class SensorConfigItem(
    val type: SensorType,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val isAvailable: Boolean,
    val isEnabled: Boolean,
    val frequencyHz: Int,
    val freqRange: IntRange,
    val isIntervalSensor: Boolean = false
)
