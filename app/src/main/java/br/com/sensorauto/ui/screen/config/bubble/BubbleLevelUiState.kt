package br.com.sensorauto.ui.screen.config.bubble

data class BubbleLevelUiState(
    val pitchDeg: Float = 0f,
    val rollDeg: Float = 0f,
    val isLevel: Boolean = false,
    val toleranceDeg: Float = 2f,
    val offsetPitch: Float = 0f,
    val offsetRoll: Float = 0f
)