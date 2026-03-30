package digital.euforia.app.domain.model.config

data class CriticalUpdateConfig(
    val needUpdate: Boolean = false,
    val cancelable: Boolean = false,
    val minVersionCode: Int = 1
)
