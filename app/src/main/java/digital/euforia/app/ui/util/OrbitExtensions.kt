package digital.euforia.app.ui.util

import org.orbitmvi.orbit.ContainerHost
import timber.log.Timber

/**
 * Helper extension function to simplify the common pattern of using intent { reduce { } }
 * for state updates in Orbit MVI.
 *
 * @param reducer A lambda that takes the current state and returns a new state.
 */
inline fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.reduceState(
    crossinline reducer: STATE.() -> STATE
) {
    intent {
        reduce {
            reducer(state)
        }
        Timber.tag("STATE_REDUCED").d("Reduced state: $state")
    }
}

/**
 * Helper extension function to simplify the common pattern of using intent { postSideEffect(...) }
 * for posting side effects in Orbit MVI.
 *
 * @param sideEffect The side effect to be posted.
 */
inline fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.postEffect(
    sideEffect: SIDE_EFFECT
) {
    intent {
        postSideEffect(sideEffect)
    }
}
