package digital.euforia.app.ui.subscription_c.util

import digital.euforia.app.billing.model.SubscriptionInfoModel
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object SubscriptionTextUtils {
    
    fun formatText(
        text: String?,
        defaultText: String?,
        info: SubscriptionInfoModel?,
        infoRelativeMonth: SubscriptionInfoModel?
    ): String {
        var result = text ?: defaultText ?: return ""
        if (result.isBlank()) return ""

        try {
            result = result.replace("%price%", info?.price ?: "")
            result = result.replace("%period%", getPeriod(info))
            result = result.replace("%trial_period%", getTrialPeriod(info))
            result = result.replace("%discount%", getDiscount(info, infoRelativeMonth))
            result = result.replace("%price_per_month%", getPricePerMonth(info))
            result = result.replace("%relative_price%", getRelativePrice(info, infoRelativeMonth))
            result = result.replace("&amp;", "&")
        } catch (e: Exception) {
            // Ignore
        }

        return result.trim()
    }

    private fun getPeriod(info: SubscriptionInfoModel?): String {
        if (info == null) return ""
        return when {
            info.subscriptionPeriod?.contains("p1m") == true -> "month"
            info.subscriptionPeriod?.contains("p1y") == true -> "year"
            else -> ""
        }
    }

    private fun getTrialPeriod(info: SubscriptionInfoModel?): String {
        if (info == null) return ""
        return when {
            info.subscriptionPeriod?.contains("3dt") == true -> "3 days"
            info.subscriptionPeriod?.contains("7dt") == true -> "7 days"
            else -> ""
        }
    }

    private fun getDiscount(info: SubscriptionInfoModel?, infoRelativeMonth: SubscriptionInfoModel?): String {
        if (info == null || infoRelativeMonth == null) return ""
        try {
            val saleValue = 100 - ((info.priceAmountMicros.toDouble() / (infoRelativeMonth.priceAmountMicros * 12.0)) * 100.0).toInt()
            return saleValue.toString()
        } catch (e: Exception) {
            return ""
        }
    }

    private fun getPricePerMonth(info: SubscriptionInfoModel?): String {
        if (info == null) return ""
        try {
            var priceYearlyPerMonth = (info.priceAmountMicros / 12.0 / 1000000.0).toFloat()
            val priceYearlyPerMonthUnit = priceYearlyPerMonth.toInt()
            val rem = priceYearlyPerMonth - priceYearlyPerMonthUnit

            priceYearlyPerMonth = when {
                rem >= 0.0f && rem < 0.15f -> (priceYearlyPerMonthUnit - 1) + 0.99000f
                rem >= 0.15f && rem < 0.40f -> priceYearlyPerMonthUnit + 0.19000f
                rem >= 0.40f && rem < 0.65f -> priceYearlyPerMonthUnit + 0.49000f
                rem >= 0.65f && rem < 0.90f -> priceYearlyPerMonthUnit + 0.75000f
                rem >= 0.90f && rem <= 0.99f -> priceYearlyPerMonthUnit + 0.99000f
                else -> priceYearlyPerMonth
            }

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
                roundingMode = RoundingMode.FLOOR
                currency = Currency.getInstance(info.priceCurrencyCode ?: "USD")
            }
            return currencyFormat.format(priceYearlyPerMonth)
        } catch (e: Exception) {
            return ""
        }
    }

    private fun getRelativePrice(info: SubscriptionInfoModel?, infoRelativeMonth: SubscriptionInfoModel?): String {
        if (info == null || infoRelativeMonth == null) return ""
        try {
            val priceYearlyRelative = (infoRelativeMonth.priceAmountMicros * 12.0 / 1000000.0)
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                currency = Currency.getInstance(infoRelativeMonth.priceCurrencyCode ?: "USD")
            }
            return currencyFormat.format(priceYearlyRelative)
        } catch (e: Exception) {
            return ""
        }
    }
}

