package digital.euforia.app.ui.programs

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.config.BlockType
import digital.euforia.app.domain.model.config.ProgramsConfig
import digital.euforia.app.domain.usecase.program.GetProgramsWithChildrenUseCase
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
class ProgramsViewModel @Inject constructor(
    private val profilePreferences: ProfilePreferences,
    private val getProgramsWithChildrenUseCase: GetProgramsWithChildrenUseCase,
    private val configFetcher: EuforiaRemoteConfigFetcher,
) : ViewModel(),
    ContainerHost<ProgramsState, ProgramsSideEffect> {
    override val container = container<ProgramsState, ProgramsSideEffect>(
        initialState = ProgramsState(),
        onCreate = {
            observePremium()
            loadConfig()
            loadData()
        }
    )

    private fun loadData() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getProgramsWithChildrenUseCase.invoke().onSuccess { resultList ->
                if (resultList != null) {
                    val articles = mutableListOf<ArticleUi>()
                    val exercises = mutableListOf<ExerciseUi>()
                    val programs = resultList.map { result ->
//                        result.articles
                        articles.addAll(result.articles.map { article -> article.toArticleUi() })
                        exercises.addAll(result.exercises.map { exercise -> exercise.toExerciseUi() })
                        result.pkg.toProgramUi(result.articles.size + result.exercises.size + result.meditations.size)
                    }
                    exercises.sortBy { exerciseUi -> exerciseUi.publishedAt }
                    reduceState {
                        copy(
                            programs = programs,
                            articles = articles.take(12),
                            exercises = exercises.take(12),
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

    private fun loadConfig() {
        viewModelScope.launch {
            val programsConfig = configFetcher.getProgramsConfig()
            val exercisesTitle =
                programsConfig.firstOrNull { it.type == BlockType.EXERCISE_LIST }?.data?.description
            val articlesTitle =
                programsConfig.firstOrNull { it.type == BlockType.ARTICLE_LIST }?.data?.description
            reduceState {
                copy(
                    exerciseTitle = exercisesTitle ?: "",
                    articleTitle = articlesTitle ?: "",
                    programsConfig = programsConfig
                )
            }
        }
    }

    fun onRetryClick() {
        loadData()
    }

    fun onDownloadsClicked() {
        postEffect(ProgramsSideEffect.NavigateToDownloads)
    }

    fun onProgramClicked(programUi: ProgramUi) {
        postEffect(ProgramsSideEffect.NavigateToProgramDetail(programUi.id))
    }
}

@Immutable
data class ProgramUi(
    val id: Int,
    val isPremium: Boolean,
    val authorId: Int?,
    val name: String,
    val subtitle: String?,
    val description: String?,
    val keywords: String?,
    val imageUrl: String?,
    val imagePreviewUrl: String?,
    val imageCoverUrl: String?,
    val color1: String?,
    val color2: String?,
    val color3: String?,
    val resourceCount: Int = 0,
)

@Immutable
data class MeditationUi(
    val id: Int,
    val isPremium: Boolean,
    val alias: String,
    val authorId: Int?,
    val mainCategoryId: Int?,
    val mainPackageId: Int?,
    val name: String,
    val subtitle: String?,
    val description: String?,
    val keywords: String?,
    val imageUrl: String?,
    val imagePreviewUrl: String?,
    val imageCoverUrl: String?,
    val musicFileUrl: String?,
    val color1: String?,
    val color2: String?,
    val color3: String?,
    val publishedAt: Long?,
    val duration: Int?
)

@Immutable
data class ArticleUi(
    val id: Int,
    val isPremium: Boolean,
    val alias: String,
    val authorId: Int?,
    val mainCategoryId: Int?,
    val mainPackageId: Int?,
    val name: String,
    val subtitle: String?,
    val description: String?,
    val keywords: String?,
    val imageUrl: String?,
    val imagePreviewUrl: String?,
    val imageCoverUrl: String?,
    val musicFileUrl: String?,
    val color1: String?,
    val color2: String?,
    val color3: String?,
    val publishedAt: Long?,
    val duration: Int?,
)

@Immutable
data class ExerciseUi(
    val id: Int,
    val isPremium: Boolean,
    val alias: String,
    val authorId: Int?,
    val mainCategoryId: Int?,
    val mainPackageId: Int?,
    val name: String,
    val subtitle: String?,
    val description: String?,
    val keywords: String?,
    val imageUrl: String?,
    val imagePreviewUrl: String?,
    val imageCoverUrl: String?,
    val musicFileUrl: String?,
    val color1: String?,
    val color2: String?,
    val color3: String?,
    val duration: Int?,
    val publishedAt: Long?,
)

data class ProgramsState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val programs: List<ProgramUi> = emptyList(),
    val exercises: List<ExerciseUi> = emptyList(),
    val articles: List<ArticleUi> = emptyList(),
    val exerciseTitle: String = "Exercises",
    val articleTitle: String = "Articles",
    val programsConfig: List<ProgramsConfig> = emptyList()
)

sealed class ProgramsSideEffect {
    data class NavigateToProgramDetail(val programId: Int) : ProgramsSideEffect()
    data object NavigateToDownloads : ProgramsSideEffect()
}