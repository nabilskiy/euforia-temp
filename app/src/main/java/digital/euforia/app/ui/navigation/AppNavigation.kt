package digital.euforia.app.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
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
import androidx.navigation.toRoute
import digital.euforia.app.R
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
import digital.euforia.app.ui.settings.language.LanguageScreen
import digital.euforia.app.ui.settings.maxInfo.AboutPremiumScreen
import digital.euforia.app.ui.settings.name.NameScreen
import digital.euforia.app.ui.settings.notifications.NotificationsScreen
import digital.euforia.app.ui.settings.personaldata.PersonalDataScreen
import digital.euforia.app.ui.settings.personaldata.cleardata.ClearDataScreen
import digital.euforia.app.ui.settings.subscription.SubscriptionScreen
import digital.euforia.app.ui.settings.subscription.info.DeviceInfoScreen
import digital.euforia.app.ui.settings.voice.VoiceScreen
import digital.euforia.app.ui.sos.EmergencyScreen
import digital.euforia.app.ui.sos.contacts.ContactsScreen
import digital.euforia.app.ui.splash.SplashScreen
import digital.euforia.app.ui.util.widget.ComingSoonView
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
                enterTransition = {
                    // forward navigation
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                exitTransition = {
                    // screen we leave when going forward
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popEnterTransition = {
                    // when pressing back
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popExitTransition = {
                    // screen we leave when popping back
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(durationMillis = 300)
                    )
                }
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
//            enterTransition = {
//            fadeIn(animationSpec = tween(2500)) // 500 мс fade out
//        }
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
                composable<HomeDestination.Plan> {
                    isBottomBarShown.value = true
                    PlanScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                        animatedVisibilityScope = this
                    )
                }
                composable<HomeDestination.Programs> {
                    isBottomBarShown.value = true
                    ProgramsScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                    )
                }

                composable<HomeDestination.Soundscapes> {
                    isBottomBarShown.value = true
                    ComingSoonView(
                        titleRes = R.string.scenes_title,
                        navController = navController
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
                composable<HomeDestination.Settings> {
                    isBottomBarShown.value = true
                    SettingsScreen(navController, hiltViewModel(), this)
                }

                composable<HomeDestination.Name> {
                    NameScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.Email> {
                    EmailScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.Voice> {
                    VoiceScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.FAQ> {
                    FAQScreen(navController, hiltViewModel())
                }

                composable<HomeDestination.Language> {
                    LanguageScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.Subscription> {
                    SubscriptionScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.Notifications> {
                    NotificationsScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.DeviceInfo> {
                    DeviceInfoScreen(navController, hiltViewModel(), isBottomBarShown)
                }
                composable<HomeDestination.FirstWeek> {
                    FirstWeekScreen(navController, hiltViewModel(), isBottomBarShown)
                }

                composable<HomeDestination.HowItWorks> {
                    HowItWorksScreen(navController, hiltViewModel(), isBottomBarShown)
                }

                composable<HomeDestination.PersonalData> {
                    PersonalDataScreen(navController, hiltViewModel(), isBottomBarShown, this)
                }

                composable<HomeDestination.AppData> {
                    ClearDataScreen(navController, hiltViewModel(), isBottomBarShown)
                }

                composable<HomeDestination.AboutPremium> {
                    AboutPremiumScreen(navController, hiltViewModel(), isBottomBarShown)
                }
                composable<HomeDestination.Emergency> {
                    EmergencyScreen(navController, hiltViewModel(), isBottomBarShown)
                }
                composable<HomeDestination.EmergencyContacts> {
                    ContactsScreen(navController, hiltViewModel(), isBottomBarShown)
                }
                composable<HomeDestination.Downloads> {
                    isBottomBarShown.value = false
                    ComingSoonView(
                        titleRes = R.string.downloads_title,
                        navController = navController,
                        isBackAllowed = true
                    )
                }
                composable<HomeDestination.ProgramDetails> {
                    ProgramDetailsScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                    )
                }
                composable<HomeDestination.PublicationDetails> {
                    PublicationScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                    )
                }
                composable<HomeDestination.Publications> {
                    PublicationsScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                    )
                }

                composable<HomeDestination.PublicationPlayer> {
                    PublicationPlayerScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
                        navBarVisibilityState = isBottomBarShown,
                    )
                }
                composable<HomeDestination.Exercises> {
                    ExercisesScreen(
                        navController = navController,
                        viewModel = hiltViewModel(),
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
