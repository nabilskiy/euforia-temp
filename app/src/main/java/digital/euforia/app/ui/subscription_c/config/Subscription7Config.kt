package digital.euforia.app.ui.subscription_c.config

import java.io.Serializable

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

data class Subscription7Config(
    var buttonMonth: String = "Start Premium",
    var buttonYear: String = "Start Free Trial",
    var yearLabel: String = "Save up to 50%",
    var yearLine2: String = "Best choice",
    var nameMonth: String = "Monthly",
    var nameYear: String = "Annual",
    var offerMonth: String = "",
    var offerYear: String = "Try 7 days for free",
    var subsDisplay: String = Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_YEAR,
    var subsMonth: String = Subscription1Config.SUBSCRIPTION_MONTH_SPECIAL,
    var subsYear: String = Subscription1Config.SUBSCRIPTION_YEAR_TRIAL,
    var title: String = "Get unlimited access to all pictures and NYMF content",
    var background: String = "https://dubnitskiy.com/storage/manual/mp4/1/123bdd87b916e1e778b6f294edcbd90d.mp4",
    var items: List<Subscription7Item> = emptyList()
) : Serializable {
    data class Subscription7Item(
        var title: String? = null,
        var text: String? = null
    ) : Serializable
}

