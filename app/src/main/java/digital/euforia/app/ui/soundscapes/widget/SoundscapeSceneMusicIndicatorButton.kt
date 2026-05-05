/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.White

// Mirrors iOS MusicIndicatorContentView (pt → dp)
private val BarWidth = 2.dp
private val BarSpacing = 3.dp
private val BarIdleHeight = 6.dp
private val BarMaxPeakHeight = 20.dp
private val BarPeakHeightsDp = floatArrayOf(13f, 16f, 20f, 14f)

/**
 * Wave / headphones visuals matching iOS [MusicIndicatorView] (non-interactive).
 */
@Composable
fun SoundscapeSceneMusicIndicatorContent(
    isPlaying: Boolean,
    sceneMusicUrl: String?,
    musicVolume: Float,
    modifier: Modifier = Modifier,
) {
    val hasMusicUrl = !sceneMusicUrl.isNullOrBlank()
    val effectiveMuted = musicVolume <= 0.001f
    val showWave = hasMusicUrl && !effectiveMuted

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            showWave -> MusicWaveBars(playing = isPlaying, modifier = Modifier.fillMaxSize())
            effectiveMuted && hasMusicUrl -> {
                Icon(
                    painter = painterResource(R.drawable.ic_headphones),
                    contentDescription = null,
                    tint = White.copy(alpha = 0.35f),
                    modifier = Modifier.size(28.dp),
                )
            }
            else -> {
                Icon(
                    painter = painterResource(R.drawable.ic_headphones),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

/**
 * Compact indicator over track artwork (music options sheet, picker preview), matching iOS overlay.
 */
@Composable
fun SoundscapeSceneMusicIndicatorThumbnailOverlay(
    isPlaying: Boolean,
    sceneMusicUrl: String?,
    musicVolume: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        SoundscapeSceneMusicIndicatorContent(
            isPlaying = isPlaying,
            sceneMusicUrl = sceneMusicUrl,
            musicVolume = musicVolume,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 3.dp),
        )
    }
}

/**
 * Scene bottom bar control matching iOS [MusicIndicatorButton] / [MusicIndicatorView]:
 * four rounded bars when scene has background music; headphones when none or muted (volume 0).
 */
@Composable
fun SoundscapeSceneMusicIndicatorButton(
    isPlaying: Boolean,
    sceneMusicUrl: String?,
    musicVolume: Float,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        SoundscapeSceneMusicIndicatorContent(
            isPlaying = isPlaying,
            sceneMusicUrl = sceneMusicUrl,
            musicVolume = musicVolume,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun MusicWaveBars(playing: Boolean, modifier: Modifier = Modifier) {
    if (playing) {
        val transition = rememberInfiniteTransition(label = "soundscape_music_wave")
        val durations = remember { intArrayOf(580, 720, 640, 680) }
        val fractions = List(4) { index ->
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durations[index], easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bar_$index",
            ).value
        }
        MusicWaveBarsCanvas(fractions, modifier)
    } else {
        MusicWaveBarsCanvas(List(4) { 0f }, modifier)
    }
}

@Composable
private fun MusicWaveBarsCanvas(
    oscillationFractions: List<Float>,
    modifier: Modifier = Modifier,
) {
    val barColor = White.copy(alpha = 0.5f)
    Canvas(modifier = modifier) {
        val barW = BarWidth.toPx()
        val gap = BarSpacing.toPx()
        val maxH = BarMaxPeakHeight.toPx()
        val idleH = BarIdleHeight.toPx()
        val contentWidth = 4f * barW + 3f * gap
        val originX = (size.width - contentWidth) / 2f
        val originY = (size.height - maxH) / 2f
        val centerY = originY + maxH / 2f
        val corner = CornerRadius(barW / 2f, barW / 2f)
        var x = originX
        repeat(4) { index ->
            val peak = BarPeakHeightsDp[index].dp.toPx().coerceIn(idleH, maxH)
            val t = oscillationFractions[index].coerceIn(0f, 1f)
            val h = idleH + (peak - idleH) * t
            val top = centerY - h / 2f
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, top),
                size = Size(barW, h),
                cornerRadius = corner,
            )
            x += barW + gap
        }
    }
}
