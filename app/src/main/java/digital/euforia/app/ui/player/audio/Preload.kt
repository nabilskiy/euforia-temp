package digital.euforia.app.ui.player.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil.imageLoader
import coil.request.ImageRequest
import androidx.compose.ui.unit.dp

@Composable
fun PreloadImages(avatarsList: List<AvatarUi>, soundsList: List<SoundEffectUi>) {
    val context = LocalContext.current
    val imageLoader = context.imageLoader
    val soundSize = with(LocalDensity.current) { 128.dp.roundToPx() }
    val avatarSize = with(LocalDensity.current) { 256.dp.roundToPx() }

    LaunchedEffect(soundsList) {
        soundsList.forEach { sound ->
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(sound.imageUrl)
                    .diskCacheKey(sound.imageUrl)
                    .size(soundSize)
                    .allowHardware(true)
                    .build()
            )
        }
    }
    LaunchedEffect(avatarsList) {
        avatarsList.forEach { avatar ->
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(avatar.imageUrl)
                    .diskCacheKey(avatar.imageUrl)
                    .size(avatarSize)
                    .allowHardware(true)
                    .build()
            )
        }
    }
}
