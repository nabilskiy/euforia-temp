package digital.euforia.app.ui.util.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Magenta
import digital.euforia.app.ui.theme.OnboardingSphereColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FloatingOrbitingBalls(
    modifier: Modifier = Modifier,
    // розміри
    ballRadius: Float = 70f,
    orbitRadius: Float = 120f,
    // дрейф центра
    driftRadiusX: Float = 80f,
    driftRadiusY: Float = 120f,
    // швидкості (меныше = повільніше)
    orbitPeriodMs: Int = 40000,   // один оберт
    driftPeriodMsX: Int = 11000, // дрейф по X
    driftPeriodMsY: Int = 15000, // дрейф по Y
    // кольори куль
//    ballColor: Color = Color(0xFF7AA8FF),
    firstBallColor: Color = OnboardingSphereColors.first(),
    secondBallColor: Color = OnboardingSphereColors.last()
) {
    val transition = rememberInfiniteTransition(label = "orb")
    val orbitT = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(orbitPeriodMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitT"
    )
    val driftTx = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(driftPeriodMsX, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "driftTx"
    )
    val driftTy = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(driftPeriodMsY, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "driftTy"
    )

    Canvas(modifier = modifier) {
        // базовий центр полотна
        val cx = size.width / 2f
        val cy = size.height / 2f

        // плаваючий центр орбіти (ліссажу)
        val driftCenter = Offset(
            x = cx + sin(driftTx.value * 2f * PI.toFloat()) * driftRadiusX,
            y = cy + cos(driftTy.value * 2f * PI.toFloat()) * driftRadiusY
        )

        // кутова позиція по колу
        val angle = orbitT.value * 2f * PI.toFloat()

        // дві кулі на одній орбіті зі зсувом фази
        val phase2 = angle + PI.toFloat() * 0.6f // 108°
        val p1 = Offset(
            x = driftCenter.x + cos(angle) * orbitRadius,
            y = driftCenter.y + sin(angle) * orbitRadius
        )
        val p2 = Offset(
            x = driftCenter.x + cos(phase2) * orbitRadius,
            y = driftCenter.y + sin(phase2) * orbitRadius
        )

        // радіальний градієнт від непрозорого до повністю прозорого краю
        fun ballBrush(center: Offset, ballColor: Color) = Brush.radialGradient(
            colors = listOf(
                ballColor.copy(alpha = 1f),
                ballColor.copy(alpha = 0.0f)
            ),
            center = center,
            radius = ballRadius
        )

        // малюємо м’яко, шар за шаром (можна без saveLayer — прозорості достатньо)
        drawCircle(brush = ballBrush(p1, firstBallColor), radius = ballRadius, center = p1)
        drawCircle(brush = ballBrush(p2, secondBallColor), radius = ballRadius, center = p2)
    }
}
