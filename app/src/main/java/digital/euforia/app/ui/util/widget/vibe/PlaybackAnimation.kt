package digital.euforia.app.ui.util.widget.vibe

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.VibesBackground
import digital.euforia.app.ui.theme.eveningColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Represents the playback state of the animation
 */
enum class PlayState {
    /**
     * Animation is playing with full movement and effects
     */
    PLAYING,

    /**
     * Animation is paused with reduced movement and effects
     */
    PAUSED,
    LOADING,
    LOADED,
    READY,
    COMPLETED
}

enum class AnimationType {
    SPHERES, CIRCLE
}

/**
 * Parameters for a background sphere
 */
private data class BackgroundSphereParams(
    val orbitRadius: Float,
    val blurRadius: Float
)

/**
 * Parameters for a foreground sphere
 */
private data class ForegroundSphereParams(
    val baseRadius: Float,
    val initialDirection: Offset,
    val color: Color
)

/**
 * Main component that renders an orbiting animation with background and foreground spheres.
 *
 * @param modifier Modifier to be applied to the component
 * @param state Current playback state (Playing or Paused)
 * @param contentSize Logical size of the scene
 * @param buttonRadius Radius of the central button (for hiding spheres behind it when paused)
 * @param colors List of colors to use for the spheres
 */
@Composable
fun PlaybackAnimation(
    modifier: Modifier = Modifier,
    animationType: AnimationType = AnimationType.SPHERES,
    state: PlayState,
    paddingState: Dp,
    contentSize: Dp = 280.dp,
    buttonRadius: Dp = 90.dp,
    colors: List<Color> = eveningColors,
) {
    val density = LocalDensity.current
    val sizePx = with(density) { contentSize.toPx() }
    val infinite = rememberInfiniteTransition(label = "infinite")

    // ===== ROTATION ANIMATIONS =====
    // Base rotation animation for all spheres (background and foreground)
    // All spheres rotate at the same speed with fixed 120° spacing
    val fgAngleBase by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(15000, easing = LinearEasing)), label = "fgAngle"
    )

    // ===== BACKGROUND SPHERE RADIUS ANIMATIONS =====
    // Controls how background spheres change size over time
    // Each sphere has different timing for a random, organic effect
    val bgRadiusT1 by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(
            animation = keyframes {
                durationMillis = 9600
                0f at 0 with CubicBezierEasing(0.4f, 0f, 0.6f, 1f)
                1f at 1800
                0f at 3600
            }
        ),
        label = "bgRadius1"
    )

    val bgRadiusT2 by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(
            animation = keyframes {
                durationMillis = 12200
                0f at 0 with CubicBezierEasing(0.45f, 0f, 0.55f, 1f)
                1f at 2100
                0f at 4200
            }
        ),
        label = "bgRadius2"
    )

    val bgRadiusT3 by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(
            animation = keyframes {
                durationMillis = 19000
                0f at 0 with CubicBezierEasing(0.35f, 0f, 0.65f, 1f)
                1f at 1500
                0f at 3000
            }
        ),
        label = "bgRadius3"
    )

    // ===== SPHERE CONFIGURATION =====
    // Background sphere parameters: (orbit radius, blur amount, angle offset)
    val bgSpecs = listOf(
        Triple(0.38f * sizePx, 136f, 0f),  // Inner sphere
        Triple(0.46f * sizePx, 144f, 0f),  // Middle sphere
        Triple(0.52f * sizePx, 152f, 0f)   // Outer sphere
    )

    Box(
        modifier = modifier.fillMaxSize()
//            .size(contentSize)
    ) {
        // Use drawWithCache for both background and foreground spheres
        val canvasModifier = Modifier
            .fillMaxSize()
            .background(VibesBackground)
            .blur(8.dp)
            .drawBackgroundSpheresWithCache(
                paddingState = paddingState,
                bgSpecs = bgSpecs,
                fgAngleBase = fgAngleBase,
                bgRadiusT1 = bgRadiusT1,
                bgRadiusT2 = bgRadiusT2,
                bgRadiusT3 = bgRadiusT3,
                colors = colors
            )

        if (animationType == AnimationType.SPHERES) {
            ForegroundSpheresAnimation(
                modifier = canvasModifier,
                state = state,
                fgAngleBase = fgAngleBase,
                sizePx = sizePx,
                colors = colors,
                paddingState = paddingState
            )
        } else {
            CircularAvatarAnimation(
                modifier = canvasModifier,
                infinite = infinite,
                colors = colors,
                fgAngle = fgAngleBase
            )
        }
    }
}


@Composable
private fun ForegroundSpheresAnimation(
    modifier: Modifier,
    state: PlayState,
    fgAngleBase: Float,
    sizePx: Float,
    colors: List<Color>,
    paddingState: Dp,
) {
    val infinite = rememberInfiniteTransition(label = "infinite")
    val isCompactMode = state == PlayState.LOADED || state == PlayState.READY
    // ===== MORPHING ANIMATIONS =====
    // Controls how spheres change shape between circle and ellipse
    // Each sphere has its own timing for independent "breathing" effect
    val morphT1 by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(
            animation = keyframes {
                durationMillis = 9400
                0f at 0 with CubicBezierEasing(0.45f, 0f, 0.55f, 1f)
                1f at 4700
                0f at 9400
            }
        ),
        label = "morph1"
    )

    val morphT2 by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(
            animation = keyframes {
                durationMillis = 13000  // Different duration
                0f at 0 with CubicBezierEasing(0.4f, 0f, 0.6f, 1f)  // Different easing
                1f at 6500
                0f at 13000
            }
        ),
        label = "morph2"
    )

    val morphT3 by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(
            animation = keyframes {
                durationMillis = 15100  // Different duration
                0f at 0 with CubicBezierEasing(0.5f, 0f, 0.5f, 1f)  // Different easing
                1f at 7550
                0f at 15100
            }
        ),
        label = "morph3"
    )

    // ===== BOUNCE ANIMATIONS =====
    // Controls how foreground spheres bounce away from center
    // Each sphere has different timing and easing for varied movement
    val bounceT1 by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0f at 0 with CubicBezierEasing(0.2f, 0f, 0.8f, 1f)
                1f at 2500
                0f at 5000
            }
        ),
        label = "bounce1"
    )

    val bounceT2 by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(
            animation = keyframes {
                durationMillis = 6400
                0f at 0 with CubicBezierEasing(0.3f, 0f, 0.7f, 1f)
                1f at 3200
                0f at 6400
            }
        ),
        label = "bounce2"
    )

    val bounceT3 by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(
            animation = keyframes {
                durationMillis = 9800
                0f at 0 with CubicBezierEasing(0.25f, 0f, 0.75f, 1f)
                1f at 5000
                0f at 9800
            }
        ),
        label = "bounce3"
    )

    // ===== SELF-ROTATION ANIMATIONS =====
    // Controls how foreground spheres rotate around their own centers
    // Each sphere has different speed and direction for independent rotation
    val selfRotation1 by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(
            animation = tween(
                durationMillis = 8000,  // 8 seconds for a full rotation
                easing = LinearEasing
            )
        ),
        label = "selfRotation1"
    )

    val selfRotation2 by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(
            animation = tween(
                durationMillis = 12000,  // 12 seconds for a full rotation
                easing = LinearEasing
            )
        ),
        label = "selfRotation2"
    )

    val selfRotation3 by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(
            animation = tween(
                durationMillis = 10000,  // 10 seconds for a full rotation
                easing = LinearEasing
            )
        ),
        label = "selfRotation3"
    )

    // ===== SHAPE MULTIPLIER ANIMATIONS =====
    // Controls the x and y multipliers for sphere shape when state changes
    // When playing, spheres are more elliptical; when paused, they're more circular
    val orbitXMultiplier by animateFloatAsState(
        targetValue = if (state == PlayState.PLAYING) 1.05f else if (isCompactMode) 0.5f else 0.9f,
        animationSpec = spring(
            stiffness = Spring.DampingRatioLowBouncy,
            dampingRatio = 0.9f
        ),
        label = "shapeXMultiplier"
    )

    val orbitYMultiplier by animateFloatAsState(
        targetValue = if (state == PlayState.PLAYING) 0.95f else if (isCompactMode) 0.4f else 0.8f,
        animationSpec = spring(
            stiffness = Spring.DampingRatioLowBouncy,
            dampingRatio = 0.9f
        ),
        label = "shapeYMultiplier"
    )

    // ===== ORBIT RADIUS ANIMATION =====
    // Controls how far spheres orbit from center based on play state
    // When playing, spheres orbit further out; when paused, they move toward center
    val targetOrbitPlaying = sizePx / 8
    val targetOrbitPaused = sizePx / 16 // almost under the button
    val targetOrbitReady = sizePx / 30 // almost under the button
    val orbitRadius by animateFloatAsState(
        targetValue = if (state == PlayState.PLAYING) targetOrbitPlaying else if (state == PlayState.LOADED || state == PlayState.READY) targetOrbitReady else targetOrbitPaused,
        animationSpec = spring(
            stiffness = Spring.DampingRatioLowBouncy,
        ),
        label = "orbitRadius"
    )

    // ===== ENTRY ANIMATION =====
    // Controls how spheres initially move from corners to their orbiting positions
    val entryProgresses = remember { List(3) { Animatable(0f) } }
    val sphereScale = remember { Animatable(1f) }
    val sphereBounceRange = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Stagger animation starts by 100ms for each sphere
        entryProgresses.forEachIndexed { idx, anim ->
            scope.launch {
                delay(idx * 100L)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 3650,
                        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
                    )
                )
            }
        }
    }

    LaunchedEffect(state) {
        scope.launch {
            if (state == PlayState.PAUSED) {
                sphereScale.animateTo(
                    targetValue = 0.95f,
                    animationSpec = spring(
                        stiffness = Spring.DampingRatioLowBouncy,
                    )
                )
                sphereBounceRange.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        stiffness = Spring.DampingRatioLowBouncy,
                    )
                )
            } else if (isCompactMode) {
                sphereScale.animateTo(
                    targetValue = 0.9f,
                    animationSpec = spring(
                        stiffness = Spring.DampingRatioLowBouncy,
                    )
                )
                sphereBounceRange.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        stiffness = Spring.DampingRatioLowBouncy,
                    )
                )
            } else {
                sphereScale.animateTo(
                    targetValue = 1.2f,
                    animationSpec = spring(
                        stiffness = Spring.DampingRatioLowBouncy,
                    )
                )
                sphereBounceRange.animateTo(
                    targetValue = 10f,
                    animationSpec = spring(
                        stiffness = Spring.DampingRatioLowBouncy,
                    )
                )
            }
        }
    }

    // Foreground sphere base radius
    val fgBaseR = 0.3f * sizePx * sphereScale.value

    Canvas(
        modifier.drawForegroundSpheresWithCache(
            paddingState = paddingState,
            state = state,
            fgAngleBase = fgAngleBase,
            morphT1 = morphT1,
            morphT2 = morphT2,
            morphT3 = morphT3,
            bounceT1 = bounceT1,
            bounceT2 = bounceT2,
            bounceT3 = bounceT3,
            selfRotation1 = selfRotation1,
            selfRotation2 = selfRotation2,
            selfRotation3 = selfRotation3,
            orbitXMultiplier = orbitXMultiplier,
            orbitYMultiplier = orbitYMultiplier,
            orbitRadius = orbitRadius,
            targetOrbitPlaying = targetOrbitPlaying,
            entryProgresses = entryProgresses,
            fgBaseR = fgBaseR,
            bounceRange = sphereBounceRange.value,
            colors = colors
        )
    ) {}
}

/* ====================== Helper Functions ======================= */

/**
 * Calculates a point on a circle given a center, radius, and angle in degrees
 *
 * @param center The center point of the circle
 * @param r The radius of the circle
 * @param angleDeg The angle in degrees (0 = right, 90 = bottom, etc.)
 * @return The point on the circle at the given angle
 */
private fun polar(center: Offset, r: Float, angleDeg: Float): Offset {
    val rad = Math.toRadians(angleDeg.toDouble())
    return center + Offset((r * cos(rad)).toFloat(), (r * sin(rad)).toFloat())
}

/**
 * Multiplies an Offset by a scalar value
 */
private operator fun Offset.times(k: Float) = Offset(this.x * k, this.y * k)

/**
 * Adds two Offsets together
 */
private operator fun Offset.plus(o: Offset) = Offset(this.x + o.x, this.y + o.y)

/**
 * Linearly interpolates between two float values
 *
 * @param a Start value
 * @param b End value
 * @param t Fraction between 0 and 1
 * @return Interpolated value
 */
private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t.coerceIn(0f, 1f)

/**
 * Linearly interpolates between two Color values
 *
 * @param start Start color
 * @param end End color
 * @param fraction Fraction between 0 and 1
 * @return Interpolated color
 */
private fun lerp(start: Color, end: Color, fraction: Float): Color {
    val fractionClamped = fraction.coerceIn(0f, 1f)
    return Color(
        red = lerp(start.red, end.red, fractionClamped),
        green = lerp(start.green, end.green, fractionClamped),
        blue = lerp(start.blue, end.blue, fractionClamped),
        alpha = lerp(start.alpha, end.alpha, fractionClamped)
    )
}

/**
 * Creates a modifier that draws background spheres with caching for better performance.
 *
 * @param bgSpecs List of background sphere specifications (orbit radius, blur amount)
 * @param fgAngleBase Base angle for rotation of all spheres
 * @param bgRadiusT1 Animation value for the first sphere's radius
 * @param bgRadiusT2 Animation value for the second sphere's radius
 * @param bgRadiusT3 Animation value for the third sphere's radius
 * @param colors List of colors to use for the spheres
 * @return Modifier with drawWithCache applied
 */
private fun Modifier.drawBackgroundSpheresWithCache(
    paddingState: Dp,
    bgSpecs: List<Triple<Float, Float, Float>>,
    fgAngleBase: Float,
    bgRadiusT1: Float,
    bgRadiusT2: Float,
    bgRadiusT3: Float,
    colors: List<Color>
): Modifier = drawWithCache {
    // Cache the size and center calculation
    // Shift the center upwards by the provided bottom padding (in dp), converted to px
    val bottomPaddingPx = paddingState.toPx()
    val center = Offset(size.width / 2f, size.height / 2f - (bottomPaddingPx/2))

    onDrawBehind {
        // Draw each background sphere
        bgSpecs.forEachIndexed { i, (orbit, blur, _) ->
            // Use fgAngleBase for all spheres to keep them aligned with foreground spheres
            val pos = polar(center, orbit, fgAngleBase + i * 120f)

            // Calculate varying radius based on animation - use different animation value for each sphere
            val animValue = when (i) {
                0 -> bgRadiusT1
                1 -> bgRadiusT2
                else -> bgRadiusT3
            }
            val m = 0.5f * (1f - cos(2 * Math.PI * animValue).toFloat()) // 0..1..0 smoothly
            val r = lerp(size.minDimension * 0.9f, size.minDimension * 1.1f, m)

            drawBackgroundSphere(
                center = pos,
                radius = r,
                color = colors[i % colors.size].copy(alpha = 1f),
                blurPx = blur
            )
        }
    }
}

/**
 * Creates a modifier that draws foreground spheres with caching for better performance.
 *
 * @param state Current playback state (Playing or Paused)
 * @param fgAngleBase Base angle for rotation of all spheres
 * @param morphT1 Animation value for the first sphere's morphing
 * @param morphT2 Animation value for the second sphere's morphing
 * @param morphT3 Animation value for the third sphere's morphing
 * @param bounceT1 Animation value for the first sphere's bouncing
 * @param bounceT2 Animation value for the second sphere's bouncing
 * @param bounceT3 Animation value for the third sphere's bouncing
 * @param selfRotation1 Animation value for the first sphere's self-rotation
 * @param selfRotation2 Animation value for the second sphere's self-rotation
 * @param selfRotation3 Animation value for the third sphere's self-rotation
 * @param orbitXMultiplier X-axis multiplier for ellipse shape
 * @param orbitYMultiplier Y-axis multiplier for ellipse shape
 * @param orbitRadius Current orbit radius based on play state
 * @param targetOrbitPlaying Target orbit radius when playing
 * @param entryProgresses List of entry animation progress values
 * @param fgBaseR Base radius for foreground spheres
 * @param colors List of colors to use for the spheres
 * @return Modifier with drawWithCache applied
 */
private fun Modifier.drawForegroundSpheresWithCache(
    paddingState: Dp,
    state: PlayState,
    fgAngleBase: Float,
    morphT1: Float,
    morphT2: Float,
    morphT3: Float,
    bounceT1: Float,
    bounceT2: Float,
    bounceT3: Float,
    selfRotation1: Float,
    selfRotation2: Float,
    selfRotation3: Float,
    orbitXMultiplier: Float,
    orbitYMultiplier: Float,
    orbitRadius: Float,
    targetOrbitPlaying: Float,
    bounceRange: Float,
    entryProgresses: List<Animatable<Float, AnimationVector1D>>,
    fgBaseR: Float,
    colors: List<Color>
): Modifier = drawWithCache {
    // Cache the size and center calculation
    // Shift the center upwards by the provided bottom padding (in dp), converted to px
    val bottomPaddingPx = paddingState.toPx()
    val center = Offset(size.width / 2f, size.height / 2f - (bottomPaddingPx/2))

    // Directions for initial entry from corners
    val entryDir = listOf(
        Offset(-1.5f, -1.5f),  // top-left corner
        Offset(1.5f, -1.5f),   // top-right corner
        Offset(1.5f, 1.5f)     // bottom-right corner
    )

    onDrawBehind {
        colors.forEachIndexed { i, color ->
            val initialColor = colors.first()  // First color for all spheres initially
            val targetColor = colors[i]

            // Get entry animation progress for both color transition and position
            val t = entryProgresses[i].value

            // Interpolate between initial and target colors
            val currentColor = lerp(initialColor, targetColor, t)

            val fgStrokeTop = currentColor.copy(alpha = 0.8f)  // More opaque at the top
            val fgStrokeBottom =
                currentColor.copy(alpha = 0.60f)  // More transparent at the bottom
            val fgFillTop = currentColor.copy(alpha = 1f)
            val fgFillBottom = currentColor.copy(alpha = 0.9f)

            val angle = fgAngleBase + i * 120f

            // Get the appropriate bounce animation value for this sphere
            val bounceValue = when (i) {
                0 -> bounceT1
                1 -> bounceT2
                else -> bounceT3
            }

            // Calculate bounce effect - more pronounced when playing
//            val bounceAmount = if (state == PlayState.Playing) {
//                // Convert 0..1..0 to -1..0..1 for outward bounce
//                (2 * bounceValue - 1) * (0.15f * targetOrbitPlaying)
//            } else {
//                // Reduced bounce when paused
//                (2 * bounceValue - 1) * (0.05f * targetOrbitPlaying)
//            }

            // Apply bounce to orbit radius
            val bouncedRadius = orbitRadius + bounceRange
            val posOnOrbit = polar(center, bouncedRadius, angle)

            // Initial movement from corner to center: (1 - t) * cornerVector * magnitude
            val cornerOffset = entryDir[i] * (size.minDimension * 0.6f * (1f - t))
            val pos = posOnOrbit + cornerOffset

            // Morph interpolation between circle and ellipse:
            // m=0 -> ellipse (rx>ry), m=1 -> circle (rx≈ry). Create "breathing" based on different morphT values
            // Use different animations for each figure
            val morphValue = when (i) {
                0 -> morphT1
                1 -> morphT2
                else -> morphT3
            }
            val m = 0.5f * (1f - cos(2 * Math.PI * morphValue).toFloat()) // 0..1..0 smoothly
            val rx = lerp(fgBaseR * orbitXMultiplier, fgBaseR * 0.75f, m)
            val ry = lerp(fgBaseR * orbitYMultiplier, fgBaseR * 0.75f, m)

            // "Glass" look: semi-transparent gradient + thin outline
            val bodyAlpha = 1f
            val strokeAlpha = 0.05f

            // Get the appropriate self-rotation value for this sphere
            val selfRotationValue = when (i) {
                0 -> selfRotation1
                1 -> selfRotation2
                else -> selfRotation3
            }

            drawForegroundSphere(
                center = pos,
                rx = rx,
                ry = ry,
                bodyTop = fgFillTop.copy(alpha = bodyAlpha),
                bodyBottom = fgFillBottom.copy(alpha = bodyAlpha * 1f),
                strokeTop = fgStrokeTop.copy(alpha = strokeAlpha),  // Use strokeTop instead of stroke
                strokeBottom = fgStrokeBottom.copy(alpha = strokeAlpha),  // Add strokeBottom
                strokeWidth = size.minDimension * 0.026f,
                // light internal blur only for the body (looks better on A12+)
                bodyBlurPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 12f else 28f,
                screenCenter = center, // Pass the screen center explicitly
                selfRotationAngle = selfRotationValue // Pass the self-rotation angle
            )
        }
    }
}
