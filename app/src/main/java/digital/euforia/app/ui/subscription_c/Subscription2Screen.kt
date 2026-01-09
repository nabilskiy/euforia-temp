package digital.euforia.app.ui.subscription_c

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import coil.compose.AsyncImage
import com.google.gson.Gson
import digital.euforia.app.billing.BillingViewModel
import digital.euforia.app.billing.localdb.AugmentedSkuDetails
import digital.euforia.app.billing.model.SubscriptionInfoModel
import digital.euforia.app.ui.subscription_c.config.*
import digital.euforia.app.ui.subscription_c.util.SubscriptionTextUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Subscription2Screen(
    viewModel: BillingViewModel,
    config: Subscription2Config = Subscription2Config(),
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

    // Знаходимо SKU деталі
    val selectedMonthSku = remember(config.plans, skuDetails) {
        config.plans.find { it.productIdentifier?.contains("month") == true }?.let { plan ->
            skuDetails.find { it.sku == plan.productIdentifier }
        } ?: skuDetails.find { it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_MONTHLY }
    }

    val selectedYearSku = remember(config.primaryProduct, skuDetails) {
        skuDetails.find { it.sku == config.primaryProduct }
            ?: skuDetails.find { it.sku == digital.euforia.app.billing.BillingRepository.BillingSku.PREMIUM_YEARLY }
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

    val planMonth = remember(config.plans) {
        config.plans.find { it.productIdentifier?.contains("month") == true }
    }

    val planYear = remember(config.plans) {
        config.plans.find { it.productIdentifier?.contains("year") == true }
    }

    LaunchedEffect(premiumStatus) {
        premiumStatus?.let { status ->
            if (status.entitled) {
                selectedYearSku?.let {
                    onPurchaseSuccess(it)
                }
            }
        }
    }

    LaunchedEffect(billingStatus) {
        billingStatus?.let { status ->
            if (status == com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
                isLoading = false
            }
        }
    }

    // Анімація для стрілки вниз
    val infiniteTransition = rememberInfiniteTransition(label = "arrow")
    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowAlpha"
    )

    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowOffset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top section with video background
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp)
                ) {
                    // Background gradient (відео можна додати пізніше)
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = SubscriptionTextUtils.formatText(
                                config.title,
                                "Get unlimited access",
                                yearPlanInfo,
                                monthPlanInfo
                            ),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                selectedYearSku?.let { sku ->
                                    activity?.let {
                                        viewModel.makePurchase(it, sku)
                                        isLoading = true
                                    }
                                }
                            },
                            enabled = !isLoading && selectedYearSku != null,
                            modifier = Modifier.fillMaxWidth(),
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
                                        config.primaryButtonTitle,
                                        "Start Premium",
                                        yearPlanInfo,
                                        monthPlanInfo
                                    ),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (config.primaryButtonSubtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = SubscriptionTextUtils.formatText(
                                    config.primaryButtonSubtitle,
                                    "",
                                    yearPlanInfo,
                                    monthPlanInfo
                                ),
                                fontSize = 12.sp,
                                color = Color(0xFFCCCCCC),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Animated arrow down
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Scroll down",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(arrowAlpha)
                                .offset(y = arrowOffset.dp)
                        )
                    }

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

            // Benefits section
            if (config.benefits.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = SubscriptionTextUtils.formatText(
                                config.benefitsTitle,
                                "Benefits",
                                yearPlanInfo,
                                monthPlanInfo
                            ),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        config.benefits.forEach { benefit ->
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
                                    text = benefit,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Reviews section
            if (config.reviews.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Reviews",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "What our users say",
                            fontSize = 14.sp,
                            color = Color(0xFFCCCCCC)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(config.reviews) { review ->
                                ReviewCard(review = review)
                            }
                        }
                    }
                }
            }

            // Plans section
            if (config.plans.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = SubscriptionTextUtils.formatText(
                                config.plansTitle,
                                "Choose your plan",
                                yearPlanInfo,
                                monthPlanInfo
                            ),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (planYear != null) {
                            PlanCard(
                                plan = planYear,
                                planInfo = yearPlanInfo,
                                monthPlanInfo = monthPlanInfo,
                                onClick = {
                                    selectedYearSku?.let { sku ->
                                        activity?.let {
                                            viewModel.makePurchase(it, sku)
                                            isLoading = true
                                        }
                                    }
                                },
                                enabled = !isLoading
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (planMonth != null) {
                            PlanCard(
                                plan = planMonth,
                                planInfo = monthPlanInfo,
                                monthPlanInfo = null,
                                onClick = {
                                    selectedMonthSku?.let { sku ->
                                        activity?.let {
                                            viewModel.makePurchase(it, sku)
                                            isLoading = true
                                        }
                                    }
                                },
                                enabled = !isLoading
                            )
                        }
                    }
                }
            }

            // Extra benefits section
            if (config.extraBenefits.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = SubscriptionTextUtils.formatText(
                                config.extraBenefitsTitle,
                                "Extra Benefits",
                                yearPlanInfo,
                                monthPlanInfo
                            ),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        config.extraBenefits.forEach { benefit ->
                            ExtraBenefitCard(benefit = benefit)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Bottom section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = SubscriptionTextUtils.formatText(
                            config.footerText,
                            "Get unlimited access",
                            yearPlanInfo,
                            monthPlanInfo
                        ),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectedYearSku?.let { sku ->
                                activity?.let {
                                    viewModel.makePurchase(it, sku)
                                    isLoading = true
                                }
                            }
                        },
                        enabled = !isLoading && selectedYearSku != null,
                        modifier = Modifier.fillMaxWidth(),
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
                                    config.primaryBottomButtonTitle,
                                    "Start Premium",
                                    yearPlanInfo,
                                    monthPlanInfo
                                ),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (config.primaryButtonSubtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = SubscriptionTextUtils.formatText(
                                config.primaryButtonSubtitle,
                                "",
                                yearPlanInfo,
                                monthPlanInfo
                            ),
                            fontSize = 12.sp,
                            color = Color(0xFFCCCCCC),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: ReviewSubscriptionModel) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a2a)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.name ?: "User",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row {
                    repeat(review.rating) {
                        Text("⭐", fontSize = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.text ?: "",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC)
            )
        }
    }
}

@Composable
fun PlanCard(
    plan: PlanConfig,
    planInfo: SubscriptionInfoModel?,
    monthPlanInfo: SubscriptionInfoModel?,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a2a)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (!plan.badge.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = SubscriptionTextUtils.formatText(
                            plan.badge,
                            "",
                            planInfo,
                            monthPlanInfo
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFF0f0b01)
                    )
                }
            }

            Text(
                text = SubscriptionTextUtils.formatText(
                    plan.title,
                    "Plan",
                    planInfo,
                    monthPlanInfo
                ),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (!plan.details.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SubscriptionTextUtils.formatText(
                        plan.details,
                        "",
                        planInfo,
                        monthPlanInfo
                    ),
                    fontSize = 14.sp,
                    color = Color(0xFFCCCCCC)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = SubscriptionTextUtils.formatText(
                        plan.buttonTitle,
                        "Subscribe",
                        planInfo,
                        monthPlanInfo
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!plan.buttonSubtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SubscriptionTextUtils.formatText(
                        plan.buttonSubtitle,
                        "",
                        planInfo,
                        monthPlanInfo
                    ),
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ExtraBenefitCard(benefit: BenefitConfig) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a2a)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!benefit.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = benefit.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                val title = benefit.title
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                val text = benefit.text
                if (!text.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = text,
                        fontSize = 14.sp,
                        color = Color(0xFFCCCCCC)
                    )
                }
            }
        }
    }
}

