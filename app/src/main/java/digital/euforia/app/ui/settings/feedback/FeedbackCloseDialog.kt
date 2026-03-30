package digital.euforia.app.ui.settings.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.dialogButton
import digital.euforia.app.ui.theme.dialogMessage
import digital.euforia.app.ui.theme.dialogTitle
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun FeedbackCloseDialog(
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
            .background(color = BottomSheetBackground, shape = RoundedCornerShape(24.dp))
        ,
        containerColor = Color.Transparent,
        onDismissRequest = onCancel,
        title = {
            Text(
                text = localizedRes.string(R.string.feedback_form_exit_confirm_title),
                style = dialogTitle
            )
        },
        text = {
            Text(
                text = localizedRes.string(R.string.feedback_form_exit_confirm_message),
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
                    text = localizedRes.string(R.string.close),
                    style = dialogButton
                )
            }
        }
    )
}

@Composable
fun FeedbackThanksDialog(
    hazeState: HazeState,
    onConfirm: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current

    AlertDialog(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .hazeEffect(
                hazeState,
                style = HazeMaterials.regular(AppBarBackground)
            )
            .background(color = BottomSheetBackground, shape = RoundedCornerShape(24.dp))
        ,
        containerColor = Color.Transparent,
        onDismissRequest = onConfirm,
        title = {
            Text(
                text = localizedRes.string(R.string.feedback_form_finish_title),
                style = dialogTitle
            )
        },
        text = {
            Text(
                text = localizedRes.string(R.string.feedback_form_finish_message),
                style = dialogMessage
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = localizedRes.string(R.string.ok),
                    style = dialogButton
                )
            }
        }
    )
}


@Composable
fun FeedbackRequestDialog(
    hazeState: HazeState,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current

    AlertDialog(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .hazeEffect(
                hazeState,
                style = HazeMaterials.regular(AppBarBackground)
            )
            .background(color = BottomSheetBackground, shape = RoundedCornerShape(24.dp))
        ,
        containerColor = Color.Transparent,
        onDismissRequest = onCancel,
        title = {
            Text(
                text = localizedRes.string(R.string.feedback_alert_title),
                style = dialogTitle
            )
        },
        text = {
            Text(
                text = localizedRes.string(R.string.feedback_alert_message),
                style = dialogMessage
            )
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = localizedRes.string(R.string.feedback_alert_later_button),
                    style = dialogButton
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = localizedRes.string(R.string.feedback_alert_write_button),
                    style = dialogButton
                )
            }
        }
    )
}
