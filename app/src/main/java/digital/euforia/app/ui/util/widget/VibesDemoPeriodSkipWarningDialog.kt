package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.dialogButton
import digital.euforia.app.ui.theme.dialogMessage
import digital.euforia.app.ui.theme.dialogTitle
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun WeekSkipWarningDialog(
    hazeState: HazeState,
    onCancel: () -> Unit,
    onSkip: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current

    AlertDialog(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .hazeEffect(
                hazeState,
                style = HazeMaterials.regular(AppBarBackground)
            )
//            .background(color = NavBarBackground, shape = RoundedCornerShape(24.dp))
        ,
        containerColor = Color.Transparent,
        onDismissRequest = onCancel,
        title = {
            Text(
                text = localizedRes.string(R.string.vibes_demo_period_skip_warning_title),
                style = dialogTitle
            )
        },
        text = {
            Text(
                text = localizedRes.string(R.string.vibes_demo_period_skip_warning_message),
                style = dialogMessage
            )
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = localizedRes.string(R.string.cancel),
                    style = dialogButton
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSkip) {
                Text(
                    text = localizedRes.string(R.string.skip),
                    style = dialogButton
                )
            }
        }
    )
}
