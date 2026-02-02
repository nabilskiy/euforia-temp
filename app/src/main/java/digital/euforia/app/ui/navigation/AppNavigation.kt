package digital.euforia.app.ui.navigation

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
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import digital.euforia.app.R
import digital.euforia.app.domain.usecase.ParseDeepLinkUseCase
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
    val parseDeepLinkUseCase = ParseDeepLinkUseCase()

    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            Timber.tag("NAVIGATION")
                .d("Destination changed: ${destination.route}, args: $arguments")
        }
    }

    LaunchedEffect(deepLinkUri) {
        deepLinkUri?.let { uriString ->
            val currentDestination = navController.currentDestination
            val currentRoute = currentDestination?.route
            if (currentRoute != null && (currentRoute.contains("Splash") || currentRoute.contains("Splash?"))) {
                // If we are on Splash, it will be handled by SplashViewModel
                return@let
            }
            
            // If we are already on the destination, don't navigate again (to avoid loops if URI state is not cleared)
            val destination = parseDeepLinkUseCase.invoke(uriString.toUri())
            
            Timber.tag("NAVIGATION").d("Deep link received: $uriString -> $destination")

            // If we are already on this destination with the same arguments, skip navigation
            val currentBackStackEntry = navController.currentBackStackEntry
            val destinationRoute = destination::class.qualifiedName ?: destination.toString()
            if (currentBackStackEntry?.destination?.route == destinationRoute) {
                // Check if it's the same publication ID for example
                // This is a bit simplified, but helps avoid loops
                Timber.tag("NAVIGATION").d("Already on destination $destinationRoute, skipping navigation")
                return@let
            }
            
            navController.navigate(destination) {
                // If we are navigating to a HomeDestination from a deep link, 
                // we want to ensure Home is the base.
                if (destination is HomeDestination && destination !is HomeDestination.Plan) {
                    try {
                        // Check if Home is already in backstack
                        val hasHome = try { navController.getBackStackEntry<Home>(); true } catch (e: Exception) { false }
                        
                        if (!hasHome) {
                            // If no Home, navigate to it first
                            navController.navigate(Home()) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        
                        popUpTo("digital.euforia.app.ui.navigation.Home") {
                            saveState = true
                            inclusive = false
                        }
                    } catch (e: Exception) {
                        Timber.tag("NAVIGATION").e("Failed to ensure Home base: ${e.message}")
                    }
                }
                launchSingleTop = true
                restoreState = false 
            }
        }
    }

    SharedTransitionLayout {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = Splash(),
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
                    SplashScreen(
                        navController = navController,
                        viewModel = hiltViewModel()
                    )
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
                    val home = it.toRoute<Home>()
                    isBottomBarShown.value = true
                    // Only navigate to Plan if we are exactly on the Home route
                    // and not on any of its sub-destinations.
                    // This prevents overriding deep links.
                    LaunchedEffect(home.deepLinkUri, deepLinkUri) {
                        val route = navController.currentDestination?.route
                        Timber.tag("NAVIGATION").d("Home route reached. Current destination route: $route, args: ${it.arguments}")
                        
                        val isExactHome = route == Home::class.qualifiedName || 
                                         route == "digital.euforia.app.ui.navigation.Home" || 
                                         route?.startsWith("digital.euforia.app.ui.navigation.Home?") == true ||
                                         route == "digital.euforia.app.ui.navigation.HomeDestination.Plan"
                        
                        if (isExactHome) {
                            if (!deepLinkUri.isNullOrBlank()) {
                                val destination = parseDeepLinkUseCase.invoke(deepLinkUri.toUri())
                                Timber.tag("NAVIGATION").d("Home redirecting to DeepLink: $deepLinkUri -> $destination")
                                navController.navigate(destination) {
                                    // We keep Home in backstack
                                    popUpTo("digital.euforia.app.ui.navigation.Home") { inclusive = false }
                                }
                            } else if (!home.deepLinkUri.isNullOrBlank()) {
                                val destination = parseDeepLinkUseCase.invoke(home.deepLinkUri.toUri())
                                Timber.tag("NAVIGATION").d("Home redirecting to DeepLink (from route): ${home.deepLinkUri} -> $destination")
                                navController.navigate(destination) {
                                    // We keep Home in backstack
                                    popUpTo("digital.euforia.app.ui.navigation.Home") { inclusive = false }
                                }
                            } else {
                                Timber.tag("NAVIGATION").d("Redirecting to Plan from Home (no deep link)")
                                navController.navigate(HomeDestination.Plan) {
                                    popUpTo("digital.euforia.app.ui.navigation.Home") { inclusive = true }
                                }
                            }
                        } else {
                            Timber.tag("NAVIGATION").d("Not redirecting from Home. isExactHome: $isExactHome, route: $route")
                        }
                    }
                    Box(Modifier.fillMaxSize()) // Render an empty box to avoid black screen while navigating
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
