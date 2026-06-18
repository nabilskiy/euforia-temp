package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
fun SocialProofPage(isPageActive: Boolean) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val appear = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(700))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 126.dp, bottom = 128.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = localizedRes.string(R.string.intro_social_proof_step_title),
            color = White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 25.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.introProofAppear(appear.value, 0f),
        )
        Text(
            text = localizedRes.string(R.string.intro_social_proof_step_text),
            color = White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 19.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .introProofAppear(appear.value, 0.15f),
        )
        SocialProofCard(
            modifier = Modifier
                .padding(top = 18.dp)
                .introProofAppear(appear.value, 0.3f),
        )
    }
}

@Composable
private fun SocialProofCard(modifier: Modifier = Modifier) {
    val localizedRes = LocalLocalizedRes.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1F2228), RoundedCornerShape(25.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.img_woman_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) {
                        Text(
                            text = "★",
                            color = Color(0xFFFFD214),
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 32.sp),
                        )
                    }
                }
                Text(
                    text = localizedRes.string(R.string.intro_social_proof_step_user_name),
                    color = White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                )
            }
        }
        IntroBoldText(
            text = localizedRes.string(R.string.intro_social_proof_step_comment),
            color = White.copy(alpha = 0.7f),
            boldColor = White,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 20.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

private fun Modifier.introProofAppear(progress: Float, delayFraction: Float): Modifier {
    val value = ((progress - delayFraction) / (1f - delayFraction)).coerceIn(0f, 1f)
    return graphicsLayer {
        alpha = value
        translationY = (1f - value) * 22f
        scaleX = 0.98f + value * 0.02f
        scaleY = 0.98f + value * 0.02f
    }
}
