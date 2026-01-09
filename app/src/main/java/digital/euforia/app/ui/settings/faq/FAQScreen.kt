package digital.euforia.app.ui.settings.faq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.widget.AnimatedSizeButton
import digital.euforia.app.ui.util.widget.noRippleClickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.NoConnectionView
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun FAQScreen(
    navController: NavHostController,
    viewModel: FAQViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    FAQContent(
        isLoading = state.isLoading,
        errorState = state.errorState,
        categories = state.uiFaqCategories,
        analyticSender = viewModel.analyticSender,
        onBackClick = { navController.popBackStack() },
        onDownloadsClick = { navController.navigate(HomeDestination.Downloads) },
        onRetryClick = viewModel::loadFAQCategories,
    )
}

@Composable
private fun FAQContent(
    isLoading: Boolean,
    errorState: ErrorViewState?,
    categories: List<UiFAQCategory>,
    analyticSender: AnalyticSender,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onBackClick: () -> Unit
) {
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
        AppBar(shouldBlur = shouldBlur, hazeState = hazeState, onBackClick = onBackClick)

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
                    start = 16.dp,
                    end = 16.dp,
                    top = AppBarHeightMedium + 16.dp,
                    bottom = 56.dp
                ),
            ) {
                item { Spacer(modifier = Modifier.fillMaxWidth().statusBarsPadding()) }
                categories.forEach { category ->
                    categoryItem(category) { analyticSender.faqItemClick() }
                }
                if (categories.isNotEmpty()) {
                    questionItem { analyticSender.faqSupportClick() }
                    item {
                        Spacer(
                            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                                .padding(top = 36.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.categoryItem(category: UiFAQCategory, onClick: () -> Unit) =
    item(key = category.id) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = spacedBy(16.dp)
        ) {
            Text(
                text = category.title,
                color = White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = SemiBold
                )
            )

            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                category.items.forEach { item ->
                    faqItem(item = item, onClick = onClick)
                    if (item != category.items.last()) {
                        Divider()
                    }
                }
            }
        }
    }

@Composable
fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(0.5.dp)
            .background(White.copy(alpha = 0.2f))
    )
}

@Composable
private fun faqItem(item: UiFAQItem, onClick: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val iconAngle by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "faq_icon_rotation"
    )
    Column(modifier = Modifier.noRippleClickable {
        onClick()
        isExpanded = !isExpanded
    }.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = item.question,
                color = White,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                painter = painterResource(R.drawable.ic_next),
                contentDescription = null,
                tint = White,
                modifier = Modifier.rotate(iconAngle)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = item.answer,
                color = White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun LazyListScope.questionItem(onClick: () -> Unit) = item(key = "question_item") {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier
                .clickable { onClick() }
                .padding(vertical = 12.dp),
            text = localizedRes.string(R.string.faq_footer_text),
            color = White,
            style = MaterialTheme.typography.bodyLarge
        )
        AnimatedSizeButton(
            text = localizedRes.string(R.string.faq_footer_support),
            onClick = onClick,
            suppressInitialAnimation = true,
            buttonColor = NavBarBackground,
            textColor = White
        )
    }
}

@Composable
private fun AppBar(shouldBlur: Boolean, hazeState: HazeState? = null, onBackClick: () -> Unit) {
    val localizedRes = LocalLocalizedRes.current
    val appBarModifier = if (shouldBlur && hazeState != null) {
        Modifier
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.regular(AppBarBackground)
            )
            .zIndex(1f)
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
            modifier = Modifier.align(CenterStart),
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .noRippleClickable { onBackClick() }
                    .padding(8.dp),
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = White
            )
            Text(
                text = localizedRes.string(R.string.profile_title),
                color = White,
                style = appbarMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = localizedRes.string(R.string.faq),
            color = White,
            style = appbarMedium
        )
    }

}

private fun handleSideEffect(sideEffect: FAQSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}