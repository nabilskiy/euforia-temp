package digital.euforia.app.domain.usecase.onboarding

import android.content.Context
import android.content.res.Configuration
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.R
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.domain.model.onboarding.Language
import digital.euforia.app.domain.util.readRawResource
import java.util.Locale
import javax.inject.Inject

class GetGoalsUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(tag: String): List<Goal> {
        val locale = Locale.forLanguageTag(tag)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        val json = readRawResource(localizedContext, R.raw.goals)
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, Goal::class.java)
        val adapter = moshi.adapter<List<Goal>>(type)

        return adapter.fromJson(json) ?: emptyList()
    }
}


//spanishContext.resources.openRawResource(R.raw.goals).bufferedReader().use{ it.readText()}