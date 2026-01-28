package digital.euforia.app.data.config

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType
import digital.euforia.app.domain.model.config.BannerConfig
import digital.euforia.app.domain.model.config.DemoUnlockDayConfig
import digital.euforia.app.domain.model.config.ExerciseConfig
import digital.euforia.app.domain.model.config.ProgramsConfig
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.config.defaultTimeOfDayConfig
import digital.euforia.app.domain.model.config.toDomain
import digital.euforia.app.domain.model.plan.TodayPresentType
import digital.euforia.app.domain.model.plan.toTodayPresentType
import timber.log.Timber

class EuforiaRemoteConfigFetcher(
    private val moshi: Moshi,
    private val onConfigsUpdatedListener: (EuforiaRemoteConfigFetcher) -> Unit
) : FirebaseRemoteConfigFetcher(moshi) {
    override fun onConfigsUpdated() {
        super.onConfigsUpdated()
        onConfigsUpdatedListener(this)
    }

    override fun logError(tag: String, e: Throwable) {
        Timber.tag(tag).e(e)
    }

    fun getAllowedLanguages(): List<String> {
        return getListConfig(KEY_ALLOWED_LANGUAGES).dataOrNull ?: emptyList()
    }

    fun getIntroEmailStepShow(): Boolean {
        return getBooleanConfig(KEY_INTRO_EMAIL_STEP_SHOW).dataOrNull ?: true
    }

    fun getIntroGoalsStepShow(): Boolean {
        return getBooleanConfig(KEY_INTRO_GOALS_STEP_SHOW).dataOrNull ?: true
    }

    fun getIntroInterestsStepShow(): Boolean {
        return getBooleanConfig(KEY_INTRO_INTERESTS_STEP_SHOW).dataOrNull ?: true
    }

    fun getIntroLangStepShow(): Boolean {
        return getBooleanConfig(KEY_INTRO_LANG_STEP_SHOW).dataOrNull ?: true
    }

    fun getIntroNameStepShow(): Boolean {
        return getBooleanConfig(KEY_INTRO_NAME_STEP_SHOW).dataOrNull ?: true
    }

    fun getIntroNotificationsStepShow(): Boolean {
        return getBooleanConfig(KEY_INTRO_NOTIFICATIONS_STEP_SHOW).dataOrNull ?: true
    }

    fun getIntroPremiumScreenEnabled(): Boolean {
        return getBooleanConfig(KEY_INTRO_PREMIUM_SCREEN_ENABLED).dataOrNull ?: true
    }

    fun getIntroReasonsStepShow(): Boolean {
        return getBooleanConfig(KEY_INTRO_REASONS_STEP_SHOW).dataOrNull ?: true
    }

    fun getIntroVideoSkipAllow(): Boolean {
        return getBooleanConfig(KEY_INTRO_VIDEO_SKIP_ALLOW).dataOrNull ?: true
    }

    fun getTimeOfDayConfig(): TimeOfDayConfig? {
        return getConfig(
            key = KEY_TIME_OF_DAY_CONFIG,
            moshiClazz = NetworkTimeOfDayConfig::class.java,
            map = { it.toTimeOfDayConfig() }
        ).dataOrNull ?: defaultTimeOfDayConfig()
    }

    fun getTodayPresentType(): TodayPresentType {
        return remoteConfig.getString(KEY_TODAY_PRESENT_TYPE).ifBlank { "none" }
            .toTodayPresentType()
    }

    fun getTodayBannerConfig(): BannerConfig? {
        return getConfig(
            key = KEY_TODAY_BANNER_1,
            moshiClazz = NetworkBannerConfig::class.java,
            map = { it.toBannerConfig() }
        ).dataOrNull
    }

    fun getStringsJson(): String {
        return remoteConfig.getString(KEY_STRINGS)
    }

    fun getExtraPackageId(): String? {
        val packageId = remoteConfig.getString(KEY_EXTRA_PACKAGE_ID)
        return packageId.ifBlank { null }
    }

    fun getVoiceAvatarPreviewsIds(): List<Int> {
        return getListConfig(KEY_VOICE_AVATAR_PREVIEWS_ID).dataOrNull?.map { it.toInt() }
            ?: emptyList()
    }

    fun getDemoUnlockDayConfig(): DemoUnlockDayConfig {
        return getConfig(
            key = KEY_VIBES_DEMO_UNLOCK_DAY_CONFIG,
            moshiClazz = NetworkDemoUnlockDayConfig::class.java,
            map = { it.toDemoUnlockDayConfig() }
        ).dataOrNull ?: DemoUnlockDayConfig()
    }

    fun getTodayIntroVideoUrl(): String? {
        val url = remoteConfig.getString(KEY_TODAY_INTRO_VIDEO)
        return url.ifBlank { null }
    }

    fun getTodayIntroVideoCoverUrl(): String? {
        val url = remoteConfig.getString(KEY_TODAY_INTRO_VIDEO_COVER)
        return url.ifBlank { null }
    }

    fun getFeedbackFormId(): String? {
        val formId = remoteConfig.getString(KEY_FEEDBACK_FORM_ID)
        return formId.ifBlank { null }
    }

    fun getSosOption1VideoUrl(): String? {
        val url = remoteConfig.getString(KEY_SOS_OPTION_1_VIDEO)
        return url.ifBlank { null }
    }

    fun getSosOption2VideoUrl(): String? {
        val url = remoteConfig.getString(KEY_SOS_OPTION_2_VIDEO)
        return url.ifBlank { null }
    }

    fun getProgramsTitle(): String {
        val title = remoteConfig.getString(KEY_LIBRARY_TITLE)
        return title.ifBlank { "Programs" }
    }

    fun getProgramsConfig(): List<ProgramsConfig> {
        // The library config is an ARRAY of blocks. We should pick only the "package_list" block
        // from the active template selected by key "library_list_template_key".
        return try {
            val templateKey = remoteConfig.getString(KEY_LIBRARY_LIST_TEMPLATE_KEY)
                .ifBlank { DEFAULT_LIBRARY_TEMPLATE_KEY }

            val json = remoteConfig.getString(templateKey)
                .ifBlank { remoteConfig.getString(KEY_LIBRARY_LIST_TEMPLATE) }

            if (json.isBlank()) return emptyList()

            val listType = newParameterizedType(
                MutableList::class.java,
                NetworkPackageConfig::class.java
            )
            val blocks: List<NetworkPackageConfig> =
                moshi.adapter<List<NetworkPackageConfig>>(listType).fromJson(json) ?: emptyList()

            blocks.map { block ->
                block.toDomain()
            }
        } catch (e: Throwable) {
            logError(TAG, e)
           emptyList()
        }
    }

    fun getExercisesConfig(): List<ExerciseConfig> {
        return try {
            val json = remoteConfig.getString(KEY_EXERCISES_LIST_TEMPLATE)
            if (json.isBlank()) return emptyList()

            val listType = newParameterizedType(
                MutableList::class.java,
                NetworkExerciseConfig::class.java
            )
            val blocks: List<NetworkExerciseConfig> =
                moshi.adapter<List<NetworkExerciseConfig>>(listType).fromJson(json) ?: emptyList()

            blocks.map { block ->
                block.toDomain()
            }
        } catch (e: Throwable) {
            logError(TAG, e)
            emptyList()
        }
    }

    companion object {
        private const val KEY_ALLOWED_LANGUAGES = "allow_languages"
        private const val KEY_INTRO_EMAIL_STEP_SHOW = "intro_email_step_show"
        private const val KEY_INTRO_GOALS_STEP_SHOW = "intro_goals_step_show"
        private const val KEY_INTRO_INTERESTS_STEP_SHOW = "intro_interests_step_show"
        private const val KEY_INTRO_LANG_STEP_SHOW = "intro_lang_step_show"
        private const val KEY_INTRO_NAME_STEP_SHOW = "intro_name_step_show"
        private const val KEY_INTRO_NOTIFICATIONS_STEP_SHOW = "intro_notifications_step_show"
        private const val KEY_INTRO_PREMIUM_SCREEN_ENABLED = "intro_premium_screen_enabled"
        private const val KEY_INTRO_PREMIUM_SCREEN_VARIANT = "intro_premium_screen_variant"
        private const val KEY_INTRO_REASONS_STEP_SHOW = "intro_reasons_step_show"
        private const val KEY_INTRO_VIDEO_SKIP_ALLOW = "intro_video_skip_allow"
        private const val KEY_TIME_OF_DAY_CONFIG = "time_of_day_config"
        private const val KEY_TODAY_PRESENT_TYPE = "today_present_type"
        private const val KEY_TODAY_BANNER_1 = "today_banner_1"
        private const val KEY_STRINGS = "strings"
        private const val KEY_EXTRA_PACKAGE_ID = "extra_package_id"
        private const val KEY_VOICE_AVATAR_PREVIEWS_ID = "voice_avatar_previews_ids"
        private const val KEY_VIBES_DEMO_UNLOCK_DAY_CONFIG = "vibes_demo_unlock_day_config"
        private const val KEY_FEEDBACK_FORM_ID = "feedback_form_id"
        private const val KEY_TODAY_INTRO_VIDEO = "today_intro_video"
        private const val KEY_TODAY_INTRO_VIDEO_COVER = "today_intro_video_cover"
        private const val KEY_SOS_OPTION_1_VIDEO = "sos_option_1_video"
        private const val KEY_SOS_OPTION_2_VIDEO = "sos_option_2_video"
        private const val KEY_LIBRARY_TITLE = "library_title"
        private const val KEY_LIBRARY_LIST_TEMPLATE_KEY = "library_list_template_key"
        private const val KEY_LIBRARY_LIST_TEMPLATE = "library_list_template"
        private const val KEY_EXERCISES_LIST_TEMPLATE = "exercises_list_template"
        private const val DEFAULT_LIBRARY_TEMPLATE_KEY = "library_2_list_template"
    }
}