package digital.euforia.app.ui.programs.exercise

import android.content.Context
import android.content.ComponentName
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_MEDITATION_BACKGROUND
import digital.euforia.app.data.repository.ExerciseRepository
import digital.euforia.app.data.repository.ResourceRepository
import digital.euforia.app.domain.usecase.resources.GetResourcesUseCase
import digital.euforia.app.ui.util.widget.SoundEffectUi
import digital.euforia.app.ui.programs.ExerciseUi
import digital.euforia.app.ui.programs.toExerciseUi
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject
import digital.euforia.app.service.ExerciseVideoPlaybackService
import digital.euforia.app.ui.player.audio.toSoundEffectUi
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val getResourcesUseCase: GetResourcesUseCase,
    private val resourceRepository: ResourceRepository,
) : ViewModel(), ContainerHost<ExerciseState, ExerciseSideEffect> {

    private val id: Int =
        requireNotNull(savedStateHandle.get<Int>("id"))
    override val container = container<ExerciseState, ExerciseSideEffect>(
        initialState = ExerciseState(),
        onCreate = {
            loadExercise()
            loadSoundEffects()
        }
    )

    private var mediaController: MediaController? = null
    private var currentUri: String? = null

    suspend fun getOrCreateController(context: Context): MediaController {
        val existing = mediaController
        if (existing != null) return existing
        val appCtx = context.applicationContext
        val token =
            SessionToken(appCtx, ComponentName(appCtx, ExerciseVideoPlaybackService::class.java))
        val future = MediaController.Builder(appCtx, token).buildAsync()
        return suspendCancellableCoroutine { cont ->
            future.addListener({
                try {
                    val controller = future.get()
                    mediaController = controller
                    cont.resume(controller)
                } catch (t: Throwable) {
                    cont.resumeWithException(t)
                }
            }, { runnable -> runnable.run() })
            cont.invokeOnCancellation { future.cancel(true) }
        }
    }

    fun player(): Player? = mediaController

    fun prepareAndPlay(exercise: ExerciseUi) {
        val ctrl = mediaController ?: return
        val uri = exercise.videoUrl ?: return
        if (currentUri == uri) return
        val metadata = MediaMetadata.Builder()
            .setTitle(exercise.name)
            .setArtist(exercise.subtitle)
            .setArtworkUri(exercise.imageUrl?.let { Uri.parse(it) })
            .build()
        val item = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
        ctrl.setMediaItem(item)
        ctrl.prepare()
        ctrl.playWhenReady = true
        currentUri = uri
    }

    fun stopPlaybackAndRelease(stopService: Boolean = true) {
        val ctrl = mediaController ?: return

        try {
            ctrl.playWhenReady = false
            ctrl.stop()
            ctrl.clearMediaItems()
        } catch (_: Throwable) {
        }

        if (stopService) {
            try {
                ctrl.sendCustomCommand(
                    ExerciseVideoPlaybackService.Commands.STOP_SERVICE,
                    android.os.Bundle.EMPTY
                )
            } catch (_: Throwable) {
            }
        }

        try {
            ctrl.release()
        } catch (_: Throwable) {
        }
        mediaController = null
        currentUri = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPlaybackAndRelease(stopService = false)
    }

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

    private fun loadSoundEffects() {
        viewModelScope.launch {
            resourceRepository.getByClassAlias(CLASS_ALIAS_MEDITATION_BACKGROUND)
                .onSuccess { resources ->
                    val soundEffects = resources.map { it.toSoundEffectUi() }
                    reduceState { copy(soundEffectsList = soundEffects) }
                }.onFailure {
                    Timber.d("Failed to load sound effects. $it")
                }
        }
    }

    fun onSoundEffectSelected(index: Int) {
        intent {
            if (state.selectedSoundEffectIndex == index) return@intent
            val soundEffect = state.soundEffectsList.getOrNull(index) ?: return@intent
            reduce { state.copy(selectedSoundEffectIndex = index) }
        }
    }

    fun onMuteClicked() {
        intent {
            reduce { state.copy(selectedSoundEffectIndex = -1) }
        }
    }
}

data class ExerciseState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val exercise: ExerciseUi? = null,
    val soundEffectsList: List<SoundEffectUi> = emptyList(),
    val selectedSoundEffectIndex: Int = -1,
)

sealed class ExerciseSideEffect {}