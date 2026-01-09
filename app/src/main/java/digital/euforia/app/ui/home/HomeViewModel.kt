package digital.euforia.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.home.NavBarItem
import digital.euforia.app.domain.model.home.defaultNavBarItems
import digital.euforia.app.domain.usecase.accompaniment.SyncAccompanimentsUseCase
import digital.euforia.app.domain.usecase.home.GetNavBarItemsFlowUseCase
import digital.euforia.app.domain.usecase.program.SyncPackagesUseCase
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNavBarItemsFlowUseCase: GetNavBarItemsFlowUseCase,
    private val syncAccompanimentsUseCase: SyncAccompanimentsUseCase,
    private val syncTopProgramsUseCase: SyncPackagesUseCase,
    private val profilePreferences: ProfilePreferences,
    val analyticSender: AnalyticSender
) : ViewModel(), ContainerHost<HomeState, HomeSideEffect> {
    override val container = container<HomeState, HomeSideEffect>(
        initialState = HomeState(),
        onCreate = {
            analyticSender.mainScreenShow()
            syncAccompaniments()
            observeNavBarItems()
        }
    )

    private fun observeNavBarItems() {
        viewModelScope.launch {
            getNavBarItemsFlowUseCase().collectLatest {
                reduceState { copy(navBarItems = it) }
            }
        }
    }

    private suspend fun syncAccompaniments() {
        viewModelScope.async {
//            val isDemo = profilePreferences.getIsDemo()
//            syncAccompanimentsUseCase(isDemo)
            syncTopProgramsUseCase()
        }.await()
    }

    fun onNavBarItemSelected(index: Int) {
        reduceState { copy(selectedItemIndex = index) }
    }
}

data class HomeState(
    val navBarItems: List<NavBarItem> = defaultNavBarItems,
    val selectedItemIndex: Int = 0
)

sealed class HomeSideEffect {}