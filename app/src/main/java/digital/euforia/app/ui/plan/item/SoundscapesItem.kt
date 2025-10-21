package digital.euforia.app.ui.plan.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.plan.PlanViewItems

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import digital.euforia.app.ui.theme.SoundscapeColors
import digital.euforia.app.ui.theme.eveningColors
import digital.euforia.app.R
import digital.euforia.app.ui.theme.SecondaryText
import digital.euforia.app.ui.theme.SoundscapesButtonBackground
import digital.euforia.app.ui.theme.White

fun LazyListScope.soundscapesItem(
    isDemo: Boolean,
    onSoundscapesClick: () -> Unit,
    onBannerClick: () -> Unit,
) = item(key = PlanViewItems.SOUNDSCAPES, contentType = PlanViewItems.SOUNDSCAPES) {
    Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = 600.dp).triRadialGradient(
            center1 = Offset(200f, 500f),
            center2 = Offset(900f, 700f),
            center3 = Offset(300f, 1200f),
            radius1Px = 400f,
            radius2Px = 500f,
            radius3Px = 600f,
            color1 = eveningColors[0],
            color2 = eveningColors[1],
            color3 = eveningColors[2],
        )
    ) {
        SoundscapesContent(onSoundscapesClick)
    }
}

@Composable
private fun SoundscapesContent(onSoundscapesClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)) {
        soundscapes.forEachIndexed { index, soundscapeUi ->
            FloatingSoundscape(
                modifier = Modifier.padding(0.dp),
                soundscapeUi = soundscapeUi
            )
        }

        Column(
            modifier = Modifier.align(Alignment.TopCenter)
                .padding(start = 48.dp, top = 144.dp, end = 48.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(R.string.today_info_step_scenes_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
                color = White,
                textAlign = TextAlign.Start,
            )

            Text(
                text = stringResource(R.string.today_info_step_scenes_text),
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                textAlign = TextAlign.Start,

                )
            SoundscapesButton(onSoundscapesClick)
        }
    }
}

@Composable
private fun BoxScope.FloatingSoundscape(modifier: Modifier = Modifier, soundscapeUi: SoundscapeUi) {
    val transition = rememberInfiniteTransition(label = "soundscape${soundscapeUi.iconRes}")
    val easing = FastOutSlowInEasing
    val size by transition.animateFloat(
        initialValue = soundscapeUi.sizeRange.first,
        targetValue = soundscapeUi.sizeRange.second,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = soundscapeUi.durationMs,
                easing = easing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing-scale"
    )
    val animatedOffset by transition.animateValue(
        initialValue = soundscapeUi.minOffset,
        targetValue = soundscapeUi.maxOffset,
        typeConverter = Offset.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = soundscapeUi.durationMs,
                easing = easing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    Icon(
        modifier = modifier
            .align(Alignment.Center)
            .graphicsLayer {
                translationX = animatedOffset.x
                translationY = animatedOffset.y
                scaleX = size
                scaleY = size
            }
            .border(width = 1.dp, color = Color.White.copy(0.6f), shape = CircleShape)
            .padding(soundscapeUi.borderPadding)
            .background(color = soundscapeUi.color, shape = CircleShape)
            .padding(8.dp)
            .size(30.dp),
        painter = painterResource(soundscapeUi.iconRes),
        contentDescription = null,
        tint = Color.Unspecified,
    )
}

fun Modifier.triRadialGradient(
    center1: Offset,
    center2: Offset,
    center3: Offset,
    radius1Px: Float,
    radius2Px: Float,
    radius3Px: Float,
    color1: Color,
    color2: Color,
    color3: Color,
): Modifier = composed {

    drawWithCache {

        fun ballBrush(center: Offset, radius: Float, base: Color) = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to base.copy(alpha = 0.4f),
                1.0f to base.copy(alpha = 0f)
            ),
            center = center,
            radius = radius
        )

        val blendMode = BlendMode.ColorDodge

        onDrawBehind {
            drawCircle(
                brush = ballBrush(center1, radius1Px, color1),
                radius = radius1Px,
                center = center1,
                blendMode = blendMode
            )
            drawCircle(
                brush = ballBrush(center2, radius2Px, color2),
                radius = radius2Px,
                center = center2,
                blendMode = blendMode
            )
            drawCircle(
                brush = ballBrush(center3, radius3Px, color3),
                radius = radius3Px,
                center = center3,
                blendMode = blendMode
            )
        }
    }
}

@Composable
fun SoundscapesButton(onClick: () -> Unit) {
    FilledTonalButton(
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = SoundscapesButtonBackground,
        ),
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.today_info_step_scenes_button),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
            maxLines = 3,
            overflow = Ellipsis,
            color = White
        )
    }
}

private data class SoundscapeUi(
    val iconRes: Int,
    val color: Color,
    val sizeRange: Pair<Float, Float>,
    val iconPadding: Dp = 6.dp,
    val borderPadding: Dp = 4.dp,
    val minOffset: Offset,
    val maxOffset: Offset,
    val durationMs: Int = 5000,
)

private val soundscapes = buildList {
    add(
        SoundscapeUi(
            iconRes = R.drawable.ic_fire,
            color = SoundscapeColors[0],
            sizeRange = 0.8f to 1f,
            minOffset = Offset(-310f, -160f),
            maxOffset = Offset(-350f, -320f),
        )
    )
    add(
        SoundscapeUi(
            iconRes = R.drawable.ic_book,
            color = SoundscapeColors[1],
            borderPadding = 5.dp,
            sizeRange = 0.6f to 0.7f,
            minOffset = Offset(-26f, -196f),
            maxOffset = Offset(-6f, -186f),
        )
    )
    add(
        SoundscapeUi(
            iconRes = R.drawable.ic_forest,
            color = SoundscapeColors[2],
            iconPadding = 12.dp,
            sizeRange = 1f to 1.1f,
            minOffset = Offset(160f, -66f),
            maxOffset = Offset(120f, -46f),
        )
    )
    add(
        SoundscapeUi(
            iconRes = R.drawable.ic_birds,
            color = SoundscapeColors[3],
            iconPadding = 12.dp,
            sizeRange = 1.1f to 1.2f,
            minOffset = Offset(440f, -20f),
            maxOffset = Offset(420f, 40f),
        )
    )
    add(
        SoundscapeUi(
            iconRes = R.drawable.ic_book,
            color = SoundscapeColors[4],
            borderPadding = 5.dp,
            sizeRange = 0.5f to 0.6f,
            minOffset = Offset(-250f, 560f),
            maxOffset = Offset(-260f, 580f),
        )
    )
    add(
        SoundscapeUi(
            iconRes = R.drawable.ic_snow,
            color = SoundscapeColors[5],
            sizeRange = 0.7f to 0.8f,
            minOffset = Offset(150f, 520f),
            maxOffset = Offset(220f, 460f),
        )
    )
    add(
        SoundscapeUi(
            iconRes = R.drawable.ic_cat,
            color = SoundscapeColors[6],
            sizeRange = 0.9f to 1.1f,
            minOffset = Offset(400f, 460f),
            maxOffset = Offset(450f, 510f),
        )
    )
}
