package digital.euforia.app.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import digital.euforia.app.domain.model.onboarding.Gender
import digital.euforia.app.domain.model.subscription.SubscriptionLevel
import digital.euforia.app.domain.model.subscription.toSubscriptionLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class ProfilePreferences(
    private val store: DataStore<Preferences>
) {
    private val keySubscriptionLevel = intPreferencesKey("subscription_level")
    private val keyIsPremium = booleanPreferencesKey("is_premium")
    private val keyIsDemo = booleanPreferencesKey("is_demo")
    private val keyDemoSkipTimestamp = longPreferencesKey("demo_skip_timestamp")
    private val keyName = stringPreferencesKey("name")
    private val keyEmail = stringPreferencesKey("email")
    private val keyGender = intPreferencesKey("gender")
    suspend fun setSubscriptionLevel(level: SubscriptionLevel) {
        store.edit { preferences ->
            preferences[keySubscriptionLevel] = level.ordinal
        }
    }

    suspend fun getSubscriptionLevel(): SubscriptionLevel {
        val prefs = store.data.firstOrNull()
        val index = prefs?.get(keySubscriptionLevel) ?: 0
        return index.toSubscriptionLevel()
    }

    suspend fun getSubscriptionLevelFlow(): Flow<SubscriptionLevel> {
        return store.data.map { preferences ->
            (preferences[keySubscriptionLevel] ?: 0).toSubscriptionLevel()
        }
    }

    suspend fun setIsPremium(isPremium: Boolean) {
        store.edit { preferences ->
            preferences[keyIsPremium] = isPremium
        }
    }

    suspend fun getIsPremium(): Boolean {
        val prefs = store.data.firstOrNull()
        return prefs?.get(keyIsPremium) ?: false
    }

    suspend fun getIsPremiumFlow(): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[keyIsPremium] ?: false
        }
    }

    suspend fun setIsDemo(isDemo: Boolean) {
        store.edit { preferences ->
            preferences[keyIsDemo] = isDemo
        }
    }

    suspend fun getIsDemo(): Boolean {
        val prefs = store.data.firstOrNull()
        return prefs?.get(keyIsDemo) ?: true
    }

    suspend fun getIsDemoFlow(): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[keyIsDemo] ?: true
        }
    }
    suspend fun getName(): String? {
        return store.data.firstOrNull()?.get(keyName)
    }

    suspend fun setName(name: String) {
        store.edit { preferences ->
            preferences[keyName] = name
        }
    }

    suspend fun getEmail(): String? {
        return store.data.firstOrNull()?.get(keyEmail)
    }

    suspend fun setEmail(email: String) {
        store.edit { preferences ->
            preferences[keyEmail] = email
        }
    }

    suspend fun getGender(): Gender {
        val genderIndex = store.data.firstOrNull()?.get(keyGender) ?: Gender.UNSPECIFIED.ordinal
        return Gender.entries.toTypedArray().getOrElse(genderIndex) { Gender.UNSPECIFIED }
    }

    suspend fun setGender(gender: Gender) {
        store.edit { preferences ->
            preferences[keyGender] = gender.ordinal
        }
    }
}