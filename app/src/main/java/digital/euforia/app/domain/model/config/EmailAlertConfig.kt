package digital.euforia.app.domain.model.config

data class EmailAlertConfig(
    val isEnabled: Boolean = false,
    val launchCount: Int = 0,
    val timeInterval: Long = 0
)
