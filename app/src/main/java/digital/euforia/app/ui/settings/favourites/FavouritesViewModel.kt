package digital.euforia.app.ui.settings.favourites


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.usecase.program.GetFavouritePublicationsUseCase
import digital.euforia.app.ui.programs.exercises.ExercisesSideEffect
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
class FavouritesViewModel @Inject constructor(
    private val getFavouritePublicationsUseCase: GetFavouritePublicationsUseCase,
    private val profilePreferences: ProfilePreferences,
) : ViewModel(), ContainerHost<FavouritesState, FavouritesSideEffect> {
    override val container = container<FavouritesState, FavouritesSideEffect>(
        initialState = FavouritesState(),
        onCreate = {
            loadFavourites()
            observePremium()
        }
    )

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium ->
                reduceState {
                    copy(isPremium = isPremium)
                }
            }
        }
    }

    private fun loadFavourites() {
        viewModelScope.launch {
            getFavouritePublicationsUseCase.invoke().onSuccess { favourites ->
                reduceState {
                    copy(
                        meditations = favourites.meditations,
                        exercises = favourites.exercises,
                        articles = favourites.articles
                    )
                }
            }.onFailure { error ->
                reduceState { copy(errorState = error.mapToErrorViewState()) }
            }.onFinish {
                reduceState { copy(isLoading = false) }
            }

        }
    }

    fun onPublicationClicked(publicationInfo: PublicationInfo) {
        viewModelScope.launch {
            postEffect(
                FavouritesSideEffect.NavigateToPublication(
                    id = publicationInfo.id,
                    type = publicationInfo.publicationType,
                )
            )
        }
    }

    fun onRetryClick() {
        loadFavourites()
    }

    fun onDownloadsClicked() {
        postEffect(FavouritesSideEffect.NavigateToDownloads)
    }
}

data class FavouritesState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val meditations: List<PublicationInfo> = emptyList(),
    val exercises: List<PublicationInfo> = emptyList(),
    val articles: List<PublicationInfo> = emptyList(),
)

sealed class FavouritesSideEffect {
    data class NavigateToPublication(
        val id: Int,
        val type: PublicationType,
    ) : FavouritesSideEffect()

    data object NavigateToDownloads : FavouritesSideEffect()
}