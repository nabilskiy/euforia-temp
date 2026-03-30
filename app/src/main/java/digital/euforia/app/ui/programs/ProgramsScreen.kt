package digital.euforia.app.ui.programs

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.model.config.ProgramsConfig
import digital.euforia.app.domain.usecase.program.SearchResults
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.programs.publication.PublicationType
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
            isSearchLoading = state.isSearchLoading,
            errorState = state.errorState,
            programsConfig = state.programsConfig,
            programs = state.programs,
            articles = state.articles,
            exercises = state.exercises,
            exerciseDescription = state.exerciseTitle,
            articleDescription = state.articleTitle,
            searchQuery = state.searchQuery,
            searchResults = state.searchResults,
            suggestions = state.searchSuggestions,
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
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onSuggestionClick = viewModel::onSuggestionClicked,
            onSearchClick = viewModel::onSearchClicked,
            onTitleClick = viewModel::onTitleClicked
        )
    }
}

@Composable
private fun ProgramsContent(
    navController: NavHostController,
    isPremium: Boolean,
    isLoading: Boolean,
    isSearchLoading: Boolean,
    errorState: ErrorViewState?,
    programsConfig: List<ProgramsConfig>,
    programs: List<ProgramUi>,
    articles: List<PublicationInfo>,
    exercises: List<PublicationInfo>,
    exerciseDescription: String,
    articleDescription: String,
    searchQuery: String?,
    searchResults: SearchResults?,
    suggestions: List<String>,
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
    onSearchQueryChanged: (String?) -> Unit,
    onSuggestionClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onTitleClick: (String) -> Unit,
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

    // Overlay state for currently long-pressed program item
    data class ProgramOverlay(
        val left: Int,
        val top: Int,
        val width: Int,
        val height: Int,
        val program: ProgramUi
    )

    var programOverlay by remember { mutableStateOf<ProgramOverlay?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            titleRes = R.string.library_title,
            isBackAllowed = false,
            shouldBlur = shouldBlur, hazeState = hazeState, onBackClick = onBackClick,
            navController = navController,
            premiumButtonState = if (isPremium) PremiumButtonState.NONE else PremiumButtonState.UPGRADE,
            onUpgradeClick = launchSubscriptionActivity
        )

        // Fullscreen blur overlay and floating pressed item replica (drawn above content when active)
        val overlay = programOverlay
        if (overlay != null) {
            // Blur everything behind
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.regular(NavBarBackground)
                    )
                    .zIndex(2f)
            )
            // Draw the pressed program card above the blur at its original position
            val widthDp = with(LocalDensity.current) { overlay.width.toDp() }
            val heightDp = with(LocalDensity.current) { overlay.height.toDp() }
            val leftDp = with(LocalDensity.current) { overlay.left.toDp() }
            val topDp = with(LocalDensity.current) { overlay.top.toDp() }
            Box(
                modifier = Modifier
                    .zIndex(3f)
                    .offset(x = leftDp, y = topDp)
                    .size(widthDp, heightDp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                    }
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    model = overlay.program.imageUrl,
                    contentDescription = null,
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "${overlay.program.resourceCount} resources",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraLight),
                        color = White.copy(alpha = 0.6f),
                    )
                    Text(
                        modifier = Modifier,
                        text = overlay.program.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = White,
                    )
                }
                if (!isPremium && overlay.program.isPremium) {
                    MaxView(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
                } else {
                    MoreView(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
                }
            }
        }

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
                    isLoading = isSearchLoading,
                    onTextChanged = onSearchQueryChanged,
                    onCancelClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onSearchQueryChanged(null)
                    },
                    onSearchClick = onSearchClick
                )
                if (searchQuery != null) {
                    if (searchResults != null && searchResults.hasResults()) {
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
                                onItemClick = { onArticleClick(it) },
                                onTitleClick = { onTitleClick(PublicationType.MEDITATION.value) }
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
                                onItemClick = { onExerciseClick(it) },
                                onTitleClick = { onTitleClick(PublicationType.EXERCISE.value) }

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
                                onItemClick = { onArticleClick(it) },
                                onTitleClick = { onTitleClick(PublicationType.ARTICLE.value) }
                            )
                        }
                    } else {
                        suggestionItem(
                            suggestions = suggestions,
                            onClick = onSuggestionClick
                        )
                    }
                } else {
                    programsItem(
                        programs = programs,
                        isPremium = isPremium,
                        onProgramClick = onProgramClick,
                        onProgramLongPressStart = { program, left, top, width, height ->
                            programOverlay = ProgramOverlay(left, top, width, height, program)
                        },
                        onProgramLongPressEnd = {
                            programOverlay = null
                        },
                        activeOverlayProgramId = programOverlay?.program?.id
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
                        onItemClick = { onExerciseClick(it) },
                        onTitleClick = { onTitleClick(PublicationType.EXERCISE.value) }
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
                        onItemClick = { onArticleClick(it) },
                        onTitleClick = { onTitleClick(PublicationType.ARTICLE.value) }
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
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 1.05f else 1f, label = "scale")
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 0.dp,
        label = "elevation"
    )

    Column(
        modifier = Modifier
            .width(196.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, RoundedCornerShape(32.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { isPressed = true },
                    onPress = {
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
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
    onProgramClick: (ProgramUi) -> Unit,
    onProgramLongPressStart: (ProgramUi, left: Int, top: Int, width: Int, height: Int) -> Unit,
    onProgramLongPressEnd: () -> Unit,
    activeOverlayProgramId: Int?
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
                    onProgramClick = onProgramClick,
                    onProgramLongPressStart = onProgramLongPressStart,
                    onProgramLongPressEnd = onProgramLongPressEnd,
                    activeOverlayProgramId = activeOverlayProgramId
                )
            }
        }
    }

@Composable
private fun ColumnScope.ProgramsView(
    programs: List<ProgramUi>,
    isPremium: Boolean,
    onProgramClick: (ProgramUi) -> Unit,
    onProgramLongPressStart: (ProgramUi, left: Int, top: Int, width: Int, height: Int) -> Unit,
    onProgramLongPressEnd: () -> Unit,
    activeOverlayProgramId: Int?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        programs.forEach { programUi ->
            ProgramItemView(
                programUi = programUi,
                isPremium = isPremium,
                activeOverlayProgramId = activeOverlayProgramId,
                onProgramLongPressStart = onProgramLongPressStart,
                onProgramLongPressEnd = onProgramLongPressEnd,
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
    activeOverlayProgramId: Int?,
    onProgramLongPressStart: (ProgramUi, left: Int, top: Int, width: Int, height: Int) -> Unit,
    onProgramLongPressEnd: () -> Unit,
    onProgramClick: () -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 1.05f else 1f, label = "scale")
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 0.dp,
        label = "elevation"
    )
    var lastBounds by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .weight(1f)
            .onGloballyPositioned { coords ->
                lastBounds = coords.boundsInRoot()
            }
            .graphicsLayer {
                // Hide the original when overlay is active for this item
                alpha = if (activeOverlayProgramId == programUi.id) 0f else 1f
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        isPressed = true
                        val rect = lastBounds
                        onProgramLongPressStart(
                            programUi,
                            rect.left.toInt(),
                            rect.top.toInt(),
                            rect.width.toInt(),
                            rect.height.toInt()
                        )
                    },
                    onPress = {
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                            onProgramLongPressEnd()
                        }
                    },
                    onTap = { onProgramClick() }
                )
            }
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
    isLoading: Boolean,
    onTextChanged: (String?) -> Unit,
    onCancelClick: () -> Unit,
    onSearchClick: () -> Unit
) = item(key = "search_item") {
    SearchTextField(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        value = text.orEmpty(),
        onValueChanged = { onTextChanged(it.text) },
        focusManager = focusManager,
        keyboardController = keyboardController,
        onCancelClick = onCancelClick,
        isLoading = isLoading,
        onClearClick = { onTextChanged(null) },
        onClick = onSearchClick
    )
}

private fun LazyListScope.suggestionItem(
    suggestions: List<String>,
    onClick: (Int) -> Unit
) = item(key = "suggestions") {
    val localizedRes = LocalLocalizedRes.current

    Column(
        modifier = Modifier.fillParentMaxWidth()
            .padding(top = 96.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = localizedRes.string(R.string.search_empty_not_found),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = White,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = localizedRes.string(R.string.search_empty_not_found_details),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal
            ),
            color = White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            suggestions.forEachIndexed { index, suggestion ->
                SuggestionChip(
                    text = suggestion,
                    index = index,
                    onClick = { onClick(index) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(text: String, index: Int, onClick: () -> Unit) {
    TextButton(
        modifier = Modifier.padding(horizontal = 4.dp),
        border = BorderStroke(1.dp, White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = NavBarBackground
        ),
        onClick = onClick,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = White,
            modifier = Modifier.padding(8.dp)
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