package digital.euforia.app.ui.navigation

import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import digital.euforia.app.ui.devoptions.DevOptionsScreen
import digital.euforia.app.ui.downloads.DownloadsScreen
import digital.euforia.app.ui.home.HomeScreen
import digital.euforia.app.ui.howitworks.HowItWorksScreen
import digital.euforia.app.ui.onboarding.OnboardingScreen
import digital.euforia.app.ui.plan.PlanScreen
import digital.euforia.app.ui.plan.firstweek.FirstWeekScreen
import digital.euforia.app.ui.player.VibesPlayerScreen
import digital.euforia.app.ui.player.audio.AudioPlayerScreen
import digital.euforia.app.ui.programs.ProgramsScreen
import digital.euforia.app.ui.programs.exercises.ExercisesScreen
import digital.euforia.app.ui.programs.player.PublicationPlayerScreen
import digital.euforia.app.ui.programs.publication.PublicationScreen
import digital.euforia.app.ui.programs.programdetails.ProgramDetailsScreen
import digital.euforia.app.ui.programs.publications.PublicationsScreen
import digital.euforia.app.ui.settings.SettingsScreen
import digital.euforia.app.ui.settings.email.EmailScreen
import digital.euforia.app.ui.settings.faq.FAQScreen
import digital.euforia.app.ui.settings.favourites.FavouritesScreen
import digital.euforia.app.ui.settings.language.LanguageScreen
import digital.euforia.app.ui.settings.maxInfo.AboutPremiumScreen
import digital.euforia.app.ui.settings.name.NameScreen
import digital.euforia.app.ui.settings.notifications.NotificationsScreen
import digital.euforia.app.ui.settings.personaldata.PersonalDataScreen
import digital.euforia.app.ui.settings.personaldata.cleardata.ClearDataScreen
import digital.euforia.app.ui.settings.subscription.SubscriptionScreen
import digital.euforia.app.ui.settings.subscription.info.DeviceInfoScreen
import digital.euforia.app.ui.settings.voice.VoiceScreen
import digital.euforia.app.ui.soundscapes.scene.SoundscapeSceneScreen
import digital.euforia.app.ui.soundscapes.playlist.SoundscapePlaylistScreen
import digital.euforia.app.ui.soundscapes.catalog.SoundscapesScreen
import digital.euforia.app.ui.sos.EmergencyScreen
import digital.euforia.app.ui.sos.contacts.ContactsScreen
import digital.euforia.app.ui.splash.SplashScreen
import digital.euforia.app.ui.video.VideoScreen
import timber.log.Timber

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    spheresState: MutableState<Boolean>,
    isBottomBarShown: MutableState<Boolean>,
    deepLinkUri: String?
) {
    LaunchedEffect(deepLinkUri) {
        if (!deepLinkUri.isNullOrBlank()) {
            val uri = Uri.parse(deepLinkUri)
            // If we're not on splash, maybe navigate directly?
            // But usually Splash handles it.
            Timber.tag("NAVIGATION").d("AppNavigation deepLinkUri updated: $deepLinkUri")
        }
    }

    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            Timber.tag("NAVIGATION")
                .d("Destination changed: ${destination.route}, args: $arguments")
        }
    }

    SharedTransitionLayout {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = Splash(deepLinkUri),
//                enterTransition = NavAnimations.enter,
//                exitTransition = NavAnimations.exit,
//                popEnterTransition = NavAnimations.popEnter,
//                popExitTransition = NavAnimations.popExit
            ) {
                composable<Splash>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(1500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(1500)) // 500 мс fade out
                    }
                ) {
                    isBottomBarShown.value = false
                    SplashScreen(navController = navController, viewModel = hiltViewModel())
                }
                composable<Video>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(1500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(1500)) // 500 мс fade out
                    }) {
                    isBottomBarShown.value = false
                    spheresState.value = true
                    VideoScreen(navController = navController, viewModel = hiltViewModel())
                }
                composable<Onboarding>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    isBottomBarShown.value = false
                    spheresState.value = true
                    OnboardingScreen(navController = navController, viewModel = hiltViewModel())
                }

                composable<Vibes> {
                    isBottomBarShown.value = false
                    VibesPlayerScreen(navController = navController, viewModel = hiltViewModel())
                    spheresState.value = false
                }

                composable<Home> {
//                    if (deepLinkUri != null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(HomeDestination.Plan) {
                            popUpTo(Home(deepLinkUri)) { inclusive = true }
                        }
                    }
//                    } else {
//                        HomeScreen(navController, hiltViewModel(), isBottomBarShown)
//                    }
                }

                // Home Destinations moved here
                composable<HomeDestination.Plan>(
                    enterTransition = NavAnimations.enter,
//                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
//                    popExitTransition = NavAnimations.popExit
                ) {
                    spheresState.value = false
                    isBottomBarShown.value = true

                    PlanScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                        animatedVisibilityScope = this
                    )
                }
                composable<HomeDestination.Programs>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    isBottomBarShown.value = true
                    ProgramsScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                    )
                }

                composable<HomeDestination.Soundscapes>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    isBottomBarShown.value = true
                    SoundscapesScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        animatedVisibilityScope = this
                    )
                }
                composable<HomeDestination.SoundscapesScene>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    isBottomBarShown.value = false
                    SoundscapeSceneScreen(
                        navController = navController,
                        viewModel = hiltViewModel()
                    )
                }
                composable<HomeDestination.SoundscapesPlaylist>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    isBottomBarShown.value = false
                    SoundscapePlaylistScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        animatedVisibilityScope = this
                    )
                }
                composable<HomeDestination.AudioPlayer> {
                    isBottomBarShown.value = false
                    AudioPlayerScreen(
                        navController,
                        hiltViewModel(),
                        this
                    )
                }
                composable<HomeDestination.Settings>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    isBottomBarShown.value = true
                    SettingsScreen(navController, hiltViewModel(), this)
                }

                composable<HomeDestination.Name>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    NameScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.Email>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    EmailScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.Voice>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    VoiceScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.FAQ>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    FAQScreen(navController, hiltViewModel())
                }

                composable<HomeDestination.Language>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    LanguageScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.Subscription>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    SubscriptionScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.Notifications>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    NotificationsScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.DeviceInfo>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    DeviceInfoScreen(navController, hiltViewModel(), isBottomBarShown)
                }
                composable<HomeDestination.FirstWeek>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    FirstWeekScreen(navController, hiltViewModel(), isBottomBarShown)
                }

                composable<HomeDestination.HowItWorks>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    HowItWorksScreen(navController, hiltViewModel(), isBottomBarShown)
                }

                composable<HomeDestination.PersonalData>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    PersonalDataScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.AppData>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    ClearDataScreen(navController, hiltViewModel(), isBottomBarShown)
                }

                composable<HomeDestination.AboutPremium>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    AboutPremiumScreen(navController, hiltViewModel(), isBottomBarShown)
                }
                composable<HomeDestination.Emergency>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    EmergencyScreen(navController, hiltViewModel(), isBottomBarShown)
                }
                composable<HomeDestination.EmergencyContacts>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    ContactsScreen(navController, hiltViewModel(), isBottomBarShown)
                }
                composable<HomeDestination.Downloads>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    DownloadsScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                        animatedVisibilityScope = this
                    )
                }
                composable<HomeDestination.ProgramDetails>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    ProgramDetailsScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                    )
                }
                composable<HomeDestination.PublicationDetails>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    PublicationScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                    )
                }
                composable<HomeDestination.Publications>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    PublicationsScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                    )
                }

                composable<HomeDestination.PublicationPlayer>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    PublicationPlayerScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                    )
                }
                composable<HomeDestination.Exercises>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    ExercisesScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                    )
                }

                composable<HomeDestination.Favourites>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    FavouritesScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                    )
                }
                composable<HomeDestination.DevOptions>(
                    enterTransition = NavAnimations.enter,
                    exitTransition = NavAnimations.exit,
                    popEnterTransition = NavAnimations.popEnter,
                    popExitTransition = NavAnimations.popExit
                ) {
                    isBottomBarShown.value = true
                    DevOptionsScreen(
                        navController = navController,
                        viewModel = hiltViewModel()
                    )
                }
            }

            HomeScreen(navController, hiltViewModel(), isBottomBarShown)
        }
    }
}

@Composable
fun NavBarlessScreen(
    navBarVisibilityState: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    LaunchedEffect(navBarVisibilityState.value) {
        navBarVisibilityState.value = false
    }

    DisposableEffect(Unit) {
        onDispose {
            navBarVisibilityState.value = true
        }
    }
    content()
}

@Composable
fun NavBarScreen(
    navBarVisibilityState: MutableState<Boolean>,
    content: @Composable () -> Unit
) {

}
