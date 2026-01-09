package digital.euforia.app.ui.subscription_c

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import digital.euforia.app.billing.BillingViewModel
import digital.euforia.app.billing.localdb.AugmentedSkuDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: BillingViewModel,
    from: String,
    tag: String?,
    onPurchaseSuccess: (AugmentedSkuDetails) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val premiumStatus by viewModel.premiumLiveData.observeAsState(initial = null)
    val skuDetails by viewModel.subsSkuDetailsListLiveData.observeAsState(initial = emptyList())
    val billingStatus by viewModel.billingStatus.observeAsState(initial = null)

    var isLoading by remember { mutableStateOf(false) }

    // Обробка успішних покупок
    LaunchedEffect(premiumStatus) {
        premiumStatus?.let { status ->
            if (status.entitled) {
                skuDetails.firstOrNull { !it.canPurchase }?.let { sku ->
                    onPurchaseSuccess(sku)
                }
            }
        }
    }

    // Обробка статусу billing
    LaunchedEffect(billingStatus) {
        billingStatus?.let { status ->
            // BillingResponseCode.OK означає що покупка завершена
            if (status == com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                if (skuDetails.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No subscriptions available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(skuDetails) { sku ->
                            SubscriptionItem(
                                sku = sku,
                                onClick = {
                                    activity?.let {
                                        viewModel.makePurchase(it, sku)
                                        isLoading = true
                                    }
                                },
                                enabled = sku.canPurchase && !isLoading
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionItem(
    sku: AugmentedSkuDetails,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = sku.title ?: sku.sku,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sku.description ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sku.price ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (!enabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Already purchased",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

