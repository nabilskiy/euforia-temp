package digital.euforia.app.domain.model.onboarding

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Interest(
    val identifier: String,
    val code: String,
    val imageName: String,
    val sceneCategoryId: Int,
    val name: String
)