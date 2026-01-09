@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.settings.email

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.theme.subtitleSmall
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.CorporateTextField
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.titleItem
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SharedTransitionScope.EmailScreen(
    navController: NavHostController,
    viewModel: EmailViewModel,
    navBarVisibilityState: MutableState<Boolean>,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    NavBarlessScreen(navBarVisibilityState) {
        EmailContent(
            navController = navController,
            email = state.email,
            onEmailChanged = viewModel::onEmailChanged,
            onBackClick = { navController.popBackStack() },
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun SharedTransitionScope.EmailContent(
    navController: NavHostController,
    email: String? = null,
    onEmailChanged: (String) -> Unit = {},
    onBackClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
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
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }

    Box() {
        BlurredAppBar(
            backTitleRes = R.string.profile_title,
            titleRes = R.string.change_email_title,
            shouldBlur = shouldBlur,
            hazeState = hazeState,
            onBackClick = onBackClick,
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
                titleRes = R.string.change_email_title
            )
            emailItem(
                email = email,
                onEmailChanged = onEmailChanged
            )
        }
    }
}

fun LazyListScope.emailItem(email: String?, onEmailChanged: (String) -> Unit) = item {
    val localizedRes = LocalLocalizedRes.current

    val focusRequester = remember { FocusRequester() }

    CorporateTextField(
        modifier = Modifier.fillMaxWidth(),
        value = email.orEmpty(),
        placeholder = localizedRes.string(R.string.email_placeholder),
        onValueChanged = { onEmailChanged(it.text) },
        maxLength = 150,
    )

    Text(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        text = localizedRes.string(R.string.intro_email_info),
        style = subtitleSmall,
        color = Color.White.copy(alpha = 0.4f)
    )
}

@Composable
private fun AppBar(shouldBlur: Boolean, hazeState: HazeState? = null, onBackClick: () -> Unit) {
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
        val localizedRes = LocalLocalizedRes.current
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
            text = localizedRes.string(R.string.change_email_title),
            color = White,
            style = appbarMedium
        )
    }

}

private fun handleSideEffect(sideEffect: EmailSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}
