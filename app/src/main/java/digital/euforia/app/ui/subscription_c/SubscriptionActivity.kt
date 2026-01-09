package digital.euforia.app.ui.subscription_c

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import digital.euforia.app.billing.BillingViewModel
import digital.euforia.app.ui.theme.EuforiaTheme

class SubscriptionActivity : ComponentActivity() {

    private lateinit var viewModel: BillingViewModel

    companion object {
        const val EXTRA_FROM = "extra_from"
        const val EXTRA_TAG = "extra_tag"
        const val EXTRA_SCREEN_ID = "extra_screen_id"
        const val RESULT_PURCHASE_SUCCESS = RESULT_OK
        const val RESULT_PURCHASE_CANCELLED = RESULT_CANCELED
        const val EXTRA_PURCHASE_SKU = "extra_purchase_sku"

        fun createIntent(
            context: android.content.Context, 
            from: String = "primary", 
            tag: String? = null,
            screenId: Int = 1
        ): Intent {
            return Intent(context, SubscriptionActivity::class.java).apply {
                putExtra(EXTRA_FROM, from)
                putExtra(EXTRA_TAG, tag)
                putExtra(EXTRA_SCREEN_ID, screenId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val from = intent.getStringExtra(EXTRA_FROM) ?: "primary"
        val tag = intent.getStringExtra(EXTRA_TAG)
        val screenId = intent.getIntExtra(EXTRA_SCREEN_ID, 1)

        viewModel = ViewModelProvider(this)[BillingViewModel::class.java]

        setContent {
            EuforiaTheme {
                // Вибираємо екран на основі screenId
                when (screenId) {
                    8 -> Subscription8Screen(
                        viewModel = viewModel,
                        from = from,
                        tag = tag,
                        onPurchaseSuccess = { sku ->
                            setResult(RESULT_PURCHASE_SUCCESS, Intent().apply {
                                putExtra(EXTRA_PURCHASE_SKU, sku.sku)
                            })
                            finish()
                        },
                        onDismiss = {
                            setResult(RESULT_PURCHASE_CANCELLED)
                            finish()
                        }
                    )
                    7 -> Subscription7Screen(
                        viewModel = viewModel,
                        from = from,
                        tag = tag,
                        onPurchaseSuccess = { sku ->
                            setResult(RESULT_PURCHASE_SUCCESS, Intent().apply {
                                putExtra(EXTRA_PURCHASE_SKU, sku.sku)
                            })
                            finish()
                        },
                        onDismiss = {
                            setResult(RESULT_PURCHASE_CANCELLED)
                            finish()
                        }
                    )
                    6 -> Subscription6Screen(
                        viewModel = viewModel,
                        from = from,
                        tag = tag,
                        onPurchaseSuccess = { sku ->
                            setResult(RESULT_PURCHASE_SUCCESS, Intent().apply {
                                putExtra(EXTRA_PURCHASE_SKU, sku.sku)
                            })
                            finish()
                        },
                        onDismiss = {
                            setResult(RESULT_PURCHASE_CANCELLED)
                            finish()
                        }
                    )
                    3 -> Subscription3Screen(
                        viewModel = viewModel,
                        from = from,
                        tag = tag,
                        onPurchaseSuccess = { sku ->
                            setResult(RESULT_PURCHASE_SUCCESS, Intent().apply {
                                putExtra(EXTRA_PURCHASE_SKU, sku.sku)
                            })
                            finish()
                        },
                        onDismiss = {
                            setResult(RESULT_PURCHASE_CANCELLED)
                            finish()
                        }
                    )
                    2 -> Subscription2Screen(
                        viewModel = viewModel,
                        from = from,
                        tag = tag,
                        onPurchaseSuccess = { sku ->
                            setResult(RESULT_PURCHASE_SUCCESS, Intent().apply {
                                putExtra(EXTRA_PURCHASE_SKU, sku.sku)
                            })
                            finish()
                        },
                        onDismiss = {
                            setResult(RESULT_PURCHASE_CANCELLED)
                            finish()
                        }
                    )
                    else -> Subscription1Screen(
                        viewModel = viewModel,
                        from = from,
                        tag = tag,
                        onPurchaseSuccess = { sku ->
                            setResult(RESULT_PURCHASE_SUCCESS, Intent().apply {
                                putExtra(EXTRA_PURCHASE_SKU, sku.sku)
                            })
                            finish()
                        },
                        onDismiss = {
                            setResult(RESULT_PURCHASE_CANCELLED)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

