@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.downloads

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.domain.usecase.soundscapes.SoundscapeDownloadCard
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.AvatarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.MenuItem
import digital.euforia.app.ui.util.widget.OptionsMenu
import digital.euforia.app.ui.util.widget.titleItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SharedTransitionScope.DownloadsScreen(
    navController: NavHostController,
    viewModel: DownloadsViewModel,
    navBarVisibilityState: MutableState<Boolean>,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(navController, sideEffect)
    }
    SubscriptionActivityLauncher { launchSubscription ->
        NavBarlessScreen(navBarVisibilityState) {
            DownloadsContent(
                navController = navController,
                state = state,
                animatedVisibilityScope = animatedVisibilityScope,
                onBackClick = { navController.popBackStack() },
                onClearAllClick = viewModel::onClearAllDownloadsClick,
                onItemClick = viewModel::onDownloadClick,
                onUpgradeClick = launchSubscription,
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.DownloadsContent(
    navController: NavHostController,
    state: DownloadsState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBackClick: () -> Unit,
    onClearAllClick: () -> Unit,
    onItemClick: (SoundscapeDownloadCard) -> Unit,
    onUpgradeClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        BlurredAppBar(
            shouldBlur = shouldBlur,
            titleRes = R.string.downloads_title,
            backTitleRes = R.string.profile_title,
            sharedElementKeyForBackTitle = "my_euforia_title",
            animatedVisibilityScope = animatedVisibilityScope,
            hazeState = hazeState,
            navController = navController,
            onBackClick = onBackClick,
            actionButton = {
                Box {
                    if (state.downloads.isNotEmpty()) IconButton(onClick = { showMenu = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = null,
                            tint = White
                        )
                    }
                    if (state.downloads.isNotEmpty()) {
                        OptionsMenu(
                            expanded = showMenu,
                            onExpandedChange = { showMenu = it },
                            menuItems = listOf(
                                MenuItem(
                                    titleRes = R.string.downloads_clear_all,
                                    onClick = onClearAllClick
                                )
                            )
                        )
                    }
                }
            }
        )
        if (state.downloads.isEmpty()) {
            DownloadsEmptyState(
                onUpgradeClick = onUpgradeClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = AppBarHeightMedium + 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState),
                state = listState,
                contentPadding = PaddingValues(
                    top = AppBarHeightMedium + 16.dp,
                    bottom = 56.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                titleItem(
                    titleRes = R.string.downloads_title,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                item(key = "downloads_soundscapes_section") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = localizedRes.string(R.string.scenes_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.downloads, key = { it.download.id }) { item ->
                                Box(modifier = Modifier.size(width = 186.dp, height = 306.dp)) {
                                    DownloadSceneCard(
                                        item = item,
                                        onClick = { onItemClick(item) },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsEmptyState(
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = localizedRes.string(R.string.profile_downloads_free_empty_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = White,
            textAlign = TextAlign.Center
        )
        Text(
            text = localizedRes.string(R.string.profile_downloads_free_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF2B2E3A))
                .clickable(onClick = onUpgradeClick)
                .padding(horizontal = 40.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = localizedRes.string(R.string.profile_downloads_free_empty_button),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = White
            )
        }
    }
}

@Composable
private fun DownloadSceneCard(
    item: SoundscapeDownloadCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(26.dp)
    val hazeState = rememberHazeState()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = PrimaryBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(cardShape)
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                        .background(Color(0xFF2C2E3A))
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.ultraThin(AvatarBackground)
                    )
                    .zIndex(1f)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun handleSideEffect(navController: NavHostController, sideEffect: DownloadsSideEffect) {
    when (sideEffect) {
        is DownloadsSideEffect.OpenScene -> {
            navController.navigate(HomeDestination.SoundscapesScene(sideEffect.sceneId))
        }
    }
}