package digital.euforia.app.ui.programs.publication

import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.R
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.usecase.program.GetPublicationInfoUseCase
import digital.euforia.app.domain.usecase.program.GetSimilarPublicationsUseCase
import digital.euforia.app.ui.util.postEffect
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class PublicationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getPublicationInfoUseCase: GetPublicationInfoUseCase,
    private val getSimilarPublicationsUseCase: GetSimilarPublicationsUseCase
) : ViewModel(), ContainerHost<PublicationState, PublicationSideEffect> {

    private val id: Int =
        requireNotNull(savedStateHandle.get<Int>("id"))
    private val publicationType: PublicationType =
        requireNotNull(savedStateHandle.get<PublicationType>("publicationType"))
    val packageTitle: String? =
        requireNotNull(savedStateHandle.get<String>("packageTitle"))
    override val container = container<PublicationState, PublicationSideEffect>(
        initialState = PublicationState(),
        onCreate = {
            load()
        }
    )

    private fun load() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getPublicationInfoUseCase.invoke(
                publicationType = publicationType,
                publicationId = id
            ).onSuccess { publicationInfo ->
                loadSimilarPublications(publicationInfo)
                reduceState {
                    copy(
                        publicationInfo = publicationInfo,
                        errorState = null
                    )
                }
            }.onFailure { error ->
                reduceState {
                    copy(errorState = error.mapToErrorViewState())
                }
            }.onFinish {
                reduceState {
                    copy(isLoading = false)
                }
            }
        }
    }

    private fun loadSimilarPublications(publicationInfo: PublicationInfo) {
        viewModelScope.launch {
            getSimilarPublicationsUseCase.invoke(
                publicationType = publicationType,
                categoryId = publicationInfo.categoryId,
                excludePublicationId = publicationInfo.id
            ).onSuccess { similarPublications ->
                reduceState {
                    copy(
                        similarPublications = similarPublications
                    )
                }
            }
        }
    }

    fun onPublicationClicked(publicationInfo: PublicationInfo) {
        postEffect(
            PublicationSideEffect.NavigateToPublication(
                id = publicationInfo.id,
                type = publicationInfo.publicationType,
                packageTitle = packageTitle.orEmpty()
            )
        )
    }

    fun onPlayClicked() {
    }

    fun onDownloadsClicked() {
        viewModelScope.launch {
            postEffect(PublicationSideEffect.NavigateToDownloads)
        }
    }

    fun onRetryClicked() {
        load()
    }
}

data class PublicationState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val publicationInfo: PublicationInfo? = null,
    val similarPublications: List<PublicationInfo> = emptyList()
)

sealed class PublicationSideEffect {
    data object NavigateToDownloads : PublicationSideEffect()
    data class NavigateToPublication(
        val id: Int,
        val type: PublicationType,
        val packageTitle: String
    ) : PublicationSideEffect()
}

@Keep
enum class PublicationType {
    ARTICLE,
    EXERCISE,
    MEDITATION
}

fun PublicationType.getIconRes(): Int {
    return when (this) {
        PublicationType.MEDITATION -> R.drawable.ic_type_audio
        PublicationType.EXERCISE -> R.drawable.ic_type_exercise
        else -> R.drawable.ic_type_read
    }
}

fun PublicationType.getTitleRes(): Int {
    return when (this) {
        PublicationType.MEDITATION -> R.string.type_meditation
        PublicationType.EXERCISE -> R.string.type_exercise
        PublicationType.ARTICLE -> R.string.type_article
    }
}