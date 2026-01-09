package digital.euforia.app.ui.downloads

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor() : ViewModel(), ContainerHost<DownloadsState, DownloadsSideEffect> {
    override val container = container<DownloadsState, DownloadsSideEffect>(
        initialState = DownloadsState(),
        onCreate = {}
    )
}

data class DownloadsState(val errorMessage: String? = null)

sealed class DownloadsSideEffect {}