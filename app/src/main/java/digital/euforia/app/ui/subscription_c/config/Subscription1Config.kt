package digital.euforia.app.ui.subscription_c.config

import java.io.Serializable

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

data class Subscription1Config(
    var subsDisplay: String = SUBSCRIPTION_DISPLAY_TYPE_ALL,
    var subsMonth: String = SUBSCRIPTION_MONTH_SPECIAL,
    var subsYear: String = SUBSCRIPTION_YEAR_SPECIAL,
    var buttonMonth: String = "Start Premium",
    var buttonYear: String = "Start Free Trial",
    var nameMonth: String = "Monthly",
    var nameYear: String = "Annual Special",
    var offerMonth: String = "",
    var offerYear: String = "Try 7 days for free",
    var titleFromIntro: String = "Get unlimited access to all pictures and NYMF content",
    var titleFromPrimary: String = "Get unlimited access to all NYMF content",
    var titleFromSecond: String = "Get unlimited access to all stories and NYMF content",
    var videoFromIntro: String = "https://dubnitskiy.com/storage/manual/mp4/1/123bdd87b916e1e778b6f294edcbd90d.mp4",
    var videoFromPrimary: String = "video1",
    var videoFromSecond: String = "https://dubnitskiy.com/storage/manual/mp4/2/2003e69a36ce5183b7a2cedca4251087.mp4",
    var yearLabel: String = "Save up to 50%",
    var yearLine2: String = "Best choice",
    var titleCaps: Boolean = false,
    var titleColor: String = "#ffffff",
    var titleMaxLines: Int = 2
) : Serializable {
    companion object {
        const val SUBSCRIPTION_DISPLAY_TYPE_ALL = "all"
        const val SUBSCRIPTION_DISPLAY_TYPE_MONTH = "month"
        const val SUBSCRIPTION_DISPLAY_TYPE_YEAR = "year"
        const val SUBSCRIPTION_DISPLAY_TYPE_OFF = "off"

        const val SUBSCRIPTION_MONTH_DEFAULT = "default"
        const val SUBSCRIPTION_MONTH_TRIAL = "trial"
        const val SUBSCRIPTION_MONTH_SPECIAL = "special"
        const val SUBSCRIPTION_MONTH_SPECIAL_TRIAL = "special_trial"

        const val SUBSCRIPTION_YEAR_DEFAULT = "default"
        const val SUBSCRIPTION_YEAR_TRIAL = "trial"
        const val SUBSCRIPTION_YEAR_SPECIAL = "special"
        const val SUBSCRIPTION_YEAR_SPECIAL_TRIAL = "special_trial"
    }
}

