package digital.euforia.app.ui.subscription_c.config

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

data class Subscription2Config(
    @SerializedName("title")
    var title: String = "",
    @SerializedName("video")
    var video: String = "",
    @SerializedName("music")
    var music: String = "",
    @SerializedName("primaryButtonTitle")
    var primaryButtonTitle: String = "",
    @SerializedName("primaryBottomButtonTitle")
    var primaryBottomButtonTitle: String = "",
    @SerializedName("primaryButtonSubtitle")
    var primaryButtonSubtitle: String = "",
    @SerializedName("primaryProduct")
    var primaryProduct: String = "",
    @SerializedName("footerText")
    var footerText: String = "",
    @SerializedName("plansTitle")
    var plansTitle: String = "",
    @SerializedName("benefitsTitle")
    var benefitsTitle: String = "",
    @SerializedName("extraBenefitsTitle")
    var extraBenefitsTitle: String = "",
    @SerializedName("benefits")
    var benefits: ArrayList<String> = ArrayList(),
    @SerializedName("plans")
    var plans: ArrayList<PlanConfig> = ArrayList(),
    @SerializedName("extraBenefits")
    var extraBenefits: ArrayList<BenefitConfig> = ArrayList(),
    var reviews: ArrayList<ReviewSubscriptionModel> = ArrayList()
) : Serializable

data class BenefitConfig(
    @SerializedName("imageUrl")
    var imageUrl: String? = null,
    @SerializedName("text")
    var text: String? = null,
    @SerializedName("title")
    var title: String? = null
) : Serializable

data class PlanConfig(
    @SerializedName("productIdentifier")
    var productIdentifier: String? = null,
    @SerializedName("badge")
    var badge: String? = null,
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("details")
    var details: String? = null,
    @SerializedName("buttonTitle")
    var buttonTitle: String? = null,
    @SerializedName("buttonSubtitle")
    var buttonSubtitle: String? = null
) : Serializable

data class ReviewSubscriptionModel(
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("text")
    var text: String? = null,
    @SerializedName("rating")
    var rating: Int = 5
) : Serializable

