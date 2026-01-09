package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkSubscription(
    @Json(name = "id") val id: Long?,
    @Json(name = "valid") val valid: Boolean?,
    @Json(name = "sandbox") val sandbox: Boolean?,
    @Json(name = "order_id") val orderId: String?,
    @Json(name = "os") val os: String?,
    @Json(name = "provider") val provider: String?,
    @Json(name = "firebase_id") val firebaseId: String?,
    @Json(name = "state") val state: String?,
    @Json(name = "renew_state") val renewState: String?,
    @Json(name = "product_id") val productId: String?,
    @Json(name = "expires_date") val expiresDate: String?,
    @Json(name = "saved") val saved: String?,
    @Json(name = "token") val token: String?,
)