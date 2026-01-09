package digital.euforia.app.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import digital.euforia.app.billing.BillingRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {
    @Provides
    @Singleton
    fun provideBillingRepository(
        @ApplicationContext context: Context
    ): BillingRepository = BillingRepository.getInstance(context.applicationContext as Application)
}