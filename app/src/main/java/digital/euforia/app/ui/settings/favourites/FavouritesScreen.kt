package digital.euforia.app.ui.settings.favourites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.genericRowItem
import digital.euforia.app.ui.util.widget.titleItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun FavouritesScreen(
    navController: NavHostController,
    viewModel: FavouritesViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    NavBarlessScreen(navBarVisibilityState) {
        FavouritesContent(
            navController = navController,
            isLoading = state.isLoading,
            isPremium = state.isPremium,
            errorState = state.errorState,
            meditations = state.meditations,
            exercises = state.exercises,
            articles = state.articles,
            onPublicationClick = viewModel::onPublicationClicked,
            onBackClick = { navController.popBackStack() },
            onRetryClick = viewModel::onRetryClick,
            onDownloadsClick = viewModel::onDownloadsClicked,
            onMoreMeditationsClick = {},
            onMoreExercisesClick = {},
            onMoreArticlesClick = {},
            onTitleClick = viewModel::onTitleClicked
        )
    }
}

@Composable
private fun FavouritesContent(
    navController: NavHostController,
    isLoading: Boolean,
    isPremium: Boolean,
    errorState: ErrorViewState?,
    meditations: List<PublicationInfo>,
    exercises: List<PublicationInfo>,
    articles: List<PublicationInfo>,
    onPublicationClick: (PublicationInfo) -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onMoreMeditationsClick: () -> Unit,
    onMoreExercisesClick: () -> Unit,
    onMoreArticlesClick: () -> Unit,
    onTitleClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    val context = LocalContext.current
    val thresholdPx = with(density) { 16.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }
    val localizedRes = LocalLocalizedRes.current

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            backTitleRes = R.string.profile_title,
            titleRes = R.string.favorites_title,
            shouldBlur = shouldBlur,
            hazeState = hazeState,
            onBackClick = onBackClick,
            navController = navController
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
                    titleRes = R.string.favorites_title
                )
                genericRowItem(
                    title = localizedRes.string(R.string.meditations_title),
                    items = meditations,
                    onItemClick = onPublicationClick,
                    isPremium = isPremium,
                    description = null,
                    iconRes = R.drawable.ic_type_audio,
                    itemId = { it.id },
                    itemAlias = { it.alias },
                    itemTitle = { it.title },
                    isPremiumContent = { it.isPremium },
                    itemImageUrl = { it.imageUrl },
                    itemDuration = { it.durationMinutes ?: 1 },
                    onMoreClick = onMoreMeditationsClick,
                    onTitleClick = { onTitleClick(PublicationType.MEDITATION.value) }
                )
                genericRowItem(
                    title = localizedRes.string(R.string.exercises_title),
                    items = exercises,
                    onItemClick = onPublicationClick,
                    isPremium = isPremium,
                    description = null,
                    iconRes = R.drawable.ic_type_exercise,
                    itemId = { it.id },
                    itemAlias = { it.alias },
                    itemTitle = { it.title },
                    isPremiumContent = { it.isPremium },
                    itemImageUrl = { it.imageUrl },
                    itemDuration = { it.durationMinutes ?: 1 },
                    onMoreClick = onMoreExercisesClick,
                    onTitleClick = { onTitleClick(PublicationType.EXERCISE.value) }
                )
                genericRowItem(
                    title = localizedRes.string(R.string.articles_title),
                    items = articles,
                    onItemClick = onPublicationClick,
                    isPremium = isPremium,
                    description = null,
                    iconRes = R.drawable.ic_type_read,
                    itemId = { it.id },
                    itemAlias = { it.alias },
                    itemTitle = { it.title },
                    isPremiumContent = { it.isPremium },
                    itemImageUrl = { it.imageUrl },
                    itemDuration = { it.durationMinutes ?: 1 },
                    onMoreClick = onMoreArticlesClick,
                    onTitleClick = { onTitleClick(PublicationType.ARTICLE.value) }
                )
            }
        }
    }
}

private fun handleSideEffect(sideEffect: FavouritesSideEffect, navController: NavHostController) {
    when (sideEffect) {
        is FavouritesSideEffect.NavigateToPublication -> navController.navigate(
            HomeDestination.PublicationDetails(
                id = sideEffect.id,
                publicationType = sideEffect.type,
                packageTitle = ""
            )
        )

        else -> {}
    }
}