package digital.euforia.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

object NavAnimations {
    private const val DURATION_MILLIS = 600

    val enter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(durationMillis = DURATION_MILLIS)
        )
    }

    val exit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(durationMillis = DURATION_MILLIS)
        )
    }

    val popEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(durationMillis = DURATION_MILLIS)
        )
    }

    val popExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(durationMillis = DURATION_MILLIS)
        )
    }
}
