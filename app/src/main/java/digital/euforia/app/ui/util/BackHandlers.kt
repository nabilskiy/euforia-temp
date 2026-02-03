package digital.euforia.app.ui.util

import androidx.navigation.NavHostController
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.Splash


fun onDeepLinkBackClick(navController: NavHostController) {
    val previousRoute = navController.previousBackStackEntry?.destination?.route
    val isSplash = previousRoute?.contains(Splash::class.qualifiedName.toString()) == true
    if (previousRoute == null || isSplash) {
        navController.navigate(HomeDestination.Plan) {
            popUpTo(0) { inclusive = true }
        }
    } else {
        navController.popBackStack()
    }
}
