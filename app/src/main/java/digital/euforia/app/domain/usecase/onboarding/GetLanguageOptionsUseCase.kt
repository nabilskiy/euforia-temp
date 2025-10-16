package digital.euforia.app.domain.usecase.onboarding

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.domain.model.onboarding.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetLanguageOptionsUseCase @Inject constructor(
    private val remoteConfigFetcher: EuforiaRemoteConfigFetcher
) {
    suspend operator fun invoke(): List<Language> {
        return withContext(Dispatchers.IO) {
            val allowedLanguages = remoteConfigFetcher.getAllowedLanguages()
            Language.entries.filter { languageOption ->
                allowedLanguages.contains(languageOption.tag)
            }
        }
    }
}
