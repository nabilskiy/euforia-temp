package digital.euforia.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import digital.euforia.app.data.db.converter.AccompanimentTypeConverters
import digital.euforia.app.data.db.converter.PhraseListConverters
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
import digital.euforia.app.data.db.dao.PackageDao
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
import digital.euforia.app.data.db.entity.Package

@TypeConverters(value = [AccompanimentTypeConverters::class, PhraseListConverters::class])
@Database(
    version = 1,
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
}