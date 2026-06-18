package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

private val SummaryAccentPink = Color(0xFFFF78C8)
private val SummaryAccentBlue = Color(0xFF7A98FF)
private val SummaryCard = Color(0xFF1F2228)

@Composable
fun SummaryPage(
    selectedGoal: Goal?,
    isPageActive: Boolean,
    onNextClick: () -> Unit,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val isDirectionUp = selectedGoal?.summaryLevelDirectionUp ?: true
    val subtitle = selectedGoal?.summarySubtitle ?: localizedRes.string(R.string.intro_summary_subtitle)
    val info = buildString {
        append(selectedGoal?.summaryInfo ?: localizedRes.string(R.string.intro_summary_header))
        append(" ")
        append(localizedRes.string(R.string.intro_summary_header_2))
    }
    val contentAppear = remember { Animatable(0f) }
    val graphAppear = remember { Animatable(0f) }
    val dotsAppear = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { contentAppear.animateTo(1f, tween(550)) }
        launch {
            delay(160)
            graphAppear.animateTo(1f, tween(1_000))
        }
        launch {
            delay(620)
            dotsAppear.animateTo(1f, tween(650))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17191F)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 40.dp)
                .padding(bottom = 124.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .graphicsLayer {
                        alpha = contentAppear.value
                        translationY = (1f - contentAppear.value) * 18f
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = subtitle,
                    color = White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                )
                IntroBoldText(
                    text = if (isDirectionUp) {
                        localizedRes.string(R.string.intro_summary_title_low)
                    } else {
                        localizedRes.string(R.string.intro_summary_title_high)
                    },
                    color = White.copy(alpha = 0.5f),
                    boldColor = White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 25.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            SummaryGraph(
                isDirectionUp = isDirectionUp,
                graphProgress = graphAppear.value,
                dotsProgress = dotsAppear.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(top = 20.dp),
            )

            SummaryCard(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp),
            ) {
                IntroBoldText(
                    text = info,
                    color = White.copy(alpha = 0.5f),
                    boldColor = SummaryAccentPink,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }

            SummaryCard(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 30.dp),
                contentPadding = PaddingValues(horizontal = 30.dp, vertical = 26.dp),
            ) {
                Text(
                    text = localizedRes.string(R.string.intro_summary_items),
                    color = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        SummaryBottomButton(
            text = localizedRes.string(R.string.intro_summary_button),
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = onNextClick,
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 30.dp, vertical = 28.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SummaryCard, RoundedCornerShape(20.dp))
            .padding(contentPadding),
    ) {
        content()
    }
}

@Composable
private fun SummaryBottomButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x0017191F),
                        Color(0xFF17191F),
                    ),
                ),
            )
            .padding(horizontal = 40.dp, vertical = 30.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(7.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFE29B31), Color(0xFFFF5589), Color(0xFF204FC0)),
                        ),
                        CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(White, CircleShape)
                    .noRippleClickable(onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    color = PrimaryButtonText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SummaryGraph(
    isDirectionUp: Boolean,
    graphProgress: Float,
    dotsProgress: Float,
    modifier: Modifier = Modifier,
) {
    val localizedRes = LocalLocalizedRes.current
    val dots = remember(isDirectionUp) {
        if (isDirectionUp) {
            listOf(
                SummaryDot(0.1f, localizedRes.string(R.string.intro_summary_dot_3), localizedRes.string(R.string.intro_summary_dot_1_text)),
                SummaryDot(0.4f, localizedRes.string(R.string.intro_summary_dot_2), localizedRes.string(R.string.intro_summary_dot_2_text)),
                SummaryDot(0.87f, localizedRes.string(R.string.intro_summary_dot_1), localizedRes.string(R.string.intro_summary_dot_3_text)),
            )
        } else {
            listOf(
                SummaryDot(0.1f, localizedRes.string(R.string.intro_summary_dot_1), localizedRes.string(R.string.intro_summary_dot_1_text)),
                SummaryDot(0.4f, localizedRes.string(R.string.intro_summary_dot_2), localizedRes.string(R.string.intro_summary_dot_2_text)),
                SummaryDot(0.87f, localizedRes.string(R.string.intro_summary_dot_3), localizedRes.string(R.string.intro_summary_dot_3_text)),
            )
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val graphTop = 10.dp.toPx()
            val lineWidth = 5.dp.toPx()
            val scaleX = size.width / 404f
            val baseSamples = summaryGraphSamples()
            val samples = baseSamples.map { point ->
                Offset(
                    x = if (isDirectionUp) size.width - point.x * scaleX else point.x * scaleX,
                    y = graphTop + point.y.dp.toPx(),
                )
            }.sortedBy { it.x }

            val visibleSamples = samples.take(max(2, (samples.size * graphProgress).toInt()))
            val visiblePath = Path().apply {
                moveTo(visibleSamples.first().x, visibleSamples.first().y)
                visibleSamples.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val fillPath = Path().apply {
                moveTo(visibleSamples.first().x, visibleSamples.first().y + 12.dp.toPx())
                visibleSamples.drop(1).forEach { lineTo(it.x, it.y + 12.dp.toPx()) }
                lineTo(visibleSamples.last().x, size.height)
                lineTo(visibleSamples.first().x, size.height)
                close()
            }
            val graphBrush = Brush.horizontalGradient(
                colors = listOf(SummaryAccentPink, SummaryAccentPink, SummaryAccentBlue),
                startX = 0f,
                endX = size.width,
            )

            drawPath(
                path = fillPath,
                brush = graphBrush,
                alpha = graphProgress * 0.34f,
            )
            drawPath(
                path = visiblePath,
                brush = graphBrush,
                style = Stroke(width = lineWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            dots.forEachIndexed { index, dot ->
                val dotProgress = ((dotsProgress - index * 0.2f) / 0.6f).coerceIn(0f, 1f)
                val point = samples.pointAtProgress(dot.progress)
                drawLine(
                    color = Color.Black.copy(alpha = 0.35f),
                    start = Offset(point.x, point.y),
                    end = Offset(point.x, size.height - 28.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                )
                drawCircle(
                    color = if (point.x < size.width * 0.56f) SummaryAccentPink else SummaryAccentBlue,
                    radius = 15.dp.toPx() * dotProgress,
                    center = point,
                )
                drawCircle(
                    color = Color(0xFF17191F),
                    radius = 10.dp.toPx() * dotProgress,
                    center = point,
                )
            }
        }
        SummaryGraphLabels(
            isDirectionUp = isDirectionUp,
            dots = dots,
            dotsProgress = dotsProgress,
        )
    }
}

@Composable
private fun SummaryGraphLabels(
    isDirectionUp: Boolean,
    dots: List<SummaryDot>,
    dotsProgress: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
    ) {
        dots.forEachIndexed { index, dot ->
            val x = when (index) {
                0 -> 0.1f
                1 -> 0.4f
                else -> 0.87f
            }.let { progress ->
                if (isDirectionUp) progress else progress
            }
            val appear = ((dotsProgress - index * 0.2f) / 0.6f).coerceIn(0f, 1f)
            Text(
                text = dot.title,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = appear
                        translationX = (x - 0.5f) * size.width
                        translationY = when (index) {
                            0 -> 96.dp.toPx()
                            1 -> 54.dp.toPx()
                            else -> 0.dp.toPx()
                        }
                    },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Text(
                text = dot.subtitle,
                color = White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .graphicsLayer {
                        alpha = appear
                        translationX = (x - 0.5f) * size.width
                    },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

private data class SummaryDot(
    val progress: Float,
    val title: String,
    val subtitle: String,
)

private fun summaryGraphSamples(): List<Offset> {
    val curves = listOf(
        Cubic(Offset(0f, 2.5f), Offset(0f, 2.5f), Offset(38.7565f, 2.5f), Offset(67.5777f, 14f)),
        Cubic(Offset(67.5777f, 14f), Offset(119.412f, 34.6823f), Offset(117.751f, 66.2332f), Offset(171.93f, 79.5f)),
        Cubic(Offset(171.93f, 79.5f), Offset(182.095f, 81.989f), Offset(196.768f, 82.1919f), Offset(213.5f, 82.7188f)),
        Cubic(Offset(213.5f, 82.7188f), Offset(231.112f, 83.2733f), Offset(270.32f, 88.4993f), Offset(270.32f, 88.4993f)),
        Cubic(Offset(270.32f, 88.4993f), Offset(270.32f, 88.4993f), Offset(375.389f, 107.084f), Offset(402f, 125.5f)),
    )
    val samples = mutableListOf<Offset>()
    curves.forEach { curve ->
        repeat(80) { step ->
            samples += curve.pointAt(step / 80f)
        }
    }
    samples += Offset(404f, 127f)
    return samples
}

private fun List<Offset>.pointAtProgress(progress: Float): Offset {
    if (isEmpty()) return Offset.Zero
    val lengths = MutableList(size) { 0f }
    for (index in 1 until size) {
        val previous = this[index - 1]
        val point = this[index]
        lengths[index] = lengths[index - 1] + hypot(point.x - previous.x, point.y - previous.y)
    }
    val target = lengths.last() * progress.coerceIn(0f, 1f)
    for (index in 1 until lengths.size) {
        if (lengths[index] >= target) {
            val start = this[index - 1]
            val end = this[index]
            val segment = (lengths[index] - lengths[index - 1]).coerceAtLeast(0.001f)
            val t = (target - lengths[index - 1]) / segment
            return Offset(
                x = start.x + (end.x - start.x) * t,
                y = start.y + (end.y - start.y) * t,
            )
        }
    }
    return last()
}

private data class Cubic(
    val p0: Offset,
    val p1: Offset,
    val p2: Offset,
    val p3: Offset,
) {
    fun pointAt(t: Float): Offset {
        val u = 1f - t
        val tt = t * t
        val uu = u * u
        val uuu = uu * u
        val ttt = tt * t
        return Offset(
            x = uuu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + ttt * p3.x,
            y = uuu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + ttt * p3.y,
        )
    }
}
