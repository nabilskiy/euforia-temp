package digital.euforia.app.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.euforia.app.BuildConfig
import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.network.AuthToken
import digital.euforia.app.data.network.AuthTokenProvider
import digital.euforia.app.data.network.DeviceToken
import digital.euforia.app.data.network.DeviceTokenProvider
import digital.euforia.app.data.network.HeadersInterceptor
import digital.euforia.app.data.network.TokenProvider
import digital.euforia.app.data.network.TokensProvider
import digital.euforia.app.data.network.adapter.ResultAdapterFactory
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Singleton
    @Provides
    @DeviceToken
    fun provideDeviceTokenProvider(
        appPreferences: AppPreferences,
        @ApplicationCoroutineScope scope: CoroutineScope
    ): DeviceTokenProvider = DeviceTokenProvider(appPreferences, scope)

    @Singleton
    @Provides
    @AuthToken
    fun provideAuthTokenProvider(
        appPreferences: AppPreferences,
        @ApplicationCoroutineScope scope: CoroutineScope
    ): AuthTokenProvider = AuthTokenProvider(appPreferences, scope)


    @Singleton
    @Provides
    fun provideTokenProviders(
        appPreferences: AppPreferences,
        profilePreferences: ProfilePreferences,
    ): TokensProvider = TokensProvider(appPreferences, profilePreferences)

    @Singleton
    @Provides
    fun provideEuforiaApi(
        retrofit: Retrofit
    ): EuforiaApi = retrofit.create(EuforiaApi::class.java)

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.APP_API_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ResultAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()


    @Singleton
    @Provides
    fun provideOkHttpClient(
        headersInterceptor: HeadersInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(headersInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideHeaderInterceptor(
        tokensProvider: TokensProvider,
//        @DeviceToken deviceTokenProvider: DeviceTokenProvider,
//        @AuthToken authTokenProvider: AuthTokenProvider,
    ): HeadersInterceptor {
        return HeadersInterceptor(tokensProvider)
    }

    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }


    companion object {
        const val NAMED_MOSHI_NETWORK = "moshi_network"
        const val TIMEOUT_SEC = 60L
    }
}