@file:OptIn(ExperimentalMaterial3Api::class)

package digital.euforia.app.ui.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import digital.euforia.app.R
import digital.euforia.app.service.soundscapes.beginSoundscapeInterruption
import digital.euforia.app.service.soundscapes.endSoundscapeInterruption
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

private const val SOS_VIDEO_INTERRUPTION_TOKEN = "sos_emergency_video"

@Composable
fun EmergencyBottomSheet(
    videoUi: EmergencyVideoUi?,
    onDismiss: () -> Unit
) {
    videoUi ?: return
    val localizedRes = LocalLocalizedRes.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
//        modifier = Modifier.statusBarsPadding().padding(top = 16.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.padding(top = 16.dp).statusBarsPadding(),
        dragHandle = {},
        scrimColor = Color.Transparent,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = BottomSheetBackground,
        tonalElevation = 12.dp,
//        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top=16.dp),
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                    tint = White
                )
            }
            Text(
                text = localizedRes.string(videoUi.titleRes),
                style = MaterialTheme.typography.displaySmall,
                color = White,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            VideoView(url = videoUi.videoUrl)
            Text(
                text = localizedRes.string(videoUi.descriptionRes),
                style = MaterialTheme.typography.bodyLarge,
                color = White.copy(alpha = 0.8f),
                modifier = Modifier.navigationBarsPadding().padding(bottom=24.dp)
            )
        }
    }
}

@Composable
private fun VideoView(url: String) {
    // Player
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context)
            .build().apply {
                repeatMode = ExoPlayer.REPEAT_MODE_OFF
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = false
            }
    }

    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        onDispose {
            endSoundscapeInterruption(context, SOS_VIDEO_INTERRUPTION_TOKEN)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(20.dp))
            .fillMaxWidth()
    ) {
        AndroidView(
            factory = { ctx ->
                android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.player_view_texture, null, false).also { root ->
                        root.findViewById<PlayerView>(R.id.player_view).apply {
                            player = exoPlayer
                            useController = false
                            setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                            setKeepContentOnPlayerReset(true)
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    }
            },
            update = { root ->
                root.findViewById<PlayerView>(R.id.player_view).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .fillMaxWidth()
        )

        // Center play/pause button (no other controls)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(White.copy(alpha = 0.2f))
                .clickable {
                    isPlaying = !isPlaying
                    if (isPlaying) {
                        beginSoundscapeInterruption(context, SOS_VIDEO_INTERRUPTION_TOKEN)
                    } else {
                        endSoundscapeInterruption(context, SOS_VIDEO_INTERRUPTION_TOKEN)
                    }
                    exoPlayer.playWhenReady = isPlaying
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = null,
                tint = White
            )
        }
    }
}