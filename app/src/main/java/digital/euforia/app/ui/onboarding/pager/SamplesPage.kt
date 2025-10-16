package digital.euforia.app.ui.onboarding.pager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.onboarding.OnboardingViewModel
import digital.euforia.app.ui.onboarding.SamplePlaybackState
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.LocalizedResources
import digital.euforia.app.ui.util.widget.AudioEqualizerView
import digital.euforia.app.ui.util.widget.applyIf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SamplesPage(
    viewModel: OnboardingViewModel,
    playbackState: SamplePlaybackState,
    isPageOpened: () -> Boolean
) {
    SamplesPageContent(
        playbackState = playbackState,
        onSampleSelected = viewModel::onSampleSelected,
        isPageOpened = isPageOpened
    )
}

@Composable
private fun SamplesPageContent(
    playbackState: SamplePlaybackState,
    onSampleSelected: (Int) -> Unit,
    isPageOpened: () -> Boolean
) {
    val isOpened = isPageOpened()
    var isVisible by remember { mutableStateOf(false) }
    var animatedSize = animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "animetedSize"
    )
    LaunchedEffect(isOpened) {
//        isOpened = isPageOpened()
        // Select the first sample by default
        if (isOpened) {
            isVisible = true
//            onSampleSelected(audioResList[0])
        } else {
            isVisible = false
        }
    }

//    Box(modifier = Modifier.fillMaxSize()) {
//        AnimatedVisibility(
//            visible = isVisible,
//            modifier = Modifier.fillMaxSize(),
//            enter = slideInVertically(animationSpec = tween(500)) { fullHeight -> fullHeight }/* + expandIn(
//            animationSpec = tween(durationMillis = 3000)
//        )*/
//        ) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.fillMaxWidth().weight(0.25f))
        Column(
            modifier = Modifier.fillMaxWidth().weight(0.75f)/*.scale(animatedSize.value)*/
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val localizedRes = LocalLocalizedRes.current

            //            TitleText(isVisible, localizedRes)
            AnimatedTextAdvanced(
                text = localizedRes.string(R.string.intro_scenes_step_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = White),
                isVisible = isVisible,
                delay = 150
            )
            AnimatedTextAdvanced(
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
                text = localizedRes.string(R.string.intro_scenes_step_text),
                style = MaterialTheme.typography.bodyMedium.copy(color = White.copy(alpha = 0.4f)),
                isVisible = isVisible,
                delay = 300
            )
            //            SubtitleText(isVisible, localizedRes)
            SamplesView(450, isVisible, playbackState)
        }
    }
//        }
//    }
}

@Composable
private fun TitleText(isVisible: Boolean, localizedRes: LocalizedResources) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(animationSpec = tween(500)) { fullHeight -> fullHeight }
    ) {
        Text(
            modifier = Modifier.padding(top = 10.dp),
            color = Color.White,
            text = localizedRes.string(R.string.intro_scenes_step_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SubtitleText(isVisible: Boolean, localizedRes: LocalizedResources) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(animationSpec = tween(500)) { fullHeight -> fullHeight }
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
            color = Color.White.copy(alpha = 0.4f),
            text = localizedRes.string(R.string.intro_scenes_step_text),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SamplesView(
    delay: Long = 600,
    isVisible: Boolean,
    playbackState: SamplePlaybackState
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.9f) }
    val offsetY = remember { Animatable(80f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isVisible) {
        if (isVisible) {
            scope.launch {
                kotlinx.coroutines.delay(delay)

                // Паралельна анімація всіх властивостей
                scope.launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 555,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                scope.launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 555,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                scope.launch {
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 555,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        }
    }

    Row(

        modifier = Modifier
            .alpha(alpha.value)
            .scale(scale.value)
            .padding(horizontal = 0.dp, vertical = offsetY.value.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        imagesList.forEachIndexed { index, imageRes ->
            AnimatedSampleImage(
                isSelected = index == playbackState.currentSampleIndex,
                imageRes = imageRes
            )
        }
    }
}

@Composable
private fun AnimatedSampleImage(
    isSelected: Boolean,
    imageRes: Int,
) {
    val padding by animateFloatAsState(
        targetValue = if (isSelected) 0f else 8f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "padding"
    )

    Box(
        modifier = Modifier.padding(horizontal = 6.dp).size(86.dp).padding(padding.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .size(80.dp)
                .applyIf(isSelected) {
                    border(
                        color = White,
                        width = if (isSelected) 2.dp else 0.dp,
                        shape = RoundedCornerShape(12.dp)
                    )
                },
            contentScale = ContentScale.Crop
        )
//        }

        // Audio equalizer that appears above the image when animated
        if (isSelected) {
            AudioEqualizerView(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
            )
        }
    }
}

@Composable
fun AnimatedTextAdvanced(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle,
    isVisible: Boolean,
    delay: Long = 0
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.9f) }
    val offsetY = remember { Animatable(80f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isVisible) {
        if (isVisible) {
            scope.launch {
                delay(delay)

                // Паралельна анімація всіх властивостей
                scope.launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 555,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                scope.launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 555,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                scope.launch {
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 555,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        }
    }

    Text(
        modifier = modifier
            .alpha(alpha.value)
            .scale(scale.value)
            .padding(horizontal = 0.dp, vertical = offsetY.value.dp),
        text = text,
        style = style,
        textAlign = TextAlign.Center,
        color = White
    )
}

private val imagesList: List<Int> = listOf(
    R.drawable.img_intro_scene_preview_1,
    R.drawable.img_intro_scene_preview_2,
    R.drawable.img_intro_scene_preview_3
)

private val audioResList: List<Int> = listOf(
    R.raw.bgm_intro_scene_1,
    R.raw.bgm_intro_scene_2,
    R.raw.bgm_intro_scene_3
)
