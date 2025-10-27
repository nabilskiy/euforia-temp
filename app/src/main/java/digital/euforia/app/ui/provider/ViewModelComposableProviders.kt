package digital.euforia.app.ui.provider

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.ui.player.audio.AudioPlayerViewModel

@Composable
fun audioPlayerViewModel(
    accompanimentId: Int,
    timeOfDay: TimeOfDay,
): AudioPlayerViewModel {
    // NavArgs are injected into the ViewModel via SavedStateHandle; no explicit factory needed
    return hiltViewModel()
}