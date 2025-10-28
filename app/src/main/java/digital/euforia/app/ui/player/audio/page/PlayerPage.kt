package digital.euforia.app.ui.player.audio.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.ui.player.InfoView
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.player.audio.AvatarUi
import digital.euforia.app.ui.player.audio.PlaybackProgressView
import digital.euforia.app.ui.player.audio.SoundEffectUi
import digital.euforia.app.ui.theme.PlayButtonBackground
import digital.euforia.app.ui.theme.PlayButtonDarkBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.eveningColors
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.HeadphonesInfoView
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.clickableSingle
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.vibe.AnimationType
import digital.euforia.app.ui.util.widget.vibe.PlayState
import digital.euforia.app.ui.util.widget.vibe.PlaybackAnimation
import kotlinx.coroutines.delay

@Composable
fun PlayerPage(
    entryPoint: AudioPlayerEntryPoint,
    title: String,
    progress: Float,
    playState: PlayState,
    selectedSoundIndex: Int,
    avatarPreviewIds: List<Int>,
    avatarPreviewUrl: String?,
    avatarUi: AvatarUi?,
    avatarsList: List<AvatarUi>,
    soundsEffects: List<SoundEffectUi>,
    onAvatarClick: () -> Unit,
    onMuteClick: () -> Unit,
    onSoundEffectClick: (Int) -> Unit,
    onPause: () -> Unit,
    onPlay: () -> Unit,
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
        PlaybackAnimation(
            state = playState,
            animationType = AnimationType.SPHERES,
            paddingState = animatedPadding
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SoundsRow(
                soundsEffects = soundsEffects,
                selectedIndex = selectedSoundIndex,
                avatarPreviewUrl = previewUrl,
                onAvatarClick = onAvatarClick,
                onMuteClick = onMuteClick,
                onClick = onSoundEffectClick
            )
            PlaybackProgressView(
                currentTime = 0.8f,
                duration = 1f,
                colors = eveningColors.reversed(),
                onSeek = onSeekTo
            )
        }
    }
}


@Composable
private fun BoxScope.PlayButton(
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
fun SoundsRow(
    soundsEffects: List<SoundEffectUi>,
    selectedIndex: Int,
    avatarPreviewUrl: String?,
    onAvatarClick: () -> Unit,
    onMuteClick: () -> Unit,
    onClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 30.dp)
    ) {
        item {
            AvatarPreviewItem(
                url = avatarPreviewUrl,
                onClick = onAvatarClick
            )
        }
        item {
            MuteItem(isSelected = selectedIndex == -1, onClick = onMuteClick)
        }
        itemsIndexed(items = soundsEffects, key = { index, item -> item.id }) { index, item ->
            SoundItem(
                soundEffectUi = item,
                isSelected = index == selectedIndex,
                onClick = {
                    onClick(index)
                }
            )
        }
    }
}

@Composable
private fun SoundItem(
    soundEffectUi: SoundEffectUi,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val borderWidth = if (isSelected) 3.dp else 1.dp
    val borderColor = if (isSelected) White else White.copy(alpha = 0.3f)
    val size by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 48.dp,
        animationSpec = tween(durationMillis = 300),
        label = "size"
    )
    Box(
        modifier = Modifier.size(56.dp)
            .noRippleClickable(onClick = onClick)
    ) {
        AsyncImage(
            modifier = Modifier.clip(CircleShape)
                .border(width = borderWidth, color = borderColor, shape = CircleShape)
                .align(Alignment.Center)
                .size(size),
            model = soundEffectUi.imageUrl,
            contentDescription = null
        )
        if (isSelected) {
            Box(
                modifier = Modifier.align(Alignment.Center).size(size)
                    .background(color = White.copy(alpha = 0.2f), shape = CircleShape)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.Center).size(24.dp),
                    painter = painterResource(id = R.drawable.ic_slider),
                    contentDescription = null,
                    tint = White
                )
            }
        }
    }
}

@Composable
fun MuteItem(isSelected: Boolean, onClick: () -> Unit) {
    val borderWidth = if (isSelected) 3.dp else 1.dp
    val borderColor = if (isSelected) White else White.copy(alpha = 0.3f)

    Box(
        modifier = Modifier.size(56.dp)
            .noRippleClickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.align(Alignment.Center).size(48.dp)
                .background(color = White.copy(alpha = 0.2f), shape = CircleShape)
                .border(width = borderWidth, color = borderColor, shape = CircleShape)
        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center).size(24.dp),
                painter = painterResource(id = R.drawable.ic_sound_off),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}


@Composable
private fun AvatarPreviewItem(
    url: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clip(CircleShape).size(56.dp)
            .background(color = White.copy(alpha = 0.2f), shape = CircleShape)
            .border(width = 1.dp, color = White.copy(alpha = 0.3f), shape = CircleShape)
            .noRippleClickable(onClick = onClick)
    ) {
        if (url != null) {
            AsyncImage(
                modifier = Modifier.clip(CircleShape)
                    .align(Alignment.Center)
                    .fillMaxSize(),
                model = url,
                contentDescription = null
            )
        } else {
            Icon(
                modifier = Modifier.align(Alignment.Center).size(24.dp),
                painter = painterResource(id = R.drawable.ic_custom_photo),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}