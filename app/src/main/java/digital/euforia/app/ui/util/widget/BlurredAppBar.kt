package digital.euforia.app.ui.util.widget

import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavHostController
//import androidx.compose.animation.rememberSharedContentState
//import androidx.compose.animation.sharedElement
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun SharedTransitionScope.BlurredAppBar(
    shouldBlur: Boolean,
    titleRes: Int,
    isBackAllowed: Boolean = true,
    backTitleRes: Int? = null,
    hazeState: HazeState? = null,
    // Shared element params: optional
    sharedElementKeyForBackTitle: String? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    navController: NavHostController,
    premiumButtonState: PremiumButtonState = PremiumButtonState.NONE,
    onUpgradeClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
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
        if (isBackAllowed) {
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
                if (
                    sharedElementKeyForBackTitle != null &&
                    animatedVisibilityScope != null
                ) {
                    Text(
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = sharedElementKeyForBackTitle),
                            animatedVisibilityScope,
                            boundsTransform = { _, _ -> tween(durationMillis = 300) }
                        ),
                        text = localizedRes.string(backTitleRes ?: R.string.back),
                        color = White,
                        style = appbarMedium.copy(fontWeight = FontWeight.Medium)
                    )
                } else {
                    Text(
                        text = localizedRes.string(backTitleRes ?: R.string.back),
                        color = White,
                        style = appbarMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
        if (shouldBlur) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = localizedRes.string(titleRes),
                color = White,
                style = appbarMedium
            )
        }

        when (premiumButtonState) {
            PremiumButtonState.UPGRADE -> {
                UpgradeView(
                    Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                    onClick = onUpgradeClick
                )
            }

            PremiumButtonState.MAX -> {
                MaxView(
                    Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                    onClick = { navController.navigate(HomeDestination.AboutPremium) }
                )
            }

            else -> {}
        }
    }
}

@Composable
fun BlurredAppBar(
    shouldBlur: Boolean,
    titleRes: Int,
    isBackAllowed: Boolean = true,
    backTitleRes: Int? = null,
    hazeState: HazeState? = null,
    navController: NavHostController,
    premiumButtonState: PremiumButtonState = PremiumButtonState.NONE,
    onUpgradeClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    actionButton: @Composable () -> Unit? = {}
) {
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
        if (isBackAllowed) {
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
                    text = localizedRes.string(backTitleRes ?: R.string.back),
                    color = White,
                    style = appbarMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
        Box(
            modifier = Modifier.align(Alignment.CenterEnd),

            ) {
            actionButton()
        }
        if (shouldBlur) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = localizedRes.string(titleRes),
                color = White,
                style = appbarMedium
            )
        }

        when (premiumButtonState) {
            PremiumButtonState.UPGRADE -> {
                UpgradeView(
                    Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                    onClick = onUpgradeClick
                )
            }

            PremiumButtonState.MAX -> {
                MaxView(
                    Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                    onClick = { navController.navigate(HomeDestination.AboutPremium) }
                )
            }

            else -> {}
        }
    }
}

fun LazyListScope.titleItem(titleRes: Int,modifier: Modifier = Modifier) = item {
    val localizedRes = LocalLocalizedRes.current
    Text(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        text = localizedRes.string(titleRes),
        style = MaterialTheme.typography.displaySmall,
        color = White
    )
}

@Keep
enum class PremiumButtonState { NONE, UPGRADE, MAX }