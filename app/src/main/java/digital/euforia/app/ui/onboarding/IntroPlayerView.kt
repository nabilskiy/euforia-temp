@file:OptIn(ExperimentalMaterial3Api::class, UnstableApi::class)

package digital.euforia.app.ui.onboarding

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.view.LayoutInflater
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Colors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.rememberExoPlayer
import digital.euforia.app.ui.util.widget.TriangleTooltipBubble
import digital.euforia.app.ui.util.widget.noRippleClickable
import androidx.compose.ui.graphics.Color as ComposeColor


@Composable
fun BoxScope.IntroPlayerView(
    modifier: Modifier = Modifier,
    onSkipClick: () -> Unit,
    onFadeOutStart: () -> Unit,
    onPlaybackComplete: (Boolean) -> Unit,
    nearEndLeadMs: Long = 1_000L,
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val exoPlayer = rememberExoPlayer(
        videoRes = R.raw.vid_intro,
        nearEndLeadMs = nearEndLeadMs,
        onNearEnd = onFadeOutStart,
        onPlaybackComplete = {
            onPlaybackComplete(true)
        },
    )
    var volume by remember { mutableFloatStateOf(exoPlayer.volume) }
    var systemVolume by remember {
        mutableFloatStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        )
    }

    DisposableEffect(
        exoPlayer
    ) {
        val listener = object : Player.Listener {
            override fun onVolumeChanged(newVolume: Float) {
                volume = newVolume
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        )
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    AndroidView(
        factory = { ctx ->
            LayoutInflater.from(ctx)
                .inflate(R.layout.player_view_texture, null, false).also { root ->
                    root.findViewById<PlayerView>(R.id.player_view).apply {
                        this.player = exoPlayer
                        setShutterBackgroundColor(Color.TRANSPARENT)
                        setKeepContentOnPlayerReset(true)
                    }
                }
        },
        update = { root ->
            root.findViewById<PlayerView>(R.id.player_view).player = exoPlayer
        },
        modifier = modifier.fillMaxSize()
    )
    if (volume == 0f || systemVolume == 0f) {
        MuteIcon(onClick = {
            exoPlayer.volume = 0.2f
            if (systemVolume == 0f) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.2f).toInt()
                        .coerceAtLeast(1),
                    AudioManager.FLAG_SHOW_UI
                )
            }
        })
    } else {
        SkipText(onClick = onSkipClick)
    }
}

@Composable
private fun BoxScope.MuteIcon(onClick: () -> Unit) {
    Icon(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .padding(top = 22.dp, end = 16.dp)
            .size(32.dp)
            .background(color = White.copy(alpha = 0.2f), shape = CircleShape)
            .noRippleClickable(onClick = onClick)
            .padding(6.dp),
        painter = painterResource(R.drawable.ic_muted),
        contentDescription = null,
        tint = ComposeColor.Unspecified
    )
}

@Composable
private fun BoxScope.SkipText(onClick: () -> Unit) {
    val localizedResources = LocalLocalizedRes.current
    var isPressed by remember { mutableStateOf(false) }
    val color by animateColorAsState(
        targetValue = if (isPressed) White.copy(alpha = 0.3f) else White.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 200),
        label = "colorAnimation"
    )

    Text(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 22.dp, end = 8.dp)
            .noRippleClickable(onClick = onClick)
            .padding(8.dp)
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        text = localizedResources.string(R.string.skip),
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}
