package digital.euforia.app.ui.navigation

import androidx.annotation.Keep
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.programs.publication.PublicationType
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Splash(val deepLinkUri: String? = null) : HomeDestination()

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
data class Home(val deepLinkUri: String? = null)

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
    data class SoundscapesScene(
        val sceneId: Int
    ) : HomeDestination(showNavBar = false)

    @Serializable
    @Keep
    data object Settings : HomeDestination()

    @Serializable
    @Keep
    data class AudioPlayer(
        val accompanimentId: Int,
        val timeOfDay: TimeOfDay,
        val entryPoint: AudioPlayerEntryPoint
    ) : HomeDestination()

    @Serializable
    @Keep
    data object DevOptions : HomeDestination()

    @Serializable
    @Keep
    data object Favourites : HomeDestination()

    @Serializable
    @Keep
    data object FAQ : HomeDestination()

    @Serializable
    @Keep
    data object Name : HomeDestination()

    @Serializable
    @Keep
    data object Email : HomeDestination()

    @Serializable
    @Keep
    data object Voice : HomeDestination()

    @Serializable
    @Keep
    data object Language : HomeDestination()

    @Serializable
    @Keep
    data object Subscription : HomeDestination()

    @Serializable
    @Keep
    data object DeviceInfo : HomeDestination()

    @Serializable

    @Keep
    data object Notifications : HomeDestination()

    @Serializable
    @Keep
    data object FirstWeek : HomeDestination()

    @Serializable
    @Keep
    data object FinishWeek : HomeDestination()

    @Serializable
    @Keep
    data object HowItWorks : HomeDestination()

    @Serializable
    @Keep
    data object PersonalData : HomeDestination()

    @Serializable
    @Keep
    data object AppData : HomeDestination()

    @Serializable
    @Keep
    data object AboutPremium : HomeDestination()

    @Serializable
    @Keep
    data object Emergency : HomeDestination()

    @Serializable
    @Keep
    data object EmergencyContacts : HomeDestination()

    @Serializable
    @Keep
    data object Downloads : HomeDestination()

    @Serializable
    @Keep
    data class ProgramDetails(
        val programId: Int,
    ) : HomeDestination()

    @Serializable
    @Keep
    data class PublicationDetails(
        val id: Int,
        val publicationType: PublicationType,
        val packageTitle: String,
        val categoryId: Int? = null,
    ) : HomeDestination()

    @Serializable
    @Keep
    data class Publications(
        val type: PublicationType,
        val ids: String,
    ) : HomeDestination()

    @Serializable
    @Keep
    data class PublicationPlayer(
        val id: Int,
        val publicationType: PublicationType,
    ): HomeDestination()

    @Serializable
    @Keep
    data object Exercises: HomeDestination()
}
