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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.model.config.ProgramsConfig
import digital.euforia.app.domain.usecase.program.SearchResults
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
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
import digital.euforia.app.ui.util.widget.SearchTextField
import digital.euforia.app.ui.util.widget.genericRowItem
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
            searchQuery = state.searchQuery,
            searchResults = state.searchResults,
            onRetryClick = viewModel::onRetryClick,
            onDownloadsClick = viewModel::onDownloadsClicked,
            onBackClick = { navController.popBackStack() },
            onProgramClick = viewModel::onProgramClicked,
            onArticleClick = viewModel::onArticleClicked,
            onExerciseClick = viewModel::onExerciseClicked,
            onMoreSearchedMeditationsClick = viewModel::onMoreSearchedMeditationsClicked,
            onMoreExercisesClick = viewModel::onMoreExercisesClicked,
            onMoreArticlesClick = viewModel::onMoreArticlesClicked,
            launchSubscriptionActivity = launchSubscriptionActivity,
            onSearchQueryChanged = viewModel::onSearchQueryChanged
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
    articles: List<PublicationInfo>,
    exercises: List<PublicationInfo>,
    exerciseDescription: String,
    articleDescription: String,
    searchQuery: String?,
    searchResults: SearchResults?,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onBackClick: () -> Unit,
    onProgramClick: (ProgramUi) -> Unit,
    onArticleClick: (PublicationInfo) -> Unit,
    onExerciseClick: (PublicationInfo) -> Unit,
    onMoreSearchedMeditationsClick: () -> Unit,
    onMoreExercisesClick: () -> Unit,
    onMoreArticlesClick: () -> Unit,
    launchSubscriptionActivity: () -> Unit,
    onSearchQueryChanged: (String?) -> Unit
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current


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
                searchItem(
                    text = searchQuery,
                    focusManager = focusManager,
                    keyboardController = keyboardController,
                    onTextChanged = onSearchQueryChanged,
                    onCancelClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onSearchQueryChanged(null)
                    }
                )
                if (searchResults != null) {
                    if (searchQuery != null) {
                        if (searchResults.meditations.isNotEmpty()) {
                            genericRowItem(
                                items = searchResults.meditations,
                                title = localizedRes.string(R.string.meditations_title),
                                description = localizedRes.string(R.string.search_result_section_see_more),
                                isPremium = isPremium,
                                iconRes = R.drawable.ic_type_audio,
                                itemId = { it.id },
                                itemAlias = { it.alias },
                                itemTitle = { it.title },
                                isPremiumContent = { it.isPremium },
                                itemImageUrl = { it.imageUrl },
                                itemDuration = { it.durationMinutes ?: 0 },
                                onMoreClick = onMoreSearchedMeditationsClick,
                                onItemClick = { onArticleClick(it) }
                            )
                        }
                        if (searchResults.exercises.isNotEmpty()) {
                            genericRowItem(
                                items = searchResults.exercises,
                                title = localizedRes.string(R.string.exercises_title),
                                description = localizedRes.string(R.string.search_result_section_see_more),
                                isPremium = isPremium,
                                iconRes = R.drawable.ic_type_exercise,
                                itemId = { it.id },
                                itemAlias = { it.alias },
                                itemTitle = { it.title },
                                isPremiumContent = { it.isPremium },
                                itemImageUrl = { it.imageUrl },
                                itemDuration = { it.durationMinutes ?: 0 },
                                onMoreClick = onMoreExercisesClick,
                                onItemClick = { onExerciseClick(it) }
                            )
                        }
                        if (searchResults.articles.isNotEmpty()) {
                            genericRowItem(
                                items = searchResults.articles,
                                title = localizedRes.string(R.string.articles_title),
                                description = localizedRes.string(R.string.search_result_section_see_more),
                                isPremium = isPremium,
                                iconRes = R.drawable.ic_type_read,
                                itemId = { it.id },
                                itemAlias = { it.alias },
                                itemTitle = { it.title },
                                isPremiumContent = { it.isPremium },
                                itemImageUrl = { it.imageUrl },
                                itemDuration = { it.durationMinutes ?: 0 },
                                onMoreClick = onMoreArticlesClick,
                                onItemClick = { onArticleClick(it) }
                            )
                        }
                    } else {

                    }
                } else {
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
                        itemTitle = { it.title },
                        isPremiumContent = { it.isPremium },
                        itemImageUrl = { it.imageUrl },
                        itemDuration = { it.durationMinutes ?: 0 },
                        onMoreClick = onMoreExercisesClick,
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
                        itemTitle = { it.title },
                        isPremiumContent = { it.isPremium },
                        itemImageUrl = { it.imageUrl },
                        itemDuration = { it.durationMinutes ?: 0 },
                        onMoreClick = onMoreArticlesClick,
                        onItemClick = { onArticleClick(it) }
                    )
                }
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
                modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(32.dp)),
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
            modifier = Modifier.padding(horizontal = 8.dp),
            text = titleText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
            color = White,
            maxLines = 3,
            overflow = Ellipsis
        )
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

private fun LazyListScope.searchItem(
    text: String?,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    onTextChanged: (String?) -> Unit,
    onCancelClick: () -> Unit
) = item(key = "search_item") {
    SearchTextField(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        value = text.orEmpty(),
        onValueChanged = { onTextChanged(it.text) },
        focusManager = focusManager,
        keyboardController = keyboardController,
        onCancelClick = onCancelClick,
        onClearClick = { onTextChanged(null) }
    )
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

        is ProgramsSideEffect.NavigateToPublications -> navController.navigate(
            HomeDestination.Publications(
                type = sideEffect.publicationType,
                ids = sideEffect.ids
            )
        )

        is ProgramsSideEffect.NavigateToExercises -> navController.navigate(HomeDestination.Exercises)

        else -> {}
    }
}