package digital.euforia.app.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import digital.euforia.app.data.util.parseJsonToFlatMap
import digital.euforia.app.domain.model.onboarding.Gender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID

@Suppress("TooManyFunctions")
class AppPreferences(
    private val store: DataStore<Preferences>
) {
    private val keyDeviceToken = stringPreferencesKey("deviceToken")

    private val keyLanguage = stringPreferencesKey("language")
    private val keyNotificationPermissionGranted =
        booleanPreferencesKey("notification_permission_granted")
    private val keyCompletedDays = intPreferencesKey("completed_days")
    private val keyIsOnboardingCompleted = booleanPreferencesKey("is_onboarding_completed")
    private val keyCompletedDailyTasks = intPreferencesKey("completed_daily_tasks")
    private val keyContinuousDays = intPreferencesKey("continuous_days")
    private val keyFirstLaunchDate = longPreferencesKey("first_launch_date")
    private val keyLastLaunchDate = longPreferencesKey("last_launch_date")
    private val keyTranslationsJson = stringPreferencesKey("translations_json")
    private val keyMorningNotificationEnabled =
        booleanPreferencesKey("morning_notification_enabled")
    private val keyDayNotificationEnabled = booleanPreferencesKey("day_notification_enabled")
    private val keyEveningNotificationEnabled =
        booleanPreferencesKey("evening_notification_enabled")
    private val keyMorningNotificationTime = intPreferencesKey("morning_notification_time")
    private val keyDayNotificationTime = intPreferencesKey("day_notification_time")
    private val keyEveningNotificationTime = intPreferencesKey("evening_notification_time")
    private val shouldSyncPackagesKey = booleanPreferencesKey("should_sync_packages")
    private val keyMeditationBackgroundIndex = intPreferencesKey("meditation_background_index")

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

    suspend fun initDeviceToken(): String {
        val existing = store.data.firstOrNull()?.get(keyDeviceToken)
        if (existing != null) return existing

        val deviceToken = UUID.randomUUID().toString()
        store.edit { preferences ->
            preferences[keyDeviceToken] = deviceToken
        }
        return deviceToken
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

    fun isOnboardingCompletedFlow(): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[keyIsOnboardingCompleted] ?: false
        }
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
        try {
            val json = store.data.firstOrNull()?.get(keyTranslationsJson) ?: return emptyMap()
            return parseJsonToFlatMap(json)
        } catch (e: Exception) {
            Timber.d("Failed to parse translations JSON: ${e.message}")
            return emptyMap()
        }
    }

    suspend fun getTranslationsMapFlow(): Flow<Map<String, String>> {
        return store.data.map { preferences ->
            val json = preferences[keyTranslationsJson] ?: return@map emptyMap()
            parseJsonToFlatMap(json)
        }
    }

    suspend fun setMorningNotificationEnabled(enabled: Boolean) {
        store.edit { preferences ->
            preferences[keyMorningNotificationEnabled] = enabled
        }
    }

    suspend fun isMorningNotificationEnabled(): Boolean {
        return store.data.firstOrNull()?.get(keyMorningNotificationEnabled) ?: false
    }

    suspend fun setDayNotificationEnabled(enabled: Boolean) {
        store.edit { preferences ->
            preferences[keyDayNotificationEnabled] = enabled
        }
    }

    suspend fun isDayNotificationEnabled(): Boolean {
        return store.data.firstOrNull()?.get(keyDayNotificationEnabled) ?: false
    }

    suspend fun setEveningNotificationEnabled(enabled: Boolean) {
        store.edit { preferences ->
            preferences[keyEveningNotificationEnabled] = enabled
        }
    }

    suspend fun isEveningNotificationEnabled(): Boolean {
        return store.data.firstOrNull()?.get(keyEveningNotificationEnabled) ?: false
    }

    suspend fun setMorningNotificationTime(hours: Int, minutes: Int) {
        store.edit { preferences ->
            preferences[keyMorningNotificationTime] = hours * 60 + minutes
        }
    }

    suspend fun getMorningNotificationTime(): Pair<Int, Int> {
        val total = store.data.firstOrNull()?.get(keyMorningNotificationTime) ?: (7 * 60)
        return total / 60 to total % 60
    }

    suspend fun setDayNotificationTime(hours: Int, minutes: Int) {
        store.edit { preferences ->
            preferences[keyDayNotificationTime] = hours * 60 + minutes
        }
    }

    suspend fun getDayNotificationTime(): Pair<Int, Int> {
        val total = store.data.firstOrNull()?.get(keyDayNotificationTime) ?: (12 * 60)
        return total / 60 to total % 60
    }

    suspend fun setEveningNotificationTime(hours: Int, minutes: Int) {
        store.edit { preferences ->
            preferences[keyEveningNotificationTime] = hours * 60 + minutes
        }
    }

    suspend fun getEveningNotificationTime(): Pair<Int, Int> {
        val total = store.data.firstOrNull()?.get(keyEveningNotificationTime) ?: (20 * 60)
        return total / 60 to total % 60
    }

    suspend fun clearAll() {
        store.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun setShouldSyncPackages(shouldSync: Boolean) {
        store.edit { preferences ->
            preferences[shouldSyncPackagesKey] = shouldSync
        }
    }

    suspend fun getShouldSyncPackages(): Boolean {
        return store.data.firstOrNull()?.get(shouldSyncPackagesKey) ?: true
    }

    suspend fun setMeditationBackgroundIndex(index: Int) {
        store.edit { preferences ->
            preferences[keyMeditationBackgroundIndex] = index
        }
    }

    suspend fun getMeditationBackgroundIndex(): Int {
        return store.data.firstOrNull()?.get(keyMeditationBackgroundIndex) ?: -1
    }
}
