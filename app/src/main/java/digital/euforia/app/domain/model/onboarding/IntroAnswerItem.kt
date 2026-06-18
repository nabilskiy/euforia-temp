package digital.euforia.app.domain.model.onboarding

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IntroAnswerItem(
    val identifier: String,
    val code: String,
    val text: String,
    val icon: String? = null,
    val imageUrl: String? = null,
    val subtitle: String? = null,
    val entityId: Int? = null,
)
