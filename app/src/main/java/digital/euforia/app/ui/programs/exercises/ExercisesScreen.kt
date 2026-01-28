package digital.euforia.app.ui.programs.exercises

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ExercisesScreen(
    navController: NavHostController,
    viewModel: ExercisesViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    ExercisesContent()
}

@Composable
private fun ExercisesContent() {
}

private fun handleSideEffect(sideEffect: ExercisesSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}