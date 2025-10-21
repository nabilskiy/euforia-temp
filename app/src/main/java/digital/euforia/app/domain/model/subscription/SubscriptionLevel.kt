package digital.euforia.app.domain.model.subscription

enum class SubscriptionLevel {
    DEMO,
    PREMIUM,
}

fun Int.toSubscriptionLevel(): SubscriptionLevel {
    return SubscriptionLevel.entries.getOrNull(this) ?: SubscriptionLevel.DEMO
}