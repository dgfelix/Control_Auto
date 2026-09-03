package br.com.sensorauto.domain.usecase

import br.com.sensorauto.domain.model.Recording
import br.com.sensorauto.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow

class ObserveRecordingsUseCase(private val repository: RecordingRepository) {
    operator fun invoke(): Flow<List<Recording>> = repository.observeAll()
}
