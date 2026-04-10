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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapePlaylist
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.SoundscapeColors
import digital.euforia.app.ui.theme.SoundscapesActionButtonActiveBackground
import digital.euforia.app.ui.theme.SoundscapesActionButtonBackground
import digital.euforia.app.ui.theme.SoundscapesCardSurface
import digital.euforia.app.ui.theme.SoundscapesScreenBackground
import digital.euforia.app.ui.theme.SoundscapesTileBorder
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.domain.usecase.soundscapes.SOUNDSCAPE_SECTION_FALLBACK_ALL
import digital.euforia.app.domain.usecase.soundscapes.SOUNDSCAPE_SECTION_UNCATEGORIZED
import digital.euforia.app.domain.usecase.soundscapes.SoundscapeCategorySection
import digital.euforia.app.ui.util.LocalizedResources
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.widget.MaxView
import digital.euforia.app.ui.util.widget.UpgradeView
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SoundscapesScreen(
    navController: NavHostController,
    viewModel: SoundscapesViewModel
) {
    val state by viewModel.collectAsState()
    var showSearchDialog by rememberSaveable { mutableStateOf(false) }
    var showPlaylists by rememberSaveable { mutableStateOf(false) }
    val localizedRes = LocalLocalizedRes.current

    SubscriptionActivityLauncher { launchSubscription ->
        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                is SoundscapesSideEffect.OpenScene -> {
                    navController.navigate(HomeDestination.SoundscapesScene(sideEffect.sceneId))
                }
                is SoundscapesSideEffect.OpenPlaylist -> {
                    navController.navigate(HomeDestination.SoundscapesPlaylist(sideEffect.playlistId))
                }
                SoundscapesSideEffect.OpenPaywall -> launchSubscription()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoundscapesScreenBackground)
        ) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 56.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SoundscapesHeader(
                            title = localizedRes.string(R.string.scenes_title),
                            onUpgradeClick = launchSubscription,
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        SearchPlaylistsRow(
                            searchLabel = localizedRes.string(R.string.scenes_search),
                            playlistsLabel = localizedRes.string(R.string.scenes_playlists),
                            playlistsActive = showPlaylists,
                            onSearchClick = {
                                showPlaylists = false
                                showSearchDialog = true
                            },
                            onPlaylistsClick = { showPlaylists = !showPlaylists },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    if (showPlaylists) {
                        item {
                            PlaylistsInlineBlock(
                                playlists = state.playlists,
                                onPlaylistClick = viewModel::onPlaylistClick,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    items(
                        items = state.displaySections,
                        key = { sec -> "${sec.categoryId}_${sec.scenes.joinToString { it.id.toString() }}" }
                    ) { section ->
                        SectionBlock(
                            title = sectionTitle(section, localizedRes),
                            scenes = section.scenes,
                            onSceneClick = viewModel::onSceneClick,
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(160.dp).navigationBarsPadding())
                    }
                }
                if (state.miniPlayer.isVisible) {
                    SoundscapesMiniPlayer(
                        title = state.miniPlayer.title,
                        isPlaying = state.miniPlayer.isPlaying,
                        onClick = viewModel::onMiniPlayerClick,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text(localizedRes.string(R.string.scenes_search)) },
            text = {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(localizedRes.string(R.string.scenes_search)) }
                )
            },
            confirmButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text(localizedRes.string(R.string.ok))
                }
            }
        )
    }

}

private fun sectionTitle(
    section: SoundscapeCategorySection,
    localizedRes: LocalizedResources
): String {
    return when (section.categoryId) {
        SOUNDSCAPE_SECTION_UNCATEGORIZED -> localizedRes.string(R.string.soundscapes_section_other)
        SOUNDSCAPE_SECTION_FALLBACK_ALL -> localizedRes.string(R.string.scenes_all)
        else -> section.title.ifBlank { localizedRes.string(R.string.scenes_all) }
    }
}

@Composable
private fun SoundscapesHeader(
    title: String,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = White,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            modifier = Modifier.weight(1f)
        )
        UpgradeView(
            modifier = Modifier
                .padding(8.dp),
            onClick = onUpgradeClick
        )
    }
}

@Composable
private fun SearchPlaylistsRow(
    searchLabel: String,
    playlistsLabel: String,
    playlistsActive: Boolean,
    onSearchClick: () -> Unit,
    onPlaylistsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onSearchClick)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = SoundscapesActionButtonBackground,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, SoundscapesTileBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 14.dp, horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = searchLabel,
                        color = White,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onPlaylistsClick)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = if (playlistsActive) SoundscapesActionButtonActiveBackground else SoundscapesActionButtonBackground,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, SoundscapesTileBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 14.dp, horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_playlist),
                        contentDescription = null,
                        tint = White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = playlistsLabel,
                        color = White,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    scenes: List<Scene>,
    onSceneClick: (Scene) -> Unit,
) {
    val sortedScenes = scenes.sortedBy { it.pro }
    val sceneColumns = sortedScenes.chunked(2)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            color = White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = sceneColumns,
                key = { columnScenes -> columnScenes.joinToString(separator = "_") { it.id.toString() } }
            ) { columnScenes ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    columnScenes.forEach { scene ->
                        SceneCard(
                            scene = scene,
                            modifier = Modifier.size(width = 156.dp, height = 96.dp),
                            onClick = { onSceneClick(scene) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SceneCard(
    scene: Scene,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = scene.imagePreviewUrl?.takeIf { it.isNotBlank() }
                ?: scene.imageUrl?.takeIf { it.isNotBlank() }
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = scene.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2C2E3A))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
            Text(
                text = scene.name,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(12.dp),
                color = White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (scene.pro) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    MaxView()
                }
            }
        }
    }
}

@Composable
private fun PlaylistsInlineBlock(
    playlists: List<SoundscapePlaylist>,
    onPlaylistClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (playlists.isEmpty()) {
            Text(
                text = localizedRes.string(R.string.no_data_empty_text),
                color = White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
            return@Column
        }
        playlists.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { pl ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onPlaylistClick(pl.id) },
                        color = SoundscapesCardSurface,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SoundscapesTileBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val playlistIconUrl = pl.imageUrl?.takeIf { it.isNotBlank() }
                            if (playlistIconUrl != null) {
                                AsyncImage(
                                    model = playlistIconUrl,
                                    contentDescription = pl.name,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PrimaryBackground)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = pl.name,
                                color = White,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SoundscapesMiniPlayer(
    title: String,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.ifBlank { "Soundscape" },
                style = MaterialTheme.typography.titleSmall,
                color = White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (isPlaying) "▶" else "❚❚",
                style = MaterialTheme.typography.labelLarge,
                color = White.copy(alpha = 0.85f)
            )
        }
    }
}
