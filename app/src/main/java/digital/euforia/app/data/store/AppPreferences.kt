package digital.euforia.app.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.firebase.datastorage.getOrDefault
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
    private val keyIsTimeSensitiveKey = booleanPreferencesKey("isTimeSensitive")
    private val shouldSyncPackagesKey = booleanPreferencesKey("should_sync_packages")
    private val keyMeditationBackgroundIndex = intPreferencesKey("meditation_background_index")
    private val keyDailyPlayedSeconds = longPreferencesKey("daily_played_seconds")
    private val keyDailyCompletedAccompanimentsCount = intPreferencesKey("daily_completed_accompaniments_count")
    private val keyLastPlaybackResetDate = stringPreferencesKey("last_playback_reset_date")
    private val keyDeletedAvatarIds = stringSetPreferencesKey("deleted_avatar_ids")
    private val keyCustomAvatarUris = stringSetPreferencesKey("custom_avatar_uris")
    private val keySoundscapesEnabled = booleanPreferencesKey("soundscapes_enabled")
    private val keySoundscapesLevel = intPreferencesKey("soundscapes_level")
    private val keySoundscapesLastPreset = intPreferencesKey("soundscapes_last_preset")
    private val keyFavoriteMusicIds = stringSetPreferencesKey("favorite_music_ids")
    private val keySoundAnimationsEnabled = booleanPreferencesKey("sound_animations_enabled")

    // Rating app preferences
    private val keyRateAppLaunchCount = intPreferencesKey("rate_app_launch_count")
    private val keyRateAppLastPromptAt = longPreferencesKey("rate_app_last_prompt_at")
    private val keyRateAppRated = booleanPreferencesKey("rate_app_rated")
    private val keyFeedbackFormLaunchCount = intPreferencesKey("feedback_form_launch_count")
    private val keyFeedbackFormLastPromptAt = longPreferencesKey("feedback_form_last_prompt_at")
    private val keyFeedbackFormCompleted = booleanPreferencesKey("feedback_form_completed")
    private val keyEmailAlertLaunchCount = intPreferencesKey("email_alert_launch_count")
    private val keyEmailAlertLastPromptAt = longPreferencesKey("email_alert_last_prompt_at")

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
        return store.data.firstOrNull()?.getOrDefault(keyCompletedDays, 0) ?: 0
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
        return store.data.firstOrNull()?.get(keyMorningNotificationEnabled) ?: true
    }

    suspend fun setDayNotificationEnabled(enabled: Boolean) {
        store.edit { preferences ->
            preferences[keyDayNotificationEnabled] = enabled
        }
    }

    suspend fun isDayNotificationEnabled(): Boolean {
        return store.data.firstOrNull()?.get(keyDayNotificationEnabled) ?: true
    }

    suspend fun setEveningNotificationEnabled(enabled: Boolean) {
        store.edit { preferences ->
            preferences[keyEveningNotificationEnabled] = enabled
        }
    }

    suspend fun isEveningNotificationEnabled(): Boolean {
        return store.data.firstOrNull()?.get(keyEveningNotificationEnabled) ?: true
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

    suspend fun setIsTimeSensitive(enabled: Boolean) {
        store.edit { preferences ->
            preferences[keyIsTimeSensitiveKey] = enabled
        }
    }

    suspend fun getIsTimeSensitive(): Boolean {
        return store.data.firstOrNull()?.get(keyIsTimeSensitiveKey) ?: true
    }

    // region Rate App
    suspend fun getRateAppLaunchCount(): Int {
        return store.data.firstOrNull()?.get(keyRateAppLaunchCount) ?: 0
    }

    suspend fun incrementRateAppLaunchCount() {
        store.edit { preferences ->
            val current = preferences[keyRateAppLaunchCount] ?: 0
            preferences[keyRateAppLaunchCount] = current + 1
        }
    }

    suspend fun setRateAppLaunchCount(count: Int) {
        store.edit { preferences ->
            preferences[keyRateAppLaunchCount] = count
        }
    }

    suspend fun getRateAppLastPromptAt(): Long? {
        return store.data.firstOrNull()?.get(keyRateAppLastPromptAt)
    }

    suspend fun setRateAppLastPromptNow() {
        store.edit { preferences ->
            preferences[keyRateAppLastPromptAt] = System.currentTimeMillis()
        }
    }

    suspend fun isRateAppRated(): Boolean {
        return store.data.firstOrNull()?.get(keyRateAppRated) ?: false
    }

    suspend fun setRateAppRated(rated: Boolean) {
        store.edit { preferences ->
            preferences[keyRateAppRated] = rated
        }
    }
    // endregion Rate App

    // region Feedback Form
    suspend fun getFeedbackFormLaunchCount(): Int {
        return store.data.firstOrNull()?.get(keyFeedbackFormLaunchCount) ?: 0
    }

    suspend fun incrementFeedbackFormLaunchCount() {
        store.edit { preferences ->
            val current = preferences[keyFeedbackFormLaunchCount] ?: 0
            preferences[keyFeedbackFormLaunchCount] = current + 1
        }
    }

    suspend fun setFeedbackFormLaunchCount(count: Int) {
        store.edit { preferences ->
            preferences[keyFeedbackFormLaunchCount] = count
        }
    }

    suspend fun getFeedbackFormLastPromptAt(): Long? {
        return store.data.firstOrNull()?.get(keyFeedbackFormLastPromptAt)
    }

    suspend fun setFeedbackFormLastPromptNow() {
        store.edit { preferences ->
            preferences[keyFeedbackFormLastPromptAt] = System.currentTimeMillis()
        }
    }

    suspend fun isFeedbackFormCompleted(): Boolean {
        return store.data.firstOrNull()?.get(keyFeedbackFormCompleted) ?: false
    }

    suspend fun setFeedbackFormCompleted(completed: Boolean) {
        store.edit { preferences ->
            preferences[keyFeedbackFormCompleted] = completed
        }
    }
    // endregion Feedback Form

    // region Email Alert
    suspend fun getEmailAlertLaunchCount(): Int {
        return store.data.firstOrNull()?.get(keyEmailAlertLaunchCount) ?: 0
    }

    suspend fun incrementEmailAlertLaunchCount() {
        store.edit { preferences ->
            val current = preferences[keyEmailAlertLaunchCount] ?: 0
            preferences[keyEmailAlertLaunchCount] = current + 1
        }
    }

    suspend fun setEmailAlertLaunchCount(count: Int) {
        store.edit { preferences ->
            preferences[keyEmailAlertLaunchCount] = count
        }
    }

    suspend fun getEmailAlertLastPromptAt(): Long? {
        return store.data.firstOrNull()?.get(keyEmailAlertLastPromptAt)
    }

    suspend fun setEmailAlertLastPromptNow() {
        store.edit { preferences ->
            preferences[keyEmailAlertLastPromptAt] = System.currentTimeMillis()
        }
    }
    // endregion Email Alert

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

    suspend fun getDailyPlayedSeconds(): Long {
        checkDailyReset()
        return store.data.firstOrNull()?.get(keyDailyPlayedSeconds) ?: 0L
    }

    suspend fun incrementDailyPlayedSeconds(seconds: Long) {
        checkDailyReset()
        store.edit { preferences ->
            val current = preferences[keyDailyPlayedSeconds] ?: 0L
            preferences[keyDailyPlayedSeconds] = current + seconds
        }
    }

    fun getDailyPlayedSecondsFlow(): Flow<Long> {
        return store.data.map { preferences ->
            // Note: We don't call checkDailyReset() here to avoid side effects in a Flow.
            // Reset should be triggered by explicit calls to getters/setters or periodically.
            preferences[keyDailyPlayedSeconds] ?: 0L
        }
    }

    suspend fun getDailyCompletedAccompanimentsCount(): Int {
        checkDailyReset()
        return store.data.firstOrNull()?.get(keyDailyCompletedAccompanimentsCount) ?: 0
    }

    suspend fun incrementDailyCompletedAccompanimentsCount() {
        checkDailyReset()
        store.edit { preferences ->
            val current = preferences[keyDailyCompletedAccompanimentsCount] ?: 0
            preferences[keyDailyCompletedAccompanimentsCount] = current + 1
        }
    }

    suspend fun getDailyCompletedAccompanimentsCountFlow(): Flow<Int> {
        return store.data.map { preferences ->
            preferences[keyDailyCompletedAccompanimentsCount] ?: 0
        }
    }

    suspend fun getDeletedAvatarIds(): Set<Int> {
        return store.data.firstOrNull()?.get(keyDeletedAvatarIds)?.mapNotNull { it.toIntOrNull() }?.toSet()
            ?: emptySet()
    }

    suspend fun addDeletedAvatarIds(ids: List<Int>) {
        store.edit { preferences ->
            val current = preferences[keyDeletedAvatarIds] ?: emptySet()
            preferences[keyDeletedAvatarIds] = current + ids.map { it.toString() }.toSet()
        }
    }

    suspend fun getCustomAvatarUris(): Set<String> {
        return store.data.firstOrNull()?.get(keyCustomAvatarUris) ?: emptySet()
    }

    suspend fun addCustomAvatarUri(id: String, uri: String) {
        store.edit { preferences ->
            val current = preferences[keyCustomAvatarUris] ?: emptySet()
            preferences[keyCustomAvatarUris] = current + "$id|$uri"
        }
    }

    suspend fun setSoundscapesEnabled(enabled: Boolean) {
        store.edit { preferences ->
            preferences[keySoundscapesEnabled] = enabled
        }
    }

    suspend fun isSoundscapesEnabled(): Boolean {
        return store.data.firstOrNull()?.get(keySoundscapesEnabled) ?: true
    }

    fun isSoundscapesEnabledFlow(): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[keySoundscapesEnabled] ?: true
        }
    }

    suspend fun setSoundscapesLevel(level: Int) {
        store.edit { preferences ->
            preferences[keySoundscapesLevel] = level.coerceIn(0, 100)
        }
    }

    suspend fun getSoundscapesLevel(): Int {
        return store.data.firstOrNull()?.get(keySoundscapesLevel) ?: 35
    }

    fun getSoundscapesLevelFlow(): Flow<Int> {
        return store.data.map { preferences ->
            preferences[keySoundscapesLevel] ?: 35
        }
    }

    suspend fun setSoundscapesLastPreset(presetId: Int) {
        store.edit { preferences ->
            preferences[keySoundscapesLastPreset] = presetId
        }
    }

    suspend fun getSoundscapesLastPreset(): Int {
        return store.data.firstOrNull()?.get(keySoundscapesLastPreset) ?: -1
    }

    suspend fun getFavoriteMusicIds(): Set<Int> {
        return store.data.firstOrNull()?.get(keyFavoriteMusicIds)
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun getFavoriteMusicIdsFlow(): Flow<Set<Int>> {
        return store.data.map { preferences ->
            preferences[keyFavoriteMusicIds]
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet()
                ?: emptySet()
        }
    }

    suspend fun setFavoriteMusicIds(ids: Set<Int>) {
        store.edit { preferences ->
            preferences[keyFavoriteMusicIds] = ids.map(Int::toString).toSet()
        }
    }

    suspend fun setSoundAnimationsEnabled(enabled: Boolean) {
        store.edit { preferences ->
            preferences[keySoundAnimationsEnabled] = enabled
        }
    }

    suspend fun isSoundAnimationsEnabled(): Boolean {
        return store.data.firstOrNull()?.get(keySoundAnimationsEnabled) ?: true
    }

    fun isSoundAnimationsEnabledFlow(): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[keySoundAnimationsEnabled] ?: true
        }
    }

    private suspend fun checkDailyReset() {
        val today = java.time.LocalDate.now().toString()
        val lastReset = store.data.firstOrNull()?.get(keyLastPlaybackResetDate)
        if (lastReset != today) {
            store.edit { preferences ->
                preferences[keyDailyPlayedSeconds] = 0L
                preferences[keyDailyCompletedAccompanimentsCount] = 0
                preferences[keyLastPlaybackResetDate] = today
            }
        }
    }
}
