package digital.euforia.app.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import digital.euforia.app.ui.home.HomeScreen
import digital.euforia.app.ui.onboarding.OnboardingScreen
import digital.euforia.app.ui.player.VibesPlayerScreen
import digital.euforia.app.ui.player.audio.AudioPlayerScreen
import digital.euforia.app.ui.splash.SplashScreen
import digital.euforia.app.ui.video.VideoScreen
import timber.log.Timber

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    spheresState: MutableState<Boolean>,
    deepLinkDestination: HomeDestination?
) {
    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            Timber.tag("NAVIGATION")
                .d("Destination changed: ${destination.route}, args: $arguments")
        }
    }

    SharedTransitionLayout {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = Splash
        ) {
            composable<Splash>(
                enterTransition = {
                    fadeIn(animationSpec = tween(1500))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(1500)) // 500 мс fade out
                }
            ) {
                SplashScreen(navController = navController, viewModel = hiltViewModel())
            }
            composable<Video>(
                enterTransition = {
                    fadeIn(animationSpec = tween(1500))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(1500)) // 500 мс fade out
                }) {
                spheresState.value = true
                VideoScreen(navController = navController, viewModel = hiltViewModel())
            }
            composable<Onboarding>(
//            enterTransition = {
//            fadeIn(animationSpec = tween(2500)) // 500 мс fade out
//        }
            ) {
                spheresState.value = true
                OnboardingScreen(navController = navController, viewModel = hiltViewModel())
            }

            composable<Vibes> {
                VibesPlayerScreen(navController = navController, viewModel = hiltViewModel())
                spheresState.value = false
//            val playState = remember { mutableStateOf(PlayState.Playing) }
//            OrbitingStage(modifier = Modifier.noRippleClickable(onClick = {
//                playState.value =
//                    if (playState.value == PlayState.Playing) PlayState.Paused else PlayState.Playing
//            }), state = playState.value)

//            AnimatedGlassyBackground28p(R.drawable.img_intro_scene_preview_1)

                //            VibesPlayerWithCustomTrajectory()
//            AdvancedVibesAnimationView()
//            VibesAnimationDemo()
            }

            composable<Home> {
                spheresState.value = false
                HomeScreen(hiltViewModel())
            }

            composable<HomeDestination.AudioPlayer> {
//            spheresState.value = false
                AudioPlayerScreen(
                    navController = navController,
                    viewModel = hiltViewModel(),
                    animatedVisibilityScope = this
                )
            }
        }
    }
}
