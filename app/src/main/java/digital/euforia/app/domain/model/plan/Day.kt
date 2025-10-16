package digital.euforia.app.domain.model.plan

data class Day (
    val displayName: String,
    val isDemo: Boolean = false,
    val isLocked: Boolean = false
)