package digital.euforia.app.ui.onboardingV3.pager

import android.media.MediaPlayer
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.ui.onboardingV3.components.V3PrimaryButton
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.BackgroundPlayerHelper
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SCENE_SWITCH_MS = 5_000L
private const val SCENE_TRANSITION_MS = 1_200
private const val SCENE_AUDIO_FADE_MS = 1_000L
private val SceneSoundButtonSize = 60.dp

class ScenesPreviewPrewarmState(
    val players: List<ExoPlayer>,
) {
    var firstSceneFrameRendered by mutableStateOf(false)
        private set

    fun markFirstSceneFrameRendered() {
        firstSceneFrameRendered = true
    }

    fun release() {
        players.forEach { it.release() }
    }
}

@Composable
internal fun rememberScenesPreviewPrewarmState(): ScenesPreviewPrewarmState {
    val context = LocalContext.current
    val state = remember(context) {
        ScenesPreviewPrewarmState(
            players = introScenes.map { scene ->
                ExoPlayer.Builder(context).build().apply {
                    repeatMode = ExoPlayer.REPEAT_MODE_ONE
                    volume = 0f
                    setMediaItem(MediaItem.fromUri(scene.videoUrl))
                    prepare()
                    playWhenReady = false
                }
            },
        )
    }
    DisposableEffect(state) {
        onDispose { state.release() }
    }
    return state
}

@Composable
internal fun ScenesPreviewPrewarmHost(
    state: ScenesPreviewPrewarmState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!enabled || state.firstSceneFrameRendered) return

    SceneVideoBackground(
        player = state.players.first(),
        modifier = modifier.graphicsLayer { alpha = 0.01f },
        onFirstFrameRendered = state::markFirstSceneFrameRendered,
    )
}

@Composable
fun ScenesPreviewPage(
    isPageActive: Boolean,
    prewarmState: ScenesPreviewPrewarmState,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    if (!isPageActive) return

    var sceneIndex by remember { mutableIntStateOf(0) }
    var sceneAudioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val currentSceneAudioPlayer = rememberUpdatedState(sceneAudioPlayer)
    val appearance = remember { Animatable(0f) }
    val context = LocalContext.current
    val scenePlayers = prewarmState.players

    DisposableEffect(scenePlayers) {
        BackgroundPlayerHelper.pauseWithFade()
        onDispose {
            currentSceneAudioPlayer.value?.release()
            sceneAudioPlayer = null
            scenePlayers.forEach { it.playWhenReady = false }
            BackgroundPlayerHelper.resumeWithFade(context, R.raw.bgm_intro)
        }
    }

    LaunchedEffect(Unit) {
        while (!prewarmState.firstSceneFrameRendered) {
            delay(16L)
        }
        appearance.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(SCENE_SWITCH_MS)
            sceneIndex = (sceneIndex + 1) % introScenes.size
        }
    }

    LaunchedEffect(sceneIndex) {
        scenePlayers.forEachIndexed { index, player ->
            player.playWhenReady = index == sceneIndex
        }

        val oldPlayer = sceneAudioPlayer
        if (oldPlayer != null) {
            launch {
                fadeSceneAudio(oldPlayer, from = 1f, to = 0f)
                oldPlayer.release()
            }
        }
        val newPlayer = MediaPlayer.create(context, introScenes[sceneIndex].audioRes).apply {
            isLooping = true
            setVolume(0f, 0f)
            start()
        }
        sceneAudioPlayer = newPlayer
        fadeSceneAudio(newPlayer, from = 0f, to = 1f)
    }

    val titleAppear = scenesIntroStagger(appearance.value, start = 0f, end = 0.65f)
    val bodyAppear = scenesIntroStagger(appearance.value, start = 0.12f, end = 0.78f)
    val localizedRes = LocalLocalizedRes.current
    val chromeAlpha by animateFloatAsState(
        targetValue = if (prewarmState.firstSceneFrameRendered) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "scenesPreviewChromeAlpha",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = sceneIndex,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                fadeIn(tween(SCENE_TRANSITION_MS)) togetherWith
                        fadeOut(tween(SCENE_TRANSITION_MS)) using
                        SizeTransform(clip = false)
            },
            label = "scenesPreviewVideo",
        ) { index ->
            SceneVideoBackground(
                player = scenePlayers[index],
                modifier = Modifier.graphicsLayer {
                    alpha = if (index == 0 && !prewarmState.firstSceneFrameRendered) 0.01f else 1f
                },
                onFirstFrameRendered = {
                    if (index == 0) {
                        prewarmState.markFirstSceneFrameRendered()
                    }
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = chromeAlpha }
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Transparent,
                            0.30f to Color.Black.copy(alpha = 0.30f),
                            1.00f to Color.Black.copy(alpha = 0.90f),
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer { alpha = chromeAlpha }
                .height(330.dp),
        ) {
            SceneBottomTint(gradient = introScenes[sceneIndex].bottomGradient)
        }

        Box(modifier = Modifier.graphicsLayer { alpha = chromeAlpha }) {
            SceneSoundIcons(scene = introScenes[sceneIndex])
        }

        Icon(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 20.dp)
                .size(24.dp)
                .align(Alignment.TopStart)
                .graphicsLayer { alpha = chromeAlpha }
                .noRippleClickable(onBackClick),
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = White,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .graphicsLayer { alpha = chromeAlpha }
                .padding(start = 40.dp, end = 40.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = localizedRes.string(R.string.intro_scenes_step_title),
                color = White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 25.sp,
                    lineHeight = 37.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = titleAppear
                    translationY = (1f - titleAppear) * 24f
                },
            )
            IntroBoldText(
                text = localizedRes.string(R.string.intro_scenes_step_text),
                modifier = Modifier
                    .padding(top = 30.dp)
                    .graphicsLayer {
                        alpha = bodyAppear
                        translationY = (1f - bodyAppear) * 24f
                    },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 19.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            V3PrimaryButton(
                text = localizedRes.string(R.string.intro_next),
                modifier = Modifier.padding(top = 78.dp),
                onClick = onNextClick,
            )
        }
    }
}

@Composable
private fun SceneVideoBackground(
    player: ExoPlayer,
    modifier: Modifier = Modifier,
    onFirstFrameRendered: () -> Unit,
) {
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                onFirstFrameRendered()
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    AndroidView(
        factory = { ctx ->
            LayoutInflater.from(ctx).inflate(R.layout.player_view_texture, null, false).also { root ->
                root.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                root.findViewById<PlayerView>(R.id.player_view).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setKeepContentOnPlayerReset(true)
                }
            }
        },
        update = { root ->
            root.findViewById<PlayerView>(R.id.player_view).player = player
        },
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun SceneBottomTint(gradient: SceneBottomGradient) {
    val topColor by animateColorAsState(
        targetValue = gradient.top,
        animationSpec = tween(durationMillis = SCENE_TRANSITION_MS, easing = FastOutSlowInEasing),
        label = "sceneBottomTintTop",
    )
    val centerColor by animateColorAsState(
        targetValue = gradient.center,
        animationSpec = tween(durationMillis = SCENE_TRANSITION_MS, easing = FastOutSlowInEasing),
        label = "sceneBottomTintCenter",
    )
    val bottomColor by animateColorAsState(
        targetValue = gradient.bottom,
        animationSpec = tween(durationMillis = SCENE_TRANSITION_MS, easing = FastOutSlowInEasing),
        label = "sceneBottomTintBottom",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(30.dp)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Transparent,
                        0.34f to topColor,
                        0.72f to centerColor,
                        1.00f to bottomColor,
                    ),
                ),
            ),
    )
}

@Composable
private fun SceneSoundIcons(scene: IntroScenePreview) {
    AnimatedContent(
        targetState = scene.sounds,
        transitionSpec = {
            scaleIn(animationSpec = tween(260), initialScale = 0.82f) + fadeIn(tween(180)) togetherWith
                    scaleOut(animationSpec = tween(180), targetScale = 0.82f) + fadeOut(tween(160)) using
                    SizeTransform(clip = false)
        },
        label = "sceneSoundIcons",
    ) { sounds ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f),
        ) {
            sounds.forEach { sound ->
                SceneSoundButton(
                    iconUrl = sound.iconUrl,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            start = maxWidth * sound.x - SceneSoundButtonSize / 2,
                            top = maxHeight * sound.y - SceneSoundButtonSize / 2,
                        ),
                )
            }
        }
    }
}

@Composable
private fun SceneSoundButton(
    iconUrl: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(SceneSoundButtonSize)
            .shadow(
                elevation = 14.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.15f),
            )
            .clip(CircleShape)
            .background(Color(0xFF2D3038).copy(alpha = 0.30f), CircleShape)
            .border(2.dp, White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = iconUrl,
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

private data class IntroScenePreview(
    val videoUrl: String,
    val audioRes: Int,
    val bottomGradient: SceneBottomGradient,
    val sounds: List<IntroSceneSound>,
)

private data class SceneBottomGradient(
    val top: Color,
    val center: Color,
    val bottom: Color,
)

private data class IntroSceneSound(
    val iconUrl: String,
    val x: Float,
    val y: Float,
)

private val introScenes = listOf(
    IntroScenePreview(
        videoUrl = "https://ca-euforia-video.s3.ca-east-tor.io.cloud.ovh.net/mp4/b/b2684cabc92d8c7b0ac8849ae4a9ed6a.mp4",
        audioRes = R.raw.scenes_preview_poppies_field,
        bottomGradient = SceneBottomGradient(
            top = Color(0xFFE6A145).copy(alpha = 0.18f),
            center = Color(0xFFB46A2A).copy(alpha = 0.42f),
            bottom = Color(0xFF20110A).copy(alpha = 0.82f),
        ),
        sounds = listOf(
            IntroSceneSound("https://euforia.digital/storage/models/png/e/ea1feec4204ff72176c05bbec8d84ab6.png", 0.30f, 0.30f),
            IntroSceneSound("https://euforia.digital/storage/models/png/0/0672e63cdeb39d60fbabc9320ffebc7d.png", 0.70f, 0.70f),
        ),
    ),
    IntroScenePreview(
        videoUrl = "https://ca-euforia-video.s3.ca-east-tor.io.cloud.ovh.net/mp4/e/e8d7fecff38afd22f871ff0c0ee8e090.mp4",
        audioRes = R.raw.scenes_preview_fuji,
        bottomGradient = SceneBottomGradient(
            top = Color(0xFF5778C8).copy(alpha = 0.16f),
            center = Color(0xFF24396F).copy(alpha = 0.44f),
            bottom = Color(0xFF090E25).copy(alpha = 0.86f),
        ),
        sounds = listOf(
            IntroSceneSound("https://euforia.digital/storage/models/png/2/2a845f5089fac54fc332544cff68e25d.png", 0.50f, 0.50f),
        ),
    ),
    IntroScenePreview(
        videoUrl = "https://ca-euforia-video.s3.ca-east-tor.io.cloud.ovh.net/mp4/6/6b11ff1f9b429c3551f65bafceeb737a.mp4",
        audioRes = R.raw.scenes_preview_cozy_fireplace,
        bottomGradient = SceneBottomGradient(
            top = Color(0xFFFFB257).copy(alpha = 0.20f),
            center = Color(0xFFC2481F).copy(alpha = 0.48f),
            bottom = Color(0xFF190704).copy(alpha = 0.88f),
        ),
        sounds = listOf(
            IntroSceneSound("https://euforia.digital/storage/models/png/b/bd2eac852f12ad36aa644d31705493f3.png", 0.60f, 0.20f),
            IntroSceneSound("https://euforia.digital/storage/models/png/e/e0a6e2df3a993dd5eff987ee5d8f98ec.png", 0.20f, 0.70f),
        ),
    ),
    IntroScenePreview(
        videoUrl = "https://ca-euforia-video.s3.ca-east-tor.io.cloud.ovh.net/mp4/5/5f74e46609fec39203e4a6d4bf48ed2d.mp4",
        audioRes = R.raw.scenes_preview_coffee_shop_of_thoughts,
        bottomGradient = SceneBottomGradient(
            top = Color(0xFFC9864A).copy(alpha = 0.17f),
            center = Color(0xFF7A442D).copy(alpha = 0.46f),
            bottom = Color(0xFF140C08).copy(alpha = 0.86f),
        ),
        sounds = listOf(
            IntroSceneSound("https://euforia.digital/storage/models/png/2/2bd53189b3c344057e23be020078536c.png", 0.50f, 0.50f),
        ),
    ),
)

private fun scenesIntroStagger(progress: Float, start: Float, end: Float): Float {
    if (end <= start) return if (progress >= end) 1f else 0f
    val normalized = ((progress - start) / (end - start)).coerceIn(0f, 1f)
    return normalized * normalized * normalized * (normalized * (normalized * 6f - 15f) + 10f)
}

private suspend fun fadeSceneAudio(
    player: MediaPlayer,
    from: Float,
    to: Float,
) {
    val steps = 20
    val stepDelay = SCENE_AUDIO_FADE_MS / steps
    repeat(steps + 1) { index ->
        val progress = index.toFloat() / steps
        val volume = from + (to - from) * progress
        runCatching { player.setVolume(volume, volume) }
        delay(stepDelay)
    }
}
