package digital.euforia.app.ui.programs.publication

import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.repository.FavouritesRepository
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.usecase.program.GetPublicationInfoUseCase
import digital.euforia.app.domain.usecase.program.GetSimilarPublicationsUseCase
import digital.euforia.app.domain.usecase.program.UpdateFavouriteUseCase
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
class PublicationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getPublicationInfoUseCase: GetPublicationInfoUseCase,
    private val getSimilarPublicationsUseCase: GetSimilarPublicationsUseCase,
    private val updateFavouriteUseCase: UpdateFavouriteUseCase,
    private val favouritesRepository: FavouritesRepository,
    private val profilePreferences: ProfilePreferences,
    private val analyticSender: AnalyticSender
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
            logShow()
            observePremium()
            load()
        }
    )


    private fun logShow() {
        viewModelScope.launch { analyticSender.entityShow(publicationType.value, id.toString()) }
    }

    private fun load() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getPublicationInfoUseCase.invoke(
                publicationType = publicationType,
                id = id
            ).onSuccess { publicationInfo ->
                loadSimilarPublications(publicationInfo)
                observeIsFavourite(publicationInfo)
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

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium ->
                reduceState { copy(isPremium = isPremium) }
            }
        }
    }

    private fun loadSimilarPublications(publicationInfo: PublicationInfo) {
        viewModelScope.launch {
            publicationInfo.categoryId ?: return@launch

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

    fun onPublicationClicked(publicationInfo: PublicationInfo) {
        viewModelScope.launch { analyticSender.entitySimilarItemClick(publicationType.value) }
        postEffect(
            PublicationSideEffect.NavigateToPublication(
                id = publicationInfo.id,
                type = publicationInfo.publicationType,
                packageTitle = packageTitle.orEmpty()
            )
        )
    }

    fun onShowSimilarClicked() {
        intent {
            analyticSender.entitySimilarClick(publicationType.value)
            val ids = state.similarPublications.map { it.id }.joinToString(",")
            postSideEffect(
                PublicationSideEffect.NavigateToSimilar(
                    publicationType = publicationType,
                    ids = ids
                )
            )
        }
    }

    fun onPlayClicked() {
        viewModelScope.launch {
            analyticSender.entityActionClick(publicationType.value)
            when (publicationType) {
                PublicationType.ARTICLE -> postEffect(PublicationSideEffect.OpenArticle(id))
                PublicationType.EXERCISE -> postEffect(PublicationSideEffect.OpenExercise(id))
                PublicationType.MEDITATION -> postEffect(PublicationSideEffect.OpenMeditation(id))
            }
        }
    }

    fun onDownloadsClicked() {
        viewModelScope.launch {
            postEffect(PublicationSideEffect.NavigateToDownloads)
        }
    }

    fun onRetryClicked() {
        load()
    }

    fun onFavouriteClicked(publicationInfo: PublicationInfo) {
        viewModelScope.launch {
            val newIsFavourite = !publicationInfo.isFavourite
            if (newIsFavourite) {
                analyticSender.entityAddToFavoritesClick(publicationType.value)
            } else {
                analyticSender.entityRemoveFromFavoritesClick(publicationType.value)
            }
            updateFavouriteUseCase.invoke(
                id = publicationInfo.id,
                isFavourite = newIsFavourite,
                type = publicationInfo.publicationType
            )
        }
    }

    fun onShareClicked() {
        viewModelScope.launch { analyticSender.entityMenuShareClick(publicationType.value) }
        postEffect(
            PublicationSideEffect.Share(
                type = publicationType,
                id = id
            )
        )
    }

    fun onPackageClicked() {
        intent {
            if (state.publicationInfo?.packageId != null) {
                analyticSender.entityPackageClick(state.publicationInfo?.id.toString())
                postSideEffect(
                    PublicationSideEffect.NavigateToPackage(
                        packageId = state.publicationInfo!!.packageId!!
                    )
                )
            }
        }
    }

    fun onArticleReaderShow() {
        viewModelScope.launch { analyticSender.articleReaderShow(id.toString()) }
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

    data class NavigateToSimilar(
        val publicationType: PublicationType,
        val ids: String
    ) : PublicationSideEffect()

    data class OpenArticle(val id: Int) : PublicationSideEffect()
    data class OpenExercise(val id: Int) : PublicationSideEffect()
    data class OpenMeditation(val id: Int) : PublicationSideEffect()
    data class NavigateToPackage(val packageId: Int) : PublicationSideEffect()
    data class Share(val type: PublicationType, val id: Int) : PublicationSideEffect()
}

@Keep
enum class PublicationType(val value: String) {
    ARTICLE("article"),
    EXERCISE("exercise"),
    MEDITATION("meditation");
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