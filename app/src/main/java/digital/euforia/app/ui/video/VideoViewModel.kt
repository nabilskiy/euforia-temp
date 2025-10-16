package digital.euforia.app.ui.video

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor() : ViewModel(),
    ContainerHost<VideoState, VideoSideEffect> {
    override val container = container<VideoState, VideoSideEffect>(
        initialState = VideoState(),
        onCreate = { }
    )
}

data class VideoState(
    val isLoading: Boolean = false
)

sealed class VideoSideEffect {
    data object NavigateToOnboarding : VideoSideEffect()
}