package digital.euforia.app.ui.subscription_c

import android.app.Activity
import androidx.annotation.Keep
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.google.gson.Gson
import digital.euforia.app.billing.BillingViewModel
import digital.euforia.app.billing.localdb.AugmentedSkuDetails
import digital.euforia.app.billing.model.SubscriptionInfoModel
import digital.euforia.app.ui.subscription_c.config.Subscription1Config
import digital.euforia.app.ui.subscription_c.util.SubscriptionTextUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Subscription1Screen(
    viewModel: BillingViewModel,
    config: Subscription1Config = Subscription1Config(),
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
    var selectedPlan by remember { mutableStateOf<PlanType?>(null) }

    // Знаходимо SKU деталі
    val skuDetailsMonthly = remember(skuDetails) {
        skuDetails.find { it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_MONTHLY }
    }
    val skuDetailsYearly = remember(skuDetails) {
        skuDetails.find { it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_YEARLY }
    }

    val selectedMonthSku = remember(config.subsMonth, skuDetails) {
        when (config.subsMonth) {
            Subscription1Config.SUBSCRIPTION_MONTH_TRIAL -> skuDetails.find { 
                it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_MONTHLY_TRIAL 
            } ?: skuDetailsMonthly
            Subscription1Config.SUBSCRIPTION_MONTH_SPECIAL -> skuDetails.find { 
                it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_SPECIAL_MONTHLY 
            } ?: skuDetailsMonthly
            Subscription1Config.SUBSCRIPTION_MONTH_SPECIAL_TRIAL -> skuDetails.find { 
                it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_SPECIAL_MONTHLY_TRIAL 
            } ?: skuDetailsMonthly
            else -> skuDetailsMonthly
        }
    }

    val selectedYearSku = remember(config.subsYear, skuDetails) {
        when (config.subsYear) {
            Subscription1Config.SUBSCRIPTION_YEAR_TRIAL -> skuDetails.find { 
                it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_YEARLY_TRIAL 
            } ?: skuDetailsYearly
            Subscription1Config.SUBSCRIPTION_YEAR_SPECIAL -> skuDetails.find { 
                it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_SPECIAL_YEARLY 
            } ?: skuDetailsYearly
            Subscription1Config.SUBSCRIPTION_YEAR_SPECIAL_TRIAL -> skuDetails.find { 
                it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_SPECIAL_YEARLY_TRIAL 
            } ?: skuDetailsYearly
            else -> skuDetailsYearly
        }
    }

    val monthPlanInfo = remember(selectedMonthSku) {
        selectedMonthSku?.originalJson?.let {
            try {
                Gson().fromJson(it, SubscriptionInfoModel::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    val yearPlanInfo = remember(selectedYearSku) {
        selectedYearSku?.originalJson?.let {
            try {
                Gson().fromJson(it, SubscriptionInfoModel::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Визначаємо який план показувати
    val showMonthly = when (config.subsDisplay) {
        Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_MONTH -> true
        Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_YEAR -> false
        Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_OFF -> false
        else -> selectedMonthSku != null
    }

    val showYearly = when (config.subsDisplay) {
        Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_MONTH -> false
        Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_YEAR -> true
        Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_OFF -> false
        else -> selectedYearSku != null
    }

    // Встановлюємо початковий вибір
    LaunchedEffect(showMonthly, showYearly) {
        if (selectedPlan == null) {
            selectedPlan = when {
                showYearly -> PlanType.Yearly
                showMonthly -> PlanType.Monthly
                else -> null
            }
        }
    }

    // Обробка успішних покупок
    LaunchedEffect(premiumStatus) {
        premiumStatus?.let { status ->
            if (status.entitled) {
                (selectedPlan?.let { if (it == PlanType.Monthly) selectedMonthSku else selectedYearSku })?.let {
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

    val title = when (from) {
        "intro" -> config.titleFromIntro
        "second" -> config.titleFromSecond
        else -> config.titleFromPrimary
    }

    val currentOffer = when (selectedPlan) {
        PlanType.Monthly -> config.offerMonth
        PlanType.Yearly -> config.offerYear
        null -> ""
    }

    val currentButtonText = when (selectedPlan) {
        PlanType.Monthly -> config.buttonMonth
        PlanType.Yearly -> config.buttonYear
        null -> "Start Premium"
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

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            // Title
            Text(
                text = title,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xE6FFFFFF),
                textAlign = TextAlign.Center,
                maxLines = config.titleMaxLines,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 11.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Monthly plan
            if (showMonthly) {
                SubscriptionPlanCard(
                    title = SubscriptionTextUtils.formatText(
                        config.nameMonth,
                        "Monthly",
                        monthPlanInfo,
                        null
                    ),
                    price = monthPlanInfo?.price ?: "",
                    isSelected = selectedPlan == PlanType.Monthly,
                    onClick = { selectedPlan = PlanType.Monthly },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }

            // Yearly plan
            if (showYearly) {
                SubscriptionPlanCard(
                    title = SubscriptionTextUtils.formatText(
                        config.nameYear,
                        "Annual",
                        yearPlanInfo,
                        monthPlanInfo
                    ),
                    price = yearPlanInfo?.price ?: "",
                    isSelected = selectedPlan == PlanType.Yearly,
                    onClick = { selectedPlan = PlanType.Yearly },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    badge = SubscriptionTextUtils.formatText(
                        config.yearLabel,
                        "Save up to 50%",
                        yearPlanInfo,
                        monthPlanInfo
                    ),
                    subtitle = SubscriptionTextUtils.formatText(
                        config.yearLine2,
                        "Best choice",
                        yearPlanInfo,
                        monthPlanInfo
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Offer text
            if (currentOffer.isNotBlank()) {
                Text(
                    text = SubscriptionTextUtils.formatText(
                        currentOffer,
                        null,
                        if (selectedPlan == PlanType.Monthly) monthPlanInfo else yearPlanInfo,
                        if (selectedPlan == PlanType.Yearly) monthPlanInfo else null
                    ),
                    fontSize = 12.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Purchase button
            Button(
                onClick = {
                    val selectedSku = when (selectedPlan) {
                        PlanType.Monthly -> selectedMonthSku
                        PlanType.Yearly -> selectedYearSku
                        null -> null
                    }
                    selectedSku?.let { sku ->
                        activity?.let {
                            viewModel.makePurchase(it, sku)
                            isLoading = true
                        }
                    }
                },
                enabled = !isLoading && selectedPlan != null,
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
                            currentButtonText,
                            "Start Premium",
                            if (selectedPlan == PlanType.Monthly) monthPlanInfo else yearPlanInfo,
                            null
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Terms text
            Text(
                text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                fontSize = 12.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
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

@Composable
fun SubscriptionPlanCard(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    subtitle: String? = null
) {
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.6f, label = "alpha")

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(if (badge != null) 80.dp else 60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5).copy(alpha = alpha)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (badge != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subtitle ?: "",
                        fontSize = 14.sp,
                        color = Color(0xFF0f0b01).copy(alpha = alpha),
                        fontWeight = FontWeight.Normal
                    )
                    if (badge.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 12.sp,
                                color = Color(0xFF0f0b01),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color(0xFF0f0b01).copy(alpha = alpha),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = price,
                    fontSize = 15.sp,
                    color = Color(0xFF0f0b01).copy(alpha = alpha)
                )
            }
        }
    }
}

@Keep
enum class PlanType {
    Monthly, Yearly
}

