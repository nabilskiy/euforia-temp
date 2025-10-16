package digital.euforia.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.AppDatabase
import digital.euforia.app.data.repository.AccompanimentItemRepository
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.repository.AppSettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideAccompanimentRepository(
        api: EuforiaApi,
        database: AppDatabase
    ): AccompanimentRepository {
        return AccompanimentRepository(
            api,
            database.accompanimentDao(),
            database.fileDao(),
            database.accompanimentItemDao()
        )
    }

    @Singleton
    @Provides
    fun provideAccompanimentItemRepository(
        api: EuforiaApi,
        database: AppDatabase
    ): AccompanimentItemRepository {
        return AccompanimentItemRepository(database.accompanimentItemDao())
    }

    @Singleton
    @Provides
    fun provideAppSettingsRepository(
        api: EuforiaApi,
        database: AppDatabase
    ): AppSettingsRepository {
        return AppSettingsRepository(
            api,
            database.appSettingsDao()
        )
    }

}