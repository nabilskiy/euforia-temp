package digital.euforia.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

object NavAnimations {
    private const val DURATION_MILLIS = 600

    /** Vertical sheet-style transitions for the soundscape scene editor. */
    const val SOUNDSCAPE_SCENE_TRANSITION_MS = 380

    /** Programs list → program details (pairs with shared library title morph). */
    const val PROGRAMS_DETAIL_TRANSITION_MS = 300

    const val LIBRARY_TITLE_SHARED_BOUNDS_MS = 300

    val programsDetailEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        fadeIn(animationSpec = tween(durationMillis = PROGRAMS_DETAIL_TRANSITION_MS))
    }

    val programsDetailExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        fadeOut(animationSpec = tween(durationMillis = PROGRAMS_DETAIL_TRANSITION_MS))
    }

    val programsDetailPopEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
        programsDetailEnter

    val programsDetailPopExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
        programsDetailExit

    val soundscapeSceneEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = tween(durationMillis = SOUNDSCAPE_SCENE_TRANSITION_MS)
        )
    }

    val soundscapeSceneExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(durationMillis = SOUNDSCAPE_SCENE_TRANSITION_MS)
        )
    }

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
