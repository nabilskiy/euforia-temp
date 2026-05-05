/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.widget

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White

@Composable
fun SceneSoundFloatingButton(
    imageUrl: String?,
    contentDescription: String,
    showSoundAnimation: Boolean,
    repeatProgress: Float?,
    modifier: Modifier = Modifier,
) {
    val borderColor = White.copy(alpha = 0.95f)
    val pulseTransition = rememberInfiniteTransition(label = "sound_button_wave")
    val pulseProgress = pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2300, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sound_button_wave_progress"
    ).value

    val waveScale = 1f + (0.7f * pulseProgress) // iOS: 1.0 -> 1.7
    val waveAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.15f // iOS: white alpha 0.15 fading to 0

    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showSoundAnimation) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(waveScale)
                    .background(White.copy(alpha = waveAlpha), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(1.dp, borderColor, CircleShape)
                .background(Color.Black.copy(alpha = 0.42f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier
                    .padding(10.dp)
                    .size(36.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_sounds),
                contentDescription = contentDescription,
                tint = White,
                modifier = Modifier.size(26.dp)
            )
        }
        }
        if (repeatProgress != null) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 2.dp.toPx()
                val inset = strokeWidth / 2f + 1.dp.toPx()
                drawArc(
                    color = White,
                    startAngle = -90f,
                    sweepAngle = 360f * repeatProgress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = size.copy(width = size.width - inset * 2f, height = size.height - inset * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
    }
}
