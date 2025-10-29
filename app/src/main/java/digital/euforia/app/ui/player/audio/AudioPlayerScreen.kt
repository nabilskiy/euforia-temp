package digital.euforia.app.ui.player.audio

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import digital.euforia.app.R
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.service.AudioMediaService
import digital.euforia.app.ui.player.audio.page.AvatarsPage
import digital.euforia.app.ui.player.audio.page.PlayerPage
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.vibe.PlayState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import kotlinx.coroutines.delay

val AppBarHeight = 66.dp

@OptIn(UnstableApi::class)
@Composable
fun AudioPlayerScreen(
    navController: NavHostController,
    viewModel: AudioPlayerViewModel
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val volumeSheetVisibleState = remember { mutableStateOf(false) }
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    // MediaController bound to our MediaSessionService
    val controllerHolder = remember { mutableStateOf<MediaController?>(null) }

    LaunchedEffect(state.accompaniment) {
        val url = viewModel.getMusicUrlForTimeOfDay()
        if (!url.isNullOrBlank() && controllerHolder.value == null) {
            val sessionToken =
                SessionToken(context, ComponentName(context, AudioMediaService::class.java))
            val future = MediaController.Builder(context, sessionToken).buildAsync()
            future.addListener({
                val controller = future.get()
                controllerHolder.value = controller
                controller.setMediaItem(MediaItem.fromUri(url))
                controller.prepare()
                controller.play()
            }, context.mainExecutor)
        }
    }

    // Release when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            // Stop any SFX playback
            context.startService(Intent(context, AudioMediaService::class.java).apply {
                action = AudioMediaService.ACTION_STOP_SFX
            })
            controllerHolder.value?.release()
            controllerHolder.value = null
        }
    }
    PreloadImages(
        avatarsList = state.avatarsList,
        soundsList = state.soundEffectsList
    )

    // Playback progress states (milliseconds)
    val currentPositionState = remember { mutableStateOf(0f) }
    val durationState = remember { mutableStateOf(1f) }

    // Poll controller for playback position updates
    LaunchedEffect(controllerHolder.value) {
        val controller = controllerHolder.value
        if (controller != null) {
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        viewModel.onPlayStateChanged(PlayState.PLAYING)
                    } else {
                        viewModel.onPlayStateChanged(PlayState.PAUSED)
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> { /* prepared */ }
                        Player.STATE_ENDED -> { /* finished */ }
                    }
                }
            })
            // initialize duration if available
            val dur = controller.duration
            if (dur > 0) durationState.value = dur.toFloat()
            while (controllerHolder.value === controller) {
                val d = controller.duration
                if (d > 0 && d.toFloat() != durationState.value) {
                    durationState.value = d.toFloat()
                }
                currentPositionState.value = controller.currentPosition.toFloat()
                delay(100)
            }
        }
    }

    AudioPlayerContent(
        entryPoint = state.entryPoint,
        timeOfDay = state.timeOfDay,
        title = state.title.orEmpty(),
        playState = state.playState,
        pages = state.pages,
        soundsEffects = state.soundEffectsList,
        currentPageIndex = state.currentPageIndex,
        avatarPreviewUrl = state.avatarPreviewUrl,
        avatarPreviewIds = state.avatarPreviewIds,
        avatarUi = state.selectedAvatar,
        avatarsList = state.avatarsList,
        selectedSoundIndex = state.selectedSoundEffectIndex,
        currentTimeMs = currentPositionState.value,
        durationMs = durationState.value,
        onMuteClick = {
            viewModel.onMuteClicked()
            controllerHolder.value?.let { stopSfx(it) }
        },
        onSoundEffectClick = { index ->
            if (index != state.selectedSoundEffectIndex) {
                viewModel.onSoundEffectSelected(index)
                state.soundEffectsList.getOrNull(index)?.let { sfx ->
                    controllerHolder.value?.let { playSfx(it, sfx.audioUrl, sfx.maxVolume) }
                }
            } else {
                volumeSheetVisibleState.value = true
            }
        },
        onPageSelected = viewModel::onPageSelected,
        navigateAvatars = viewModel::onNavigateToAvatars,
        navigatePlayer = viewModel::onNavigateToPlayer,
        navigateBack = { navController.popBackStack() },
        onAvatarClick = viewModel::onAvatarSelected,
        onPause = { controllerHolder.value?.pause() },
        onPlay = { controllerHolder.value?.play() },
        onSeekTo = { positionMs ->
            controllerHolder.value?.seekTo(positionMs.toLong())
        }
    )
}

@Composable
private fun AudioPlayerContent(
    entryPoint: AudioPlayerEntryPoint,
    timeOfDay: TimeOfDay,
    title: String,
    playState: PlayState,
    pages: List<PlayerPage>,
    currentPageIndex: Int,
    selectedSoundIndex: Int,
    avatarPreviewUrl: String?,
    avatarsList: List<AvatarUi>,
    avatarPreviewIds: List<Int>,
    avatarUi: AvatarUi?,
    soundsEffects: List<SoundEffectUi>,
    currentTimeMs: Float,
    durationMs: Float,
    onMuteClick: () -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    onPageSelected: (Int) -> Unit,
    navigateAvatars: () -> Unit,
    navigatePlayer: () -> Unit,
    navigateBack: () -> Unit,
    onAvatarClick: (AvatarUi?) -> Unit,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    onSeekTo: (Float) -> Unit
) {
    BackHandler { if (currentPageIndex == 1) navigatePlayer() else navigateBack() }
    val pagerState = rememberPagerState(initialPage = currentPageIndex) { pages.size }

    LaunchedEffect(currentPageIndex) {
        if (pagerState.currentPage != currentPageIndex) {
            pagerState.animateScrollToPage(currentPageIndex, animationSpec = tween(600))
        }
    }
//    subscribeToPagerUpdates(
//        coroutineScope = rememberCoroutineScope(),
//        pagerState = pagerState,
//        page = currentPageIndex
//    )
//    LaunchedEffect(pagerState) {
//        snapshotFlow { pagerState.currentPage }
//            .collect { page -> onPageSelected(page) }
//    }

    Box {
        HorizontalPager(
            modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp),
            state = pagerState,
            userScrollEnabled = false
        ) { position ->
            when (position) {
                0 -> PlayerPage(
                    entryPoint = entryPoint,
                    timeOfDay = timeOfDay,
                    title = title,
                    playState = playState,
                    selectedSoundIndex = selectedSoundIndex,
                    avatarPreviewUrl = avatarPreviewUrl,
                    avatarPreviewIds = avatarPreviewIds,
                    avatarUi = avatarUi,
                    avatarsList = avatarsList,
                    soundsEffects = soundsEffects,
                    currentTimeMs = currentTimeMs,
                    durationMs = durationMs,
                    onAvatarClick = navigateAvatars,
                    onMuteClick = onMuteClick,
                    onSoundEffectClick = onSoundEffectClick,
                    onPause = onPause,
                    onPlay = onPlay,
                    onSeekTo = onSeekTo
                )

                else -> AvatarsPage(
                    avatarsList = avatarsList,
                    selectedAvatar = avatarUi,
                    onAvatarClick = onAvatarClick
                )
            }
        }
        AppBar(currentPageIndex, onBackClick = {
            if (currentPageIndex == 1) navigatePlayer() else navigateBack()
        })
    }
}

private fun handleBackClick(
    pages: List<PlayerPage>,
    currentPageIndex: Int,
    navigatePlayer: () -> Unit,
    navigateBack: () -> Unit
) {
    if (pages[currentPageIndex] == PlayerPage.Avatars) {
        navigatePlayer()
    } else {
        navigateBack()
    }
}

@Composable
private fun AppBar(currentPageIndex: Int, onBackClick: () -> Unit) {
    AnimatedContent(
        targetState = currentPageIndex, label = "icon_transition",
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { target ->
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .statusBarsPadding()
                .heightIn(min = AppBarHeight)
        ) {
            when (target) {
                0 -> {
                    Icon(
                        modifier = Modifier.align(Alignment.CenterStart).size(24.dp)
                            .noRippleClickable(onClick = onBackClick),
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }

                else -> {
                    Icon(
                        modifier = Modifier.align(Alignment.CenterStart).size(24.dp)
                            .noRippleClickable(onClick = onBackClick),
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

private fun subscribeToPagerUpdates(
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    page: Int
) {
    coroutineScope.launch {
        if (pagerState.currentPage != page) {
            pagerState.animateScrollToPage(
                page = page,
                animationSpec = tween(durationMillis = 600)
            )
        }
    }
}

@Composable
fun PreloadImages(avatarsList: List<AvatarUi>, soundsList: List<SoundEffectUi>) {
    val imageLoader = LocalContext.current.imageLoader
    val context = LocalContext.current
    val soundSize = with(LocalDensity.current) { 128.dp.roundToPx() }
    val avatarSize = with(LocalDensity.current) { 256.dp.roundToPx() }

    LaunchedEffect(soundsList) {
        soundsList.forEach { sound ->
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(sound.imageUrl)
                    .diskCacheKey(sound.imageUrl)
                    .size(soundSize) // or use a concrete px size matching your cell
                    .allowHardware(true)
                    .build()
            )
        }
    }
    LaunchedEffect(avatarsList) {
        avatarsList.forEach { avatar ->
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(avatar.imageUrl)
                    .diskCacheKey(avatar.imageUrl)
                    .size(avatarSize) // or use a concrete px size matching your cell
                    .allowHardware(true)
                    .build()
            )
        }
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

private fun handleSideEffect(sideEffect: AudioPlayerSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}