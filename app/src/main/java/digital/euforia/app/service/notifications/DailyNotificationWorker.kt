package digital.euforia.app.service.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import digital.euforia.app.domain.model.notifications.NotificationType
import digital.euforia.app.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import digital.euforia.app.ui.MainActivity
import org.json.JSONObject
import kotlin.random.Random
import java.util.concurrent.TimeUnit

class DailyNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val typeName = inputData.getString(KEY_TYPE) ?: return Result.failure()
        val type = NotificationType.valueOf(typeName)

        // TODO: дістань актуальні налаштування з DataStore / репозиторія
        // і покажи нотифікацію тільки якщо цей тип увімкнено

        val message = loadRandomMessageFor(type)

        showNotification(type, message)
        return Result.success()
    }

    private fun showNotification(type: NotificationType, message: String) {
        if (message.isBlank()) return

        val context = applicationContext

        // Create channel if needed (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, pendingFlags)

        val title = context.getString(R.string.app_name)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(notificationIdFor(type), notification)
        }
    }

    private fun notificationIdFor(type: NotificationType): Int = when (type) {
        NotificationType.Morning -> 1001
        NotificationType.Day -> 1002
        NotificationType.Evening -> 1003
    }

    private fun loadRandomMessageFor(type: NotificationType): String {
        return try {
            val inputStream = applicationContext.resources.openRawResource(R.raw.vibes_notifications_2)
            val text = inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val key = when (type) {
                NotificationType.Morning -> "morning"
                NotificationType.Day -> "daytime"
                NotificationType.Evening -> "evening"
            }
            val arr = json.getJSONArray(key)
            if (arr.length() == 0) "" else arr.getString(Random.nextInt(arr.length()))
        } catch (_: Exception) {
            ""
        }
    }

    companion object {
        const val KEY_TYPE = "type"
        private const val CHANNEL_ID = "daily_vibes_channel"
        private const val CHANNEL_NAME = "Daily vibes"
        private const val CHANNEL_DESCRIPTION = "Daily motivational notifications"

        fun scheduleNextDay(type: NotificationType, context: Context) {
            val workManager = WorkManager.getInstance(context)

            val data = workDataOf(KEY_TYPE to type.name)

            val request = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
                .setInitialDelay(24, TimeUnit.HOURS)
                .setInputData(data)
                .build()

            workManager.enqueueUniqueWork(
                "notification_${type.name.lowercase()}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
