package digital.euforia.app.ui.onboardingV3

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.OnboardingV3
import digital.euforia.app.ui.onboardingV3.pager.PagerPage
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.AnimatedSizeButton
import digital.euforia.app.ui.util.widget.AnimatedVerticalShrink
import digital.euforia.app.ui.util.widget.TermsAndPrivacyText
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun OnboardingV3Screen(
    navController: NavHostController,
    viewModel: OnboardingV3ViewModel,
) {
    val state by viewModel.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.onPause()
                Lifecycle.Event.ON_RESUME -> viewModel.onResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is OnboardingV3SideEffect.NavigateAudioPlayer -> {
                navController.navigate(
                    HomeDestination.AudioPlayer(
                        accompanimentId = effect.accompanimentId,
                        timeOfDay = effect.timeOfDay,
                        entryPoint = AudioPlayerEntryPoint.ONBOARDING,
                    )
                ) {
                    popUpTo(OnboardingV3) { inclusive = true }
                }
            }
        }
    }

    OnboardingV3Content(viewModel = viewModel, state = state)
}

@Composable
private fun OnboardingV3Content(
    viewModel: OnboardingV3ViewModel,
    state: OnboardingV3State,
) {
    val pagerState = rememberPagerState { state.pages.size.coerceAtLeast(1) }
    val localizedRes = LocalLocalizedRes.current

    LaunchedEffect(state.currentPage.position) {
        pagerState.animateScrollToPage(state.currentPage.position)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onPageUpdated(page)
        }
    }

    BackHandler {
        if (state.currentPage.pageType.isBackAllowed) {
            viewModel.onPreviousPage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .noRippleClickable { },
    ) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            userScrollEnabled = false,
        ) { position ->
            PagerPage(viewModel, state, position)
        }

        if (state.isShellChromeVisible) {
            V3AppBar(
                page = state.currentPage.pageType,
                title = state.currentPage.pageType.topBarTitleRes?.let { localizedRes.string(it) },
                onBackClick = viewModel::onPreviousPage,
                onSkipClick = viewModel::onSkipPage,
            )
        }

        if (state.isNextButtonVisible) {
            V3Footer(
                isTermsShown = state.pages.indexOfFirst { it.isQuestion }.let { firstQuestionIndex ->
                    firstQuestionIndex >= 0 && state.currentPage.position == firstQuestionIndex
                },
                isButtonEnabled = state.isNextEnabled,
                onNextClick = viewModel::onNextPage,
                onPrivacyClick = viewModel::onPrivacyClicked,
                onTermsClick = viewModel::onTermsClicked,
            )
        }
    }
}

private val OnboardingV3Page.topBarTitleRes: Int?
    get() = when (this) {
        OnboardingV3Page.About1Page,
        OnboardingV3Page.About2Page,
        OnboardingV3Page.About3Page -> null

        else -> titleRes
    }

@Composable
private fun V3AppBar(
    page: OnboardingV3Page,
    title: String?,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
        ) {
            if (page.isBackAllowed) {
                var isPressed by remember { mutableStateOf(false) }
                val color by animateColorAsState(
                    targetValue = if (isPressed) White.copy(alpha = 0.3f) else White,
                    animationSpec = tween(200),
                    label = "backColor",
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(24.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                },
                                onTap = { onBackClick() },
                            )
                        },
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = color,
                )
            }
            if (page.isSkippable) {
                var isPressed by remember { mutableStateOf(false) }
                val color by animateColorAsState(
                    targetValue = if (isPressed) White.copy(alpha = 0.3f) else White.copy(alpha = 0.6f),
                    animationSpec = tween(200),
                    label = "skipColor",
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                },
                                onTap = { onSkipClick() },
                            )
                        },
                    text = LocalLocalizedRes.current.string(R.string.skip),
                    color = color,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        title?.let {
            Text(
                text = it,
                color = White,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BoxScope.V3Footer(
    isTermsShown: Boolean,
    isButtonEnabled: Boolean,
    onNextClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 30.dp)
            .fillMaxWidth()
            .imePadding(),
    ) {
        AnimatedSizeButton(
            text = LocalLocalizedRes.current.string(R.string.intro_next),
            isEnabled = isButtonEnabled,
            isVisible = true,
            onClick = onNextClick,
        )
        AnimatedVerticalShrink(isTermsShown) {
            TermsAndPrivacyText(
                modifier = Modifier.padding(top = 30.dp),
                onTermsClick = onTermsClick,
                onPrivacyClick = onPrivacyClick,
            )
        }
    }
}
