package digital.euforia.app.domain.usecase.translation

import digital.euforia.app.data.store.AppPreferences
import javax.inject.Inject

class GetTranslationUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    suspend operator fun invoke(key: String, isFormatted: Boolean = true): String {
        val translationKey = if (isFormatted) {
            key.clearFormatting()
        } else key
        return appPreferences.getTranslationsMap()[translationKey].orEmpty()
    }

    private fun String.clearFormatting(): String {
        val rx = Regex("""\$\{rc\.strings\.([^}]+)\}""")
        val value = rx
            .find(this)
            ?.groupValues?.get(1)

        return value ?: this
    }
}