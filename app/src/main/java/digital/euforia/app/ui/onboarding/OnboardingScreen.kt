package digital.euforia.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.Home
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.Onboarding
import digital.euforia.app.ui.onboarding.pager.PagerPage
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.LocalizedResources
import digital.euforia.app.ui.util.rememberImeState
import digital.euforia.app.ui.util.widget.AnimatedSizeButton
import digital.euforia.app.ui.util.widget.AnimatedVerticalShrink
import digital.euforia.app.ui.util.widget.DelayedVisibilityAnimation
import digital.euforia.app.ui.util.widget.TermsAndPrivacyText
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val ONBOARDING_ANIMATION_DURATION = 2500

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(navController, sideEffect)
    }
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = ONBOARDING_ANIMATION_DURATION))
    ) {
        OnboardingContent(
            viewModel = viewModel,
            state = state,
            onBack = { navController.popBackStack() }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun OnboardingContent(
    viewModel: OnboardingViewModel,
    state: OnboardingState,
    onBack: () -> Unit,
) {
    val pagerState = rememberPagerState { state.pages.size }
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    var notificationPageExcluded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(permission?.status) {
        permission?.status?.isGranted?.let { isGranted ->
            if (isGranted) viewModel.onNotificationPermissionGranted()
        }
    }
    subscribeToPagerUpdates(
        coroutineScope = rememberCoroutineScope(),
        pagerState = pagerState,
        page = state.currentPage.position
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> viewModel.onPageUpdated(page) }
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeState by rememberImeState()
    val clearInputFocus = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }
    BackHandler {
        clearInputFocus()
        if (state.currentPage.pageType.isBackAllowed) {
            viewModel.onPreviousPage()
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize().noRippleClickable {
                clearInputFocus()
            }
    ) {
        DelayedVisibilityAnimation(
            triggerCondition = true,
            durationMillis = ONBOARDING_ANIMATION_DURATION,
            modifier = Modifier.fillMaxSize()
        ) {
            // Pager content
            Column {
                AppBar(
                    page = state.currentPage.pageType,
                    interestsCount = state.selectedInterests.size,
                    onBackClick = {
                        clearInputFocus()
                        viewModel.onPreviousPage()
                    },
                    onSkipClick = {
                        viewModel.onSkipPage()
                        clearInputFocus()
                    },
                )
                HorizontalPager(
                    modifier = Modifier.weight(1f),
                    state = pagerState,
                    userScrollEnabled = false
                ) { position ->
                    PagerPage(viewModel, state, position, focusManager, keyboardController)
                }
            }
            FooterView(
                isTermsShown = state.currentPage.position == 0,
                currentPage = state.currentPage,
                isButtonEnabled = state.isNextEnabled,
                isButtonVisible = state.isNextButtonVisible,
                onNextClick = {
                    clearInputFocus()

                    val currentPage = viewModel.container.stateFlow.value.currentPage
                    if (currentPage.pageType == OnboardingPage.NotificationsPage) {
                        requestNotificationPermission(
                            state, permission,
                            onPermissionRequested = {
//                                requestedOnce = true
                            },
                            onGranted = {
                                viewModel.onNotificationPermissionGranted()
                                clearInputFocus()
                                viewModel.onNextPage()
                            }
                        )
                    }/* else if (currentPage.pageType == OnboardingPage.InterestsPage) {
                        permission?.status?.isGranted?.let { isGranted ->
//                            viewModel.onNextPage(skipPage = isGranted)
                            notificationPageExcluded = true
                            viewModel.onNextPage()
                        }
                    }*/ else {
                        viewModel.onNextPage()
                    }
                },
                onPrivacyClick = { viewModel.onPrivacyClicked() },
                onTermsClick = { viewModel.onTermsClicked() },
            )
        }
    }
}

@Composable
private fun AppBar(
    page: OnboardingPage,
    interestsCount: Int = 0,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current

    Column(
        modifier = Modifier.statusBarsPadding().padding(start = 16.dp, top = 16.dp, end = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(44.dp),
        ) {
            if (page.isBackAllowed) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(24.dp)
                        .clickable(onClick = onBackClick),
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
            if (page.isSkippable) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(onClick = onSkipClick),
                    text = localizedRes.string(R.string.skip),
                    color = White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        page.titleRes?.let {
            Text(
                modifier = Modifier.animateContentSize().padding(top = 10.dp),
                color = Color.White,
                text = localizedRes.string(it),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
        }

        var isSubtitleVisible by rememberSaveable { mutableStateOf(true) }
        page.subtitleRes?.let {
            val subtitleText = if (page !is OnboardingPage.InterestsPage) {
                localizedRes.string(it)
            } else {
                getInterestsCountText(it, interestsCount, localizedRes)
            }
            isSubtitleVisible = !subtitleText.isNullOrEmpty()

            AnimatedVisibility(
                visible = isSubtitleVisible,
                enter = expandVertically(animationSpec = tween(durationMillis = 600)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 600),
                    shrinkTowards = Alignment.Top
                )
            ) {
//                subtitleText?.let { text ->
                Text(
                    modifier = Modifier.animateContentSize().padding(top = 10.dp),
                    color = Color.White.copy(alpha = 0.4f),
                    text = subtitleText.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (page.titleRes != null || page.subtitleRes != null) {
            Box(
                modifier = Modifier.padding(top = 30.dp).size(width = 60.dp, height = 1.dp)
                    .background(White.copy(0.2f))
            )
        }
    }
}

@Composable
private fun BoxScope.FooterView(
    modifier: Modifier = Modifier,
    isTermsShown: Boolean = true,
    currentPage: CurrentPage,
    isButtonEnabled: Boolean,
    isButtonVisible: Boolean,
    onNextClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
) {
    val localizedResources = LocalLocalizedRes.current
//    var requestedOnce by remember { mutableStateOf(false) }
//    var shouldRequestPermission by remember { mutableStateOf(currentPage.pageType == OnboardingPage.NotificationsPage && !requestedOnce) }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 16.dp, vertical = 30.dp)
            .fillMaxWidth()
            .imePadding(),
//        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        AnimatedSizeButton(
            text = localizedResources.string(R.string.intro_next),
            isEnabled = isButtonEnabled,
            isVisible = isButtonVisible,
            onClick = {
//                isTermsShown
//                if (shouldRequestPermission) {
//                    // Request permission first
//                    requestedOnce = true
//                } else {
                onNextClick()
//                }
            }
        )
        TermsText(
            isTermsShown = isTermsShown,
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )
    }

//    if (requestedOnce) {
//        NotificationPermissionRequest(
//            onGranted = { onNextClick() }
//        )
//    }
}

@Composable
private fun TermsText(
    isTermsShown: Boolean,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    val localizedResources = LocalLocalizedRes.current
    AnimatedVerticalShrink(isTermsShown) {
        TermsAndPrivacyText(
            modifier = Modifier.padding(top = 30.dp),
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )
//        Text(
//            modifier = Modifier.padding(top = 30.dp),
//            text = localizedResources.string(R.string.intro_terms),
//            style = MaterialTheme.typography.labelMedium,
//            color = White.copy(0.7f),
//            textAlign = TextAlign.Center,
//        )
    }
}

private fun getInterestsCountText(
    textRes: Int,
    count: Int,
    localizedRes: LocalizedResources
): String? {
    return if (count == 0) null
    else localizedRes.string(textRes, count, 3)
}

private fun subscribeToPagerUpdates(
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    page: Int
) {
    coroutineScope.launch {
        pagerState.animateScrollToPage(page)
    }
}

private fun handleSideEffect(
    navController: NavHostController,
    sideEffect: OnboardingSideEffect,
) {
    when (sideEffect) {
        OnboardingSideEffect.NavigateHome -> {
            navController.navigate(Home) {
                popUpTo(Onboarding) { inclusive = true }
            }
        }

        is OnboardingSideEffect.NavigateAudioPlayer -> {
            navController.navigate(
                HomeDestination.AudioPlayer(
                    accompanimentId = sideEffect.accompanimentId,
                    timeOfDay = sideEffect.timeOfDay,
                    entryPoint = AudioPlayerEntryPoint.ONBOARDING,
                )
            ) {
                popUpTo(Onboarding) { inclusive = true }
            }
        }


        else -> {}
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun requestNotificationPermission(
    state: OnboardingState,
    permissionState: com.google.accompanist.permissions.PermissionState?,
    onPermissionRequested: () -> Unit,
    onGranted: () -> Unit
) {
    if (permissionState == null) {
        onGranted()
    } else if (!permissionState.status.isGranted) {
        permissionState.launchPermissionRequest()
        onPermissionRequested()
    } else if (permissionState.status.shouldShowRationale) {
        onGranted()
    } else if (permissionState.status.isGranted) {
        onGranted()
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    EuforiaTheme {
//        OnboardingContent(pagesSize = 3, position = 0, onPageUpdated = {})
    }
}
