/**
 * Copyright (C) 2018 Google Inc. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package digital.euforia.app.billing

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.util.Consumer
import androidx.lifecycle.LiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap
import digital.euforia.app.billing.localdb.AugmentedSkuDetails
import digital.euforia.app.billing.localdb.Entitlement
import digital.euforia.app.billing.localdb.LocalBillingDb
import digital.euforia.app.billing.localdb.PHOTO360_PURCHASE
import digital.euforia.app.billing.localdb.Photo360
import digital.euforia.app.billing.localdb.Premium
import digital.euforia.app.billing.model.SubscriptionInfoModel
import digital.euforia.app.util.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */

class BillingRepository private constructor(private val application: Application) :
    PurchasesUpdatedListener, BillingClientStateListener {

    private lateinit var playStoreBillingClient: BillingClient
    private lateinit var localCacheBillingClient: LocalBillingDb
    var currentPurchases: List<Purchase>? = null
    private val productDetailsById = ConcurrentHashMap<String, ProductDetails>()

    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS

    val subsSkuDetailsListLiveData: LiveData<List<AugmentedSkuDetails>> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDb.getInstance(application)
        }
        localCacheBillingClient.skuDetailsDao().getSubscriptionSkuDetails()
    }

    val inappSkuDetailsListLiveData: LiveData<List<AugmentedSkuDetails>> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDb.getInstance(application)
        }
        localCacheBillingClient.skuDetailsDao().getInappSkuDetails()
    }

    val photo360LiveData: LiveData<Photo360> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDb.getInstance(application)
        }
        localCacheBillingClient.entitlementsDao().getGasTank()
    }

    val premiumLiveData: LiveData<Premium> by lazy {
        if (!::localCacheBillingClient.isInitialized) {
            localCacheBillingClient = LocalBillingDb.getInstance(application)
        }
        localCacheBillingClient.entitlementsDao().getPremiumStatus()
    }

    val billingStatus: SingleLiveEvent<Int> by lazy {
        SingleLiveEvent<Int>()
    }

    fun startDataSourceConnections() {
        Log.d("BillingRepository", "startDataSourceConnections: $this")
        instantiateAndConnectToPlayBillingService()
        localCacheBillingClient = LocalBillingDb.getInstance(application)
    }

    fun endDataSourceConnections() {
        //playStoreBillingClient.endConnection()
    }

    private fun instantiateAndConnectToPlayBillingService() {
        playStoreBillingClient = BillingClient.newBuilder(application.applicationContext)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener(this)
            .build()
        connectToPlayBillingService()
    }

    public fun connectToPlayBillingService(): Boolean {
        if (!playStoreBillingClient.isReady) {
            Log.d("BillingRepository", "startConnection, isReady=false")
            playStoreBillingClient.startConnection(this)
            return true
        }
        Log.d("BillingRepository", "BillingClient already ready, querying products")
        queryProductDetailsAsync(BillingClient.ProductType.INAPP, BillingSku.INAPP_SKUS)
        queryProductDetailsAsync(BillingClient.ProductType.SUBS, BillingSku.SUBS_SKUS)
        queryPurchasesAsync()
        return false
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(
                    "BillingRepository",
                    "onBillingSetupFinished OK, querying products"
                )
                queryProductDetailsAsync(BillingClient.ProductType.INAPP, BillingSku.INAPP_SKUS)
                queryProductDetailsAsync(BillingClient.ProductType.SUBS, BillingSku.SUBS_SKUS)
                queryPurchasesAsync()
            }

            else -> {
                Log.w(
                    "BillingRepository",
                    "onBillingSetupFinished: ${billingResult.responseCode} ${billingResult.debugMessage}"
                )
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d("BillingRepository", "onBillingServiceDisconnected: $this")
        retryBillingServiceConnectionWithExponentialBackoff()
    }

    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        reconnectHandler.postDelayed(
            { connectToPlayBillingService() },
            reconnectMilliseconds
        )
        reconnectMilliseconds = min(
            reconnectMilliseconds * 2,
            RECONNECT_TIMER_MAX_TIME_MILLISECONDS
        )
    }

    fun queryPurchasesAsync(callback: Consumer<Set<Purchase>>? = null) {
        val purchasesParamsInApp = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val purchasesResult = HashSet<Purchase>()

        playStoreBillingClient.queryPurchasesAsync(purchasesParamsInApp) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchasesResult.addAll(purchaseList)

                if (isSubscriptionSupported()) {
                    val purchasesParamsSubs = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()

                    playStoreBillingClient.queryPurchasesAsync(
                        purchasesParamsSubs
                    ) { billingResult2, purchaseList2 ->
                        if (billingResult2.responseCode == BillingClient.BillingResponseCode.OK) {
                            purchasesResult.addAll(purchaseList2)
                            currentPurchases = purchasesResult.toList()
                            processPurchases(purchasesResult)
                            callback?.accept(purchasesResult)
                        }
                    }
                } else {
                    currentPurchases = purchasesResult.toList()
                    processPurchases(purchasesResult)
                    callback?.accept(purchasesResult)
                }
            }
        }
    }

    fun refreshPurchases(callback: Consumer<Set<Purchase>>) {
        queryPurchasesAsync(callback)
    }

    private fun processPurchases(purchasesResult: Set<Purchase>) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            val validPurchases = HashSet<Purchase>(purchasesResult.size)
            purchasesResult.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (purchase.isSuspended) {
                        // Suspended subs stay attributed to the user but must not grant access.
                    } else if (isSignatureValid(purchase)) {
                        validPurchases.add(purchase)
                    }
                }
            }
            val (consumables, nonConsumables) = validPurchases.partition {
                BillingSku.CONSUMABLE_SKUS.contains(it.products[0])
            }
            // Clear previous cached entitlements before inserting the new state (keeps legacy behavior)
            localCacheBillingClient.entitlementsDao().deleteAll()
            localCacheBillingClient.purchaseDao().insert(*validPurchases.toTypedArray())
            handleConsumablePurchasesAsync(consumables)
            acknowledgeNonConsumablePurchasesAsync(nonConsumables)
        }

    private fun handleConsumablePurchasesAsync(consumables: List<Purchase>) {
        consumables.forEach {
            val params = ConsumeParams.newBuilder().setPurchaseToken(it.purchaseToken).build()

            // Need remove
            playStoreBillingClient.consumeAsync(params) { billingResult, purchaseToken ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        purchaseToken.apply { disburseConsumableEntitlements(it) }
                    }

                    else -> {
                    }
                }
            }
        }
    }

    private fun acknowledgeNonConsumablePurchasesAsync(nonConsumables: List<Purchase>) {
        nonConsumables.forEach { purchase ->
            val params =
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                    .build()
            if (!purchase.isAcknowledged) {
                try {
                    val skuDetails = localCacheBillingClient.skuDetailsDao()
                        .getById(purchase.products.firstOrNull() ?: "")
                    skuDetails?.let {
                        val subscriptionInfoModel = Gson().fromJson(
                            it.originalJson,
                            SubscriptionInfoModel::class.java
                        )
//                        AppsFlyerLib.getInstance().validateAndLogInAppPurchase(
//                            application.applicationContext,
//                            Security.BASE_64_ENCODED_PUBLIC_KEY,
//                            purchase.signature,
//                            purchase.originalJson,
//                            subscriptionInfoModel.price,
//                            subscriptionInfoModel.priceCurrencyCode,
//                            emptyMap()
//                        )
                    }
                } catch (ignored: Exception) {
                }
                playStoreBillingClient.acknowledgePurchase(params) { billingResult ->
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            disburseNonConsumableEntitlement(purchase)
                        }
                    }
                }
            } else {
                disburseNonConsumableEntitlement(purchase)
            }
        }
    }

    private fun disburseNonConsumableEntitlement(purchase: Purchase) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            when (purchase.products[0]) {
                BillingSku.PREMIUM_MONTHLY, BillingSku.PREMIUM_YEARLY,
                BillingSku.PREMIUM_SPECIAL_MONTHLY, BillingSku.PREMIUM_SPECIAL_YEARLY,
                BillingSku.PREMIUM_MONTHLY_TRIAL, BillingSku.PREMIUM_YEARLY_TRIAL,
                BillingSku.PREMIUM_SPECIAL_MONTHLY_TRIAL, BillingSku.PREMIUM_SPECIAL_YEARLY_TRIAL,
                -> {
                    val premiumStatus = Premium(true)
                    insert(premiumStatus)
                    localCacheBillingClient.skuDetailsDao()
                        .insertOrUpdate(purchase.products[0], premiumStatus.mayPurchase())
                    BillingSku.premium_status_SKUS.forEach { otherSku ->
                        if (otherSku != purchase.products[0]) {
                            localCacheBillingClient.skuDetailsDao()
                                .insertOrUpdate(otherSku, !premiumStatus.mayPurchase())
                        }
                    }
                }
            }
            localCacheBillingClient.purchaseDao().delete(purchase)
        }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(
            Security.BASE_64_ENCODED_PUBLIC_KEY,
            purchase.originalJson,
            purchase.signature
        )
    }

    private fun isSubscriptionSupported(): Boolean {
        val billingResult =
            playStoreBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        var succeeded = false
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ->
                retryBillingServiceConnectionWithExponentialBackoff()

            BillingClient.BillingResponseCode.OK -> succeeded = true
        }
        return succeeded
    }

    private fun queryProductDetailsAsync(productType: String, productIds: List<String>) {
        if (productIds.isEmpty()) return
        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        playStoreBillingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            val fetched = queryProductDetailsResult.productDetailsList
            val unfetched = queryProductDetailsResult.unfetchedProductList
            Log.d(
                "BillingRepository",
                "queryProductDetails($productType) code=${billingResult.responseCode} " +
                    "msg=${billingResult.debugMessage} fetched=${fetched.size} unfetched=${unfetched.size}"
            )
            unfetched.forEach { product ->
                Log.w(
                    "BillingRepository",
                    "unfetched product=${product.productId} status=${product.statusCode}"
                )
            }
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    fetched.forEach { productDetails ->
                        productDetailsById[productDetails.productId] = productDetails
                        CoroutineScope(Job() + Dispatchers.IO).launch {
                            try {
                                localCacheBillingClient.skuDetailsDao().insertOrUpdate(productDetails)
                            } catch (t: Throwable) {
                                Log.e("BillingRepository", "insertOrUpdate failed for ${productDetails.productId}", t)
                            }
                        }
                    }
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) {
        val productDetails = productDetailsById[augmentedSkuDetails.sku]
        if (productDetails == null) {
            Log.e(
                "BillingRepository",
                "launchBillingFlow missing ProductDetails for ${augmentedSkuDetails.sku}"
            )
            return
        }
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply {
                val offerToken = ProductDetailsMapper.selectedOfferToken(productDetails)
                if (!offerToken.isNullOrEmpty()) {
                    setOfferToken(offerToken)
                }
            }
            .build()
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        playStoreBillingClient.launchBillingFlow(activity, billingFlowParams)
    }

    // After launchBillingFlow !! или при старте приложение (если есть необработанные)
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        billingStatus.setValue(billingResult.responseCode)
        currentPurchases = purchases?.toList()
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.apply { processPurchases(this.toSet()) }
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                queryPurchasesAsync()
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                retryBillingServiceConnectionWithExponentialBackoff()
            }
        }
    }

    private fun disburseConsumableEntitlements(purchase: Purchase) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            if (purchase.products[0] == BillingSku.PHOTO_360) {
                buyPhoto360(Photo360(PHOTO360_PURCHASE))
                localCacheBillingClient.purchaseDao().delete(purchase)
            }
        }

    @WorkerThread
    suspend fun buyPhoto360(photo360: Photo360) = withContext(Dispatchers.IO) {
        var update: Photo360 = photo360
        photo360LiveData.value?.apply {
            synchronized(this) {
                if (this != photo360) {//new purchase
                    update = Photo360(getPhoto360Id() + photo360.getPhoto360Id())
                }
                localCacheBillingClient.entitlementsDao().update(update)
            }
        }
        if (photo360LiveData.value == null) {
            localCacheBillingClient.entitlementsDao().insert(update)
        }
        localCacheBillingClient.skuDetailsDao()
            .insertOrUpdate(BillingSku.PHOTO_360, update.mayPurchase())
    }

    @WorkerThread
    private suspend fun insert(entitlement: Entitlement) = withContext(Dispatchers.IO) {
        localCacheBillingClient.entitlementsDao().insert(entitlement)
    }

    companion object {
        @Volatile
        private var INSTANCE: BillingRepository? = null

        fun getInstance(application: Application): BillingRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: BillingRepository(application)
                        .also { INSTANCE = it }
            }

        private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
        private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L // 15 minutes

        private val reconnectHandler = Handler(Looper.getMainLooper())
    }

    public object BillingSku {
        const val PREMIUM_MONTHLY = "digital.euforia.app.premium_monthly"
        const val PREMIUM_YEARLY = "digital.euforia.app.premium_yearly"
        const val PREMIUM_SPECIAL_MONTHLY = "digital.euforia.app.special_monthly"
        const val PREMIUM_SPECIAL_YEARLY = "digital.euforia.app.special_yearly"
        const val PREMIUM_MONTHLY_TRIAL = "digital.euforia.app.premium_monthly_ex"
        const val PREMIUM_YEARLY_TRIAL = "digital.euforia.app.premium_yearly_ex"
        const val PREMIUM_SPECIAL_MONTHLY_TRIAL = "digital.euforia.app.special_monthly_ex"
        const val PREMIUM_SPECIAL_YEARLY_TRIAL = "digital.euforia.app.special_yearly_ex"

        const val IOS_PREMIUM_MONTHLY = "digital.euforia.premium_month"
        const val IOS_PREMIUM_YEARLY = "digital.euforia.premium_year"
        const val IOS_PREMIUM_SPECIAL_MONTHLY = "digital.euforia.premium_month_special"
        const val IOS_PREMIUM_SPECIAL_YEARLY = "digital.euforia.premium_year_special"
        const val IOS_PREMIUM_MONTHLY_TRIAL = "digital.euforia.premium_month_trial"
        const val IOS_PREMIUM_YEARLY_TRIAL = "digital.euforia.premium_year_trial"
        const val IOS_PREMIUM_SPECIAL_MONTHLY_TRIAL = "digital.euforia.premium_year_special_trial"
        const val IOS_PREMIUM_SPECIAL_YEARLY_TRIAL = "digital.euforia.premium_year_special_trial"

        const val PHOTO_360 = "digital.euforia.app.not_used"

        val INAPP_SKUS = listOf(PHOTO_360)
        val SUBS_SKUS =
            listOf(
                PREMIUM_MONTHLY,
                PREMIUM_YEARLY,
                PREMIUM_SPECIAL_MONTHLY,
                PREMIUM_SPECIAL_YEARLY,
                PREMIUM_MONTHLY_TRIAL,
                PREMIUM_YEARLY_TRIAL,
                PREMIUM_SPECIAL_MONTHLY_TRIAL,
                PREMIUM_SPECIAL_YEARLY_TRIAL
            )
        val CONSUMABLE_SKUS = listOf(PHOTO_360)
        val premium_status_SKUS = SUBS_SKUS

        @JvmStatic
        fun contains(o: Any): Boolean {
            return SUBS_SKUS.contains(o)
        }
    }
}



