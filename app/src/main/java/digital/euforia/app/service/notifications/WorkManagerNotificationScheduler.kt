package digital.euforia.app.service.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import digital.euforia.app.domain.model.notifications.NotificationType
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class WorkManagerNotificationScheduler(
    private val context: Context
) : NotificationScheduler {

    override fun scheduleDaily(
        type: NotificationType,
        hour: Int,
        minute: Int
    ) {
        val workManager = WorkManager.getInstance(context)
        val uniqueName = "notification_${type.name.lowercase()}"

        // Cancel all previously scheduled works for this notification type
        workManager.cancelAllWorkByTag(uniqueName)

        val now = ZonedDateTime.now()
        var next = now
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)

        if (next.isBefore(now)) {
            next = next.plusDays(1)
        }

        val delay = Duration.between(now, next).toMillis()

        val request = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(DailyNotificationWorker.KEY_TYPE to type.name)
            )
            .addTag(uniqueName)
            .build()

        workManager.enqueue(request)
    }

    override fun scheduleNextDays(
        type: NotificationType,
        hour: Int,
        minute: Int,
        days: Int
    ) {
        val workManager = WorkManager.getInstance(context)
        val tag = "notification_${type.name.lowercase()}"

        // Clear all existing scheduled works for this type first
        workManager.cancelAllWorkByTag(tag)

        val now = ZonedDateTime.now()
        var next = now
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)

        if (next.isBefore(now)) {
            next = next.plusDays(1)
        }

        for (i in 0 until days) {
            val scheduledTime = next.plusDays(i.toLong())
            val delay = java.time.Duration.between(now, scheduledTime).toMillis()

            val request = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(DailyNotificationWorker.KEY_TYPE to type.name)
                )
                .addTag(tag)
                .build()

            // Unique name per day to avoid collisions if enqueued multiple times
            val uniqueName = "${tag}_${scheduledTime.toLocalDate()}"
            workManager.enqueueUniqueWork(
                uniqueName,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override fun cancel(type: NotificationType) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("notification_${type.name.lowercase()}")
    }
}