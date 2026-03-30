package digital.euforia.app.domain.model

import androidx.compose.runtime.Immutable
import digital.euforia.app.ui.programs.publication.PublicationType

@Immutable
data class PublicationInfo(
    val id: Int,
    val packageId: Int? = null,
    val categoryId: Int? = null,
    val categoryVideoCoverUrl: String? = null,
    val isPremium: Boolean,
    val publicationType: PublicationType,
    val alias: String,
    val title: String,
    val subtitle: String?,
    val imageUrl: String?,
    val videoUrl: String? = null,
    val color1: String? = null,
    val color2: String? = null,
    val color3: String? = null,
    val publishedAt: Long?,
    val durationMinutes: Int?,
    val isFavourite: Boolean = false
)