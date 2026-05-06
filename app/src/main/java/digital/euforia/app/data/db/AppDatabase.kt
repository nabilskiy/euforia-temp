/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import digital.euforia.app.data.db.converter.AccompanimentTypeConverters
import digital.euforia.app.data.db.converter.FeedbackTypeConverters
import digital.euforia.app.data.db.converter.IntListConverter
import digital.euforia.app.data.db.converter.PhraseListConverters
import digital.euforia.app.data.db.converter.PublicationTypeConverter
import digital.euforia.app.data.db.dao.AccompanimentDao
import digital.euforia.app.data.db.dao.FileDao
import digital.euforia.app.data.db.dao.SampleEntityDao
import digital.euforia.app.data.db.dao.PhraseDao
import digital.euforia.app.data.db.dao.AccompanimentItemDao
import digital.euforia.app.data.db.dao.AppSettingsDao
import digital.euforia.app.data.db.dao.ArticleDao
import digital.euforia.app.data.db.dao.ExerciseDao
import digital.euforia.app.data.db.dao.MeditationDao
import digital.euforia.app.data.db.dao.MusicDao
import digital.euforia.app.data.db.dao.MusicCategoryDao
import digital.euforia.app.data.db.dao.PackageDao
import digital.euforia.app.data.db.dao.ResourceDao
import digital.euforia.app.data.db.dao.SceneCategoryDao
import digital.euforia.app.data.db.dao.SceneDao
import digital.euforia.app.data.db.dao.FaqCategoryDao
import digital.euforia.app.data.db.dao.FaqItemDao
import digital.euforia.app.data.db.dao.FavouritePublicationsDao
import digital.euforia.app.data.db.dao.FeedbackFormDao
import digital.euforia.app.data.db.dao.SoundscapeDownloadDao
import digital.euforia.app.data.db.dao.SoundscapeSceneLocalStateDao
import digital.euforia.app.data.db.dao.SoundscapePlaylistDao
import digital.euforia.app.data.db.dao.SoundscapePresetDao
import digital.euforia.app.data.db.dao.SoundscapeSoundDao
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.File
import digital.euforia.app.data.db.entity.SampleEntity
import digital.euforia.app.data.db.entity.Phrase
import digital.euforia.app.data.db.entity.AccompanimentItem
import digital.euforia.app.data.db.entity.AppSettings
import digital.euforia.app.data.db.entity.Article
import digital.euforia.app.data.db.entity.Exercise
import digital.euforia.app.data.db.entity.Meditation
import digital.euforia.app.data.db.entity.Music
import digital.euforia.app.data.db.entity.MusicCategory
import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.data.db.entity.Resource
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SceneCategory
import digital.euforia.app.data.db.entity.FaqCategory
import digital.euforia.app.data.db.entity.FaqItem
import digital.euforia.app.data.db.entity.FavouritePublication
import digital.euforia.app.data.db.entity.FeedbackForm
import digital.euforia.app.data.db.entity.FeedbackOption
import digital.euforia.app.data.db.entity.FeedbackQuestion
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.db.entity.SoundscapeSceneLocalState
import digital.euforia.app.data.db.entity.SoundscapePlaylist
import digital.euforia.app.data.db.entity.SoundscapePreset
import digital.euforia.app.data.db.entity.SoundscapeSound
import digital.euforia.app.data.db.entity.SoundscapeSoundCategory

@TypeConverters(value = [AccompanimentTypeConverters::class, PhraseListConverters::class, FeedbackTypeConverters::class, IntListConverter::class, PublicationTypeConverter::class])
@Database(
    version = 17,
    entities = [
        SampleEntity::class,
        Accompaniment::class,
        File::class,
        Phrase::class,
        AccompanimentItem::class,
        AppSettings::class,
        Package::class,
        Meditation::class,
        Exercise::class,
        Article::class,
        Music::class,
        MusicCategory::class,
        Resource::class,
        FaqCategory::class,
        FaqItem::class,
        FeedbackForm::class,
        FeedbackQuestion::class,
        FeedbackOption::class,
        FavouritePublication::class,
        SceneCategory::class,
        Scene::class,
        SoundscapePlaylist::class,
        SoundscapePreset::class,
        SoundscapeDownloadItem::class,
        SoundscapeSound::class,
        SoundscapeSoundCategory::class,
        SoundscapeSceneLocalState::class,
    ]
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun sampleEntityDao(): SampleEntityDao
    abstract fun accompanimentDao(): AccompanimentDao
    abstract fun fileDao(): FileDao
    abstract fun phraseDao(): PhraseDao
    abstract fun accompanimentItemDao(): AccompanimentItemDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun packageDao(): PackageDao
    abstract fun meditationDao(): MeditationDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun articleDao(): ArticleDao
    abstract fun musicDao(): MusicDao
    abstract fun musicCategoryDao(): MusicCategoryDao
    abstract fun resourceDao(): ResourceDao
    abstract fun faqCategoryDao(): FaqCategoryDao
    abstract fun faqItemDao(): FaqItemDao
    abstract fun feedbackFormDao(): FeedbackFormDao
    abstract fun favouritePublicationsDao(): FavouritePublicationsDao
    abstract fun sceneCategoryDao(): SceneCategoryDao
    abstract fun sceneDao(): SceneDao
    abstract fun soundscapePlaylistDao(): SoundscapePlaylistDao
    abstract fun soundscapePresetDao(): SoundscapePresetDao
    abstract fun soundscapeDownloadDao(): SoundscapeDownloadDao
    abstract fun soundscapeSoundDao(): SoundscapeSoundDao
    abstract fun soundscapeSceneLocalStateDao(): SoundscapeSceneLocalStateDao
}