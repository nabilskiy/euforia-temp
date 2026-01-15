package digital.euforia.app.ui.programs.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.repository.ExerciseRepository
import digital.euforia.app.domain.usecase.resources.GetResourcesUseCase
import digital.euforia.app.ui.player.audio.SoundEffectUi
import digital.euforia.app.ui.programs.ExerciseUi
import digital.euforia.app.ui.programs.toExerciseUi
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val getResourcesUseCase: GetResourcesUseCase,
) : ViewModel(), ContainerHost<ExerciseState, ExerciseSideEffect> {

    private val id: Int =
        requireNotNull(savedStateHandle.get<Int>("id"))
    override val container = container<ExerciseState, ExerciseSideEffect>(
        initialState = ExerciseState(),
        onCreate = {
            loadExercise()
        }
    )

    private fun loadExercise() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            exerciseRepository.getExerciseById(id).onSuccess { exercise ->
                reduceState { copy(exercise = exercise.toExerciseUi()) }
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
}

data class ExerciseState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val exercise: ExerciseUi? = null,
    val soundEffectsList: List<SoundEffectUi> = emptyList(),
)

sealed class ExerciseSideEffect {}