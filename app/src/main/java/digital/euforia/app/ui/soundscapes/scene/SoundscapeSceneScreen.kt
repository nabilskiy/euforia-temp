/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.scene

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.hypot
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.soundscapes.widget.AnimatedAddSoundsButton
import digital.euforia.app.ui.soundscapes.widget.SleepTimerPickerDialog
import digital.euforia.app.ui.soundscapes.widget.MusicOptionsBottomSheetContent
import digital.euforia.app.ui.soundscapes.widget.MusicPickerBottomSheetContent
import digital.euforia.app.ui.soundscapes.widget.SceneSoundFloatingButton
import digital.euforia.app.ui.soundscapes.widget.SoundLayerBottomSheetContent
import digital.euforia.app.ui.soundscapes.widget.SoundsPickerBottomSheetContent
import digital.euforia.app.data.model.BackgroundMediaItem
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
import digital.euforia.app.ui.util.widget.ProgressIndicator
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private enum class BackgroundImportSource { PEXELS, UNSPLASH, PHOTOS }

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
    var topBarModalVisible by remember { mutableStateOf(false) }
    var showImportSourceDialog by remember { mutableStateOf(false) }
    var showUnsplashPicker by remember { mutableStateOf(false) }
    var showPexelsPicker by remember { mutableStateOf(false) }
    var pendingCropImageUri by remember { mutableStateOf<Uri?>(null) }
    var draggingSoundKey by remember { mutableStateOf<String?>(null) }
    var pressedSoundKey by remember { mutableStateOf<String?>(null) }
    var tapPulseSoundKey by remember { mutableStateOf<String?>(null) }
    var trashHovered by remember { mutableStateOf(false) }
    var interactionNonce by remember { mutableLongStateOf(0L) }
    val inertiaScope = rememberCoroutineScope()
    val inertiaJobs = remember { mutableMapOf<String, Job>() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val musicSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val soundsPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var soundDragOffsets by remember(state.sceneId) { mutableStateOf(mapOf<String, Offset>()) }
    val hasModalOpen = showPreferences ||
        showMusicOptions ||
        showMusicPicker ||
        showSoundsPicker ||
        selectedSoundLayerKey != null ||
        showUnsavedExitDialog ||
        showTimerPickerDialog ||
        topBarModalVisible
    fun markInteraction() {
        controlsVisible = true
        interactionNonce++
    }
    fun requestCloseScene() {
        if (state.isDirty) showUnsavedExitDialog = true
        else navController.popBackStack()
    }
    val pickBackgroundMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val picked = uri ?: return@rememberLauncherForActivityResult
        val mimeType = context.contentResolver.getType(picked)
        if (mimeType?.lowercase()?.startsWith("video/") == true) {
            viewModel.onBackgroundLocalFileSelected(picked, mimeType)
            showPreferences = false
        } else {
            pendingCropImageUri = picked
        }
    }

    fun openSource(source: BackgroundImportSource) {
        showImportSourceDialog = false
        when (source) {
            BackgroundImportSource.PEXELS -> {
                viewModel.onPexelsBackgroundPickerOpened(initialVideoTab = true)
                showPexelsPicker = true
            }
            BackgroundImportSource.UNSPLASH -> {
                viewModel.onUnsplashBackgroundPickerOpened()
                showUnsplashPicker = true
            }
            BackgroundImportSource.PHOTOS -> {
                pickBackgroundMediaLauncher.launch(arrayOf("image/*", "video/*"))
            }
        }
    }

    LaunchedEffect(state.layers, selectedSoundLayerKey) {
        val key = selectedSoundLayerKey ?: return@LaunchedEffect
        if (state.layers.none { it.instanceKey == key }) {
            selectedSoundLayerKey = null
        }
    }

    BackHandler(enabled = true) {
        requestCloseScene()
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
    LaunchedEffect(showUnsplashPicker, state.backgroundSearchQuery, state.backgroundSearchRequestNonce) {
        if (!showUnsplashPicker) return@LaunchedEffect
        viewModel.refreshUnsplashBackgroundSearch()
    }
    LaunchedEffect(
        showPexelsPicker,
        state.backgroundSearchQuery,
        state.pexelsVideoTabSelected,
        state.backgroundSearchRequestNonce
    ) {
        if (!showPexelsPicker) return@LaunchedEffect
        viewModel.refreshPexelsBackgroundSearch()
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

            SceneGradientOverlays()

            SceneFloatingSoundButtons(
                visible = controlsVisible,
                state = state,
                soundDragOffsets = soundDragOffsets,
                onSoundDragOffsetsChange = { soundDragOffsets = it },
                draggingSoundKey = draggingSoundKey,
                onDraggingSoundKeyChange = { draggingSoundKey = it },
                pressedSoundKey = pressedSoundKey,
                onPressedSoundKeyChange = { pressedSoundKey = it },
                tapPulseSoundKey = tapPulseSoundKey,
                onTapPulseSoundKeyChange = { tapPulseSoundKey = it },
                trashHovered = trashHovered,
                onTrashHoveredChange = { trashHovered = it },
                selectedSoundLayerKey = selectedSoundLayerKey,
                onSelectedSoundLayerKeyChange = { selectedSoundLayerKey = it },
                markInteraction = ::markInteraction,
                onOpenLayerSettings = { layer ->
                    showMusicOptions = false
                    showPreferences = false
                    selectedSoundLayerKey = layer.instanceKey
                    viewModel.onLayerSettingsOpened(
                        layerKey = layer.instanceKey,
                        soundId = layer.id,
                        title = layer.title
                    )
                },
                onRemoveLayer = viewModel::onLayerRemove,
                onSoundButtonPositionChanged = viewModel::onSoundButtonPositionChanged,
                inertiaScope = inertiaScope,
                inertiaJobs = inertiaJobs,
                deleteContentDescription = localizedRes.string(R.string.delete)
            )

            SceneTopControls(
                visible = controlsVisible,
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
                hasActiveTimer = (state.timerSeconds ?: 0) > 0,
                canShare = state.presetId == null,
                onCollapse = ::requestCloseScene,
                onSaveChanges = viewModel::onSavePreset,
                onSaveAndDownload = viewModel::onSaveAndDownload,
                onRenameScene = viewModel::onRenameScene,
                onDeleteDownloaded = {
                    viewModel.onDeleteDownloadedScene()
                    navController.popBackStack()
                },
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
                        ?: return@SceneTopControls
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
                onModalVisibilityChanged = { isVisible ->
                    topBarModalVisible = isVisible
                    if (isVisible) controlsVisible = true
                }
            )

            SceneBottomControls(
                visible = controlsVisible,
                isPlaying = state.isPlaying,
                timerTotalSeconds = state.timerSeconds,
                timerRemainingSeconds = state.timerRemainingSeconds,
                sceneMusicUrl = state.sceneMusicUrl,
                musicVolume = state.musicVolume,
                musicContentDescription = localizedRes.string(R.string.audio_scene_background_music_settings),
                onTogglePlay = { viewModel.onPlayPause() },
                onAddSoundClick = {
                    markInteraction()
                    showPreferences = false
                    showMusicOptions = false
                    showMusicPicker = false
                    selectedSoundLayerKey = null
                    showSoundsPicker = true
                },
                onMusicClick = {
                    markInteraction()
                    selectedSoundLayerKey = null
                    showPreferences = false
                    showMusicPicker = false
                    showSoundsPicker = false
                    showMusicOptions = true
                }
            )

            SceneModalHost(
                state = state,
                showPreferences = showPreferences,
                onShowPreferencesChange = { showPreferences = it },
                showMusicOptions = showMusicOptions,
                onShowMusicOptionsChange = { showMusicOptions = it },
                showMusicPicker = showMusicPicker,
                onShowMusicPickerChange = { showMusicPicker = it },
                showSoundsPicker = showSoundsPicker,
                onShowSoundsPickerChange = { showSoundsPicker = it },
                selectedSoundLayerKey = selectedSoundLayerKey,
                onSelectedSoundLayerKeyChange = { selectedSoundLayerKey = it },
                onShowImportSourceDialog = { showImportSourceDialog = true },
                viewModel = viewModel,
                layerSheetState = sheetState,
                musicSheetState = musicSheetState,
                soundsPickerSheetState = soundsPickerSheetState
            )
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

            ScenePreparingOverlay(
                visible = state.isPreparing || state.isSceneDownloadInProgress,
                isSceneDownloadInProgress = state.isSceneDownloadInProgress,
                downloadProgress = state.downloadProgress,
                preparingTotal = state.preparingTotal,
                preparingCompleted = state.preparingCompleted,
                onCancel = viewModel::onCancelPreparation
            )

            SceneDialogsHost(
                state = state,
                showUnsavedExitDialog = showUnsavedExitDialog,
                onShowUnsavedExitDialogChange = { showUnsavedExitDialog = it },
                showTimerPickerDialog = showTimerPickerDialog,
                onShowTimerPickerDialogChange = { showTimerPickerDialog = it },
                showImportSourceDialog = showImportSourceDialog,
                onShowImportSourceDialogChange = { showImportSourceDialog = it },
                showUnsplashPicker = showUnsplashPicker,
                onShowUnsplashPickerChange = { showUnsplashPicker = it },
                showPexelsPicker = showPexelsPicker,
                onShowPexelsPickerChange = { showPexelsPicker = it },
                pendingCropImageUri = pendingCropImageUri,
                onPendingCropImageUriChange = { pendingCropImageUri = it },
                onSavePreset = viewModel::onSavePreset,
                onDiscardChangesAndExit = viewModel::onDiscardChangesAndExit,
                onSetTimerSeconds = viewModel::onSetTimerSeconds,
                onDisableTimer = viewModel::onDisableTimer,
                onOpenSource = ::openSource,
                onPexelsTabChange = viewModel::onPexelsImportVideoTabChange,
                onBackgroundImportQueryChange = viewModel::onBackgroundImportQueryChange,
                onBackgroundVideoSelected = viewModel::onBackgroundVideoSelected,
                onBackgroundImageSelected = viewModel::onBackgroundImageSelected,
                onBackgroundCroppedLocalImageSelected = viewModel::onBackgroundCroppedLocalImageSelected,
                onCloseAfterExitAction = { navController.popBackStack() },
                onHidePreferences = { showPreferences = false },
                soundsPickerSheetState = soundsPickerSheetState
            )
        }
    }
}

@Composable
private fun SceneSheetDragHandle() {
    Box(
        Modifier
            .padding(vertical = 10.dp)
            .width(36.dp)
            .height(4.dp)
            .background(White.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
    )
}

@Composable
private fun BoxScope.SceneGradientOverlays() {
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
}

@Composable
private fun BoxScope.SceneTopControls(
    visible: Boolean,
    title: String,
    subtitle: String,
    showMaxBadge: Boolean,
    isDownloaded: Boolean,
    hasActiveTimer: Boolean,
    canShare: Boolean,
    onCollapse: () -> Unit,
    onSaveChanges: () -> Unit,
    onSaveAndDownload: (String) -> Unit,
    onRenameScene: (String) -> Unit,
    onDeleteDownloaded: () -> Unit,
    onTimer1hClick: () -> Unit,
    onTimer2hClick: () -> Unit,
    onTimerSetupClick: () -> Unit,
    onTimerStopClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onShare: () -> Unit,
    onModalVisibilityChanged: (Boolean) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
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
                title = title,
                subtitle = subtitle,
                showMaxBadge = showMaxBadge,
                isDownloaded = isDownloaded,
                onCollapse = onCollapse,
                onSaveChanges = onSaveChanges,
                onSaveAndDownload = onSaveAndDownload,
                onRenameScene = onRenameScene,
                onDeleteDownloaded = onDeleteDownloaded,
                hasActiveTimer = hasActiveTimer,
                onTimer1hClick = onTimer1hClick,
                onTimer2hClick = onTimer2hClick,
                onTimerSetupClick = onTimerSetupClick,
                onTimerStopClick = onTimerStopClick,
                onPreferencesClick = onPreferencesClick,
                onShare = onShare,
                canShare = canShare,
                onModalVisibilityChanged = onModalVisibilityChanged
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SceneDialogsHost(
    state: SoundscapeSceneState,
    showUnsavedExitDialog: Boolean,
    onShowUnsavedExitDialogChange: (Boolean) -> Unit,
    showTimerPickerDialog: Boolean,
    onShowTimerPickerDialogChange: (Boolean) -> Unit,
    showImportSourceDialog: Boolean,
    onShowImportSourceDialogChange: (Boolean) -> Unit,
    showUnsplashPicker: Boolean,
    onShowUnsplashPickerChange: (Boolean) -> Unit,
    showPexelsPicker: Boolean,
    onShowPexelsPickerChange: (Boolean) -> Unit,
    pendingCropImageUri: Uri?,
    onPendingCropImageUriChange: (Uri?) -> Unit,
    onSavePreset: () -> Unit,
    onDiscardChangesAndExit: () -> Unit,
    onSetTimerSeconds: (Int) -> Unit,
    onDisableTimer: () -> Unit,
    onOpenSource: (BackgroundImportSource) -> Unit,
    onPexelsTabChange: (Boolean) -> Unit,
    onBackgroundImportQueryChange: (String) -> Unit,
    onBackgroundVideoSelected: (String, String?) -> Unit,
    onBackgroundImageSelected: (String) -> Unit,
    onBackgroundCroppedLocalImageSelected: (String) -> Unit,
    onCloseAfterExitAction: () -> Unit,
    onHidePreferences: () -> Unit,
    soundsPickerSheetState: androidx.compose.material3.SheetState
) {
    val localizedRes = LocalLocalizedRes.current
    if (showUnsavedExitDialog) {
        AlertDialog(
            onDismissRequest = { onShowUnsavedExitDialogChange(false) },
            title = { Text(text = localizedRes.string(R.string.audio_scene_unsaved_changes_alert_title)) },
            text = { Text(text = localizedRes.string(R.string.audio_scene_unsaved_changes_alert_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onSavePreset()
                    onShowUnsavedExitDialogChange(false)
                    onCloseAfterExitAction()
                }) { Text(text = localizedRes.string(R.string.audio_scene_unsaved_changes_alert_save)) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { onShowUnsavedExitDialogChange(false) }) {
                        Text(text = localizedRes.string(R.string.cancel))
                    }
                    TextButton(onClick = {
                        onDiscardChangesAndExit()
                        onShowUnsavedExitDialogChange(false)
                        onCloseAfterExitAction()
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
            onDismiss = { onShowTimerPickerDialogChange(false) },
            onSet = { seconds ->
                onSetTimerSeconds(seconds)
                onShowTimerPickerDialogChange(false)
            },
            onDisable = {
                onDisableTimer()
                onShowTimerPickerDialogChange(false)
            }
        )
    }

    if (showImportSourceDialog) {
        AlertDialog(
            onDismissRequest = { onShowImportSourceDialogChange(false) },
            title = { Text(text = localizedRes.string(R.string.import_from)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(White.copy(alpha = 0.06f))
                            .clickable { onOpenSource(BackgroundImportSource.PEXELS) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Collections,
                            contentDescription = null,
                            tint = White.copy(alpha = 0.9f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Pexels",
                            style = MaterialTheme.typography.bodyMedium,
                            color = White
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(White.copy(alpha = 0.06f))
                            .clickable { onOpenSource(BackgroundImportSource.UNSPLASH) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = White.copy(alpha = 0.9f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Unsplash",
                            style = MaterialTheme.typography.bodyMedium,
                            color = White
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(White.copy(alpha = 0.06f))
                            .clickable { onOpenSource(BackgroundImportSource.PHOTOS) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = White.copy(alpha = 0.9f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Photos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = White
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onShowImportSourceDialogChange(false) }) {
                    Text(text = localizedRes.string(R.string.cancel))
                }
            }
        )
    }

    if (showUnsplashPicker || showPexelsPicker) {
        ModalBottomSheet(
            onDismissRequest = {
                onShowUnsplashPickerChange(false)
                onShowPexelsPickerChange(false)
            },
            sheetState = soundsPickerSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = BottomSheetBackground
        ) {
            BackgroundSearchSheet(
                title = if (showUnsplashPicker) "Unsplash" else "Pexels",
                query = state.backgroundSearchQuery,
                isPexels = showPexelsPicker,
                pexelsVideoTabSelected = state.pexelsVideoTabSelected,
                onPexelsTabChange = onPexelsTabChange,
                onQueryChange = onBackgroundImportQueryChange,
                items = state.backgroundSearchItems,
                loading = state.backgroundSearchLoading,
                error = state.backgroundSearchError,
                onItemClick = { item ->
                    if (item.isVideo) {
                        onBackgroundVideoSelected(item.mediaUrl, item.previewUrl)
                    } else {
                        onBackgroundImageSelected(item.mediaUrl)
                    }
                    onShowUnsplashPickerChange(false)
                    onShowPexelsPickerChange(false)
                    onHidePreferences()
                },
                onClose = {
                    onShowUnsplashPickerChange(false)
                    onShowPexelsPickerChange(false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.98f)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }

    val cropUri = pendingCropImageUri
    if (cropUri != null) {
        LocalImageCropDialog(
            sourceUri = cropUri,
            onDismiss = { onPendingCropImageUriChange(null) },
            onApply = { croppedImageUrl ->
                onPendingCropImageUriChange(null)
                onBackgroundCroppedLocalImageSelected(croppedImageUrl)
                onHidePreferences()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SceneModalHost(
    state: SoundscapeSceneState,
    showPreferences: Boolean,
    onShowPreferencesChange: (Boolean) -> Unit,
    showMusicOptions: Boolean,
    onShowMusicOptionsChange: (Boolean) -> Unit,
    showMusicPicker: Boolean,
    onShowMusicPickerChange: (Boolean) -> Unit,
    showSoundsPicker: Boolean,
    onShowSoundsPickerChange: (Boolean) -> Unit,
    selectedSoundLayerKey: String?,
    onSelectedSoundLayerKeyChange: (String?) -> Unit,
    onShowImportSourceDialog: () -> Unit,
    viewModel: SoundscapeSceneViewModel,
    layerSheetState: androidx.compose.material3.SheetState,
    musicSheetState: androidx.compose.material3.SheetState,
    soundsPickerSheetState: androidx.compose.material3.SheetState
) {
    val selectedLayer = selectedSoundLayerKey?.let { key ->
        state.layers.firstOrNull { it.instanceKey == key }
    }
    val selectedButton = selectedLayer?.let { layer ->
        state.soundFloatingButtons.firstOrNull { it.instanceKey == layer.instanceKey }
    }

    if (showPreferences) {
        ModalBottomSheet(
            onDismissRequest = { onShowPreferencesChange(false) },
            sheetState = musicSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = BottomSheetBackground,
            dragHandle = { SceneSheetDragHandle() }
        ) {
            ScenePreferencesBottomSheetContent(
                sceneTitle = state.title,
                soundAnimationsEnabled = state.soundAnimationsEnabled,
                onSoundAnimationsToggle = viewModel::onSoundAnimationsEnabledChanged,
                onChangeBackgroundClick = onShowImportSourceDialog,
                onDone = { onShowPreferencesChange(false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    } else if (showMusicOptions) {
        ModalBottomSheet(
            onDismissRequest = { onShowMusicOptionsChange(false) },
            sheetState = musicSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = BottomSheetBackground,
            dragHandle = { SceneSheetDragHandle() }
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
                    onShowMusicOptionsChange(false)
                    onShowMusicPickerChange(true)
                },
                onDone = { onShowMusicOptionsChange(false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    } else if (showMusicPicker) {
        ModalBottomSheet(
            onDismissRequest = { onShowMusicPickerChange(false) },
            sheetState = soundsPickerSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = BottomSheetBackground,
            dragHandle = { SceneSheetDragHandle() }
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
                onDismiss = { onShowMusicPickerChange(false) },
                onApply = { selectedId ->
                    viewModel.onApplyMusicSelection(selectedId)
                    onShowMusicPickerChange(false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    } else if (selectedSoundLayerKey != null && selectedLayer != null && selectedButton != null) {
        ModalBottomSheet(
            onDismissRequest = { onSelectedSoundLayerKeyChange(null) },
            sheetState = layerSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = BottomSheetBackground,
            dragHandle = { SceneSheetDragHandle() }
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
                    onSelectedSoundLayerKeyChange(null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    } else if (showSoundsPicker) {
        ModalBottomSheet(
            onDismissRequest = { onShowSoundsPickerChange(false) },
            sheetState = soundsPickerSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = BottomSheetBackground,
            dragHandle = { SceneSheetDragHandle() }
        ) {
            SoundsPickerBottomSheetContent(
                sounds = state.availableSounds,
                categories = state.soundCategories,
                defaultSceneSoundIds = state.defaultSceneSoundIds,
                suggestedSoundIds = state.suggestedSoundIds,
                sceneSoundButtons = state.soundFloatingButtons,
                initialSelectedIds = state.layers.map { it.id }.toSet(),
                onSelectionChanged = viewModel::onApplySoundsSelection,
                onDismiss = { onShowSoundsPickerChange(false) },
                onApply = { selected ->
                    viewModel.onApplySoundsSelection(selected)
                    onShowSoundsPickerChange(false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SceneFloatingSoundButtons(
    visible: Boolean,
    state: SoundscapeSceneState,
    soundDragOffsets: Map<String, Offset>,
    onSoundDragOffsetsChange: (Map<String, Offset>) -> Unit,
    draggingSoundKey: String?,
    onDraggingSoundKeyChange: (String?) -> Unit,
    pressedSoundKey: String?,
    onPressedSoundKeyChange: (String?) -> Unit,
    tapPulseSoundKey: String?,
    onTapPulseSoundKeyChange: (String?) -> Unit,
    trashHovered: Boolean,
    onTrashHoveredChange: (Boolean) -> Unit,
    selectedSoundLayerKey: String?,
    onSelectedSoundLayerKeyChange: (String?) -> Unit,
    markInteraction: () -> Unit,
    onOpenLayerSettings: (SoundLayerUi) -> Unit,
    onRemoveLayer: (String) -> Unit,
    onSoundButtonPositionChanged: (String, Float, Float) -> Unit,
    inertiaScope: kotlinx.coroutines.CoroutineScope,
    inertiaJobs: MutableMap<String, Job>,
    deleteContentDescription: String
) {
    val latestSoundDragOffsets by rememberUpdatedState(newValue = soundDragOffsets)
    val latestDraggingSoundKey by rememberUpdatedState(newValue = draggingSoundKey)
    val latestPressedSoundKey by rememberUpdatedState(newValue = pressedSoundKey)
    val latestTapPulseSoundKey by rememberUpdatedState(newValue = tapPulseSoundKey)
    AnimatedVisibility(
        visible = visible,
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
            val trashSizePx = with(density) { 50.dp.toPx() }
            val trashTopPx = 0f
            val trashLeftPx = (maxWpx - trashSizePx) / 2f
            val trashRightPx = trashLeftPx + trashSizePx
            val trashBottomPx = trashTopPx + trashSizePx
            val trashCenterX = (trashLeftPx + trashRightPx) / 2f
            val trashCenterY = (trashTopPx + trashBottomPx) / 2f
            val removeDistancePx = with(density) { 80.dp.toPx() }
            fun isOverTrash(centerX: Float, centerY: Float): Boolean {
                val dx = centerX - trashCenterX
                val dy = centerY - trashCenterY
                return hypot(dx, dy) < removeDistancePx
            }
            val visibleIds = state.layers.map { it.instanceKey }.toSet()
            val layersByKey = state.layers.associateBy { it.instanceKey }
            AnimatedVisibility(
                visible = visible && draggingSoundKey != null,
                enter = fadeIn(animationSpec = tween(120)),
                exit = fadeOut(animationSpec = tween(120)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 0.dp)
                    .zIndex(8f)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = if (trashHovered) {
                                Color.Red.copy(alpha = 0.3f)
                            } else {
                                Color.Black.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(25.dp)
                        )
                        .border(
                            width = 1.5.dp,
                            color = White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(25.dp)
                        )
                        .graphicsLayer {
                            val scale = if (trashHovered) 1.1f else 1f
                            scaleX = scale
                            scaleY = scale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = deleteContentDescription,
                        tint = White.copy(alpha = if (trashHovered) 1f else 0.86f)
                    )
                }
            }
            state.soundFloatingButtons
                .filter { it.instanceKey in visibleIds }
                .forEach { btn ->
                    val soundScale by animateFloatAsState(
                        targetValue = when {
                            draggingSoundKey == btn.instanceKey -> 1.1f
                            pressedSoundKey == btn.instanceKey -> 0.94f
                            tapPulseSoundKey == btn.instanceKey -> 1.05f
                            else -> 1f
                        },
                        animationSpec = spring(
                            dampingRatio = 0.58f,
                            stiffness = 430f
                        ),
                        label = "sound_button_scale"
                    )
                    val extra = latestSoundDragOffsets[btn.instanceKey] ?: Offset.Zero
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
                            .graphicsLayer {
                                scaleX = soundScale
                                scaleY = soundScale
                            }
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
                                    onPressedSoundKeyChange(btn.instanceKey)
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
                                                onTapPulseSoundKeyChange(btn.instanceKey)
                                                inertiaScope.launch {
                                                    delay(110)
                                                    if (latestTapPulseSoundKey == btn.instanceKey) {
                                                        onTapPulseSoundKeyChange(null)
                                                    }
                                                }
                                                state.layers.firstOrNull { it.instanceKey == btn.instanceKey }
                                                    ?.let(onOpenLayerSettings)
                                            } else if (dragging) {
                                                val savedOffset =
                                                    latestSoundDragOffsets[btn.instanceKey] ?: Offset.Zero
                                                val dropX = (baseXpx + savedOffset.x)
                                                    .coerceIn(0f, (maxWpx - btnPx).coerceAtLeast(0f))
                                                val dropY = (baseYpx + savedOffset.y)
                                                    .coerceIn(0f, (maxHpx - btnPx).coerceAtLeast(0f))
                                                val dropCenterX = dropX + btnPx / 2f
                                                val dropCenterY = dropY + btnPx / 2f
                                                if (isOverTrash(dropCenterX, dropCenterY)) {
                                                    onRemoveLayer(btn.instanceKey)
                                                    if (selectedSoundLayerKey == btn.instanceKey) {
                                                        onSelectedSoundLayerKeyChange(null)
                                                    }
                                                    onSoundDragOffsetsChange(
                                                        latestSoundDragOffsets
                                                            .toMutableMap()
                                                            .apply { remove(btn.instanceKey) }
                                                    )
                                                    break
                                                }
                                                val flingVx = velocityX * 0.1f
                                                val flingVy = velocityY * 0.1f
                                                val minVelocity = 35f
                                                val boostedFlingVx = flingVx * 1.8f
                                                val boostedFlingVy = flingVy * 1.8f
                                                if (hypot(boostedFlingVx, boostedFlingVy) >= minVelocity) {
                                                    inertiaJobs[btn.instanceKey] = inertiaScope.launch {
                                                        var vx = boostedFlingVx
                                                        var vy = boostedFlingVy
                                                        var currentOffset = savedOffset
                                                        val dt = 1f / 60f
                                                        val friction = 0.95f
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
                                                            onSoundDragOffsetsChange(
                                                                latestSoundDragOffsets
                                                                    .toMutableMap()
                                                                    .apply { this[btn.instanceKey] = currentOffset }
                                                            )
                                                            if (hitHorizontal) vx = -vx * 0.45f
                                                            if (hitVertical) vy = -vy * 0.45f
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
                                                        onSoundButtonPositionChanged(
                                                            btn.instanceKey,
                                                            finalPosXFraction,
                                                            finalPosYFraction
                                                        )
                                                        onSoundDragOffsetsChange(
                                                            latestSoundDragOffsets
                                                                .toMutableMap()
                                                                .apply { remove(btn.instanceKey) }
                                                        )
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
                                                    onSoundButtonPositionChanged(
                                                        btn.instanceKey,
                                                        finalPosXFraction,
                                                        finalPosYFraction
                                                    )
                                                    onSoundDragOffsetsChange(
                                                        latestSoundDragOffsets
                                                            .toMutableMap()
                                                            .apply { remove(btn.instanceKey) }
                                                    )
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
                                            onPressedSoundKeyChange(null)
                                            onDraggingSoundKeyChange(btn.instanceKey)
                                        }
                                        markInteraction()
                                        change.consume()
                                        onSoundDragOffsetsChange(
                                            latestSoundDragOffsets.toMutableMap().apply {
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
                                                if (latestDraggingSoundKey == btn.instanceKey) {
                                                    val centerX = cx + btnPx / 2f
                                                    val centerY = cy + btnPx / 2f
                                                    onTrashHoveredChange(isOverTrash(centerX, centerY))
                                                }
                                            }
                                        )
                                    }
                                    if (latestDraggingSoundKey == btn.instanceKey) {
                                        onDraggingSoundKeyChange(null)
                                        onTrashHoveredChange(false)
                                    }
                                    if (latestPressedSoundKey == btn.instanceKey) {
                                        onPressedSoundKeyChange(null)
                                    }
                                }
                            }
                    )
                }
        }
    }
}

@Composable
private fun BoxScope.SceneBottomControls(
    visible: Boolean,
    isPlaying: Boolean,
    timerTotalSeconds: Int?,
    timerRemainingSeconds: Int?,
    sceneMusicUrl: String?,
    musicVolume: Float,
    musicContentDescription: String,
    onTogglePlay: () -> Unit,
    onAddSoundClick: () -> Unit,
    onMusicClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
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
                isPlaying = isPlaying,
                timerTotalSeconds = timerTotalSeconds,
                timerRemainingSeconds = timerRemainingSeconds,
                onToggle = onTogglePlay,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            AnimatedAddSoundsButton(
                onClick = onAddSoundClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .size(55.dp),
                enabled = visible
            )
            SoundscapeSceneMusicIndicatorButton(
                isPlaying = isPlaying,
                sceneMusicUrl = sceneMusicUrl,
                musicVolume = musicVolume,
                onClick = onMusicClick,
                contentDescription = musicContentDescription,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .size(55.dp),
                enabled = visible,
            )
        }
    }
}

@Composable
private fun ScenePreparingOverlay(
    visible: Boolean,
    isSceneDownloadInProgress: Boolean,
    downloadProgress: Int,
    preparingTotal: Int,
    preparingCompleted: Int,
    onCancel: () -> Unit
) {
    if (!visible) return
    val localizedRes = LocalLocalizedRes.current
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
            ProgressIndicator()
            val progressText = if (isSceneDownloadInProgress) {
                localizedRes.string(R.string.downloads_progress_format, downloadProgress)
            } else {
                if (preparingTotal > 0) {
                    localizedRes.string(R.string.scene_preparing_progress_format, preparingCompleted, preparingTotal)
                } else {
                    localizedRes.string(R.string.loading)
                }
            }
            Text(
                text = if (isSceneDownloadInProgress) {
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
            if (!isSceneDownloadInProgress) {
                TextButton(onClick = onCancel) {
                    Text(text = localizedRes.string(R.string.cancel), color = White)
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun BindSoundscapeMediaSession() {
    rememberSoundscapeMediaController()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackgroundSearchSheet(
    title: String,
    query: String,
    isPexels: Boolean,
    pexelsVideoTabSelected: Boolean,
    onPexelsTabChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    items: List<BackgroundMediaItem>,
    loading: Boolean,
    error: String?,
    onItemClick: (BackgroundMediaItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isPexels) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = White.copy(alpha = 0.85f)
                    )
                }
            } else {
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text("Cancel", color = Color(0xFF0A84FF))
                }
            }
            Text(
                text = title,
                color = White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = White.copy(alpha = 0.10f),
                unfocusedContainerColor = White.copy(alpha = 0.10f),
                disabledContainerColor = White.copy(alpha = 0.10f),
                focusedTextColor = White,
                unfocusedTextColor = White,
                focusedPlaceholderColor = White.copy(alpha = 0.55f),
                unfocusedPlaceholderColor = White.copy(alpha = 0.55f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = White,
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = White.copy(alpha = 0.65f)
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = White.copy(alpha = 0.65f)
                        )
                    }
                }
            },
            placeholder = {
                Text(if (isPexels && pexelsVideoTabSelected) "Search videos" else "Search photos")
            }
        )
        if (isPexels) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val activeBg = White.copy(alpha = 0.18f)
                TextButton(
                    onClick = { onPexelsTabChange(false) },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!pexelsVideoTabSelected) activeBg else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        ),
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = White.copy(alpha = if (!pexelsVideoTabSelected) 1f else 0.75f)
                    )
                ) { Text("Photos") }
                TextButton(
                    onClick = { onPexelsTabChange(true) },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (pexelsVideoTabSelected) activeBg else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        ),
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = White.copy(alpha = if (pexelsVideoTabSelected) 1f else 0.75f)
                    )
                ) { Text("Videos") }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressIndicator()
                }
            }
            error != null -> {
                Text(
                    text = error,
                    color = White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items.isEmpty() -> {
                Text(
                    text = "No results",
                    color = White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        Box(
                            modifier = Modifier
                                .clickable { onItemClick(item) }
                                .fillMaxWidth()
                                .height(if ((item.id.hashCode() and 1) == 0) 185.dp else 255.dp)
                                .background(Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .border(1.dp, White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = item.previewUrl,
                                contentDescription = item.author,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            0f to Color.Black.copy(alpha = 0.45f),
                                            1f to Color.Transparent
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            0f to Color.Transparent,
                                            1f to Color.Black.copy(alpha = 0.48f)
                                        )
                                    )
                            )
                            Text(
                                text = item.author.ifBlank { "Unknown" },
                                color = White.copy(alpha = 0.96f),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                            if (item.isVideo && item.durationSec != null) {
                                Text(
                                    text = formatDuration(item.durationSec),
                                    color = White.copy(alpha = 0.96f),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        } 
                    }
                }
            }
        }
    }
}

private fun formatDuration(totalSec: Int): String {
    val safe = totalSec.coerceAtLeast(0)
    val mm = safe / 60
    val ss = safe % 60
    return "%02d:%02d".format(mm, ss)
}

@Composable
private fun LocalImageCropDialog(
    sourceUri: Uri,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
) {
    val context = LocalContext.current
    var scale by remember(sourceUri) { mutableStateOf(1f) }
    var offset by remember(sourceUri) { mutableStateOf(Offset.Zero) }
    var frameSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Text(text = "Crop", color = White, style = MaterialTheme.typography.titleMedium)
                    TextButton(
                        onClick = {
                            if (saving || frameSize.width <= 0 || frameSize.height <= 0) return@TextButton
                            saving = true
                            scope.launch {
                                val cropped = importLocalImageWithManualCrop(
                                    context = context,
                                    sourceUri = sourceUri,
                                    cropFrameWidthPx = frameSize.width,
                                    cropFrameHeightPx = frameSize.height,
                                    scale = scale,
                                    offsetX = offset.x,
                                    offsetY = offset.y
                                )
                                saving = false
                                cropped?.let { onApply(it.imageUrl) }
                            }
                        }
                    ) { Text("Apply") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(9f / 16f)
                            .background(Color.Black)
                            .onSizeChanged { frameSize = it }
                            .pointerInput(sourceUri, frameSize) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    val maxX = frameSize.width * (scale - 1f) / 2f
                                    val maxY = frameSize.height * (scale - 1f) / 2f
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                        y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                    )
                                }
                            }
                    ) {
                        AsyncImage(
                            model = sourceUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Text(
                    text = "Move and zoom to choose crop area (9:16)",
                    style = MaterialTheme.typography.bodySmall,
                    color = White.copy(alpha = 0.75f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            if (saving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressIndicator()
                }
            }
        }
    }
}
