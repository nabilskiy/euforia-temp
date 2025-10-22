package digital.euforia.app.domain.usecase.plan

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.config.BannerConfig
import digital.euforia.app.domain.usecase.translation.GetTranslationUseCase
import javax.inject.Inject

class GetBannerConfigUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    private val configFetcher: EuforiaRemoteConfigFetcher,
    private val getTranslationUseCase: GetTranslationUseCase
) {

    suspend operator fun invoke(): BannerConfig? {
        val bannerConfig = configFetcher.getTodayBannerConfig()
        val translation = getTranslationUseCase(bannerConfig?.subtitle.orEmpty())
        val logoTag = "\n![](img://euforia_max)"
        return bannerConfig?.copy(subtitle = translation.replace(logoTag, ""))
    }
}