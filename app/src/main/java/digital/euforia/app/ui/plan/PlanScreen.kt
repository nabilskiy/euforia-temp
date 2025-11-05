@file:OptIn(ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.plan

import androidx.annotation.Keep
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.BannerConfig
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.plan.DailyTask
import digital.euforia.app.domain.model.plan.ExtraPackage
import digital.euforia.app.domain.model.plan.RankedPackage
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.plan.item.continuousItem
import digital.euforia.app.ui.plan.item.dayItem
import digital.euforia.app.ui.plan.item.extraItem
import digital.euforia.app.ui.plan.item.soundscapesItem
import digital.euforia.app.ui.plan.item.tasksItem
import digital.euforia.app.ui.plan.item.topProgramsItem
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.widget.MenuItem
import digital.euforia.app.ui.util.widget.OptionsMenu
import digital.euforia.app.ui.util.widget.ifTrue
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SharedTransitionScope.PlanScreen(
    navController: NavHostController,
    viewModel: PlanViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    PlanContent(
        days = state.days,
        completedDays = state.completedDays,
        selectedDayIndex = state.selectedDayIndex,
        freeDemoDays = state.freeDemoDays,
        isPremium = state.isPremium,
        isDemo = state.isDemo,
        timeOfDay = state.timeOfDay,
        timeOfDayConfig = state.timeOfDayConfig,
        completedDailyTasks = state.completedDailyTasks,
        dailyTasks = state.dailyTasks,
        continuousDays = state.continuousDays,
        topPrograms = state.topPackages,
        bannerConfig = state.bannerConfig,
        extraPackage = state.extraPackage,
        animatedVisibilityScope = animatedVisibilityScope,
        onDaySelected = viewModel::onDaySelected,
        onDayTimeItemClick = viewModel::onDayTimeItemClick
    )
}

@Composable
private fun SharedTransitionScope.PlanContent(
    days: List<DayUi>,
    completedDays: Int,
    selectedDayIndex: Int,
    freeDemoDays: Int,
    isPremium: Boolean,
    isDemo: Boolean,
    timeOfDay: TimeOfDay,
    timeOfDayConfig: TimeOfDayConfig,
    completedDailyTasks: Int,
    dailyTasks: List<DailyTask>,
    continuousDays: Int,
    topPrograms: List<RankedPackage>,
    bannerConfig: BannerConfig?,
    extraPackage: ExtraPackage?,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onDaySelected: (Int) -> Unit,
    onDayTimeItemClick: (DayTimeItemUi) -> Unit
) {
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    val statusBarPadding = WindowInsets.statusBars
        .only(WindowInsetsSides.Top)
        .asPaddingValues().calculateTopPadding()
    val density = LocalDensity.current
    val topThresholdPx = with(density) { (16.dp).roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            // Blur only after the first item is fully scrolled off (i.e., index of first visible item is 1 or more)
            firstIndex >= 1
        }
    }
    Box {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(color = PrimaryBackground)
                .hazeSource(hazeState),
            state = listState,
            contentPadding = PaddingValues(
                start = 0.dp,
                end = 0.dp,
                top = AppBarHeightMedium,
                bottom = 16.dp
            ),
        ) {
            item(key = PlanViewItems.DAYS, contentType = PlanViewItems.DAYS) {
                dayItem(
                    days = days,
                    completedDays = completedDays,
                    freeDemoDays = freeDemoDays,
                    isPremium = isPremium,
                    isDemo = isDemo,
                    selectedDayIndex = selectedDayIndex,
                    timeOfDay = timeOfDay,
                    timeOfDayConfig = timeOfDayConfig,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onDaySelected = onDaySelected,
                    onDayTimeItemClick = onDayTimeItemClick
                )
            }
            tasksItem(
                isDemo = isDemo,
                tasks = dailyTasks,
                completedTasks = completedDailyTasks
            )
            if (!isDemo) {
                continuousItem(days = continuousDays)
            }
            extraItem(extraPackage = extraPackage) {}
            topProgramsItem(
                isDemo = isDemo,
                topPrograms = topPrograms
            )
            soundscapesItem(
                isDemo = isDemo,
                isPremium = isPremium,
                bannerConfig = bannerConfig,
                onSoundscapesClick = {},
                onBannerClick = {},
                onFAQClick = {},
                onSupportClick = {},
                onSOSClick = {}

            )
            item {
                Spacer(modifier = Modifier.fillMaxWidth().height(200.dp))
            }
        }
        AppBar(
            shouldBlur = shouldBlur,
            hazeState = hazeState,
            onActionsClick = {}
        )
    }
}

@Composable
private fun BoxScope.AppBar(
    shouldBlur: Boolean, hazeState: HazeState, onActionsClick: () -> Unit,
    onFinishWeekClick: () -> Unit = {},
    onHowItWorksClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .zIndex(1f)
            .ifTrue(shouldBlur) {
                hazeEffect(
                    hazeState,
                    style = HazeMaterials.regular(AppBarBackground)
                )
            }
            .noRippleClickable { }
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .align(Alignment.TopCenter)
            .heightIn(min = AppBarHeightMedium)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(24.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (shouldBlur) {
                Text(
                    text = stringResource(R.string.today_title),
                    color = White,
                    style = appbarMedium
                )
            }
        }

        Box {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .noRippleClickable {
                        expanded = !expanded
                        onActionsClick()
                    },
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = null,
                tint = Color.Unspecified
            )
            OptionsMenu(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                menuItems = listOf(
                    MenuItem(
                        titleRes = R.string.today_menu_skip_intro,
                        onClick = onFinishWeekClick
                    ),
                    MenuItem(
                        titleRes = R.string.today_menu_info,
                        onClick = onHowItWorksClick
                    ),
                    MenuItem(
                        titleRes = R.string.today_menu_about,
                        onClick = onAboutClick
                    )
                )
            )
        }
    }
}

private fun handleSideEffect(sideEffect: PlanSideEffect, navController: NavHostController) {
    when (sideEffect) {
        is PlanSideEffect.NavigateAudioPlayer -> {
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

@Keep
enum class PlanViewItems { DAYS, TASKS, EXTRA, STREAK, TOP, SOUNDSCAPES, SOS }