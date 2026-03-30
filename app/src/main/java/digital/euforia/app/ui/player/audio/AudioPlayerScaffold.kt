@file:OptIn(ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.player.audio

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.domain.model.getColors
import digital.euforia.app.ui.player.audio.page.AvatarsPage
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.NotificationToast
import digital.euforia.app.ui.player.audio.page.CropView
import digital.euforia.app.ui.player.audio.PreloadImages
import digital.euforia.app.ui.player.audio.page.PlayerPage as PlayerPageComposable
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.delay


@Composable
fun SharedTransitionScope.AudioPlayerScaffold(
    modifier: Modifier,
    isPremium: Boolean,
    isEditMode: Boolean,
    ui: AudioPlayerUiState,
    currentTimeMs: Float,
    durationMs: Float,
    isRateShown: MutableState<Boolean>,
    shared: SharedTransitionScope.SharedContentState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isNetworkAvailable: Boolean,
    errorState: ErrorViewState?,
    shareText: String,
    rating: Int,
    selectedAvatarIds: List<Int>,
    onSubmitClick: (Int, String?) -> Unit,
    onRatingUpdated: (Int?) -> Unit,
    onBack: () -> Unit,
    onPageSelected: (Int) -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onMuteClick: () -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    onAvatarClick: (AvatarUi?) -> Unit,
    navigateAvatars: () -> Unit,
    navigatePlayer: () -> Unit,
    saveProgress: () -> Unit,
    logListenLaterEvent: () -> Unit,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onEditClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onSelect: (Int) -> Unit,
    onDeleteSelectedClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    onDoneClick: () -> Unit,
    onAddAvatarClick: () -> Unit,
    isCropping: Boolean = false,
    croppingImageUri: Uri? = null,
    onCropDone: (Uri) -> Unit = {},
    onCropCancel: () -> Unit = {},
    showAvatarChangedToast: Boolean = false,
    onDismissAvatarChangedToast: () -> Unit = {}
) {
    val localizedRes = LocalLocalizedRes.current
    var isVisible by remember { mutableStateOf(false) }
    BackHandler {
        if (ui.pages.getOrNull(ui.currentPageIndex) == PlayerPage.Avatars) {
            navigatePlayer()
        } else {
            if (isRateShown.value) {
                if (durationMs > 0 && currentTimeMs >= durationMs * 0.95f) {
                    onBack()
                } else {
                    isRateShown.value = false
                }
            } else {
                saveProgress()
                onBack()
            }
        }
    }

    val pagerState = rememberPagerState(initialPage = ui.currentPageIndex) { ui.pages.size }

    LaunchedEffect(ui.currentPageIndex) {
        if (pagerState.currentPage != ui.currentPageIndex) {
            pagerState.animateScrollToPage(ui.currentPageIndex, animationSpec = tween(600))
        }
    }


    LaunchedEffect(Unit) {
        isVisible = true
    }

    PreloadImages(avatarsList = ui.avatarsList, soundsList = ui.soundsEffects)

    Box(
        modifier = modifier.fillMaxSize().background(
            brush = Brush.linearGradient(
                ui.timeOfDay.getColors().map { it.copy(alpha = 0.4f) }),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 600)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            if (errorState == null) {
                HorizontalPager(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp),
                    state = pagerState,
                    userScrollEnabled = false
                ) { page ->
                    when (page) {
                        0 -> PlayerPageComposable(
                            entryPoint = ui.entryPoint,
                            timeOfDay = ui.timeOfDay,
                            title = ui.title,
                            playState = ui.playState,
                            selectedSoundIndex = ui.selectedSoundIndex,
                            avatarPreviewUrl = ui.avatarPreviewUrl,
                            avatarPreviewIds = ui.avatarPreviewIds,
                            avatarUi = ui.avatarUi,
                            avatarsList = ui.avatarsList,
                            soundsEffects = ui.soundsEffects,
                            currentTimeMs = currentTimeMs,
                            durationMs = durationMs,
                            isRateShown = isRateShown,
                            shareText = shareText,
                            rating = rating,
                            onSubmitClick = onSubmitClick,
                            onRatingUpdated = onRatingUpdated,
                            onAvatarClick = navigateAvatars,
                            onMuteClick = onMuteClick,
                            onSoundEffectClick = onSoundEffectClick,
                            onPause = onPause,
                            onPlay = onPlay,
                            onSeekTo = onSeekTo,
                            onListenLaterClick = {
                                logListenLaterEvent()
                                saveProgress()
                            }
                        )

                        else -> AvatarsPage(
                            isPremium = isPremium,
                            isEditMode = isEditMode,
                            avatarsList = ui.avatarsList,
                            selectedAvatar = ui.avatarUi,
                            selectedAvatarIds = selectedAvatarIds,
                            onAvatarClick = onAvatarClick,
                            onEditClick = onEditClick,
                            onSelectAllClick = onSelectAllClick,
                            onSelect = onSelect,
                            onDeleteSelectedClick = onDeleteSelectedClick,
                            onDeleteClick = onDeleteClick,
                            onAddAvatarClick = onAddAvatarClick
                        )
                    }
                }
            } else {
                ErrorView(
                    modifier = Modifier.align(Alignment.Center).fillMaxSize(),
                    state = errorState,
                    onRetryClick = onRetryClick,
                    onDownloadsClick = onDownloadsClick
                )
//                NoConnectionView(
//                    modifier = Modifier.align(Alignment.Center).fillMaxSize(),
//                    onRetryClick = onRetryClick,
//                    onDownloadClick = onDownloadsClick
//                )
            }
            AudioPlayerAppBar(
                currentPageIndex = ui.currentPageIndex,
                isEditMode = isEditMode,
                entryPoint = ui.entryPoint,
                onBackClick = {
                    if (isRateShown.value) {
                        if (durationMs > 0 && currentTimeMs >= durationMs * 0.95f) {
                            onBack()
                        } else {
                            isRateShown.value = false
                        }
                    } else {
                        onBack()
                    }
                },
                onDoneClick = onDoneClick
            )

            NotificationToast(
                text = localizedRes.string(R.string.vibes_avatar_changed),
                isVisible = showAvatarChangedToast,
                onDismissed = onDismissAvatarChangedToast
            )

            if (isCropping && croppingImageUri != null) {
                CropView(
                    imageUri = croppingImageUri,
                    onCancelClick = onCropCancel,
                    onDoneClick = onCropDone
                )
            }
        }

    }
}

@Composable
private fun AudioPlayerAppBar(
    currentPageIndex: Int,
    isEditMode: Boolean,
    entryPoint: AudioPlayerEntryPoint,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    val localizedRes = LocalLocalizedRes.current

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
                .heightIn(min = AppBarHeightLarge)
        ) {
            when (target) {
                0 -> {
                    if (entryPoint == AudioPlayerEntryPoint.DAY) {
                        Icon(
                            modifier = Modifier.align(Alignment.CenterStart).size(24.dp)
                                .noRippleClickable(onClick = onBackClick),
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }

                else -> {
                    if (!isEditMode) {
//                        Row(
//                            modifier = Modifier.noRippleClickable(onBackClick),
//                            horizontalArrangement = Arrangement.spacedBy(4.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                        ) {
                        Icon(
                            modifier = Modifier.align(Alignment.CenterStart).size(24.dp)
                                .noRippleClickable(onClick = onBackClick),
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
//                            Text(
//                                text = localizedRes.string(R.string.back),
//                                color = White,
//                                style = appbarMedium.copy(fontWeight = FontWeight.Medium),
//                                modifier = Modifier
//                                    .padding(horizontal = 16.dp)
//                                    .noRippleClickable(onDoneClick)
//                            )
//                        }
                    } else {
                        Text(
                            text = localizedRes.string(R.string.done),
                            color = White,
                            style = appbarMedium.copy(fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .noRippleClickable(onDoneClick)
                                .align(Alignment.CenterStart)
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
}