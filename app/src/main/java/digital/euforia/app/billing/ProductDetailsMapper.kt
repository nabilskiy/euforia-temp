package digital.euforia.app.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.google.gson.Gson
import digital.euforia.app.billing.localdb.AugmentedSkuDetails
import digital.euforia.app.billing.model.SubscriptionInfoModel

/**
 * Maps Play Billing [ProductDetails] into the Room/UI [AugmentedSkuDetails] shape
 * that subscription screens still parse via [SubscriptionInfoModel].
 */
internal object ProductDetailsMapper {

    private val gson = Gson()

    private data class PricingInfo(
        val formattedPrice: String?,
        val priceAmountMicros: Long,
        val priceCurrencyCode: String?,
        val billingPeriod: String?
    )

    fun toAugmentedSkuDetails(
        productDetails: ProductDetails,
        canPurchase: Boolean
    ): AugmentedSkuDetails {
        val pricing = selectedPricing(productDetails)
        val info = SubscriptionInfoModel(
            description = productDetails.description,
            title = productDetails.title,
            subscriptionPeriod = subscriptionPeriodLabel(productDetails),
            priceCurrencyCode = pricing?.priceCurrencyCode,
            priceAmountMicros = pricing?.priceAmountMicros ?: 0L,
            price = pricing?.formattedPrice,
            type = productDetails.productType,
            productId = productDetails.productId
        )
        return AugmentedSkuDetails(
            canPurchase = canPurchase,
            sku = productDetails.productId,
            type = productDetails.productType,
            price = pricing?.formattedPrice,
            title = productDetails.title,
            description = productDetails.description,
            originalJson = gson.toJson(info)
        )
    }

    fun selectedOfferToken(productDetails: ProductDetails): String? {
        return productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
    }

    private fun selectedOffer(
        productDetails: ProductDetails
    ): ProductDetails.SubscriptionOfferDetails? {
        return productDetails.subscriptionOfferDetails?.firstOrNull()
    }

    private fun selectedPricing(productDetails: ProductDetails): PricingInfo? {
        val phases = selectedOffer(productDetails)?.pricingPhases?.pricingPhaseList.orEmpty()
        if (phases.isNotEmpty()) {
            val phase = phases.lastOrNull { it.priceAmountMicros > 0L } ?: phases.last()
            return PricingInfo(
                formattedPrice = phase.formattedPrice,
                priceAmountMicros = phase.priceAmountMicros,
                priceCurrencyCode = phase.priceCurrencyCode,
                billingPeriod = phase.billingPeriod
            )
        }
        val oneTime = productDetails.oneTimePurchaseOfferDetailsList?.firstOrNull()
            ?: productDetails.oneTimePurchaseOfferDetails
        return oneTime?.let { offer ->
            PricingInfo(
                formattedPrice = offer.formattedPrice,
                priceAmountMicros = offer.priceAmountMicros,
                priceCurrencyCode = offer.priceCurrencyCode,
                billingPeriod = null
            )
        }
    }

    private fun subscriptionPeriodLabel(productDetails: ProductDetails): String? {
        if (productDetails.productType != BillingClient.ProductType.SUBS) return null
        val phases = selectedOffer(productDetails)?.pricingPhases?.pricingPhaseList.orEmpty()
        if (phases.isEmpty()) return null
        val labels = mutableListOf<String>()
        val recurring = phases.lastOrNull { it.priceAmountMicros > 0L } ?: phases.last()
        recurring.billingPeriod.lowercase().let { labels.add(it) }
        val trial = phases.firstOrNull { it.priceAmountMicros == 0L }
        when {
            trial?.billingPeriod.equals("P3D", ignoreCase = true) -> labels.add("3dt")
            trial?.billingPeriod.equals("P7D", ignoreCase = true) -> labels.add("7dt")
        }
        return labels.joinToString(" ").ifBlank { null }
    }
}
