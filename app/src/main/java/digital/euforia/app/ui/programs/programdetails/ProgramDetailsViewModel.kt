package digital.euforia.app.ui.programs.programdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.config.ProgramsConfig
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
    private val getProgramWithChildrenUseCase: GetProgramWithChildrenUseCase,
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
            getProgramWithChildrenUseCase.invoke(programId).onSuccess { result ->
                if (result != null) {
                    val meditations =
                        result.meditations.map { meditation -> meditation.toMeditationUi() }
                    val articles = result.articles.map { article -> article.toArticleUi() }
                    val exercises = result.exercises.map { exercise -> exercise.toExerciseUi() }
                    val program = result.pkg.toProgramUi(
                        result.articles.size + result.exercises.size + result.meditations.size
                    )
                    reduceState {
                        copy(
                            program = program,
                            articles = articles,
                            exercises = exercises,
                            meditations = meditations,
                            resourcesCount = articles.size + exercises.size + meditations.size,
                            errorState = null
                        )
                    }
                } else {
                    reduceState { copy(errorState = ErrorViewState.EmptyState) }
                }
                // Handle successful data load
            }.onFailure { error ->
                reduceState { copy(isLoading = false, errorState = error.mapToErrorViewState()) }
                // Handle error during data load
            }.onFinish {
                reduceState { copy(isLoading = false) }
            }
            // Implementation for loading data goes here
        }
    }

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium -> }
            reduceState { copy(isPremium = isPremium) }
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

    fun onArticleClicked(articleUi: ArticleUi) {
        val state = container.stateFlow.value
        postEffect(
            ProgramDetailsSideEffect.NavigateToPublication(
                id = articleUi.id,
                type = PublicationType.ARTICLE,
                packageTitle = state.program?.name.orEmpty()
            )
        )
//        postEffect(ProgramDetailsSideEffect.NavigateToArticle(articleUi.id))
    }

    fun onExerciseClicked(exerciseUi: ExerciseUi) {
        val state = container.stateFlow.value
        postEffect(
            ProgramDetailsSideEffect.NavigateToPublication(
                id = exerciseUi.id,
                type = PublicationType.EXERCISE,
                packageTitle = state.program?.name.orEmpty()
            )
        )
//        postEffect(ProgramDetailsSideEffect.NavigateToExercise(exerciseUi.id))
    }

    fun onMeditationClicked(meditationUi: MeditationUi) {
        val state = container.stateFlow.value
        postEffect(
            ProgramDetailsSideEffect.NavigateToPublication(
                id = meditationUi.id,
                type = PublicationType.MEDITATION,
                packageTitle = state.program?.name.orEmpty()
            )
        )
//        postEffect(ProgramDetailsSideEffect.NavigateToMeditation(meditationUi.id))
    }
}

data class ProgramDetailsState(
    val currentPage: Int = 0,
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val program: ProgramUi? = null,
    val exercises: List<ExerciseUi> = emptyList(),
    val articles: List<ArticleUi> = emptyList(),
    val meditations: List<MeditationUi> = emptyList(),
    val programsConfig: List<ProgramsConfig> = emptyList(),
    val resourcesCount: Int = 0,
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