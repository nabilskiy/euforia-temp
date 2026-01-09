package digital.euforia.app.domain.model

import digital.euforia.app.ui.programs.material.PublicationType

data class PublicationInfo(
    val id: Int,
    val isPremium: Boolean,
    val publicationType: PublicationType,
    val title: String?,
    val subtitle: String?,
    val imageUrl: String?,
    val color1: String? = null,
    val color2: String? = null,
    val color3: String? = null,
    val publishedAt: Long?,
    val durationMinutes: Int?,
)