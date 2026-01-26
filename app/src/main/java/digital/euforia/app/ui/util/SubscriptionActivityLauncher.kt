package digital.euforia.app.ui.util

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import digital.euforia.app.ui.subscription.UserActivity
import digital.euforia.app.ui.subscription.UserActivity.PURCHASE_SUCCESS
import digital.euforia.app.ui.util.widget.CongratsPopup
import timber.log.Timber

@Composable
fun SubscriptionActivityLauncher(
    screenId: Int? = null,
    onSuccess: () -> Unit = {},
    content: @Composable (launch: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val isCongratsVisible = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Timber.tag("SUBSCRIPTION_LAUNCHER").d("Activity result: $result")
        if (result.resultCode == PURCHASE_SUCCESS) {
            isCongratsVisible.value = true
            onSuccess()
        }
    }

    val launch: () -> Unit = {
        val intent = Intent(context, UserActivity::class.java).apply {
            if (screenId != null) {
                putExtra(UserActivity.EXTRA_SCREEN_ID, screenId)
            }
        }
        launcher.launch(intent)
    }

    content(launch)

    if (isCongratsVisible.value) {
        CongratsPopup {
            isCongratsVisible.value = false
        }
    }
}
