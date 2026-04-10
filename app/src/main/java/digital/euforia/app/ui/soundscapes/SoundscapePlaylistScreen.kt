/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.theme.SoundscapesActionButtonBackground
import digital.euforia.app.ui.theme.SoundscapesScreenBackground
import digital.euforia.app.ui.theme.SoundscapesTileBorder
import digital.euforia.app.ui.theme.White
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SoundscapePlaylistScreen(
    navController: NavHostController,
    viewModel: SoundscapePlaylistViewModel,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SoundscapePlaylistSideEffect.OpenScene ->
                navController.navigate(HomeDestination.SoundscapesScene(sideEffect.sceneId))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundscapesScreenBackground)
    ) {
        PlaylistHeader(
            title = state.title,
            onBack = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.scenes, key = { it.id }) { scene ->
                SceneCard(
                    scene = scene,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp),
                    onClick = { viewModel.onSceneClick(scene.id) }
                )
            }
        }
        Box(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun PlaylistHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onBack)
        ) {
            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .fillMaxWidth(0.18f),
                color = SoundscapesActionButtonBackground,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SoundscapesTileBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                        tint = White
                    )
                }
            }
        }
        Text(
            text = title.ifBlank { "Playlist" },
            color = White,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

