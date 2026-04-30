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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.domain.usecase.soundscapes.SoundscapeDownloadCard
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.BlurredAppBar
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
    NavBarlessScreen(navBarVisibilityState) {
        DownloadsContent(
            navController = navController,
            state = state,
            animatedVisibilityScope = animatedVisibilityScope,
            onBackClick = { navController.popBackStack() },
            onClearAllClick = viewModel::onClearAllDownloadsClick,
            onItemClick = viewModel::onDownloadClick
        )
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
) {
    var showMenu by remember { mutableStateOf(false) }
    val localizedRes = LocalLocalizedRes.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        BlurredAppBar(
            shouldBlur = false,
            titleRes = R.string.downloads_title,
            backTitleRes = R.string.profile_title,
            sharedElementKeyForBackTitle = "my_euforia_title",
            animatedVisibilityScope = animatedVisibilityScope,
            navController = navController,
            onBackClick = onBackClick,
            actionButton = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = null,
                            tint = White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = localizedRes.string(R.string.downloads_clear_all)) },
                            onClick = {
                                showMenu = false
                                onClearAllClick()
                            }
                        )
                    }
                }
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = AppBarHeightMedium + 16.dp,
                bottom = 56.dp
            )
        ) {
            titleItem(titleRes = R.string.downloads_title)
            item {
                Text(
                    text = localizedRes.string(R.string.scenes_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
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

@Composable
private fun DownloadSceneCard(
    item: SoundscapeDownloadCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(26.dp)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(242.dp)
            ) {
                if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2C2E3A)))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                )
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
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0x24231F1A),
                            1f to Color(0xAA1E2C1D)
                        )
                    )
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = White,
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp)
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