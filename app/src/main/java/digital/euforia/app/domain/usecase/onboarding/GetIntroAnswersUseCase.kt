package digital.euforia.app.domain.usecase.onboarding

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.RawRes
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.domain.model.onboarding.IntroAnswerItem
import digital.euforia.app.domain.util.readRawResource
import java.util.Locale
import javax.inject.Inject

class GetIntroAnswersUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(@RawRes resId: Int, languageTag: String): List<IntroAnswerItem> {
        val locale = Locale.forLanguageTag(languageTag)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        val json = readRawResource(localizedContext, resId)
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, IntroAnswerItem::class.java)
        val adapter = moshi.adapter<List<IntroAnswerItem>>(type)
        return adapter.fromJson(json) ?: emptyList()
    }
}
