package digital.euforia.app.ui.onboardingV3.pager

import android.content.Context
import androidx.annotation.RawRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.MediaPlayerHelper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val KEEP_EXPLORING_TEXT_TRANSITION_MS = 1_000
private const val KEEP_EXPLORING_TEXT_HOLD_NO_VOICE_MS = 2_600

private data class KeepExploringVoiceText(
    val text: String,
    @param:RawRes val voiceRes: Int? = null,
)

@Composable
fun KeepExploringV3Page(
    isPageActive: Boolean,
    onFinished: () -> Unit,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val context = LocalContext.current
    val anim = remember { AnimatedTextState() }
    var text by remember { mutableStateOf("") }

    LaunchedEffect(isPageActive) {
        if (!isPageActive) return@LaunchedEffect

        anim.snapToVisible()
        text = ""
        showIntroSequenceText(
            context = context,
            anim = anim,
            item = KeepExploringVoiceText(
                text = localizedRes.string(R.string.intro_rate_thank_you),
                voiceRes = R.raw.snd_intro_rate_thank_1_female,
            ),
            onTextChanged = { text = it },
        )
        showIntroSequenceText(
            context = context,
            anim = anim,
            item = KeepExploringVoiceText(
                text = localizedRes.string(R.string.intro_keep_exploring_title),
                voiceRes = R.raw.snd_intro_rate_thank_2_female,
            ),
            onTextChanged = { text = it },
        )
        showIntroSequenceText(
            context = context,
            anim = anim,
            item = KeepExploringVoiceText(text = ""),
            waitAfterShow = false,
            onTextChanged = { text = it },
        )
        onFinished()
    }

    DisposableEffect(Unit) {
        onDispose { MediaPlayerHelper.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18191D))
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 38.sp,
                lineHeight = 49.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier
                .graphicsLayer {
                    alpha = anim.alpha.value
                    scaleX = anim.scale.value
                    scaleY = anim.scale.value
                }
                .blur(anim.blur.value.dp),
        )
    }
}

private class AnimatedTextState {
    val alpha = Animatable(1f)
    val scale = Animatable(1f)
    val blur = Animatable(0f)

    suspend fun snapToVisible() {
        alpha.snapTo(1f)
        scale.snapTo(1f)
        blur.snapTo(0f)
    }

    suspend fun blurOut() {
        kotlinx.coroutines.coroutineScope {
            launch {
                alpha.animateTo(0f, tween(KEEP_EXPLORING_TEXT_TRANSITION_MS, easing = LinearOutSlowInEasing))
            }
            launch {
                scale.animateTo(0.8f, tween(KEEP_EXPLORING_TEXT_TRANSITION_MS, easing = LinearOutSlowInEasing))
            }
            launch {
                blur.animateTo(20f, tween(KEEP_EXPLORING_TEXT_TRANSITION_MS, easing = LinearOutSlowInEasing))
            }
        }
    }

    suspend fun blurIn() {
        alpha.snapTo(0f)
        scale.snapTo(0.8f)
        blur.snapTo(20f)
        kotlinx.coroutines.coroutineScope {
            launch {
                alpha.animateTo(1f, tween(KEEP_EXPLORING_TEXT_TRANSITION_MS, easing = LinearOutSlowInEasing))
            }
            launch {
                scale.animateTo(1f, tween(KEEP_EXPLORING_TEXT_TRANSITION_MS, easing = LinearOutSlowInEasing))
            }
            launch {
                blur.animateTo(0f, tween(KEEP_EXPLORING_TEXT_TRANSITION_MS, easing = LinearOutSlowInEasing))
            }
        }
    }
}

private suspend fun showIntroSequenceText(
    context: Context,
    anim: AnimatedTextState,
    item: KeepExploringVoiceText,
    waitAfterShow: Boolean = true,
    onTextChanged: (String) -> Unit,
) {
    anim.blurOut()
    onTextChanged(item.text)

    if (item.voiceRes != null) {
        coroutineScope {
            val textAppearJob = launch { anim.blurIn() }
            val voiceJob = launch {
                delay((KEEP_EXPLORING_TEXT_TRANSITION_MS * 0.75f).toLong())
                playKeepExploringVoice(context, item.voiceRes)
            }
            textAppearJob.join()
            voiceJob.join()
        }
    } else {
        anim.blurIn()
    }

    if (waitAfterShow && item.text.isNotEmpty() && item.voiceRes == null) {
        delay(KEEP_EXPLORING_TEXT_HOLD_NO_VOICE_MS.toLong())
    }
}

private suspend fun playKeepExploringVoice(
    context: Context,
    @RawRes voiceRes: Int,
) {
    suspendCancellableCoroutine { continuation ->
        MediaPlayerHelper.play(context, voiceRes) {
            if (continuation.isActive) {
                continuation.resume(Unit)
            }
        }
        continuation.invokeOnCancellation {
            MediaPlayerHelper.release()
        }
    }
}
