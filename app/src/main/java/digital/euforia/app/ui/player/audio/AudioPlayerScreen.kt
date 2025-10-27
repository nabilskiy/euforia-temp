package digital.euforia.app.ui.player.audio

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import digital.euforia.app.service.AudioPlaybackService
import digital.euforia.app.ui.plan.item.subscribeToPagerUpdates
import digital.euforia.app.ui.player.audio.page.AvatarsPage
import digital.euforia.app.ui.player.audio.page.PlayerPage
import digital.euforia.app.ui.util.widget.vibe.PlayState
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AudioPlayerScreen(
    navController: NavHostController,
    viewModel: AudioPlayerViewModel
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    // Start playback when accompaniment data loaded
    LaunchedEffect(state.accompaniment) {
        val url = viewModel.getMusicUrlForTimeOfDay()
        if (!url.isNullOrBlank()) {
            context.startService(AudioPlaybackService.playMainIntent(context, url))
        }
    }

    // Release when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            context.startService(AudioPlaybackService.releaseIntent(context))
        }
    }

    AudioPlayerContent(
        title = state.title.orEmpty(),
        pages = state.pages,
        currentPageIndex = state.currentPageIndex,
        onPageSelected = viewModel::onPageSelected,
        navigateAvatars = viewModel::onNavigateToAvatars,
        navigatePlayer = viewModel::onNavigateToPlayer,
    )
}

@Composable
private fun AudioPlayerContent(
    title: String,
    pages: List<PlayerPage>,
    currentPageIndex: Int,
    onPageSelected: (Int) -> Unit,
    navigateAvatars: () -> Unit,
    navigatePlayer: () -> Unit
) {
    val pagerState = rememberPagerState { pages.size }
    subscribeToPagerUpdates(
        coroutineScope = rememberCoroutineScope(),
        pagerState = pagerState,
        page = currentPageIndex
    )
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> onPageSelected(page) }
    }
    HorizontalPager(
        modifier = Modifier.fillMaxWidth(1f).heightIn(min = 260.dp),
        state = pagerState,
        userScrollEnabled = true
    ) { position ->
        when (position) {
            0 -> PlayerPage(
                title = title,
                progress = 0f,
                playState = PlayState.LOADING,
                onPause = { },
                onPlay = { },
                onSeekTo = { }
            )

            else -> AvatarsPage()
        }
    }
}

private fun handleSideEffect(sideEffect: AudioPlayerSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}