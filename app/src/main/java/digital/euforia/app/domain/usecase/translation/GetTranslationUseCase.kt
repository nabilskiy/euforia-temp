package digital.euforia.app.domain.usecase.translation

import digital.euforia.app.data.store.AppPreferences
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

class GetTranslationUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    suspend operator fun invoke(key: String, isFormatted: Boolean = true): String {
        val prefix = "\${rc.strings."
        val translationKey = if (isFormatted) {
            key.clearFormatting()
        } else if (key.startsWith(prefix)) {
            key.replace(prefix, "").removeSuffix("}")
        } else  key
        return appPreferences.getTranslationsMap()[translationKey].orEmpty()
    }

    // Non-suspending variant to be safely called from Java code
    fun getNow(key: String, isFormatted: Boolean = true): String = runBlocking {
        invoke(key, isFormatted)
    }

    private fun String.clearFormatting(): String {
        val rx = Regex("""\$\{rc\.strings\.([^}]+)\}""")
        val value = rx
            .find(this)
            ?.groupValues?.get(1)

        return value ?: this
    }
}