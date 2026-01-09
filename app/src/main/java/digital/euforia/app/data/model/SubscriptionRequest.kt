package digital.euforia.app.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionRequest(
    val orderId: String,
    val packageName: String,
    val productId: String,
    val purchaseTime: Long,
    val purchaseState: Int,
    val purchaseToken: String,
    val quantity: Int,
    val autoRenewing: Boolean,
    val acknowledged: Boolean
)