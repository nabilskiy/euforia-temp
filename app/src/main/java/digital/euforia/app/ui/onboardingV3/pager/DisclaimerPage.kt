package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DisclaimerPage(isPageActive: Boolean) {
    if (!isPageActive) return

    val imageAppear = remember { Animatable(0f) }
    val iconAppear = remember { Animatable(0f) }
    val textAppear = remember { Animatable(0f) }
    val localizedRes = LocalLocalizedRes.current

    LaunchedEffect(Unit) {
        launch {
            delay(80L)
            imageAppear.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow),
            )
        }
        launch {
            delay(260L)
            iconAppear.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessLow),
            )
        }
        launch {
            delay(360L)
            textAppear.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 780, easing = FastOutSlowInEasing),
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val imageSize = (maxWidth - 58.dp) / 2f
        val topPadding = maxHeight * 0.135f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = topPadding, bottom = 142.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Row {
                Image(
                    painter = painterResource(R.drawable.img_dictor_man),
                    contentDescription = null,
                    modifier = Modifier
                        .size(imageSize)
                        .graphicsLayer {
                            alpha = imageAppear.value
                            scaleX = 0.86f + imageAppear.value * 0.14f
                            scaleY = 0.86f + imageAppear.value * 0.14f
                            translationX = (1f - imageAppear.value) * -70f
                            translationY = (1f - imageAppear.value) * 18f
                        }
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                )
                Image(
                    painter = painterResource(R.drawable.img_dictor_woman),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(imageSize)
                        .graphicsLayer {
                            alpha = imageAppear.value
                            scaleX = 0.86f + imageAppear.value * 0.14f
                            scaleY = 0.86f + imageAppear.value * 0.14f
                            translationX = (1f - imageAppear.value) * 70f
                            translationY = (1f - imageAppear.value) * 18f
                        }
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = imageSize - 30.dp)
                    .size(66.dp)
                    .background(Color(0xFF17191F), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_no_ai),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer {
                            alpha = iconAppear.value
                            scaleX = 0.35f + iconAppear.value * 0.65f
                            scaleY = 0.35f + iconAppear.value * 0.65f
                            translationY = (1f - iconAppear.value) * 12f
                        },
                )
            }
        }

        Text(
            text = localizedRes.string(R.string.intro_disclaimer_title),
            color = White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 25.sp,
                lineHeight = 37.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier
                .padding(top = 56.dp)
                .graphicsLayer {
                    alpha = textAppear.value
                    translationY = (1f - textAppear.value) * 30f
                },
        )

        IntroBoldText(
            text = localizedRes.string(R.string.intro_disclaimer_text),
            modifier = Modifier
                .padding(top = 34.dp)
                .padding(horizontal = 14.dp)
                .graphicsLayer {
                    val appear = ((textAppear.value - 0.12f) / 0.88f).coerceIn(0f, 1f)
                    alpha = appear
                    translationY = (1f - appear) * 32f
                },
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        }
    }
}
