/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.domain.usecase.soundscapes

import digital.euforia.app.data.db.entity.SoundscapePreset
import digital.euforia.app.data.repository.SoundscapesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSoundscapePresetsFlowUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    operator fun invoke(): Flow<List<SoundscapePreset>> = repository.getPresetsFlow()
}

class SaveSoundscapePresetUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    suspend operator fun invoke(preset: SoundscapePreset) = repository.upsertPreset(preset)
}

class DeleteSoundscapePresetUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    suspend operator fun invoke(id: Int) = repository.deletePreset(id)
}

