package digital.euforia.app.ui.onboardingV3.pager

import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.view.LayoutInflater
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import digital.euforia.app.R
import digital.euforia.app.ui.onboardingV3.OnboardingV3ViewModel
import digital.euforia.app.ui.theme.Inter
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.MediaPlayerHelper
import digital.euforia.app.ui.util.rememberExoPlayer
import digital.euforia.app.ui.util.widget.AnimatedSizeButton
import digital.euforia.app.ui.util.widget.TermsAndPrivacyText
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

private const val BRAND_TEXT = "EUFORIA"

private data class IntroVoiceText(
    val text: String,
    @param:RawRes val voiceRes: Int,
)

@Composable
fun StartPage(
    viewModel: OnboardingV3ViewModel,
    isPageActive: Boolean,
) {
    if (!isPageActive) return

    val context = LocalContext.current
    val localizedRes = LocalLocalizedRes.current
    val introTexts = remember(localizedRes) {
        listOf(
            IntroVoiceText(
                text = localizedRes.string(R.string.intro_v3_start_item_1),
                voiceRes = R.raw.snd_intro_voice_1_female,
            ),
            IntroVoiceText(
                text = localizedRes.string(R.string.intro_v3_start_item_2),
                voiceRes = R.raw.snd_intro_voice_2_female,
            ),
            IntroVoiceText(
                text = localizedRes.string(R.string.intro_v3_start_item_3),
                voiceRes = R.raw.snd_intro_voice_3_female,
            ),
        )
    }
    val lastText = remember(localizedRes) {
        IntroVoiceText(
            text = localizedRes.string(R.string.intro_v3_start_item_last),
            voiceRes = R.raw.snd_intro_start_item_last_female,
        )
    }

    val blurAnim = rememberIntroBlurTextAnimState()
    var displayText by remember { mutableStateOf("") }
    var isLargeText by remember { mutableStateOf(true) }
    var showsBloom by remember { mutableStateOf(false) }
    var isBrandVisible by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) }

    val bloomProgress = remember { Animatable(0f) }
    val brandProgress = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(0f) }

    val mainFontSize = if (isLargeText) 35.sp else 20.sp

    LaunchedEffect(isPageActive) {
        if (!isPageActive) return@LaunchedEffect

        displayText = ""
        isLargeText = true
        showsBloom = false
        isBrandVisible = false
        showControls = false
        blurAnim.alpha.snapTo(1f)
        blurAnim.scale.snapTo(1f)
        blurAnim.blurRadius.snapTo(0f)
        bloomProgress.snapTo(0f)
        brandProgress.snapTo(0f)
        contentOffsetY.snapTo(0f)

        for (item in introTexts) {
            if (!isActive) return@LaunchedEffect
            showIntroVoiceText(
                context = context,
                animState = blurAnim,
                item = item,
                onTextChanged = { displayText = it },
            )
        }

        if (!isActive) return@LaunchedEffect
        showIntroText(blurAnim, text = "", onTextChanged = { displayText = it }, holdAfterShow = false)

        showsBloom = true
        isBrandVisible = true
        coroutineScope {
            launch {
                bloomProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1_000, easing = LinearOutSlowInEasing),
                )
            }
            launch {
                brandProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = INTRO_BRAND_REVEAL_MS, easing = LinearEasing),
                )
            }
        }

        isLargeText = false
        delay(INTRO_BEFORE_LAST_TEXT_MS.toLong())

        if (!isActive) return@LaunchedEffect
        showIntroVoiceText(
            context = context,
            animState = blurAnim,
            item = lastText,
            onTextChanged = { displayText = it },
        )

        showControls = true
        contentOffsetY.animateTo(
            targetValue = -28f,
            animationSpec = tween(INTRO_SHOW_BUTTONS_MS, easing = LinearOutSlowInEasing),
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            MediaPlayerHelper.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF171724),
                        Color(0xFF111827),
                        Color(0xFF0D1320),
                    ),
                ),
            ),
    ) {
        StartIosLikeBackground()

        if (!showsBloom && !isBrandVisible) {
            IntroBlurText(
                text = displayText,
                animState = blurAnim,
                fontSize = mainFontSize,
                lineHeight = mainFontSize * 1.12f,
                maxLines = if (isLargeText) 3 else 2,
                modifier = Modifier
                    .align(Alignment.Center),
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = contentOffsetY.value.dp)
                    .padding(top = 128.dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showsBloom) {
                        StartBloomVideo(progress = bloomProgress.value)
                    }
                }

                if (isBrandVisible) {
                    IntroBrandText(
                        progress = brandProgress.value,
                        modifier = Modifier.height(60.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                IntroBlurText(
                    text = displayText,
                    animState = blurAnim,
                    fontSize = mainFontSize,
                    lineHeight = mainFontSize * 1.12f,
                    maxLines = if (isLargeText) 3 else 2,
                )
            }
        }

        AnimatedVisibility(
            visible = showControls,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(INTRO_SHOW_BUTTONS_MS)) + slideInVertically(
                animationSpec = tween(INTRO_SHOW_BUTTONS_MS, easing = LinearOutSlowInEasing),
                initialOffsetY = { it / 2 },
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedSizeButton(
                    text = localizedRes.string(R.string.intro_v3_start_button),
                    isEnabled = true,
                    isVisible = true,
                    onClick = { viewModel.onStartFlowCompleted() },
                )
                TermsAndPrivacyText(
                    modifier = Modifier.padding(top = 20.dp),
                    onPrivacyClick = viewModel::onPrivacyClicked,
                    onTermsClick = viewModel::onTermsClicked,
                )
            }
        }
    }
}

@Composable
private fun IntroBrandText(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BRAND_TEXT.forEachIndexed { index, letter ->
            val letterProgress = ((progress * BRAND_TEXT.length) - index).coerceIn(0f, 1f)
            Text(
                text = letter.toString(),
                color = White,
                fontSize = 54.sp,
                fontFamily = Inter,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = letterProgress
                        scaleX = 0.8f + letterProgress * 0.2f
                        scaleY = 0.8f + letterProgress * 0.2f
                    }
                    .blur(((1f - letterProgress) * 20f).dp),
            )
        }
    }
}

private suspend fun showIntroVoiceText(
    context: Context,
    animState: IntroBlurTextAnimState,
    item: IntroVoiceText,
    onTextChanged: (String) -> Unit,
) {
    animateIntroTextBlurOut(animState)
    onTextChanged(item.text)

    coroutineScope {
        val textAppearJob = launch {
            animateIntroTextBlurIn(animState)
        }
        val voiceJob = launch {
            delay((INTRO_TEXT_TRANSITION_MS * 0.75f).toLong())
            playIntroVoice(context, item.voiceRes)
        }

        textAppearJob.join()
        voiceJob.join()
    }
}

private suspend fun playIntroVoice(
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

@Composable
private fun StartIosLikeBackground() {
    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Matches iOS IntroStartViewController: CirclesView(colors purple/blue, alpha 0.1)
        // plus a subtle full-screen GlassView-like vignette.
        val firstCenter = Offset(x = size.width * 0.20f, y = size.height * 0.36f)
        val firstRadius = size.maxDimension * 0.55f
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF9933A2).copy(alpha = 0.10f),
                    0.45f to Color(0xFF9933A2).copy(alpha = 0.045f),
                    1.0f to Color.Transparent,
                ),
                center = firstCenter,
                radius = firstRadius,
            ),
            radius = firstRadius,
            center = firstCenter,
            blendMode = BlendMode.Screen,
        )
        val secondCenter = Offset(x = size.width * 0.78f, y = size.height * 0.52f)
        val secondRadius = size.maxDimension * 0.58f
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF0C42AE).copy(alpha = 0.10f),
                    0.48f to Color(0xFF0C42AE).copy(alpha = 0.045f),
                    1.0f to Color.Transparent,
                ),
                center = secondCenter,
                radius = secondRadius,
            ),
            radius = secondRadius,
            center = secondCenter,
            blendMode = BlendMode.Screen,
        )
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.018f),
                    0.34f to Color.Transparent,
                    0.72f to Color.Black.copy(alpha = 0.035f),
                    1.0f to Color.Black.copy(alpha = 0.08f),
                ),
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.66f to Color.Transparent,
                    1.0f to Color.Black.copy(alpha = 0.18f),
                ),
                center = Offset(size.width / 2f, size.height * 0.45f),
                radius = size.maxDimension * 0.72f,
            ),
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun StartBloomVideo(progress: Float) {
    val breath = rememberBreathPhase()
    val bloomScale = (0.14f + progress * 0.86f) * (0.948f + breath * 0.102f)
    val bloomAlpha = ((progress - 0.08f) / 0.92f).coerceIn(0f, 1f)
    val bloomBlur = (1f - progress) * 6f
    val exoPlayer = rememberExoPlayer(videoRes = R.raw.vid_bloom).apply {
        volume = 0f
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .scale(bloomScale)
            .alpha(bloomAlpha)
            .blur(bloomBlur.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { ctx ->
                LayoutInflater.from(ctx).inflate(R.layout.player_view_texture, null, false).also { root ->
                    root.findViewById<PlayerView>(R.id.player_view).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShutterBackgroundColor(TRANSPARENT)
                        setBackgroundColor(TRANSPARENT)
                    }
                    root.setBackgroundColor(TRANSPARENT)
                }
            },
            update = { root ->
                root.findViewById<PlayerView>(R.id.player_view).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShutterBackgroundColor(TRANSPARENT)
                    setBackgroundColor(TRANSPARENT)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(maxWidth * 0.6f)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                    blendMode = BlendMode.Screen
                }
                .softBloomVideoMask(progress),
        )
    }
}

private fun Modifier.softBloomVideoMask(progress: Float): Modifier {
    return graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            val revealProgress = ((progress - 0.08f) / 0.92f).coerceIn(0f, 1f)
            val maskRadius = size.width * (0.18f + revealProgress * 0.24f)
            drawRect(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color.White,
                        0.46f to Color.White,
                        0.72f to Color.White.copy(alpha = 0.45f),
                        1.0f to Color.Transparent,
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = maskRadius,
                ),
                blendMode = BlendMode.DstIn,
            )
        }
}

@Composable
private fun rememberBreathPhase(): Float {
    var phase by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = System.currentTimeMillis()
        while (isActive) {
            val elapsed = (System.currentTimeMillis() - start) / 1000.0
            val wave = (kotlin.math.sin(elapsed * Math.PI / 1.8) + 1.0) * 0.5
            phase = wave.toFloat()
            delay(33)
        }
    }
    return phase
}
