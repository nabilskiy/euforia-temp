/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.widget

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun AnimatedAddSoundsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val localizedRes = LocalLocalizedRes.current
    val transition = rememberInfiniteTransition(label = "add_sounds_button_transition")
    val crossPulseScale by transition.animateFloat(
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
    val crossPulseRotation by transition.animateFloat(
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
    val crossPulseAlpha by transition.animateFloat(
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
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(28.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_sounds_scene_add_note),
                contentDescription = localizedRes.string(R.string.sound_add_to_scene),
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize(),
            )
            Icon(
                painter = painterResource(R.drawable.ic_sounds_scene_add_cross),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Viewport 24×24: stroke cross centered at ~(20, 4)
                        transformOrigin = TransformOrigin(20f / 24f, 4f / 24f)
                        scaleX = if (enabled) crossPulseScale else 1f
                        scaleY = if (enabled) crossPulseScale else 1f
                        rotationZ = if (enabled) crossPulseRotation else 0f
                        alpha = if (enabled) crossPulseAlpha else 1f
                    },
            )
        }
    }
}
