package digital.euforia.app.ui.programs.article

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.repository.ArticleRepository
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.model.article.ArticleBlock
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository
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
            val article = articleRepository.getArticleById(id).onFailure {
                reduceState { copy(errorState = it.mapToErrorViewState()) }
            }.dataOrNull

            reduceState { copy(publicationInfo = article) }
            articleRepository.getArticleContent(id).onSuccess { articleBody ->
                reduceState {
                    if (articleBody.isNotEmpty()) {
                        copy(articleBody = articleBody)
                    } else {
                        copy(errorState = ErrorViewState.EmptyState)
                    }
                }
            }.onFailure {
                reduceState { copy(errorState = it.mapToErrorViewState()) }
            }.onFinish {
                reduceState { copy(isLoading = false) }
            }
        }
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