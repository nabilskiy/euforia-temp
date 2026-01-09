package digital.euforia.app.ui.settings.personaldata.cleardata

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.Red
import digital.euforia.app.ui.theme.dialogButton
import digital.euforia.app.ui.theme.dialogMessage
import digital.euforia.app.ui.theme.dialogTitle
import digital.euforia.app.ui.util.LocalLocalizedRes


@Composable
fun ClearDataDialog(
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current

    AlertDialog(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))

//            .background(color = NavBarBackground, shape = RoundedCornerShape(24.dp))
        ,
        containerColor = NavBarBackground,
        onDismissRequest = onCancel,
        title = {
            Text(
                text = localizedRes.string(R.string.personal_data_clear_confirm_title),
                style = dialogTitle
            )
        },
        text = {
            Text(
                text = localizedRes.string(R.string.personal_data_clear_confirm_message),
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
            TextButton(onClick = onDelete) {
                Text(
                    text = localizedRes.string(R.string.delete),
                    style = dialogButton,
                    color = Red
                )
            }
        }
    )
}
