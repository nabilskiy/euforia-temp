package digital.euforia.app.ui.onboardingV3

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.OnboardingV3
import digital.euforia.app.ui.onboardingV3.pager.PagerPage
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.theme.ButtonDisabled
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.AnimatedVerticalShrink
import digital.euforia.app.ui.util.widget.TermsAndPrivacyText
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val INTRO_V3_START_EXIT_MS = 1_500
private const val INTRO_V3_CONTENT_FADE_MS = 300

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
    val localizedRes = LocalLocalizedRes.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) viewModel.onNotificationPermissionGranted()
        viewModel.onNextPage()
    }

    LaunchedEffect(state.currentPage.position) {
        viewModel.onPageUpdated(state.currentPage.position)
    }

    BackHandler {
        if (state.currentPage.pageType.isBackAllowed) {
            viewModel.onPreviousPage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(state.currentPage.pageType.transitionBackgroundColor())
            .noRippleClickable { },
    ) {
        AnimatedContent(
            targetState = state.currentPage.position,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val isLeavingStart = initialState == 0 && targetState == 1
                val enterDelay = if (isLeavingStart) INTRO_V3_START_EXIT_MS else INTRO_V3_CONTENT_FADE_MS
                fadeIn(animationSpec = tween(INTRO_V3_CONTENT_FADE_MS, delayMillis = enterDelay)) togetherWith
                        fadeOut(animationSpec = tween(if (isLeavingStart) INTRO_V3_START_EXIT_MS else INTRO_V3_CONTENT_FADE_MS)) using
                        SizeTransform(clip = false)
            },
            label = "onboardingV3Content",
        ) { position ->
            PagerPage(viewModel, state, position)
        }

        if (state.isShellChromeVisible) {
            V3AppBar(
                page = state.currentPage.pageType,
                pages = state.pages,
                position = state.currentPage.position,
                title = state.currentPage.pageType.topBarTitleRes?.let { localizedRes.string(it) },
                subtitle = state.currentPage.pageType.topBarSubtitleRes?.let { localizedRes.string(it) },
                selectedScenesCount = state.selectedScenes.size,
                onBackClick = viewModel::onPreviousPage,
                onSkipClick = viewModel::onSkipPage,
            )
        }

        if (state.isNextButtonVisible) {
            V3Footer(
                isTermsShown = false,
                isButtonEnabled = state.isNextEnabled,
                bottomExtraPadding = if (state.currentPage.pageType == OnboardingV3Page.AgePage) 250.dp else 0.dp,
                onNextClick = {
                    if (state.currentPage.pageType == OnboardingV3Page.NotificationsSetupPage) {
                        viewModel.saveNotificationSettings()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.onNotificationPermissionGranted()
                            viewModel.onNextPage()
                        }
                    } else {
                        viewModel.onNextPage()
                    }
                },
                onPrivacyClick = viewModel::onPrivacyClicked,
                onTermsClick = viewModel::onTermsClicked,
            )
        }
    }
}

private fun OnboardingV3Page.transitionBackgroundColor(): Color {
    return when (this) {
        OnboardingV3Page.About1Page -> Color(0xFFD69618).mix(Color.Black, 0.92f)
        OnboardingV3Page.About2Page -> Color(0xFF395BD3).mix(Color.Black, 0.92f)
        OnboardingV3Page.About3Page -> Color(0xFFB1385F).mix(Color.Black, 0.92f)
        else -> Color(0xFF17191F)
    }
}

private fun Color.mix(other: Color, amount: Float): Color {
    val clamped = amount.coerceIn(0f, 1f)
    val inverse = 1f - clamped
    return Color(
        red = red * inverse + other.red * clamped,
        green = green * inverse + other.green * clamped,
        blue = blue * inverse + other.blue * clamped,
        alpha = alpha * inverse + other.alpha * clamped,
    )
}

private val OnboardingV3Page.topBarTitleRes: Int?
    get() = when (this) {
        OnboardingV3Page.About1Page,
        OnboardingV3Page.About2Page,
        OnboardingV3Page.About3Page -> null

        else -> titleRes
    }

private val OnboardingV3Page.topBarSubtitleRes: Int?
    get() = when (this) {
        OnboardingV3Page.AgePage -> R.string.intro_age_info
        else -> null
    }

@Composable
private fun V3AppBar(
    page: OnboardingV3Page,
    pages: List<OnboardingV3Page>,
    position: Int,
    title: String?,
    subtitle: String?,
    selectedScenesCount: Int,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    val questionPages = pages.filter { it.isQuestion }
    val questionPage = pages.getOrNull(position)
    val questionIndex = questionPages.indexOf(questionPage)
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp),
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
            if (questionIndex >= 0) {
                V3QuestionProgress(
                    modifier = Modifier.align(Alignment.Center),
                    currentIndex = questionIndex,
                    total = questionPages.size,
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
                    .padding(top = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            subtitle?.let { subtitleText ->
                Text(
                    text = subtitleText,
                    color = White.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            if (page == OnboardingV3Page.ScenesPage) {
                Text(
                    text = "$selectedScenesCount of 3",
                    color = White.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 26.dp)
                    .size(width = 64.dp, height = 1.dp),
                color = White.copy(alpha = 0.18f),
            )
        }
    }
}

@Composable
private fun V3QuestionProgress(
    currentIndex: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            if (index == currentIndex) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 7.dp)
                        .background(White, RoundedCornerShape(999.dp)),
                )
            } else if (index == total - 1) {
                Text(
                    text = "✸",
                    color = White.copy(alpha = 0.24f),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            color = White.copy(alpha = 0.24f),
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.V3Footer(
    isTermsShown: Boolean,
    isButtonEnabled: Boolean,
    bottomExtraPadding: androidx.compose.ui.unit.Dp,
    onNextClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 30.dp, bottom = 30.dp + bottomExtraPadding)
            .fillMaxWidth()
            .imePadding(),
    ) {
        V3NextButton(
            text = LocalLocalizedRes.current.string(R.string.intro_next),
            isEnabled = isButtonEnabled,
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

@Composable
private fun V3NextButton(
    text: String,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isEnabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(5.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFE29B31),
                                    Color(0xFFFF5589),
                                    Color(0xFF204FC0),
                                ),
                            ),
                            shape = CircleShape,
                        ),
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = if (isEnabled) White else White.copy(alpha = 0.14f),
                        shape = CircleShape,
                    )
                    .then(if (isEnabled) Modifier.noRippleClickable(onClick) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    color = if (isEnabled) PrimaryButtonText else White.copy(alpha = 0.28f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}
