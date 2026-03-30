package digital.euforia.app.domain.model.config

data class RateConfig(
    val morning: RatePeriodConfig = RatePeriodConfig(),
    val daytime: RatePeriodConfig = RatePeriodConfig(isEnabled = false),
    val evening: RatePeriodConfig = RatePeriodConfig()
)

data class RatePeriodConfig(
    val isEnabled: Boolean = true,
    val eventCount: Int = 0,
    val timeInterval: Int = 0,
    val timeIntervalAfterCancel: Int = 0
)
