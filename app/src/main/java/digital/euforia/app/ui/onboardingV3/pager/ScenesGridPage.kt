package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.IntroAnswerItem
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.AnimatedSizeBox
import digital.euforia.app.ui.util.widget.fadeBottom
import kotlinx.coroutines.delay

@Composable
fun ScenesGridPage(
    scenes: List<IntroAnswerItem>,
    selectedScenes: List<IntroAnswerItem>,
    onSceneSelected: (IntroAnswerItem) -> Unit,
    topPadding: Dp,
    isPageActive: Boolean,
) {
    if (!isPageActive) return

    val indexedScenes = scenes.mapIndexed { index, scene -> index to scene }
    val leftColumnScenes = indexedScenes.filter { (index, _) -> index % 2 == 0 }
    val rightColumnScenes = indexedScenes.filter { (index, _) -> index % 2 == 1 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fadeBottom(),
        contentPadding = PaddingValues(
            start = 28.dp,
            top = topPadding,
            end = 28.dp,
            bottom = 150.dp,
        ),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(30.dp),
                ) {
                    leftColumnScenes.forEach { (index, scene) ->
                        val isSelected = selectedScenes.any { it.identifier == scene.identifier }
                        val isDisabled = selectedScenes.size >= MAX_SELECTED_SCENES && !isSelected
                        SceneCard(
                            index = index,
                            scene = scene,
                            isSelected = isSelected,
                            isDisabled = isDisabled,
                            onClick = if (isDisabled) null else ({ onSceneSelected(scene) }),
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(30.dp),
                ) {
                    rightColumnScenes.forEach { (index, scene) ->
                        val isSelected = selectedScenes.any { it.identifier == scene.identifier }
                        val isDisabled = selectedScenes.size >= MAX_SELECTED_SCENES && !isSelected
                        SceneCard(
                            index = index,
                            scene = scene,
                            isSelected = isSelected,
                            isDisabled = isDisabled,
                            onClick = if (isDisabled) null else ({ onSceneSelected(scene) }),
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.fillMaxWidth().navigationBarsPadding())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SceneCard(
    index: Int,
    scene: IntroAnswerItem,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(20.dp)
    val appear = remember(scene.identifier) { Animatable(0f) }
    val startOffsetY = with(LocalDensity.current) { 34.dp.toPx() }

    LaunchedEffect(scene.identifier) {
        appear.snapTo(0f)
        delay(index * 44L)
        appear.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 620,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    AnimatedSizeBox(onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = appear.value * if (isDisabled) 0.35f else 1f
                    translationY = (1f - appear.value) * startOffsetY
                    scaleX = 0.96f + appear.value * 0.04f
                    scaleY = 0.96f + appear.value * 0.04f
                }
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 340,
                        easing = FastOutSlowInEasing,
                    ),
                )
                .background(White.copy(alpha = 0.04f), shape)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, White, shape)
                    } else {
                        Modifier
                    },
                )
                .padding(10.dp)
                .padding(bottom = 5.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                val imageRes = scene.imageUrl?.drawableResourceId() ?: 0
                if (imageRes != 0) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                SelectionBadge(
                    isSelected = isSelected,
                    isDisabled = isDisabled,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                )
            }

            Text(
                text = scene.text,
                color = White,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically(
                    animationSpec = tween(340, easing = FastOutSlowInEasing),
                ) + fadeIn(tween(220, delayMillis = 70)),
                exit = shrinkVertically(
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                ) + fadeOut(tween(160)),
            ) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    scene.subtitle.orEmpty()
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .forEach { tag ->
                            Text(
                                text = tag.uppercase(),
                                color = PrimaryButtonText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                modifier = Modifier
                                    .background(White, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 3.dp),
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun SelectionBadge(
    isSelected: Boolean,
    isDisabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                color = if (isSelected) White else Color.Transparent,
                shape = CircleShape,
            )
            .border(
                width = 2.dp,
                color = when {
                    isSelected -> White
                    isDisabled -> White.copy(alpha = 0.28f)
                    else -> White.copy(alpha = 0.75f)
                },
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = PrimaryButtonText,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private const val MAX_SELECTED_SCENES = 3

@Composable
private fun String.drawableResourceId(): Int {
    val context = LocalContext.current
    val name = substringBeforeLast(".")
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}
