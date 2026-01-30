package digital.euforia.app.ui.programs.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.usecase.program.ExerciseUiBlock
import digital.euforia.app.domain.usecase.program.GetExerciseBlocksUseCase
import digital.euforia.app.ui.programs.ProgramsSideEffect
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.util.postEffect
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val getExerciseExerciseBlocksUseCase: GetExerciseBlocksUseCase,
    private val profilePreferences: ProfilePreferences
) : ViewModel(),
    ContainerHost<ExercisesState, ExercisesSideEffect> {
    override val container = container<ExercisesState, ExercisesSideEffect>(
        initialState = ExercisesState(),
        onCreate = {
            observePremium()
            loadExerciseBlocks()
        }
    )

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium -> }
            reduceState {
                copy(isPremium = isPremium)
            }
        }
    }

    private fun loadExerciseBlocks() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getExerciseExerciseBlocksUseCase.invoke().onSuccess { exerciseBlocks ->
                if (exerciseBlocks.isNotEmpty()) {
                    reduceState {
                        copy(
                            exerciseBlocks = exerciseBlocks,
                            errorState = null
                        )
                    }
                } else {
                    reduceState { copy(errorState = ErrorViewState.EmptyState) }
                }
            }.onFailure { error ->
                reduceState {
                    copy(errorState = error.mapToErrorViewState())
                }
            }.onFinish {
                reduceState { copy(isLoading = false) }
            }
        }
    }

    fun onMoreClicked(publicationsList: List<PublicationInfo>) {
        postEffect(
            ExercisesSideEffect.NavigateToPublications(
                publicationType = PublicationType.EXERCISE,
                ids = publicationsList.joinToString(",") { it.id.toString() }
            )
        )
    }

    fun onExerciseClicked(publication: PublicationInfo) {
        postEffect(
            ExercisesSideEffect.NavigateToPublication(
                id = publication.id,
                type = PublicationType.EXERCISE,
                packageTitle = ""
            )
        )
    }

    fun onBannerClicked(banner: ExerciseUiBlock.Banner) {
        val sideEffect = if (banner.ids.isNotEmpty()) {
            val ids = banner.ids.joinToString(",")
            ExercisesSideEffect.NavigateToPublications(
                publicationType = PublicationType.EXERCISE,
                ids = ids
            )
        } else if (banner.id != null) {
            ExercisesSideEffect.NavigateToPublication(
                id = banner.id,
                type = PublicationType.EXERCISE,
                packageTitle = ""
            )
        } else {
            return
        }

        postEffect(sideEffect)
    }

    fun onRetryClick() {
        loadExerciseBlocks()
    }

    fun onDownloadsClicked() {
        postEffect(ExercisesSideEffect.NavigateToDownloads)
    }
}

data class ExercisesState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val exerciseBlocks: List<ExerciseUiBlock> = emptyList()
)

sealed class ExercisesSideEffect {
    data class NavigateToPublication(
        val id: Int,
        val type: PublicationType,
        val packageTitle: String
    ) : ExercisesSideEffect()

    data class NavigateToPublications(
        val publicationType: PublicationType,
        val ids: String
    ) : ExercisesSideEffect()

    data object NavigateToDownloads : ExercisesSideEffect()
}