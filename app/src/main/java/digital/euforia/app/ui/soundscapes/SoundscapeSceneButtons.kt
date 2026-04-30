/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White

@Composable
fun AnimatedAddSoundsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "add_sounds_button_transition")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2500
                1f at 0 using FastOutSlowInEasing
                1.25f at 370 using FastOutSlowInEasing
                1f at 1000 using FastOutSlowInEasing
                1f at 2500
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "add_sounds_button_scale"
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2500
                0f at 0 using FastOutSlowInEasing
                90f at 370 using FastOutSlowInEasing
                180f at 1000 using FastOutSlowInEasing
                180f at 2500
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "add_sounds_button_rotation"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2500
                0.5f at 0 using FastOutSlowInEasing
                1f at 370 using FastOutSlowInEasing
                0.5f at 1000 using FastOutSlowInEasing
                0.5f at 2500
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "add_sounds_button_alpha"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_sounds_scene_add),
            contentDescription = stringResource(R.string.sound_add_to_scene),
            tint = White,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer(
                    scaleX = if (enabled) scale else 1f,
                    scaleY = if (enabled) scale else 1f,
                    rotationZ = if (enabled) rotation else 0f,
                    alpha = if (enabled) alpha else 1f
                )
        )
    }
}
