@file:OptIn(ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.player.audio

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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.domain.model.getColors
import digital.euforia.app.ui.player.audio.page.AvatarsPage
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.player.audio.page.PlayerPage as PlayerPageComposable
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.delay


@Composable
fun SharedTransitionScope.AudioPlayerScaffold(
    modifier: Modifier,
    ui: AudioPlayerUiState,
    currentTimeMs: Float,
    durationMs: Float,
    shared: SharedTransitionScope.SharedContentState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isNetworkAvailable: Boolean,
    errorState: ErrorViewState?,
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
    onDownloadsClick: () -> Unit
) {
    BackHandler {
        if (ui.pages.getOrNull(ui.currentPageIndex) == PlayerPage.Avatars) {
            navigatePlayer()
        } else {
            saveProgress()
//            onBack()
        }
    }

    val pagerState = rememberPagerState(initialPage = ui.currentPageIndex) { ui.pages.size }

    LaunchedEffect(ui.currentPageIndex) {
        if (pagerState.currentPage != ui.currentPageIndex) {
            pagerState.animateScrollToPage(ui.currentPageIndex, animationSpec = tween(600))
        }
    }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    Box(
        modifier = modifier.fillMaxSize().background(
            brush = Brush.linearGradient(
                ui.timeOfDay.getColors().map { it.copy(alpha = 0.4f) }),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 2000)),
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
                            avatarsList = ui.avatarsList,
                            selectedAvatar = ui.avatarUi,
                            onAvatarClick = onAvatarClick
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
                entryPoint = ui.entryPoint,
                onBackClick = {
                    if (ui.pages.getOrNull(ui.currentPageIndex) == PlayerPage.Avatars) navigatePlayer() else saveProgress()
                })
        }

    }
}

@Composable
private fun AudioPlayerAppBar(
    currentPageIndex: Int,
    entryPoint: AudioPlayerEntryPoint,
    onBackClick: () -> Unit
) {
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