package br.com.sensorauto.domain.usecase

import br.com.sensorauto.domain.repository.RecordingRepository

class DeleteRecordingUseCase(private val repository: RecordingRepository) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
