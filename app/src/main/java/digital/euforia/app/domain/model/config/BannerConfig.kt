package digital.euforia.app.domain.model.config

data class BannerConfig(
    val subtitle: String,
    val imgUrl: String,
    val actionUrl: String,
    val style: StyleConfig,
    val predicate: String,
    val isFullWidth: Boolean
)