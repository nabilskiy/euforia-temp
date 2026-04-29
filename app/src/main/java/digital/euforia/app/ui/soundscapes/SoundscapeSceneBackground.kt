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
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import digital.euforia.app.App
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
    isPlaying: Boolean,
    isPreparing: Boolean,
    isParallaxEnabled: Boolean,
    onPlaybackProgress: (positionMs: Long, durationMs: Long) -> Unit,
) {
    val context = LocalContext.current
    var displayImageUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(imageUrl) {
        if (!imageUrl.isNullOrBlank()) {
            displayImageUrl = imageUrl
        }
    }

    val parallaxScale = if (isParallaxEnabled) 1.06f else 1f

    if (!displayImageUrl.isNullOrBlank()) {
        AsyncImage(
            model = displayImageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = parallaxScale, scaleY = parallaxScale),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(Modifier.fillMaxSize().background(Black))
    }

    val videoExo = remember(videoUrl) {
        if (videoUrl.isNullOrBlank()) null else ExoPlayer.Builder(context)
            .setMediaSourceFactory(buildCachedMediaSourceFactory(context))
            .build().apply {
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
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = parallaxScale, scaleY = parallaxScale)
        )
    }

    val showBlurredPlaceholder = isPreparing || !hasRenderedFirstFrame
    if (showBlurredPlaceholder && !displayImageUrl.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(displayImageUrl)
                .allowHardware(false)
                .crossfade(false)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = parallaxScale, scaleY = parallaxScale)
                .blur(34.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.38f))
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
private fun buildCachedMediaSourceFactory(context: android.content.Context): DefaultMediaSourceFactory {
    val cacheFactory = CacheDataSource.Factory()
        .setCache(App.getExoCache(context))
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    return DefaultMediaSourceFactory(cacheFactory)
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
