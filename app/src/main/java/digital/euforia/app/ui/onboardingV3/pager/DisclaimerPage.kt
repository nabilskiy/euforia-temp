package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
            imageAppear.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
            )
        }
        launch {
            delay(120L)
            iconAppear.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
            )
        }
        launch {
            delay(180L)
            textAppear.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
            .padding(top = 96.dp, bottom = 142.dp),
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
                        .size(150.dp)
                        .graphicsLayer {
                            alpha = imageAppear.value
                            scaleX = 0.8f + imageAppear.value * 0.2f
                            scaleY = 0.8f + imageAppear.value * 0.2f
                            translationX = (1f - imageAppear.value) * -50f
                        }
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                )
                Image(
                    painter = painterResource(R.drawable.img_dictor_woman),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(150.dp)
                        .graphicsLayer {
                            alpha = imageAppear.value
                            scaleX = 0.8f + imageAppear.value * 0.2f
                            scaleY = 0.8f + imageAppear.value * 0.2f
                            translationX = (1f - imageAppear.value) * 50f
                        }
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 120.dp)
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
                            scaleX = 0.2f + iconAppear.value * 0.8f
                            scaleY = 0.2f + iconAppear.value * 0.8f
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
                .padding(top = 82.dp)
                .graphicsLayer {
                    alpha = textAppear.value
                    translationY = (1f - textAppear.value) * 24f
                },
        )

        IntroBoldText(
            text = localizedRes.string(R.string.intro_disclaimer_text),
            modifier = Modifier
                .padding(top = 50.dp)
                .graphicsLayer {
                    val appear = ((textAppear.value - 0.15f) / 0.85f).coerceIn(0f, 1f)
                    alpha = appear
                    translationY = (1f - appear) * 24f
                },
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
