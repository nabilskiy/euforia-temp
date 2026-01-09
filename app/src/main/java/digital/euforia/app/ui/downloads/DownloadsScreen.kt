package digital.euforia.app.ui.downloads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DownloadsScreen(
    navController: NavHostController,
    viewModel: DownloadsViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    DownloadsContent()
}

@Composable
private fun DownloadsContent() {
}

private fun handleSideEffect(sideEffect: DownloadsSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}