package digital.euforia.app.ui.programs.article

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.repository.ArticleRepository
import digital.euforia.app.data.repository.FavouritesRepository
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.model.article.ArticleBlock
import digital.euforia.app.domain.usecase.program.GetPublicationInfoUseCase
import digital.euforia.app.domain.usecase.program.UpdateFavouriteUseCase
import digital.euforia.app.ui.programs.publication.PublicationSideEffect
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
class ArticleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getPublicationInfoUseCase: GetPublicationInfoUseCase,
    private val articleRepository: ArticleRepository,
    private val updateFavouriteUseCase: UpdateFavouriteUseCase,
    private val favouritesRepository: FavouritesRepository,
    private val analyticSender: AnalyticSender
) : ViewModel(), ContainerHost<ArticleState, ArticleSideEffect> {
    private val id: Int =
        requireNotNull(savedStateHandle.get<Int>("id"))

    override val container = container<ArticleState, ArticleSideEffect>(
        initialState = ArticleState(),
        onCreate = {
            loadArticleContent()
        }
    )

    fun loadArticleContent() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getPublicationInfoUseCase.invoke(
                id = id,
                publicationType = PublicationType.ARTICLE
            ).map {
                observeIsFavourite(it)
                reduceState { copy(publicationInfo = publicationInfo) }
                articleRepository.getArticleContent(id).map { articleBody ->
                    reduceState {
                        if (articleBody.isNotEmpty()) {
                            copy(articleBody = articleBody)
                        } else {
                            copy(errorState = ErrorViewState.EmptyState)
                        }
                    }
                }
            }.onFailure {
                reduceState { copy(errorState = it.mapToErrorViewState()) }
            }.onFinish {
                reduceState { copy(isLoading = false) }
            }
        }
    }

    private fun observeIsFavourite(publicationInfo: PublicationInfo) {
        viewModelScope.launch {
            intent {
                favouritesRepository.observeByIdAndType(
                    publicationId = publicationInfo.id,
                    publicationType = publicationInfo.publicationType
                ).collectLatest { favourite ->
                    reduceState {
                        copy(
                            publicationInfo = publicationInfo.copy(
                                isFavourite = favourite != null
                            )
                        )
                    }
                }
            }
        }
    }

    fun onFavouriteClicked() {
        viewModelScope.launch {
            val publicationInfo = container.stateFlow.value.publicationInfo
            publicationInfo?.let {
                val newIsFavourite = !it.isFavourite
                if (newIsFavourite) {
                    analyticSender.articleReaderAddToFavoritesClick()
                } else {
                    analyticSender.articleReaderRemoveFromFavoritesClick()
                }
                updateFavouriteUseCase.invoke(
                    id = publicationInfo.id,
                    isFavourite = newIsFavourite,
                    type = publicationInfo.publicationType
                )
            }
        }
    }

    fun onShareClicked() {
        viewModelScope.launch { analyticSender.articleReaderShareClick() }
    }
}

data class ArticleState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val articleBlocks: List<ArticleBlock> = emptyList(),
    val articleBody: String = "",
    val publicationInfo: PublicationInfo? = null
)

sealed class ArticleSideEffect {}