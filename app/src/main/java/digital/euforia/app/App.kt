package digital.euforia.app

import android.app.Application
import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import dagger.hilt.android.HiltAndroidApp
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.di.ApplicationCoroutineScopeDefault
import digital.euforia.app.domain.usecase.InitUseCase
import digital.euforia.app.domain.usecase.accompaniment.SyncAccompanimentsUseCase
import digital.euforia.app.domain.usecase.app_settings.SyncAppSettingsUseCase
import digital.euforia.app.domain.usecase.faq.SyncFAQUseCase
import digital.euforia.app.domain.usecase.resources.SyncResourcesUseCase
import digital.euforia.app.domain.usecase.translation.SyncTranslationsUseCase
import digital.euforia.app.domain.usecase.feedback.SyncFeedbackFormUseCase
import digital.euforia.app.ui.subscription.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@UnstableApi
@HiltAndroidApp
class App : Application() {

    @Inject
    @ApplicationCoroutineScopeDefault
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var profilePreferences: ProfilePreferences

    @Inject
    lateinit var initUseCase: InitUseCase

    @Inject
    lateinit var syncAppSettingsUseCase: SyncAppSettingsUseCase

    @Inject
    lateinit var syncTranslationsUseCase: SyncTranslationsUseCase

    @Inject
    lateinit var syncResourcesUseCase: SyncResourcesUseCase

    @Inject
    lateinit var syncAccompanimentsUseCase: SyncAccompanimentsUseCase

    @Inject
    lateinit var syncFAQUseCase: SyncFAQUseCase

    @Inject
    lateinit var syncFeedbackFormUseCase: SyncFeedbackFormUseCase

    companion object {
        var exoDatabaseProvider: StandaloneDatabaseProvider? = null
        var exoCache: SimpleCache? = null

        @UnstableApi
        fun getExoDatabaseProvider(context: Context): StandaloneDatabaseProvider {
            if (exoDatabaseProvider == null) {
                exoDatabaseProvider = StandaloneDatabaseProvider(context)
            }
            return exoDatabaseProvider!!
        }

        @UnstableApi
        fun getExoCache(context: Context): SimpleCache {
            if (exoCache == null) {
                val cacheFolder = File(context.getCacheDir(), "media")
                val cacheEvictor =
                    LeastRecentlyUsedCacheEvictor(Configuration.DEFAULT_VIDEO_CACHE_SIZE_IN_BYTES)
                exoCache =
                    SimpleCache(cacheFolder, cacheEvictor, getExoDatabaseProvider(context))
            }
            return exoCache!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        initLogger()
//         Ensure tokens are initialized before any network sync in release
//        coroutineScope.launch {
//            initUseCase()
//            // Now perform network-dependent syncs sequentially to avoid race with headers
//            syncAppSettingsUseCase()
//            syncTranslationsUseCase()
//            val isDemo = profilePreferences.getIsDemo()
//            syncAccompanimentsUseCase(isDemo)
//            syncResourcesUseCase()
//        }

        coroutineScope.launch {
            Firebase.remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).await()
            syncTranslations()
        }

        initTokens()
//        syncAppSettings()
//        syncAccompaniments()
//        syncResources()
//        syncFAQ()
        syncFeedbackForm()
//        Firebase.remoteConfig.setDefaultsAsync(R.raw.remote_config_defaults)
//        Firebase.remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private fun initLogger() {
        Timber.plant(Timber.DebugTree())
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    private fun initTokens() {
        coroutineScope.launch { initUseCase() }
    }

    private fun syncAppSettings() {
        coroutineScope.launch { syncAppSettingsUseCase() }
    }

    private fun syncTranslations() {
        coroutineScope.launch { syncTranslationsUseCase() }
    }

    private fun syncResources() {
        coroutineScope.launch { syncResourcesUseCase() }
    }

    private fun syncAccompaniments() {
        coroutineScope.launch {
//            val isDemo = profilePreferences.getIsDemo()
//            syncAccompanimentsUseCase(isDemo)
            syncAccompanimentsUseCase(false)
        }
    }

    private fun syncFAQ() {
        coroutineScope.launch { syncFAQUseCase() }
    }

    private fun syncFeedbackForm() {
        coroutineScope.launch { syncFeedbackFormUseCase() }
    }

    private fun shouldSync() {
        coroutineScope.launch {
            appPreferences.setShouldSyncPackages(true)
        }
    }
}