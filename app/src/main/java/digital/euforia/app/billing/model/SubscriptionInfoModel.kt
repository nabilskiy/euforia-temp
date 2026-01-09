package digital.euforia.app.billing.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */

data class SubscriptionInfoModel(
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("subscriptionPeriod")
    var subscriptionPeriod: String? = null,
    @SerializedName("price_currency_code")
    var priceCurrencyCode: String? = null,
    @SerializedName("price_amount_micros")
    var priceAmountMicros: Long = 0,
    @SerializedName("price")
    var price: String? = null,
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("productId")
    var productId: String? = null,
    @SerializedName("skuDetailsToken")
    var skuDetailsToken: String? = null
) : Serializable



