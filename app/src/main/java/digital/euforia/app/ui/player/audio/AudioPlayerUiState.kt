package digital.euforia.app.ui.player.audio

import androidx.compose.runtime.Immutable
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.ui.util.widget.SoundEffectUi
import digital.euforia.app.ui.util.widget.vibe.PlayState

@Immutable
data class AudioPlayerUiState(
    val entryPoint: AudioPlayerEntryPoint,
    val timeOfDay: TimeOfDay,
    val title: String,
    val playState: PlayState,
    val pages: List<PlayerPage>,
    val currentPageIndex: Int,
    val selectedSoundIndex: Int,
    val avatarPreviewUrl: String?,
    val avatarsList: List<AvatarUi>,
    val avatarPreviewIds: List<Int>,
    val avatarUi: AvatarUi?,
    val soundsEffects: List<SoundEffectUi>,
    val isRated: Boolean,
    val sharedElementKey: String?
)