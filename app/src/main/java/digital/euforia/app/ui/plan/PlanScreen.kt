@file:OptIn(ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.plan

import androidx.annotation.Keep
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import digital.euforia.app.ui.util.openSystemSettings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import digital.euforia.app.ui.plan.item.notificationItem
import digital.euforia.app.ui.plan.item.soundscapesItem
import digital.euforia.app.ui.plan.item.tasksItem
import digital.euforia.app.ui.plan.item.topProgramsItem
import digital.euforia.app.ui.plan.item.videoItem
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.settings.feedback.FeedbackBottomSheet
import digital.euforia.app.ui.settings.feedback.FeedbackRequestDialog
import digital.euforia.app.ui.settings.support.SupportBottomSheet
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.openAboutEuforia
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.MenuItem
import digital.euforia.app.ui.settings.support.SupportSideEffect
import digital.euforia.app.ui.settings.support.SupportViewModel
import digital.euforia.app.ui.util.widget.NotificationToast
import digital.euforia.app.ui.util.widget.NoConnectionView
import digital.euforia.app.ui.util.widget.OptionsMenu
import digital.euforia.app.ui.util.widget.EmailDialog
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.WeekSkipWarningDialog
import digital.euforia.app.ui.util.widget.ifTrue
import digital.euforia.app.ui.util.widget.noRippleClickable
import com.google.android.play.core.review.ReviewManagerFactory
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.hilt.navigation.compose.hiltViewModel
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.util.widget.WhiteAlphaOutlinedButton

@Composable
fun SharedTransitionScope.PlanScreen(
    navController: NavHostController,
    viewModel: PlanViewModel,
    navBarVisibilityState: MutableState<Boolean>,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val state by viewModel.collectAsState()
    val supportViewModel: SupportViewModel = hiltViewModel()
    var isSupportSheetVisible by remember { mutableStateOf(false) }
    var isSupportToastVisible by remember { mutableStateOf(false) }
    var supportToastMessageRes by remember { mutableStateOf(R.string.sent_success) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isPermissionGranted =
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
                viewModel.updateNotificationPermission(isPermissionGranted)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val hazeState = rememberHazeState()
    var isSkipWeekDialogVisible by remember { mutableStateOf(false) }
    var isFinishWeekDialogVisible by remember { mutableStateOf(false) }

    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(
            sideEffect = sideEffect,
            navController = navController,
            showFinishWeekDialog = {
                isSkipWeekDialogVisible = true
            },
            showFinishWeekCalendar = {
                isFinishWeekDialogVisible = true
            },
            onRateNow = {
                viewModel.onRateNow()
            },
            onRateDismissed = {
                viewModel.onRatePromptDismissed()
            }
        )
    }

    supportViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SupportSideEffect.ShowToast -> {
                supportToastMessageRes = sideEffect.messageRes
                isSupportToastVisible = true
            }

            is SupportSideEffect.CloseSheet -> {
                isSupportSheetVisible = false
            }
        }
    }

    val completedDailyTasks by viewModel.completedTask.collectAsStateWithLifecycle()
    val localizedRes = LocalLocalizedRes.current

    SubscriptionActivityLauncher { launchSubscriptionActivity ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                completedDailyTasks = completedDailyTasks,
                dailyTasks = state.dailyTasks,
                continuousDays = state.continuousDays,
                topPrograms = state.topPackages,
                bannerConfig = state.bannerConfig,
                extraPackage = state.extraPackage,
                navController = navController,
                animatedVisibilityScope = animatedVisibilityScope,
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
                launchSubscriptionActivity = launchSubscriptionActivity,
                onProgramClick = viewModel::onProgramClicked,
                onProgramsClick = viewModel::onProgramsClicked,
                isNotificationPermissionGranted = state.isNotificationPermissionGranted,
                showNotificationPermissionItem = state.showNotificationPermissionItem,
                onNotificationPermissionDismiss = viewModel::dismissNotificationPermissionItem,
                isSupportSheetVisible = isSupportSheetVisible,
                onSupportSheetVisibilityChange = { isSupportSheetVisible = it },
                supportViewModel = supportViewModel
            )

            if (state.showFeedbackRequestDialog) {
                FeedbackRequestDialog(
                    hazeState = hazeState,
                    onCancel = viewModel::onFeedbackRequestCancel,
                    onConfirm = viewModel::onFeedbackRequestConfirm
                )
            }

            if (state.showFeedbackBottomSheet) {
                FeedbackBottomSheet(
                    onDismiss = viewModel::onFeedbackBottomSheetDismiss
                )
            }

            if (state.showEmailAlert) {
                EmailDialog(
                    hazeState = hazeState,
                    onCancel = viewModel::onEmailAlertCancel,
                    onConfirm = viewModel::onEmailAlertConfirm
                )
            }

            NotificationToast(
                text = localizedRes.string(supportToastMessageRes),
                isVisible = isSupportToastVisible,
                onDismissed = { isSupportToastVisible = false }
            )

            if (state.showLockedDayPopup) {
                Box(Modifier.fillMaxSize().zIndex(2f)) {
                    LockedDayPopup(
                        hazeState = hazeState,
                        onClose = viewModel::onCloseLockedDayPopup
                    )
                }
            }

            AnimatedVisibility(
                visible = isFinishWeekDialogVisible,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f),
                enter = slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth },
                exit = slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth },
            ) {
                FinishWeekScreen(
                    navController = navController,
                    navBarVisibilityState = navBarVisibilityState,
                    onPremiumClick = {
                        launchSubscriptionActivity()
                    },
                    onBackClick = {
                        isFinishWeekDialogVisible = false
                    },
                )
            }
        }

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
    coverUrl: String?,
    todayOffset: TodayOffset,
//    networkAvailable: Boolean,
    isLoading: Boolean,
    isAccompanimentsLoading: Boolean,
    errorState: ErrorViewState?,
    isRefreshing: Boolean,
    analyticSender: AnalyticSender,
    isNotificationPermissionGranted: Boolean,
    showNotificationPermissionItem: Boolean,
    onRefresh: () -> Unit,
    launchSubscriptionActivity: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onDayTimeItemClick: (DayTimeItemUi) -> Unit,
    onSkipDemoClick: () -> Unit,
    onFinishWeekClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onProgramClick: (id: Int) -> Unit,
    onProgramsClick: () -> Unit,
    onNotificationPermissionDismiss: () -> Unit,
    isSupportSheetVisible: Boolean,
    onSupportSheetVisibilityChange: (Boolean) -> Unit,
    supportViewModel: SupportViewModel
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

    val context = LocalContext.current
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

    @OptIn(ExperimentalFoundationApi::class)
    Box(Modifier.fillMaxSize()) {
//        if (isLoading) {
//            ProgressIndicator(Modifier.align(Alignment.Center))
//        } else {
        CompositionLocalProvider(
            LocalOverscrollFactory provides null
        ) {
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
//                if (isAccompanimentsLoading) {
//                    loadingItem("acc_loading")
//                } else {
//                }
                item(key = PlanViewItems.DAYS, contentType = PlanViewItems.DAYS) {
                    dayItem(
                        modifier = Modifier,
                        isLoading = isAccompanimentsLoading,
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

                    notificationItem(
                        visible = !isNotificationPermissionGranted && showNotificationPermissionItem,
                        onClick = {
                            openSystemSettings(context)
                            onNotificationPermissionDismiss()
                        },
                        onCloseClick = onNotificationPermissionDismiss
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

                    extraItem(
                        extraPackage = extraPackage,
                        onClick = onProgramClick
                    )

                    topProgramsItem(
                        isDemo = isDemo,
                        topPrograms = topPrograms,
                        onProgramClick = onProgramClick,
                        onProgramsClick = onProgramsClick
                    )

                    soundscapesItem(
                        isDemo = isDemo,
                        isPremium = isPremium,
                        bannerConfig = bannerConfig,
                        onSoundscapesClick = {
                            analyticSender.todayScenesClick()
                            navController.navigate(HomeDestination.Soundscapes)
                        },
                        onBannerClick = {
                            launchSubscriptionActivity()
                        },
                        onFAQClick = {
                            analyticSender.todayQuestionClick()
                            navController.navigate(HomeDestination.FAQ)
                        },
                        onSupportClick = {
                            analyticSender.todaySupportClick()
                            onSupportSheetVisibilityChange(true)
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
            val localizedRes = LocalLocalizedRes.current
            SupportBottomSheet(
                title = localizedRes.string(R.string.feedback_support_title),
                subtitle = localizedRes.string(R.string.feedback_support_subtitle),
                viewModel = supportViewModel,
            ) {
                onSupportSheetVisibilityChange(false)
            }
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
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 260.dp + 112.dp)) {
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
    onRateNow: () -> Unit,
    onRateDismissed: () -> Unit,
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

        is PlanSideEffect.NavigateToProgramDetail -> {
            navController.navigate(HomeDestination.ProgramDetails(sideEffect.programId))
        }

        is PlanSideEffect.NavigateToPrograms -> navController.navigate(HomeDestination.Programs)

        is PlanSideEffect.ShowRateAppPrompt -> {
            val context = navController.context
            val manager = ReviewManagerFactory.create(context)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val activity = context.findActivity()
                    if (activity != null) {
                        val flow = manager.launchReviewFlow(activity, reviewInfo)
                        flow.addOnCompleteListener { _ ->
                            // The flow has finished. The API does not indicate whether the user
                            // reviewed or not, or even whether the review dialog was shown. Thus, no
                            // matter the result, we continue our app flow.
                            onRateNow()
                        }
                    } else {
                        onRateDismissed()
                    }
                } else {
                    onRateDismissed()
                }
            }
        }

        is PlanSideEffect.ShowFeedbackRequestDialog -> {
            // Handled via state in PlanScreen
        }
    }
}

@Composable
private fun LockedDayPopup(
    hazeState: HazeState,
    onClose: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    Box(
        modifier = Modifier.fillMaxSize()
            .noRippleClickable {
                // Consume clicks outside the popup content if desired, 
                // but usually we want to allow closing on OK button.
                // If we want to close on outside click:
                // onClose()
            }
            .hazeEffect(
                hazeState,
                style = HazeMaterials.regular(AppBarBackground)
            ).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .background(
                    color = NavBarBackground,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = localizedRes.string(R.string.vibes_unavailable_demo_alert_text),
                color = White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal)
            )
            WhiteAlphaOutlinedButton(
                modifier = Modifier,
                text = localizedRes.string(R.string.ok),
                onClick = onClose
            )
        }
    }
}

private fun android.content.Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}

@Keep
enum class PlanViewItems { DAYS, TASKS, EXTRA, STREAK, TOP, SOUNDSCAPES, SOS, VIDEO }