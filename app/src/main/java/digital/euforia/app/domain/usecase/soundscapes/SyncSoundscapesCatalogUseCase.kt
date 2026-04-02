/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.domain.usecase.soundscapes

import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.domain.util.ResultWrapper
import javax.inject.Inject

class SyncSoundscapesCatalogUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    suspend operator fun invoke(): ResultWrapper<Unit> = repository.syncCatalog()
}

