package digital.euforia.app.ui.settings.language

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.onboarding.Language
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel(),
    ContainerHost<LanguageState, LanguageSideEffect> {
    override val container = container<LanguageState, LanguageSideEffect>(
        initialState = LanguageState(),
        onCreate = {
            loadLanguage()
        }
    )

    private fun loadLanguage() {
        intent {
            val lang = appPreferences.getLanguage()
            reduce {
                state.copy(
                    language = lang
                )
            }
        }
    }

    fun onLanguageChanged(language: Language) {
        intent {
            appPreferences.setLanguage(language.tag)
            reduce {
                state.copy(
                    language = language.tag
                )
            }
        }
    }
}

data class LanguageState(
    val errorMessage: String? = null,
    val language: String? = null
)

sealed class LanguageSideEffect {}