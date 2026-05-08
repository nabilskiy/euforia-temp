@file:OptIn(ExperimentalMaterial3Api::class)

package digital.euforia.app.ui.settings.maxInfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.domain.model.subscription.MaxInfo
import digital.euforia.app.domain.model.subscription.MaxInfoItem
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.service.soundscapes.beginSoundscapeInterruption
import digital.euforia.app.service.soundscapes.endSoundscapeInterruption
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.MaxGradient
import digital.euforia.app.ui.theme.MaxGradientReversed
import digital.euforia.app.ui.theme.MaxGradientReversedHorizontal
import digital.euforia.app.ui.theme.PremiumGradient
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.AnimatedSizeBox
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val ABOUT_PREMIUM_INTERRUPTION_TOKEN = "about_premium_video"

@Composable
fun AboutPremiumScreen(
    navController: NavHostController,
    viewModel: AboutPremiumViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    NavBarlessScreen(navBarVisibilityState) {
        AboutPremiumContent(
            info = state.maxInfo,
            navController = navController
        ) {
            navController.popBackStack()
        }
    }
}

@Composable
private fun AboutPremiumContent(
    navController: NavHostController,
    info: MaxInfo?,
    onBackClick: () -> Unit = { }
) {

    val listState = rememberLazyListState()
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 16.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf<MaxInfoItem?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            titleRes = R.string.active_subscription_default_name,
            shouldBlur = shouldBlur,
            hazeState = hazeState,
            onBackClick = onBackClick,
            navController = navController
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().hazeSource(hazeState),
            state = listState,
            verticalArrangement = Arrangement.Absolute.spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = AppBarHeightMedium + 16.dp,
                bottom = 56.dp
            ),
        ) {
            info?.let {
                titleItem(info)

                for (item in info.items) {
                    infoItem(infoItem = item, onClick = {
                        currentItem = item
                        isBottomSheetVisible = true
                    })
                }

                footerItem(info)
            }
        }
        if (isBottomSheetVisible) {
            AboutBottomSheet(
                infoItem = currentItem,
                isVisible = isBottomSheetVisible,
                onDismiss = {
                    isBottomSheetVisible = false
                    currentItem = null
                }
            )
        }
    }
}

private fun LazyListScope.infoItem(infoItem: MaxInfoItem, onClick: () -> Unit) = item {
    Row(
        modifier = Modifier
            .noRippleClickable(onClick).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(16.dp)
    ) {
        AsyncImage(
            model = infoItem.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(40.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = infoItem.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                color = White
            )
            Text(
                text = infoItem.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                color = DarkGray

            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_next),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }
}

private fun LazyListScope.titleItem(info: MaxInfo) = item {
    val localizedRes = LocalLocalizedRes.current
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.align(Alignment.Center).padding(top = 24.dp),
                text = localizedRes.string(R.string.active_subscription_default_name).uppercase(),
                style = MaterialTheme.typography.displaySmall.copy(
                    brush = PremiumGradient,
                    letterSpacing = (-0.6).sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                ),
                color = White
            )
        }
        Text(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
            text = info.details,
            style = MaterialTheme.typography.bodyMedium,
            color = White
        )
    }
}

private fun LazyListScope.footerItem(infoItem: MaxInfo) = item {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Divider()
        Text(
            text = infoItem.bottomCardTitle,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = SemiBold),
            color = White
        )
        Text(
            text = infoItem.bottomCardText,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkGray,
        )
    }
}


@Composable
fun AboutBottomSheet(
    infoItem: MaxInfoItem?,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    infoItem ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.padding(top = 32.dp).statusBarsPadding(),
        dragHandle = {},
        scrimColor = Color.Transparent,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = BottomSheetBackground,
        tonalElevation = 0.dp,
//        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {
        //exoplayer that fill max width with looped playback of infoItem.videoUrl and hidden controls 
        val context = androidx.compose.ui.platform.LocalContext.current
        val videoUrl = infoItem.videoUrl
        if (!videoUrl.isNullOrBlank()) {
            val exoPlayer = androidx.compose.runtime.remember(videoUrl) {
                androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                    repeatMode = androidx.media3.exoplayer.ExoPlayer.REPEAT_MODE_ALL
                    setMediaItem(androidx.media3.common.MediaItem.fromUri(videoUrl))
                    prepare()
                    beginSoundscapeInterruption(context, ABOUT_PREMIUM_INTERRUPTION_TOKEN)
                    playWhenReady = true
                }
            }

            androidx.compose.runtime.DisposableEffect(exoPlayer) {
                onDispose {
                    endSoundscapeInterruption(context, ABOUT_PREMIUM_INTERRUPTION_TOKEN)
                    exoPlayer.release()
                }
            }

            AndroidView(
                factory = { ctx ->
                    android.view.LayoutInflater.from(ctx)
                        .inflate(digital.euforia.app.R.layout.player_view_texture, null, false).also { root ->
                            root.findViewById<androidx.media3.ui.PlayerView>(digital.euforia.app.R.id.player_view).apply {
                                this.player = exoPlayer
                                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                                setKeepContentOnPlayerReset(true)
                                // Fill width and keep aspect without controls
                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                                useController = false
                                hideController()
                            }
                        }
                },
                update = { root ->
                    root.findViewById<androidx.media3.ui.PlayerView>(digital.euforia.app.R.id.player_view).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
//                    .height(220.dp)
            )
        }

        Text(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            textAlign = TextAlign.Center,
            text = infoItem.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = SemiBold),
            color = White
        )

        Text(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            text = infoItem.text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkGray,
        )

        AnimatedSizeBox(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 56.dp, vertical = 16.dp)
                .height(48.dp)
                .background(White, CircleShape),
            onClick = onDismiss
        ) {
            val localizedRes = LocalLocalizedRes.current
            Text(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
                text = localizedRes.string(R.string.ok),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = SemiBold,
                    brush = MaxGradientReversedHorizontal
                ),
            )
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .background(color = White.copy(alpha = 0.2f))
            .fillMaxWidth()
            .height(1.dp)
    )
}

private fun handleSideEffect(sideEffect: MaxInfoSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}