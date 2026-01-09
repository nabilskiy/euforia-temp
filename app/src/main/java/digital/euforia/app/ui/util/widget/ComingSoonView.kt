package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun ComingSoonView(
    titleRes: Int,
    navController: NavHostController,
    isBackAllowed: Boolean = false
) {

    Box(modifier = Modifier.fillMaxSize()) {
        BlurredAppBar(
            shouldBlur = false,
            titleRes = titleRes,
            isBackAllowed = isBackAllowed,
            navController = navController,
            premiumButtonState = PremiumButtonState.NONE

        )
        val localizedRes = LocalLocalizedRes.current
        Text(
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                .padding(top = 16.dp + AppBarHeightMedium, start = 16.dp),
            text = localizedRes.string(titleRes),
            style = MaterialTheme.typography.displaySmall,
            color = White
        )
        Text(
            text = "Coming Soon!",
            modifier = Modifier.align(Alignment.Center),
            color = White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold)
        )
    }
}