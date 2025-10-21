package digital.euforia.app.domain.model.plan


data class RankedPackage(
    val rank: Int,
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
)