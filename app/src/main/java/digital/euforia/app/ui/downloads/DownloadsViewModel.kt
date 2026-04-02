package digital.euforia.app.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.domain.usecase.soundscapes.GetSoundscapeDownloadsFlowUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val getSoundscapeDownloadsFlowUseCase: GetSoundscapeDownloadsFlowUseCase
) : ViewModel(), ContainerHost<DownloadsState, DownloadsSideEffect> {
    override val container = container<DownloadsState, DownloadsSideEffect>(
        initialState = DownloadsState(),
        onCreate = {
            observeDownloads()
        }
    )

    private fun observeDownloads() {
        viewModelScope.launch {
            getSoundscapeDownloadsFlowUseCase().collectLatest { downloads ->
                intent {
                    reduce { state.copy(downloads = downloads) }
                }
            }
        }
    }
}

data class DownloadsState(
    val errorMessage: String? = null,
    val downloads: List<SoundscapeDownloadItem> = emptyList()
)

sealed class DownloadsSideEffect {}