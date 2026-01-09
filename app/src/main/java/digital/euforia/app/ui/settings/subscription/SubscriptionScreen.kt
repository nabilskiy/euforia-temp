@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.settings.subscription

import androidx.annotation.Keep
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Language
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.openAboutSubscriptions
import digital.euforia.app.ui.util.openPlayStoreSubscriptions
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.titleItem
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SharedTransitionScope.SubscriptionScreen(
    navController: NavHostController,
    viewModel: SubscriptionViewModel,
    navBarVisibilityState: MutableState<Boolean>,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }
    val context = LocalContext.current
    NavBarlessScreen(navBarVisibilityState) {
        SubscriptionContent(
            navController = navController,
            onOptionClick = { option ->
                when (option) {
                    SubscriptionOption.MANAGE -> {
                        val sku = state.currentPurchases.firstOrNull()?.skus?.firstOrNull()
                        openPlayStoreSubscriptions(context, sku)
                    }

                    SubscriptionOption.ABOUT -> {
                        openAboutSubscriptions(context)
                    }
                    SubscriptionOption.LOGS -> {
                        navController.navigate(HomeDestination.DeviceInfo)
                    }
                    else -> {}
                }


            },
            onBackClick = {
                navController.popBackStack()
            },
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun SharedTransitionScope.SubscriptionContent(
    navController: NavHostController,
    onOptionClick: (SubscriptionOption) -> Unit,
    onBackClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val listState = rememberLazyListState()
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
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

    Box() {
        BlurredAppBar(
            titleRes = R.string.subscriptions_managment,
            backTitleRes = R.string.profile_title,
            shouldBlur = shouldBlur, hazeState = hazeState, onBackClick = onBackClick,
            sharedElementKeyForBackTitle = "my_euforia_title",
            animatedVisibilityScope = animatedVisibilityScope,
            navController = navController
        )
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
            titleItem(
                titleRes = R.string.subscriptions_managment
            )
            optionsItem(onOptionClick)
        }
    }
}

fun LazyListScope.optionsItem(
    handleClick: (SubscriptionOption) -> Unit
) = item {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = NavBarBackground, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {

        SubscriptionOption.entries.forEachIndexed { index, option ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(32.dp)
                    .noRippleClickable { handleClick(option) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = localizedRes.string(option.titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = White
                )

                Icon(
                    painter = painterResource(R.drawable.ic_next),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
            if (index != SubscriptionOption.entries.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(0.5.dp)
                        .background(White.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Keep
enum class SubscriptionOption(val titleRes: Int) {
    MANAGE(R.string.manage_subscriptions),
    ABOUT(R.string.about_subscription),
    LOGS(R.string.debug_information)
}

private fun handleSideEffect(sideEffect: SubscriptionSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}