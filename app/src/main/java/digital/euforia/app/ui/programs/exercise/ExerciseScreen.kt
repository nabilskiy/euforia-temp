package digital.euforia.app.ui.programs.exercise

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.programs.ExerciseUi
import digital.euforia.app.ui.theme.AvatarBackground
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.ProgressIndicator
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlinx.coroutines.delay

@Composable
fun ExerciseScreen(
    navController: NavHostController,
    viewModel: ExerciseViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    val activity = LocalContext.current.findActivity()

    ApplyExerciseScreenOrientation(activity)
    ApplyExerciseScreenSystemBars(activity)
    ReleasePlaybackOnDispose(activity, state.exercise?.id, viewModel)

    NavBarlessScreen(navBarVisibilityState) {
        ExerciseContent(
            exercise = state.exercise,
            navController = navController,
            isPremium = state.isPremium,
            isLoading = state.isLoading,
            errorState = state.errorState,
            getController = { ctx -> viewModel.getOrCreateController(ctx) },
            onPrepareAndPlay = { ex -> viewModel.prepareAndPlay(ex) },
            onRetryClick = { },
            onDownloadsClick = { }
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExerciseContent(
    exercise: ExerciseUi?,
    navController: NavHostController,
    isPremium: Boolean,
    isLoading: Boolean,
    errorState: ErrorViewState?,
    getController: suspend (Context) -> Player,
    onPrepareAndPlay: (ExerciseUi) -> Unit,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit
) {
    val videoUrl = exercise?.videoUrl

    if (videoUrl.isNullOrBlank()) return

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
                exercise = exercise,
                getController = getController,
                onPrepareAndPlay = onPrepareAndPlay,
                onClose = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExerciseVideoPlayer(
    exercise: ExerciseUi,
    getController: suspend (Context) -> Player,
    onPrepareAndPlay: (ExerciseUi) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }
    val configuration = LocalConfiguration.current

    var isFullscreen by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    var controlsVisible by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var interactionTick by remember { mutableIntStateOf(0) }

    var player by remember { mutableStateOf<androidx.media3.common.Player?>(null) }
    LaunchedEffect(exercise.id) {
        val controller = getController(context)
        onPrepareAndPlay(exercise)
        player = controller
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

    LaunchedEffect(isFullscreen, configuration.orientation) {
        activity?.let { act ->
            if (isFullscreen) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
        }
    }

    DisposableEffect(activity, isFullscreen) {
        onDispose {
            activity?.let { act ->
                val isChanging = try {
                    act.isChangingConfigurations
                } catch (_: Throwable) {
                    false
                }
                if (!isChanging) {
                    act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                }
            }
        }
    }
    val hazeState = dev.chrisbanes.haze.rememberHazeState()

    Box(modifier = if (isFullscreen) Modifier.fillMaxSize() else modifier) {
        val p = player
        if (p != null) AndroidView(
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
                player = player!!,
                title = exercise.name,
                isFullscreen = isFullscreen,
                isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE,
                hazeState = hazeState,
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
                }
            )
        }
    }
}

private fun handleSideEffect(sideEffect: ExerciseSideEffect) {
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
    player: Player,
    title: String,
    isFullscreen: Boolean,
    isLandscape: Boolean,
    hazeState: HazeState,
    onToggleFullscreen: () -> Unit,
    onClose: () -> Unit,
    onUserInteraction: () -> Unit
) {
    var durationMs by remember { androidx.compose.runtime.mutableLongStateOf(player.duration.coerceAtLeast(0L)) }
    var positionMs by remember { androidx.compose.runtime.mutableLongStateOf(player.currentPosition) }
    var bufferedPositionMs by remember { androidx.compose.runtime.mutableLongStateOf(player.bufferedPosition) }
    var playWhenReady by remember { mutableStateOf(player.playWhenReady) }

    LaunchedEffect(player) {
        while (true) {
            durationMs = player.duration.coerceAtLeast(0L)
            positionMs = player.currentPosition
            bufferedPositionMs = player.bufferedPosition
            playWhenReady = player.playWhenReady
            delay(250)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(color = Black.copy(alpha = 0.3f))) {
        ControlsTopBar(
            title = title,
            isLandscape = isLandscape,
            isFullscreen = isFullscreen,
            onClose = {
                onUserInteraction(); onClose()
            },
            onToggleFullscreen = {
                onUserInteraction(); onToggleFullscreen()
            },
        )

        PlayPauseButton(
            playing = playWhenReady,
            onToggle = {
                onUserInteraction()
                val newPlay = !playWhenReady
                player.playWhenReady = newPlay
                playWhenReady = newPlay
            },
            hazeState = hazeState,
        )

        BottomControls(
            durationMs = durationMs,
            positionMs = positionMs,
            bufferedPositionMs = bufferedPositionMs,
            isLandscape = isLandscape,
            isFullscreen = isFullscreen,
            onToggleFullscreen = {
                onUserInteraction(); onToggleFullscreen()
            },
            onSeek = { pos ->
                player.seekTo(pos)
                onUserInteraction()
            }
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val seconds = (totalSeconds % 60).toInt()
    val minutes = ((totalSeconds / 60) % 60).toInt()
    val hours = (totalSeconds / 3600).toInt()
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}


@Composable
private fun ApplyExerciseScreenOrientation(activity: Activity?) {
    DisposableEffect(activity) {
        val previous = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        onDispose {
            val isChanging = try { activity?.isChangingConfigurations == true } catch (_: Throwable) { false }
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
    DisposableEffect(activity) {
        activity?.let { act ->
            val window = act.window
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            activity?.let { act ->
                val isChanging = try { act.isChangingConfigurations } catch (_: Throwable) { false }
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
private fun ReleasePlaybackOnDispose(activity: Activity?, key: Any?, viewModel: ExerciseViewModel) {
    DisposableEffect(activity, key) {
        onDispose {
            val isChanging = try { activity?.isChangingConfigurations == true } catch (_: Throwable) { false }
            if (!isChanging) {
                viewModel.stopPlaybackAndRelease(stopService = true)
            }
        }
    }
}

@Composable
private fun BoxScope.ControlsTopBar(
    title: String,
    isLandscape: Boolean,
    isFullscreen: Boolean,
    onClose: () -> Unit,
    onToggleFullscreen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(painter = painterResource(R.drawable.ic_close), contentDescription = null, tint = White)
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
            IconButton(onClick = { }) {
                Icon(painter = painterResource(R.drawable.ic_menu), contentDescription = "Menu", tint = White)
            }
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

@Composable
private fun BoxScope.BottomControls(
    durationMs: Long,
    positionMs: Long,
    bufferedPositionMs: Long,
    isLandscape: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                DefaultTimeBar(ctx).apply {
                    setPlayedColor(White.toArgb())
                    setBufferedColor(DarkGray.toArgb())
                    setUnplayedColor(NavBarBackground.toArgb())
                    setScrubberColor(White.toArgb())
                    addListener(object : TimeBar.OnScrubListener {
                        override fun onScrubStart(timeBar: TimeBar, position: Long) { }
                        override fun onScrubMove(timeBar: TimeBar, position: Long) { }
                        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
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
            Text(text = formatTime(positionMs), color = White)
            if (!isLandscape) {
                IconButton(onClick = onToggleFullscreen) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rotate),
                        contentDescription = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                        tint = White
                    )
                }
            }
            Text(text = formatTime(durationMs.coerceAtLeast(0L)), color = White)
        }
    }
}