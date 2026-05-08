/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.catalog

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import coil.compose.AsyncImage
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapePlaylist
import digital.euforia.app.ui.home.NavBarHeight
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.SoundscapesActionButtonActiveBackground
import digital.euforia.app.ui.theme.SoundscapesActionButtonBackground
import digital.euforia.app.ui.theme.SoundscapesCardSurface
import digital.euforia.app.ui.theme.SoundscapesSearchDialogBackground
import digital.euforia.app.ui.theme.SoundscapesSearchFieldBorder
import digital.euforia.app.ui.theme.SoundscapesSearchFieldContainer
import digital.euforia.app.ui.theme.SoundscapesSearchSuggestionDivider
import digital.euforia.app.ui.theme.SoundscapesSearchSuggestionText
import digital.euforia.app.ui.theme.SoundscapesTileBorder
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.domain.usecase.soundscapes.SOUNDSCAPE_SECTION_FALLBACK_ALL
import digital.euforia.app.domain.usecase.soundscapes.SOUNDSCAPE_SECTION_DEFAULT_PLAYLIST
import digital.euforia.app.domain.usecase.soundscapes.SOUNDSCAPE_SECTION_MY
import digital.euforia.app.domain.usecase.soundscapes.SOUNDSCAPE_SECTION_UNCATEGORIZED
import digital.euforia.app.domain.usecase.soundscapes.SoundscapeCategorySection
import digital.euforia.app.ui.util.LocalizedResources
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.MaxView
import digital.euforia.app.ui.util.widget.PremiumButtonState
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.UpgradeView
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val ActiveSceneBackgroundDimAlpha = 0.55f
private val ActiveSceneBackgroundBlurRadiusDp = 24.dp
private const val SoundscapesSharedTitleKey = "soundscapes_title"

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun SharedTransitionScope.SoundscapesScreen(
    navController: NavHostController,
    viewModel: SoundscapesViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val state by viewModel.collectAsState()
    var showSearchDialog by rememberSaveable { mutableStateOf(false) }
    var showPlaylists by rememberSaveable { mutableStateOf(false) }
    val localizedRes = LocalLocalizedRes.current
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 16.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }
    val playlistsBackdropHeight by animateDpAsState(
        targetValue = if (showPlaylists) 445.dp else 0.dp,
        animationSpec = tween(durationMillis = 320),
        label = "playlists_backdrop_height"
    )
    val activeScene = remember(state.activeSceneId, state.scenes, state.myScenes) {
        val activeId = state.activeSceneId ?: return@remember null
        state.scenes.firstOrNull { it.id == activeId } ?: state.myScenes.firstOrNull { it.id == activeId }
    }
    val activeSceneBackgroundUrl = state.activeSceneImageUrl?.takeIf { it.isNotBlank() }
        ?: activeScene?.imageUrl?.takeIf { it.isNotBlank() }
        ?: activeScene?.imagePreviewUrl?.takeIf { it.isNotBlank() }

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
                .background(PrimaryBackground)
        ) {
            if (!activeSceneBackgroundUrl.isNullOrBlank()) {
                AsyncImage(
                    model = activeSceneBackgroundUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(ActiveSceneBackgroundBlurRadiusDp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = ActiveSceneBackgroundDimAlpha))
                )
            }

            if (playlistsBackdropHeight > 0.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(playlistsBackdropHeight)
                        .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                        .background(SoundscapesActionButtonBackground)
                )
            }

            BlurredAppBar(
                shouldBlur = shouldBlur,
                titleRes = R.string.scenes_title,
                isBackAllowed = false,
                hazeState = hazeState,
                navController = navController,
                premiumButtonState = if (state.isPremium) PremiumButtonState.NONE else PremiumButtonState.UPGRADE,
                onUpgradeClick = launchSubscription,
            )
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .hazeSource(hazeState),
                    state = listState,
                    contentPadding = PaddingValues(top = AppBarHeightMedium + 16.dp, bottom = 56.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val sectionsToRender = state.displaySections
                        .filterNot { it.categoryId == SOUNDSCAPE_SECTION_DEFAULT_PLAYLIST }
                    item(key = "title_shared_soundscapes") {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = SoundscapesSharedTitleKey),
                                    animatedVisibilityScope
                                ),
                            text = localizedRes.string(R.string.scenes_title),
                            style = MaterialTheme.typography.displaySmall,
                            color = White
                        )
                    }
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            SearchPlaylistsRow(
                                searchLabel = localizedRes.string(R.string.scenes_search),
                                playlistsLabel = localizedRes.string(R.string.scenes_playlists),
                                playlistsActive = showPlaylists,
                                onSearchClick = {
                                    viewModel.onSearchOpened()
                                    showPlaylists = false
                                    showSearchDialog = true
                                },
                                onPlaylistsClick = { showPlaylists = !showPlaylists },
                            )
                            AnimatedVisibility(
                                visible = showPlaylists,
                                enter = fadeIn(animationSpec = tween(260)) +
                                    expandVertically(animationSpec = tween(260)),
                                exit = fadeOut(animationSpec = tween(220)) +
                                    shrinkVertically(animationSpec = tween(220))
                            ) {
                                PlaylistsInlineBlock(
                                    playlists = state.playlists,
                                    onPlaylistClick = viewModel::onPlaylistClick,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                )
                            }
                        }
                    }
                    if (showPlaylists) {
                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }
                    items(
                        items = sectionsToRender,
                        key = { sec -> "${sec.categoryId}_${sec.scenes.joinToString { it.id.toString() }}" }
                    ) { section ->
                        SectionBlock(
                            title = sectionTitle(section, localizedRes),
                            scenes = section.scenes,
                            singleRow = section.categoryId == SOUNDSCAPE_SECTION_MY,
                            isPremium = state.isPremium,
                            activeSceneId = state.activeSceneId,
                            isPlaybackActive = state.isPlaybackActive,
                            onSceneClick = viewModel::onSceneClick,
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(160.dp).navigationBarsPadding())
                    }
                }
            }
            if (state.isLoading) {
                ProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (showSearchDialog) {
        Dialog(
            onDismissRequest = { showSearchDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = SoundscapesSearchDialogBackground
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = viewModel::onSearchQueryChanged,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            singleLine = true,
                            placeholder = { Text(localizedRes.string(R.string.scenes_search)) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_search),
                                    contentDescription = null,
                                    tint = White.copy(alpha = 0.42f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = White,
                                focusedTextColor = White,
                                unfocusedBorderColor = SoundscapesSearchFieldBorder,
                                focusedBorderColor = SoundscapesSearchFieldBorder.copy(alpha = 1f),
                                unfocusedContainerColor = SoundscapesSearchFieldContainer,
                                focusedContainerColor = SoundscapesSearchFieldContainer,
                                unfocusedPlaceholderColor = White.copy(alpha = 0.42f),
                                focusedPlaceholderColor = White.copy(alpha = 0.42f),
                                cursorColor = White.copy(alpha = 0.85f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        TextButton(
                            onClick = { showSearchDialog = false },
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = localizedRes.string(R.string.cancel),
                                color = White.copy(alpha = 0.92f),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(26.dp))

                    if (state.searchSuggestions.isNotEmpty()) {
                        Text(
                            text = localizedRes.string(R.string.search_suggestions),
                            color = White,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        state.searchSuggestions.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                row.forEach { suggestion ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.onSearchSuggestionClick()
                                                viewModel.onSearchQueryChanged(suggestion)
                                                showSearchDialog = false
                                            }
                                            .padding(top = 8.dp, bottom = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_search),
                                                contentDescription = null,
                                                tint = SoundscapesSearchSuggestionText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = suggestion,
                                                color = SoundscapesSearchSuggestionText,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(SoundscapesSearchSuggestionDivider)
                                        )
                                    }
                                }
                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (state.popularScenes.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = localizedRes.string(R.string.search_popular_scenes),
                            color = White,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.popularScenes, key = { it.id }) { scene ->
                                SceneCard(
                                    scene = scene,
                                    modifier = Modifier.size(width = 156.dp, height = 96.dp),
                                    onClick = {
                                        viewModel.onPopularSceneClick()
                                        showSearchDialog = false
                                        viewModel.onSceneClick(scene)
                                    },
                                    isPremium = state.isPremium,
                                    isActive = state.isPlaybackActive && state.activeSceneId == scene.id,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

private fun sectionTitle(
    section: SoundscapeCategorySection,
    localizedRes: LocalizedResources
): String {
    return when (section.categoryId) {
        SOUNDSCAPE_SECTION_DEFAULT_PLAYLIST -> localizedRes.string(R.string.scenes_default_playlist)
        SOUNDSCAPE_SECTION_MY -> localizedRes.string(R.string.playlist_type_my_scenes)
        SOUNDSCAPE_SECTION_UNCATEGORIZED -> localizedRes.string(R.string.soundscapes_section_other)
        SOUNDSCAPE_SECTION_FALLBACK_ALL -> localizedRes.string(R.string.scenes_all)
        else -> section.title.ifBlank { localizedRes.string(R.string.scenes_all) }
    }
}

@Composable
private fun SoundscapesHeader(
    title: String,
    isPremium: Boolean,
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
        if (!isPremium) {
            UpgradeView(
                modifier = Modifier
                    .padding(8.dp),
                onClick = onUpgradeClick
            )
        }
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
                .height(55.dp)
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
                .height(55.dp)
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
    singleRow: Boolean,
    isPremium: Boolean,
    activeSceneId: Int?,
    isPlaybackActive: Boolean,
    onSceneClick: (Scene) -> Unit,
) {
    val sortedScenes = scenes.sortedBy { it.pro }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            color = White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (singleRow) {
                items(sortedScenes, key = { it.id }) { scene ->
                    SceneCard(
                        scene = scene,
                        modifier = Modifier.size(width = 156.dp, height = 96.dp),
                        onClick = { onSceneClick(scene) },
                        isPremium = isPremium,
                        isActive = isPlaybackActive && activeSceneId == scene.id,
                    )
                }
            } else {
                val sceneColumns = sortedScenes.chunked(2)
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
                                isPremium = isPremium,
                                isActive = isPlaybackActive && activeSceneId == scene.id,
                            )
                        }
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
    isPremium: Boolean,
    isActive: Boolean,
) {
    val cardShape = RoundedCornerShape(24.dp)
    val sceneOverlayBaseColor = Color(0xFF181A1D)
    val sceneSelectionAccentColor = Color(0xFF4257C9)

    Card(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = cardShape,
        border = if (isActive) BorderStroke(2.dp, White) else null,
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
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                sceneOverlayBaseColor.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
            )

            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(sceneSelectionAccentColor.copy(alpha = 0.25f))
                )
            }

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
            if (!isPremium && scene.pro) {
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
    imageUrl: String?,
    isPlaying: Boolean,
    timerRemainingSeconds: Int?,
    onClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizedRes = LocalLocalizedRes.current
    val hasTimer = (timerRemainingSeconds ?: 0) > 0
    val coverSize = 46.dp
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = NavBarBackground.copy(alpha = 0.98f)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(coverSize)
                        .clip(RoundedCornerShape(10.dp))
                        .background(White.copy(alpha = 0.15f)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(coverSize),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title.ifBlank { localizedRes.string(R.string.soundscape_title_fallback) },
                        style = MaterialTheme.typography.titleMedium,
                        color = White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (hasTimer) {
                        Text(
                            text = formatMiniPlayerTimer(timerRemainingSeconds ?: 0),
                            style = MaterialTheme.typography.bodyMedium,
                            color = White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(coverSize)
                ) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = localizedRes.string(
                            if (isPlaying) R.string.soundscape_miniplayer_pause else R.string.soundscape_miniplayer_play
                        ),
                        tint = White.copy(alpha = 0.85f)
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(coverSize)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = localizedRes.string(R.string.next),
                        tint = White.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NavBarHeight)
                    .navigationBarsPadding()
            )
        }
    }
}

private fun formatMiniPlayerTimer(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hh = safe / 3600
    val mm = (safe % 3600) / 60
    val ss = safe % 60
    return if (hh > 0) {
        String.format("%02d:%02d:%02d", hh, mm, ss)
    } else {
        String.format("%02d:%02d", mm, ss)
    }
}
