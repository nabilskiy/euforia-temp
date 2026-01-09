package digital.euforia.app.ui.sos

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val configFetcher: EuforiaRemoteConfigFetcher,
    val analyticSender: AnalyticSender
) : ViewModel(),
    ContainerHost<EmergencyState, EmergencySideEffect> {
    override val container = container<EmergencyState, EmergencySideEffect>(
        initialState = EmergencyState(),
        onCreate = {
            analyticSender.sosShow()
            loadConfig()
        }
    )

    private fun loadConfig() {
        viewModelScope.launch {
            val video1Url = configFetcher.getSosOption1VideoUrl()
            val video2Url = configFetcher.getSosOption2VideoUrl()
            val videosList = listOf(
                EmergencyVideoUi(
                    titleRes = R.string.sos_option_1_title,
                    durationRes = R.string.sos_option_1_details,
                    descriptionRes = R.string.sos_option_1_text,
                    bgRes = R.drawable.img_bg_2_minutes,
                    videoUrl = video1Url.orEmpty(),
                    iconBgColor = Color(0xFF5B89E5)
                ),
                EmergencyVideoUi(
                    titleRes = R.string.sos_option_2_title,
                    durationRes = R.string.sos_option_2_details,
                    descriptionRes = R.string.sos_option_2_text,
                    bgRes = R.drawable.img_bg_5_minutes,
                    videoUrl = video2Url.orEmpty(),
                    iconBgColor = Color(0xFF3DC2F6)
                )
            )
            intent {
                reduce {
                    state.copy(
                        videos = videosList,
                    )
                }
            }
        }
    }
}

data class EmergencyState(
    val errorMessage: String? = null,
    val videos: List<EmergencyVideoUi> = emptyList(),
)

data class EmergencyVideoUi(
    @StringRes val titleRes: Int,
    @StringRes val durationRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val bgRes: Int,
    val videoUrl: String,
    val iconBgColor: Color
)

sealed class EmergencySideEffect {}