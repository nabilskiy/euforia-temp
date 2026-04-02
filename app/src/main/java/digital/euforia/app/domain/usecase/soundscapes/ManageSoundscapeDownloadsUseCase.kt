/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.domain.usecase.soundscapes

import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.repository.SoundscapesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSoundscapeDownloadsFlowUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    operator fun invoke(): Flow<List<SoundscapeDownloadItem>> = repository.getDownloadsFlow()
}

class QueueSoundscapeDownloadUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    suspend operator fun invoke(item: SoundscapeDownloadItem) = repository.queueDownload(item)
}

