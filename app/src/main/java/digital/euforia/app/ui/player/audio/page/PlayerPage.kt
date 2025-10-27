package digital.euforia.app.ui.player.audio.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.player.InfoView
import digital.euforia.app.ui.theme.PlayButtonBackground
import digital.euforia.app.ui.theme.PlayButtonDarkBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.HeadphonesInfoView
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.vibe.AnimationType
import digital.euforia.app.ui.util.widget.vibe.PlayState
import digital.euforia.app.ui.util.widget.vibe.PlaybackAnimation

@Composable
fun PlayerPage(
    title: String,
    progress: Float,
    playState: PlayState,
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

        InfoView(isVisible = playState == PlayState.READY)

//        Box(
//            modifier = Modifier.fillMaxWidth().aspectRatio(1f).align(Alignment.Center)
//                .padding(52.dp).background(Black, CircleShape)
//        )
        HeadphonesInfoView(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp)
                .navigationBarsPadding().align(Alignment.BottomCenter)
        )
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