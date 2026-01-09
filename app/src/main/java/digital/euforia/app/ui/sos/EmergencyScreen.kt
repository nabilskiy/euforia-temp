package digital.euforia.app.ui.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.domain.model.subscription.MaxInfoItem
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.plan.item.videoItem
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.highlight
import digital.euforia.app.ui.util.normal
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.titleItem
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun EmergencyScreen(
    navController: NavHostController,
    viewModel: EmergencyViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    NavBarlessScreen(navBarVisibilityState) {
        SosContent(
            videoUiList = state.videos,
            navController = navController,
            analyticSender = viewModel.analyticSender
        )
    }
}

@Composable
private fun SosContent(
    videoUiList: List<EmergencyVideoUi>,
    navController: NavHostController,
    analyticSender: AnalyticSender
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
    var currentVideoUi by remember { mutableStateOf<EmergencyVideoUi?>(null) }

    Box() {
        BlurredAppBar(
            titleRes = R.string.sos_title,
            shouldBlur = shouldBlur,
            hazeState = hazeState,
            backTitleRes = R.string.today_title,
            onBackClick = { navController.popBackStack() },
            navController = navController
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().hazeSource(hazeState),
            state = listState,
            verticalArrangement = Arrangement.Absolute.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = AppBarHeightMedium + 16.dp,
                bottom = 56.dp
            ),
        ) {
            titleItem(R.string.sos_title)
            infoTextBlock()
            videoUiList.forEachIndexed { index, videoUi ->
                videoItem(
                    videoUi = videoUi,
                    onClick = {
                        if (index == 0) {
                            analyticSender.sosOption1Click()
                        } else {
                            analyticSender.sosOption2Click()
                        }
                        currentVideoUi = videoUi
                        isBottomSheetVisible = true
                    }
                )
            }
            callItem(
                onClick = {
                    analyticSender.sosCallClick()
                    navController.navigate(HomeDestination.EmergencyContacts)
                })
        }

        if (isBottomSheetVisible && currentVideoUi != null) {
            EmergencyBottomSheet(
                videoUi = currentVideoUi!!,
                onDismiss = {
                    isBottomSheetVisible = false
                    currentVideoUi = null
                }
            )
        }
    }
}

fun LazyListScope.callItem(onClick: () -> Unit) = item(key = "call_item") {
    val localizedRes = LocalLocalizedRes.current
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        color = White.copy(alpha = 0.2f),
    )
    Text(
        text = localizedRes.string(R.string.sos_call_text),
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = SemiBold),
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        textAlign = TextAlign.Center,
        color = White
    )

    FilledTonalButton(
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = White,
            contentColor = Black
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.navigationBarsPadding().padding(vertical = 24.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_phone),
                contentDescription = null,
                tint = Color.Unspecified,
            )
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = localizedRes.string(R.string.sos_call_button),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                color = Black
            )
        }
    }
}

fun LazyListScope.infoTextBlock() = item(key = "info_text_block") {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // Paragraph 1
        Text(
            text = buildAnnotatedString {
                normal(localizedRes.string(R.string.mental_text_part_1))
                append(" ")
                highlight(localizedRes.string(R.string.mental_text_highlight_1))
                append(" ")
                normal(localizedRes.string(R.string.mental_text_part_2))
            },
            style = MaterialTheme.typography.bodyLarge
        )

        // Paragraph 2
        Text(
            text = buildAnnotatedString {
                normal(localizedRes.string(R.string.mental_text_part_3))
                append(" ")
                highlight(localizedRes.string(R.string.mental_text_highlight_2))
                append(" ")
                normal(localizedRes.string(R.string.mental_text_part_4))
            },
            style = MaterialTheme.typography.bodyLarge
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            color = White.copy(alpha = 0.2f)
        )
    }
}

fun LazyListScope.videoItem(
    videoUi: EmergencyVideoUi,
    onClick: () -> Unit
) = item(key = videoUi.titleRes) {
    EmergencyVideoPreview(
        videoUi = videoUi,
        onClick = onClick
    )
}

@Composable
fun EmergencyVideoPreview(videoUi: EmergencyVideoUi, onClick: () -> Unit) {
    val localizedRes = LocalLocalizedRes.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .noRippleClickable { onClick() }
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = videoUi.bgRes,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = localizedRes.string(videoUi.titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    color = White
                )
                Text(
                    text = localizedRes.string(videoUi.durationRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = White.copy(alpha = 0.6f)
                )
            }

            Icon(
                modifier = Modifier.align(Alignment.Bottom)
                    .background(color = videoUi.iconBgColor, shape = CircleShape)
                    .padding(12.dp).size(16.dp),
                painter = painterResource(id = R.drawable.ic_play),
                contentDescription = null,
                tint = White,
            )
        }
    }
}

private fun handleSideEffect(sideEffect: EmergencySideEffect) {
    when (sideEffect) {
        else -> {}
    }
}