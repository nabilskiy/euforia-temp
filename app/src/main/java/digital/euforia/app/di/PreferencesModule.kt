package digital.euforia.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.di.AppModule.Companion.PREFS_NAME
import digital.euforia.app.di.NetworkModule.Companion.NAMED_MOSHI_NETWORK
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PreferencesModule {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = PREFS_NAME,
        corruptionHandler = ReplaceFileCorruptionHandler { mutablePreferencesOf() }
    )

    @Singleton
    @Provides
    fun provideAppPreferences(
        @ApplicationContext applicationContext: Context
    ): AppPreferences {
        return AppPreferences(
            store = applicationContext.dataStore
        )
    }
    @Singleton
    @Provides
    fun provideProfilePreferences(
        @ApplicationContext applicationContext: Context
    ): ProfilePreferences {
        return ProfilePreferences(
            store = applicationContext.dataStore
        )
    }

    @Singleton
    @Provides
    @Named(NAMED_MOSHI_NETWORK)
    fun provideMoshiNetwork(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
}