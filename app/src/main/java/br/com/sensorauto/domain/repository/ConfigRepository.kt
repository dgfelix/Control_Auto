package br.com.sensorauto.domain.repository

import br.com.sensorauto.domain.model.AppConfig

interface ConfigRepository {
    suspend fun load(): AppConfig
    suspend fun save(config: AppConfig)
}
