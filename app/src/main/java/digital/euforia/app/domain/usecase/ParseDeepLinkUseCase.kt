package digital.euforia.app.domain.usecase

import android.net.Uri
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.soundscapes.copySceneIdFromPresetId
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.deeplink.DeepLinkCommand
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.programs.publication.PublicationType
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ParseDeepLinkUseCase @Inject constructor(
    private val accompanimentRepository: AccompanimentRepository,
    private val profilePreferences: ProfilePreferences,
    private val appPreferences: AppPreferences,
) {

    suspend operator fun invoke(uri: Uri): HomeDestination {
        val isCustomScheme = uri.scheme == "euforia"
        val isSupportedWebLink = uri.scheme == "https" && uri.host.equals("euforia.digital", ignoreCase = true)
        if (!isCustomScheme && !isSupportedWebLink) return HomeDestination.Plan

        if (isSupportedWebLink) {
            val segments = uri.pathSegments
            if (segments.firstOrNull().equals("scenes", ignoreCase = true)) {
                val seg1 = segments.getOrNull(1)
                if (seg1?.equals("mysaved", ignoreCase = true) == true) {
                    val savedId = segments.getOrNull(2)?.toIntOrNull()
                    return if (savedId == null) {
                        HomeDestination.Soundscapes
                    } else {
                        HomeDestination.SoundscapesScene(
                            sceneId = copySceneIdFromPresetId(savedId),
                            savedId = savedId,
                        )
                    }
                }
                val sceneId = seg1?.toIntOrNull()
                return if (sceneId == null) HomeDestination.Soundscapes
                else HomeDestination.SoundscapesScene(sceneId = sceneId)
            }
            return HomeDestination.Plan
        }

        val host = uri.host.orEmpty()           // meditations part
        val segments = uri.pathSegments         // /10
        val action = uri.getQueryParameter("action")?.lowercase()

        fun actionOrNull() = when (action) {
            "play" -> DeepLinkCommand.Action.Play
            else -> null
        }

        fun segInt(i: Int): Int? = segments.getOrNull(i)?.toIntOrNull()

        return when (host) {
            "continue_intro_preview" -> {
                val storedReminder = appPreferences.getFirstExperienceReminder()
                val previewType = uri.getQueryParameter("type")
                    ?.takeIf { it.isNotBlank() }
                    ?: storedReminder?.previewType
                val entityId = uri.getQueryParameter("entity_id")
                    ?.toIntOrNull()
                    ?: storedReminder?.entityId
                val accompanimentId = uri.getQueryParameter("accompaniment_id")
                    ?.toIntOrNull()
                    ?: storedReminder?.accompanimentId
                val timeOfDay = uri.getQueryParameter("time_of_day")
                    ?.takeIf { it.isNotBlank() }
                    ?: storedReminder?.timeOfDay
                val sceneTimerSeconds = uri.getQueryParameter("scene_timer_seconds")
                    ?.toIntOrNull()
                    ?: storedReminder?.sceneTimerSeconds
                    ?: DEFAULT_INTRO_SCENE_TIMER_SECONDS

                appPreferences.clearFirstExperienceReminder()

                when (previewType) {
                    "Accompaniment" -> {
                        val fallbackId = resolveCurrentAccompanimentId()
                        HomeDestination.AudioPlayer(
                            accompanimentId = accompanimentId ?: fallbackId,
                            timeOfDay = timeOfDay.toTimeOfDayOrDefault(),
                            entryPoint = AudioPlayerEntryPoint.ONBOARDING,
                            isOnboardingPreview = true,
                        )
                    }
                    "Meditation" -> {
                        if (entityId == null) {
                            HomeDestination.Plan
                        } else {
                            HomeDestination.PublicationPlayer(
                                id = entityId,
                                publicationType = PublicationType.MEDITATION,
                                isOnboardingPreview = true,
                            )
                        }
                    }
                    "Soundscape" -> {
                        if (entityId == null) {
                            HomeDestination.Soundscapes
                        } else {
                            HomeDestination.SoundscapesScene(
                                sceneId = entityId,
                                isOnboardingPreview = true,
                                introSceneTimerSeconds = sceneTimerSeconds,
                            )
                        }
                    }
                    else -> HomeDestination.Plan
                }
            }

            // Session
            "session" -> {
                val isDemo = profilePreferences.getIsDemo()
                val completedDays = accompanimentRepository.getCompletedAccompanimentsCount()
                val accompaniments = accompanimentRepository.getAllWithItemsFlow(isDemo).firstOrNull() ?: emptyList()
                
                val currentAcc = accompaniments.getOrNull(completedDays)?.accompaniment
                val accId = currentAcc?.id ?: 1

                when (segments.firstOrNull()) {
                    null, "morning" -> HomeDestination.AudioPlayer(
                        accompanimentId = accId,
                        timeOfDay = TimeOfDay.MORNING,
                        entryPoint = AudioPlayerEntryPoint.DAY
                    )

                    "daytime" -> HomeDestination.AudioPlayer(
                        accompanimentId = accId,
                        timeOfDay = TimeOfDay.DAYTIME,
                        entryPoint = AudioPlayerEntryPoint.DAY
                    )

                    else -> HomeDestination.AudioPlayer(
                        accompanimentId = accId,
                        timeOfDay = TimeOfDay.EVENING,
                        entryPoint = AudioPlayerEntryPoint.DAY
                    )
                }
            }

            // Audio scenes (audioscene://... + scenes://...)
            "audioscene", "scenes" -> {
                val s0 = segments.firstOrNull()
                if (s0.equals("mysaved", ignoreCase = true)) {
                    val savedId = segInt(1)
                    if (savedId == null) {
                        HomeDestination.Soundscapes
                    } else {
                        HomeDestination.SoundscapesScene(
                            sceneId = copySceneIdFromPresetId(savedId),
                            savedId = savedId,
                        )
                    }
                } else {
                    val id = segInt(0)
                    if (id == null) {
                        HomeDestination.Soundscapes
                    } else {
                        HomeDestination.SoundscapesScene(sceneId = id)
                    }
                }
            }

            "playlists" -> {
                val id = segInt(0)
                if (id == null) HomeDestination.Soundscapes
                else HomeDestination.SoundscapesPlaylist(playlistId = id)
            }

            // Meditations
            "meditations" -> {
                val id = segInt(0)
                val idsParam = uri.getQueryParameter("ids")
                val ids = idsParam
                    ?.split(",")
                    ?.mapNotNull { it.trim().toLongOrNull() }
                    ?.takeIf { it.isNotEmpty() }

                val pro = uri.getQueryParameter("pro")?.lowercase() == "pro"

                when {
                    id != null -> {
                        HomeDestination.PublicationDetails(
                            id = id,
                            publicationType = PublicationType.MEDITATION,
                            packageTitle = ""
                        )
                    }

                    ids != null || pro -> {
                        HomeDestination.Publications(
                            type = PublicationType.MEDITATION,
                            ids = ids?.joinToString(",").orEmpty()
                        )
                    }

                    else -> HomeDestination.Plan
                }
            }

            "exercises" -> {
                val id = segInt(0)
                val idsParam = uri.getQueryParameter("ids")
                val ids = idsParam
                    ?.split(",")
                    ?.mapNotNull { it.trim().toLongOrNull() }
                    ?.takeIf { it.isNotEmpty() }

                val pro = uri.getQueryParameter("pro")?.lowercase() == "pro"

                when {
                    id != null -> {
                        HomeDestination.PublicationDetails(
                            id = id,
                            publicationType = PublicationType.EXERCISE,
                            packageTitle = ""
                        )
                    }

                    ids != null || pro -> {
                        HomeDestination.Publications(
                            type = PublicationType.EXERCISE,
                            ids = ids?.joinToString(",").orEmpty()
                        )
                    }

                    else -> HomeDestination.Plan
                }
            }

            "articles" -> {
                val id = segInt(0)
                val idsParam = uri.getQueryParameter("category_id")
                val ids = idsParam
                    ?.split(",")
                    ?.mapNotNull { it.trim().toLongOrNull() }
                    ?.takeIf { it.isNotEmpty() }

                val pro = uri.getQueryParameter("pro")?.lowercase() == "pro"

                when {
                    id != null -> {
                        HomeDestination.PublicationDetails(
                            id = id,
                            publicationType = PublicationType.ARTICLE,
                            packageTitle = ""
                        )
                    }

                    ids != null || pro -> {
                        HomeDestination.Publications(
                            type = PublicationType.ARTICLE,
                            ids = ids?.joinToString(",").orEmpty()
                        )
                    }

                    else -> HomeDestination.Plan
                }
            }

//            // Articles
//            "articles" -> {
//                val categoryId = uri.getQueryParameter("category_id")?.toLongOrNull()
//                DeepLinkCommand.ArticlesFilter(categoryId)
//            }

//            // Packages
//            "packages" -> {
//                val id = segInt(0)
//                if (id == null) DeepLinkCommand.PackagesRoot else DeepLinkCommand.Package(id)
//            }

//            // Favorites / Downloads / History
//            "favorites" -> {
//                val s = segments.firstOrNull()
//                when (s) {
//                    null -> DeepLinkCommand.FavoritesRoot
//                    "meditations" -> DeepLinkCommand.FavoritesSection(DeepLinkCommand.Section.Meditations)
//                    "articles" -> DeepLinkCommand.FavoritesSection(DeepLinkCommand.Section.Articles)
//                    "exercises" -> DeepLinkCommand.FavoritesSection(DeepLinkCommand.Section.Exercises)
//                    else -> DeepLinkCommand.Unknown
//                }
//            }

//            "downloads" -> {
//                val s = segments.firstOrNull()
//                when (s) {
//                    null -> DeepLinkCommand.DownloadsRoot
//                    "meditations" -> DeepLinkCommand.DownloadsSection(DeepLinkCommand.Section.Meditations)
//                    "articles" -> DeepLinkCommand.DownloadsSection(DeepLinkCommand.Section.Articles)
//                    "exercises" -> DeepLinkCommand.DownloadsSection(DeepLinkCommand.Section.Exercises)
//                    "scenes" -> DeepLinkCommand.DownloadsSection(DeepLinkCommand.Section.Scenes)
//                    else -> DeepLinkCommand.Unknown
//                }
//            }

//            "history" -> {
//                val s = segments.firstOrNull()
//                when (s) {
//                    null -> DeepLinkCommand.HistoryRoot
//                    "meditations" -> DeepLinkCommand.HistorySection(DeepLinkCommand.Section.Meditations)
//                    "articles" -> DeepLinkCommand.HistorySection(DeepLinkCommand.Section.Articles)
//                    "exercises" -> DeepLinkCommand.HistorySection(DeepLinkCommand.Section.Exercises)
//                    "scenes" -> DeepLinkCommand.HistorySection(DeepLinkCommand.Section.Scenes)
//                    else -> DeepLinkCommand.Unknown
//                }
//            }
//
//            // Continue watch
//            "continue_watch" -> {
//                val s = segments.firstOrNull()
//                when (s) {
//                    null -> DeepLinkCommand.ContinueWatchRoot
//                    "meditations" -> DeepLinkCommand.ContinueWatchSection(DeepLinkCommand.ContinueSection.Meditations)
//                    "exercises" -> DeepLinkCommand.ContinueWatchSection(DeepLinkCommand.ContinueSection.Exercises)
//                    else -> DeepLinkCommand.Unknown
//                }
//            }

//            // Achievements
//            "achievements" -> when (segments.firstOrNull()) {
//                null -> DeepLinkCommand.AchievementsRoot
//                "premium_start" -> DeepLinkCommand.AchievementPremiumStart
//                else -> DeepLinkCommand.Unknown
//            }
//
//            // Search
//            "search" -> DeepLinkCommand.Search(uri.getQueryParameter("query"))
//
//            // Notification
//            "notification" -> {
//                val id = segInt(0)
//                DeepLinkCommand.Notification(
//                    id = id,
//                    title = uri.getQueryParameter("title"),
//                    body = uri.getQueryParameter("body"),
//                    imageUrl = uri.getQueryParameter("imageUrl"),
//                    buttonTitle = uri.getQueryParameter("buttonTitle"),
//                    buttonLink = uri.getQueryParameter("buttonLink"),
//                )
//            }
//
//            // Subscription
//            "subscription" -> {
//                val id = segInt(0)
//                if (id == null) DeepLinkCommand.SubscriptionRoot else DeepLinkCommand.Subscription(
//                    id
//                )
//            }

//            else -> HomeDestination.Plan
            else -> HomeDestination.Plan
        }
    }

    private suspend fun resolveCurrentAccompanimentId(): Int {
        val isDemo = profilePreferences.getIsDemo()
        val completedDays = accompanimentRepository.getCompletedAccompanimentsCount()
        val accompaniments = accompanimentRepository.getAllWithItemsFlow(isDemo).firstOrNull() ?: emptyList()
        return accompaniments.getOrNull(completedDays)?.accompaniment?.id ?: 1
    }

    private fun String?.toTimeOfDayOrDefault(): TimeOfDay {
        return runCatching {
            TimeOfDay.valueOf(this ?: TimeOfDay.EVENING.name)
        }.getOrDefault(TimeOfDay.EVENING)
    }

    private companion object {
        const val DEFAULT_INTRO_SCENE_TIMER_SECONDS = 600
    }
}