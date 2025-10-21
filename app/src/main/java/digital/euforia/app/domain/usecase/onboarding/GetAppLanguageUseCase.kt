package digital.euforia.app.domain.usecase.onboarding

import digital.euforia.app.data.store.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class GetAppLanguageUseCase @Inject constructor(
    private val appPreferences: AppPreferences
) {
    suspend operator fun invoke(): String {
        return withContext(Dispatchers.IO) {
            appPreferences.getLanguage() ?: Locale.getDefault().language
        }
    }
}