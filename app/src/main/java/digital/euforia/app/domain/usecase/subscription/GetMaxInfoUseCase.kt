package digital.euforia.app.domain.usecase.subscription

import android.content.Context
import android.content.res.Configuration
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.R
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.subscription.MaxInfo
import digital.euforia.app.domain.model.subscription.MaxInfoItem
import digital.euforia.app.domain.util.readRawResource
import java.util.Locale
import javax.inject.Inject

class GetMaxInfoUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(): MaxInfo {
        val tag = appPreferences.getLanguage() ?: "en"
        val locale = Locale.forLanguageTag(tag)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        val json = readRawResource(localizedContext, R.raw.about_premium)
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val adapter = moshi.adapter(AboutPremiumDto::class.java)
        val dto = adapter.fromJson(json) ?: AboutPremiumDto(
            title = "",
            details = "",
            items = emptyList(),
            bottomCardTitle = "",
            bottomCardText = ""
        )

        val items = dto.items.map { item ->
            val imageResName = item.imageUrl.removePrefix("img://")
            val resId = context.resources.getIdentifier(imageResName, "drawable", context.packageName)
            MaxInfoItem(
                title = item.title,
                text = item.text,
                imageUrl = resId,
                videoUrl = item.videoUrl
            )
        }

        return MaxInfo(
            title = dto.title,
            details = dto.details,
            items = items,
            bottomCardTitle = dto.bottomCardTitle,
            bottomCardText = dto.bottomCardText
        )
    }

    @JsonClass(generateAdapter = true)
    data class AboutPremiumDto(
        val title: String,
        val details: String,
        val items: List<ItemDto>,
        val bottomCardTitle: String,
        val bottomCardText: String
    )

    @JsonClass(generateAdapter = true)
    data class ItemDto(
        val title: String,
        val text: String,
        val imageUrl: String,
        val videoUrl: String
    )
}