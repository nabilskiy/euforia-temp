/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.scene

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.hypot
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.ui.soundscapes.widget.AnimatedAddSoundsButton
import digital.euforia.app.ui.soundscapes.widget.SleepTimerPickerDialog
import digital.euforia.app.ui.soundscapes.widget.MusicOptionsBottomSheetContent
import digital.euforia.app.ui.soundscapes.widget.MusicPickerBottomSheetContent
import digital.euforia.app.ui.soundscapes.widget.SceneSoundFloatingButton
import digital.euforia.app.ui.soundscapes.widget.SoundLayerBottomSheetContent
import digital.euforia.app.ui.soundscapes.widget.SoundsPickerBottomSheetContent
import digital.euforia.app.ui.soundscapes.widget.SoundscapeSceneBackground
import digital.euforia.app.ui.soundscapes.widget.SoundscapeSceneMusicIndicatorButton
import digital.euforia.app.ui.soundscapes.widget.ScenePreferencesBottomSheetContent
import digital.euforia.app.ui.soundscapes.widget.SoundscapeScenePlayControl
import digital.euforia.app.ui.soundscapes.widget.SoundscapeSceneTopBar
import digital.euforia.app.ui.soundscapes.widget.rememberSoundscapeMediaController
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.LinkGenerator
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SoundscapeSceneScreen(
    navController: NavHostController,
    viewModel: SoundscapeSceneViewModel
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val localizedRes = LocalLocalizedRes.current
    // Bind MediaSession while scene is shown (notification / remote); transport is driven by ViewModel.
    BindSoundscapeMediaSession()

    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var selectedSoundLayerKey by remember { mutableStateOf<String?>(null) }
    var showMusicOptions by remember { mutableStateOf(false) }
    var showPreferences by remember { mutableStateOf(false) }
    var showMusicPicker by remember { mutableStateOf(false) }
    var showSoundsPicker by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var showUnsavedExitDialog by remember { mutableStateOf(false) }
    var showTimerPickerDialog by remember { mutableStateOf(false) }
    var interactionNonce by remember { mutableLongStateOf(0L) }
    val inertiaScope = rememberCoroutineScope()
    val inertiaJobs = remember { mutableMapOf<String, Job>() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val musicSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val soundsPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var soundDragOffsets by remember(state.sceneId) { mutableStateOf(mapOf<String, Offset>()) }
    val hasModalOpen = showPreferences || showMusicOptions || showMusicPicker || showSoundsPicker || selectedSoundLayerKey != null
    fun markInteraction() {
        controlsVisible = true
        interactionNonce++
    }

    LaunchedEffect(state.layers, selectedSoundLayerKey) {
        val key = selectedSoundLayerKey ?: return@LaunchedEffect
        if (state.layers.none { it.instanceKey == key }) {
            selectedSoundLayerKey = null
        }
    }
    LaunchedEffect(selectedSoundLayerKey) {
        if (selectedSoundLayerKey != null) {
            showMusicOptions = false
            showPreferences = false
            showMusicPicker = false
            showSoundsPicker = false
            controlsVisible = true
        }
    }
    LaunchedEffect(interactionNonce, hasModalOpen) {
        if (hasModalOpen) return@LaunchedEffect
        delay(5000)
        controlsVisible = false
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
                isPlaying = state.isPlaying,
                isPreparing = state.isPreparing,
                isParallaxEnabled = state.scenePlayerConfig.isParallaxEnabled,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.45f),
                            1f to Color.Transparent
                        )
                    )
                    .zIndex(2f)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.55f)
                        )
                    )
                    .zIndex(2f)
            )

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(260)),
                modifier = Modifier.zIndex(3f)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(top = 72.dp, bottom = 140.dp)
                ) {
                val btnSize = 56.dp
                val density = LocalDensity.current
                val maxWpx = with(density) { maxWidth.toPx() }
                val maxHpx = with(density) { maxHeight.toPx() }
                val btnPx = with(density) { btnSize.toPx() }
                val tapSlopPx = with(density) { 12.dp.toPx() }
                val visibleIds = state.layers.map { it.instanceKey }.toSet()
                val layersByKey = state.layers.associateBy { it.instanceKey }
                state.soundFloatingButtons
                    .filter { it.instanceKey in visibleIds }
                    .forEach { btn ->
                        val extra = soundDragOffsets[btn.instanceKey] ?: Offset.Zero
                        val baseXpx = maxWpx * btn.posXFraction - btnPx / 2f
                        val baseYpx = maxHpx * btn.posYFraction - btnPx / 2f
                        val xPx = (baseXpx + extra.x).coerceIn(0f, (maxWpx - btnPx).coerceAtLeast(0f))
                        val yPx = (baseYpx + extra.y).coerceIn(0f, (maxHpx - btnPx).coerceAtLeast(0f))
                        val xOff = with(density) { xPx.toDp() }
                        val yOff = with(density) { yPx.toDp() }
                        SceneSoundFloatingButton(
                            imageUrl = btn.imageUrl,
                            contentDescription = btn.title,
                            showSoundAnimation = state.soundAnimationsEnabled &&
                                state.isPlaying &&
                                ((layersByKey[btn.instanceKey]?.volume ?: 0f) > 0f) &&
                                (layersByKey[btn.instanceKey]?.muted != true),
                            repeatProgress = run {
                                val layer = layersByKey[btn.instanceKey] ?: return@run null
                                if (layer.isContinuous || layer.repeatIntervalSec <= 0) return@run null
                                val remaining = layer.repeatRemainingMs ?: return@run null
                                (remaining.toFloat() / (layer.repeatIntervalSec * 1000f)).coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .offset(xOff, yOff)
                                .pointerInput(
                                    btn.instanceKey,
                                    maxWpx,
                                    maxHpx,
                                    btn.posXFraction,
                                    btn.posYFraction
                                ) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        markInteraction()
                                        inertiaJobs.remove(btn.instanceKey)?.cancel()
                                        var dragTotal = Offset.Zero
                                        var dragging = false
                                        var velocityX = 0f
                                        var velocityY = 0f
                                        var lastEventTimeMs = down.uptimeMillis
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Main)
                                            val change =
                                                event.changes.firstOrNull { it.id == down.id }
                                                    ?: break
                                            if (change.changedToUp()) {
                                                if (!dragging && hypot(
                                                        dragTotal.x,
                                                        dragTotal.y
                                                    ) < tapSlopPx
                                                ) {
                                                    showMusicOptions = false
                                                    showPreferences = false
                                                    selectedSoundLayerKey = btn.instanceKey
                                                    state.layers.firstOrNull { it.instanceKey == btn.instanceKey }
                                                        ?.let { layer ->
                                                            viewModel.onLayerSettingsOpened(
                                                                layerKey = layer.instanceKey,
                                                                soundId = layer.id,
                                                                title = layer.title
                                                            )
                                                        }
                                                } else if (dragging) {
                                                    val savedOffset =
                                                        soundDragOffsets[btn.instanceKey] ?: Offset.Zero
                                                    val flingVx = velocityX * 0.1f
                                                    val flingVy = velocityY * 0.1f
                                                    val minVelocity = 35f
                                                    if (hypot(flingVx, flingVy) >= minVelocity) {
                                                        inertiaJobs[btn.instanceKey] = inertiaScope.launch {
                                                            var vx = flingVx
                                                            var vy = flingVy
                                                            var currentOffset = savedOffset
                                                            val dt = 1f / 60f
                                                            val friction = 0.92f
                                                            while (isActive && hypot(vx, vy) > minVelocity) {
                                                                val proposedOffset = Offset(
                                                                    x = currentOffset.x + vx * dt,
                                                                    y = currentOffset.y + vy * dt
                                                                )
                                                                val candidateX = baseXpx + proposedOffset.x
                                                                val candidateY = baseYpx + proposedOffset.y
                                                                val clampedX = candidateX.coerceIn(
                                                                    0f,
                                                                    (maxWpx - btnPx).coerceAtLeast(0f)
                                                                )
                                                                val clampedY = candidateY.coerceIn(
                                                                    0f,
                                                                    (maxHpx - btnPx).coerceAtLeast(0f)
                                                                )
                                                                val hitHorizontal = clampedX != candidateX
                                                                val hitVertical = clampedY != candidateY
                                                                currentOffset = Offset(
                                                                    x = clampedX - baseXpx,
                                                                    y = clampedY - baseYpx
                                                                )
                                                                soundDragOffsets = soundDragOffsets
                                                                    .toMutableMap()
                                                                    .apply { this[btn.instanceKey] = currentOffset }
                                                                if (hitHorizontal) vx = -vx * 0.35f
                                                                if (hitVertical) vy = -vy * 0.35f
                                                                vx *= friction
                                                                vy *= friction
                                                                delay(16)
                                                            }
                                                            val finalX = (baseXpx + currentOffset.x)
                                                                .coerceIn(
                                                                    0f,
                                                                    (maxWpx - btnPx).coerceAtLeast(0f)
                                                                )
                                                            val finalY = (baseYpx + currentOffset.y)
                                                                .coerceIn(
                                                                    0f,
                                                                    (maxHpx - btnPx).coerceAtLeast(0f)
                                                                )
                                                            val safeMaxW = maxWpx.coerceAtLeast(1f)
                                                            val safeMaxH = maxHpx.coerceAtLeast(1f)
                                                            val finalPosXFraction =
                                                                ((finalX + btnPx / 2f) / safeMaxW)
                                                                    .coerceIn(0f, 1f)
                                                            val finalPosYFraction =
                                                                ((finalY + btnPx / 2f) / safeMaxH)
                                                                    .coerceIn(0f, 1f)
                                                            viewModel.onSoundButtonPositionChanged(
                                                                instanceKey = btn.instanceKey,
                                                                posXFraction = finalPosXFraction,
                                                                posYFraction = finalPosYFraction
                                                            )
                                                            soundDragOffsets = soundDragOffsets
                                                                .toMutableMap()
                                                                .apply { remove(btn.instanceKey) }
                                                            inertiaJobs.remove(btn.instanceKey)
                                                        }
                                                    } else {
                                                        val finalX = (baseXpx + savedOffset.x)
                                                            .coerceIn(
                                                                0f,
                                                                (maxWpx - btnPx).coerceAtLeast(0f)
                                                            )
                                                        val finalY = (baseYpx + savedOffset.y)
                                                            .coerceIn(
                                                                0f,
                                                                (maxHpx - btnPx).coerceAtLeast(0f)
                                                            )
                                                        val safeMaxW = maxWpx.coerceAtLeast(1f)
                                                        val safeMaxH = maxHpx.coerceAtLeast(1f)
                                                        val finalPosXFraction =
                                                            ((finalX + btnPx / 2f) / safeMaxW)
                                                                .coerceIn(0f, 1f)
                                                        val finalPosYFraction =
                                                            ((finalY + btnPx / 2f) / safeMaxH)
                                                                .coerceIn(0f, 1f)
                                                        viewModel.onSoundButtonPositionChanged(
                                                            instanceKey = btn.instanceKey,
                                                            posXFraction = finalPosXFraction,
                                                            posYFraction = finalPosYFraction
                                                        )
                                                        soundDragOffsets = soundDragOffsets
                                                            .toMutableMap()
                                                            .apply { remove(btn.instanceKey) }
                                                    }
                                                }
                                                break
                                            }
                                            val delta = change.positionChange()
                                            val eventTimeMs = change.uptimeMillis
                                            val dtSec = ((eventTimeMs - lastEventTimeMs).coerceAtLeast(1L)) / 1000f
                                            val instantVx = delta.x / dtSec
                                            val instantVy = delta.y / dtSec
                                            velocityX = velocityX * 0.65f + instantVx * 0.35f
                                            velocityY = velocityY * 0.65f + instantVy * 0.35f
                                            lastEventTimeMs = eventTimeMs
                                            dragTotal += delta
                                            if (!dragging) {
                                                if (hypot(
                                                        dragTotal.x,
                                                        dragTotal.y
                                                    ) < tapSlopPx
                                                ) continue
                                                dragging = true
                                            }
                                            markInteraction()
                                            change.consume()
                                            soundDragOffsets =
                                                soundDragOffsets.toMutableMap().apply {
                                                    val cur = this[btn.instanceKey] ?: Offset.Zero
                                                    val nx = cur + delta
                                                    val candX = baseXpx + nx.x
                                                    val candY = baseYpx + nx.y
                                                    val cx = candX.coerceIn(
                                                        0f,
                                                        (maxWpx - btnPx).coerceAtLeast(0f)
                                                    )
                                                    val cy = candY.coerceIn(
                                                        0f,
                                                        (maxHpx - btnPx).coerceAtLeast(0f)
                                                    )
                                                    this[btn.instanceKey] =
                                                        Offset(cx - baseXpx, cy - baseYpx)
                                                }
                                        }
                                    }
                                }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(260)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .zIndex(4f)
                    .fillMaxWidth()
            ) {
                Column {
                    SoundscapeSceneTopBar(
                        title = state.title.ifBlank {
                            localizedRes.string(
                                R.string.soundscape_scene_title_fallback,
                                state.sceneId
                            )
                        },
                        subtitle = if (state.isDirty) {
                            localizedRes.string(R.string.audio_scene_unsaved_changes)
                        } else if (state.presetId != null) {
                            localizedRes.string(R.string.playlist_type_my_scenes)
                        } else {
                            state.subtitle
                        },
                        showMaxBadge = state.isPro,
                        isDownloaded = state.downloadState == SoundscapeDownloadItem.STATUS_READY,
                        onCollapse = {
                            if (state.isDirty) showUnsavedExitDialog = true
                            else navController.popBackStack()
                        },
                        onSaveChanges = viewModel::onSavePreset,
                        onSaveAndDownload = viewModel::onSaveAndDownload,
                        onRenameScene = viewModel::onRenameScene,
                        onDeleteDownloaded = viewModel::onDeleteDownloadedScene,
                        hasActiveTimer = (state.timerSeconds ?: 0) > 0,
                        onTimer1hClick = { viewModel.onSetTimerSeconds(60 * 60) },
                        onTimer2hClick = { viewModel.onSetTimerSeconds(60 * 60 * 2) },
                        onTimerSetupClick = { showTimerPickerDialog = true },
                        onTimerStopClick = viewModel::onDisableTimer,
                        onPreferencesClick = {
                            markInteraction()
                            selectedSoundLayerKey = null
                            showMusicOptions = false
                            showMusicPicker = false
                            showSoundsPicker = false
                            showPreferences = true
                        },
                        onShare = {
                            markInteraction()
                            val shareUrl = LinkGenerator.buildAudioSceneShareUrl(state.originalSceneId)
                                ?: return@SoundscapeSceneTopBar
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    shareUrl
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    send,
                                    localizedRes.string(R.string.share)
                                )
                            )
                        },
                        canShare = state.presetId == null
                    )
                }
            }

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(260)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(5f)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            ) {
                Box {
                    SoundscapeScenePlayControl(
                        isPlaying = state.isPlaying,
                        timerTotalSeconds = state.timerSeconds,
                        timerRemainingSeconds = state.timerRemainingSeconds,
                        onToggle = { viewModel.onPlayPause() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                    AnimatedAddSoundsButton(
                        onClick = {
                            markInteraction()
                            showPreferences = false
                            showMusicOptions = false
                            showMusicPicker = false
                            selectedSoundLayerKey = null
                            showSoundsPicker = true
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 24.dp)
                            .size(55.dp),
                        enabled = controlsVisible
                    )
                    SoundscapeSceneMusicIndicatorButton(
                        isPlaying = state.isPlaying,
                        sceneMusicUrl = state.sceneMusicUrl,
                        musicVolume = state.musicVolume,
                        onClick = {
                            markInteraction()
                            selectedSoundLayerKey = null
                            showPreferences = false
                            showMusicPicker = false
                            showSoundsPicker = false
                            showMusicOptions = true
                        },
                        contentDescription = localizedRes.string(R.string.audio_scene_background_music_settings),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 24.dp)
                            .size(55.dp),
                        enabled = controlsVisible,
                    )
                }
            }

            val selectedLayer = selectedSoundLayerKey?.let { key ->
                state.layers.firstOrNull { it.instanceKey == key }
            }
            val selectedButton = selectedLayer?.let { layer ->
                state.soundFloatingButtons.firstOrNull { it.instanceKey == layer.instanceKey }
            }

            if (showPreferences) {
                ModalBottomSheet(
                    onDismissRequest = { showPreferences = false },
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
                    ScenePreferencesBottomSheetContent(
                        sceneTitle = state.title,
                        soundAnimationsEnabled = state.soundAnimationsEnabled,
                        onSoundAnimationsToggle = viewModel::onSoundAnimationsEnabledChanged,
                        onChangeBackgroundClick = {
                            // Keep menu parity with iOS; background source picker is wired separately.
                        },
                        onDone = { showPreferences = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            } else if (showMusicOptions) {
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
                        musicCoverUrl = remember(state.selectedMusicId, state.availableMusic, state.sceneMusicUrl) {
                            state.availableMusic.firstOrNull { it.id == state.selectedMusicId }?.imageUrl
                                ?: state.availableMusic.firstOrNull { it.fileUrl == state.sceneMusicUrl }?.imageUrl
                        },
                        isPlaying = state.isPlaying,
                        sceneMusicUrl = state.sceneMusicUrl,
                        volume = state.musicVolume,
                        onVolumeChange = viewModel::onMusicVolume,
                        onChangeMusicClick = {
                            showMusicOptions = false
                            showMusicPicker = true
                        },
                        onDone = { showMusicOptions = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            } else if (showMusicPicker) {
                ModalBottomSheet(
                    onDismissRequest = { showMusicPicker = false },
                    sheetState = soundsPickerSheetState,
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
                    MusicPickerBottomSheetContent(
                        music = state.availableMusic,
                        categories = state.musicCategories,
                        suggestedMusicIds = state.suggestedMusicIds,
                        favoriteMusicIds = state.favoriteMusicIds,
                        initialSelectedId = state.selectedMusicId,
                        isScenePlaying = state.isPlaying,
                        musicVolume = state.musicVolume,
                        onFavoriteClick = viewModel::onToggleMusicFavorite,
                        onMusicClick = viewModel::onPreviewMusicSelection,
                        onDismiss = { showMusicPicker = false },
                        onApply = { selectedId ->
                            viewModel.onApplyMusicSelection(selectedId)
                            showMusicPicker = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            } else if (selectedSoundLayerKey != null && selectedLayer != null && selectedButton != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedSoundLayerKey = null },
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
                        onVolumeChange = { viewModel.onLayerVolume(selectedLayer.instanceKey, it) },
                        showRepeatInterval = !selectedLayer.isContinuous,
                        repeatIntervalSec = selectedLayer.repeatIntervalSec,
                        minRepeatDelaySec = selectedLayer.minRepeatDelaySec,
                        maxRepeatDelaySec = selectedLayer.maxRepeatDelaySec,
                        onRepeatIntervalChange = { viewModel.onLayerRepeatInterval(selectedLayer.instanceKey, it) },
                        onRepeatIntervalChangeFinished = viewModel::onLayerRepeatIntervalChangeFinished,
                        onDelete = {
                            viewModel.onLayerRemove(selectedLayer.instanceKey)
                            selectedSoundLayerKey = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            } else if (showSoundsPicker) {
                ModalBottomSheet(
                    onDismissRequest = { showSoundsPicker = false },
                    sheetState = soundsPickerSheetState,
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
                    SoundsPickerBottomSheetContent(
                        sounds = state.availableSounds,
                        categories = state.soundCategories,
                        defaultSceneSoundIds = state.defaultSceneSoundIds,
                        suggestedSoundIds = state.suggestedSoundIds,
                        sceneSoundButtons = state.soundFloatingButtons,
                        initialSelectedIds = state.layers.map { it.id }.toSet(),
                        onSelectionChanged = viewModel::onApplySoundsSelection,
                        onDismiss = { showSoundsPicker = false },
                        onApply = { selected ->
                            viewModel.onApplySoundsSelection(selected)
                            showSoundsPicker = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
            if (!controlsVisible && !hasModalOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(20f)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { markInteraction() })
                        }
                )
            }

            if (state.isPreparing || state.isSceneDownloadInProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.62f))
                        .zIndex(30f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        CircularProgressIndicator(color = White)
                        val progressText = if (state.isSceneDownloadInProgress) {
                            localizedRes.string(R.string.downloads_progress_format, state.downloadProgress)
                        } else {
                            val total = state.preparingTotal
                            val done = state.preparingCompleted
                            if (total > 0) {
                                localizedRes.string(R.string.scene_preparing_progress_format, done, total)
                            } else {
                                localizedRes.string(R.string.loading)
                            }
                        }
                        Text(
                            text = if (state.isSceneDownloadInProgress) {
                                localizedRes.string(R.string.audio_scene_saving)
                            } else {
                                localizedRes.string(R.string.scene_preparing)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center
                        )
                        if (!state.isSceneDownloadInProgress) {
                            TextButton(onClick = viewModel::onCancelPreparation) {
                                Text(text = localizedRes.string(R.string.cancel), color = White)
                            }
                        }
                    }
                }
            }

            if (showUnsavedExitDialog) {
                AlertDialog(
                    onDismissRequest = { showUnsavedExitDialog = false },
                    title = { Text(text = localizedRes.string(R.string.audio_scene_unsaved_changes_alert_title)) },
                    text = { Text(text = localizedRes.string(R.string.audio_scene_unsaved_changes_alert_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.onSavePreset()
                            showUnsavedExitDialog = false
                            navController.popBackStack()
                        }) { Text(text = localizedRes.string(R.string.audio_scene_unsaved_changes_alert_save)) }
                    },
                    dismissButton = {
                        Row {
                            TextButton(onClick = { showUnsavedExitDialog = false }) {
                                Text(text = localizedRes.string(R.string.cancel))
                            }
                            TextButton(onClick = {
                                viewModel.onDiscardChangesAndExit()
                                showUnsavedExitDialog = false
                                navController.popBackStack()
                            }) {
                                Text(text = localizedRes.string(R.string.audio_scene_unsaved_changes_alert_discard))
                            }
                        }
                    }
                )
            }

            if (showTimerPickerDialog) {
                SleepTimerPickerDialog(
                    initialSeconds = state.timerSeconds,
                    onDismiss = { showTimerPickerDialog = false },
                    onSet = { seconds ->
                        viewModel.onSetTimerSeconds(seconds)
                        showTimerPickerDialog = false
                    },
                    onDisable = {
                        viewModel.onDisableTimer()
                        showTimerPickerDialog = false
                    }
                )
            }
        }
    }


}

@OptIn(UnstableApi::class)
@Composable
private fun BindSoundscapeMediaSession() {
    rememberSoundscapeMediaController()
}
