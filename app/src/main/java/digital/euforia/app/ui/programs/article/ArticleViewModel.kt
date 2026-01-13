package digital.euforia.app.ui.programs.article

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.domain.model.article.ArticleBlock
import digital.euforia.app.domain.usecase.article.GetArticleContentUseCase
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
    private val getArticleContentUseCase: GetArticleContentUseCase
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
            getArticleContentUseCase.invoke(id).onSuccess { articleBlocks ->
                reduceState {
                    copy(articleBlocks = articleBlocks)
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
    val articleBlocks: List<ArticleBlock> = emptyList()
)

sealed class ArticleSideEffect {}