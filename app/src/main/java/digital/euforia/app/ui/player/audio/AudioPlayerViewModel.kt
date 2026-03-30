package digital.euforia.app.ui.player.audio

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.dao.AccompanimentDao
import digital.euforia.app.data.db.dao.AccompanimentItemDao
import digital.euforia.app.data.db.entity.AccompanimentWithItems
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_VOICE_AVATAR
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_VOICE_MUSIC
import digital.euforia.app.data.repository.AccompanimentItemRepository
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import javax.inject.Inject
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.DemoUnlockDayConfig
import digital.euforia.app.domain.model.onboarding.Gender
import digital.euforia.app.domain.model.toEventParam
import digital.euforia.app.domain.usecase.accompaniment.GetAccompanimentUseCase
import digital.euforia.app.domain.usecase.accompaniment.GetAccompanimentWithItemsUseCase
import digital.euforia.app.domain.usecase.accompaniment.SaveAccompanimentProgressUseCase
import digital.euforia.app.domain.usecase.accompaniment.SendRatingCommentUseCase
import digital.euforia.app.domain.usecase.accompaniment.SyncAccompanimentsUseCase
import digital.euforia.app.domain.usecase.accompaniment.UpdateAccompanimentItemUseCase
import digital.euforia.app.domain.usecase.network.CheckInternetConnectionUseCase
import digital.euforia.app.domain.usecase.resources.GetResourcesUseCase
import digital.euforia.app.domain.usecase.resources.SyncResourcesUseCase
import digital.euforia.app.ui.util.logTag
import digital.euforia.app.ui.util.postEffect
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.SoundEffectUi
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import digital.euforia.app.ui.util.widget.vibe.PlayState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber

@HiltViewModel
class AudioPlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val configFetcher: EuforiaRemoteConfigFetcher,
    private val getAccompanimentUseCase: GetAccompanimentUseCase,
    private val getAccompanimentWithItemsUseCase: GetAccompanimentWithItemsUseCase,
    private val getResourcesUseCase: GetResourcesUseCase,
    private val updateAccompanimentItemUseCase: UpdateAccompanimentItemUseCase,
    private val saveAccompanimentProgressUseCase: SaveAccompanimentProgressUseCase,
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
    private val syncAccompanimentsUseCase: SyncAccompanimentsUseCase,
    private val syncResourcesUseCase: SyncResourcesUseCase,
    private val analyticSender: AnalyticSender,
    private val accompanimentItemRepository: AccompanimentItemRepository,
    private val sendRatingCommentUseCase: SendRatingCommentUseCase,
    private val accompanimentRepository: AccompanimentRepository,

    @ApplicationContext private val context: Context
) : ViewModel(), ContainerHost<AudioPlayerState, AudioPlayerSideEffect> {
    val accompanimentId: Int = requireNotNull(savedStateHandle.get<Int>("accompanimentId"))
    private val timeOfDay: TimeOfDay = requireNotNull(savedStateHandle.get<TimeOfDay>("timeOfDay"))
    private val entryPoint: AudioPlayerEntryPoint =
        requireNotNull(savedStateHandle.get<AudioPlayerEntryPoint>("entryPoint"))

    override val container = container<AudioPlayerState, AudioPlayerSideEffect>(
        initialState = AudioPlayerState(entryPoint = entryPoint, timeOfDay = timeOfDay),
        onCreate = {
            observePremium()
            if (entryPoint == AudioPlayerEntryPoint.ONBOARDING) {
                analyticSender.introFinalShow()
                syncAccompaniments()
//                syncTodayAccompaniment()
            } else {
                intent {
                    val day = appPreferences.getCompletedDays()
                    val isDemo = profilePreferences.getIsDemo()
                    if (isDemo) {
                        analyticSender.audioSessionDemoShow(
                            timeOfDay.toEventParam(),
                            day,
                            accompanimentId,
                            state.selectedAvatar?.id ?: 0
                        )
                    } else {
                        analyticSender.audioSessionShow(
                            timeOfDay.toEventParam(),
                            day,
                            accompanimentId,
                            state.selectedAvatar?.id ?: 0
                        )
                    }
                }
                getAccompaniment()
            }
        }
    )

    private var isPlayClickedOnce = false

    fun syncAccompaniments() {
        viewModelScope.launch {
            reduceState { copy(playState = PlayState.LOADING, errorState = null) }
            syncResourcesUseCase.invoke().onSuccess {
                Timber.tag(logTag()).d("Resources synced successfully")
            }.onFailure {
                Timber.tag(logTag()).d("Error syncing resources: ${it.message}")
            }
            syncAccompanimentsUseCase.invoke(true).onSuccess {
                analyticSender.introPreparingComplete()
                getAccompaniment()
            }.onFailure {
                analyticSender.introPreparingFailed()
                Timber.tag(logTag()).d("Error syncing accompaniments: ${it.message}")
                reduceState {
                    copy(
                        playState = PlayState.LOADING,
                        errorState = it.mapToErrorViewState()
                    )
                }
                postEffect(AudioPlayerSideEffect.NavigateHome)
            }
        }
    }

    fun getMusicUrlForTimeOfDay(): Uri? {
        val state = container.stateFlow.value
        val accompanimentWithItems = state.accompanimentWithItems ?: return null
        val accompaniment = accompanimentWithItems.accompaniment
        val gender = state.gender
        val isMale = gender == Gender.MALE

        val url = when (timeOfDay) {
            TimeOfDay.MORNING -> {
                if (!isMale) accompaniment.morningFemaleAudioUrl else accompaniment.morningMaleAudioUrl
            }

            TimeOfDay.DAYTIME -> {
                val item = accompanimentWithItems.items.first { it.timeOfDay == TimeOfDay.DAYTIME }
                val nextPhrase = accompaniment.phrases.firstOrNull { it.id !in item.viewedPhraseId }

                nextPhrase?.let {
                    if (!isMale) it.femaleAudioUrl else it.maleAudioUrl
                } ?: finishedPhraseFallback(isMale)
            }

            TimeOfDay.EVENING -> {
                if (!isMale) accompaniment.eveningFemaleAudioUrl else accompaniment.eveningMaleAudioUrl
            }
        }

        return url?.toUri()
    }

    private fun finishedPhraseFallback(isMale: Boolean): String {
        val resId = if (!isMale) {
            R.raw.voice_finished_phrase_female
        } else {
            R.raw.voice_finished_phrase_male
        }

        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .path(resId.toString())
            .build()
            .toString()
    }

    fun getCurrentSoundEffectUi(): SoundEffectUi? {
        val state = container.stateFlow.value
        val index = state.selectedSoundEffectIndex
        return state.soundEffectsList.getOrNull(index)
    }

    private fun getAccompaniment() {
        viewModelScope.launch {
            val isDemo = profilePreferences.getIsDemo()
            val day = appPreferences.getCompletedDays()
            val accompanimentWithItems =
                getAccompanimentWithItemsUseCase(accompanimentId) ?: return@launch
            val accompaniment = accompanimentWithItems.accompaniment
            val title = when (timeOfDay) {
                TimeOfDay.MORNING -> context.getString(R.string.now_playing_vibes_morning)
                TimeOfDay.DAYTIME -> context.getString(R.string.now_playing_vibes_daytime)
                TimeOfDay.EVENING -> context.getString(R.string.now_playing_vibes_evening)
            }

            val subtitle = when (timeOfDay) {
                TimeOfDay.MORNING -> accompaniment.morningTitle
                TimeOfDay.DAYTIME -> {
                    val currentPhrase =
                        accompanimentWithItems.items.firstOrNull { it.timeOfDay == TimeOfDay.DAYTIME }
                            ?.let { item ->
                                accompaniment.phrases.firstOrNull { it.id !in item.viewedPhraseId }
                            }
                    currentPhrase?.daytimeTitle
                        ?: context.getString(R.string.vibes_daytime_completed)
                }

                TimeOfDay.EVENING -> accompaniment.eveningTitle
            }

            val gender = profilePreferences.getGender()
            val isMale = gender == Gender.MALE

            val audioUrl = when (timeOfDay) {
                TimeOfDay.MORNING -> accompaniment.morningMusicUrl
                TimeOfDay.DAYTIME -> accompaniment.daytimeMusicUrl
                TimeOfDay.EVENING -> accompaniment.eveningMusicUrl
                    ?: if (!isMale) accompaniment.eveningFemaleAudioUrl else accompaniment.eveningMaleAudioUrl
            }
            val imageUrl = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .path(R.drawable.ic_launcher_playstore.toString())
                .build()
                .toString()

            val soundEffects =
                getResourcesUseCase(CLASS_ALIAS_VOICE_MUSIC).map { it.toSoundEffectUi() }
            val avatars = getResourcesUseCase(CLASS_ALIAS_VOICE_AVATAR).map { it.toAvatarUi() }
                .toMutableList()
            val customAvatars = appPreferences.getCustomAvatarUris().map { entry ->
                if (entry.contains("|")) {
                    val parts = entry.split("|", limit = 2)
                    AvatarUi(
                        id = parts[0].hashCode(),
                        title = "Custom",
                        imageUrl = parts[1],
                        isCustom = true
                    )
                } else {
                    AvatarUi(
                        id = entry.hashCode(),
                        title = "Custom",
                        imageUrl = entry,
                        isCustom = true
                    )
                }
            }
            avatars.addAll(0, customAvatars)
            val avatarPreviewIds = configFetcher.getVoiceAvatarPreviewsIds()
            val dayUnlockConfig = configFetcher.getDemoUnlockDayConfig()
            val shareMessage = configFetcher.getShareMessage()

            val currentItem = accompanimentWithItems.items.firstOrNull { it.timeOfDay == timeOfDay }
            val isRated = currentItem?.isRated ?: false
            val rating = currentItem?.rating ?: 0
            reduceState {
                copy(
                    accompanimentWithItems = accompanimentWithItems,
                    title = subtitle,
                    subtitle = subtitle,
                    imageUrl = imageUrl,
                    audioUrl = audioUrl,
                    avatarsList = avatars,
                    avatarPreviewIds = avatarPreviewIds,
                    soundEffectsList = soundEffects,
                    gender = gender,
                    unlockDayConfig = dayUnlockConfig,
                    isDemo = isDemo,
                    day = day,
                    shareText = shareMessage.orEmpty(),
                    rating = rating,
                    isRated = isRated
                )
            }
            if (entryPoint == AudioPlayerEntryPoint.ONBOARDING) {
                reduceState { copy(playState = PlayState.LOADED) }
                delay(2000)
                reduceState { copy(playState = PlayState.READY) }
            }
        }
    }

    fun onPageSelected(index: Int) {
        intent {
            if (index < 0 || index >= state.pages.size) return@intent
            reduce { state.copy(currentPageIndex = index) }
        }
    }

    fun onNavigateToAvatars() {
        analyticSender.audioSessionAvatarsClick()
        reduceState { copy(currentPageIndex = 1) }
    }

    fun onNavigateToPlayer() {
        reduceState { copy(currentPageIndex = 0) }
    }

    fun onSoundEffectSelected(index: Int) {
        intent {
            if (state.selectedSoundEffectIndex == index) return@intent
            val soundEffect = state.soundEffectsList.getOrNull(index) ?: return@intent
            analyticSender.voiceMusicsSelect(soundEffect.id)
            reduce { state.copy(selectedSoundEffectIndex = index) }
        }
    }

    fun onMuteClicked() {
        intent {
            reduce { state.copy(selectedSoundEffectIndex = -1) }
        }
    }

    fun onAvatarSelected(avatar: AvatarUi?) {
        intent {
            if (state.isEditMode) {
                if (avatar != null) {
                    val selectedIds = state.selectedAvatarsIds.toMutableList()
                    if (selectedIds.contains(avatar.id)) {
                        selectedIds.remove(avatar.id)
                    } else {
                        selectedIds.add(avatar.id)
                    }
                    reduce { state.copy(selectedAvatarsIds = selectedIds) }
                }
            } else {
                reduceState { copy(selectedAvatar = avatar, showAvatarChangedToast = true) }
            }
        }
    }

    fun onDismissAvatarChangedToast() {
        reduceState { copy(showAvatarChangedToast = false) }
    }

    fun onPlayStateChanged(playState: PlayState) {
        intent {
            if (playState == PlayState.PLAYING) {
                startTrackingPlayback()
            } else {
                stopTrackingPlayback()
            }
            reduce {
                state.copy(
                    playState = playState
                )
            }
        }
    }

    private var playbackTrackingJob: kotlinx.coroutines.Job? = null

    private fun startTrackingPlayback() {
        if (playbackTrackingJob?.isActive == true) return
        playbackTrackingJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                appPreferences.incrementDailyPlayedSeconds(1)
            }
        }
    }

    private fun stopTrackingPlayback() {
        playbackTrackingJob?.cancel()
        playbackTrackingJob = null
    }

    fun savePlaybackProgress(progress: Float, isRateShown: Boolean = false) {
        stopTrackingPlayback()
        intent {
            viewModelScope.async {
                val state = container.stateFlow.value
                val accompanimentWithItems = state.accompanimentWithItems ?: return@async
                saveAccompanimentProgressUseCase.invoke(
                    accompanimentWithItems = accompanimentWithItems,
                    playbackProgress = progress,
                    timeOfDay = timeOfDay
                )
            }.await()
            if (progress == 1f) {
                appPreferences.incrementDailyCompletedAccompanimentsCount()
                if (state.isDemo) {
                    analyticSender.audioSessionCompleted(
                        timeOfDay.toEventParam(),
                        state.day,
                        accompanimentId,
                        state.selectedAvatar?.id ?: 0
                    )
                } else {
                    analyticSender.audioSessionDemoCompleted(
                        timeOfDay.toEventParam(),
                        state.day,
                        accompanimentId,
                        state.selectedAvatar?.id ?: 0
                    )
                }
            }
            val sideEffect = when (entryPoint) {
                AudioPlayerEntryPoint.DAY -> {
                    if (state.isDemo) {
                        analyticSender.audioSessionDemoClose(timeOfDay.toEventParam(), state.day)
                    } else {
                        analyticSender.audioSessionClose(timeOfDay.toEventParam(), state.day)
                    }
                    if (progress == 1f && (!state.isRated || isRateShown)) {
                        null
                    } else {
                        AudioPlayerSideEffect.NavigateBack
                    }
                }

//                AudioPlayerEntryPoint.ONBOARDING -> AudioPlayerSideEffect.NavigateHome
                AudioPlayerEntryPoint.ONBOARDING -> {
                    if (state.isPremium) {
                        AudioPlayerSideEffect.NavigateHome
                    } else {
                        AudioPlayerSideEffect.NavigatePaywall
                    }
                }
            }
            sideEffect?.let { postSideEffect(it) }
        }
    }

    fun onNavigateHome() {
        intent {
            postSideEffect(AudioPlayerSideEffect.NavigateHome)
        }
    }

    fun checkNetwork(onNetworkAvailable: () -> Unit = {}) {
        intent {
            val isNetworkAvailable = checkInternetConnectionUseCase.invoke()
            if (isNetworkAvailable) {
                onNetworkAvailable()
            }
            reduce {
                state.copy(
                    isNetworkAvailable = isNetworkAvailable
                )
            }
        }
    }

    fun onRetryClicked() {
        viewModelScope.launch {
//            checkNetwork() {
            if (entryPoint == AudioPlayerEntryPoint.ONBOARDING) {
                syncAccompaniments()
            } else {
                getAccompaniment()
            }
        }
    }

    fun onDownloadsClicked() {
        intent {
            postSideEffect(AudioPlayerSideEffect.NavigateDownloads)
        }
    }

    fun onListenLaterClicked() {
        intent {
            val sideEffect = when (entryPoint) {
                AudioPlayerEntryPoint.DAY -> AudioPlayerSideEffect.NavigateBack
                AudioPlayerEntryPoint.ONBOARDING -> {
                    if (state.isPremium) {
                        AudioPlayerSideEffect.NavigateHome
                    } else {
                        AudioPlayerSideEffect.NavigatePaywall
                    }
                }
            }
            postSideEffect(sideEffect)
        }
    }

    fun logPlayClicked() {
        if (entryPoint != AudioPlayerEntryPoint.ONBOARDING) {
            if (isPlayClickedOnce) return
            isPlayClickedOnce = true
            analyticSender.introFirstPlayClick()
        } else {
            intent {
                if (state.isDemo) {
                    analyticSender.audioSessionDemoPlayClick(timeOfDay.toEventParam(), state.day)
                } else {
                    analyticSender.audioSessionPlayClick(timeOfDay.toEventParam(), state.day)
                }
            }
        }
    }

    fun logOnSeek() {
        if (entryPoint != AudioPlayerEntryPoint.ONBOARDING) {
            intent {
                if (state.isDemo) {
                    analyticSender.audioSessionDemoSeek(timeOfDay.toEventParam(), state.day)
                } else {
                    analyticSender.audioSessionSeek(timeOfDay.toEventParam(), state.day)
                }
            }
        }
    }

    fun updateRating(rating: Int?) {
        intent {
            val accompanimentWithItems = state.accompanimentWithItems ?: return@intent
            val currentItem = accompanimentWithItems.items.firstOrNull { it.timeOfDay == timeOfDay }
                ?: return@intent
            viewModelScope.launch(Dispatchers.IO) {
                accompanimentItemRepository.updateRatingById(
                    currentItem.id,
                    isRated = true,
                    rating = rating ?: 0
                )
            }
            reduceState { copy(rating = rating ?: 0, isRated = true) }
        }
    }

    fun submitFeedback(rating: Int, message: String?) {
        intent {
            state.accompanimentWithItems?.let {
                sendRatingCommentUseCase.invoke(
                    accompanimentWithItems = it,
                    timeOfDay = timeOfDay,
                    title = state.title.orEmpty(),
                    phraseId = state.accompanimentWithItems?.items?.firstOrNull { it.timeOfDay == timeOfDay }?.viewedPhraseId?.lastOrNull()
                        ?: 0,
                    rating = state.rating,
                    isDemo = state.isDemo,
                    comment = message
                )
            }
            postSideEffect(AudioPlayerSideEffect.NavigateBack)
        }
    }

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium ->
                reduceState { copy(isPremium = isPremium) }
            }
        }
    }

    fun onEditClicked() {
        reduceState { copy(isEditMode = true) }
    }

    fun onSelectAllClicked() {
        intent {
            val selectedIds = state.avatarsList.map { it.id }
            reduce { state.copy(selectedAvatarsIds = selectedIds) }
        }
    }

    fun onAvatarSelectedEdit(id: Int) {
        intent {
            val selectedIds = state.selectedAvatarsIds.toMutableList()
            if (selectedIds.contains(id)) {
                selectedIds.remove(id)
            } else {
                selectedIds.add(id)
            }
            reduce { state.copy(selectedAvatarsIds = selectedIds) }
        }
    }

    fun onDeleteSelectedClicked() {
        intent {
            val selectedIds = state.selectedAvatarsIds
            if (selectedIds.isNotEmpty()) {
                appPreferences.addDeletedAvatarIds(selectedIds)
                val updatedAvatars = state.avatarsList.filterNot { selectedIds.contains(it.id) }
                reduce {
                    state.copy(
                        avatarsList = updatedAvatars,
                        selectedAvatarsIds = emptyList(),
                        selectedAvatar = if (selectedIds.contains(state.selectedAvatar?.id)) null else state.selectedAvatar
                    )
                }
            }
        }
    }

    fun onDeleteClicked(id: Int) {
        intent {
            appPreferences.addDeletedAvatarIds(listOf(id))
            val updatedAvatars = state.avatarsList.filterNot { it.id == id }
            reduce {
                state.copy(
                    avatarsList = updatedAvatars,
                    selectedAvatarsIds = state.selectedAvatarsIds.filterNot { it == id },
                    selectedAvatar = if (state.selectedAvatar?.id == id) null else state.selectedAvatar
                )
            }
        }
    }

    fun onDoneClicked() {
        intent {
            reduceState { copy(isEditMode = false, selectedAvatarsIds = emptyList()) }
        }
    }

    fun onAddAvatarClicked() {
        intent {
            postSideEffect(AudioPlayerSideEffect.PickImageFromGallery)
        }
    }

    fun onImageSelected(uri: Uri) {
        intent {
            reduce { state.copy(isCropping = true, croppingImageUri = uri) }
        }
    }

    fun onCropDone(uri: Uri) {
        intent {
            val uriString = uri.toString()
            val uniqueId = java.util.UUID.randomUUID().toString()
            appPreferences.addCustomAvatarUri(uniqueId, uriString)
            val customAvatar = AvatarUi(
                id = uniqueId.hashCode(),
                title = "Custom",
                imageUrl = uriString,
                isCustom = true
            )
            val newList = state.avatarsList.toMutableList()
            newList.add(0, customAvatar)
            reduce {
                state.copy(
                    avatarsList = newList,
                    isCropping = false,
                    croppingImageUri = null
                )
            }
        }
    }

    fun onCropCancel() {
        reduceState { copy(isCropping = false, croppingImageUri = null) }
    }
}

sealed class PlayerPage() {
    object Vibes : PlayerPage()
    object Avatars : PlayerPage()
}

data class AudioPlayerState(
    val isNetworkAvailable: Boolean = true,
    val playState: PlayState = PlayState.LOADING,
    val accompanimentWithItems: AccompanimentWithItems? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val pages: List<PlayerPage> = listOf(PlayerPage.Vibes, PlayerPage.Avatars),
    val currentPageIndex: Int = 0,
    val avatarPreviewIds: List<Int> = emptyList(),
    val avatarPreviewUrl: String? = null,
    val selectedAvatar: AvatarUi? = null,
    val avatarsList: List<AvatarUi> = emptyList(),
    val soundEffectsList: List<SoundEffectUi> = emptyList(),
    val selectedSoundEffectIndex: Int = -1,
    val entryPoint: AudioPlayerEntryPoint,
    val timeOfDay: TimeOfDay = TimeOfDay.DAYTIME,
    val gender: Gender = Gender.UNSPECIFIED,
    val unlockDayConfig: DemoUnlockDayConfig? = null,
    val errorState: ErrorViewState? = null,
    val isDemo: Boolean = false,
    val isPremium: Boolean = false,
    val day: Int = 0,
    val shareText: String = "Share",
    val rating: Int = 0,
    val isRated: Boolean = false,
    val isEditMode: Boolean = false,
    val selectedAvatarsIds: List<Int> = emptyList(),
    val showAvatarChangedToast: Boolean = false,
    val isCropping: Boolean = false,
    val croppingImageUri: Uri? = null
)

@Immutable
data class AvatarUi(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val isCustom: Boolean = false
)

@Keep
enum class AudioPlayerEntryPoint { ONBOARDING, DAY }

sealed class AudioPlayerSideEffect {
    object NavigateBack : AudioPlayerSideEffect()
    object NavigateHome : AudioPlayerSideEffect()
    object NavigatePaywall : AudioPlayerSideEffect()
    object NavigateDownloads : AudioPlayerSideEffect()
    object PickImageFromGallery : AudioPlayerSideEffect()
}