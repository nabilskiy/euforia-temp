package digital.euforia.app.ui.programs.publications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun PublicationsScreen(
    navController: NavHostController,
    viewModel: PublicationsViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    PublicationsContent()
}

@Composable
private fun PublicationsContent() {
}

private fun handleSideEffect(sideEffect: PublicationsSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}