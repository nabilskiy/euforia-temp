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
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import digital.euforia.app.ui.theme.SoundscapeColors
import digital.euforia.app.ui.theme.eveningColors
import digital.euforia.app.R
import digital.euforia.app.domain.model.config.BannerConfig
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.MaxGradientReversed
import digital.euforia.app.ui.theme.SecondaryText
import digital.euforia.app.ui.theme.SoundscapesButtonBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.AnimatedSizeBox
import digital.euforia.app.ui.util.widget.clickableSingle

fun LazyListScope.soundscapesItem(
    isDemo: Boolean,
    isPremium: Boolean,
    bannerConfig: BannerConfig?,
    onSoundscapesClick: () -> Unit,
    onBannerClick: () -> Unit,
    onFAQClick: () -> Unit,
    onSupportClick: () -> Unit,
    onSOSClick: () -> Unit
) = item(key = PlanViewItems.SOUNDSCAPES, contentType = PlanViewItems.SOUNDSCAPES) {
    Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = 600.dp).triRadialGradient(
            center1 = Offset(200f, 500f),
            center2 = Offset(900f, 700f),
            center3 = Offset(500f, 1500f),
            radius1Px = 400f,
            radius2Px = 500f,
            radius3Px = 800f,
            color1 = eveningColors[0],
            color2 = eveningColors[1],
            color3 = eveningColors[2],
        )
    ) {
        Column {
            SoundscapesContent(onSoundscapesClick)
            PremiumBannerContent(
                isPremium = isPremium,
                bannerConfig = bannerConfig,
                onClick = onBannerClick
            )
            FooterButtons(
                onFAQClick = onFAQClick,
                onSupportClick = onSupportClick,
                onSOSClick = onSOSClick
            )
        }
    }
}

@Composable
private fun SoundscapesContent(onSoundscapesClick: () -> Unit) {
    val localizedRes = LocalLocalizedRes.current
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
                text = localizedRes.string(R.string.today_info_step_scenes_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
                color = White,
                textAlign = TextAlign.Start,
            )

            Text(
                text = localizedRes.string(R.string.today_info_step_scenes_text),
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                textAlign = TextAlign.Start,
            )
            SoundscapesButton(onSoundscapesClick)
        }
    }
}

@Composable
fun ColumnScope.FooterButtons(
    onFAQClick: () -> Unit,
    onSupportClick: () -> Unit,
    onSOSClick: () -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    Text(
        modifier = Modifier.padding(top = 48.dp, bottom = 24.dp).fillMaxWidth(),
        text = localizedRes.string(R.string.today_footer),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
        color = White,
        textAlign = TextAlign.Center,
    )
    FooterButton(
        textRes = R.string.today_question_button,
        onClick = onFAQClick
    )
    FooterButton(
        textRes = R.string.today_support_button,
        onClick = onSupportClick
    )
    FooterButton(
        textRes = R.string.today_sos_button,
        containerColor = White,
        textColor = Black,
        onClick = onSOSClick
    )
}

@Composable
fun ColumnScope.FooterButton(
    textRes: Int,
    containerColor: Color = White.copy(alpha = 0.1f),
    textColor: Color = White,
    onClick: () -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    AnimatedSizeBox(
        modifier = Modifier.padding(horizontal = 64.dp, vertical = 8.dp),
        pressedScale = 1.02f,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(color = containerColor, shape = CircleShape)
                .height(48.dp),
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = localizedRes.string(textRes),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                maxLines = 3,
                overflow = Ellipsis,
                color = textColor
            )
        }
    }
}

@Composable
fun PremiumBannerContent(isPremium: Boolean, bannerConfig: BannerConfig?, onClick: () -> Unit) {
    if (!isPremium && bannerConfig != null) {
        val localizedRes = LocalLocalizedRes.current
        val fontSize = bannerConfig.style.subtitleSize.sp
        Box(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 164.dp, end = 16.dp)
                .clickableSingle(onClick = onClick)
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                model = bannerConfig.imgUrl,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(4.dp)
            ) {
                Text(
                    text = bannerConfig.subtitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = fontSize
                    ),
                    color = White,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = localizedRes.string(R.string.plan_banner_max),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = fontSize,
                        brush = MaxGradientReversed
                    ),
                    textAlign = TextAlign.Center,
                )
            }
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
                0.0f to base.copy(alpha = 0.6f),
                1.0f to base.copy(alpha = 0f)
            ),
            center = center,
            radius = radius
        )

        val blendMode = BlendMode.ColorDodge

        onDrawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to White.copy(alpha = 0f),
                        0.5f to White.copy(alpha = 0.1f),
                        1.0f to White.copy(alpha = 0f)
                    ),
                )
            )
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
    val localizedRes = LocalLocalizedRes.current
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
            text = localizedRes.string(R.string.today_info_step_scenes_button),
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
