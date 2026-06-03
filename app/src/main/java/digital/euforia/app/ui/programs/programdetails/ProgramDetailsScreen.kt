@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.programs.programdetails

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.model.config.ProgramsConfig
import digital.euforia.app.ui.home.NavBarHeight
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavAnimations
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.util.widget.LIBRARY_TITLE_SHARED_KEY
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.programs.ProgramUi
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.shareProgram
import digital.euforia.app.ui.util.toComposeColor
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.MaxView
import digital.euforia.app.ui.util.widget.ProgramOptionMenu
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ProgramDetailsScreen(
    sharedTransitionScope: SharedTransitionScope,
    navController: NavHostController,
    viewModel: ProgramDetailsViewModel,
    navBarVisibilityState: MutableState<Boolean>,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    NavBarlessScreen(
        navBarVisibilityState = navBarVisibilityState,
        hideNavBarDelayMs = NavAnimations.PROGRAMS_DETAIL_TRANSITION_MS.toLong(),
    ) {
        SubscriptionActivityLauncher { launchSubscriptionActivity ->
            ProgramDetailsContent(
                sharedTransitionScope = sharedTransitionScope,
                navController = navController,
                animatedVisibilityScope = animatedVisibilityScope,
                isPremium = state.isPremium,
                isLoading = state.isLoading,
                errorState = state.errorState,
                programUi = state.program,
                meditations = state.meditations,
                articles = state.articles,
                exercises = state.exercises,
                programsConfig = state.programsConfig,
                currentPage = state.currentPage,
                onRetryClick = viewModel::onRetryClicked,
                onDownloadsClick = viewModel::onDownloadsClicked,
                onBackClick = { navController.popBackStack() },
                onPageSelected = viewModel::onPageSelected,
                onPublicationClick = viewModel::onPublicationClicked,
                onShareClick = viewModel::onShareClicked,
                onAboutClick = viewModel::onAboutClicked,
                onMeditationsClick = viewModel::onMeditationsClicked,
                onArticlesClick = viewModel::onArticlesClicked,
                onExercisesClick = viewModel::onExercisesClicked,
                launchSubscriptionActivity = launchSubscriptionActivity,
            )
        }
    }
}

@Composable
private fun ProgramDetailsContent(
    sharedTransitionScope: SharedTransitionScope,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isPremium: Boolean,
    isLoading: Boolean,
    errorState: ErrorViewState?,
    programUi: ProgramUi?,
    meditations: List<PublicationInfo>,
    articles: List<PublicationInfo>,
    exercises: List<PublicationInfo>,
    programsConfig: List<ProgramsConfig>,
    currentPage: Int,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onBackClick: () -> Unit,
    onPageSelected: (Int) -> Unit,
    onPublicationClick: (PublicationInfo) -> Unit,
    onShareClick: () -> Unit,
    onAboutClick: () -> Unit,
    onMeditationsClick: () -> Unit = {},
    onArticlesClick: () -> Unit = {},
    onExercisesClick: () -> Unit = {},
    launchSubscriptionActivity: () -> Unit,
) {
    val pagerState = rememberPagerState { 3 }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val localizedRes = LocalLocalizedRes.current
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 236.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            // Blur when the very first list item (spacer) scrolled off enough
            // or when any next item became the first visible one.
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }
    val overlapPx = with(density) { 64.dp.toPx() }

    val stickyOffsetPx = remember(AppBarHeightMedium, statusBarPadding, density) {
        with(density) {
            AppBarHeightMedium.toPx() + statusBarPadding.toPx()
        }
    }

    // Calculate sticky header translation directly from scroll offset so it starts moving immediately
    // and linearly with the top item without any lag.
    // Smoothly fade out the title item as it scrolls up
    val titleAlpha by remember(listState) {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            when {
                index > 0 -> 0f
                else -> (1f - (offset / thresholdPx.toFloat())).coerceIn(0f, 1f)
            }
        }
    }
    val topPadding by animateDpAsState(
        animationSpec = tween(durationMillis = 250),
        targetValue = if (shouldBlur) 16.dp + AppBarHeightMedium + statusBarPadding else 0.dp,
        label = "stickyPadding"
    )

    val stickyTranslationYPx by remember(listState, stickyOffsetPx, overlapPx) {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset.toFloat()
            if (index == 0) {
                // Start at -overlapPx (pulled under the title) and move down with the exact scroll
                // until reaching the final sticky position under the app bar.
                (-overlapPx + offset).coerceIn(-overlapPx, stickyOffsetPx)
            } else {
                // Once the first item is gone, keep the header fixed under the app bar.
                stickyOffsetPx
            }
        }
    }

    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }
    }

    LaunchedEffect(pagerState, currentPage) {
        snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
            .collect { (inProgress, page) ->
                if (!inProgress && page != currentPage) {
                    onPageSelected(page)
                }
            }
    }

    val context = LocalContext.current
    val isAboutVisible = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(NavBarBackground)) {
        AppBar(
            sharedTransitionScope = sharedTransitionScope,
            titleText = programUi?.name.orEmpty(),
            showTitle = shouldBlur,
            onBackClick = onBackClick,
            animatedVisibilityScope = animatedVisibilityScope,
            actionButton = {
                Row(
                    horizontalArrangement = spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (programUi?.isPremium == true && !isPremium) {
                        MaxView(
                            modifier = Modifier.noRippleClickable(launchSubscriptionActivity)
                        )
                    }
                    ProgramOptionMenu(
                        onAboutClick = {
                            onAboutClick()
                            isAboutVisible.value = true
                        },
                        onShareClick = {
                            onShareClick()
                            shareProgram(
                                context = context,
                                id = programUi?.id ?: 0,
                            )
                        }
                    )
                }
            }
        )

        if (isLoading && programUi == null) {
            ProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorState != null && programUi == null) {
            ErrorView(
                modifier = Modifier.align(Alignment.Center),
                state = errorState,
                onRetryClick = onRetryClick,
                onDownloadsClick = onDownloadsClick
            )
        } else if (programUi != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                state = listState,
                verticalArrangement = Arrangement.Absolute.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    bottom = 56.dp
                ),
            ) {
                titleItem(
                    titleText = programUi?.name.orEmpty(),
                    resourcesCount = programUi?.resourceCount ?: 0,
                    imageUrl = programUi?.imageUrl,
                    alpha = titleAlpha
                )
                pageSelector(
                    modifier = Modifier.graphicsLayer {
                        translationY = stickyTranslationYPx
                    }.zIndex(1f),
                    currentPage = currentPage,
                    selectionColor = programUi?.color1?.toComposeColor() ?: White,
                    onPageSelected = onPageSelected
                )
                pagerItem(
                    modifier = Modifier.graphicsLayer {
                        translationY = stickyTranslationYPx
                    },
                    isPremium = isPremium,
                    color = programUi?.color1?.toComposeColor() ?: White,
                    meditationUi = meditations,
                    articlesUi = articles,
                    exercisesUi = exercises,
                    pagerState = pagerState,
                    onPublicationClick = onPublicationClick,
                )
                item {
                    Spacer(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(bottom = NavBarHeight + 32.dp)
                    )
                }
            }
        }

        if (isAboutVisible.value) {
            AboutPopup(
                text = programUi?.description.orEmpty(),
                hazeState = hazeState,
                onClick = { isAboutVisible.value = false }
            )
        }
    }
}

private fun LazyListScope.pagerItem(
    modifier: Modifier,
    isPremium: Boolean,
    color: Color = White,
    meditationUi: List<PublicationInfo>,
    articlesUi: List<PublicationInfo>,
    exercisesUi: List<PublicationInfo>,
    pagerState: PagerState,
    onPublicationClick: (PublicationInfo) -> Unit,
) = item(key = "pager") {
    ProgramDetailsTabsPager(
        modifier = modifier,
        pagerState = pagerState,
        isPremium = isPremium,
        color = color,
        meditations = meditationUi,
        articles = articlesUi,
        exercises = exercisesUi,
        onPublicationClick = onPublicationClick,
    )
}

/**
 * Keeps pager height equal to the tallest tab so [HorizontalPager] animations and swipes
 * do not resize the parent [LazyColumn] item (which caused visible jumps).
 */
@Composable
private fun ProgramDetailsTabsPager(
    modifier: Modifier,
    pagerState: PagerState,
    isPremium: Boolean,
    color: Color,
    meditations: List<PublicationInfo>,
    articles: List<PublicationInfo>,
    exercises: List<PublicationInfo>,
    onPublicationClick: (PublicationInfo) -> Unit,
) {
    val density = LocalDensity.current
    val tabsContentKey = remember(meditations, articles, exercises) {
        Triple(meditations.size, articles.size, exercises.size)
    }

    SubcomposeLayout(
        modifier = modifier.fillMaxWidth(),
    ) { constraints ->
        val measureConstraints = constraints.copy(minHeight = 0)

        fun measureTabHeight(page: Int): Int =
            subcompose("measure_$page") {
                ProgramDetailsTabPage(
                    page = page,
                    isPremium = isPremium,
                    color = color,
                    meditations = meditations,
                    articles = articles,
                    exercises = exercises,
                    onPublicationClick = onPublicationClick,
                )
            }.maxOfOrNull { it.measure(measureConstraints).height } ?: 0

        val pagerHeightPx = maxOf(
            measureTabHeight(0),
            measureTabHeight(1),
            measureTabHeight(2),
        )

        val pagerConstraints = constraints.copy(
            minHeight = pagerHeightPx,
            maxHeight = pagerHeightPx,
        )

        val pagerPlaceable = subcompose("pager_$tabsContentKey") {
            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { pagerHeightPx.toDp() }),
                state = pagerState,
                beyondViewportPageCount = 1,
                userScrollEnabled = true,
            ) { position ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    ProgramDetailsTabPage(
                        page = position,
                        isPremium = isPremium,
                        color = color,
                        meditations = meditations,
                        articles = articles,
                        exercises = exercises,
                        onPublicationClick = onPublicationClick,
                    )
                }
            }
        }.map { it.measure(pagerConstraints) }.first()

        layout(constraints.maxWidth, pagerHeightPx) {
            pagerPlaceable.place(0, 0)
        }
    }
}

@Composable
private fun ProgramDetailsTabPage(
    page: Int,
    isPremium: Boolean,
    color: Color,
    meditations: List<PublicationInfo>,
    articles: List<PublicationInfo>,
    exercises: List<PublicationInfo>,
    onPublicationClick: (PublicationInfo) -> Unit,
) {
    when (page) {
        0 -> GenericPagerPage(
            color = color,
            isPremium = isPremium,
            iconRes = R.drawable.ic_type_audio,
            isPlayable = true,
            items = meditations,
            itemImageUrl = { it.imageUrl.orEmpty() },
            itemTitle = { it.title },
            itemDuration = { it.durationMinutes ?: 1 },
            isPremiumContent = { it.isPremium },
            onClick = onPublicationClick,
        )

        1 -> GenericPagerPage(
            color = color,
            isPremium = isPremium,
            iconRes = R.drawable.ic_type_read,
            isPlayable = false,
            items = articles,
            itemImageUrl = { it.imageUrl.orEmpty() },
            itemTitle = { it.title },
            itemDuration = { it.durationMinutes ?: 1 },
            isPremiumContent = { it.isPremium },
            onClick = onPublicationClick,
        )

        else -> GenericPagerPage(
            color = color,
            isPremium = isPremium,
            iconRes = R.drawable.ic_type_exercise,
            isPlayable = true,
            items = exercises,
            itemImageUrl = { it.imageUrl.orEmpty() },
            itemTitle = { it.title },
            itemDuration = { it.durationMinutes ?: 1 },
            isPremiumContent = { it.isPremium },
            onClick = onPublicationClick,
        )
    }
}

@Composable
fun <T> GenericPagerPage(
    color: Color,
    isPremium: Boolean,
    iconRes: Int,
    isPlayable: Boolean,
    items: List<T>,
    itemImageUrl: (T) -> String,
    itemTitle: (T) -> String,
    itemDuration: (T) -> Int,
    isPremiumContent: (T) -> Boolean,
    onClick: (T) -> Unit
) {
    val hazeState = rememberHazeState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { item ->
            GenericListItem(
                isPremium = isPremium,
                iconRes = iconRes,
                isPlayable = isPlayable,
                color = color,
                hazeState = hazeState,
                item = item,
                itemImageUrl = itemImageUrl,
                itemTitle = itemTitle,
                itemDuration = itemDuration,
                isPremiumContent = isPremiumContent,
                onClick = onClick
            )
        }
    }

}

@Composable
private fun <T> GenericListItem(
    isPremium: Boolean,
    iconRes: Int,
    isPlayable: Boolean,
    color: Color,
    hazeState: HazeState,
    item: T,
    itemImageUrl: (T) -> String,
    itemTitle: (T) -> String,
    itemDuration: (T) -> Int,
    isPremiumContent: (T) -> Boolean,
    onClick: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .noRippleClickable(onClick = { onClick(item) })
            .fillMaxWidth()
            .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                modifier = Modifier
                    .hazeSource(hazeState)
                    .clip(RoundedCornerShape(24.dp)).size(96.dp),
                model = itemImageUrl(item),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
            if (!isPremium && isPremiumContent(item)) {
                MaxView(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                if (isPlayable) {
                    Box(
                        modifier = Modifier.align(Alignment.Center)
                            .clip(CircleShape)
                            .hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.ultraThin(DarkGray)
                            )
                            .zIndex(1f)
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(16.dp),
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = null,
                            tint = White
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = itemTitle(item),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                color = White,
            )
            Row() {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = White.copy(alpha = 0.6f)
                )

                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = "•",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraLight,
                    ),
                    color = White.copy(alpha = 0.6f),
                )

                Text(
                    text = "${itemDuration(item)} minutes",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraLight,
                    ),
                    color = White.copy(alpha = 0.6f),
                )
            }
        }
    }
}

private fun LazyListScope.pageSelector(
    modifier: Modifier = Modifier,
    currentPage: Int,
    selectionColor: Color,
    onPageSelected: (Int) -> Unit
) =
    stickyHeader(key = "selector") {
        Box(
            modifier = modifier.fillMaxWidth()
                .background(
                    NavBarBackground,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(vertical = 16.dp)
                .height(AppBarHeightMedium)
        ) {
            // Animated page indicator that slides under the selected title
            AnimatedPageIndicator(
                modifier = Modifier.padding(horizontal = 16.dp),
                currentPage = currentPage,
                selectionColor = selectionColor
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth().align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleView(
                    textRes = R.string.meditations_title,
                    isSelected = currentPage == 0
                ) { onPageSelected(0) }
                TitleView(
                    textRes = R.string.articles_title,
                    isSelected = currentPage == 1
                ) { onPageSelected(1) }
                TitleView(
                    textRes = R.string.exercises_title,
                    isSelected = currentPage == 2
                ) { onPageSelected(2) }
            }
        }
    }

@Composable
private fun RowScope.TitleView(textRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val localizedRes = LocalLocalizedRes.current
    Text(
        modifier = Modifier.weight(1f).noRippleClickable(onClick),
        text = localizedRes.string(textRes),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = if (isSelected) White else White.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AnimatedPageIndicator(
    modifier: Modifier = Modifier,
    currentPage: Int,
    selectionColor: Color,
) {
    // Fills the parent to measure full width and compute target offset.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidth = maxWidth
        // Keep same visual width as before: 30% of the parent width
        val indicatorWidth = containerWidth * 0.3f

        fun targetLeftFor(page: Int) = when (page.coerceIn(0, 2)) {
            0 -> (containerWidth * (1f / 6f)) - (indicatorWidth / 2)
            1 -> (containerWidth * (1f / 2f)) - (indicatorWidth / 2)
            else -> (containerWidth * (5f / 6f)) - (indicatorWidth / 2)
        }

        val targetX by animateDpAsState(
            targetValue = targetLeftFor(currentPage),
            animationSpec = tween(durationMillis = 250),
            label = "pageIndicatorX"
        )

        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .offset(x = targetX)
                .width(indicatorWidth)
                .fillMaxHeight()
                .background(selectionColor.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
        ) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).height(2.dp).width(32.dp)
                    .background(color = White)
            ) {}
        }
    }
}

private fun LazyListScope.titleItem(
    titleText: String,
    resourcesCount: Int,
    imageUrl: String?,
    alpha: Float = 1f,
) = item {
    val localizedRes = LocalLocalizedRes.current
    // Apply smooth fade based on scroll progress
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .graphicsLayer { this.alpha = alpha }
    ) {
        AsyncImage(
            modifier = Modifier.align(Alignment.TopCenter),
            model = imageUrl,
            contentScale = ContentScale.FillWidth,
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "$resourcesCount resources",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraLight),
                color = White.copy(alpha = 0.6f)
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                color = White
            )
        }
    }
}


@Composable
private fun AppBar(
    sharedTransitionScope: SharedTransitionScope,
    titleText: String,
    showTitle: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBackClick: () -> Unit,
    actionButton: @Composable () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val appBarModifier = if (showTitle) {
        Modifier.zIndex(1f).background(NavBarBackground)
    } else {
        Modifier.zIndex(1f)
    }
    Row(
        modifier = appBarModifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(AppBarHeightMedium),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .noRippleClickable { onBackClick() },
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .padding(8.dp),
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = White
            )
            with(sharedTransitionScope) {
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = LIBRARY_TITLE_SHARED_KEY),
                        animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = 300) },
                    ),
                    text = localizedRes.string(R.string.library_title),
                    color = White,
                    style = appbarMedium.copy(fontWeight = FontWeight.Medium),
                )
            }
        }
        Text(
            modifier = Modifier
                .weight(1f)
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                ),
            text = if (showTitle) titleText else "",
            color = White,
            style = appbarMedium,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        actionButton()

    }
}

private fun handleSideEffect(
    sideEffect: ProgramDetailsSideEffect,
    navController: NavHostController
) {
    when (sideEffect) {
        is ProgramDetailsSideEffect.NavigateToDownloads -> navController.navigate(
            HomeDestination.Downloads
        )

        is ProgramDetailsSideEffect.NavigateToPublication -> navController.navigate(
            HomeDestination.PublicationDetails(
                id = sideEffect.id,
                publicationType = sideEffect.type,
                packageTitle = sideEffect.packageTitle
            )
        )

        else -> {}
    }
}