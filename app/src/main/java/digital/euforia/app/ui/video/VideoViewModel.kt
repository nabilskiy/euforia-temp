package digital.euforia.app.ui.video

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.R
import digital.euforia.app.ui.util.BackgroundPlayerHelper
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    @ApplicationContext val context: android.content.Context,

    ) : ViewModel(),
    ContainerHost<VideoState, VideoSideEffect> {
    override val container = container<VideoState, VideoSideEffect>(
        initialState = VideoState(),
        onCreate = {
            BackgroundPlayerHelper.playLooping(
                context = context,
                soundRes = R.raw.bgm_intro
            )
        }
    )
}

data class VideoState(
    val isLoading: Boolean = false
)

sealed class VideoSideEffect {
    data object NavigateToOnboarding : VideoSideEffect()
}