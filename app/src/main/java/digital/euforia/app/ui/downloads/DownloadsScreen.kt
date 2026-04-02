package digital.euforia.app.ui.downloads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    DownloadsContent(state = state)
}

@Composable
private fun DownloadsContent(state: DownloadsState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Downloads", style = MaterialTheme.typography.headlineSmall)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.downloads) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(text = item.title, style = MaterialTheme.typography.titleSmall)
                        Text(text = item.status, style = MaterialTheme.typography.bodySmall)
                        Text(text = "Progress: ${item.progress}%", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun handleSideEffect(sideEffect: DownloadsSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}