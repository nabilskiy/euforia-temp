@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.settings.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
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
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Gender
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.theme.subtitleSmall
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.titleItem
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SharedTransitionScope.VoiceScreen(
    navController: NavHostController,
    viewModel: VoiceViewModel,
    navBarVisibilityState: MutableState<Boolean>,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    NavBarlessScreen(navBarVisibilityState) {
        VoiceContent(
            navController = navController,
            gender = state.gender ?: Gender.FEMALE,
            onGenderSelected = viewModel::onGenderSelected,
            onBackClick = { navController.popBackStack() },
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun SharedTransitionScope.VoiceContent(
    navController: NavHostController,
    gender: Gender,
    onGenderSelected: (Gender) -> Unit,
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
            // Blur when the very first list item (spacer) scrolled off enough
            // or when any next item became the first visible one.
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            backTitleRes = R.string.profile_title,
            titleRes = R.string.change_voice_title,
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
                titleRes = R.string.change_voice_title
            )
            optionsItem(gender, onGenderSelected)
        }
    }
}

private fun LazyListScope.optionsItem(selectedGender: Gender, onGenderSelected: (Gender) -> Unit) = item {
    Column {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(color = NavBarBackground, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Gender.entries.forEachIndexed { index, gender ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(32.dp)
                        .noRippleClickable { onGenderSelected(gender) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = LocalLocalizedRes.current.string(gender.textRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = White
                    )
                    if (gender == selectedGender) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
                if (index != Gender.entries.lastIndex) {
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
        Text(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            text = LocalLocalizedRes.current.string(R.string.change_voice_description),
            style = subtitleSmall,
            color = Color.White.copy(alpha = 0.4f)
        )
    }
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
            text = localizedRes.string(R.string.change_voice_title),
            color = White,
            style = appbarMedium
        )
    }
}

private fun handleSideEffect(sideEffect: VoiceSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}