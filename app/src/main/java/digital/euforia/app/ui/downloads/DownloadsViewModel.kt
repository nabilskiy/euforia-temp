/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.domain.usecase.soundscapes.ClearSoundscapeDownloadsUseCase
import digital.euforia.app.domain.usecase.soundscapes.GetSoundscapeDownloadCardsFlowUseCase
import digital.euforia.app.domain.usecase.soundscapes.SoundscapeDownloadCard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val getSoundscapeDownloadCardsFlowUseCase: GetSoundscapeDownloadCardsFlowUseCase,
    private val clearSoundscapeDownloadsUseCase: ClearSoundscapeDownloadsUseCase,
) : ViewModel(), ContainerHost<DownloadsState, DownloadsSideEffect> {
    override val container = container<DownloadsState, DownloadsSideEffect>(
        initialState = DownloadsState(),
        onCreate = {
            observeDownloads()
        }
    )

    private fun observeDownloads() {
        viewModelScope.launch {
            getSoundscapeDownloadCardsFlowUseCase().collectLatest { downloads ->
                intent {
                    reduce { state.copy(downloads = downloads) }
                }
            }
        }
    }

    fun onDownloadClick(item: SoundscapeDownloadCard) {
        viewModelScope.launch {
            intent {
                postSideEffect(DownloadsSideEffect.OpenScene(item.download.sceneId))
            }
        }
    }

    fun onClearAllDownloadsClick() {
        viewModelScope.launch {
            clearSoundscapeDownloadsUseCase()
        }
    }
}

data class DownloadsState(
    val errorMessage: String? = null,
    val downloads: List<SoundscapeDownloadCard> = emptyList()
)

sealed class DownloadsSideEffect {
    data class OpenScene(val sceneId: Int) : DownloadsSideEffect()
}