package digital.euforia.app.ui.programs.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.usecase.program.ExerciseUiBlock
import digital.euforia.app.domain.usecase.program.GetExerciseBlocksUseCase
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val getExerciseExerciseBlocksUseCase: GetExerciseBlocksUseCase,
    private val profilePreferences: ProfilePreferences
) : ViewModel(),
    ContainerHost<ExercisesState, ExercisesSideEffect> {
    override val container = container<ExercisesState, ExercisesSideEffect>(
        initialState = ExercisesState(),
        onCreate = {
            observePremium()
            loadExerciseBlocks()
        }
    )

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium -> }
            reduceState {
                copy(isPremium = isPremium)
            }
        }
    }

    private fun loadExerciseBlocks() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getExerciseExerciseBlocksUseCase.invoke().onSuccess { exerciseBlocks ->
                if (exerciseBlocks.isNotEmpty()) {
                    reduceState {
                        copy(
                            exerciseBlocks = exerciseBlocks,
                            errorState = null
                        )
                    }
                } else {
                    reduceState { copy(errorState = ErrorViewState.EmptyState) }
                }
            }.onFailure { error ->
                reduceState {
                    copy(errorState = error.mapToErrorViewState())
                }
            }.onFinish {
                reduceState { copy(isLoading = false) }
            }
        }
    }
}

data class ExercisesState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val exerciseBlocks: List<ExerciseUiBlock> = emptyList()
)

sealed class ExercisesSideEffect {}