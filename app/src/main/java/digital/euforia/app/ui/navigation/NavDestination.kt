package digital.euforia.app.ui.navigation

import androidx.annotation.Keep
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import kotlinx.serialization.Serializable

@Serializable
@Keep
data object Splash

@Serializable
@Keep
data object Video

@Serializable
@Keep
data object Onboarding

@Serializable
@Keep
data object Paywall

@Serializable
@Keep
data object Home

@Serializable
@Keep
data object Vibes

@Serializable
sealed class HomeDestination(val showNavBar: Boolean = true) {
    @Serializable
    @Keep
    data object Plan : HomeDestination()

    @Serializable
    @Keep
    data object Programs : HomeDestination()

    @Serializable
    @Keep
    data object Soundscapes : HomeDestination()

    @Serializable
    @Keep
    data object Settings : HomeDestination()

    @Serializable
    @Keep
    data class AudioPlayer(val accompanimentId: Int, val timeOfDay: TimeOfDay, val entryPoint: AudioPlayerEntryPoint)
}
