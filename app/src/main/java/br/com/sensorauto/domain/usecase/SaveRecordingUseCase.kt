package br.com.sensorauto.domain.usecase

import br.com.sensorauto.domain.model.Recording
import br.com.sensorauto.domain.repository.RecordingRepository

class SaveRecordingUseCase(private val repository: RecordingRepository) {
    suspend operator fun invoke(recording: Recording): Long = repository.save(recording)
}
