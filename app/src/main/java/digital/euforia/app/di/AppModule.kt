package digital.euforia.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.db.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        return Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, DB_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideAnalyticSender(@ApplicationContext applicationContext: Context): AnalyticSender {
        val analyticSender = AnalyticSender(applicationContext)
        return analyticSender
    }

    companion object {
        private const val DB_NAME = "database-euforia"
        const val PREFS_NAME = "settings"
    }
}