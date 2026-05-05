@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.playlist

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.soundscapes.catalog.SceneCard
import digital.euforia.app.ui.theme.SoundscapesScreenBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.ProgressIndicator
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SharedTransitionScope.SoundscapePlaylistScreen(
    navController: NavHostController,
    viewModel: SoundscapePlaylistViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val localizedRes = LocalLocalizedRes.current

    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SoundscapePlaylistSideEffect.OpenScene ->
                navController.navigate(
                    HomeDestination.SoundscapesScene(
                        sceneId = sideEffect.sceneId,
                        playlistId = sideEffect.playlistId
                    )
                )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundscapesScreenBackground)
    ) {
        BlurredAppBar(
            shouldBlur = false,
            titleRes = R.string.playlist_title,
            backTitleRes = R.string.scenes_title,
            sharedElementKeyForBackTitle = "soundscapes_title",
            animatedVisibilityScope = animatedVisibilityScope,
            navController = navController,
            onBackClick = { navController.popBackStack() },
        )
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(AppBarHeightMedium + 24.dp))
            Text(
                text = state.title.ifBlank { localizedRes.string(R.string.playlist_title) },
                color = White,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
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
                        onClick = { viewModel.onSceneClick(scene.id) },
                        isPremium = state.isPremium,
                        isActive = state.isPlaybackActive && state.activeSceneId == scene.id,
                    )
                }
            }
            Box(modifier = Modifier.navigationBarsPadding())
        }
        if (state.isLoading) {
            ProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

