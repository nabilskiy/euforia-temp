package digital.euforia.app.ui.programs.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
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
import digital.euforia.app.domain.usecase.program.ExerciseUiBlock
import digital.euforia.app.domain.usecase.program.ExerciseUiBlock.ExerciseList
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.programs.ProgramsSideEffect
import digital.euforia.app.ui.programs.publication.getIconRes
import digital.euforia.app.ui.theme.AvatarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.MaxView
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.genericRowItem
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.titleItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ExercisesScreen(
    navController: NavHostController,
    viewModel: ExercisesViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    ExercisesContent(
        navController = navController,
        isLoading = state.isLoading,
        errorState = state.errorState,
        exerciseBlocks = state.exerciseBlocks,
        isPremium = state.isPremium,
        onBackClick = {
            navController.popBackStack()
        },
        onExerciseClick = viewModel::onExerciseClicked,
        onMoreClick = viewModel::onMoreClicked,
        onBannerClick = viewModel::onBannerClicked,
        onRetryClick = viewModel::onRetryClick,
        onDownloadsClick = viewModel::onDownloadsClicked
    )
}

@Composable
private fun ExercisesContent(
    navController: NavHostController,
    isPremium: Boolean,
    isLoading: Boolean,
    errorState: ErrorViewState?, exerciseBlocks: List<ExerciseUiBlock>,
    onBackClick: () -> Unit,
    onExerciseClick: (PublicationInfo) -> Unit,
    onMoreClick: (List<PublicationInfo>) -> Unit,
    onBannerClick: (ExerciseUiBlock.Banner) -> Unit,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit
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
            // Blur when the very first list item (spacer) scrolled off enough
            // or when any next item became the first visible one.
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }
    val itemsHazeState = rememberHazeState()


    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            backTitleRes = R.string.library_title,
            titleRes = R.string.exercises_title,
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
                    titleRes = R.string.exercises_title
                )

                exerciseBlocks.forEachIndexed { index, block ->
                    when (block) {
                        is ExerciseList -> genericRowItem(
                            items = block.publicationInfoList,
                            title = block.title,
                            description = block.description,
                            isPremium = isPremium,
                            iconRes = R.drawable.ic_type_exercise,
                            itemId = { it.id },
                            itemAlias = { it.alias },
                            itemTitle = { it.title },
                            isPremiumContent = { it.isPremium },
                            itemImageUrl = { it.imageUrl },
                            itemDuration = { it.durationMinutes ?: 0 },
                            onMoreClick = { onMoreClick(block.publicationInfoList) },
                            onItemClick = { onExerciseClick(it) }
                        )

                        is ExerciseUiBlock.Banner -> {
                            bannerItem(
                                index = index,
                                banner = block,
                                onClick = onBannerClick
                            )
                        }

                        is ExerciseUiBlock.Exercise -> {
                            exerciseItem(
                                index = index,
                                publicationInfo = block.publicationInfo,
                                isPremium = isPremium,
                                hazeState = itemsHazeState,
                                onClick = onExerciseClick
                            )
                        }

                        is ExerciseUiBlock.Category -> {
                            genericRowItem(
                                items = block.publicationInfoList,
                                title = block.title,
                                description = null,
                                isPremium = isPremium,
                                iconRes = R.drawable.ic_type_exercise,
                                itemId = { it.id },
                                itemAlias = { it.alias },
                                itemTitle = { it.title },
                                isPremiumContent = { it.isPremium },
                                itemImageUrl = { it.imageUrl },
                                itemDuration = { it.durationMinutes ?: 0 },
                                onMoreClick = { onMoreClick(block.publicationInfoList) },
                                onItemClick = { onExerciseClick(it) }
                            )
                        }

                        else -> {}
                    }
                }
                item {
                    Spacer(modifier = Modifier.navigationBarsPadding().padding(bottom = 96.dp))
                }
            }
        }
    }
}

private fun LazyListScope.bannerItem(
    index: Int,
    banner: ExerciseUiBlock.Banner,
    onClick: (ExerciseUiBlock.Banner) -> Unit
) = item(key = "banner_${index}") {
    AsyncImage(
        model = banner.imageUrl,
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .fillMaxWidth()
            .noRippleClickable {
                onClick(banner)
            }
    )
}

private fun LazyListScope.exerciseItem(
    index: Int,
    publicationInfo: PublicationInfo,
    isPremium: Boolean,
    hazeState: HazeState,
    onClick: (PublicationInfo) -> Unit,
) = item(key = "exercise_$index") {

    Box(
        modifier = Modifier.noRippleClickable { onClick(publicationInfo) }
            .padding(horizontal = 16.dp).clip(RoundedCornerShape(32.dp))
            .fillMaxWidth(),
    ) {
        AsyncImage(
            modifier = Modifier.aspectRatio(1f)
                .hazeSource(hazeState),
            model = publicationInfo.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        if (!isPremium && publicationInfo.isPremium) {
            MaxView(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            )
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.ultraThin(AvatarBackground)
                )
                .zIndex(1f)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

            Icon(
                modifier = Modifier.background(
                    color = White.copy(alpha = 0.1f),
                    shape = CircleShape
                ).padding(10.dp).size(14.dp),
                painter = painterResource(id = R.drawable.ic_play),
                contentDescription = null,
                tint = White
            )
        }
    }
}


private fun handleSideEffect(sideEffect: ExercisesSideEffect, navController: NavHostController) {
    when (sideEffect) {
        is ExercisesSideEffect.NavigateToPublication -> navController.navigate(
            HomeDestination.PublicationDetails(
                sideEffect.id,
                sideEffect.type,
                sideEffect.packageTitle
            )
        )

        is ExercisesSideEffect.NavigateToPublications -> navController.navigate(
            HomeDestination.Publications(
                type = sideEffect.publicationType,
                ids = sideEffect.ids
            )
        )

        is ExercisesSideEffect.NavigateToDownloads -> navController.navigate(HomeDestination.Downloads)
        else -> {}
    }
}