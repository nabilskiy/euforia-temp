@file:kotlin.OptIn(ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.player.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.activity.result.contract.ActivityResultContracts
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
import digital.euforia.app.ui.navigation.Home
import digital.euforia.app.ui.player.audio.components.VolumeBottomSheet
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import timber.log.Timber

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
    val shared =
        rememberSharedContentState(key = "${state.accompanimentWithItems?.accompaniment?.id}+${state.timeOfDay.name}")
    val url = viewModel.getMusicUrlForTimeOfDay()

    val controller = rememberMediaController(
        timeOfDayUrl = url,
        playWhenReady = state.entryPoint == AudioPlayerEntryPoint.DAY,
        onIsPlayingChanged = { isPlaying ->
            viewModel.onPlayStateChanged(if (isPlaying) PlayState.PLAYING else PlayState.PAUSED)
        },
        onEnded = {
            //todo show paywall
            viewModel.savePlaybackProgress(1f)
        },
        onSeek = {
            viewModel.logOnSeek()
        }
    )
    SubscriptionActivityLauncher(
        screenId = 18,
        onSuccess = { viewModel.onNavigateHome() }
    ) { launchSubscriptionActivity ->
        val wrappedLaunch = {
            controller?.pause()
            launchSubscriptionActivity()
        }
        viewModel.collectSideEffect { sideEffect ->
            handleSideEffect(sideEffect, navController, wrappedLaunch)
        }


        // Release player when leaving the screen
        DisposableEffect(Unit) {
            onDispose {
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
            ui = ui,
            currentTimeMs = currentMs,
            durationMs = durationMs,
            shared = shared,
            animatedVisibilityScope = animatedVisibilityScope,
            isNetworkAvailable = state.isNetworkAvailable,
            errorState = state.errorState,
            logListenLaterEvent = viewModel::onListenLaterClicked,
            onBack = {
                if (state.pages.getOrNull(state.currentPageIndex) == PlayerPage.Avatars) {
                    viewModel.onNavigateToPlayer()
                } else {
                    viewModel.savePlaybackProgress(currentMs / durationMs)
                    navController.popBackStack()
                }
            },
            onPageSelected = viewModel::onPageSelected,
            onPlay = {
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
            saveProgress = { viewModel.savePlaybackProgress(currentMs / durationMs) },
            onRetryClick = viewModel::onRetryClicked,
            onDownloadsClick = viewModel::onDownloadsClicked
        )
    }
}


// Send via the same MediaController you already hold
fun playSfx(controller: MediaController, url: String, vol: Float) {
    val cmd = SessionCommand("play_sfx", Bundle.EMPTY)
    val args = Bundle().apply {
        putString("url", url)
        putFloat("volume", vol)
    }
    controller.sendCustomCommand(cmd, args)
}

fun stopSfx(controller: MediaController) {
    controller.sendCustomCommand(SessionCommand("stop_sfx", Bundle.EMPTY), Bundle.EMPTY)
}

fun setSfxVolume(controller: MediaController, vol: Float) {
    val cmd = SessionCommand("volume_sfx", Bundle.EMPTY)
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
        AudioPlayerSideEffect.NavigateHome -> navController.navigate(Home) {
            popUpTo(Home) { inclusive = false }
        }

        AudioPlayerSideEffect.NavigatePaywall -> {
            launchSubscriptionActivity()
            navController.navigate(Home) {
                popUpTo(Home) { inclusive = false }
            }
        }

        else -> {}
    }
}