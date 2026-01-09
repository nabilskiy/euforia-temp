package digital.euforia.app.ui.settings.voice

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.onboarding.Gender
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val profilePreferences: ProfilePreferences
) : ViewModel(),
    ContainerHost<VoiceState, VoiceSideEffect> {
    override val container = container<VoiceState, VoiceSideEffect>(
        initialState = VoiceState(),
        onCreate = {
            loadGender()
        }
    )

    private fun loadGender() {
        intent {
            val gender = profilePreferences.getGender()
            reduce { state.copy(gender = gender) }
        }
    }

    fun onGenderSelected(gender: Gender) {
        intent {
            profilePreferences.setGender(gender)
            reduce { state.copy(gender = gender) }
        }
    }
}

data class VoiceState(
    val errorMessage: String? = null,
    val gender: Gender? = null
)

sealed class VoiceSideEffect {}