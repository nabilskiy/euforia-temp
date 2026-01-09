package digital.euforia.app.domain.model.subscription

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PremiumBenefit(
    val title: String,
    val imageUrl: String,
)