package digital.euforia.app.ui.settings.favourites


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.usecase.program.GetFavouritePublicationsUseCase
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val getFavouritePublicationsUseCase: GetFavouritePublicationsUseCase
) : ViewModel(), ContainerHost<FavouritesState, FavouritesSideEffect> {
    override val container = container<FavouritesState, FavouritesSideEffect>(
        initialState = FavouritesState(),
        onCreate = {}
    )

    private fun loadFavourites() {
        viewModelScope.launch {
            getFavouritePublicationsUseCase.invoke().let { favourites ->
                val meditations =
                    favourites.filter { it.publicationType == PublicationType.MEDITATION }
                val exercises = favourites.filter { it.publicationType == PublicationType.EXERCISE }
                val articles = favourites.filter { it.publicationType == PublicationType.ARTICLE }
//                reduceState {
//                    copy(
//                        meditations = meditations,
//                        exercises = exercises,
//                        articles = articles
//                    )
//                }
            }
        }
    }
}

data class FavouritesState(
    val errorMessage: String? = null,
    val meditations: List<PublicationInfo> = emptyList(),
    val exercises: List<PublicationInfo> = emptyList(),
    val articles: List<PublicationInfo> = emptyList(),
)

sealed class FavouritesSideEffect {}