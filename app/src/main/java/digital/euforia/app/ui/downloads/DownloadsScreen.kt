package digital.euforia.app.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
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

    DownloadsContent(state = state, onRetryClick = viewModel::retry)
}

@Composable
private fun DownloadsContent(
    state: DownloadsState,
    onRetryClick: (SoundscapeDownloadItem) -> Unit,
) {
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    val statuses = remember(state.downloads) { state.downloads.map { it.status }.distinct() }
    val filtered = remember(state.downloads, selectedStatus) {
        state.downloads.filter { item -> selectedStatus == null || item.status == selectedStatus }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.downloads_title), style = MaterialTheme.typography.headlineSmall)
        if (statuses.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipLabel(
                    label = stringResource(R.string.downloads_filter_all),
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null }
                )
                statuses.forEach { status ->
                    FilterChipLabel(
                        label = status,
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status }
                    )
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(text = item.title, style = MaterialTheme.typography.titleSmall)
                        StatusBadge(status = item.status)
                        Text(
                            text = stringResource(R.string.downloads_progress_format, item.progress),
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (item.status == SoundscapeDownloadItem.STATUS_FAILED) {
                            Text(
                                text = stringResource(R.string.downloads_failed_hint),
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { onRetryClick(item) }) {
                                Text(stringResource(R.string.downloads_retry))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipLabel(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.1f)
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .height(32.dp)
            .background(bg, RoundedCornerShape(16.dp))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val bg = when (status) {
        SoundscapeDownloadItem.STATUS_READY -> Color(0xFF1E8E5A)
        SoundscapeDownloadItem.STATUS_DOWNLOADING -> Color(0xFF3B5CC4)
        SoundscapeDownloadItem.STATUS_FAILED -> Color(0xFFB73A3A)
        SoundscapeDownloadItem.STATUS_QUEUED -> Color(0xFF856404)
        else -> Color.White.copy(alpha = 0.2f)
    }
    Text(
        text = status,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .padding(top = 6.dp, bottom = 4.dp)
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

private fun handleSideEffect(sideEffect: DownloadsSideEffect) {
    // No side effects are defined for this screen yet.
    @Suppress("UNUSED_VARIABLE")
    val ignored = sideEffect
}