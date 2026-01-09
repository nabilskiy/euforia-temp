package digital.euforia.app.service.notifications

import digital.euforia.app.domain.model.notifications.NotificationType

interface NotificationScheduler {

    fun scheduleDaily(
        type: NotificationType,
        hour: Int,
        minute: Int
    )

    /**
     * Schedule one notification per day at the specified time for the next [days] days.
     * Existing scheduled work for the same [type] should be cleared before scheduling.
     */
    fun scheduleNextDays(
        type: NotificationType,
        hour: Int,
        minute: Int,
        days: Int
    )

    fun cancel(type: NotificationType)
}
