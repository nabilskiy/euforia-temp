package digital.euforia.app.domain.model.settings

enum class StartupRequestsPriority(
    val priority: Int
) {
    RATE_APP(3),
    FEEDBACK(2),
    EMAIL(1),
}