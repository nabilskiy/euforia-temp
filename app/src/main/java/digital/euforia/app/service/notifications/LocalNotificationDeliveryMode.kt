package digital.euforia.app.service.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import digital.euforia.app.R
import java.time.LocalTime

object LocalNotificationDeliveryMode {

    const val QUIET_CHANNEL_ID = "scheduled_quiet_v1"

    fun isQuietHours(now: LocalTime = LocalTime.now()): Boolean {
        return !now.isBefore(QUIET_START) || now.isBefore(QUIET_END)
    }

    fun ensureQuietChannel(context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return QUIET_CHANNEL_ID

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(QUIET_CHANNEL_ID) != null) return QUIET_CHANNEL_ID

        val channel = NotificationChannel(
            QUIET_CHANNEL_ID,
            context.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Quiet scheduled notifications"
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
        return QUIET_CHANNEL_ID
    }

    fun priority(isQuietHours: Boolean, isTimeSensitive: Boolean): Int {
        return when {
            isQuietHours -> NotificationCompat.PRIORITY_LOW
            isTimeSensitive -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private val QUIET_START: LocalTime = LocalTime.of(23, 0)
    private val QUIET_END: LocalTime = LocalTime.of(8, 0)
}
