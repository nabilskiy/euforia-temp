package digital.euforia.app.ui.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import digital.euforia.app.ui.devoptions.DevOptionsScreen
import digital.euforia.app.ui.plan.PlanScreen
import digital.euforia.app.ui.player.audio.AudioPlayerScreen

@Composable
@androidx.compose.animation.ExperimentalSharedTransitionApi
fun HomeNavigation(navController: NavHostController, isBottomBarShown: MutableState<Boolean>) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = HomeDestination.Plan
        ) {
            composable<HomeDestination.Plan> {
                val route = it.toRoute<HomeDestination.Plan>()
                isBottomBarShown.value = true
                PlanScreen(
                    navController = navController,
                    viewModel = hiltViewModel(),
                    animatedVisibilityScope = this
                )
            }
            composable<HomeDestination.Programs> {
                val route = it.toRoute<HomeDestination.Programs>()
                isBottomBarShown.value = true
//            ProgramsScreen(route, navController)
                Box() {}
            }

            composable<HomeDestination.Soundscapes> {
                val route = it.toRoute<HomeDestination.Soundscapes>()
                isBottomBarShown.value = true
//            SoundscapesScreen(route, navController)
                Box() {}
            }
            composable<HomeDestination.Settings> {
                val route = it.toRoute<HomeDestination.Settings>()
                isBottomBarShown.value = false
//            SettingsScreen(route, navController)
                Box() {}
            }
            composable<HomeDestination.AudioPlayer> {
                isBottomBarShown.value = false
                AudioPlayerScreen(
                    navController,
                    hiltViewModel(),
                    this
                )
            }
            composable<HomeDestination.DevOptions> {
//                val route = it.toRoute<HomeDestination.DevOptions>()
                isBottomBarShown.value = true
                DevOptionsScreen(navController, hiltViewModel())
            }
        }
    }
}