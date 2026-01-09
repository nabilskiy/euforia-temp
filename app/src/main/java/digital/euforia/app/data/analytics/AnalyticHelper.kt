package digital.euforia.app.data.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics


object AnalyticHelper {
    @JvmOverloads
    fun get(context: Context, userId: String? = null): FirebaseAnalytics {
        val analytics = FirebaseAnalytics.getInstance(context)
        analytics.setAnalyticsCollectionEnabled(true)
        if (userId != null && !userId.isEmpty()) analytics.setUserId(userId)
        return analytics
    }
}

class An