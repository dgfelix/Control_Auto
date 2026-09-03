package br.com.sensorauto.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable data object ActiveSession   : Screen()
    @Serializable data object EquipmentConfig : Screen()
    @Serializable data object SensorConfig    : Screen()
    @Serializable data object RecordingFiles  : Screen()
    @Serializable data object SessionFiles    : Screen()
    @Serializable data class  RecordingDetail(val id: Long, val name: String) : Screen()
}
