package digital.euforia.app.ui.util.widget.vibe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.eveningColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Composable
fun CircularAvatarAnimation(
    modifier: Modifier,
    infinite: InfiniteTransition,
    fgAngle: Float,
    contentSize: Dp = 200.dp,
    animationDuration: Int = 8000,
    minAlpha: Float = 0.0f,
    maxAlpha: Float = 1.0f,
    waveWidth: Float = 90f,
    colors: List<Color> = eveningColors,
    reverseDirection: Boolean = false,
    chaotic: Boolean = true,
    waveCount: Int = 3,
    minWaveDuration: Int = 2000,
    maxWaveDuration: Int = 6000,
    minWaveLifetime: Int = 1000,
    maxWaveLifetime: Int = 3000,
    optimized: Boolean = true,
    edgeFeatherRatio: Float = 0.40f, // 20% згладження на кожному краї маски
    ringStrokeWidth: Dp? = 24.dp
) {
    // Two transparent mask arcs with independent timing and size
    val maskCount = 3
    val maskAlphas = remember { List(maskCount) { Animatable(0f) } }
    val maskStartAngles = remember { List(maskCount) { mutableStateOf(0f) } }

    // Animated sweep angle shared across masks (kept for minimal change)
    val arcSweepDeg = remember { Animatable(90f) }

    // Animate each mask independently: random angle, random durations
    LaunchedEffect(Unit) {
        repeat(maskCount) { i ->
            launch {
                // initial small random delay to desync
                delay(Random.nextLong(0L, 600L))
                while (true) {
                    val start = Random.nextFloat() * 360f
                    val expand = Random.nextInt(400, 1200)
                    val hold = Random.nextInt(150, 600)
                    val contract = Random.nextInt(400, 1200)

                    maskStartAngles[i].value = start

                    // Animate alpha up and down
                    maskAlphas[i].animateTo(
                        1f,
                        tween(durationMillis = expand, easing = LinearOutSlowInEasing)
                    )
                    delay(hold.toLong())
                    maskAlphas[i].animateTo(
                        0f,
                        tween(durationMillis = contract, easing = FastOutLinearInEasing)
                    )

                    // Idle a bit before next cycle
                    delay(Random.nextLong(800L, 4000L))
                }
            }
        }
    }
    // Animate the visible arc's sweep angle randomly over time
    LaunchedEffect(Unit) {
        // Continually pick random targets for the arc sweep
        while (true) {
            val target = Random.nextFloat() * (220f - 20f) + 120f // 20°..220°
            val duration = Random.nextInt(1800, 2800)
            arcSweepDeg.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
            )
            // Small idle between changes
            delay(Random.nextLong(200L, 900L))
        }
    }
    // Continuous rotation for masks
    val maskSpin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reverseDirection) -360f else 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "maskSpin"
    )
    val continuousColors = colors + colors.first()
    Box {
        Canvas(
            modifier
                .padding(16.dp)
                .graphicsLayer {
                    rotationZ = fgAngle
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                }.drawWithCache {
                    onDrawBehind {
                        // Draw a single ring arc using a sweep gradient with continuous colors
                        val ringCenter = Offset(size.width / 2f, size.height / 2f)
                        val sweepBrush = Brush.sweepGradient(
                            colors = continuousColors,
                            center = ringCenter
                        )

                        // Build highlight colors (slightly brighter) and corresponding sweep brush
                        val highlightColors = (colors.map {
                            Color(
                                red = min(1f, it.red * 1.5f),
                                green = min(1f, it.green * 1.5f),
                                blue = min(1f, it.blue * 1.5f),
                                alpha = it.alpha
                            )
                        } + colors.first().let {
                            Color(
                                red = min(1f, it.red * 1.3f),
                                green = min(1f, it.green * 1.3f),
                                blue = min(1f, it.blue * 1.3f),
                                alpha = it.alpha
                            )
                        })
                        val highlightBrush = Brush.sweepGradient(
                            colors = highlightColors,
                            center = ringCenter
                        )

                        val minDim = min(size.width, size.height)
                        val strokeWidth = ringStrokeWidth?.toPx() ?: (minDim * 0.08f)
                        val arcSize = Size(minDim - strokeWidth, minDim - strokeWidth)
                        val topLeft = Offset(
                            (size.width - arcSize.width) / 2f,
                            (size.height - arcSize.height) / 2f
                        )

                        // Draw into an isolated layer so we can apply a transparency mask afterwards
                        drawContext.canvas.saveLayer(
                            androidx.compose.ui.geometry.Rect(Offset.Zero, size),
                            androidx.compose.ui.graphics.Paint()
                        )

                        // Main color ring
                        drawArc(
                            brush = sweepBrush,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )

                        // Inner highlight ring centered on the main ring path with half the stroke width
                        val innerStrokeWidth = strokeWidth * 0.5f
                        drawArc(
                            brush = highlightBrush,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = innerStrokeWidth)
                        )

                        val rawSweepDeg = arcSweepDeg.value.coerceIn(5f, 300f)

                        // Невелике "оверсканування", щоб накрити скруглені капи
                        val radius = arcSize.width / 2f
                        val capAngleDeg =
                            ((strokeWidth / 2f) / max(1f, radius)) * (180f / PI.toFloat())
                        val overscanDeg = capAngleDeg.coerceIn(2f, 45f)

                        val sweepDeg = (rawSweepDeg + overscanDeg * 2f).coerceAtMost(360f)

                        // Нормалізовані фракції уздовж КУТУ (0..1 = 360°)
                        val sweepFrac = (sweepDeg / 360f).coerceIn(0.01f, 1f)
                        // ⬅️ "Пір’їна" = 20% (за замовч.) від поточної ширини маски З КОЖНОГО БОКУ
                        val featherEachSide = (sweepFrac * edgeFeatherRatio)
                            .coerceIn(0.02f, 0.30f) // запобігаємо надто вузьким/широким краям

                        // Функції для плавного рампу
                        fun clamp01(x: Float) = when {
                            x < 0f -> 0f
                            x > 1f -> 1f
                            else -> x
                        }

                        fun smootherstep01(t: Float): Float {
                            val x = clamp01(t)
                            return x * x * x * (x * (x * 6f - 15f) + 10f)
                        }

                        // Намалювати кілька масок (3) поверх шару
                        repeat(maskCount) { i ->
                            val a = maskAlphas[i].value.coerceIn(0f, 1f)
                            if (a <= 0f) return@repeat
                            val startAngleAdj = maskStartAngles[i].value - overscanDeg

                            // Генеруємо градієнт маски з плавними краями
                            // p біжить від 0 до sweepFrac (ми повернемо полотно на startAngleAdj)
                            val samples = 192
                            val stops = ArrayList<Pair<Float, Color>>(samples + 4)
                            stops.add(0f to Color.Black.copy(alpha = 0f)) // за межами маски — прозоро

                            for (s in 0..samples) {
                                val p =
                                    sweepFrac * (s.toFloat() / samples.toFloat()) // [0..sweepFrac]

                                // Відстань до лівого/правого краю, нормована до featherEachSide
                                val tLeft = if (featherEachSide > 0f) p / featherEachSide else 1f
                                val tRight = if (featherEachSide > 0f)
                                    (sweepFrac - p) / featherEachSide else 1f

                                // Плавний підйом/спад з обох боків
                                val rise = smootherstep01(tLeft)
                                val fall = smootherstep01(tRight)

                                // Усередині (між краями) alpha ≈ 1; біля країв йде плавний перехід
                                val localAlpha = min(rise, fall) * a

                                stops.add(p to Color.Black.copy(alpha = localAlpha))
                            }

                            stops.add(1f to Color.Black.copy(alpha = 0f))

                            val maskBrush = Brush.sweepGradient(
                                colorStops = stops.toTypedArray(),
                                center = ringCenter
                            )

                            // Повертаємо маску на потрібний стартовий кут і "вирізаємо" її DstOut
                            rotate(degrees = startAngleAdj + maskSpin, pivot = ringCenter) {
                                drawArc(
                                    brush = maskBrush,
                                    startAngle = 0f,
                                    sweepAngle = sweepDeg,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    blendMode = BlendMode.DstOut, // ⬅️ плавне стирання
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = strokeWidth,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                            }
                        }

                        drawContext.canvas.restore()
                    }
                }) {}
    }
}