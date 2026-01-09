package digital.euforia.app.ui.subscription_c

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Приклад використання SubscriptionActivity в Compose screen
 * 
 * Використання:
 * ```kotlin
 * @Composable
 * fun YourScreen() {
 *     SubscriptionButton(
 *         from = "home_screen",
 *         tag = "premium_button"
 *     )
 * }
 * ```
 */
@Composable
fun SubscriptionButton(
    from: String = "primary",
    tag: String? = null,
    onPurchaseSuccess: ((String) -> Unit)? = null,
    onPurchaseCancelled: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Launcher для отримання результату від SubscriptionActivity
    val purchaseLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val sku = result.data?.getStringExtra(SubscriptionActivity.EXTRA_PURCHASE_SKU)
                sku?.let { onPurchaseSuccess?.invoke(it) }
                // Тут можна оновити стан преміум або показати повідомлення про успіх
            }
            Activity.RESULT_CANCELED -> {
                onPurchaseCancelled?.invoke()
                // Користувач скасував покупку
            }
        }
    }

    Button(
        onClick = {
            val intent = SubscriptionActivity.createIntent(
                context = context,
                from = from,
                tag = tag
            )
            purchaseLauncher.launch(intent)
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Subscribe to Premium")
    }
}

