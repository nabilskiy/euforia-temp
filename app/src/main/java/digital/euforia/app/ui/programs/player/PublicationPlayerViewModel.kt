package digital.euforia.app.ui.programs.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_MEDITATION_BACKGROUND
import digital.euforia.app.data.repository.ExerciseRepository
import digital.euforia.app.data.repository.ResourceRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.usecase.program.GetPlaylistUseCase
import digital.euforia.app.domain.usecase.program.GetPublicationInfoUseCase
import digital.euforia.app.domain.usecase.program.GetPublicationInfosUseCase
import digital.euforia.app.domain.usecase.resources.GetResourcesUseCase
import digital.euforia.app.service.ExerciseVideoPlaybackService
import digital.euforia.app.ui.player.audio.toSoundEffectUi
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.SoundEffectUi
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class PublicationPlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val getResourcesUseCase: GetResourcesUseCase,
    private val resourceRepository: ResourceRepository,
    private val getPublicationInfoUseCase: GetPublicationInfoUseCase,
    private val getPublicationInfosUseCase: GetPublicationInfosUseCase,
    private val getPlaylistUseCase: GetPlaylistUseCase,
    private val appPreferences: AppPreferences,
) : ViewModel(), ContainerHost<PublicationPlayerState, PublicationPlayerSideEffect> {

    private val id: Int =
        requireNotNull(savedStateHandle.get<Int>("id"))
    private val publicationType: PublicationType =
        requireNotNull(savedStateHandle.get<PublicationType>("publicationType"))
    override val container = container<PublicationPlayerState, PublicationPlayerSideEffect>(
        initialState = PublicationPlayerState(),
        onCreate = {
            loadPublication()
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

    fun prepareAndPlay(publicationInfo: PublicationInfo) {
        val ctrl = mediaController ?: return
        val isMeditation = publicationInfo.publicationType == PublicationType.MEDITATION
        val uri = if (isMeditation) {
            publicationInfo.videoUrl
        } else {
            publicationInfo.videoUrl ?: publicationInfo.categoryVideoCoverUrl
        } ?: return

        if (currentUri == uri) return
        val metadata = MediaMetadata.Builder()
            .setTitle(publicationInfo.title)
            .setArtist(publicationInfo.subtitle)
            .setArtworkUri(publicationInfo.imageUrl?.let { Uri.parse(it) })
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

    private fun loadPublication() {
        viewModelScope.launch {
            reduceState { copy(isLoading = true, errorState = null) }
            getPublicationInfoUseCase.invoke(
                id = id,
                publicationType = publicationType
            ).map { publicationInfo ->
                publicationInfo.also {
                    if (it.publicationType == PublicationType.MEDITATION) {
                        publicationInfo.categoryId?.let { categoryId ->
                            getPlaylistUseCase.invoke(categoryId).onSuccess { playlist ->
                                reduceState { copy(playlist = playlist) }
                            }
                        }
                    }
                }
            }.onSuccess { publicationInfo ->
                reduceState { copy(publicationInfo = publicationInfo) }
            }.onFailure { error ->
                reduceState { copy(errorState = error.mapToErrorViewState()) }
            }.onFinish {
                reduceState { copy(isLoading = false) }
            }
        }
    }

    private fun loadSoundEffects() {
        viewModelScope.launch {
            resourceRepository.getByClassAlias(CLASS_ALIAS_MEDITATION_BACKGROUND)
                .onSuccess { resources ->
                    val soundEffects = resources.map { it.toSoundEffectUi() }
                    val selectedIndex = appPreferences.getMeditationBackgroundIndex().let { index ->
                        if (index >= soundEffects.size) -1 else index
                    }
                    reduceState {
                        copy(
                            soundEffectsList = soundEffects,
                            selectedSoundEffectIndex = selectedIndex
                        )
                    }
                }.onFailure {
                    Timber.d("Failed to load sound effects. $it")
                }
        }
    }

    fun onSoundEffectSelected(index: Int) {
        intent {
            if (state.selectedSoundEffectIndex == index) return@intent
            val soundEffect = state.soundEffectsList.getOrNull(index) ?: return@intent
            appPreferences.setMeditationBackgroundIndex(index)
            reduce { state.copy(selectedSoundEffectIndex = index) }
        }
    }

    fun onMuteClicked() {
        intent {
            reduce { state.copy(selectedSoundEffectIndex = -1) }
        }
    }
}

data class PublicationPlayerState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val errorState: ErrorViewState? = null,
    val publicationInfo: PublicationInfo? = null,
    val soundEffectsList: List<SoundEffectUi> = emptyList(),
    val selectedSoundEffectIndex: Int = -1,
    val playlist: PublicationsPlaylist? = null
)

data class PublicationsPlaylist(
    val publicationInfosList: List<PublicationInfo>
)

sealed class PublicationPlayerSideEffect {}