package digital.euforia.app.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import digital.euforia.app.data.util.parseJsonToFlatMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

@Suppress("TooManyFunctions")
class AppPreferences(
    private val store: DataStore<Preferences>
) {
    private val keyDeviceToken = stringPreferencesKey("deviceToken")
    private val keyLanguage = stringPreferencesKey("language")
    private val keyName = stringPreferencesKey("name")
    private val keyEmail = stringPreferencesKey("email")
    private val keyNotificationPermissionGranted =
        booleanPreferencesKey("notification_permission_granted")
    private val keyCompletedDays = intPreferencesKey("completed_days")
    private val keyIsOnboardingCompleted = booleanPreferencesKey("is_onboarding_completed")
    private val keyCompletedDailyTasks = intPreferencesKey("completed_daily_tasks")
    private val keyContinuousDays = intPreferencesKey("continuous_days")
    private val keyFirstLaunchDate = longPreferencesKey("first_launch_date")
    private val keyLastLaunchDate = longPreferencesKey("last_launch_date")
    private val keyTranslationsJson = stringPreferencesKey("translations_json")

    suspend fun getDeviceToken(): String {
        val existing = store.data.firstOrNull()?.get(keyDeviceToken)
        if (existing != null) return existing

        val deviceToken = UUID.randomUUID().toString()
        store.edit { preferences ->
            preferences[keyDeviceToken] = deviceToken
        }
        return deviceToken
    }

    suspend fun getDeviceTokenFlow(): Flow<String> {
        return store.data.map { preferences ->
            preferences[keyDeviceToken] ?: run {
                val deviceToken = UUID.randomUUID().toString()
                // Save the newly generated token
                store.edit { editPreferences ->
                    editPreferences[keyDeviceToken] = deviceToken
                }
                deviceToken
            }
        }
    }

    suspend fun setDeviceToken(token: String) {
        store.edit { preferences ->
            preferences[keyDeviceToken] = token
        }
    }

    suspend fun getLanguage(): String? {
        return store.data.firstOrNull()?.get(keyLanguage)
    }

    suspend fun setLanguage(language: String) {
        store.edit { preferences ->
            preferences[keyLanguage] = language
        }
    }

    fun getLanguageFlow(): Flow<String?> {
        return store.data.map { preferences ->
            preferences[keyLanguage]
        }
    }

    suspend fun setNotificationPermissionGranted(granted: Boolean) {
        store.edit { preferences ->
            preferences[keyNotificationPermissionGranted] = granted
        }
    }

    suspend fun isNotificationPermissionGranted(): Boolean {
        return store.data.firstOrNull()?.get(keyNotificationPermissionGranted) ?: false
    }

    fun isNotificationPermissionGrantedFlow(): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[keyNotificationPermissionGranted] ?: false
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

    suspend fun getCompletedDays(): Int {
        return store.data.firstOrNull()?.get(keyCompletedDays) ?: 0
    }

    suspend fun setCompletedDays(day: Int) {
        store.edit { preferences ->
            preferences[keyCompletedDays] = day
        }
    }

    fun getCompletedDaysFlow(): Flow<Int> {
        return store.data.map { preferences ->
            preferences[keyCompletedDays] ?: 0
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        store.edit { preferences ->
            preferences[keyIsOnboardingCompleted] = completed
        }
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return store.data.firstOrNull()?.get(keyIsOnboardingCompleted) ?: false
    }

    suspend fun getFirstLaunchDate(): Instant? {
        return store.data.firstOrNull()?.get(keyFirstLaunchDate)
            ?.let { Instant.ofEpochMilli(it) }
    }

    suspend fun setFirstLaunchDate() {
        store.edit { preferences ->
            preferences[keyFirstLaunchDate] = Instant.now().toEpochMilli()
        }
    }

    suspend fun getCompletedDailyTasks(): Int {
        return store.data.firstOrNull()?.get(keyCompletedDailyTasks) ?: 1
    }
    suspend fun setCompletedDailyTasks(count: Int) {
        store.edit { preferences ->
            preferences[keyCompletedDailyTasks] = count
        }
    }
    fun getCompletedDailyTasksFlow(): Flow<Int> {
        return store.data.map { preferences ->
            preferences[keyCompletedDailyTasks] ?: 1
        }
    }

    suspend fun getContinuousDays(): Int {
        return store.data.firstOrNull()?.get(keyContinuousDays) ?: 0
    }

    suspend fun setContinuousDays(days: Int) {
        store.edit { preferences ->
            preferences[keyContinuousDays] = days
        }
    }

    suspend fun getLastLaunchDate(): Instant? {
        return store.data.firstOrNull()?.get(keyLastLaunchDate)
            ?.let { Instant.ofEpochMilli(it) }
    }

    suspend fun setLastLaunchDate() {
        store.edit { preferences ->
            preferences[keyLastLaunchDate] = Instant.now().toEpochMilli()
        }
    }

    suspend fun setTranslationsJson(json: String) {
        store.edit { preferences ->
            preferences[keyTranslationsJson] = json
        }
    }

    suspend fun getTranslationsMap(): Map<String, String> {
        val json = store.data.firstOrNull()?.get(keyTranslationsJson) ?: return emptyMap()
        return parseJsonToFlatMap(json)
    }

    suspend fun getTranslationsMapFlow(): Flow<Map<String, String>> {
        return store.data.map { preferences ->
            val json = preferences[keyTranslationsJson] ?: return@map emptyMap()
            parseJsonToFlatMap(json)
        }
    }
}
