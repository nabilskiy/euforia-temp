package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

data class AboutPageConfig(
    val titleRes: Int,
    val bodyRes: Int,
    val boldPartRes: Int,
    val centerColor: Color,
    val edgeColor: Color,
    val iconEmoji: String,
)

@Composable
fun AboutPage(
    config: AboutPageConfig,
    isPageActive: Boolean,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val title = localizedRes.string(config.titleRes)
    val body = localizedRes.string(config.bodyRes)
    val boldPart = localizedRes.string(config.boldPartRes)

    val infiniteTransition = rememberInfiniteTransition(label = "rings")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2800), RepeatMode.Reverse),
        label = "ringScale",
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(tween(2800), RepeatMode.Reverse),
        label = "ringAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        config.centerColor.copy(alpha = 0.78f),
                        config.centerColor.copy(alpha = 0.34f),
                        config.edgeColor,
                        Color.Black,
                    ),
                    radius = 980f,
                ),
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 130.dp),
        ) {
            Box(
                Modifier
                    .size(210.dp)
                    .scale(ringScale)
                    .alpha(ringAlpha * 0.55f)
                    .background(White.copy(alpha = 0.2f), CircleShape),
            )
            Box(
                Modifier
                    .size(94.dp)
                    .scale(ringScale * 0.98f)
                    .alpha(ringAlpha)
                    .background(White.copy(alpha = 0.18f), CircleShape),
            )
            Box(
                Modifier
                    .size(58.dp)
                    .background(White.copy(alpha = 0.48f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = config.iconEmoji, style = MaterialTheme.typography.titleLarge)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 42.dp)
                .padding(bottom = 172.dp),
        ) {
            Text(
                text = title,
                color = White,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                ),
                textAlign = TextAlign.Center,
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
                modifier = Modifier.padding(top = 34.dp),
            )
        }
    }
}
