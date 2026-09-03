package br.com.sensorauto.ui.screen.config.equipment

import br.com.sensorauto.domain.model.AppConfig

data class EquipmentConfigUiState(
    val config: AppConfig = AppConfig(),
    val isSaving: Boolean = false
)
