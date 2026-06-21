package digital.euforia.app.ui.onboardingV3.pager

import androidx.annotation.RawRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.MediaPlayerHelper
import kotlinx.coroutines.delay

data class AboutPageConfig(
    val titleRes: Int,
    val bodyRes: Int,
    val boldPartRes: Int,
    @param:RawRes val voiceRes: Int,
    val centerColor: Color,
    val edgeColor: Color,
    val iconType: AboutIconType,
)

enum class AboutIconType {
    Headphones,
    Meditation,
    Soundscapes,
}

private val AboutIconType.iconRes: Int
    get() = when (this) {
        AboutIconType.Headphones -> R.drawable.ic_intro_audiosession
        AboutIconType.Meditation -> R.drawable.ic_intro_meditation
        AboutIconType.Soundscapes -> R.drawable.ic_intro_soundscape
    }

@Composable
fun AboutPage(
    config: AboutPageConfig,
    isPageActive: Boolean,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val context = LocalContext.current
    val title = localizedRes.string(config.titleRes)
    val body = localizedRes.string(config.bodyRes)
    val boldPart = localizedRes.string(config.boldPartRes)
    val appearance = remember(config.iconType) { Animatable(0f) }

    LaunchedEffect(config.iconType) {
        appearance.snapTo(0f)
        appearance.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1_200, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(config.voiceRes) {
        // Wait until the outgoing page is disposed so its cleanup doesn't cut the new voice.
        delay(360)
        MediaPlayerHelper.play(context, config.voiceRes)
    }

    DisposableEffect(Unit) {
        onDispose { MediaPlayerHelper.release() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "aboutHero")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(5600), RepeatMode.Reverse),
        label = "ringScale",
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5600), RepeatMode.Reverse),
        label = "ringAlpha",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(config.centerColor.mix(Color.Black, 0.92f)),
    ) {
        val unit = maxWidth / 402f
        val heroCenterY = maxHeight * 0.35f
        val iconSize = unit * 40f
        val heroAppear = introStagger(appearance.value, start = 0.10f, end = 0.72f)
        val titleAppear = introStagger(appearance.value, start = 0.34f, end = 0.78f)
        val bodyAppear = introStagger(appearance.value, start = 0.44f, end = 0.92f)

        AboutBackgroundCanvas(
            color = config.centerColor,
            ringScale = ringScale,
            ringAlpha = ringAlpha,
            appearanceProgress = appearance.value,
            modifier = Modifier.fillMaxSize(),
        )

        Image(
            painter = painterResource(config.iconType.iconRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = heroCenterY - iconSize / 2f)
                .size(iconSize)
                .graphicsLayer {
                    alpha = heroAppear
                    scaleX = 0.92f + heroAppear * 0.08f
                    scaleY = 0.92f + heroAppear * 0.08f
                },
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = maxHeight * 0.535f)
                .padding(horizontal = 42.dp),
        ) {
            Text(
                text = title,
                color = White,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAppear
                    translationY = (1f - titleAppear) * 22f
                    scaleX = 0.98f + titleAppear * 0.02f
                    scaleY = 0.98f + titleAppear * 0.02f
                },
            )

            Text(
                text = buildAnnotatedString {
                    val start = body.indexOf(boldPart)
                    if (start >= 0) {
                        append(body.substring(0, start))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(boldPart) }
                        append(body.substring(start + boldPart.length))
                    } else {
                        append(body)
                    }
                },
                color = White.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 34.dp)
                    .graphicsLayer {
                        alpha = bodyAppear
                        translationY = (1f - bodyAppear) * 26f
                    },
            )
        }
    }
}

@Composable
private fun AboutBackgroundCanvas(
    color: Color,
    ringScale: Float,
    ringAlpha: Float,
    appearanceProgress: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val heroCenter = Offset(size.width * 0.5f, size.height * 0.35f)
        val unit = size.width / 402f
        val backgroundAppear = introStagger(appearanceProgress, start = 0.0f, end = 0.62f)
        val outerAppear = introStagger(appearanceProgress, start = 0.06f, end = 0.50f)
        val middleAppear = introStagger(appearanceProgress, start = 0.16f, end = 0.62f)
        val glowAppear = introStagger(appearanceProgress, start = 0.0f, end = 0.52f)
        val darkBackground = color.mix(Color.Black, 0.92f)
        val innerColor = darkBackground.mix(color.mix(Color.White, 0.18f), backgroundAppear)
        val outerColor = darkBackground.mix(color.mix(Color.Black, 0.82f), backgroundAppear)
        val gradientRadius = maxOf(
            size.width * (0.78f + backgroundAppear * 0.52f),
            size.height * (0.36f + backgroundAppear * 0.24f),
        )

        drawRect(color = darkBackground)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(innerColor, outerColor),
                center = heroCenter,
                radius = gradientRadius,
            ),
            alpha = 0.5f + backgroundAppear * 0.5f,
        )

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to color.copy(alpha = 0.24f * ringAlpha * glowAppear),
                    0.52f to color.copy(alpha = 0.13f * ringAlpha * glowAppear),
                    1.0f to Color.Transparent,
                ),
                center = heroCenter,
                radius = 150f * unit * (0.5f + glowAppear * 0.2f) * ringScale,
            ),
            radius = 150f * unit * (0.5f + glowAppear * 0.2f) * ringScale,
            center = heroCenter,
        )
        drawCircle(
            color = White.copy(alpha = (0.02f + 0.045f * ringAlpha) * outerAppear),
            radius = 100f * unit * ringScale,
            center = heroCenter,
        )
        drawCircle(
            color = White.copy(alpha = (0.03f + 0.095f * ringAlpha) * middleAppear),
            radius = 40f * unit * (0.98f + (ringScale - 1f) * 0.5f),
            center = heroCenter,
        )
    }
}

private fun Color.mix(other: Color, amount: Float): Color {
    val clamped = amount.coerceIn(0f, 1f)
    val inverse = 1f - clamped
    return Color(
        red = red * inverse + other.red * clamped,
        green = green * inverse + other.green * clamped,
        blue = blue * inverse + other.blue * clamped,
        alpha = alpha * inverse + other.alpha * clamped,
    )
}

private fun introStagger(progress: Float, start: Float, end: Float): Float {
    if (end <= start) return if (progress >= end) 1f else 0f
    val normalized = ((progress - start) / (end - start)).coerceIn(0f, 1f)
    return normalized * normalized * normalized * (normalized * (normalized * 6f - 15f) + 10f)
}
