package digital.euforia.app.ui.programs

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.model.config.BlockType
import digital.euforia.app.domain.model.config.ProgramsConfig
import digital.euforia.app.domain.usecase.program.GetProgramsUseCase
import digital.euforia.app.domain.usecase.program.GetProgramsWithChildrenUseCase
import digital.euforia.app.domain.usecase.program.GetSearchResultsUseCase
import digital.euforia.app.domain.usecase.program.SearchResults
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.util.logTag
import digital.euforia.app.ui.util.postEffect
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val profilePreferences: ProfilePreferences,
    private val getProgramsWithChildrenUseCase: GetProgramsWithChildrenUseCase,
    private val getProgramsUseCase: GetProgramsUseCase,
    private val configFetcher: EuforiaRemoteConfigFetcher,
    private val getSearchResultsUseCase: GetSearchResultsUseCase,
    private val analyticSender: AnalyticSender
) : ViewModel(),
    ContainerHost<ProgramsState, ProgramsSideEffect> {
    override val container = container<ProgramsState, ProgramsSideEffect>(
        initialState = ProgramsState(),
        onCreate = {
            analyticSender.libraryShow()
            observePremium()
            loadConfig()
            loadData()
            getSearchSuggestions()
        }
    )

    private val searchQueryFlow = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            searchQueryFlow
                .debounce { query ->
                    if (query.isNullOrBlank()) 0L else 600L
                }
                .distinctUntilChanged()
                .collectLatest { query ->
                    intent {
                        reduceState { copy(searchQuery = query) }
                        if (!query.isNullOrBlank()) {
                            reduceState {
                                copy(
                                    isSearchLoading = true,
                                    searchResults = null
                                )
                            }

                            getSearchResultsUseCase.invoke(query)
                                .onSuccess { results ->
                                    reduceState { copy(searchResults = results) }
                                }
                                .onFailure { error ->
                                    Timber.tag(logTag()).d(error)
                                    reduceState { copy(searchResults = null) }
                                }
                                .onFinish {
                                    reduceState {
                                        copy(
                                            isSearchLoading = false,
                                        )
                                    }
                                }
                        } else {
                            reduceState { copy(searchResults = null) }
                        }
                    }
                }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getProgramsUseCase.invoke().onSuccess { result ->
                if (result.programList.isNotEmpty()) {
                    reduceState {
                        copy(
                            programs = result.programList,
                            articles = result.articlesList,
                            exercises = result.exercisesList,
                            errorState = null
                        )
                    }
                } else {
                    reduceState { copy(errorState = ErrorViewState.EmptyState) }
                }
            }.onFailure { error ->
                reduceState { copy(errorState = error.mapToErrorViewState()) }
            }.onFinish {
                reduceState { copy(isLoading = false) }
            }
        }
    }

    private fun getSearchSuggestions() {
        viewModelScope.launch {
            val suggestions = configFetcher.getSearchSuggestions()
            reduceState { copy(searchSuggestions = suggestions) }
        }
    }

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium ->
                reduceState { copy(isPremium = isPremium) }
            }
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

    fun onArticleClicked(articleUi: PublicationInfo) {
        onPublicationClicked(articleUi.id, articleUi.packageId, PublicationType.ARTICLE)
    }

    fun onExerciseClicked(exerciseUi: PublicationInfo) {
        onPublicationClicked(exerciseUi.id, exerciseUi.packageId, PublicationType.EXERCISE)
    }

    private fun onPublicationClicked(id: Int, packageId: Int?, publicationType: PublicationType) {
        intent {
            val packageTitle = state.programs.firstOrNull { it.id == packageId }?.name
            postSideEffect(
                ProgramsSideEffect.NavigateToPublication(
                    id = id,
                    type = publicationType,
                    packageTitle = packageTitle.orEmpty()
                )
            )
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

    fun onMoreSearchedMeditationsClicked() {
        viewModelScope.launch {
            val state = container.stateFlow.value
            if (state.searchResults?.meditations.isNullOrEmpty()) return@launch
            postEffect(
                ProgramsSideEffect.NavigateToPublications(
                    publicationType = PublicationType.MEDITATION,
                    ids = state.searchResults.meditations.joinToString(",") { it.id.toString() }
                )
            )
        }
    }

    fun onMoreExercisesClicked() {
        postEffect(ProgramsSideEffect.NavigateToExercises)
    }

    fun onMoreArticlesClicked() {
        postEffect(
            ProgramsSideEffect.NavigateToPublications(
                publicationType = PublicationType.ARTICLE,
                ids = ""
            )
        )
    }

    fun onSearchQueryChanged(query: String?) {
        searchQueryFlow.value = query
    }

    fun onSuggestionClicked(index: Int) {
        viewModelScope.launch {
            intent {
                val suggestion = state.searchSuggestions.getOrNull(index) ?: return@intent
                reduce { state.copy(searchQuery = suggestion) }
                searchQueryFlow.value = suggestion
            }
        }
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
    val videoUrl: String?,
    val color1: String?,
    val color2: String?,
    val color3: String?,
    val duration: Int?,
    val publishedAt: Long?,
)

data class ProgramsState(
    val isLoading: Boolean = true,
    val isSearchLoading: Boolean = false,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val programs: List<ProgramUi> = emptyList(),
    val exercises: List<PublicationInfo> = emptyList(),
    val articles: List<PublicationInfo> = emptyList(),
    val exerciseTitle: String = "Exercises",
    val articleTitle: String = "Articles",
    val programsConfig: List<ProgramsConfig> = emptyList(),
    val searchQuery: String? = null,
    val searchResults: SearchResults? = null,
    val searchSuggestions: List<String> = emptyList()
)

sealed class ProgramsSideEffect {
    data class NavigateToProgramDetail(val programId: Int) : ProgramsSideEffect()
    data object NavigateToDownloads : ProgramsSideEffect()
    data class NavigateToPublication(
        val id: Int,
        val type: PublicationType,
        val packageTitle: String
    ) : ProgramsSideEffect()

    data class NavigateToPublications(
        val publicationType: PublicationType,
        val ids: String
    ) : ProgramsSideEffect()

    data object NavigateToExercises : ProgramsSideEffect()
}