package digital.euforia.app.ui.programs.programdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.model.config.ProgramsConfig
import digital.euforia.app.domain.usecase.program.GetProgramDetailsUseCase
import digital.euforia.app.domain.usecase.program.GetProgramWithChildrenUseCase
import digital.euforia.app.ui.programs.ArticleUi
import digital.euforia.app.ui.programs.ExerciseUi
import digital.euforia.app.ui.programs.MeditationUi
import digital.euforia.app.ui.programs.ProgramUi
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.programs.toArticleUi
import digital.euforia.app.ui.programs.toExerciseUi
import digital.euforia.app.ui.programs.toMeditationUi
import digital.euforia.app.ui.programs.toProgramUi
import digital.euforia.app.ui.util.postEffect
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class ProgramDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val configFetcher: EuforiaRemoteConfigFetcher,
//    private val getProgramWithChildrenUseCase: GetProgramWithChildrenUseCase,
    private val getProgramDetailsUseCase: GetProgramDetailsUseCase,
    private val profilePreferences: ProfilePreferences,
) : ViewModel(), ContainerHost<ProgramDetailsState, ProgramDetailsSideEffect> {
    private val programId: Int = requireNotNull(savedStateHandle.get<Int>("programId"))
    override val container = container<ProgramDetailsState, ProgramDetailsSideEffect>(
        initialState = ProgramDetailsState(),
        onCreate = {
            observePremium()
            loadConfig()
            loadData()
        }
    )

    private fun loadConfig() {
        viewModelScope.launch {
            val programsConfig = configFetcher.getProgramsConfig()
            reduceState {
                copy(
                    programsConfig = programsConfig
                )
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getProgramDetailsUseCase.invoke(programId).onSuccess { programDetails ->
                reduceState {
                    copy(
                        program = programDetails.program,
                        articles = programDetails.articlesList,
                        exercises = programDetails.exercisesList,
                        meditations = programDetails.meditationsList,
                        errorState = null
                    )
                }
            }.onFailure { error ->
                reduceState { copy(errorState = error.mapToErrorViewState()) }
            }.onFinish {
                reduceState { copy(isLoading = false) }
            }
        }
    }

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium ->
                reduceState { copy(isPremium = isPremium) }
            }
        }
    }

    fun onRetryClicked() {
        loadData()
    }

    fun onDownloadsClicked() {
        postEffect(ProgramDetailsSideEffect.NavigateToDownloads)
    }

    fun onPageSelected(page: Int) {
        reduceState {
            copy(currentPage = page)
        }
    }

    fun onPublicationClicked(publicationInfo: PublicationInfo) {
        val state = container.stateFlow.value
        postEffect(
            ProgramDetailsSideEffect.NavigateToPublication(
                id = publicationInfo.id,
                type = publicationInfo.publicationType,
                packageTitle = state.program?.name.orEmpty()
            )
        )
    }
}

data class ProgramDetailsState(
    val currentPage: Int = 0,
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val program: ProgramUi? = null,
    val exercises: List<PublicationInfo> = emptyList(),
    val articles: List<PublicationInfo> = emptyList(),
    val meditations: List<PublicationInfo> = emptyList(),
    val programsConfig: List<ProgramsConfig> = emptyList(),
)

sealed class ProgramDetailsSideEffect {
    data object NavigateToDownloads : ProgramDetailsSideEffect()
    data class NavigateToArticle(val articleId: Int) : ProgramDetailsSideEffect()
    data class NavigateToExercise(val exerciseId: Int) : ProgramDetailsSideEffect()
    data class NavigateToMeditation(val meditationId: Int) : ProgramDetailsSideEffect()
    data class NavigateToPublication(
        val id: Int,
        val type: PublicationType,
        val packageTitle: String
    ) :
        ProgramDetailsSideEffect()
}