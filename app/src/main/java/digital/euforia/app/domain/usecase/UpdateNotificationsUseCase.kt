package digital.euforia.app.domain.usecase

import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.notifications.NotificationType
import digital.euforia.app.service.notifications.NotificationScheduler
import javax.inject.Inject

class UpdateNotificationsUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    private val scheduler: NotificationScheduler
) {
    /**
     * Schedules up to 3 notifications per day (morning/day/evening) for the next [days] days.
     * Uses times and toggles from [AppPreferences].
     */
    suspend operator fun invoke(days: Int = 30) {
        // Morning
        if (appPreferences.isMorningNotificationEnabled()) {
            val (h, m) = appPreferences.getMorningNotificationTime()
            scheduler.scheduleNextDays(NotificationType.Morning, h, m, days)
        } else {
            scheduler.cancel(NotificationType.Morning)
        }

        // Daytime
        if (appPreferences.isDayNotificationEnabled()) {
            val (h, m) = appPreferences.getDayNotificationTime()
            scheduler.scheduleNextDays(NotificationType.Day, h, m, days)
        } else {
            scheduler.cancel(NotificationType.Day)
        }

        // Evening
        if (appPreferences.isEveningNotificationEnabled()) {
            val (h, m) = appPreferences.getEveningNotificationTime()
            scheduler.scheduleNextDays(NotificationType.Evening, h, m, days)
        } else {
            scheduler.cancel(NotificationType.Evening)
        }
    }
}

class UpdateNotificationSettingsUseCase(
    private val appPreferences: AppPreferences,
    private val scheduler: NotificationScheduler
) {
    suspend operator fun invoke() {

    }
}
