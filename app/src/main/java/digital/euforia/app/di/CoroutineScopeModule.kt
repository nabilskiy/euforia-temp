package digital.euforia.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class CoroutineScopeModule {

    @Provides
    @Singleton
    @ApplicationCoroutineScope
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main + getCoroutineExceptionHandler())
    }

    @Provides
    @Singleton
    @ApplicationCoroutineScopeIO
    fun provideIOCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO + getCoroutineExceptionHandler())
    }

    @Provides
    @Singleton
    @ApplicationCoroutineScopeDefault
    fun provideDefaultCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default + getCoroutineExceptionHandler())
    }

    private fun getCoroutineExceptionHandler() =
        CoroutineExceptionHandler { _, throwable ->
            Timber.tag("AppCoroutineExceptionHandler").e(throwable)
        }
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationCoroutineScope

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationCoroutineScopeIO

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationCoroutineScopeDefault