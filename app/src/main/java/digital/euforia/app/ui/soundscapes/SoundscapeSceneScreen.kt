/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun SoundscapeSceneScreen(
    navController: NavHostController,
    viewModel: SoundscapeSceneViewModel
) {
    val state by viewModel.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(state.title.ifBlank { "Scene ${state.sceneId}" }, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::onPlayPause) {
                Text(if (state.isPlaying) "Pause" else "Play")
            }
            Button(onClick = viewModel::onSavePreset) { Text("Save preset") }
            Button(onClick = viewModel::onDownloadScene) { Text("Download") }
        }
        Spacer(Modifier.height(12.dp))
        Text("Music", style = MaterialTheme.typography.titleMedium)
        Slider(value = state.musicVolume, onValueChange = viewModel::onMusicVolume)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.layers) { layer ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(layer.title, style = MaterialTheme.typography.titleSmall)
                        Slider(
                            value = layer.volume,
                            onValueChange = { viewModel.onLayerVolume(layer.id, it) }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { viewModel.onLayerMute(layer.id, !layer.muted) }) {
                                Text(if (layer.muted) "Unmute" else "Mute")
                            }
                            Button(onClick = { viewModel.onLayerRemove(layer.id) }) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}

