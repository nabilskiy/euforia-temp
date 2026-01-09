package digital.euforia.app.ui.programs.programdetails

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.domain.model.config.ProgramsConfig
import digital.euforia.app.ui.home.NavBarHeight
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.onboarding.pager.PagerPage
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.programs.ArticleUi
import digital.euforia.app.ui.programs.ExerciseUi
import digital.euforia.app.ui.programs.MeditationUi
import digital.euforia.app.ui.programs.ProgramUi
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.toComposeColor
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.MaxView
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ProgramDetailsScreen(
    navController: NavHostController,
    viewModel: ProgramDetailsViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    NavBarlessScreen(navBarVisibilityState) {
        ProgramDetailsContent(
            navController = navController,
            isPremium = state.isPremium,
            isLoading = state.isLoading,
            errorState = state.errorState,
            programUi = state.program,
            meditations = state.meditations,
            articles = state.articles,
            exercises = state.exercises,
            programsConfig = state.programsConfig,
            resourcesCount = state.resourcesCount,
            currentPage = state.currentPage,
            onRetryClick = viewModel::onRetryClicked,
            onDownloadsClick = viewModel::onDownloadsClicked,
            onBackClick = { navController.popBackStack() },
            onPageSelected = viewModel::onPageSelected,
            onMeditationClick = viewModel::onMeditationClicked,
            onArticleClick = viewModel::onArticleClicked,
            onExerciseClick = viewModel::onExerciseClicked
        )
    }
}

@Composable
private fun ProgramDetailsContent(
    navController: NavHostController,
    isPremium: Boolean,
    isLoading: Boolean,
    errorState: ErrorViewState?,
    programUi: ProgramUi?,
    meditations: List<MeditationUi>,
    articles: List<ArticleUi>,
    exercises: List<ExerciseUi>,
    programsConfig: List<ProgramsConfig>,
    resourcesCount: Int,
    currentPage: Int,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onBackClick: () -> Unit,
    onPageSelected: (Int) -> Unit,
    onMeditationClick: (MeditationUi) -> Unit,
    onArticleClick: (ArticleUi) -> Unit,
    onExerciseClick: (ExerciseUi) -> Unit
) {
    val pagerState = rememberPagerState { 3 }
    subscribeToPagerUpdates(
        coroutineScope = rememberCoroutineScope(),
        pagerState = pagerState,
        page = currentPage
    )
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> onPageSelected(page) }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val localizedRes = LocalLocalizedRes.current
    val listState = rememberLazyListState()
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
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


    Box(modifier = Modifier.fillMaxSize().background(NavBarBackground)) {
        AppBar(
            titleText = programUi?.name.orEmpty(),
            showTitle = shouldBlur,
            onBackClick = onBackClick,
            navController = navController,
        )

        if (isLoading) {
            ProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorState != null) {
            ErrorView(
                modifier = Modifier.align(Alignment.Center),
                state = errorState,
                onRetryClick = onRetryClick,
                onDownloadsClick = onDownloadsClick
            )
        } else {
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
                    resourcesCount = resourcesCount,
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
                    color = programUi?.color1?.toComposeColor() ?: White,
                    meditationUi = meditations,
                    articlesUi = articles,
                    exercisesUi = exercises,
                    pagerState = pagerState,
                    onMeditationClick = onMeditationClick,
                    onArticleClick = onArticleClick,
                    onExerciseClick = onExerciseClick
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
    }
}

private fun LazyListScope.pagerItem(
    modifier: Modifier,
    color: Color = White,
    meditationUi: List<MeditationUi>,
    articlesUi: List<ArticleUi>,
    exercisesUi: List<ExerciseUi>,
    pagerState: PagerState,
    onMeditationClick: (MeditationUi) -> Unit,
    onArticleClick: (ArticleUi) -> Unit,
    onExerciseClick: (ExerciseUi) -> Unit
) = item(key = "pager") {
    HorizontalPager(
        modifier = modifier.fillMaxWidth(),
        state = pagerState,
        userScrollEnabled = true
    ) { position ->
        when (position) {
            0 -> GenericPagerPage(
                color = color,
                isPremium = false,
                iconRes = R.drawable.ic_type_audio,
                isPlayable = true,
                items = meditationUi,
                itemImageUrl = { it.imageUrl.orEmpty() },
                itemTitle = { it.name },
                itemDuration = { it.duration ?: 1 },
                isPremiumContent = { it.isPremium },
                onClick = { onMeditationClick(it) }
            )

            1 -> GenericPagerPage(
                color = color,
                isPremium = false,
                iconRes = R.drawable.ic_type_read,
                isPlayable = false,
                items = articlesUi,
                itemImageUrl = { it.imageUrl.orEmpty() },
                itemTitle = { it.name },
                itemDuration = { it.duration ?: 1 },
                isPremiumContent = { it.isPremium },
                onClick = { onArticleClick(it) }
            )

            else -> GenericPagerPage(
                color = color,
                isPremium = false,
                iconRes = R.drawable.ic_type_exercise,
                isPlayable = true,
                items = exercisesUi,
                itemImageUrl = { it.imageUrl.orEmpty() },
                itemTitle = { it.name },
                itemDuration = { it.duration ?: 1 },
                isPremiumContent = { it.isPremium },
                onClick = { onExerciseClick(it) }
            )
        }
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
                modifier = Modifier.clip(RoundedCornerShape(24.dp)).size(96.dp),
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
                    Icon(
                        modifier = Modifier.align(Alignment.Center)
                            .background(color = White.copy(alpha = 0.1f), shape = CircleShape)
                            .padding(8.dp)
                            .size(16.dp),
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = null,
                        tint = White
                    )
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
fun AppBar(
    titleText: String,
    showTitle: Boolean,
    navController: NavHostController,
    onBackClick: () -> Unit = {},
    actionButton: @Composable () -> Unit? = {}
) {
    val localizedRes = LocalLocalizedRes.current
    val appBarModifier = if (showTitle) {
        Modifier.zIndex(1f).background(NavBarBackground)
    } else {
        Modifier.zIndex(1f)
    }
    Box(
        modifier = appBarModifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(AppBarHeightMedium),
    ) {
        Row(
            modifier = Modifier
                .noRippleClickable { onBackClick() }
                .align(CenterStart),
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
            Text(
                text = localizedRes.string(R.string.library_title),
                color = White,
                style = appbarMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
        Box(
            modifier = Modifier.align(Alignment.CenterEnd),

            ) {
            actionButton()
        }
        if (showTitle) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = titleText,
                color = White,
                style = appbarMedium
            )
        }
    }
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