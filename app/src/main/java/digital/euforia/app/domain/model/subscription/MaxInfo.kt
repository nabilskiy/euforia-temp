package digital.euforia.app.domain.model.subscription

import androidx.annotation.DrawableRes

data class MaxInfo(
    val title: String,
    val details: String,
    val items: List<MaxInfoItem>,
    val bottomCardTitle: String,
    val bottomCardText: String,
)

data class MaxInfoItem(
    val title: String,
    val text: String,
    @DrawableRes val imageUrl: Int,
    val videoUrl: String,
)