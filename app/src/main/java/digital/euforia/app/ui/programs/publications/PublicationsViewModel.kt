package digital.euforia.app.ui.programs.publications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.usecase.program.GetPublicationInfosUseCase
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.util.postEffect
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class PublicationsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getPublicationInfosUseCase: GetPublicationInfosUseCase,
) : ViewModel(), ContainerHost<PublicationsState, PublicationsSideEffect> {

    private val ids: String = requireNotNull(savedStateHandle.get<String>("ids"))
    val type: PublicationType =
        requireNotNull(savedStateHandle.get<PublicationType>("type"))
    override val container = container<PublicationsState, PublicationsSideEffect>(
        initialState = PublicationsState(),
        onCreate = {
            loadPublications()
        }
    )

    private fun loadPublications() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getPublicationInfosUseCase.invoke(
                publicationType = type,
                ids = ids.split(",").map { it.toInt() }
            ).onSuccess { publicationInfos ->
                reduceState {
                    if (publicationInfos.isNotEmpty()) {
                        copy(publicationInfos = publicationInfos)
                    } else copy(errorState = ErrorViewState.EmptyState)
                }
            }.onFailure {
                reduceState { copy(errorState = it.mapToErrorViewState()) }
            }.onFinish { reduceState { copy(isLoading = false) } }
        }
    }

    fun onPublicationClicked(publicationInfo: PublicationInfo) {
        postEffect(
            PublicationsSideEffect.NavigateToPublication(
                id = publicationInfo.id,
                type = type,
                packageTitle = ""
            )
        )
    }

    fun onRetryClicked() {
        loadPublications()
    }

    fun onDownloadsClicked() {

    }
}

data class PublicationsState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val publicationInfos: List<PublicationInfo> = emptyList()
)

sealed class PublicationsSideEffect {
    data class NavigateToPublication(
        val id: Int,
        val type: PublicationType,
        val packageTitle: String
    ) : PublicationsSideEffect()
}