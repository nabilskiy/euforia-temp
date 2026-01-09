package digital.euforia.app.ui.subscription_c

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.rotate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.google.gson.Gson
import digital.euforia.app.billing.BillingViewModel
import digital.euforia.app.billing.localdb.AugmentedSkuDetails
import digital.euforia.app.billing.model.SubscriptionInfoModel
import digital.euforia.app.ui.subscription_c.config.Subscription3Config
import digital.euforia.app.ui.subscription_c.util.SubscriptionTextUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Subscription3Screen(
    viewModel: BillingViewModel,
    config: Subscription3Config = Subscription3Config(),
    from: String = "primary",
    tag: String? = null,
    onPurchaseSuccess: (AugmentedSkuDetails) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val premiumStatus by viewModel.premiumLiveData.observeAsState(initial = null)
    val skuDetails by viewModel.subsSkuDetailsListLiveData.observeAsState(initial = emptyList())
    val billingStatus by viewModel.billingStatus.observeAsState(initial = null)

    var isLoading by remember { mutableStateOf(false) }

    val selectedSku = remember(config.subscription, skuDetails) {
        skuDetails.find { it.sku == config.subscription }
            ?: skuDetails.find { it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_YEARLY }
    }

    val planInfo = remember(selectedSku) {
        selectedSku?.originalJson?.let {
            try {
                Gson().fromJson(it, SubscriptionInfoModel::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Обробка успішних покупок
    LaunchedEffect(premiumStatus) {
        premiumStatus?.let { status ->
            if (status.entitled) {
                selectedSku?.let {
                    onPurchaseSuccess(it)
                }
            }
        }
    }

    // Обробка статусу billing
    LaunchedEffect(billingStatus) {
        billingStatus?.let { status ->
            if (status == com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a1a1a),
                            Color(0xFF000000)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            // Title
            Text(
                text = SubscriptionTextUtils.formatText(
                    config.title,
                    "Get unlimited access",
                    planInfo,
                    null
                ),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Items list
            if (config.items.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(config.items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(180f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offer text
            if (config.offer.isNotBlank()) {
                Text(
                    text = SubscriptionTextUtils.formatText(
                        config.offer,
                        null,
                        planInfo,
                        null
                    ),
                    fontSize = 12.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Purchase button
            Button(
                onClick = {
                    selectedSku?.let { sku ->
                        activity?.let {
                            viewModel.makePurchase(it, sku)
                            isLoading = true
                        }
                    }
                },
                enabled = !isLoading && selectedSku != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = SubscriptionTextUtils.formatText(
                            config.button,
                            "Subscribe",
                            planInfo,
                            null
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom text
            if (config.bottomText.isNotBlank()) {
                Text(
                    text = SubscriptionTextUtils.formatText(
                        config.bottomText,
                        null,
                        planInfo,
                        null
                    ).replace("\\n", "\n"),
                    fontSize = 14.sp,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        // Top bar
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

