package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.PlayButtonBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.HeadphonesInfoView
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.vibe.AnimationType
import digital.euforia.app.ui.util.widget.vibe.PlayState
import digital.euforia.app.ui.util.widget.vibe.PlaybackAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class LoadingV3Phase {
    Preparing,
    Ready,
}

@Composable
fun LoadingV3Page(
    isPageActive: Boolean,
    onFinished: () -> Unit,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    var phase by remember { mutableStateOf(LoadingV3Phase.Preparing) }
    val contentAppear = remember { Animatable(0f) }
    val playState = if (phase == LoadingV3Phase.Preparing) PlayState.LOADING else PlayState.LOADED
    val animationPadding by animateDpAsState(
        targetValue = if (phase == LoadingV3Phase.Ready) 0.dp else 180.dp,
        animationSpec = tween(durationMillis = 2_000),
        label = "loadingV3AnimationPadding",
    )

    LaunchedEffect(Unit) {
        launch {
            delay(500)
            contentAppear.animateTo(1f, tween(durationMillis = 800))
        }
        delay(6_000)
        phase = LoadingV3Phase.Ready
        delay(2_000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10141F)),
    ) {
        PlaybackAnimation(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = contentAppear.value },
            animationType = AnimationType.SPHERES,
            state = playState,
            paddingState = animationPadding,
            contentSize = 360.dp,
            colors = listOf(
                Color(0xFF073CFF),
                Color(0xFFFF20D8),
                Color(0xFF00B9FF),
            ),
        )

        LoadingTitleBlock(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 32.dp)
                .padding(top = 68.dp)
                .graphicsLayer {
                    alpha = contentAppear.value
                    translationY = (1f - contentAppear.value) * -100f
                },
            title = if (phase == LoadingV3Phase.Preparing) {
                localizedRes.string(R.string.intro_final_title)
            } else {
                localizedRes.string(R.string.intro_final_title_ready)
            },
            subtitle = if (phase == LoadingV3Phase.Preparing) {
                localizedRes.string(R.string.intro_final_subtitle)
            } else {
                null
            },
        )

        LoadingCenterButton(
            phase = phase,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { alpha = contentAppear.value },
        )

        HeadphonesInfoView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = contentAppear.value
                    translationY = (1f - contentAppear.value) * 100f
                },
        )
    }
}

@Composable
private fun LoadingTitleBlock(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AnimatedContent(
            targetState = title,
            transitionSpec = {
                fadeIn(tween(durationMillis = 300)) togetherWith fadeOut(tween(durationMillis = 300))
            },
            label = "loadingV3Title",
        ) { titleText ->
            Text(
                text = titleText,
                color = White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 25.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        AnimatedVisibility(
            visible = subtitle != null,
            enter = fadeIn(tween(durationMillis = 300)),
            exit = fadeOut(tween(durationMillis = 300)),
        ) {
            Text(
                text = subtitle.orEmpty(),
                color = White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@Composable
private fun LoadingCenterButton(
    phase: LoadingV3Phase,
    modifier: Modifier = Modifier,
) {
    val buttonColor by animateColorAsState(
        targetValue = if (phase == LoadingV3Phase.Ready) Color(0xFFADA1FF) else PlayButtonBackground,
        animationSpec = tween(durationMillis = 600),
        label = "loadingV3ButtonColor",
    )
    val checkBounce by animateFloatAsState(
        targetValue = if (phase == LoadingV3Phase.Ready) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "loadingV3CheckBounce",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "loadingV3Glow")
    val glowPulse = infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "loadingV3GlowPulse",
    ).value

    Box(
        modifier = modifier.size(132.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(92.dp * glowPulse)
                .blur(18.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFB123FF).copy(alpha = 0.75f),
                            Color(0xFF073CFF).copy(alpha = 0.45f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(buttonColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = {
                    fadeIn(tween(durationMillis = 180)) togetherWith fadeOut(tween(durationMillis = 120))
                },
                label = "loadingV3CenterIcon",
            ) { currentPhase ->
                if (currentPhase == LoadingV3Phase.Preparing) {
                    ProgressIndicator()
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_done),
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer {
                                val bounceScale = if (checkBounce < 0.5f) {
                                    1f + checkBounce * 0.4f
                                } else {
                                    1.2f - (checkBounce - 0.5f) * 0.4f
                                }
                                scaleX = bounceScale
                                scaleY = bounceScale
                            },
                    )
                }
            }
        }
    }
}
