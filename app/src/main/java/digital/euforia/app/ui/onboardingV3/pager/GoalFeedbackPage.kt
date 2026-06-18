package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants.IterateForever
import com.airbnb.lottie.compose.rememberLottieComposition
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.MediaPlayerHelper

@Composable
fun GoalFeedbackPage(
    selectedGoal: Goal?,
    isPageActive: Boolean,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val context = LocalContext.current
    val title = localizedRes.string(R.string.intro_v3_goals_feedback_title)
    val feedback = selectedGoal?.feedback.orEmpty()
    val appearance = remember(selectedGoal?.identifier) { Animatable(0f) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.not_alone_animation))

    LaunchedEffect(selectedGoal?.identifier) {
        appearance.snapTo(0f)
        appearance.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1_000, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(selectedGoal?.identifier) {
        selectedGoal?.identifier?.goalVoiceRes()?.let { voiceRes ->
            MediaPlayerHelper.play(context, voiceRes)
        }
    }

    DisposableEffect(Unit) {
        onDispose { MediaPlayerHelper.release() }
    }

    val titleAppear = introStagger(appearance.value, start = 0.0f, end = 0.55f)
    val imageAppear = introStagger(appearance.value, start = 0.10f, end = 0.70f)
    val textAppear = introStagger(appearance.value, start = 0.20f, end = 0.85f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
    ) {
        val lottieSize = (maxWidth * 0.86f).coerceAtMost(292.dp)
        val titleTop = maxHeight * 0.13f
        val lottieTop = maxHeight * 0.29f
        val textTop = maxHeight * 0.66f

        Text(
            text = title,
            color = White,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = titleTop)
                .graphicsLayer {
                    alpha = titleAppear
                    translationY = (1f - titleAppear) * 18f
                },
        )

        LottieAnimation(
            composition = composition,
            iterations = IterateForever,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = lottieTop)
                .size(lottieSize)
                .graphicsLayer {
                    alpha = imageAppear
                    scaleX = 0.96f + imageAppear * 0.04f
                    scaleY = 0.96f + imageAppear * 0.04f
                },
            contentScale = ContentScale.Fit,
        )

        IntroBoldText(
            text = feedback,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = textTop)
                .graphicsLayer {
                    alpha = textAppear
                    translationY = (1f - textAppear) * 24f
                },
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 20.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

private fun introStagger(progress: Float, start: Float, end: Float): Float {
    if (end <= start) return if (progress >= end) 1f else 0f
    val normalized = ((progress - start) / (end - start)).coerceIn(0f, 1f)
    return normalized * normalized * normalized * (normalized * (normalized * 6f - 15f) + 10f)
}

private fun String.goalVoiceRes(): Int? = when (this) {
    "sleep" -> R.raw.snd_intro_goals_sleep_female
    "esteem" -> R.raw.snd_intro_goals_esteem_female
    "warmth" -> R.raw.snd_intro_goals_warmth_female
    "inspired" -> R.raw.snd_intro_goals_inspired_female
    "emotional" -> R.raw.snd_intro_goals_emotional_female
    "relax" -> R.raw.snd_intro_goals_relax_female
    "loneliness" -> R.raw.snd_intro_goals_loneliness_female
    "other" -> R.raw.snd_intro_goals_other_female
    "confidence" -> R.raw.snd_intro_goals_confidence_female
    "toxic" -> R.raw.snd_intro_goals_toxic_female
    else -> null
}
