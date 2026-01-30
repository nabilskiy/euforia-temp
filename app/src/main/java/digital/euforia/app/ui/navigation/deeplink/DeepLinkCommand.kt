package digital.euforia.app.ui.navigation.deeplink

import android.net.Uri
import digital.euforia.app.domain.model.TimeOfDay

sealed interface DeepLinkCommand {
    data object SessionRoot : DeepLinkCommand
    data class SessionPart(val timeOfDay: TimeOfDay) : DeepLinkCommand

    data object AudioSceneRoot : DeepLinkCommand
    data class AudioScene(val id: Long, val action: Action? = null) : DeepLinkCommand
    data object ScenesRoot : DeepLinkCommand
    data class Scene(val id: Long, val action: Action? = null) : DeepLinkCommand

    data object PlaylistsRoot : DeepLinkCommand
    data class Playlist(val id: Long) : DeepLinkCommand

    data class Meditation(val id: Long) : DeepLinkCommand
    data class MeditationsFilter(val ids: List<Long>? = null, val pro: Boolean = false) : DeepLinkCommand

    data class ArticlesFilter(val categoryId: Long?) : DeepLinkCommand

    data object PackagesRoot : DeepLinkCommand
    data class Package(val id: Long) : DeepLinkCommand

    data object FavoritesRoot : DeepLinkCommand
    data class FavoritesSection(val section: Section) : DeepLinkCommand

    data object DownloadsRoot : DeepLinkCommand
    data class DownloadsSection(val section: Section) : DeepLinkCommand

    data object HistoryRoot : DeepLinkCommand
    data class HistorySection(val section: Section) : DeepLinkCommand

    data object ContinueWatchRoot : DeepLinkCommand
    data class ContinueWatchSection(val section: ContinueSection) : DeepLinkCommand

    data object AchievementsRoot : DeepLinkCommand
    data object AchievementPremiumStart : DeepLinkCommand

    data object SearchRoot : DeepLinkCommand
    data class Search(val query: String?) : DeepLinkCommand

    data class Notification(
        val id: Long? = null,
        val title: String? = null,
        val body: String? = null,
        val imageUrl: String? = null,
        val buttonTitle: String? = null,
        val buttonLink: String? = null,
    ) : DeepLinkCommand

    data object SubscriptionRoot : DeepLinkCommand
    data class Subscription(val id: Long) : DeepLinkCommand

    data object Unknown : DeepLinkCommand

    enum class Action { Play }
    enum class Section { Meditations, Articles, Exercises, Scenes }
    enum class ContinueSection { Meditations, Exercises }
}