package digital.euforia.app.ui.settings.faq

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.domain.usecase.faq.GetFAQCategoriesUseCase
import digital.euforia.app.domain.usecase.faq.mapToUi
import digital.euforia.app.data.network.NetworkNotAvailableException
import digital.euforia.app.ui.util.logTag
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject
import retrofit2.HttpException

@HiltViewModel
class FAQViewModel @Inject constructor(
    private val getFaqCategoriesUseCase: GetFAQCategoriesUseCase,
    val analyticSender: AnalyticSender
) : ViewModel(), ContainerHost<FAQState, FAQSideEffect> {
    override val container = container<FAQState, FAQSideEffect>(
        initialState = FAQState(),
        onCreate = {
            loadFAQCategories()
            analyticSender.faqShow()
        }
    )

    fun loadFAQCategories() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getFaqCategoriesUseCase.invoke().onSuccess { faqCategories ->
                val uiFaqCategories = faqCategories.map { it.mapToUi() }
                reduceState { copy(uiFaqCategories = uiFaqCategories) }
            }.onFailure { error ->
                Timber.tag(logTag()).d("Error loading FAQ categories: ${error.message}")
                val errorState = error.mapToErrorViewState()
                reduceState { copy(errorState = errorState) }
            }.onFinish { reduceState { copy(isLoading = false) } }
        }
    }
}

@Immutable
data class UiFAQCategory(
    val id: Int,
    val title: String,
    val items: List<UiFAQItem>,
)

@Immutable
data class UiFAQItem(
    val id: Int,
    val question: String,
    val answer: String,
)

data class FAQState(
    val isLoading: Boolean = true,
    val errorState: ErrorViewState? = null,
    val uiFaqCategories: List<UiFAQCategory> = emptyList(),
)


sealed class FAQSideEffect {}