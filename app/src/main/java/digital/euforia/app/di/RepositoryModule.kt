/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.AppDatabase
import digital.euforia.app.data.repository.AccompanimentItemRepository
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.repository.AppSettingsRepository
import digital.euforia.app.data.repository.ArticleRepository
import digital.euforia.app.data.repository.ExerciseRepository
import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.data.repository.ResourceRepository
import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.data.repository.FaqCategoryRepository
import digital.euforia.app.data.repository.FeedbackFormRepository
import digital.euforia.app.data.repository.MeditationRepository
import digital.euforia.app.data.repository.MusicRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.service.notifications.NotificationScheduler
import digital.euforia.app.service.notifications.WorkManagerNotificationScheduler
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

    @Singleton
    @Provides
    fun providePackageRepository(
        api: EuforiaApi,
        database: AppDatabase,
        appPreferences: AppPreferences,
        publicationInfoMapper: PublicationInfoMapper,
    ): PackageRepository {
        return PackageRepository(
            api = api,
            database = database,
            packageDao = database.packageDao(),
            meditationDao = database.meditationDao(),
            exerciseDao = database.exerciseDao(),
            articleDao = database.articleDao(),
            appPreferences = appPreferences,
            publicationInfoMapper = publicationInfoMapper,
        )
    }

    @Singleton
    @Provides
    fun provideResourcesRepository(
        api: EuforiaApi,
        database: AppDatabase
    ): ResourceRepository {
        return ResourceRepository(
            api = api,
            resourceDao = database.resourceDao()
        )
    }

    @Singleton
    @Provides
    fun provideFaqCategoryRepository(
        api: EuforiaApi,
        database: AppDatabase
    ): FaqCategoryRepository {
        return FaqCategoryRepository(
            api = api,
            faqCategoryDao = database.faqCategoryDao(),
            faqItemDao = database.faqItemDao()
        )
    }

    @Singleton
    @Provides
    fun provideFeedbackFormRepository(
        database: AppDatabase
    ): FeedbackFormRepository {
        return FeedbackFormRepository(
            feedbackFormDao = database.feedbackFormDao()
        )
    }

    @Singleton
    @Provides
    fun provideArticleRepository(
        api: EuforiaApi,
        database: AppDatabase,
    ): ArticleRepository {
        return ArticleRepository(
            api = api,
            articleDao = database.articleDao(),
        )
    }

    @Singleton
    @Provides
    fun provideExerciseRepository(
        api: EuforiaApi,
        database: AppDatabase,
    ): ExerciseRepository {
        return ExerciseRepository(
            api = api,
            exerciseDao = database.exerciseDao(),
        )
    }

    @Singleton
    @Provides
    fun provideMeditationRepository(
        api: EuforiaApi,
        database: AppDatabase,
    ): MeditationRepository {
        return MeditationRepository(
            api = api,
            meditationDao = database.meditationDao(),
        )
    }

    @Singleton
    @Provides
    fun provideMusicRepository(
        api: EuforiaApi,
        database: AppDatabase,
    ): MusicRepository {
        return MusicRepository(
            api = api,
            musicDao = database.musicDao(),
            musicCategoryDao = database.musicCategoryDao(),
        )
    }

    @Singleton
    @Provides
    fun provideSoundscapesRepository(
        api: EuforiaApi,
        database: AppDatabase,
    ): SoundscapesRepository {
        return SoundscapesRepository(
            api = api,
            sceneCategoryDao = database.sceneCategoryDao(),
            sceneDao = database.sceneDao(),
            soundDao = database.soundscapeSoundDao(),
            localStateDao = database.soundscapeSceneLocalStateDao(),
            playlistDao = database.soundscapePlaylistDao(),
            presetDao = database.soundscapePresetDao(),
            downloadDao = database.soundscapeDownloadDao(),
            savedSoundscapeDao = database.savedSoundscapeDao(),
        )
    }

    @Provides
    fun provideScheduler(
        @ApplicationContext context: Context
    ): NotificationScheduler =
        WorkManagerNotificationScheduler(context)
}