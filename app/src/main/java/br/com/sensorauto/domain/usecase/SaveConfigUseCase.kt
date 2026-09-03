package br.com.sensorauto.domain.usecase

import br.com.sensorauto.domain.model.AppConfig
import br.com.sensorauto.domain.repository.ConfigRepository

class SaveConfigUseCase(private val repository: ConfigRepository) {
    suspend operator fun invoke(config: AppConfig) = repository.save(config)
}
