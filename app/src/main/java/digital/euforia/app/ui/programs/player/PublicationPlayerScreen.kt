package digital.euforia.app.ui.programs.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.components.VolumeBottomSheet
import digital.euforia.app.ui.player.audio.playSfx
import digital.euforia.app.ui.player.audio.setSfxVolume
import digital.euforia.app.ui.player.audio.stopSfx
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.theme.AvatarBackground
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.formatDuration
import digital.euforia.app.ui.util.sharePublication
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.PublicationOptionMenu
import digital.euforia.app.ui.util.widget.SoundEffectUi
import digital.euforia.app.ui.util.widget.SoundsEffectsView
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import timber.log.Timber

@Composable
fun PublicationPlayerScreen(
    navController: NavHostController,
    viewModel: PublicationPlayerViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    val activity = LocalContext.current.findActivity()

    ApplyExerciseScreenOrientation(activity, state.publicationInfo?.publicationType)
    ApplyExerciseScreenSystemBars(activity)
    ReleasePlaybackOnDispose(activity, state.publicationInfo?.id, viewModel)

    NavBarlessScreen(navBarVisibilityState) {
        PublicationPlayerContent(
            publicationInfo = state.publicationInfo,
            navController = navController,
            isPremium = state.isPremium,
            isLoading = state.isLoading,
            errorState = state.errorState,
            soundEffects = state.soundEffectsList,
            selectedSoundIndex = state.selectedSoundEffectIndex,
            playlist = state.playlist,
            onPublicationSelected = viewModel::onPublicationSelected,
            selectedSoundTitle = { state.soundEffectsList.getOrNull(state.selectedSoundEffectIndex)?.title.orEmpty() },
            getController = { ctx -> viewModel.getOrCreateController(ctx) },
            onPrepareAndPlay = { ex -> viewModel.prepareAndPlay(ex) },
            onRetryClick = { },
            onDownloadsClick = { },
            onSoundEffectClick = viewModel::onSoundEffectSelected,
            onMuteClick = viewModel::onMuteClicked,
            onFavouriteClick = viewModel::onFavouriteClicked
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PublicationPlayerContent(
    publicationInfo: PublicationInfo?,
    navController: NavHostController,
    isPremium: Boolean,
    isLoading: Boolean,
    errorState: ErrorViewState?,
    soundEffects: List<SoundEffectUi>,
    selectedSoundIndex: Int,
    playlist: PublicationsPlaylist?,
    onPublicationSelected: (PublicationInfo) -> Unit,
    selectedSoundTitle: () -> String,
    getController: suspend (Context) -> Player,
    onPrepareAndPlay: (PublicationInfo) -> Unit,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    onMuteClick: () -> Unit,
    onFavouriteClick: (PublicationInfo) -> Unit,
) {
    val videoUrl = publicationInfo?.videoUrl

    if (videoUrl.isNullOrBlank()) return
    var showSheet by remember { mutableStateOf(false) }
    val playlistSheetState = remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(0.35f) }

    val context = LocalContext.current
    var controllerPlayer by remember { mutableStateOf<Player?>(null) }
    LaunchedEffect(Unit) {
        controllerPlayer = getController(context)
    }

    var currentMediaId by remember(publicationInfo?.id) { mutableStateOf(publicationInfo?.id) }

    LaunchedEffect(controllerPlayer) {
        val player = controllerPlayer ?: return@LaunchedEffect
        while (true) {
            val mediaItem = player.currentMediaItem
            val id = (mediaItem?.localConfiguration?.tag as? PublicationInfo)?.id
            if (id != null && id != currentMediaId) {
                currentMediaId = id
            }
            delay(500)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            ProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorState != null) {
            ErrorView(
                modifier = Modifier.align(Alignment.Center),
                state = errorState,
                onRetryClick = onRetryClick,
                onDownloadsClick = onDownloadsClick
            )
        } else {
            ExerciseVideoPlayer(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                publicationInfo = publicationInfo,
                soundEffects = soundEffects,
                selectedSoundIndex = selectedSoundIndex,
                getController = getController,
                onPrepareAndPlay = onPrepareAndPlay,
                onClose = { navController.popBackStack() },
                onSoundEffectClick = { index ->
                    if (index != selectedSoundIndex) {
                        onSoundEffectClick(index)
                        soundEffects.getOrNull(index)?.let { sfx ->
                            (controllerPlayer as? androidx.media3.session.MediaController)?.let {
                                playSfx(it, sfx.audioUrl, volume)
                            }
                        }
                    } else {
                        showSheet = true
                    }
                },
                onMuteClick = {
                    onMuteClick()
                    (controllerPlayer as? androidx.media3.session.MediaController)?.let {
                        stopSfx(it)
                    }
                },
                onPlaylistClick = {
                    playlistSheetState.value = true
                },
                onFavouriteClick = onFavouriteClick
            )
        }

        if (playlistSheetState.value && playlist != null && publicationInfo != null) {
            PlaylistBottomSheet(
                playlist = playlist,
                selectedPublicationId = currentMediaId ?: publicationInfo.id,
                isPremium = isPremium,
                onPublicationSelected = onPublicationSelected,
                onDismiss = { playlistSheetState.value = false })
        }
        VolumeBottomSheet(
            visible = showSheet,
            value = volume,
            title = selectedSoundTitle(),
            onValueChange = {
                volume = it
                (controllerPlayer as? androidx.media3.session.MediaController)?.let { ctrl ->
                    setSfxVolume(ctrl, volume)
                }
            },
            onDone = {
                showSheet = false
            },
            onDismiss = { showSheet = false }
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExerciseVideoPlayer(
    modifier: Modifier = Modifier,
    publicationInfo: PublicationInfo,
    soundEffects: List<SoundEffectUi>,
    selectedSoundIndex: Int,
    getController: suspend (Context) -> Player,
    onPrepareAndPlay: (PublicationInfo) -> Unit,
    onClose: () -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    onMuteClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onFavouriteClick: (PublicationInfo) -> Unit,
) {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }
    val configuration = LocalConfiguration.current

    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    var controlsVisible by rememberSaveable { mutableStateOf(true) }
    var interactionTick by remember { mutableIntStateOf(0) }

    var player by remember { mutableStateOf<androidx.media3.common.Player?>(null) }
    LaunchedEffect(player == null) {
        if (player != null) return@LaunchedEffect
        val controller = getController(context)
        player = controller
        Timber.tag("PUBLICATION_PLAYBACK")
            .d("ExerciseVideoPlayer: LaunchedEffect player init, currentMediaId=${(controller.currentMediaItem?.localConfiguration?.tag as? PublicationInfo)?.id}")
        if (controller.playbackState == Player.STATE_IDLE || (controller.currentMediaItem?.localConfiguration?.tag as? PublicationInfo)?.id != publicationInfo.id) {
            onPrepareAndPlay(publicationInfo)
        }
    }

    LaunchedEffect(publicationInfo.id) {
        val controller = player ?: return@LaunchedEffect
        Timber.tag("PUBLICATION_PLAYBACK")
            .d("ExerciseVideoPlayer: LaunchedEffect publicationId changed to ${publicationInfo.id}, playerState=${controller.playbackState}, currentMediaId=${(controller.currentMediaItem?.localConfiguration?.tag as? PublicationInfo)?.id}")
        if (controller.playbackState == Player.STATE_IDLE || (controller.currentMediaItem?.localConfiguration?.tag as? PublicationInfo)?.id != publicationInfo.id) {
            onPrepareAndPlay(publicationInfo)
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying_: Boolean) {
                isPlaying = isPlaying_
            }
        }
        player?.addListener(listener)
        onDispose { player?.removeListener(listener) }
    }

    LaunchedEffect(controlsVisible, isPlaying, interactionTick) {
        if (controlsVisible && isPlaying) {
            delay(3000)
            controlsVisible = false
        }
    }

    LaunchedEffect(isFullscreen, configuration.orientation, publicationInfo.publicationType) {
        activity?.let { act ->
            if (publicationInfo.publicationType == PublicationType.MEDITATION) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else if (isFullscreen) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
        }
    }

    DisposableEffect(activity, isFullscreen, publicationInfo.publicationType) {
        onDispose {
            activity?.let { act ->
                val isChanging = try {
                    act.isChangingConfigurations
                } catch (_: Throwable) {
                    false
                }
                if (!isChanging) {
                    act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }
    val hazeState = rememberHazeState()

    Box(modifier = if (isFullscreen) Modifier.fillMaxSize() else modifier) {
        val p = player
        if (p != null) {
            val isMeditation =
                publicationInfo.publicationType == PublicationType.MEDITATION
            val selectedSfx = soundEffects.getOrNull(selectedSoundIndex)
            val coverUrl =
                if (isMeditation && selectedSfx != null && !selectedSfx.videoUrl.isNullOrBlank()) {
                    selectedSfx.videoUrl
                } else {
                    publicationInfo.categoryVideoCoverUrl
                }
            if (isMeditation && !coverUrl.isNullOrBlank()) {
                val coverPlayer = remember(coverUrl) {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(coverUrl))
                        repeatMode = Player.REPEAT_MODE_ALL
                        prepare()
                    }
                }
                DisposableEffect(coverPlayer) {
                    onDispose { coverPlayer.release() }
                }
                LaunchedEffect(coverPlayer, isPlaying) {
                    if (isPlaying) coverPlayer.play() else coverPlayer.pause()
                }
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = coverPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                    },
                    update = { view ->
                        view.player = coverPlayer
                    },
                    modifier = Modifier.matchParentSize()
                )
            } else {
                AndroidView(
                    factory = { ctx ->
                        LayoutInflater.from(ctx)
                            .inflate(R.layout.player_view_texture, null, false).also { root ->
                                root.findViewById<PlayerView>(R.id.player_view).apply {
                                    this.player = p
                                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    setKeepContentOnPlayerReset(true)
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    useController = false
                                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                }
                            }
                    },
                    update = { root ->
                        root.findViewById<PlayerView>(R.id.player_view).apply {
                            player = p
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    modifier = Modifier.matchParentSize().hazeSource(hazeState)
                )
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(controlsVisible) {
                    detectTapGestures(
                        onTap = {
                            if (controlsVisible) {
                                controlsVisible = false
                            } else {
                                controlsVisible = true
                                interactionTick++
                            }
                        }
                    )
                }
        )

        if (controlsVisible && player != null) {
            PlayerControlsOverlay(
                type = publicationInfo.publicationType,
                publicationInfo = publicationInfo,
                player = player!!,
                title = publicationInfo.title.orEmpty(),
                isFullscreen = isFullscreen,
                isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE,
                hazeState = hazeState,
                soundEffects = soundEffects,
                selectedSoundIndex = selectedSoundIndex,
                onToggleFullscreen = {
                    controlsVisible = true
                    interactionTick++
                    isFullscreen = !isFullscreen
                },
                onClose = {
                    controlsVisible = true
                    interactionTick++
                    onClose()
                },
                onUserInteraction = {
                    controlsVisible = true
                    interactionTick++
                },
                onSoundEffectClick = onSoundEffectClick,
                onMuteClick = onMuteClick,
                onPlaylistClick = onPlaylistClick,
                onFavouriteClick = onFavouriteClick
            )
        }
    }
}

private fun handleSideEffect(sideEffect: PublicationPlayerSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> this.baseContext.findActivity()
    else -> null
}

@Composable
private fun BoxScope.PlayerControlsOverlay(
    type: PublicationType,
    publicationInfo: PublicationInfo,
    player: Player,
    title: String,
    isFullscreen: Boolean,
    isLandscape: Boolean,
    hazeState: HazeState,
    soundEffects: List<SoundEffectUi>,
    selectedSoundIndex: Int,
    onToggleFullscreen: () -> Unit,
    onClose: () -> Unit,
    onUserInteraction: () -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    onMuteClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onFavouriteClick: (PublicationInfo) -> Unit,
) {
    var durationMs by remember {
        androidx.compose.runtime.mutableLongStateOf(
            player.duration.coerceAtLeast(
                0L
            )
        )
    }
    var positionMs by remember { androidx.compose.runtime.mutableLongStateOf(player.currentPosition) }
    var bufferedPositionMs by remember { androidx.compose.runtime.mutableLongStateOf(player.bufferedPosition) }
    var playWhenReady by remember { mutableStateOf(player.playWhenReady) }
    var currentTitle by remember(title) { mutableStateOf(title) }

    LaunchedEffect(player) {
        while (true) {
            durationMs = player.duration.coerceAtLeast(0L)
            positionMs = player.currentPosition
            bufferedPositionMs = player.bufferedPosition
            playWhenReady = player.playWhenReady

            val mediaTitle = player.mediaMetadata.title?.toString()
            if (!mediaTitle.isNullOrBlank()) {
                currentTitle = mediaTitle
            }

            // TODO: remove before pr
            if (System.currentTimeMillis() % 1000 < 300) {
                Timber.tag("PUBLICATION_PLAYBACK")
                    .d("PlayerState: playing=${player.isPlaying}, state=${player.playbackState}, ready=${player.playWhenReady}, pos=$positionMs/$durationMs")
            }
            delay(250)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(color = Black.copy(alpha = 0.3f))) {
        ControlsTopBar(
            title = currentTitle,
            publicationInfo = publicationInfo,
            isLandscape = isLandscape,
            isFullscreen = isFullscreen,
            onClose = {
                onUserInteraction(); onClose()
            },
            onToggleFullscreen = {
                onUserInteraction(); onToggleFullscreen()
            },
            onFavouriteClick = onFavouriteClick
        )

        PlayPauseButton(
            playing = playWhenReady,
            onToggle = {
                onUserInteraction()
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            },
            hazeState = hazeState,
        )

        BottomControls(
            type = type,
            durationMs = durationMs,
            positionMs = positionMs,
            bufferedPositionMs = bufferedPositionMs,
            isLandscape = isLandscape,
            isFullscreen = isFullscreen,
            soundEffects = soundEffects,
            selectedSoundIndex = selectedSoundIndex,
            onToggleFullscreen = {
                onUserInteraction(); onToggleFullscreen()
            },
            onSeek = { pos ->
                player.seekTo(pos)
                onUserInteraction()
            },
            onSoundEffectClick = onSoundEffectClick,
            onMuteClick = onMuteClick,
            onPlaylistClick = onPlaylistClick
        )
    }
}

@Composable
private fun ApplyExerciseScreenOrientation(
    activity: Activity?,
    publicationType: PublicationType?
) {
    DisposableEffect(activity, publicationType) {
        val previous = activity?.requestedOrientation
        if (publicationType == PublicationType.MEDITATION) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
        onDispose {
            val isChanging = try {
                activity?.isChangingConfigurations == true
            } catch (_: Throwable) {
                false
            }
            if (!isChanging) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                previous
            }
        }
    }
}

@Composable
private fun ApplyExerciseScreenSystemBars(activity: Activity?) {
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    DisposableEffect(activity, isLandscape) {
        activity?.let { act ->
            val window = act.window
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            if (isLandscape) {
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            activity?.let { act ->
                val isChanging = try {
                    act.isChangingConfigurations
                } catch (_: Throwable) {
                    false
                }
                if (!isChanging) {
                    val window = act.window
                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                    controller.show(WindowInsetsCompat.Type.systemBars())
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            }
        }
    }
}

@Composable
private fun ReleasePlaybackOnDispose(
    activity: Activity?,
    key: Any?,
    viewModel: PublicationPlayerViewModel
) {
    // We want to stop playback only when the screen is actually closed, 
    // not when the 'key' (publication ID) changes, because changing the key 
    // happens when we transition between items in a playlist.
    DisposableEffect(activity) {
        Timber.tag("PUBLICATION_PLAYBACK").d("ReleasePlaybackOnDispose: init")
        onDispose {
            val isChanging = try {
                activity?.isChangingConfigurations == true
            } catch (_: Throwable) {
                false
            }
            Timber.tag("PUBLICATION_PLAYBACK")
                .d("ReleasePlaybackOnDispose: onDispose isChanging=$isChanging")
            if (!isChanging) {
                viewModel.stopPlaybackAndRelease(stopService = true)
            }
        }
    }
}

@Composable
private fun BoxScope.ControlsTopBar(
    title: String,
    publicationInfo: PublicationInfo,
    isLandscape: Boolean,
    isFullscreen: Boolean,
    onClose: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onFavouriteClick: (PublicationInfo) -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = White
            )
        }
        Text(
            text = title,
            color = White,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
            textAlign = TextAlign.Center,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLandscape) {
                IconButton(onClick = onToggleFullscreen) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rotate),
                        contentDescription = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                        tint = White
                    )
                }
            }
            PublicationOptionMenu(
                isFavourite = publicationInfo.isFavourite,
                onAddFavouriteClick = { onFavouriteClick(publicationInfo) },
                onShareClick = {
                    sharePublication(
                        context = context,
                        publicationType = publicationInfo.publicationType,
                        id = publicationInfo.id
                    )
                },
                onReportErrorClick = {}
            )
        }
    }
}

@Composable
private fun BoxScope.PlayPauseButton(
    playing: Boolean,
    onToggle: () -> Unit,
    hazeState: HazeState,
) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .clip(CircleShape)
            .size(64.dp)
            .align(Alignment.Center)
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin(AvatarBackground))
            .zIndex(1f)
    ) {
        Icon(
            painter = painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play),
            contentDescription = if (playing) "Pause" else "Play",
            tint = White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun BoxScope.BottomControls(
    type: PublicationType,
    durationMs: Long,
    positionMs: Long,
    bufferedPositionMs: Long,
    isLandscape: Boolean,
    isFullscreen: Boolean,
    selectedSoundIndex: Int,
    soundEffects: List<SoundEffectUi>,
    onToggleFullscreen: () -> Unit,
    onSeek: (Long) -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    onMuteClick: () -> Unit,
    onPlaylistClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        SoundsEffectsView(
            soundsEffects = soundEffects,
            selectedIndex = selectedSoundIndex,
            avatarPreviewUrl = null,
            isHintShown = false,
            isAvatarItemShown = false,
            onAvatarClick = {},
            onMuteClick = onMuteClick,
            onClick = onSoundEffectClick
        )

        AndroidView(
            factory = { ctx ->
                DefaultTimeBar(ctx).apply {
                    setPlayedColor(White.toArgb())
                    setBufferedColor(DarkGray.toArgb())
                    setUnplayedColor(NavBarBackground.toArgb())
                    setScrubberColor(White.toArgb())
                    addListener(object : TimeBar.OnScrubListener {
                        override fun onScrubStart(timeBar: TimeBar, position: Long) {}
                        override fun onScrubMove(timeBar: TimeBar, position: Long) {}
                        override fun onScrubStop(
                            timeBar: TimeBar,
                            position: Long,
                            canceled: Boolean
                        ) {
                            if (!canceled) onSeek(position)
                        }
                    })
                }
            },
            update = { bar ->
                bar.setDuration(durationMs)
                bar.setPosition(positionMs)
                bar.setBufferedPosition(bufferedPositionMs)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = formatDuration(positionMs), color = White)
            if (!isLandscape) {
                if (type == PublicationType.MEDITATION) {
                    IconButton(onClick = onPlaylistClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_playlist),
                            contentDescription = "Playlist",
                            tint = White
                        )
                    }
                } else {
                    IconButton(onClick = onToggleFullscreen) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_rotate),
                            contentDescription = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                            tint = White
                        )
                    }

                }
            }
            Text(text = formatDuration(durationMs.coerceAtLeast(0L)), color = White)
        }
    }
}

