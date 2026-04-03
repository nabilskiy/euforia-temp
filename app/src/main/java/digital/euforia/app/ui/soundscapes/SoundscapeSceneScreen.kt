/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.hypot
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.formatDuration
import digital.euforia.app.ui.util.widget.MaxView
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlinx.coroutines.delay
import kotlin.math.min

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SoundscapeSceneScreen(
    navController: NavHostController,
    viewModel: SoundscapeSceneViewModel
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var selectedSoundLayerId by remember { mutableStateOf<Int?>(null) }
    var showMusicOptions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val musicSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var soundDragOffsets by remember(state.sceneId) { mutableStateOf(mapOf<Int, Offset>()) }

    LaunchedEffect(state.layers, selectedSoundLayerId) {
        val id = selectedSoundLayerId ?: return@LaunchedEffect
        if (state.layers.none { it.id == id }) {
            selectedSoundLayerId = null
        }
    }

    LaunchedEffect(selectedSoundLayerId) {
        if (selectedSoundLayerId != null) {
            showMusicOptions = false
        }
    }

    SubscriptionActivityLauncher { launchSubscription ->
        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                SoundscapeSceneSideEffect.NavigateToPaywall -> {
                    navController.popBackStack()
                    launchSubscription()
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            SoundscapeSceneBackground(
                imageUrl = state.imageUrl,
                videoUrl = state.videoUrl,
                musicUrl = state.sceneMusicUrl,
                isPlaying = state.isPlaying,
                musicVolume = state.musicVolume * state.sceneMusicVolumeFactor,
                onPlaybackProgress = { pos, dur ->
                    positionMs = pos
                    durationMs = dur
                }
            )

            // Vignette + bottom readability (aligned with iOS SoundStudio overlays)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.45f),
                            0.35f to Color.Transparent,
                            0.65f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.55f)
                        )
                    )
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 72.dp, bottom = 140.dp)
                    .zIndex(3f)
            ) {
                val btnSize = 56.dp
                val density = LocalDensity.current
                val maxWpx = with(density) { maxWidth.toPx() }
                val maxHpx = with(density) { maxHeight.toPx() }
                val btnPx = with(density) { btnSize.toPx() }
                val tapSlopPx = with(density) { 12.dp.toPx() }
                val visibleIds = state.layers.map { it.id }.toSet()
                state.soundFloatingButtons
                    .filter { it.id in visibleIds }
                    .forEach { btn ->
                        val extra = soundDragOffsets[btn.id] ?: Offset.Zero
                        val baseXpx = maxWpx * btn.posXFraction - btnPx / 2f
                        val baseYpx = maxHpx * btn.posYFraction - btnPx / 2f
                        val xPx = (baseXpx + extra.x).coerceIn(0f, (maxWpx - btnPx).coerceAtLeast(0f))
                        val yPx = (baseYpx + extra.y).coerceIn(0f, (maxHpx - btnPx).coerceAtLeast(0f))
                        val xOff = with(density) { xPx.toDp() }
                        val yOff = with(density) { yPx.toDp() }
                        SceneSoundFloatingButton(
                            imageUrl = btn.imageUrl,
                            contentDescription = btn.title,
                            modifier = Modifier
                                .offset(xOff, yOff)
                                .pointerInput(btn.id, maxWpx, maxHpx, btn.posXFraction, btn.posYFraction) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        var dragTotal = Offset.Zero
                                        var dragging = false
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Main)
                                            val change = event.changes.firstOrNull { it.id == down.id }
                                                ?: break
                                            if (change.changedToUp()) {
                                                if (!dragging && hypot(dragTotal.x, dragTotal.y) < tapSlopPx) {
                                                    showMusicOptions = false
                                                    selectedSoundLayerId = btn.id
                                                }
                                                break
                                            }
                                            val delta = change.positionChange()
                                            dragTotal += delta
                                            if (!dragging) {
                                                if (hypot(dragTotal.x, dragTotal.y) < tapSlopPx) continue
                                                dragging = true
                                            }
                                            change.consume()
                                            soundDragOffsets = soundDragOffsets.toMutableMap().apply {
                                                val cur = this[btn.id] ?: Offset.Zero
                                                val nx = cur + delta
                                                val candX = baseXpx + nx.x
                                                val candY = baseYpx + nx.y
                                                val cx = candX.coerceIn(0f, (maxWpx - btnPx).coerceAtLeast(0f))
                                                val cy = candY.coerceIn(0f, (maxHpx - btnPx).coerceAtLeast(0f))
                                                this[btn.id] = Offset(cx - baseXpx, cy - baseYpx)
                                            }
                                        }
                                    }
                                }
                        )
                    }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .zIndex(4f)
                    .fillMaxWidth()
            ) {
                SoundscapeSceneTopBar(
                    title = state.title.ifBlank { "Scene ${state.sceneId}" },
                    subtitle = state.subtitle,
                    showMaxBadge = state.isPro,
                    onClose = { navController.popBackStack() },
                    onSavePreset = viewModel::onSavePreset,
                    onDownload = viewModel::onDownloadScene,
                    onShare = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, state.title.ifBlank { context.getString(R.string.app_name) })
                        }
                        context.startActivity(Intent.createChooser(send, context.getString(R.string.share)))
                    }
                )
            }

            IconButton(
                onClick = {
                    selectedSoundLayerId = null
                    showMusicOptions = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .zIndex(6f)
                    .navigationBarsPadding()
                    .padding(end = 12.dp, bottom = 112.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, White.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_headphones),
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            SoundscapeScenePlayControl(
                isPlaying = state.isPlaying,
                positionMs = positionMs,
                durationMs = durationMs,
                onToggle = viewModel::onPlayPause,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(5f)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )

            val selectedLayer = selectedSoundLayerId?.let { id ->
                state.layers.firstOrNull { it.id == id }
            }
            val selectedButton = selectedSoundLayerId?.let { id ->
                state.soundFloatingButtons.firstOrNull { it.id == id }
            }

            if (showMusicOptions) {
                ModalBottomSheet(
                    onDismissRequest = { showMusicOptions = false },
                    sheetState = musicSheetState,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    containerColor = BottomSheetBackground,
                    dragHandle = {
                        Box(
                            Modifier
                                .padding(vertical = 10.dp)
                                .width(36.dp)
                                .height(4.dp)
                                .background(White.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
                        )
                    }
                ) {
                    MusicOptionsBottomSheetContent(
                        musicTitle = state.sceneMusicTitle.orEmpty(),
                        volume = state.musicVolume,
                        onVolumeChange = viewModel::onMusicVolume,
                        onChangeMusicClick = { /* TODO: track picker when available */ },
                        onDone = { showMusicOptions = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            } else if (selectedSoundLayerId != null && selectedLayer != null && selectedButton != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedSoundLayerId = null },
                    sheetState = sheetState,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    containerColor = BottomSheetBackground,
                    dragHandle = {
                        Box(
                            Modifier
                                .padding(vertical = 10.dp)
                                .width(36.dp)
                                .height(4.dp)
                                .background(White.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
                        )
                    }
                ) {
                    SoundLayerBottomSheetContent(
                        title = selectedButton.title,
                        volume = selectedLayer.volume,
                        onVolumeChange = { viewModel.onLayerVolume(selectedLayer.id, it) },
                        onDelete = {
                            viewModel.onLayerRemove(selectedLayer.id)
                            selectedSoundLayerId = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SceneSoundFloatingButton(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val borderColor = White.copy(alpha = 0.95f)
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .background(Color.Black.copy(alpha = 0.42f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier
                    .padding(10.dp)
                    .size(36.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_sounds),
                contentDescription = contentDescription,
                tint = White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun MusicOptionsBottomSheetContent(
    musicTitle: String,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onChangeMusicClick: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_headphones),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Music options",
                    color = White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (musicTitle.isNotBlank()) {
                    Text(
                        text = musicTitle,
                        color = White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Volume",
            color = White.copy(alpha = 0.55f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = White,
                activeTrackColor = White,
                inactiveTrackColor = White.copy(alpha = 0.28f)
            )
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onChangeMusicClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Change music",
                color = White,
                style = MaterialTheme.typography.titleSmall
            )
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = White
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = White,
                contentColor = Black
            )
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun SoundLayerBottomSheetContent(
    title: String,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = White
                )
            }
        }
        HorizontalDivider(color = White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 12.dp))
        Text(
            text = "Volume",
            color = White.copy(alpha = 0.55f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = White,
                activeTrackColor = White,
                inactiveTrackColor = White.copy(alpha = 0.25f)
            )
        )
    }
}

@Composable
private fun SoundscapeSceneTopBar(
    title: String,
    subtitle: String,
    showMaxBadge: Boolean,
    onClose: () -> Unit,
    onSavePreset: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onClose) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = White
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showMaxBadge) {
                    Spacer(Modifier.size(8.dp))
                    MaxView()
                }
            }
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More",
                    tint = White
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Share") },
                    onClick = {
                        showMenu = false
                        onShare()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Save preset") },
                    onClick = {
                        showMenu = false
                        onSavePreset()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Download") },
                    onClick = {
                        showMenu = false
                        onDownload()
                    }
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun SoundscapeSceneBackground(
    imageUrl: String?,
    videoUrl: String?,
    musicUrl: String?,
    isPlaying: Boolean,
    musicVolume: Float,
    onPlaybackProgress: (positionMs: Long, durationMs: Long) -> Unit,
) {
    val context = LocalContext.current

    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(Modifier.fillMaxSize().background(Black))
    }

    val videoExo = remember(videoUrl) {
        if (videoUrl.isNullOrBlank()) {
            null
        } else {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
            }
        }
    }

    DisposableEffect(videoExo) {
        onDispose { videoExo?.release() }
    }

    LaunchedEffect(videoExo, isPlaying) {
        val v = videoExo ?: return@LaunchedEffect
        v.playWhenReady = isPlaying
        if (isPlaying) v.play() else v.pause()
    }

    if (videoExo != null) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = videoExo
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            update = { it.player = videoExo },
            modifier = Modifier.fillMaxSize()
        )
    }

    val musicPlayer = remember(musicUrl) {
        if (musicUrl.isNullOrBlank()) {
            null
        } else {
            ExoPlayer.Builder(context).build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true
                )
                setMediaItem(MediaItem.fromUri(musicUrl))
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
            }
        }
    }

    DisposableEffect(musicPlayer) {
        onDispose { musicPlayer?.release() }
    }

    LaunchedEffect(musicPlayer, isPlaying) {
        val p = musicPlayer ?: return@LaunchedEffect
        p.playWhenReady = isPlaying
        if (isPlaying) p.play() else p.pause()
    }

    LaunchedEffect(musicPlayer, musicVolume) {
        musicPlayer?.volume = musicVolume.coerceIn(0f, 1f)
    }

    LaunchedEffect(musicPlayer, videoExo) {
        if (musicPlayer == null && videoExo == null) {
            onPlaybackProgress(0L, 0L)
            return@LaunchedEffect
        }
        while (true) {
            val pos = when {
                musicPlayer != null -> musicPlayer.currentPosition
                videoExo != null -> videoExo.currentPosition
                else -> 0L
            }
            val dur = when {
                musicPlayer != null -> musicPlayer.duration
                videoExo != null -> videoExo.duration
                else -> 0L
            }
            onPlaybackProgress(pos.coerceAtLeast(0), dur.coerceAtLeast(0))
            delay(250)
        }
    }
}

@Composable
private fun SoundscapeScenePlayControl(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (durationMs > 0) {
        min(1f, positionMs.toFloat() / durationMs.toFloat())
    } else {
        0f
    }
    val ringColor = White.copy(alpha = 0.9f)
    val trackColor = White.copy(alpha = 0.25f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatDuration(positionMs),
            color = White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val stroke = 3.dp.toPx()
                val pad = stroke / 2f + 2.dp.toPx()
                val size = Size(this.size.width - pad * 2, this.size.height - pad * 2)
                val top = Offset(pad, pad)
                // Top semicircle (clockwise from right through top to left: start 0°, sweep -180°)
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = -180f,
                    useCenter = false,
                    topLeft = top,
                    size = size,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = ringColor,
                    startAngle = 0f,
                    sweepAngle = -180f * progress,
                    useCenter = false,
                    topLeft = top,
                    size = size,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            Icon(
                painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
