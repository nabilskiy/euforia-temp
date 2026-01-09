package digital.euforia.app.domain.model.subscription

import androidx.annotation.Keep

@Keep
enum class SubscriptionLevel {
    DEMO,
    PREMIUM,
}

fun Int.toSubscriptionLevel(): SubscriptionLevel {
    return SubscriptionLevel.entries.getOrNull(this) ?: SubscriptionLevel.DEMO
}