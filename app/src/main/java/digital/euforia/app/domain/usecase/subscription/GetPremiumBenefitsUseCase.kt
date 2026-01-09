package digital.euforia.app.domain.usecase.subscription

import android.content.Context
import android.content.res.Configuration
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.R
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.subscription.PremiumBenefit
import digital.euforia.app.domain.util.readRawResource
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

class GetPremiumBenefitsUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(): List<PremiumBenefit> {
        val tag = appPreferences.getLanguage() ?: "en"
        val locale = Locale.forLanguageTag(tag)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        val json = readRawResource(localizedContext, R.raw.premium_10_benefits)
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, PremiumBenefit::class.java)
        val adapter = moshi.adapter<List<PremiumBenefit>>(type)

        return adapter.fromJson(json) ?: emptyList()
    }

    fun getNow(): List<PremiumBenefit> = runBlocking {
        invoke()
    }
}

