package digital.euforia.app.ui.programs.publication

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.navigation.Splash
import digital.euforia.app.ui.programs.article.ArticleBottomSheet
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.onDeepLinkBackClick
import digital.euforia.app.ui.util.shadow
import digital.euforia.app.ui.util.sharePublication
import digital.euforia.app.ui.util.toComposeColor
import digital.euforia.app.ui.util.toDateString
import digital.euforia.app.ui.util.widget.AnimatedSizeBox
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.MaxView
import digital.euforia.app.ui.util.widget.PremiumButtonState
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.PublicationOptionMenu
import digital.euforia.app.ui.util.widget.applyIf
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun PublicationScreen(
    navController: NavHostController,
    viewModel: PublicationViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    var isArticleSheetVisible = remember { mutableStateOf(false) }

    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(
            sideEffect = sideEffect,
            navController = navController,
            context = context,
            openArticle = {
                viewModel.onArticleReaderShow()
                isArticleSheetVisible.value = true
            })
    }

    NavBarlessScreen(navBarVisibilityState) {
        SubscriptionActivityLauncher { launchSubscriptionActivity ->
            PublicationContent(
                publicationInfo = state.publicationInfo,
                navController = navController,
                isPremium = state.isPremium,
                isLoading = state.isLoading,
                errorState = state.errorState,
                programTitle = viewModel.packageTitle.orEmpty(),
                articleSheetState = isArticleSheetVisible,
                onRetryClick = viewModel::onRetryClicked,
                onDownloadsClick = viewModel::onDownloadsClicked,
                similarItems = state.similarPublications,
                onSimilarItemClick = viewModel::onPublicationClicked,
                onShowSimilarClick = viewModel::onShowSimilarClicked,
                onBackClick = { onDeepLinkBackClick(navController) },
                onPlayClick = {
                    if (state.publicationInfo?.isPremium == true && !state.isPremium) {
                        launchSubscriptionActivity()
                    } else {
                        viewModel.onPlayClicked()
                    }
                },
                onFavouriteClick = viewModel::onFavouriteClicked,
                launchSubscriptionActivity = launchSubscriptionActivity,
                onPackageClick = viewModel::onPackageClicked,
                onShareClick = viewModel::onShareClicked
            )
        }
    }
}

@Composable
private fun PublicationContent(
    publicationInfo: PublicationInfo?,
    navController: NavHostController,
    isPremium: Boolean,
    isLoading: Boolean,
    errorState: ErrorViewState?,
    programTitle: String,
    similarItems: List<PublicationInfo>,
    articleSheetState: MutableState<Boolean>,
    onSimilarItemClick: (PublicationInfo) -> Unit,
    onShowSimilarClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onFavouriteClick: (PublicationInfo) -> Unit,
    launchSubscriptionActivity: () -> Unit,
    onPackageClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 16.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            // blur when the very first list item (spacer) scrolled off enough
            // or when any next item became the first visible one.
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }
    val stretchResistance = 0.5f
    val maxHeaderHeightPx = with(density) { 360.dp.toPx() }
    val minHeaderHeightPx = with(density) { 120.dp.toPx() }
    val maxStretchPx = with(density) { 240.dp.toPx() }

    val minOffsetPx = -(maxHeaderHeightPx - minHeaderHeightPx)
    val maxOffsetPx = maxStretchPx

    // We animate this value. When user drags, we snap to it; when released, we animate back.
    val headerOffset = remember { Animatable(0f) }

    fun isListAtTop(): Boolean =
        listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0

    val nestedScrollConnection = remember(listState) {
        object : NestedScrollConnection {

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy >= 0f) return Offset.Zero

                // scrolling up -> collapse header first (until min), then let list scroll.
                val old = headerOffset.value
                val new = (old + dy).coerceIn(minOffsetPx, maxOffsetPx)
                val applied = new - old

                if (applied != 0f) {
                    scope.launch {
                        headerOffset.snapTo(new)
                    }
                    return Offset(0f, applied) // consume only what we applied to header
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val dy = available.y
                if (dy <= 0f) return Offset.Zero

                // pulling down -> stretch only when list is really at top
                if (!isListAtTop()) return Offset.Zero

                val old = headerOffset.value
                val new = (old + dy * stretchResistance).coerceIn(minOffsetPx, maxOffsetPx)
                val applied = new - old

                if (applied != 0f) {
                    scope.launch {
                        headerOffset.snapTo(new)
                    }
                    return Offset(0f, applied)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // when user releases after stretching, snap back to normal (0)
                if (headerOffset.value > 0f) {
                    headerOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    // we consumed the “stretch”; let fling continue normally for list if needed
                }
                return super.onPreFling(available)
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                // if still stretched after fling, return to normal
                if (headerOffset.value > 0f) {
                    headerOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    val headerHeightDp: Dp by remember {
        derivedStateOf {
            val heightPx = maxHeaderHeightPx + headerOffset.value
            with(density) { heightPx.toDp() }
        }
    }

    BackHandler(enabled = true) { onBackClick() }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            titleRes = R.string.library_title,
            isBackAllowed = true,
            shouldBlur = shouldBlur, hazeState = hazeState, onBackClick = onBackClick,
            navController = navController,
            premiumButtonState = PremiumButtonState.NONE,
            actionButton = {
                publicationInfo?.let { publicationInfo ->
                    ActionsView(
                        publicationInfo = publicationInfo,
                        isPremium = isPremium,
                        isFavourite = publicationInfo.isFavourite,
                        onFavouriteClick = onFavouriteClick,
                        onShareClick = onShareClick,
                        launchSubscriptionActivity = launchSubscriptionActivity
                    )
                }
            }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                    state = listState,
                    contentPadding = PaddingValues(
                        bottom = 56.dp
                    ),
                ) {
                    playItem(
                        modifier = Modifier.fillMaxWidth().height(headerHeightDp),
                        imageUrl = publicationInfo?.imageUrl ?: "",
                        onClick = onPlayClick,
                        showPlayButton = publicationInfo?.publicationType != PublicationType.ARTICLE,
                    )

                    publicationInfo?.let {

                        infoItem(
                            publicationInfo = it,
                            programTitle = programTitle,
                            onPackageClick = onPackageClick
                        )
                    }

                    if (similarItems.isNotEmpty()) {
                        similarItem(
                            similarItems = similarItems,
                            onClick = onSimilarItemClick,
                            onShowSimilarClick = onShowSimilarClick
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.fillMaxWidth().height(152.dp))
                    }
                }
                if (shouldBlur) {
                    PlayButton(
                        modifier = Modifier.noRippleClickable(onPlayClick)
                            .align(Alignment.BottomCenter),
                        type = publicationInfo?.publicationType
                            ?: PublicationType.MEDITATION
                    )
                }

                if (articleSheetState.value) {
                    ArticleBottomSheet() {
                        articleSheetState.value = false
                    }
                }
            }
        }
    }
}

fun LazyListScope.playItem(
    modifier: Modifier = Modifier,
    imageUrl: String,
    onClick: () -> Unit,
    showPlayButton: Boolean = true,
) = item {
        Box(modifier = modifier) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = imageUrl,
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
            if (showPlayButton) {
                Icon(
                    modifier = Modifier.align(Alignment.Center)
                        .noRippleClickable(onClick)
                        .background(color = White.copy(alpha = 0.95f), shape = CircleShape)
                        .padding(20.dp)
                        .size(20.dp),
                    painter = painterResource(R.drawable.ic_play),
                    tint = Black,
                    contentDescription = null
                )
            }
        }
    }

fun LazyListScope.infoItem(
    modifier: Modifier = Modifier,
    publicationInfo: PublicationInfo,
    programTitle: String,
    onPackageClick: () -> Unit
) = item(key = "info") {
    val localizedRes = LocalLocalizedRes.current
    val color = publicationInfo.color1?.toComposeColor() ?: White
    Column(
        modifier = Modifier
            .offset(y = (-32).dp)
            .fillMaxWidth().background(
                PrimaryBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ).padding(top = 24.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (programTitle.isNotEmpty() && publicationInfo.publicationType == PublicationType.MEDITATION) {
            Text(
                modifier = Modifier
                    .noRippleClickable(onPackageClick)
                    .background(
                        color = color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                text = programTitle.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(color = color)
            )
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = publicationInfo.title.orEmpty(),
            style = MaterialTheme.typography.headlineSmall,
            color = White,
        )
        HorizontalDivider(
            color = White.copy(alpha = 0.1f),
            thickness = 1.dp,
            modifier = Modifier
                .padding(top = 8.dp)
                .width(64.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoView(
                text = "${publicationInfo.durationMinutes} minutes",
                iconRes = R.drawable.ic_timer,
                color = color.copy(alpha = 0.7f)
            )
            InfoView(
                text = localizedRes.string(publicationInfo.publicationType.getTitleRes()),
                iconRes = publicationInfo.publicationType.getIconRes(),
                color = color.copy(alpha = 0.7f)
            )

            publicationInfo.publishedAt?.toDateString()?.let {
                InfoView(
                    text = it,
                    iconRes = R.drawable.ic_calendar,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }

        Text(
            modifier = Modifier,
            text = publicationInfo.subtitle.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = White.copy(alpha = 0.7f)
        )
        HorizontalDivider(
            color = White.copy(alpha = 0.1f),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
        val titleRes = when (publicationInfo.publicationType) {
            PublicationType.ARTICLE ->
                R.string.about_article

            PublicationType.EXERCISE ->
                R.string.about_exercise

            PublicationType.MEDITATION ->
                R.string.about_meditation
        }

        val descriptionRes = when (publicationInfo.publicationType) {
            PublicationType.ARTICLE ->
                R.string.about_article_text

            PublicationType.EXERCISE ->
                R.string.about_exercise_text

            PublicationType.MEDITATION ->
                R.string.about_meditation_text
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = localizedRes.string(titleRes),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = SemiBold
            ),
            color = White
        )
        Text(
            modifier = Modifier,
            text = localizedRes.string(descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = White.copy(alpha = 0.7f)
        )
    }
}

private fun LazyListScope.similarItem(
    similarItems: List<PublicationInfo>,
    onClick: (PublicationInfo) -> Unit,
    onShowSimilarClick: () -> Unit
) = item(key = "similar") {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            color = White.copy(alpha = 0.1f),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier,
                text = localizedRes.string(R.string.similar_publications_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = SemiBold
                ),
                color = White
            )

            Icon(
                modifier = Modifier.noRippleClickable(onShowSimilarClick).size(24.dp),
                painter = painterResource(R.drawable.ic_next),
                contentDescription = null,
                tint = White.copy(0.7f)
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            similarItems.forEach { publicationInfo ->
                similarRowItem(publicationInfo) { onClick(publicationInfo) }
            }
        }
    }
}

private fun LazyListScope.similarRowItem(publicationInfo: PublicationInfo, onClick: () -> Unit) =
    item(key = "similar_${publicationInfo.id}") {
        val localizedRes = LocalLocalizedRes.current
        val configuration = LocalConfiguration.current
        val imageSizeDp = (configuration.screenWidthDp * 0.6f).dp
        Column(
            modifier = Modifier.noRippleClickable(onClick).width(imageSizeDp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .size(width = imageSizeDp, height = imageSizeDp),
                model = publicationInfo.imageUrl,
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
            Text(
                text = publicationInfo.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                color = White,
            )
            Row() {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(publicationInfo.publicationType.getIconRes()),
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
                    text = "${publicationInfo.durationMinutes} minutes",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraLight,
                    ),
                    color = White.copy(alpha = 0.6f),
                )
            }
        }
    }


@Composable
private fun InfoView(text: String, iconRes: Int, color: Color) {
    val localizedRes = LocalLocalizedRes.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PlayButton(modifier: Modifier = Modifier, type: PublicationType) {
    val localizedRes = LocalLocalizedRes.current
    val iconRes = when (type) {
        PublicationType.ARTICLE -> R.drawable.ic_type_read
        else -> R.drawable.ic_play
    }
    val titleRes = when (type) {
        PublicationType.ARTICLE -> R.string.publication_read
        else -> R.string.publication_play
    }

    AnimatedSizeBox(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 24.dp, start = 48.dp, end = 48.dp)
            .background(color = White, shape = CircleShape)
            .shadow(
                color = White,
                blurRadius = 16.dp,
                borderRadius = 30.dp,
                spread = 2.dp
            )
    ) {
        Row(
            modifier = Modifier.height(56.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.padding(end = 8.dp).size(16.dp),
                painter = painterResource(iconRes),
                tint = Black,
                contentDescription = null

            )
            Text(
                text = localizedRes.string(titleRes),
                color = Black,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun ActionsView(
    publicationInfo: PublicationInfo,
    isPremium: Boolean,
    isFavourite: Boolean,
    onFavouriteClick: (PublicationInfo) -> Unit,
    onShareClick: () -> Unit,
    launchSubscriptionActivity: () -> Unit,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (publicationInfo.isPremium) {
            MaxView(
                modifier = Modifier
                    .applyIf(!isPremium) {
                        noRippleClickable(launchSubscriptionActivity)
                    }
            )
        }
        PublicationOptionMenu(
            expanded = expanded,
            isFavourite = isFavourite,
            onAddFavouriteClick = { onFavouriteClick(publicationInfo) },
            onExpandedChange = { expanded = !expanded },
            onShareClick = onShareClick,
        )
    }
}

private fun handleSideEffect(
    sideEffect: PublicationSideEffect,
    navController: NavHostController,
    context: Context,
    openArticle: () -> Unit
) {
    when (sideEffect) {
        is PublicationSideEffect.NavigateToPublication -> {
            navController.navigate(
                HomeDestination.PublicationDetails(
                    id = sideEffect.id,
                    publicationType = sideEffect.type,
                    packageTitle = sideEffect.packageTitle
                )
            )
        }

        is PublicationSideEffect.NavigateToSimilar -> {
            navController.navigate(
                HomeDestination.Publications(
                    type = sideEffect.publicationType,
                    ids = sideEffect.ids
                )
            )
        }

        is PublicationSideEffect.OpenArticle -> openArticle()
        is PublicationSideEffect.OpenExercise -> {
            navController.navigate(
                HomeDestination.PublicationPlayer(
                    id = sideEffect.id,
                    publicationType = PublicationType.EXERCISE
                )
            )
        }

        is PublicationSideEffect.OpenMeditation -> {
            navController.navigate(
                HomeDestination.PublicationPlayer(
                    id = sideEffect.id,
                    publicationType = PublicationType.MEDITATION
                )
            )
        }

        is PublicationSideEffect.NavigateToPackage -> {
            navController.navigate(
                HomeDestination.ProgramDetails(
                    programId = sideEffect.packageId
                )
            )
        }

        is PublicationSideEffect.Share -> {
            sharePublication(
                context = context,
                publicationType = sideEffect.type,
                id = sideEffect.id,
            )
        }

        else -> {}
    }
}