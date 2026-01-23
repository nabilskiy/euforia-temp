package digital.euforia.app.ui.programs.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_MEDITATION_BACKGROUND
import digital.euforia.app.data.repository.ExerciseRepository
import digital.euforia.app.data.repository.ResourceRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.usecase.program.GetPlaylistUseCase
import digital.euforia.app.domain.usecase.program.GetPublicationInfoUseCase
import digital.euforia.app.domain.usecase.program.GetPublicationInfosUseCase
import digital.euforia.app.domain.usecase.program.UpdateFavouriteUseCase
import digital.euforia.app.domain.usecase.resources.GetResourcesUseCase
import digital.euforia.app.service.ExerciseVideoPlaybackService
import digital.euforia.app.ui.player.audio.toSoundEffectUi
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.SoundEffectUi
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(UnstableApi::class)
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
    private val profilePreferences: ProfilePreferences,
    private val updateFavouriteUseCase: UpdateFavouriteUseCase
) : ViewModel(), ContainerHost<PublicationPlayerState, PublicationPlayerSideEffect> {

    private val id: Int =
        requireNotNull(savedStateHandle.get<Int>("id"))
    private val publicationType: PublicationType =
        requireNotNull(savedStateHandle.get<PublicationType>("publicationType"))
    override val container = container<PublicationPlayerState, PublicationPlayerSideEffect>(
        initialState = PublicationPlayerState(),
        onCreate = {
            observePremium()
            loadPublication()
            loadSoundEffects()
        }
    )

    private var mediaController: MediaController? = null
    private var currentUri: String? = null

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val publicationInfo = mediaItem?.localConfiguration?.tag as? PublicationInfo ?: return
            Timber.tag("PUBLICATION_PLAYBACK")
                .d("onMediaItemTransition: publicationId=${publicationInfo.id}, reason=$reason")
            intent {
                reduce {
                    state.copy(publicationInfo = publicationInfo)
                }
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            Timber.tag("PUBLICATION_PLAYBACK")
                .d("onPlayWhenReadyChanged: $playWhenReady, reason: $reason")
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            Timber.tag("PUBLICATION_PLAYBACK").d("onPlaybackStateChanged: $playbackState")
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            Timber.tag("PUBLICATION_PLAYBACK").e(error, "onPlayerError")
        }
    }

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
                    controller.addListener(playerListener)
                    cont.resume(controller)
                } catch (t: Throwable) {
                    cont.resumeWithException(t)
                }
            }, { runnable -> runnable.run() })
            cont.invokeOnCancellation { future.cancel(true) }
        }
    }

    fun player(): Player? = mediaController

    private fun PublicationInfo.toMediaItem(): MediaItem {
        val isMeditation = publicationType == PublicationType.MEDITATION
        val uri = if (isMeditation) {
            videoUrl
        } else {
            videoUrl ?: categoryVideoCoverUrl
        } ?: ""

        Timber.tag("PUBLICATION_PLAYBACK").d("toMediaItem: id=$id, type=$publicationType, uri=$uri")

        if (uri.isBlank()) {
            Timber.tag("PUBLICATION_PLAYBACK").w("toMediaItem: URI is blank for publicationId=$id")
        }

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(subtitle)
            .setArtworkUri(imageUrl?.let { Uri.parse(it) })
            .build()
        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(metadata)
            .setTag(this)
            .build()
    }

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium ->
                reduceState { copy(isPremium = isPremium) }
            }
        }
    }

    fun prepareAndPlay(publicationInfo: PublicationInfo) {
        val ctrl = mediaController ?: return
        val playlist = container.stateFlow.value.playlist

        viewModelScope.launch(Dispatchers.Main) {
            Timber.tag("PUBLICATION_PLAYBACK")
                .d("prepareAndPlay: publicationId=${publicationInfo.id}, currentId=${(ctrl.currentMediaItem?.localConfiguration?.tag as? PublicationInfo)?.id}, state=${ctrl.playbackState}")

            // Check if we are already playing this item to avoid unnecessary resets
            val currentMediaItem = ctrl.currentMediaItem
            val currentPublicationId =
                (currentMediaItem?.localConfiguration?.tag as? PublicationInfo)?.id
            if (currentPublicationId == publicationInfo.id && (ctrl.playbackState == Player.STATE_READY || ctrl.playbackState == Player.STATE_BUFFERING) && ctrl.mediaItemCount > 0) {
                Timber.tag("PUBLICATION_PLAYBACK")
                    .d("prepareAndPlay: already playing ${publicationInfo.id}, ensuring it's playing (state=${ctrl.playbackState})")
                ctrl.play()
                return@launch
            }

            if (playlist != null) {
                val mediaItems = playlist.publicationInfosList.map { it.toMediaItem() }
                val startIndex =
                    playlist.publicationInfosList.indexOfFirst { it.id == publicationInfo.id }
                        .takeIf { it != -1 } ?: 0

                Timber.tag("PUBLICATION_PLAYBACK")
                    .d("prepareAndPlay: setting playlist, startIndex=$startIndex, items=${mediaItems.size}")
                ctrl.setMediaItems(mediaItems, startIndex, 0L)
            } else {
                val item = publicationInfo.toMediaItem()
                Timber.tag("PUBLICATION_PLAYBACK")
                    .d("prepareAndPlay: setting single item, uri=${item.localConfiguration?.uri}")
                ctrl.setMediaItem(item)
            }

            ctrl.repeatMode = Player.REPEAT_MODE_OFF
            ctrl.prepare()
            ctrl.play()
            Timber.tag("PUBLICATION_PLAYBACK")
                .d("prepareAndPlay: called prepare() and play(), repeatMode=${ctrl.repeatMode}")
        }
    }

    fun stopPlaybackAndRelease(stopService: Boolean = true) {
        val ctrl = mediaController ?: return

        viewModelScope.launch(Dispatchers.Main) {
            Timber.tag("PUBLICATION_PLAYBACK").d("stopPlaybackAndRelease: stopService=$stopService")
            ctrl.removeListener(playerListener)

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
                Timber.tag("PUBLICATION_PLAYBACK").d("loadPublication: fetched info for $id")
                publicationInfo.also {
                    if (it.publicationType == PublicationType.MEDITATION) {
                        publicationInfo.categoryId?.let { categoryId ->
                            getPlaylistUseCase.invoke(categoryId).onSuccess { playlist ->
                                Timber.tag("PUBLICATION_PLAYBACK")
                                    .d("loadPublication: fetched playlist size ${playlist.publicationInfosList.size}")
                                reduceState { copy(playlist = playlist) }
                            }
                        }
                    }
                }
            }.onSuccess { publicationInfo ->
                reduceState { copy(publicationInfo = publicationInfo) }
            }.onFailure { error ->
                Timber.tag("PUBLICATION_PLAYBACK").e(error, "loadPublication: failed")
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

    fun onPublicationSelected(publicationInfo: PublicationInfo) {
        intent {
            val ctrl = mediaController ?: return@intent
            val playlist = state.playlist
            Timber.tag("PUBLICATION_PLAYBACK")
                .d("onPublicationSelected: publicationId=${publicationInfo.id}, hasPlaylist=${playlist != null}")
            if (playlist != null) {
                val index =
                    playlist.publicationInfosList.indexOfFirst { it.id == publicationInfo.id }
                if (index != -1) {
                    Timber.tag("PUBLICATION_PLAYBACK")
                        .d("onPublicationSelected: seeking to index $index")
                    reduce {
                        state.copy(
                            publicationInfo = publicationInfo
                        )
                    }
                    withContext(Dispatchers.Main) {
                        ctrl.seekTo(index, 0L)
                        ctrl.play()
                    }
                }
            } else {
                reduce {
                    state.copy(
                        publicationInfo = publicationInfo
                    )
                }
            }
        }
    }

    fun onFavouriteClicked(publicationInfo: PublicationInfo) {
        viewModelScope.launch {
            val newIsFavourite = !publicationInfo.isFavourite

            updateFavouriteUseCase.invoke(
                id = publicationInfo.id,
                isFavourite = newIsFavourite,
                type = publicationInfo.publicationType
            )
            intent {
                reduce {
                    state.copy(
                        publicationInfo = publicationInfo.copy(
                            isFavourite = newIsFavourite
                        ),
                        playlist = state.playlist?.let { playlist ->
                            PublicationsPlaylist(
                                publicationInfosList = playlist.publicationInfosList.map { info ->
                                    if (info.id == publicationInfo.id) {
                                        info.copy(isFavourite = newIsFavourite)
                                    } else info
                                }
                            )
                        }
                    )
                }
            }
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