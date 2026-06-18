package digital.euforia.app.ui.onboardingV3.pager

import android.media.MediaPlayer
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.blur
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import digital.euforia.app.R
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.BackgroundPlayerHelper
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SCENE_SWITCH_MS = 5_000L
private const val SCENE_TRANSITION_MS = 1_200
private const val SCENE_AUDIO_FADE_MS = 1_000L

@Composable
fun ScenesPreviewPage(
    isPageActive: Boolean,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    if (!isPageActive) return

    var sceneIndex by remember { mutableIntStateOf(0) }
    var sceneAudioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val currentSceneAudioPlayer = rememberUpdatedState(sceneAudioPlayer)
    val appearance = remember { Animatable(0f) }
    val context = LocalContext.current
    val scenePlayers = remember(context) {
        introScenes.map { scene ->
            ExoPlayer.Builder(context).build().apply {
                repeatMode = ExoPlayer.REPEAT_MODE_ONE
                volume = 0f
                setMediaItem(MediaItem.fromUri(scene.videoUrl))
                prepare()
                // Match iOS prefetch behavior: start buffering before the scene is shown.
                playWhenReady = true
            }
        }
    }

    DisposableEffect(scenePlayers) {
        BackgroundPlayerHelper.pauseWithFade()
        onDispose {
            currentSceneAudioPlayer.value?.release()
            sceneAudioPlayer = null
            scenePlayers.forEach { it.release() }
            BackgroundPlayerHelper.resumeWithFade(context, R.raw.bgm_intro)
        }
    }

    LaunchedEffect(Unit) {
        delay(300L)
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
            SceneVideoBackground(player = scenePlayers[index])
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
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
                .height(260.dp)
                .blur(30.dp)
                .background(Color.Black.copy(alpha = 0.35f)),
        )

        SceneSoundIcons(scene = introScenes[sceneIndex])

        Icon(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 20.dp)
                .size(24.dp)
                .align(Alignment.TopStart)
                .noRippleClickable(onBackClick),
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = White,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
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
            V3ScenesNextButton(
                text = localizedRes.string(R.string.intro_next),
                modifier = Modifier.padding(top = 78.dp),
                onClick = onNextClick,
            )
        }
    }
}

@Composable
private fun SceneVideoBackground(player: ExoPlayer) {
    AndroidView(
        factory = { ctx ->
            LayoutInflater.from(ctx).inflate(R.layout.player_view_texture, null, false).also { root ->
                root.findViewById<PlayerView>(R.id.player_view).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setKeepContentOnPlayerReset(true)
                }
            }
        },
        update = { root ->
            root.findViewById<PlayerView>(R.id.player_view).player = player
        },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun SceneSoundIcons(scene: IntroScenePreview) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp)
            .alignByAspectRatio(),
    ) {
        scene.sounds.forEach { sound ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = maxWidth * sound.x - 30.dp,
                        top = maxHeight * sound.y - 30.dp,
                    )
                    .size(60.dp)
                    .background(White.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(sound.iconRes),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

private fun Modifier.alignByAspectRatio(): Modifier = this
    .fillMaxWidth()
    .height(520.dp)

@Composable
private fun V3ScenesNextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 320.dp)
            .height(60.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(5.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE29B31),
                            Color(0xFFFF5589),
                            Color(0xFF204FC0),
                        ),
                    ),
                    shape = CircleShape,
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

private data class IntroScenePreview(
    val videoUrl: String,
    val audioRes: Int,
    val sounds: List<IntroSceneSound>,
)

private data class IntroSceneSound(
    val iconRes: Int,
    val x: Float,
    val y: Float,
)

private val introScenes = listOf(
    IntroScenePreview(
        videoUrl = "https://ca-euforia-video.s3.ca-east-tor.io.cloud.ovh.net/mp4/b/b2684cabc92d8c7b0ac8849ae4a9ed6a.mp4",
        audioRes = R.raw.scenes_preview_poppies_field,
        sounds = listOf(
            IntroSceneSound(R.drawable.ic_sounds, 0.30f, 0.30f),
            IntroSceneSound(R.drawable.ic_forest, 0.70f, 0.70f),
        ),
    ),
    IntroScenePreview(
        videoUrl = "https://ca-euforia-video.s3.ca-east-tor.io.cloud.ovh.net/mp4/e/e8d7fecff38afd22f871ff0c0ee8e090.mp4",
        audioRes = R.raw.scenes_preview_fuji,
        sounds = listOf(
            IntroSceneSound(R.drawable.ic_sounds, 0.50f, 0.50f),
        ),
    ),
    IntroScenePreview(
        videoUrl = "https://ca-euforia-video.s3.ca-east-tor.io.cloud.ovh.net/mp4/6/6b11ff1f9b429c3551f65bafceeb737a.mp4",
        audioRes = R.raw.scenes_preview_cozy_fireplace,
        sounds = listOf(
            IntroSceneSound(R.drawable.ic_fire, 0.60f, 0.20f),
            IntroSceneSound(R.drawable.ic_sounds, 0.20f, 0.70f),
        ),
    ),
    IntroScenePreview(
        videoUrl = "https://ca-euforia-video.s3.ca-east-tor.io.cloud.ovh.net/mp4/5/5f74e46609fec39203e4a6d4bf48ed2d.mp4",
        audioRes = R.raw.scenes_preview_coffee_shop_of_thoughts,
        sounds = listOf(
            IntroSceneSound(R.drawable.ic_sounds, 0.50f, 0.50f),
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
