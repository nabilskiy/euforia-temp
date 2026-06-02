@file:kotlin.OptIn(ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.player.audio

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import digital.euforia.app.service.AudioPlaybackService
import digital.euforia.app.ui.util.widget.vibe.PlayState
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.media3.session.MediaController
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import digital.euforia.app.service.soundscapes.beginSoundscapeInterruption
import digital.euforia.app.service.soundscapes.endSoundscapeInterruption
import digital.euforia.app.ui.navigation.Home
import digital.euforia.app.ui.player.audio.components.VolumeBottomSheet
import digital.euforia.app.ui.util.SubscriptionActivityLauncher

@OptIn(UnstableApi::class)
@Composable
fun SharedTransitionScope.AudioPlayerScreen(
    navController: NavHostController,
    viewModel: AudioPlayerViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(0.35f) } // 0f..1f
//    val shared =
//        rememberSharedContentState(key = "${state.accompanimentWithItems?.accompaniment?.id}+${state.timeOfDay.name}")
    val shared = rememberSharedContentState(key = "${viewModel.accompanimentId}+${state.timeOfDay.name}")
    val isRateShown = remember { mutableStateOf(false) }
    val url = remember(state.accompanimentWithItems) {
        viewModel.getMusicUrlForTimeOfDay()
    }

    val controller = rememberMediaController(
        timeOfDayUrl = url,
        title = state.title,
        imageUrl = state.imageUrl,
        playWhenReady = state.entryPoint == AudioPlayerEntryPoint.DAY,
        onIsPlayingChanged = { isPlaying ->
            viewModel.onPlayStateChanged(if (isPlaying) PlayState.PLAYING else PlayState.PAUSED)
        },
        onEnded = {
            //todo show paywall
            viewModel.savePlaybackProgress(1f, isRateShown.value)
            if (state.entryPoint == AudioPlayerEntryPoint.DAY && !state.isRated) {
                isRateShown.value = true
            }
        },
        onSeek = {
            viewModel.logOnSeek()
        }
    )
    SubscriptionActivityLauncher(
        screenId = 18,
        onSuccess = { viewModel.onNavigateHome() },
        onClose = { viewModel.onNavigateHome() }
    ) { launchSubscriptionActivity ->
        val wrappedLaunch = {
            controller?.pause()
            launchSubscriptionActivity()
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                viewModel.onImageSelected(it)
            }
        }

        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                AudioPlayerSideEffect.PickImageFromGallery -> launcher.launch("image/*")
                else -> handleSideEffect(sideEffect, navController, wrappedLaunch)
            }
        }

        LaunchedEffect(
            controller,
            state.selectedSoundEffectIndex,
            state.soundEffectsList,
        ) {
            val mediaController = controller ?: return@LaunchedEffect
            if (state.selectedSoundEffectIndex < 0) {
                stopSfx(mediaController)
                return@LaunchedEffect
            }
            val sfx = state.soundEffectsList.getOrNull(state.selectedSoundEffectIndex) ?: return@LaunchedEffect
            if (sfx.audioUrl.isBlank()) return@LaunchedEffect
            playSfx(mediaController, sfx.audioUrl, volume)
        }

        // Release player when leaving the screen
        DisposableEffect(Unit) {
            onDispose {
                endSoundscapeInterruption(context, "audio_player_screen")
                controller?.let { c ->
                    c.pause()
                    c.clearMediaItems()
                    stopSfx(c)
                    c.release()
                }
                context.stopService(Intent(context, AudioPlaybackService::class.java))
            }
        }

        val (currentMs, durationMs) = rememberPlaybackProgress(controller)

        val sharedKey = state.accompanimentWithItems?.let { acc ->
            "acc_${acc.accompaniment.id}_${state.timeOfDay.name}"
        }

        val ui = AudioPlayerUiState(
            entryPoint = state.entryPoint,
            timeOfDay = state.timeOfDay,
            title = state.title.orEmpty(),
            playState = state.playState,
            pages = state.pages,
            currentPageIndex = state.currentPageIndex,
            selectedSoundIndex = state.selectedSoundEffectIndex,
            avatarPreviewUrl = state.avatarPreviewUrl,
            avatarsList = state.avatarsList,
            avatarPreviewIds = state.avatarPreviewIds,
            avatarUi = state.selectedAvatar,
            soundsEffects = state.soundEffectsList,
            isRated = state.isRated,
            sharedElementKey = sharedKey
        )
        VolumeBottomSheet(
            visible = showSheet,
            value = volume,
            title = viewModel.getCurrentSoundEffectUi()?.title.orEmpty(),
            onValueChange = {
                volume = it
                controller?.let { setSfxVolume(it, volume) }
            },
            onDone = {
                // player.volume = volume
                showSheet = false
            },
            onDismiss = { showSheet = false }
        )

        AudioPlayerScaffold(
            modifier = Modifier.sharedElement(shared, animatedVisibilityScope),
            isPremium = state.isPremium,
            isEditMode = state.isEditMode,
            ui = ui,
            currentTimeMs = currentMs,
            durationMs = durationMs,
            isRateShown = isRateShown,
            shared = shared,
            animatedVisibilityScope = animatedVisibilityScope,
            isNetworkAvailable = state.isNetworkAvailable,
            errorState = state.errorState,
            shareText = state.shareText,
            rating = state.rating,
            selectedAvatarIds = state.selectedAvatarsIds,
            logListenLaterEvent = viewModel::onListenLaterClicked,
            onSubmitClick = viewModel::submitFeedback,
            onBack = {
                if (state.pages.getOrNull(state.currentPageIndex) == PlayerPage.Avatars) {
                    viewModel.onNavigateToPlayer()
                } else {
                    viewModel.savePlaybackProgress(currentMs / durationMs, isRateShown.value)
                    navController.popBackStack()
                }
            },
            onPageSelected = viewModel::onPageSelected,
            onPlay = {
                beginSoundscapeInterruption(context, "audio_player_screen")
                controller?.play()
                viewModel.logPlayClicked()
            },
            onPause = { controller?.pause() },
            onSeekTo = { positionMs -> controller?.seekTo(positionMs.toLong()) },
            onMuteClick = {
                viewModel.onMuteClicked()
                controller?.let { stopSfx(it) }
            },
            onSoundEffectClick = { index ->
                if (index != state.selectedSoundEffectIndex) {
                    viewModel.onSoundEffectSelected(index)
                    state.soundEffectsList.getOrNull(index)?.let { sfx ->
                        controller?.let { playSfx(it, sfx.audioUrl, volume) }
                    }
                } else {
                    showSheet = true
                }
            },
            onAvatarClick = viewModel::onAvatarSelected,
            navigateAvatars = viewModel::onNavigateToAvatars,
            navigatePlayer = viewModel::onNavigateToPlayer,
            saveProgress = { viewModel.savePlaybackProgress(currentMs / durationMs, isRateShown.value) },
            onRetryClick = viewModel::onRetryClicked,
            onDownloadsClick = viewModel::onDownloadsClicked,
            onRatingUpdated = viewModel::updateRating,
            onEditClick = viewModel::onEditClicked,
            onSelectAllClick = viewModel::onSelectAllClicked,
            onSelect = viewModel::onAvatarSelectedEdit,
            onDeleteSelectedClick = viewModel::onDeleteSelectedClicked,
            onDeleteClick = viewModel::onDeleteClicked,
            onDoneClick = viewModel::onDoneClicked,
            onAddAvatarClick = viewModel::onAddAvatarClicked,
            isCropping = state.isCropping,
            croppingImageUri = state.croppingImageUri,
            onCropDone = viewModel::onCropDone,
            onCropCancel = viewModel::onCropCancel,
            showAvatarChangedToast = state.showAvatarChangedToast,
            onDismissAvatarChangedToast = viewModel::onDismissAvatarChangedToast
        )
    }
}


// Send via the same MediaController you already hold
fun playSfx(controller: MediaController, url: String, vol: Float) {
    if (url.isBlank()) return
    val args = Bundle().apply {
        putString("url", url)
        putFloat("volume", vol)
    }
    controller.sendCustomCommand(AudioPlaybackService.Commands.PLAY_SFX, args)
}

fun stopSfx(controller: MediaController) {
    controller.sendCustomCommand(AudioPlaybackService.Commands.STOP_SFX, Bundle.EMPTY)
}

fun setSfxVolume(controller: MediaController, vol: Float) {
    val cmd = AudioPlaybackService.Commands.VOLUME_SFX
    val args = Bundle().apply {
        putFloat("volume", vol)
    }
    controller.sendCustomCommand(cmd, args)
}

private fun handleSideEffect(
    sideEffect: AudioPlayerSideEffect, navController: NavHostController,
    launchSubscriptionActivity: () -> Unit,
) {
    when (sideEffect) {
        AudioPlayerSideEffect.NavigateBack -> navController.popBackStack()
        AudioPlayerSideEffect.NavigateHome -> navController.navigate(Home()) {
            popUpTo(Home()) { inclusive = true }
        }

        AudioPlayerSideEffect.NavigatePaywall -> {
            launchSubscriptionActivity()
        }

        else -> {}
    }
}