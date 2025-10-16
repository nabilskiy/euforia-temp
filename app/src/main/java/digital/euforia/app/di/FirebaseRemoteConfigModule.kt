package digital.euforia.app.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FirebaseRemoteConfigModule {

    @Singleton
    @Provides
    fun provideFirebaseRemoteConfigInitializer(
        @Named(NetworkModule.NAMED_MOSHI_NETWORK) moshi: Moshi,
        appPreferences: AppPreferences,
        @ApplicationCoroutineScope coroutineScope: CoroutineScope

    ): EuforiaRemoteConfigFetcher {
        return EuforiaRemoteConfigFetcher(moshi = moshi, onConfigsUpdatedListener = {
            coroutineScope.launch {
//                appPreferences.setMinSupportedVersion(it.getAppConfig()?.minVersion)
            }
        })
    }
}