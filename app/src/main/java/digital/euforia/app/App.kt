package digital.euforia.app

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import digital.euforia.app.data.network.AuthToken
import digital.euforia.app.data.network.AuthTokenProvider
import digital.euforia.app.data.network.DeviceToken
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.di.ApplicationCoroutineScopeDefault
import digital.euforia.app.domain.usecase.InitUseCase
import digital.euforia.app.domain.usecase.translation.SyncTranslationsUseCase
import digital.euforia.app.domain.usecase.app_settings.SyncAppSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    @ApplicationCoroutineScopeDefault
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var initUseCase: InitUseCase

    @Inject
    lateinit var syncAppSettingsUseCase: SyncAppSettingsUseCase

    @Inject
    lateinit var syncTranslationsUseCase: SyncTranslationsUseCase

    override fun onCreate() {
        super.onCreate()
        initLogger()
        initTokens()
        syncAppSettings()
        syncTranslations()
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
}