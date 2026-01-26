package digital.euforia.app.ui.plan.firstweek

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.plan.DayTimeItemUi
import digital.euforia.app.ui.plan.DayUi
import digital.euforia.app.ui.plan.item.AccompanimentButton
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.subscription.UserActivity
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.DayBlue
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.AccompanimentButtonDimensions
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.titleItem
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import timber.log.Timber
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3

@Composable
fun FirstWeekScreen(
    navController: NavHostController,
    viewModel: FirstWeekViewModel,
    navBarVisibilityState: MutableState<Boolean>
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }
    NavBarlessScreen(navBarVisibilityState) {
        FirstWeekContent(
            days = state.days,
            timeOfDayConfig = state.timeOfDayConfig,
            onClick = viewModel::onDayTimeItemClick,
            onBackClick = { navController.popBackStack() },
            navController = navController
        )
    }
}

private fun LazyListScope.dayItem(
    day: DayUi,
    index: Int,
    timeOfDayConfig: TimeOfDayConfig,
    onClick: (DayTimeItemUi) -> Unit
) {
    item(key = day.accompaniment?.id) {
        val (first, second, third) = day.items

        if (day.items.size == 3) {
            Box() {
                VerticalDashedLine(
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 4.dp)
                        .height(320.dp)
                )
                Box(
                    modifier = Modifier.align(Alignment.TopStart)
                        .padding(top = 8.dp).size(8.dp)
                        .background(color = DayBlue, shape = CircleShape)
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, bottom = 12.dp),
                    verticalArrangement = spacedBy(8.dp)
                ) {
                    Text(
                        text = LocalLocalizedRes.current.string(R.string.vibes_title_demo_date, index + 1),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = Bold
                        ),
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                        color = White
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                            .height(260.dp),
                        horizontalArrangement = spacedBy(8.dp)
                    ) {
                        AccompanimentButton(
                            modifier = Modifier.fillMaxHeight().weight(1f),
                            accompaniment = day.accompaniment ?: return@item,
                            item = first,
                            timeOfDayConfig = timeOfDayConfig,
                            buttonDimensions = AccompanimentButtonDimensions.PRIMARY,
                            onClick = onClick
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = spacedBy(8.dp)
                        ) {
                            AccompanimentButton(
                                modifier = Modifier.fillMaxHeight().weight(1f),
                                accompaniment = day.accompaniment,
                                item = second,
                                timeOfDayConfig = timeOfDayConfig,
                                onClick = onClick
                            )
                            AccompanimentButton(
                                modifier = Modifier.fillMaxHeight().weight(1f),
                                accompaniment = day.accompaniment,
                                item = third,
                                timeOfDayConfig = timeOfDayConfig,
                                onClick = onClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalDashedLine(
    modifier: Modifier = Modifier,
    color: Color = DarkGray,
    strokeWidth: Dp = 2.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp
) {
    Canvas(modifier = modifier) {
        drawLine(
            color = color,
            start = Offset(size.width / 2f, 0f),
            end = Offset(size.width / 2f, size.height),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(
                    dashLength.toPx(),
                    gapLength.toPx()
                )
            )
        )
    }
}

@Composable
private fun FirstWeekContent(
    navController: NavHostController,
    days: List<DayUi>,
    timeOfDayConfig: TimeOfDayConfig,
    onClick: (DayTimeItemUi) -> Unit,
    onBackClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    val context = LocalContext.current
    val thresholdPx = with(density) { 16.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            // Blur when the very first list item (spacer) scrolled off enough
            // or when any next item became the first visible one.
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }
    var isFeedbackSheetVisible by remember { mutableStateOf(false) }
    var isSupportSheetVisible by remember { mutableStateOf(false) }
    SubscriptionActivityLauncher { launchSubscriptionActivity ->
        Box() {
            BlurredAppBar(
                titleRes = R.string.today_menu_intro,
                hazeState = hazeState,
                shouldBlur = shouldBlur,
                onUpgradeClick = {
                    launchSubscriptionActivity()
                },
                isBackAllowed = true,
                onBackClick = onBackClick,
                navController = navController
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = AppBarHeightMedium,
                    bottom = 56.dp
                ),
            ) {
                titleItem(R.string.today_menu_intro)
                item {
                    Text(
                        text = LocalLocalizedRes.current.string(R.string.vibes_demo_period_header_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                days.forEachIndexed { index, day ->
                    dayItem(
                        day = day,
                        index = index,
                        timeOfDayConfig = timeOfDayConfig,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

private fun handleSideEffect(sideEffect: FirstWeekSideEffect, navController: NavHostController) {
    when (sideEffect) {
        is FirstWeekSideEffect.NavigateAudioPlayer -> {
            navController.navigate(
                HomeDestination.AudioPlayer(
                    accompanimentId = sideEffect.accompanimentId,
                    timeOfDay = sideEffect.timeOfDay,
                    entryPoint = AudioPlayerEntryPoint.DAY
                )
            )
        }

        else -> {}
    }
}