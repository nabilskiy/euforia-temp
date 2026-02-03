package digital.euforia.app.ui.programs.publications

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import digital.euforia.app.R
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.programs.publication.getIconRes
import digital.euforia.app.ui.theme.AvatarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.onDeepLinkBackClick
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
fun PublicationsScreen(
    navController: NavHostController,
    viewModel: PublicationsViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }
    NavBarlessScreen(navBarVisibilityState) {
        PublicationsContent(
            navController = navController,
            isPremium = state.isPremium,
            isLoading = state.isLoading,
            errorState = state.errorState,
            type = viewModel.type,
            publicationInfos = state.publicationInfos,
            onRetryClick = viewModel::onRetryClicked,
            onDownloadsClick = viewModel::onDownloadsClicked,
            onBackClick = { onDeepLinkBackClick(navController) },
            onPublicationClick = viewModel::onPublicationClicked,
        )
    }
}

@Composable
private fun PublicationsContent(
    navController: NavHostController,
    isPremium: Boolean,
    isLoading: Boolean,
    errorState: ErrorViewState?,
    type: PublicationType,
    publicationInfos: List<PublicationInfo>,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onBackClick: () -> Unit,
    onPublicationClick: (PublicationInfo) -> Unit,
) {
    val listState = rememberLazyListState()
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 16.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }

    BackHandler(enabled = true) { onBackClick() }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            titleRes = type.getTitle(),
            isBackAllowed = true,
            backTitleRes = R.string.back,
            shouldBlur = shouldBlur, hazeState = hazeState, onBackClick = onBackClick,
            navController = navController,
            premiumButtonState = PremiumButtonState.NONE,
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
            val itemsHazeState = dev.chrisbanes.haze.rememberHazeState()

            LazyColumn(
                modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                state = listState,
                verticalArrangement = Arrangement.Absolute.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    top = AppBarHeightMedium + 16.dp,
                    bottom = 56.dp,
                ),
            ) {
                titleItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    titleRes = type.getTitle()
                )
                items(
                    publicationInfos,
                    key = { "publication_${it.id}" },
                    contentType = { "publication" }) { publicationInfo ->

                    PublicationInfoItem(
                        publicationInfo = publicationInfo,
                        isPremium = isPremium,
                        hazeState = itemsHazeState,
                        onClick = { onPublicationClick(publicationInfo) }
                    )
                }

                item {
                    Spacer(Modifier.fillMaxWidth().height(24.dp).navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun PublicationInfoItem(
    publicationInfo: PublicationInfo,
    isPremium: Boolean,
    hazeState: HazeState,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.noRippleClickable(onClick)
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

                if (publicationInfo.publicationType == PublicationType.ARTICLE) {
                    Text(
                        text = publicationInfo.subtitle.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                        color = White.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = Ellipsis,
                    )
                }
            }

            if (publicationInfo.publicationType != PublicationType.ARTICLE) {
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
}

private fun PublicationType.getTitle(): Int {
    return when (this) {
        PublicationType.ARTICLE -> R.string.articles_title
        PublicationType.EXERCISE -> R.string.exercises_title
        PublicationType.MEDITATION -> R.string.meditations_title
    }
}

private fun handleSideEffect(sideEffect: PublicationsSideEffect, navController: NavHostController) {
    when (sideEffect) {
        is PublicationsSideEffect.NavigateToPublication -> {
            navController.navigate(
                HomeDestination.PublicationDetails(
                    id = sideEffect.id,
                    publicationType = sideEffect.type,
                    packageTitle = sideEffect.packageTitle
                )
            )
        }

        else -> {}
    }
}