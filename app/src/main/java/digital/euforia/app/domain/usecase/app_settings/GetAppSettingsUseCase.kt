package digital.euforia.app.domain.usecase.app_settings

import digital.euforia.app.data.repository.AppSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAppSettingsUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) { appSettingsRepository.get()}
}