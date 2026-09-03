package br.com.sensorauto.ui.screen.config.equipment

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sensorauto.domain.model.AppConfig
import br.com.sensorauto.domain.model.GearType
import br.com.sensorauto.domain.model.MotorType
import br.com.sensorauto.domain.model.Orientation
import br.com.sensorauto.domain.repository.ConfigRepository
import br.com.sensorauto.domain.usecase.SaveConfigUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EquipmentConfigViewModel(
    private val repository: ConfigRepository,
    private val saveConfigUseCase: SaveConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipmentConfigUiState())
    val uiState: StateFlow<EquipmentConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val config = repository.load()
            val withDevice = if (config.deviceBrand.isEmpty()) {
                config.copy(
                    deviceBrand = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
                    deviceModel = Build.MODEL
                )
            } else config
            _uiState.update { it.copy(config = withDevice) }
        }
    }

    private fun updateConfig(block: AppConfig.() -> AppConfig) {
        _uiState.update { it.copy(config = it.config.block()) }
    }

    fun updateVehicleBrand(value: String) = updateConfig { copy(vehicleBrand = value) }
    fun updateVehicleModel(value: String) = updateConfig { copy(vehicleModel = value) }
    fun updateVehicleYear(value: String) = updateConfig { copy(vehicleYear = value) }
    fun updateVehicleEngine(value: String) = updateConfig { copy(vehicleEngine = value) }
    fun updateGearType(value: GearType) = updateConfig { copy(gearType = value) }
    fun updateMotorType(value: MotorType) = updateConfig { copy(motorType = value) }
    fun updateDeviceBrand(value: String) = updateConfig { copy(deviceBrand = value) }
    fun updateDeviceModel(value: String) = updateConfig { copy(deviceModel = value) }
    fun updateDeviceYear(value: String) = updateConfig { copy(deviceYear = value) }
    fun updateOrientation(value: Orientation) = updateConfig { copy(mountOrientation = value) }
    fun updateSegmentMeters(value: Int) = updateConfig { copy(segmentMeters = value) }

    fun autoDetectDevice() = updateConfig {
        copy(
            deviceBrand = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            deviceModel = Build.MODEL
        )
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            saveConfigUseCase(_uiState.value.config)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
