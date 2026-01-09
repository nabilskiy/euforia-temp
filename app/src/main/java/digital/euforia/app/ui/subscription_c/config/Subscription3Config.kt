package digital.euforia.app.ui.subscription_c.config

import digital.euforia.app.billing.BillingRepository
import java.io.Serializable

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

data class Subscription3Config(
    var subscription: String = BillingRepository.BillingSku.PREMIUM_YEARLY,
    var subscriptionRelated: String = BillingRepository.BillingSku.PREMIUM_MONTHLY,
    var offer: String = "Try 7 days for free",
    var button: String = "Subscribe for %price%/%period%",
    var bottomText: String = "Invest in your well-being and\n upgrade today",
    var title: String = "Get unlimited access to all pictures and NYMF content",
    var background: String = "https://dubnitskiy.com/storage/manual/mp4/1/123bdd87b916e1e778b6f294edcbd90d.mp4",
    var items: List<String> = emptyList()
) : Serializable

