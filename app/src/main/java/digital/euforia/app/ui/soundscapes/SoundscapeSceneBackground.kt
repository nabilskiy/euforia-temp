/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.formatDuration
import kotlinx.coroutines.delay
import kotlin.math.min

@OptIn(UnstableApi::class)
@Composable
fun SoundscapeSceneBackground(
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
        if (videoUrl.isNullOrBlank()) null else ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
        }
    }
    DisposableEffect(videoExo) { onDispose { videoExo?.release() } }
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
        if (musicUrl.isNullOrBlank()) null else ExoPlayer.Builder(context).build().apply {
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
    DisposableEffect(musicPlayer) { onDispose { musicPlayer?.release() } }
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
fun SoundscapeScenePlayControl(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (durationMs > 0) min(1f, positionMs.toFloat() / durationMs.toFloat()) else 0f
    val ringColor = White.copy(alpha = 0.9f)
    val trackColor = White.copy(alpha = 0.25f)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatDuration(positionMs),
            color = White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val stroke = 3.dp.toPx()
                val pad = stroke / 2f + 2.dp.toPx()
                val size = Size(this.size.width - pad * 2, this.size.height - pad * 2)
                val top = Offset(pad, pad)
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
