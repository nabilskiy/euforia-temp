package digital.euforia.app.domain.model.config

data class FeedbackFormConfig(
    val isEnabled: Boolean = false,
    val launchCount: Int = 0,
    val timeInterval: Int = 0
)
