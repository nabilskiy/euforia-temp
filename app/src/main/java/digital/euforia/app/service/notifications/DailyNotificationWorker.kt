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
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.onboarding.Gender
import digital.euforia.app.ui.MainActivity
import org.json.JSONObject
import kotlin.random.Random
import java.util.concurrent.TimeUnit
import android.media.AudioAttributes

class DailyNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val typeName = inputData.getString(KEY_TYPE) ?: return Result.failure()
        val type = NotificationType.valueOf(typeName)
        val message = loadRandomMessageFor(type)

        val appPreferences = EntryPoints.get(
            applicationContext,
            DailyNotificationEntryPoint::class.java
        ).appPreferences()

        val configFetcher = EntryPoints.get(
            applicationContext,
            DailyNotificationEntryPoint::class.java
        ).configFetcher()

        val profilePreferences = EntryPoints.get(
            applicationContext,
            DailyNotificationEntryPoint::class.java
        ).profilePreferences()

        val isTimeSensitive = appPreferences.getIsTimeSensitive()
        val notificationSoundType = configFetcher.getNotificationSoundType()
        val gender = profilePreferences.getGender()

        showNotification(type, message, isTimeSensitive, notificationSoundType, gender)
        return Result.success()
    }

    private fun showNotification(
        type: NotificationType,
        message: String,
        isTimeSensitive: Boolean,
        notificationSoundType: String,
        gender: Gender
    ) {
        if (message.isBlank()) return

        val context = applicationContext

        val soundResId = when (notificationSoundType) {
            "default" -> R.raw.snd_new_accompaniment_2
            "short" -> R.raw.snd_notification
            "voice" -> {
                when (type) {
                    NotificationType.Morning -> if (gender == Gender.MALE) R.raw.snd_new_accom_morning_male else R.raw.snd_new_accom_morning_female
                    NotificationType.Day -> if (gender == Gender.MALE) R.raw.snd_new_accom_daytime_male else R.raw.snd_new_accom_daytime_female
                    NotificationType.Evening -> if (gender == Gender.MALE) R.raw.snd_new_accom_evening_male else R.raw.snd_new_accom_evening_female
                }
            }
            else -> R.raw.snd_new_accompaniment_2
        }
        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + soundResId)

        // Create channel if needed (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channelId = "${CHANNEL_ID}_${soundResId}"
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, CHANNEL_NAME, importance).apply {
                    description = CHANNEL_DESCRIPTION
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                }
                manager.createNotificationChannel(channel)
            }
            
            val deepLinkUri = when (type) {
                NotificationType.Morning -> Uri.parse("euforia://session/morning")
                NotificationType.Day -> Uri.parse("euforia://session/daytime")
                NotificationType.Evening -> Uri.parse("euforia://session/evening")
            }

            val launchIntent = Intent(Intent.ACTION_VIEW, deepLinkUri, context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, pendingFlags)

            val title = context.getString(R.string.app_name)

            val priority = if (isTimeSensitive) {
                NotificationCompat.PRIORITY_HIGH
            } else {
                NotificationCompat.PRIORITY_DEFAULT
            }

            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(priority)
                .setSound(soundUri)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isTimeSensitive) {
                notificationBuilder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            }

            val notification = notificationBuilder.build()

            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(notificationIdFor(type), notification)
                }
            } catch (e: SecurityException) {
                // Handle cases where notification permission is missing on Android 13+
            }
        } else {
            val deepLinkUri = when (type) {
                NotificationType.Morning -> Uri.parse("euforia://session/morning")
                NotificationType.Day -> Uri.parse("euforia://session/daytime")
                NotificationType.Evening -> Uri.parse("euforia://session/evening")
            }

            val launchIntent = Intent(Intent.ACTION_VIEW, deepLinkUri, context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, pendingFlags)

            val title = context.getString(R.string.app_name)

            val priority = if (isTimeSensitive) {
                NotificationCompat.PRIORITY_HIGH
            } else {
                NotificationCompat.PRIORITY_DEFAULT
            }

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(priority)
                .setSound(soundUri)

            val notification = notificationBuilder.build()

            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(notificationIdFor(type), notification)
                }
            } catch (e: SecurityException) {
                // Handle cases where notification permission is missing on Android 13+
            }
        }
    }

    private fun notificationIdFor(type: NotificationType): Int = when (type) {
        NotificationType.Morning -> 1001
        NotificationType.Day -> 1002
        NotificationType.Evening -> 1003
    }

    private fun loadRandomMessageFor(type: NotificationType): String {
        return try {
            val inputStream =
                applicationContext.resources.openRawResource(R.raw.vibes_notifications_2)
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

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DailyNotificationEntryPoint {
        fun appPreferences(): AppPreferences
        fun profilePreferences(): ProfilePreferences
        fun configFetcher(): EuforiaRemoteConfigFetcher
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
