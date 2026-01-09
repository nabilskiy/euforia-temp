@file:OptIn(ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.plan

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.BannerConfig
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.plan.DailyTask
import digital.euforia.app.domain.model.plan.ExtraPackage
import digital.euforia.app.domain.model.plan.RankedPackage
import digital.euforia.app.ui.finishweek.FinishWeekScreen
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.plan.item.continuousItem
import digital.euforia.app.ui.plan.item.dayItem
import digital.euforia.app.ui.plan.item.extraItem
import digital.euforia.app.ui.plan.item.soundscapesItem
import digital.euforia.app.ui.plan.item.tasksItem
import digital.euforia.app.ui.plan.item.topProgramsItem
import digital.euforia.app.ui.plan.item.videoItem
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.settings.support.SupportBottomSheet
import digital.euforia.app.ui.subscription.UserActivity
import digital.euforia.app.ui.subscription.UserActivity.PURCHASE_SUCCESS
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.openAboutEuforia
import digital.euforia.app.ui.util.widget.CongratsPopup
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.MenuItem
import digital.euforia.app.ui.util.widget.NoConnectionView
import digital.euforia.app.ui.util.widget.OptionsMenu
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.PullToRefresh
import digital.euforia.app.ui.util.widget.WeekSkipWarningDialog
import digital.euforia.app.ui.util.widget.ifTrue
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import timber.log.Timber

@Composable
fun SharedTransitionScope.PlanScreen(
    navController: NavHostController,
    viewModel: PlanViewModel,
    navBarVisibilityState: MutableState<Boolean>,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val state by viewModel.collectAsState()
    val hazeState = rememberHazeState()
    var isSkipWeekDialogVisible by remember { mutableStateOf(false) }
    var isFinishWeekDialogVisible by remember { mutableStateOf(false) }
    val isCongratsVisible = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Timber.tag("PLAN_SCREEN").d("Activity result: $result")
        if (result.resultCode == PURCHASE_SUCCESS) {
            isCongratsVisible.value = true
        }
        // handle result here
    }
    val launchSubscriptionActivity: () -> Unit = {
        val intent = Intent(context, UserActivity::class.java)
        launcher.launch(intent)
    }
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(
            sideEffect, navController, showFinishWeekDialog = {
                isSkipWeekDialogVisible = true
            },
            showFinishWeekCalendar = {
                isFinishWeekDialogVisible = true
            })
    }

    PlanContent(
        hazeState = hazeState,
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
        navController = navController,
        animatedVisibilityScope = animatedVisibilityScope,
        isCongratsVisible = isCongratsVisible,
        launcher = launcher,
        launchSubscriptionActivity = launchSubscriptionActivity,
        coverUrl = state.todayVideoCoverUrl,
        todayOffset = state.todayOffset,
        isLoading = state.isLoading,
        isAccompanimentsLoading = state.isAccompanimentLoading,
        errorState = state.errorState,
        isRefreshing = state.isRefreshing,
        analyticSender = viewModel.analyticSender,
        onRefresh = viewModel::onRefresh,
        onDaySelected = viewModel::onDaySelected,
        onDayTimeItemClick = viewModel::onDayTimeItemClick,
        onSkipDemoClick = viewModel::onSkipDemo,
        onFinishWeekClick = viewModel::onFinishWeek,
        onRetryClick = viewModel::onRetryClicked,
        onDownloadClick = viewModel::onDownloadClicked,
    )

    if (isSkipWeekDialogVisible) {
        WeekSkipWarningDialog(
            hazeState = hazeState,

            onCancel = { isSkipWeekDialogVisible = false },
            onSkip = {
                isSkipWeekDialogVisible = false
                viewModel.onSkipDemo()
            },
        )
    }

    AnimatedVisibility(
        visible = isFinishWeekDialogVisible,
        enter = slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth },
        exit = slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth }
    ) {
        Box(Modifier.fillMaxSize().zIndex(1f)) {
            FinishWeekScreen(
                navController = navController,
                navBarVisibilityState = navBarVisibilityState,
                hazeState = hazeState,
                onPremiumClick = {
                    launchSubscriptionActivity()
                },
                onBackClick = {
                    isFinishWeekDialogVisible = false
                }
            )
        }
    }
    BackHandler {
        if (isFinishWeekDialogVisible) {
            // If FinishWeekScreen is shown, hide it instead of popping back stack
            isFinishWeekDialogVisible = false
        } else {
            navController.popBackStack()
        }
    }
}

@Composable
private fun SharedTransitionScope.PlanContent(
    hazeState: HazeState,
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
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isCongratsVisible: MutableState<Boolean>,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    coverUrl: String?,
    todayOffset: TodayOffset,
//    networkAvailable: Boolean,
    isLoading: Boolean,
    isAccompanimentsLoading: Boolean,
    errorState: ErrorViewState?,
    isRefreshing: Boolean,
    analyticSender: AnalyticSender,
    onRefresh: () -> Unit,
    launchSubscriptionActivity: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onDayTimeItemClick: (DayTimeItemUi) -> Unit,
    onSkipDemoClick: () -> Unit,
    onFinishWeekClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDownloadClick: () -> Unit,
) {
    val listState = rememberLazyListState()
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

    var isSupportSheetVisible by remember { mutableStateOf(false) }
//    val pullState = rememberPullToRefreshState()
//
//    if (pullState.distanceFraction >= 1f && !isRefreshing && !pullState.isAnimating) {
//        onRefresh()
//    }
//    LaunchedEffect(isRefreshing) {
//        if (!isRefreshing) {
//            pullState.animateToHidden()
//        }
//    }

    Box(Modifier.fillMaxSize()) {
//        if (isLoading) {
//            ProgressIndicator(Modifier.align(Alignment.Center))
//        } else {
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
            if (isAccompanimentsLoading) {
                loadingItem("acc_loading")
            } else {
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
                        todayOffset = todayOffset,
                        animatedVisibilityScope = animatedVisibilityScope,
                        analyticSender = analyticSender,
                        onPremiumClick = { launchSubscriptionActivity() },
                        onDaySelected = onDaySelected,
                        onDayTimeItemClick = onDayTimeItemClick
                    )
                }
            }
            if (isLoading && errorState == null) {
                loadingItem("general_loading")
            } else if (errorState != null) {
                errorItem(errorState, onRetryClick, onDownloadClick)
            } else {
                tasksItem(
                    isDemo = isDemo,
                    isPremium = isPremium,
                    tasks = dailyTasks,
                    completedTasks = completedDailyTasks,
                    onPremiumClick = {
                        analyticSender.todayBannerClick("0")
                        launchSubscriptionActivity()
                    }
                )
                if (!isDemo) {
                    continuousItem(days = continuousDays)
                }

                videoItem(
                    url = coverUrl,
                    onClick = {
                        analyticSender.todayVideoClick()
                        navController.navigate(HomeDestination.HowItWorks)
                    }
                )

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
                    onBannerClick = {
                        launchSubscriptionActivity()
                    },
                    onFAQClick = {
                        analyticSender.todayQuestionClick()
                        navController.navigate(HomeDestination.FAQ)
                    },
                    onSupportClick = {
                        analyticSender.todaySupportClick()
                        isSupportSheetVisible = true
                    },
                    onSOSClick = {
                        analyticSender.todaySosClick()
                        navController.navigate(HomeDestination.Emergency)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.fillMaxWidth().height(200.dp))
            }
        }
        AppBar(
            isDemo = isDemo,
            shouldBlur = shouldBlur,
            hazeState = hazeState,
            onFinishWeekClick = {
                analyticSender.todayMenuEndTrialClick()
                onFinishWeekClick()
            },
            onFirstWeekClick = {
                analyticSender.todayMenuDemoClick()
                navController.navigate(HomeDestination.FirstWeek)
            },
            onActionsClick = {},
            onHowItWorksClick = {
                analyticSender.todayMenuVideoClick()
                navController.navigate(HomeDestination.HowItWorks)
            },
        )

        if (isSupportSheetVisible) {
            SupportBottomSheet() {
                isSupportSheetVisible = false
            }
        }

        if (isCongratsVisible.value) {
            CongratsPopup() { isCongratsVisible.value = false }
        }
    }
}

private fun LazyListScope.errorItem(
    errorState: ErrorViewState,
    onRetryClick: () -> Unit,
    onDownloadClick: () -> Unit
) = item(key = "ERROR_ITEM", contentType = "ERROR_ITEM") {
    ErrorView(
        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
        state = errorState,
        onRetryClick = onRetryClick,
        onDownloadsClick = onDownloadClick
    )
}

@Composable
private fun BoxScope.AppBar(
    isDemo: Boolean = false,
    shouldBlur: Boolean, hazeState: HazeState, onActionsClick: () -> Unit,
    onFinishWeekClick: () -> Unit = {},
    onFirstWeekClick: () -> Unit = {},
    onHowItWorksClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val localizedRes = LocalLocalizedRes.current

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
                    text = localizedRes.string(R.string.today_title),
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
                    if (isDemo) {
                        MenuItem(
                            titleRes = R.string.today_menu_skip_intro,
                            onClick = onFinishWeekClick
                        )
                    } else {
                        MenuItem(
                            titleRes = R.string.today_menu_intro,
                            onClick = onFirstWeekClick
                        )
                    },
                    MenuItem(
                        titleRes = R.string.today_menu_info,
                        onClick = onHowItWorksClick
                    ),
                    MenuItem(
                        titleRes = R.string.today_menu_about,
                        onClick = { openAboutEuforia(context) }
                    )
                )
            )
        }
    }
}

private fun LazyListScope.noNetworkItem(
    onRetryClick: () -> Unit,
    onDownloadClick: () -> Unit
) = item(key = "NO_NETWORK", contentType = "NO_NETWORK") {
    NoConnectionView(
        modifier = Modifier.padding(top = 64.dp),
        onRetryClick = onRetryClick,
        onDownloadClick = onDownloadClick
    )
}

private fun LazyListScope.loadingItem(key: String) = item(key = key, contentType = "LOADING") {
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp)) {
        ProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

private fun handleSideEffect(
    sideEffect: PlanSideEffect,
    navController: NavHostController,
    showFinishWeekDialog: () -> Unit,
    showFinishWeekCalendar: () -> Unit,
) {
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

        is PlanSideEffect.ShowSkipWeekDialog -> {
            showFinishWeekDialog()
        }

        is PlanSideEffect.NavigateFinishWeekScreen -> {
            showFinishWeekCalendar()
//            navController.navigate(HomeDestination.FinishWeek)
        }

        is PlanSideEffect.NavigateDownloads -> {
            navController.navigate(HomeDestination.Downloads)
        }

        else -> {}
    }
}

@Keep
enum class PlanViewItems { DAYS, TASKS, EXTRA, STREAK, TOP, SOUNDSCAPES, SOS, VIDEO }