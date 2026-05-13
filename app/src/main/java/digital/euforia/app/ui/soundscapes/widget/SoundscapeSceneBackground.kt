/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.widget

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import digital.euforia.app.App
import digital.euforia.app.R
import digital.euforia.app.ui.theme.SoundscapesScreenBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import kotlinx.coroutines.delay
import java.net.URI

@OptIn(UnstableApi::class)
@Composable
fun SoundscapeSceneBackground(
    imageUrl: String?,
    videoUrl: String?,
    isPlaying: Boolean,
    isPreparing: Boolean,
    isParallaxEnabled: Boolean,
    onPlaybackProgress: (positionMs: Long, durationMs: Long) -> Unit,
) {
    val context = LocalContext.current

    val parallaxScale = if (isParallaxEnabled) 1.06f else 1f

    val videoExo = remember(videoUrl) {
        if (videoUrl.isNullOrBlank()) null else {
            val trackSelector = DefaultTrackSelector(context).apply {
                parameters = buildUponParameters()
                    // Avoid ABR starting on a low rung then stepping up (looks blurry for ~1–2s).
                    .setForceHighestSupportedBitrate(true)
                    .build()
            }
            val builder = ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
            if (!isLocalUri(videoUrl)) {
                builder.setMediaSourceFactory(buildCachedMediaSourceFactory(context))
            }
            builder.build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    /* handleAudioFocus */ false
                )
                setMediaItem(MediaItem.fromUri(videoUrl))
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
            }
        }
    }
    var hasRenderedFirstFrame by remember(videoUrl) { mutableStateOf(videoUrl.isNullOrBlank()) }
    DisposableEffect(videoExo) {
        val player = videoExo
        if (player == null) {
            hasRenderedFirstFrame = true
            onDispose { }
        } else {
            hasRenderedFirstFrame = false
            val listener = object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    hasRenderedFirstFrame = true
                }
            }
            player.addListener(listener)
            onDispose {
                player.removeListener(listener)
            }
        }
    }
    DisposableEffect(videoExo) { onDispose { videoExo?.release() } }
    LaunchedEffect(videoExo, isPlaying) {
        val v = videoExo ?: return@LaunchedEffect
        val shouldPlay = isPlaying
        v.playWhenReady = shouldPlay
        if (shouldPlay) v.play() else v.pause()
    }
    // Keep blurred poster until a real video frame is drawn — STATE_READY alone still looks soft/low
    // and PlayerView can briefly show buffering/shutter chrome.
    val showBlurredVideoPreview = videoExo != null && !hasRenderedFirstFrame

    // Image-only: always sharp. Video: blurred still preview until Exo paints the first frame; then video on top.
    if (!imageUrl.isNullOrBlank()) {
        val useBlur = showBlurredVideoPreview
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .crossfade(220)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = parallaxScale, scaleY = parallaxScale)
                .then(if (useBlur) Modifier.blur(34.dp) else Modifier),
            contentScale = ContentScale.Crop
        )
        if (useBlur) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.38f))
            )
        }
    } else {
        // Match soundscapes shell tone — avoids a harsh black flash before the first frame/url arrives.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to SoundscapesScreenBackground,
                        0.55f to Color(0xFF121318),
                        1f to Color(0xFF0B0C10),
                    )
                )
        )
    }

    if (videoExo != null) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = videoExo
                    useController = false
                    setControllerAutoShow(false)
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    setShutterBackgroundColor(AndroidColor.TRANSPARENT)
                    setKeepContentOnPlayerReset(true)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            update = {
                it.player = videoExo
                it.useController = false
                it.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = parallaxScale,
                    scaleY = parallaxScale,
                    alpha = if (hasRenderedFirstFrame) 1f else 0f,
                )
        )
    }

    LaunchedEffect(videoExo) {
        if (videoExo == null) {
            onPlaybackProgress(0L, 0L)
            return@LaunchedEffect
        }
        while (true) {
            val pos = videoExo.currentPosition
            val dur = videoExo.duration
            onPlaybackProgress(pos.coerceAtLeast(0), dur.coerceAtLeast(0))
            delay(250)
        }
    }
}

@UnstableApi
private fun buildCachedMediaSourceFactory(context: Context): DefaultMediaSourceFactory {
    val cacheFactory = CacheDataSource.Factory()
        .setCache(App.getExoCache(context))
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    return DefaultMediaSourceFactory(cacheFactory)
}

private fun isLocalUri(url: String): Boolean {
    return url.startsWith("file:/") || runCatching {
        val uri = URI(url)
        uri.scheme.equals("content", ignoreCase = true)
    }.getOrDefault(false)
}

@Composable
fun SoundscapeScenePlayControl(
    isPlaying: Boolean,
    timerTotalSeconds: Int?,
    timerRemainingSeconds: Int?,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val localizedRes = LocalLocalizedRes.current
    val hasTimer = (timerTotalSeconds ?: 0) > 0 && (timerRemainingSeconds ?: -1) >= 0
    val progress = if (hasTimer) {
        val total = (timerTotalSeconds ?: 0).coerceAtLeast(1)
        val left = (timerRemainingSeconds ?: 0).coerceIn(0, total)
        left.toFloat() / total.toFloat()
    } else 0f
    val ringColor = White.copy(alpha = 0.9f)
    val trackColor = White.copy(alpha = 0.25f)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (hasTimer) {
            val left = (timerRemainingSeconds ?: 0).coerceAtLeast(0)
            val hh = left / 3600
            val mm = (left % 3600) / 60
            val ss = left % 60
            Text(
                text = String.format("%02d:%02d:%02d", hh, mm, ss),
                color = White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(88.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasTimer) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val stroke = 3.dp.toPx()
                    val pad = stroke / 2f + 3.dp.toPx()
                    val size = Size(this.size.width - pad * 2, this.size.height - pad * 2)
                    val top = Offset(pad, pad)
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = top,
                        size = size,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = top,
                        size = size,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
            Icon(
                painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = localizedRes.string(if (isPlaying) R.string.pause else R.string.play),
                tint = White,
                modifier = Modifier.size(28.dp)
            )
            }
        }
    }
}
