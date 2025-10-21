package digital.euforia.app.domain.usecase.app_settings

import digital.euforia.app.data.repository.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncAppSettingsUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) { appSettingsRepository.sync() }
}