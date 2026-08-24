package digital.euforia.app.ui.onboardingV3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.Home
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.ONBOARDING_PREVIEW_DONE_RESULT_KEY
import digital.euforia.app.ui.navigation.OnboardingV3
import digital.euforia.app.ui.onboardingV3.pager.PagerPage
import digital.euforia.app.ui.onboardingV3.pager.ScenesPreviewPrewarmHost
import digital.euforia.app.ui.onboardingV3.pager.rememberScenesPreviewPrewarmState
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.onboardingV3.components.V3PrimaryButton
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.AnimatedVerticalShrink
import digital.euforia.app.ui.util.widget.TermsAndPrivacyText
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val INTRO_V3_START_EXIT_MS = 1_500
private const val INTRO_V3_CONTENT_FADE_MS = 300
private const val QUESTION_PROGRESS_ENTRANCE_STIFFNESS = 95f
private const val QUESTION_PROGRESS_PAGE_STIFFNESS = 120f

@Composable
fun OnboardingV3Screen(
    navController: NavHostController,
    viewModel: OnboardingV3ViewModel,
) {
    val state by viewModel.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(navController) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        savedStateHandle.getStateFlow(ONBOARDING_PREVIEW_DONE_RESULT_KEY, false).collect { isDone ->
            if (isDone) {
                savedStateHandle[ONBOARDING_PREVIEW_DONE_RESULT_KEY] = false
                viewModel.onPreviewCompleted()
            }
        }
    }

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
            OnboardingV3SideEffect.NavigateHome -> {
                navController.navigate(Home()) {
                    popUpTo(OnboardingV3) { inclusive = true }
                }
            }
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
            is OnboardingV3SideEffect.NavigatePreviewAudio -> {
                navController.navigate(
                    HomeDestination.AudioPlayer(
                        accompanimentId = effect.accompanimentId,
                        timeOfDay = effect.timeOfDay,
                        entryPoint = AudioPlayerEntryPoint.ONBOARDING,
                        isOnboardingPreview = true,
                    ),
                )
            }
            is OnboardingV3SideEffect.NavigatePreviewMeditation -> {
                navController.navigate(
                    HomeDestination.PublicationPlayer(
                        id = effect.meditationId,
                        publicationType = PublicationType.MEDITATION,
                        isOnboardingPreview = true,
                    ),
                )
            }
            is OnboardingV3SideEffect.NavigatePreviewSoundscape -> {
                navController.navigate(
                    HomeDestination.SoundscapesScene(
                        sceneId = effect.sceneId,
                        isOnboardingPreview = true,
                        introSceneTimerSeconds = effect.introSceneTimerSeconds,
                        previewTitle = effect.previewTitle,
                    ),
                )
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
    val context = LocalContext.current
    var displayedPosition by remember { mutableIntStateOf(state.currentPage.position) }
    var chromePosition by remember { mutableIntStateOf(state.currentPage.position) }
    val contentAlpha = remember { Animatable(1f) }
    val chromeAlpha = remember { Animatable(1f) }
    var nextButtonBounds by remember { mutableStateOf<Rect?>(null) }
    var questionProgressBounds by remember { mutableStateOf<Rect?>(null) }
    var headerDividerBottomPx by remember { mutableStateOf<Float?>(null) }
    var headerDividerPosition by remember { mutableIntStateOf(-1) }
    var pendingQuestionProgressEntrance by remember { mutableStateOf(false) }
    var progressEntranceSpec by remember { mutableStateOf<ProgressEntranceSpec?>(null) }
    var isProgressEntranceRunning by remember { mutableStateOf(false) }
    var isQuestionProgressRevealPreparing by remember { mutableStateOf(false) }
    var questionProgressAppearanceTrigger by remember { mutableIntStateOf(0) }
    val progressEntrance = remember { Animatable(1f) }
    val scenesPreviewPrewarmState = rememberScenesPreviewPrewarmState()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onNotificationsSetupConfirmed(hasNotificationPermission = isGranted)
    }

    LaunchedEffect(state.currentPage.position) {
        viewModel.onPageUpdated(state.currentPage.position)
    }

    LaunchedEffect(state.currentPage.position) {
        val targetPosition = state.currentPage.position
        if (targetPosition == displayedPosition && targetPosition == chromePosition) {
            contentAlpha.snapTo(1f)
            chromeAlpha.snapTo(1f)
            return@LaunchedEffect
        }

        val isStartToFirstStep = displayedPosition == 0 && targetPosition == 1
        val fadeOutMs = if (isStartToFirstStep) INTRO_V3_START_EXIT_MS else INTRO_V3_CONTENT_FADE_MS
        val fadeInMs = INTRO_V3_CONTENT_FADE_MS

        launch {
            contentAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(fadeOutMs, easing = FastOutLinearInEasing),
            )
        }
        chromeAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(fadeOutMs, easing = FastOutLinearInEasing),
        )

        displayedPosition = targetPosition
        chromePosition = targetPosition
        contentAlpha.snapTo(0f)
        chromeAlpha.snapTo(0f)

        launch {
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(fadeInMs, easing = LinearOutSlowInEasing),
            )
        }
        chromeAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(fadeInMs, easing = LinearOutSlowInEasing),
        )
    }

    LaunchedEffect(state.currentPage.position, nextButtonBounds, questionProgressBounds) {
        val questionPages = state.pages.filter { it.isQuestion }
        val questionIndex = questionPages.indexOf(state.currentPage.pageType)
        val shouldRunEntrance = pendingQuestionProgressEntrance &&
                questionIndex == 0 &&
                state.currentPage.pageType.isQuestion
        val startBounds = nextButtonBounds
        val endBounds = questionProgressBounds
        if (shouldRunEntrance && startBounds != null && endBounds != null) {
            pendingQuestionProgressEntrance = false
            progressEntranceSpec = ProgressEntranceSpec(
                startBounds = startBounds,
                endCenterX = endBounds.center.x,
                endCenterY = endBounds.center.y,
            )
            isProgressEntranceRunning = true
            progressEntrance.snapTo(0f)
            progressEntrance.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = QUESTION_PROGRESS_ENTRANCE_STIFFNESS,
                ),
            )
            progressEntranceSpec = null
            isQuestionProgressRevealPreparing = true
            isProgressEntranceRunning = false
            delay(32)
            questionProgressAppearanceTrigger += 1
            delay(32)
            isQuestionProgressRevealPreparing = false
        } else if (pendingQuestionProgressEntrance && state.currentPage.pageType.isQuestion && questionIndex != 0) {
            pendingQuestionProgressEntrance = false
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
            .background(
                (state.pages.getOrNull(displayedPosition) ?: state.currentPage.pageType)
                    .transitionBackgroundColor()
            )
            .noRippleClickable { },
    ) {
        val scenesPreviewPosition = state.pages.indexOf(OnboardingV3Page.ScenesPreviewPage)
        ScenesPreviewPrewarmHost(
            state = scenesPreviewPrewarmState,
            enabled = scenesPreviewPosition > 0 &&
                    state.currentPage.position == scenesPreviewPosition - 1,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha.value),
        ) {
            val density = LocalDensity.current
            val pageType = state.pages.getOrNull(displayedPosition)
            val measuredTopPadding = headerDividerBottomPx
                ?.takeIf { headerDividerPosition == displayedPosition }
                ?.let { with(density) { it.toDp() } + 30.dp }
            PagerPage(
                viewModel = viewModel,
                state = state,
                scenesPreviewPrewarmState = scenesPreviewPrewarmState,
                position = displayedPosition,
                activePosition = displayedPosition,
                listTopPadding = measuredTopPadding ?: pageType.fallbackListTopPadding,
            )
        }

        val chromePage = state.pages.getOrNull(chromePosition) ?: state.currentPage.pageType
        val canSkip = chromePage.canSkipLikeIos()
        var isSkipDelayElapsed by remember { mutableStateOf(false) }
        LaunchedEffect(chromePosition, canSkip, state.introSkipDelaySeconds) {
            if (!canSkip) {
                isSkipDelayElapsed = false
                return@LaunchedEffect
            }
            val delayMillis = (state.introSkipDelaySeconds * 1_000).roundToInt().coerceAtLeast(0)
            if (delayMillis > 0) {
                isSkipDelayElapsed = false
                delay(delayMillis.toLong())
            }
            isSkipDelayElapsed = true
        }
        val shouldShowSkip = canSkip && (state.introSkipDelaySeconds <= 0.0 || isSkipDelayElapsed)
        val skipAlpha by animateFloatAsState(
            targetValue = if (shouldShowSkip) 1f else 0f,
            animationSpec = tween(200),
            label = "skipAlpha",
        )
        if (chromePage.hasHeaderScrollGradient) {
            V3HeaderScrollGradient(
                page = chromePage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .alpha(chromeAlpha.value),
            )
        }
        if (!chromePage.hidesShellChrome) {
            val isFirstQuestionPage = state.pages
                .filter { it.isQuestion }
                .firstOrNull() == chromePage
            val hideQuestionProgress = isFirstQuestionPage &&
                    (pendingQuestionProgressEntrance || isProgressEntranceRunning || isQuestionProgressRevealPreparing)
            V3AppBar(
                modifier = Modifier.alpha(chromeAlpha.value),
                page = chromePage,
                pages = state.pages,
                position = chromePosition,
                title = chromePage.topBarTitleRes?.let { localizedRes.string(it) },
                subtitle = chromePage.topBarSubtitleRes?.let { localizedRes.string(it) },
                selectedScenesCount = state.selectedScenes.size,
                questionProgressAlpha = if (hideQuestionProgress) 0f else 1f,
                questionProgressAppearanceTrigger = questionProgressAppearanceTrigger,
                canSkip = canSkip,
                skipAlpha = skipAlpha,
                onQuestionProgressPositioned = { questionProgressBounds = it },
                onHeaderDividerPositioned = {
                    headerDividerPosition = chromePosition
                    headerDividerBottomPx = it.bottom
                },
                onBackClick = viewModel::onPreviousPage,
                onSkipClick = viewModel::onSkipPage,
            )
        }

        val shouldShowFooter = !chromePage.hidesShellChrome &&
                chromePage != OnboardingV3Page.StartPage &&
                (!chromePage.requiresSelectionBeforeNext || state.isNextEnabled)
        val bottomExtraPadding = if (chromePage == OnboardingV3Page.AgePage) 250.dp else 0.dp
        if (chromePage.hasFooterScrollGradient) {
            val footerGradientAlpha by animateFloatAsState(
                targetValue = if (shouldShowFooter) 1f else 0f,
                animationSpec = tween(250),
                label = "footerScrollGradientAlpha",
            )
            V3FooterScrollGradient(
                page = chromePage,
                bottomExtraPadding = bottomExtraPadding,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(footerGradientAlpha * chromeAlpha.value),
            )
        }
        if (shouldShowFooter) {
            V3Footer(
                modifier = Modifier.alpha(chromeAlpha.value),
                isTermsShown = false,
                isButtonEnabled = state.isNextEnabled,
                bottomExtraPadding = bottomExtraPadding,
                onNextButtonPositioned = { nextButtonBounds = it },
                onNextClick = {
                    if (state.currentPage.pageType == OnboardingV3Page.NotificationsSetupPage) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val isGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) == PackageManager.PERMISSION_GRANTED
                            if (isGranted) {
                                viewModel.onNotificationsSetupConfirmed(hasNotificationPermission = true)
                            } else {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            viewModel.onNotificationsSetupConfirmed(hasNotificationPermission = true)
                        }
                    } else {
                        val nextPage = state.pages.getOrNull(state.currentPage.position + 1)
                        val firstQuestionPage = state.pages.firstOrNull { it.isQuestion }
                        if (nextPage != null && nextPage == firstQuestionPage) {
                            pendingQuestionProgressEntrance = true
                        }
                        viewModel.onNextPage()
                    }
                },
                onPrivacyClick = viewModel::onPrivacyClicked,
                onTermsClick = viewModel::onTermsClicked,
            )
        }

        progressEntranceSpec?.let { spec ->
            ProgressEntranceOverlay(
                spec = spec,
                progress = progressEntrance.value,
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

private val OnboardingV3Page.requiresSelectionBeforeNext: Boolean
    get() = when (this) {
        OnboardingV3Page.GoalsPage,
        OnboardingV3Page.ProgramsPage,
        OnboardingV3Page.DailyCommitmentPage,
        OnboardingV3Page.TimePage,
        OnboardingV3Page.ScenesPage -> true

        else -> false
    }

private val OnboardingV3Page?.fallbackListTopPadding
    get() = when (this) {
        OnboardingV3Page.GoalsPage -> 216.dp
        OnboardingV3Page.ScenesPage -> 262.dp
        else -> 246.dp
    }

private fun OnboardingV3Page.canSkipLikeIos(): Boolean {
    return when (this) {
        OnboardingV3Page.StartPage,
        OnboardingV3Page.About1Page,
        OnboardingV3Page.About2Page,
        OnboardingV3Page.About3Page,
        OnboardingV3Page.GenderPage,
        OnboardingV3Page.SummaryPage,
        OnboardingV3Page.LoadingPage,
        OnboardingV3Page.FirstExperiencePage,
        OnboardingV3Page.RatePage -> false

        else -> !hidesShellChrome
    }
}

private val OnboardingV3Page.hasHeaderScrollGradient: Boolean
    get() = when (this) {
        OnboardingV3Page.GoalsPage,
        OnboardingV3Page.ProgramsPage,
        OnboardingV3Page.DailyCommitmentPage,
        OnboardingV3Page.TimePage,
        OnboardingV3Page.ScenesPage -> true

        else -> false
    }

private val OnboardingV3Page.hasFooterScrollGradient: Boolean
    get() = when (this) {
        OnboardingV3Page.GoalsPage,
        OnboardingV3Page.ProgramsPage,
        OnboardingV3Page.DailyCommitmentPage,
        OnboardingV3Page.TimePage,
        OnboardingV3Page.ScenesPage -> true

        else -> false
    }

@Composable
private fun V3HeaderScrollGradient(
    page: OnboardingV3Page,
    modifier: Modifier = Modifier,
) {
    val background = page.transitionBackgroundColor()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (page == OnboardingV3Page.ScenesPage) 248.dp else 210.dp)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to background,
                        0.74f to background,
                        0.88f to background.copy(alpha = 0.96f),
                        1f to background.copy(alpha = 0f),
                    ),
                ),
            ),
    )
}

@Composable
private fun V3FooterScrollGradient(
    page: OnboardingV3Page,
    bottomExtraPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val background = page.transitionBackgroundColor()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(178.dp + bottomExtraPadding)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to background.copy(alpha = 0f),
                        0.34f to background.copy(alpha = 0.72f),
                        0.58f to background.copy(alpha = 0.96f),
                        1f to background,
                    ),
                ),
            ),
    )
}

@Composable
private fun V3AppBar(
    modifier: Modifier = Modifier,
    page: OnboardingV3Page,
    pages: List<OnboardingV3Page>,
    position: Int,
    title: String?,
    subtitle: String?,
    selectedScenesCount: Int,
    questionProgressAlpha: Float,
    questionProgressAppearanceTrigger: Int,
    canSkip: Boolean,
    skipAlpha: Float,
    onQuestionProgressPositioned: (Rect) -> Unit,
    onHeaderDividerPositioned: (Rect) -> Unit,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    val questionPages = pages.filter { it.isQuestion }
    val questionPage = pages.getOrNull(position)
    val questionIndex = questionPages.indexOf(questionPage)
    Column(
        modifier = modifier
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
                    modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(questionProgressAlpha)
                        .onGloballyPositioned { onQuestionProgressPositioned(it.boundsInRoot()) },
                    currentIndex = questionIndex,
                    total = questionPages.size,
                    appearanceTrigger = questionProgressAppearanceTrigger,
                )
            }
            if (canSkip) {
                var isPressed by remember { mutableStateOf(false) }
                val color by animateColorAsState(
                    targetValue = if (isPressed) White.copy(alpha = 0.3f) else White.copy(alpha = 0.6f),
                    animationSpec = tween(200),
                    label = "skipColor",
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .alpha(skipAlpha)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                },
                                onTap = {
                                    if (skipAlpha > 0.99f) {
                                        onSkipClick()
                                    }
                                },
                            )
                        },
                    text = LocalLocalizedRes.current.string(R.string.skip),
                    color = color,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        title?.let {
            val density = LocalDensity.current
            val titleTransition = remember { Animatable(1f) }
            LaunchedEffect(position, title, subtitle) {
                titleTransition.snapTo(0f)
                titleTransition.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = INTRO_V3_CONTENT_FADE_MS,
                        easing = LinearOutSlowInEasing,
                    ),
                )
            }
            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = titleTransition.value
                    val scale = 0.8f + (titleTransition.value * 0.2f)
                    scaleX = scale
                    scaleY = scale
                    translationY = with(density) { 10.dp.toPx() } * (1f - titleTransition.value)
                },
            ) {
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
                        .size(width = 64.dp, height = 1.dp)
                        .onGloballyPositioned { onHeaderDividerPositioned(it.boundsInRoot()) },
                    color = White.copy(alpha = 0.18f),
                )
            }
        }
    }
}

@Composable
private fun V3QuestionProgress(
    currentIndex: Int,
    total: Int,
    appearanceTrigger: Int,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return

    val appearanceProgress = remember { Animatable(1f) }
    LaunchedEffect(appearanceTrigger) {
        if (appearanceTrigger > 0) {
            appearanceProgress.snapTo(0f)
            appearanceProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 860,
                    delayMillis = 80,
                    easing = LinearEasing,
                ),
            )
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = currentIndex.coerceIn(0, total - 1).toFloat(),
        animationSpec = spring(
            dampingRatio = 0.86f,
            stiffness = QUESTION_PROGRESS_PAGE_STIFFNESS,
        ),
        label = "questionProgress",
    )
    val dotSize = 5f
    val activeWidth = 25f
    val spacing = 10f
    val tapTargetWidth = 20.dp
    val tapTargetHeight = 28.dp
    val indicatorYOffset = (tapTargetHeight - dotSize.dp) * 0.5f
    val maximumStretch = 6f
    val totalIndicators = total + 1
    val clampedProgress = animatedProgress.coerceIn(0f, (total - 1).toFloat())
    val fractional = clampedProgress - floor(clampedProgress)
    val stretch = sin(fractional * PI.toFloat())
    val animatedWidth = activeWidth + (stretch * maximumStretch)
    val maximumIndicatorWidth = activeWidth + maximumStretch
    val maximumExtraWidth = maximumIndicatorWidth - dotSize
    val liveExtraWidth = animatedWidth - dotSize
    val baseStep = dotSize + spacing
    val leadingInset = maximumExtraWidth * 0.5f
    val contentWidth = dotSize + ((totalIndicators - 1) * baseStep) + maximumExtraWidth
    val collapsedDotX = collapsedDotOriginX(contentWidth)
    val collapsedIndicatorX = collapsedIndicatorOriginX(
        contentWidth = contentWidth,
        indicatorWidth = animatedWidth,
    )
    val indicatorReveal = indicatorRevealProgress(appearanceProgress.value)
    val indicatorMotion = overshootingRevealProgress(indicatorReveal)

    Box(
        modifier = modifier
            .width(contentWidth.dp)
            .height(tapTargetHeight),
    ) {
        repeat(total) { index ->
            val originX = dotOriginX(
                index = index,
                progress = clampedProgress,
                leadingInset = leadingInset,
                step = baseStep,
                liveExtraWidth = liveExtraWidth,
            )
            val reveal = dotRevealProgress(
                appearanceProgress = appearanceProgress.value,
                index = index,
                currentIndex = currentIndex,
            )
            val motion = overshootingRevealProgress(reveal)
            val bounce = bounceScaleProgress(reveal)
            val distance = abs(index - clampedProgress)
            val alpha = when {
                distance < 0.46f -> 0f
                index < clampedProgress -> 0.96f
                else -> 0.26f
            }
            Box(
                modifier = Modifier
                    .offset(x = interpolate(collapsedDotX, originX, motion).dp)
                    .size(width = tapTargetWidth, height = tapTargetHeight),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize.dp)
                        .alpha(if (index == currentIndex) 0f else min(reveal * 1.25f, 1f))
                        .graphicsScale(max(0.18f, 0.18f + (0.82f * bounce)))
                        .background(
                            color = White.copy(alpha = alpha),
                            shape = CircleShape,
                        ),
                )
            }
        }

        val trailingOriginX = dotOriginX(
            index = total,
            progress = clampedProgress,
            leadingInset = leadingInset,
            step = baseStep,
            liveExtraWidth = liveExtraWidth,
        )
        val trailingReveal = dotRevealProgress(
            appearanceProgress = appearanceProgress.value,
            index = total,
            currentIndex = currentIndex,
        )
        val trailingMotion = overshootingRevealProgress(trailingReveal)
        val trailingBounce = bounceScaleProgress(trailingReveal)
        val rotation by rememberInfiniteTransition(label = "questionProgressFlower")
            .animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 10_000, easing = LinearEasing),
                ),
                label = "questionProgressFlowerRotation",
            )
        Box(
            modifier = Modifier
                .offset(x = interpolate(collapsedDotX, trailingOriginX, trailingMotion).dp)
                .size(width = tapTargetWidth, height = tapTargetHeight)
                .alpha(min(trailingReveal * 1.25f, 1f))
                .graphicsScale(max(0.18f, 0.18f + (0.82f * trailingBounce)))
                .rotate(rotation),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✸",
                color = White.copy(alpha = 0.34f),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 13.sp,
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        AnimatedQuestionProgressIndicator(
            modifier = Modifier
                .offset(
                    x = interpolate(
                        collapsedIndicatorX,
                        indicatorOriginX(
                            progress = clampedProgress,
                            leadingInset = leadingInset,
                            step = baseStep,
                            indicatorWidth = animatedWidth,
                        ),
                        indicatorMotion,
                    ).dp,
                    y = indicatorYOffset,
                )
                .graphicsScale(max(0.86f, 0.86f + (0.34f * indicatorMotion))),
            width = animatedWidth.dp,
            height = dotSize.dp,
        )
    }
}

private fun Modifier.graphicsScale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
)

private fun dotOriginX(
    index: Int,
    progress: Float,
    leadingInset: Float,
    step: Float,
    liveExtraWidth: Float,
): Float {
    val centerX = leadingInset + (index * step) + 2.5f
    val shift = horizontalShift(index = index, progress = progress, liveExtraWidth = liveExtraWidth)
    return centerX + shift - 10f
}

private fun indicatorOriginX(
    progress: Float,
    leadingInset: Float,
    step: Float,
    indicatorWidth: Float,
): Float {
    val centerX = leadingInset + (progress * step) + 2.5f
    return centerX - (indicatorWidth * 0.5f)
}

private fun horizontalShift(index: Int, progress: Float, liveExtraWidth: Float): Float {
    val halfExtra = liveExtraWidth * 0.5f
    val distance = index - progress
    return when {
        distance <= -1f -> -halfExtra
        distance >= 1f -> halfExtra
        distance < 0f -> -halfExtra * -distance
        distance > 0f -> halfExtra * distance
        else -> 0f
    }
}

private fun collapsedDotOriginX(contentWidth: Float): Float {
    return (contentWidth - 20f) * 0.5f
}

private fun collapsedIndicatorOriginX(contentWidth: Float, indicatorWidth: Float): Float {
    return (contentWidth - indicatorWidth) * 0.5f
}

private fun interpolate(start: Float, end: Float, progress: Float): Float {
    return start + ((end - start) * progress)
}

private fun indicatorRevealProgress(appearanceProgress: Float): Float {
    return stagedAppearanceProgress(appearanceProgress = appearanceProgress, delay = 0.02f, duration = 0.32f)
}

private fun dotRevealProgress(
    appearanceProgress: Float,
    index: Int,
    currentIndex: Int,
): Float {
    if (index == currentIndex) return 0f
    val distance = abs(index - currentIndex).toFloat()
    val delay = min(0.76f, 0.18f + (distance * 0.14f))
    val duration = max(0.2f, 0.32f - (distance * 0.02f))
    return stagedAppearanceProgress(
        appearanceProgress = appearanceProgress,
        delay = delay,
        duration = duration,
    )
}

private fun stagedAppearanceProgress(
    appearanceProgress: Float,
    delay: Float,
    duration: Float = 0.4f,
): Float {
    if (appearanceProgress <= delay) return 0f
    val end = min(1f, delay + duration)
    if (end <= delay) return 1f
    return min((appearanceProgress - delay) / (end - delay), 1f)
}

private fun overshootingRevealProgress(progress: Float): Float {
    val t = progress.coerceIn(0f, 1f)
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val p = t - 1f
    return 1f + (c3 * p * p * p) + (c1 * p * p)
}

private fun bounceScaleProgress(progress: Float): Float {
    val t = progress.coerceIn(0f, 1f)
    val c1 = 2.2f
    val c3 = c1 + 1f
    val p = t - 1f
    return 1f + (c3 * p * p * p) + (c1 * p * p)
}

@Composable
private fun AnimatedQuestionProgressIndicator(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val shimmerPhase by rememberInfiniteTransition(label = "questionProgressShimmer")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2_100, easing = LinearEasing),
            ),
            label = "questionProgressShimmerPhase",
        )
    val shimmerOffset = (-width * 0.5f) + (width * 1.5f * shimmerPhase)
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(CircleShape)
            .background(White),
    ) {
        Box(
            modifier = Modifier
                .offset(x = shimmerOffset - (width * 0.25f))
                .width(maxOf(8.dp, width * 0.44f))
                .height(height)
                .blur(0.6.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            White.copy(alpha = 0f),
                            White.copy(alpha = 0.6f),
                            White.copy(alpha = 0f),
                        ),
                    ),
                ),
        )
    }
}

private data class ProgressEntranceSpec(
    val startBounds: Rect,
    val endCenterX: Float,
    val endCenterY: Float,
)

@Composable
private fun ProgressEntranceOverlay(
    spec: ProgressEntranceSpec,
    progress: Float,
) {
    val density = LocalDensity.current
    val easedProgress = progress.coerceIn(0f, 1f)
    val finalWidthPx = with(density) { 25.dp.toPx() }
    val finalHeightPx = with(density) { 5.dp.toPx() }
    val startWidth = spec.startBounds.width
    val startHeight = spec.startBounds.height
    val width = startWidth + ((finalWidthPx - startWidth) * easedProgress)
    val height = startHeight + ((finalHeightPx - startHeight) * easedProgress)
    val startLeft = spec.startBounds.left
    val startTop = spec.startBounds.top
    val endLeft = spec.endCenterX - (finalWidthPx * 0.5f)
    val endTop = spec.endCenterY - (finalHeightPx * 0.5f)
    val left = startLeft + ((endLeft - startLeft) * easedProgress)
    val top = startTop + ((endTop - startTop) * easedProgress)

    Box(
        modifier = Modifier
            .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
            .size(
                width = with(density) { width.toDp() },
                height = with(density) { height.toDp() },
            )
            .background(White, RoundedCornerShape(999.dp)),
    )
}

@Composable
private fun BoxScope.V3Footer(
    modifier: Modifier = Modifier,
    isTermsShown: Boolean,
    isButtonEnabled: Boolean,
    bottomExtraPadding: androidx.compose.ui.unit.Dp,
    onNextButtonPositioned: (Rect) -> Unit,
    onNextClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 30.dp, bottom = 30.dp + bottomExtraPadding)
            .fillMaxWidth()
            .imePadding(),
    ) {
        V3PrimaryButton(
            text = LocalLocalizedRes.current.string(R.string.intro_next),
            isEnabled = isButtonEnabled,
            onPositioned = onNextButtonPositioned,
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

