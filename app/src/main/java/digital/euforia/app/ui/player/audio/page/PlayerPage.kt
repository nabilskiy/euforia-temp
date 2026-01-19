package digital.euforia.app.ui.player.audio.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DefaultTooltipCaretShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.getColors
import digital.euforia.app.domain.model.getLabelRes
import digital.euforia.app.ui.player.audio.AppBarHeightLarge
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.player.audio.AvatarUi
import digital.euforia.app.ui.player.audio.PlaybackProgressView
import digital.euforia.app.ui.util.widget.SoundEffectUi
import digital.euforia.app.ui.theme.PlayButtonBackground
import digital.euforia.app.ui.theme.PlayButtonDarkBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.theme.appbarSmall
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.HeadphonesInfoView
import digital.euforia.app.ui.util.widget.MenuItem
import digital.euforia.app.ui.util.widget.OptionsMenu
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.SoundsEffectsView
import digital.euforia.app.ui.util.widget.TriangleTooltipBubble
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.vibe.AnimationType
import digital.euforia.app.ui.util.widget.vibe.PlayState
import digital.euforia.app.ui.util.widget.vibe.PlaybackAnimation
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PlayerPage(
    entryPoint: AudioPlayerEntryPoint,
    timeOfDay: TimeOfDay,
    title: String,
    playState: PlayState,
    selectedSoundIndex: Int,
    avatarPreviewIds: List<Int>,
    avatarPreviewUrl: String?,
    avatarUi: AvatarUi?,
    avatarsList: List<AvatarUi>,
    soundsEffects: List<SoundEffectUi>,
    currentTimeMs: Float,
    durationMs: Float,
    onAvatarClick: () -> Unit,
    onMuteClick: () -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onListenLaterClick: () -> Unit
) {
    when (entryPoint) {
        AudioPlayerEntryPoint.DAY -> {
            DefaultPlayerOverlay(
                playState = playState,
                avatarPreviewIds = avatarPreviewIds,
                avatarsList = avatarsList,
                avatarUi = avatarUi,
                timeOfDay = timeOfDay,
                title = title,
                onPause = onPause,
                onPlay = onPlay,
                entryPoint = entryPoint,
                soundsEffects = soundsEffects,
                selectedSoundIndex = selectedSoundIndex,
                onAvatarClick = onAvatarClick,
                onMuteClick = onMuteClick,
                onSoundEffectClick = onSoundEffectClick,
                currentTimeMs = currentTimeMs,
                durationMs = durationMs,
                onSeekTo = onSeekTo
            )
        }

        AudioPlayerEntryPoint.ONBOARDING -> {
            OnboardingPlayerOverlay(
                playState = playState,
                avatarPreviewIds = avatarPreviewIds,
                avatarsList = avatarsList,
                avatarUi = avatarUi,
                timeOfDay = timeOfDay,
                title = title,
                onPause = onPause,
                onPlay = onPlay,
                entryPoint = entryPoint,
                soundsEffects = soundsEffects,
                selectedSoundIndex = selectedSoundIndex,
                onAvatarClick = onAvatarClick,
                onMuteClick = onMuteClick,
                onSoundEffectClick = onSoundEffectClick,
                currentTimeMs = currentTimeMs,
                durationMs = durationMs,
                onSeekTo = onSeekTo,
                onListenLaterClick = onListenLaterClick
            )
        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun DefaultPlayerOverlay(
    playState: PlayState,
    avatarPreviewIds: List<Int>,
    avatarsList: List<AvatarUi>,
    avatarUi: AvatarUi?,
    timeOfDay: TimeOfDay,
    title: String,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    entryPoint: AudioPlayerEntryPoint,
    soundsEffects: List<SoundEffectUi>,
    selectedSoundIndex: Int,
    onAvatarClick: () -> Unit,
    onMuteClick: () -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    currentTimeMs: Float,
    durationMs: Float,
    onSeekTo: (Float) -> Unit
) {
    val animatedPadding by animateDpAsState(
        if (playState == PlayState.READY) {
            200.dp
        } else {
            0.dp
        },
        animationSpec = tween(durationMillis = 1000, delayMillis = 0),
        label = "padding"
    )
    var previewUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(avatarPreviewIds) {
        var index = 0
        while (true) {
            delay(3000L)
            previewUrl = avatarsList.firstOrNull { avatarUi ->
                avatarUi.id == avatarPreviewIds.getOrNull(index)
            }?.imageUrl

            index = if (avatarPreviewIds.isNotEmpty()) {
                (index + 1) % avatarPreviewIds.size
            } else 0
        }

    }

    Box(modifier = Modifier.fillMaxSize()) {
        val animationType = if (avatarUi != null) {
            AnimationType.CIRCLE
        } else {
            AnimationType.SPHERES
        }
        PlaybackAnimation(
            state = playState,
            animationType = animationType,
            paddingState = animatedPadding,
            colors = timeOfDay.getColors()
        )

        if (animationType == AnimationType.CIRCLE && avatarUi != null) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).align(Alignment.Center)
                    .padding(48.dp).clip(CircleShape)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    model = avatarUi.imageUrl,
                    contentDescription = null
                )
            }
        }

        AppBar(
            timeOfDay = timeOfDay,
            title = title
        )

        PlayButton(
            playState = playState,
            paddingState = animatedPadding,
            onPause = onPause,
            onPlay = onPlay,
        )

        if (entryPoint == AudioPlayerEntryPoint.ONBOARDING) {
            InfoView(isVisible = playState == PlayState.READY)
            HeadphonesInfoView(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp)
                    .navigationBarsPadding().align(Alignment.BottomCenter)
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SoundsEffectsView(
                soundsEffects = soundsEffects,
                selectedIndex = selectedSoundIndex,
                avatarPreviewUrl = previewUrl,
                onAvatarClick = onAvatarClick,
                onMuteClick = onMuteClick,
                onClick = onSoundEffectClick
            )
            PlaybackProgressView(
                currentTime = currentTimeMs,
                duration = durationMs,
                colors = timeOfDay.getColors().reversed(),
                onSeek = onSeekTo
            )
        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun OnboardingPlayerOverlay(
    playState: PlayState,
    avatarPreviewIds: List<Int>,
    avatarsList: List<AvatarUi>,
    avatarUi: AvatarUi?,
    timeOfDay: TimeOfDay,
    title: String,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    entryPoint: AudioPlayerEntryPoint,
    soundsEffects: List<SoundEffectUi>,
    selectedSoundIndex: Int,
    onAvatarClick: () -> Unit,
    onMuteClick: () -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    currentTimeMs: Float,
    durationMs: Float,
    onSeekTo: (Float) -> Unit,
    onListenLaterClick: () -> Unit
) {

    val animatedPadding by animateDpAsState(
        if (playState == PlayState.READY) {
            200.dp
        } else {
            0.dp
        },
        animationSpec = tween(durationMillis = 1000, delayMillis = 0),
        label = "padding"
    )

    var isHintShown by remember { mutableStateOf(false) }
    var previewUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(avatarPreviewIds) {
        var index = 0
        while (true) {
            delay(3000L)
            previewUrl = avatarsList.firstOrNull { avatarUi ->
                avatarUi.id == avatarPreviewIds.getOrNull(index)
            }?.imageUrl

            index = if (avatarPreviewIds.isNotEmpty()) {
                (index + 1) % avatarPreviewIds.size
            } else 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val animationType = if (avatarUi != null) {
            AnimationType.CIRCLE
        } else {
            AnimationType.SPHERES
        }
        PlaybackAnimation(
            state = playState,
            animationType = animationType,
            paddingState = animatedPadding,
            colors = timeOfDay.getColors()
        )

        if (animationType == AnimationType.CIRCLE && avatarUi != null) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).align(Alignment.Center)
                    .padding(48.dp).clip(CircleShape)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    model = avatarUi.imageUrl,
                    contentDescription = null
                )
            }
        }

        val isControlsVisible =
            playState != PlayState.READY && playState != PlayState.LOADING && playState != PlayState.LOADED

        LaunchedEffect(isControlsVisible) {
            delay(3000L)
            isHintShown = true
            delay(6000L)
            isHintShown = false
        }

        AnimatedVisibility(
            visible = isControlsVisible,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it }
        ) {
            AppBar(
                timeOfDay = timeOfDay,
                title = title,
                menuItems = listOf(
                    MenuItem(
                        titleRes = R.string.listen_later,
                        onClick = onListenLaterClick
                    )
                )
            )
        }


        PlayButton(
            playState = playState,
            paddingState = animatedPadding,
            onPause = onPause,
            onPlay = onPlay,
        )

        AnimatedContent(
            isControlsVisible,
            label = "controlsVisibility",
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith
                        fadeOut(animationSpec = tween(500))
            }
        ) { isVisible ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (isVisible) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SoundsEffectsView(
                            soundsEffects = soundsEffects,
                            selectedIndex = selectedSoundIndex,
                            avatarPreviewUrl = previewUrl,
                            isHintShown = isHintShown,
                            onAvatarClick = onAvatarClick,
                            onMuteClick = onMuteClick,
                            onClick = onSoundEffectClick
                        )
                        PlaybackProgressView(
                            currentTime = currentTimeMs,
                            duration = durationMs,
                            colors = timeOfDay.getColors().reversed(),
                            onSeek = onSeekTo
                        )
                    }
                } else {
                    InfoView(isVisible = playState == PlayState.READY)
                    HeadphonesInfoView(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp)
                            .navigationBarsPadding().align(Alignment.BottomCenter)
                    )
                }
            }
        }

    }
}

@Composable
@androidx.compose.animation.ExperimentalSharedTransitionApi
private fun BoxScope.PlayButton(
    modifier: Modifier = Modifier,
    playState: PlayState,
    paddingState: Dp,
    onPlay: () -> Unit,
    onPause: () -> Unit
) {

    val buttonColor by animateColorAsState(
        if (playState == PlayState.READY) PlayButtonDarkBackground else PlayButtonBackground,
        label = "buttonColor",
        animationSpec = tween(durationMillis = 1000)
    )

    val iconRes = when (playState) {
        PlayState.PLAYING -> R.drawable.ic_pause
        PlayState.PAUSED, PlayState.READY -> R.drawable.ic_play
        PlayState.LOADED -> R.drawable.ic_done
        else -> null
    }

    Box(
        modifier = Modifier.align(Alignment.Center).padding(bottom = paddingState).size(90.dp)
            .background(color = buttonColor, shape = CircleShape)
            .noRippleClickable(onClick = {
                when (playState) {
                    PlayState.PLAYING -> onPause()
                    PlayState.PAUSED, PlayState.READY -> onPlay()
                    else -> {}
                }
            })
    ) {
        if (playState != PlayState.LOADING) {
            iconRes?.let {
                Icon(
                    modifier = Modifier.align(Alignment.Center).padding(30.dp),
                    painter = painterResource(id = it), contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        } else {
            ProgressIndicator(Modifier.align(Alignment.Center))

        }
    }
}

@Composable
fun BoxScope.InfoView(modifier: Modifier = Modifier, isVisible: Boolean) {
    val localizedRes = LocalLocalizedRes.current
    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 1000, delayMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 1200, delayMillis = 600)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 1000)
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.Center)
                .padding(start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(top = 160.dp, bottom = 16.dp),
                text = localizedRes.string(R.string.vibes_hint_title),
                color = White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = Bold),
                textAlign = TextAlign.Center,
            )
            Text(
                text = localizedRes.string(R.string.vibes_hint_text),
                color = White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BoxScope.AppBar(
    timeOfDay: TimeOfDay, title: String,
    menuItems: List<MenuItem> = emptyList(),
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp)
            .heightIn(min = AppBarHeightLarge).align(Alignment.TopCenter).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(24.dp))
        TitleView(
            timeOfDay = timeOfDay,
            title = title
        )

        Box {
            Icon(
                modifier = Modifier.size(24.dp).noRippleClickable { expanded = !expanded },
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = null,
                tint = Color.Unspecified
            )
            OptionsMenu(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                menuItems = menuItems
            )
        }
    }
}

@Composable
private fun RowScope.TitleView(timeOfDay: TimeOfDay, title: String) {
    Column(
        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = LocalLocalizedRes.current.string(timeOfDay.getLabelRes()),
            color = White,
            style = appbarMedium,
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = title,
            color = White.copy(alpha = 0.6f),
            style = appbarSmall,
        )
    }
}