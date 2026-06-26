package digital.euforia.app.service.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import digital.euforia.app.R
import digital.euforia.app.ui.MainActivity

class FirstExperienceReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val previewType = inputData.getString(KEY_PREVIEW_TYPE).orEmpty()
        if (previewType.isBlank()) return Result.failure()

        showNotification(
            deepLinkUri = buildDeepLinkUri(previewType),
        )
        return Result.success()
    }

    private fun showNotification(deepLinkUri: Uri) {
        val context = applicationContext
        val quietHours = LocalNotificationDeliveryMode.isQuietHours()
        val channelId = if (quietHours) {
            LocalNotificationDeliveryMode.ensureQuietChannel(context)
        } else {
            ensureChannel(context)
        }

        val launchIntent = Intent(Intent.ACTION_VIEW, deepLinkUri, context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            launchIntent,
            pendingFlags,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.intro_first_experience_reminder_notification_text))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.intro_first_experience_reminder_notification_text)),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(
                LocalNotificationDeliveryMode.priority(
                    isQuietHours = quietHours,
                    isTimeSensitive = true,
                ),
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Android 13+ can deny notification permission after scheduling.
        }
    }

    private fun buildDeepLinkUri(previewType: String): Uri {
        val entityId = inputData.getInt(KEY_ENTITY_ID, -1)
        val accompanimentId = inputData.getInt(KEY_ACCOMPANIMENT_ID, -1)
        val timeOfDay = inputData.getString(KEY_TIME_OF_DAY).orEmpty()
        val sceneTimerSeconds = inputData.getInt(KEY_SCENE_TIMER_SECONDS, DEFAULT_SCENE_TIMER_SECONDS)
        val imageUrl = inputData.getString(KEY_IMAGE_URL).orEmpty()

        return Uri.Builder()
            .scheme("euforia")
            .authority(DEEP_LINK_HOST)
            .appendQueryParameter("type", previewType)
            .appendQueryParameter("entity_id", entityId.takeIf { it > 0 }?.toString().orEmpty())
            .appendQueryParameter("accompaniment_id", accompanimentId.takeIf { it > 0 }?.toString().orEmpty())
            .appendQueryParameter("time_of_day", timeOfDay)
            .appendQueryParameter("scene_timer_seconds", sceneTimerSeconds.toString())
            .appendQueryParameter("image_url", imageUrl)
            .build()
    }

    private fun ensureChannel(context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return CHANNEL_ID

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return CHANNEL_ID

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.intro_first_experience_reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.intro_first_experience_reminder_channel_description)
        }
        manager.createNotificationChannel(channel)
        return CHANNEL_ID
    }

    companion object {
        const val KEY_PREVIEW_TYPE = "preview_type"
        const val KEY_ENTITY_ID = "entity_id"
        const val KEY_ACCOMPANIMENT_ID = "accompaniment_id"
        const val KEY_TIME_OF_DAY = "time_of_day"
        const val KEY_SCENE_TIMER_SECONDS = "scene_timer_seconds"
        const val KEY_IMAGE_URL = "image_url"

        const val DEEP_LINK_HOST = "continue_intro_preview"

        private const val CHANNEL_ID = "first_experience_reminder_channel"
        private const val NOTIFICATION_ID = 1601
        private const val DEFAULT_SCENE_TIMER_SECONDS = 600
    }
}
