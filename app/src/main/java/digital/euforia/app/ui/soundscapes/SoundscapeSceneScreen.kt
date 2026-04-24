/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    // Bind MediaSession while scene is shown (notification / remote); transport is driven by ViewModel.
    BindSoundscapeMediaSession()

    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var selectedSoundLayerKey by remember { mutableStateOf<String?>(null) }
    var showMusicOptions by remember { mutableStateOf(false) }
    var showMusicPicker by remember { mutableStateOf(false) }
    var showSoundsPicker by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var showUnsavedExitDialog by remember { mutableStateOf(false) }
    var interactionNonce by remember { mutableLongStateOf(0L) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val musicSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val soundsPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var soundDragOffsets by remember(state.sceneId) { mutableStateOf(mapOf<Int, Offset>()) }
    val hasModalOpen = showMusicOptions || showMusicPicker || showSoundsPicker || selectedSoundLayerKey != null
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
                musicUrl = state.sceneMusicUrl,
                isPlaying = state.isPlaying,
                isPreparing = state.isPreparing,
                isParallaxEnabled = state.scenePlayerConfig.isParallaxEnabled,
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
                state.soundFloatingButtons
                    .filter { it.instanceKey in visibleIds }
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
                                        markInteraction()
                                        var dragTotal = Offset.Zero
                                        var dragging = false
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Main)
                                            val change = event.changes.firstOrNull { it.id == down.id }
                                                ?: break
                                            if (change.changedToUp()) {
                                                if (!dragging && hypot(dragTotal.x, dragTotal.y) < tapSlopPx) {
                                                    showMusicOptions = false
                                                    selectedSoundLayerKey = btn.instanceKey
                                                    state.layers.firstOrNull { it.instanceKey == btn.instanceKey }?.let { layer ->
                                                        viewModel.onLayerSettingsOpened(
                                                            layerKey = layer.instanceKey,
                                                            soundId = layer.id,
                                                            title = layer.title
                                                        )
                                                    }
                                                } else if (dragging) {
                                                    val savedOffset = soundDragOffsets[btn.id] ?: Offset.Zero
                                                    val finalX = (baseXpx + savedOffset.x)
                                                        .coerceIn(0f, (maxWpx - btnPx).coerceAtLeast(0f))
                                                    val finalY = (baseYpx + savedOffset.y)
                                                        .coerceIn(0f, (maxHpx - btnPx).coerceAtLeast(0f))
                                                    val safeMaxW = maxWpx.coerceAtLeast(1f)
                                                    val safeMaxH = maxHpx.coerceAtLeast(1f)
                                                    val finalPosXFraction = ((finalX + btnPx / 2f) / safeMaxW)
                                                        .coerceIn(0f, 1f)
                                                    val finalPosYFraction = ((finalY + btnPx / 2f) / safeMaxH)
                                                        .coerceIn(0f, 1f)
                                                    viewModel.onSoundButtonPositionChanged(
                                                        instanceKey = btn.instanceKey,
                                                        posXFraction = finalPosXFraction,
                                                        posYFraction = finalPosYFraction
                                                    )
                                                    soundDragOffsets = soundDragOffsets.toMutableMap().apply { remove(btn.id) }
                                                }
                                                break
                                            }
                                            val delta = change.positionChange()
                                            dragTotal += delta
                                            if (!dragging) {
                                                if (hypot(dragTotal.x, dragTotal.y) < tapSlopPx) continue
                                                dragging = true
                                            }
                                            markInteraction()
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
                        title = state.title.ifBlank { "Scene ${state.sceneId}" },
                        subtitle = state.subtitle,
                        showMaxBadge = state.isPro,
                        onClose = {
                            if (state.isDirty) showUnsavedExitDialog = true
                            else navController.popBackStack()
                        },
                        onSavePreset = viewModel::onSavePreset,
                        onRenameScene = viewModel::onRenameScene,
                        onDownload = viewModel::onDownloadScene,
                        onShare = {
                            markInteraction()
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, state.title.ifBlank { context.getString(R.string.app_name) })
                            }
                            context.startActivity(Intent.createChooser(send, context.getString(R.string.share)))
                        }
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
                        positionMs = positionMs,
                        durationMs = durationMs,
                        onToggle = { viewModel.onPlayPause() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                    AnimatedAddSoundsButton(
                        onClick = {
                            markInteraction()
                            showMusicOptions = false
                            showMusicPicker = false
                            selectedSoundLayerKey = null
                            showSoundsPicker = true
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 24.dp)
                            .size(44.dp),
                        enabled = controlsVisible
                    )
                    IconButton(
                        onClick = {
                            markInteraction()
                            selectedSoundLayerKey = null
                            showMusicPicker = false
                            showSoundsPicker = false
                            showMusicOptions = true
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 24.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_headphones),
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            val selectedLayer = selectedSoundLayerKey?.let { key ->
                state.layers.firstOrNull { it.instanceKey == key }
            }
            val selectedButton = selectedLayer?.let { layer ->
                state.soundFloatingButtons.firstOrNull { it.instanceKey == layer.instanceKey }
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

            if (state.isPreparing) {
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
                        val total = state.preparingTotal
                        val done = state.preparingCompleted
                        val progressText = if (total > 0) {
                            stringResource(R.string.scene_preparing_progress_format, done, total)
                        } else {
                            stringResource(R.string.loading)
                        }
                        Text(
                            text = stringResource(R.string.scene_preparing),
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
                        TextButton(onClick = viewModel::onCancelPreparation) {
                            Text(text = stringResource(R.string.cancel), color = White)
                        }
                    }
                }
            }

            if (showUnsavedExitDialog) {
                AlertDialog(
                    onDismissRequest = { showUnsavedExitDialog = false },
                    title = { Text(text = stringResource(R.string.unsaved_changes_title)) },
                    text = { Text(text = stringResource(R.string.unsaved_changes_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.onSavePreset()
                            showUnsavedExitDialog = false
                            navController.popBackStack()
                        }) { Text(text = stringResource(R.string.save)) }
                    },
                    dismissButton = {
                        Row {
                            TextButton(onClick = { showUnsavedExitDialog = false }) {
                                Text(text = stringResource(R.string.cancel))
                            }
                            TextButton(onClick = {
                                viewModel.onDiscardChangesAndExit()
                                showUnsavedExitDialog = false
                                navController.popBackStack()
                            }) {
                                Text(text = stringResource(R.string.discard))
                            }
                        }
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
