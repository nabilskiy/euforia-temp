package digital.euforia.app.ui.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.domain.model.config.ProgramsConfig
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.MaxView
import digital.euforia.app.ui.util.widget.PremiumButtonState
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.titleItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ProgramsScreen(
    navController: NavHostController,
    viewModel: ProgramsViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    SubscriptionActivityLauncher { launchSubscriptionActivity ->
        ProgramsContent(
            navController = navController,
            isPremium = state.isPremium,
            isLoading = state.isLoading,
            errorState = state.errorState,
            programsConfig = state.programsConfig,
            programs = state.programs,
            articles = state.articles,
            exercises = state.exercises,
            exerciseDescription = state.exerciseTitle,
            articleDescription = state.articleTitle,
            onRetryClick = viewModel::onRetryClick,
            onDownloadsClick = viewModel::onDownloadsClicked,
            onBackClick = { navController.popBackStack() },
            onProgramClick = viewModel::onProgramClicked,
            onArticleClick = viewModel::onArticleClicked,
            onExerciseClick = viewModel::onExerciseClicked,
            launchSubscriptionActivity = launchSubscriptionActivity,

            )
    }
}

@Composable
private fun ProgramsContent(
    navController: NavHostController,
    isPremium: Boolean,
    isLoading: Boolean,
    errorState: ErrorViewState?,
    programsConfig: List<ProgramsConfig>,
    programs: List<ProgramUi>,
    articles: List<ArticleUi>,
    exercises: List<ExerciseUi>,
    exerciseDescription: String,
    articleDescription: String,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onBackClick: () -> Unit,
    onProgramClick: (ProgramUi) -> Unit,
    onArticleClick: (ArticleUi) -> Unit,
    onExerciseClick: (ExerciseUi) -> Unit,
    launchSubscriptionActivity: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val listState = rememberLazyListState()
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 16.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            // Blur when the very first list item (spacer) scrolled off enough
            // or when any next item became the first visible one.
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            titleRes = R.string.library_title,
            isBackAllowed = false,
            shouldBlur = shouldBlur, hazeState = hazeState, onBackClick = onBackClick,
            navController = navController,
            premiumButtonState = if (isPremium) PremiumButtonState.NONE else PremiumButtonState.UPGRADE,
            onUpgradeClick = launchSubscriptionActivity
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
                    top = AppBarHeightMedium + 16.dp,
                    bottom = 56.dp
                ),
            ) {
                titleItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    titleRes = R.string.library_title
                )
                searchItem()
                programsItem(
                    programs = programs,
                    isPremium = isPremium,
                    onProgramClick = onProgramClick
                )
                dividerItem()
                genericRowItem(
                    items = exercises,
                    title = localizedRes.string(R.string.exercises_title),
                    description = exerciseDescription,
                    isPremium = isPremium,
                    iconRes = R.drawable.ic_type_exercise,
                    itemId = { it.id },
                    itemAlias = { it.alias },
                    itemTitle = { it.name },
                    isPremiumContent = { it.isPremium },
                    itemImageUrl = { it.imageUrl },
                    itemDuration = { it.duration ?: 0 },
                    onMoreClick = {},
                    onItemClick = { onExerciseClick(it) }
                )
                dividerItem()
                genericRowItem(
                    items = articles,
                    title = localizedRes.string(R.string.articles_title),
                    description = articleDescription,
                    isPremium = isPremium,
                    iconRes = R.drawable.ic_type_read,
                    itemId = { it.id },
                    itemAlias = { it.alias },
                    itemTitle = { it.name },
                    isPremiumContent = { it.isPremium },
                    itemImageUrl = { it.imageUrl },
                    itemDuration = { it.duration ?: 0 },
                    onMoreClick = {},
                    onItemClick = { onArticleClick(it) }
                )
                item {
                    Spacer(modifier = Modifier.height(160.dp).navigationBarsPadding())
                }
            }
        }
    }
}

private fun LazyListScope.dividerItem() = item() {
    HorizontalDivider(
        color = White.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    )
}

private fun <T> LazyListScope.genericRowItem(
    items: List<T>,
    title: String,
    description: String,
    isPremium: Boolean,
    iconRes: Int,
    itemId: (T) -> Int,
    itemAlias: (T) -> String,
    itemTitle: (T) -> String,
    isPremiumContent: (T) -> Boolean,
    itemImageUrl: (T) -> String?,
    itemDuration: (T) -> Int,
    onMoreClick: () -> Unit,
    onItemClick: (T) -> Unit
) = item(key = "${title.lowercase()}_item") {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = description.uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = Medium),
            color = White.copy(alpha = 0.6f),
        )
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = SemiBold),
            color = White,
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items.forEach { item ->
                item(key = "item_${itemAlias(item)}_${itemId(item)}") {
                    HorizontalItemView(
                        imageUrl = itemImageUrl(item),
                        isPremiumContent = isPremiumContent(item),
                        titleText = itemTitle(item),
                        duration = itemDuration(item),
                        isPremium = isPremium,
                        iconRes = iconRes,
                        onClick = { onItemClick(item) }
                    )
                }

            }
            moreItem(onMoreClick)
        }
    }
}

private fun LazyListScope.moreItem(onClick: () -> Unit) = item {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.noRippleClickable { onClick() }.height(196.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.padding(bottom = 8.dp).size(32.dp)
                .background(color = White.copy(alpha = 0.1f), shape = CircleShape).padding(4.dp),
            painter = painterResource(R.drawable.ic_next),
            contentDescription = null,
            tint = White.copy(alpha = 0.6f)
        )
        Text(
            text = localizedRes.string(R.string.read_more),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 12.sp,
                fontWeight = SemiBold,
            ),
            color = White.copy(alpha = 0.6f),
        )
    }
}

@Composable
fun HorizontalItemView(
    imageUrl: String?,
    isPremiumContent: Boolean,
    titleText: String,
    duration: Int,
    isPremium: Boolean,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.noRippleClickable(onClick).width(196.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box() {
            AsyncImage(
                modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(24.dp)),
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            if (!isPremium && isPremiumContent) {
                MaxView(
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                )
            }
        }
        Text(
            text = titleText,
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
                text = "$duration minutes",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.ExtraLight,
                ),
                color = White.copy(alpha = 0.6f),
            )
        }
    }
}

private fun LazyListScope.programsItem(
    programs: List<ProgramUi>,
    isPremium: Boolean,
    onProgramClick: (ProgramUi) -> Unit
) =
    item(key = "programs_list_item") {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            programs.chunked(2).forEachIndexed { index, programsPair ->
                ProgramsView(
                    programs = programsPair,
                    isPremium = isPremium,
                    onProgramClick = onProgramClick
                )
            }
        }
    }

@Composable
private fun ColumnScope.ProgramsView(
    programs: List<ProgramUi>,
    isPremium: Boolean,
    onProgramClick: (ProgramUi) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        programs.forEach { programUi ->
            ProgramItemView(
                programUi = programUi,
                isPremium = isPremium,
                onProgramClick = { onProgramClick(programUi) })
        }
        if (programs.size == 1) {
            Box(
                modifier = Modifier.aspectRatio(1f).weight(1f)
            )
        }
    }
}

@Composable
private fun RowScope.ProgramItemView(
    programUi: ProgramUi,
    isPremium: Boolean,
    onProgramClick: () -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    Box(
        modifier = Modifier.noRippleClickable(onClick = { onProgramClick() }).aspectRatio(1f)
            .weight(1f)
            .clip(RoundedCornerShape(24.dp))
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            model = programUi.imageUrl,
            contentDescription = null,
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                modifier = Modifier,
                text = "${programUi.resourceCount} resources",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraLight),
                color = White.copy(alpha = 0.6f),
            )
            Text(
                modifier = Modifier,
                text = programUi.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = White,
            )
        }
        if (!isPremium && programUi.isPremium) {
            MaxView(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
        } else {
            MoreView(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
        }
    }
}

@Composable
fun MoreView(modifier: Modifier = Modifier) {
    val localizedRes = LocalLocalizedRes.current
    Text(
        text = localizedRes.string(R.string.more),
        modifier = modifier.background(
            color = White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
        )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 12.sp,
            lineHeight = 12.sp,
            fontWeight = SemiBold,
        ),
    )
}

private fun LazyListScope.searchItem() = item(key = "programs_search_item") {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(color = NavBarBackground, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val localizedRes = LocalLocalizedRes.current
        Text(
            modifier = Modifier.weight(1f),
            text = localizedRes.string(R.string.search_title),
            color = White.copy(alpha = 0.2f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = White.copy(alpha = 0.2f)
        )
    }
}

private fun handleSideEffect(sideEffect: ProgramsSideEffect, navController: NavHostController) {
    when (sideEffect) {
        is ProgramsSideEffect.NavigateToProgramDetail -> navController.navigate(
            HomeDestination.ProgramDetails(
                sideEffect.programId
            )
        )

        is ProgramsSideEffect.NavigateToPublication -> navController.navigate(
            HomeDestination.PublicationDetails(
                sideEffect.id,
                sideEffect.type,
                sideEffect.packageTitle
            )
        )

        is ProgramsSideEffect.NavigateToDownloads -> navController.navigate(HomeDestination.Downloads)

        else -> {
        }
    }
}