package digital.euforia.app.ui.settings.personaldata.cleardata

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.db.AppDatabase
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ClearDataViewModel @Inject constructor(
    private val database: AppDatabase,
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val dataStore: DataStore<Preferences>,
    val analyticSender: AnalyticSender,
    @ApplicationContext private val context: Context,
) : ViewModel(), ContainerHost<ClearDataState, ClearDataSideEffect> {
    override val container = container<ClearDataState, ClearDataSideEffect>(
        initialState = ClearDataState(),
        onCreate = {
            analyticSender.clearAccountShow()
        }
    )

    fun onDeleteClick() {
        viewModelScope.launch {
            analyticSender.clearAccountClearClick()
            try {
                // Clear all Room database tables
                database.accompanimentDao().clearAll()
                database.accompanimentItemDao().clearAll()
                database.fileDao().clearAll()
                database.phraseDao().clearAll()
                database.appSettingsDao().clear()
                database.packageDao().clearAll()
                database.meditationDao().clearAll()
                database.exerciseDao().clearAll()
                database.articleDao().clearAll()
                database.musicDao().clearAll()
                database.resourceDao().clearAll()
                database.faqCategoryDao().clearAll()
                database.faqItemDao().clearAll()

                // Clear feedback forms
                database.feedbackFormDao().clearAll()

//                // Clear LocalBillingDb
//                val billingDb = LocalBillingDb.getInstance(context)
//                billingDb.purchaseDao().deleteAll()
//                billingDb.entitlementsDao().deleteAll()
//                billingDb.skuDetailsDao().clearAll()

                // Clear DataStore preferences
                profilePreferences.clearAll()
                appPreferences.clearAll()

                // Emit side effect to restart the app
                analyticSender.clearAccountClearFinished()
                intent { postSideEffect(ClearDataSideEffect.RestartApp) }
            } catch (e: Exception) {
                Timber.e(e, "Error clearing data")
            }
        }
    }

}

data class ClearDataState(val errorMessage: String? = null)

sealed class ClearDataSideEffect {
    object RestartApp : ClearDataSideEffect()
}