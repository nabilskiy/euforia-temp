package digital.euforia.app.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AnalyticSender @Inject constructor(
    @ApplicationContext val context: Context,
) {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    init {
        initFirebaseAnalytics()
    }

    fun initFirebaseAnalytics(userId: String? = null) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics?.setAnalyticsCollectionEnabled(true)
        if (userId != null && !userId.isEmpty()) firebaseAnalytics?.setUserId(userId)
    }

    private fun logEvent(eventName: String, params: Bundle? = null) {
//        firebaseAnalytics?.logEvent(eventName, params)
    }

    fun introShow() {
        logEvent(eventName = "intro_show")
    }

    fun introGenderShow() {
        logEvent(eventName = "intro_gender_show")
    }

    fun introGenderNext() {
        logEvent(eventName = "intro_gender_next")
    }

    fun introLangShow() {
        logEvent(eventName = "intro_lang_show")
    }

    fun introLangNext() {
        logEvent(eventName = "intro_lang_next")
    }

    fun introUaModeStarted() {
        logEvent(eventName = "intro_ua_mode_started")
    }

    fun introNameShow() {
        logEvent(eventName = "intro_name_show")
    }

    fun introNameNext() {
        logEvent(eventName = "intro_name_next")
    }

    fun introNameSkip() {
        logEvent(eventName = "intro_name_skip")
    }

    fun introNotificationsShow() {
        logEvent(eventName = "intro_notifications_show")
    }

    fun introNotificationsNext() {
        logEvent(eventName = "intro_notifications_next")
    }

    fun introNotificationsSkip() {
        logEvent(eventName = "intro_notifications_skip")
    }

    fun introEmailShow() {
        logEvent(eventName = "intro_email_show")
    }

    fun introEmailNext() {
        logEvent(eventName = "intro_email_next")
    }

    fun introEmailSkip() {
        logEvent(eventName = "intro_emial_skip")
    }

    fun introInterestsShow() {
        logEvent(eventName = "intro_interests_show")
    }

    fun introInterestsNext() {
        logEvent(eventName = "intro_interests_next")
    }

    fun introInterestsSkip() {
        logEvent(eventName = "intro_interests_skip")
    }

    fun introGoalsShow() {
        logEvent(eventName = "intro_goals_show")
    }

    fun introGoalsNext() {
        logEvent(eventName = "intro_goals_next")
    }

    fun introGoalsSkip() {
        logEvent(eventName = "intro_goals_skip")
    }

    fun introScenesShow() {
        logEvent(eventName = "intro_scenes_show")
    }

    fun introScenesNext() {
        logEvent(eventName = "intro_scenes_next")
    }

    fun introReasonsShow() {
        logEvent(eventName = "intro_reasons_show")
    }

    fun introReasonsNext() {
        logEvent(eventName = "intro_reasons_next")
    }

    fun introReasonsSkip() {
        logEvent(eventName = "intro_reasons_skip")
    }

    fun introTermsClick() {
        logEvent(eventName = "intro_terms_click")
    }

    fun introFinalShow() {
        logEvent(eventName = "intro_final_show")
    }

    fun introPreparingComplete() {
        logEvent(eventName = "intro_preparing_complete")
    }

    fun introPreparingFailed() {
        logEvent(eventName = "intro_preparing_failed")
    }

    fun introFirstPlayClick() {
        logEvent(eventName = "intro_first_play_click")
    }

    fun premiumShow(from: String, version: Int, tag: String? = null) {
        val params = Bundle().apply {
            putString("from", from)
            putInt("version", version)
            tag?.let { putString("tag", it) }
        }
        logEvent(eventName = "premium_show", params = params)
    }

    fun premiumBuySuccess(productId: String, from: String, version: Int, tag: String? = null) {
        val params = Bundle().apply {
            putString("product_id", productId)
            putString("from", from)
            putInt("version", version)
            tag?.let { putString("tag", it) }
        }
        logEvent(eventName = "premium_buy_success", params = params)
    }

    fun premiumBuyClick(productId: String, from: String, version: Int, tag: String? = null) {
        val params = Bundle().apply {
            putString("product_id", productId)
            putString("from", from)
            putInt("version", version)
            tag?.let { putString("tag", it) }
        }
        logEvent(eventName = "premium_buy_click", params = params)
    }

    fun premiumBack() {
        logEvent(eventName = "premium_back")
    }

    fun premiumCompareShow() {
        logEvent(eventName = "premium_compare_show")
    }

    fun premiumOfferShow() {
        logEvent(eventName = "premium_offer_show")
    }

    fun premiumOfferBack() {
        logEvent(eventName = "premium_offer_back")
    }

    fun premiumStartTrial() {
        logEvent(eventName = "premium_start_trial")
    }

    fun premiumActive() {
        logEvent(eventName = "premium_active")
    }

    fun premiumNotActive() {
        logEvent(eventName = "premium_not_active")
    }

    fun aboutPremiumShow() {
        logEvent(eventName = "about_premium_show")
    }

    fun aboutPremiumItemClick() {
        logEvent(eventName = "about_premium_item_click")
    }

    fun aboutPremiumShareClick() {
        logEvent(eventName = "about_premium_share_click")
    }

    fun settingsShow() {
        logEvent(eventName = "settings_show")
    }

    fun settingsFavoritesClick() {
        logEvent(eventName = "settings_favorites_click")
    }

    fun settingsDownloadsClick() {
        logEvent(eventName = "settings_downloads_click")
    }

    fun settingsHistoryClick() {
        logEvent(eventName = "settings_history_click")
    }

    fun settingsUaContentClick() {
        logEvent(eventName = "settings_ua_content_click")
    }

    fun settingsTerms() {
        logEvent(eventName = "settings_terms")
    }

    fun settingsPrivacy() {
        logEvent(eventName = "settings_privacy")
    }

    fun settingsCopyright() {
        logEvent(eventName = "settings_copyright")
    }

    fun settingsAboutApp() {
        logEvent(eventName = "settings_about_app")
    }

    fun settingsAboutVibes() {
        logEvent(eventName = "settings_about_vibes")
    }

    fun settingsChangeName() {
        logEvent(eventName = "settings_change_name")
    }

    fun settingsChangeEmail() {
        logEvent(eventName = "settings_change_email")
    }

    fun settingsChangeGender() {
        logEvent(eventName = "settings_change_gender")
    }

    fun settingsChangeLang() {
        logEvent(eventName = "settings_change_lang")
    }

    fun settingsNotificationsSettings() {
        logEvent(eventName = "settings_notifications_settings")
    }

    fun settingsSystemSettings() {
        logEvent(eventName = "settings_system_settings")
    }

    fun settingsPersonalData() {
        logEvent(eventName = "settings_personal_data")
    }

    fun settingsFacebookClick() {
        logEvent(eventName = "settings_facebook_click")
    }

    fun settingsInstagramClick() {
        logEvent(eventName = "settings_instagram_click")
    }

    fun settingsYoutubeClick() {
        logEvent(eventName = "settings_youtube_click")
    }

    fun settingsTwitterClick() {
        logEvent(eventName = "settings_twitter_click")
    }

    fun settingsTiktokClick() {
        logEvent(eventName = "settings_tiktok_click")
    }

    fun settingsFaq() {
        logEvent(eventName = "settings_faq")
    }

    fun settingsSupport() {
        logEvent(eventName = "settings_support")
    }

    fun settingsOncreateClick() {
        logEvent(eventName = "settings_oncreate_click")
    }

    fun settingsVersionClick() {
        logEvent(eventName = "settings_version_click")
    }

    fun settingsShare() {
        logEvent(eventName = "settings_share")
    }

    fun settingsFeedback() {
        logEvent(eventName = "settings_feedback")
    }

    fun settingsAboutSubscriptions() {
        logEvent(eventName = "settings_about_subscriptions")
    }

    fun clearAccountShow() {
        logEvent(eventName = "clear_account_show")
    }

    fun clearAccountClearClick() {
        logEvent(eventName = "clear_account_clear_click")
    }

    fun clearAccountClearFinished() {
        logEvent(eventName = "clear_account_clear_finished")
    }

    fun mainScreenShow() {
        logEvent(eventName = "main_screen_show")
    }

    fun uaMainScreenShow() {
        logEvent(eventName = "ua_main_screen_show")
    }

    fun tabTodayClick() {
        logEvent(eventName = "tab_today_click")
    }

    fun tabLibraryClick() {
        logEvent(eventName = "tab_library_click")
    }

    fun tabScenesClick() {
        logEvent(eventName = "tab_scenes_click")
    }

    fun tabExecisesClick() {
        logEvent(eventName = "tab_execises_click")
    }

    fun tabProfileClick() {
        logEvent(eventName = "tab_profile_click")
    }

    fun tabPremiumClick() {
        logEvent(eventName = "tab_premium_click")
    }

    fun todayShow() {
        logEvent(eventName = "today_show")
    }

    fun todayQuestionClick() {
        logEvent(eventName = "today_question_click")
    }

    fun todaySosClick() {
        logEvent(eventName = "today_sos_click")
    }

    fun todaySupportClick() {
        logEvent(eventName = "today_support_click")
    }

    fun todayItemTitleClick() {
        logEvent(eventName = "today_item_title_click")
    }

    fun todayItemClick() {
        logEvent(eventName = "today_item_click")
    }

    fun todayItemLongpress() {
        logEvent(eventName = "today_item_longpress")
    }

    fun todayMenuEndTrialClick() {
        logEvent(eventName = "today_menu_end_tial_click")
    }

    fun todayMenuDemoClick() {
        logEvent(eventName = "today_menu_demo_click")
    }

    fun todayMenuVideoClick() {
        logEvent(eventName = "today_menu_video_click")
    }

    fun todayMenuHowItWorksClick() {
        logEvent(eventName = "today_menu_how_it_works_click")
    }

    fun todayNextDayClick() {
        logEvent(eventName = "today_next_day_click")
    }

    fun todayPrevDayClick() {
        logEvent(eventName = "today_prev_day_click")
    }

    fun todayUpgradeClick() {
        logEvent(eventName = "today_upgrade_click")
    }

    fun todayDemoClick() {
        logEvent(eventName = "today_demo_click")
    }

    fun todaySession365Click() {
        logEvent(eventName = "today_session_365_click")
    }

    fun todayVideoClick() {
        logEvent(eventName = "today_video_click")
    }

    fun todayExtraClick() {
        logEvent(eventName = "today_extra_click")
    }

    fun todayProgramsClick() {
        logEvent(eventName = "today_programs_click")
    }

    fun todayScenesClick() {
        logEvent(eventName = "today_scenes_click")
    }

    fun todayBannerClick(bannerId: String) {
        val params = Bundle().apply {
            putString("banner_id", bannerId)
        }
        logEvent("today_banner_click", params)
    }

    fun todayProgramsItemClick(packageId: String) {
        val params = Bundle().apply {
            putString("package_id", packageId)
        }
        logEvent("today_programs_item_click", params)
    }

    fun todayTimeOfDayClick(timeOfDay: String) {
        logEvent(eventName = "today_${timeOfDay}_click")
    }

    fun todayDemoTimeOfDayClick(timeOfDay: String) {
        logEvent(eventName = "today_demo_${timeOfDay}_click")
    }

    fun audioSessionShow(
        timeOfDay: String,
        day: Int,
        accompanimentId: Int,
        avatarId: Int
    ) {
        val params = Bundle().apply {
            putInt("day", day)
            putInt("accompaniment_id", accompanimentId)
            putInt("avatar_id", avatarId)
        }
        logEvent("audio_session_${timeOfDay}_show", params)
    }

    fun audioSessionCompleted(
        timeOfDay: String,
        day: Int,
        accompanimentId: Int,
        avatarId: Int
    ) {
        val params = Bundle().apply {
            putInt("day", day)
            putInt("accompaniment_id", accompanimentId)
            putInt("avatar_id", avatarId)
        }
        logEvent("audio_session_${timeOfDay}_completed", params)
    }

    fun audioSessionPlayClick(timeOfDay: String, dayOffset: Int) {
        val params = Bundle().apply {
            putInt("day_offset", dayOffset)
        }
        logEvent("audio_session_${timeOfDay}_play_click", params)
    }

    fun audioSessionSeek(timeOfDay: String, dayOffset: Int) {
        val params = Bundle().apply {
            putInt("day_offset", dayOffset)
        }
        logEvent("audio_session_${timeOfDay}_seek", params)
    }

    fun audioSessionClose(timeOfDay: String, dayOffset: Int) {
        val params = Bundle().apply {
            putInt("day_offset", dayOffset)
        }
        logEvent("audio_session_${timeOfDay}_close", params)
    }

    fun audioSessionDemoShow(
        timeOfDay: String,
        day: Int,
        accompanimentId: Int,
        avatarId: Int
    ) {
        val params = Bundle().apply {
            putInt("day", day)
            putInt("accompaniment_id", accompanimentId)
            putInt("avatar_id", avatarId)
        }
        logEvent("audio_session_demo_${timeOfDay}_show", params)
    }

    fun audioSessionDemoCompleted(
        timeOfDay: String,
        day: Int,
        accompanimentId: Int,
        avatarId: Int
    ) {
        val params = Bundle().apply {
            putInt("day", day)
            putInt("accompaniment_id", accompanimentId)
            putInt("avatar_id", avatarId)
        }
        logEvent("audio_session_demo_${timeOfDay}_completed", params)
    }

    fun audioSessionDemoPlayClick(timeOfDay: String, dayOffset: Int) {
        val params = Bundle().apply {
            putInt("day_offset", dayOffset)
        }
        logEvent("audio_session_demo_${timeOfDay}_play_click", params)
    }

    fun audioSessionDemoSeek(timeOfDay: String, dayOffset: Int) {
        val params = Bundle().apply {
            putInt("day_offset", dayOffset)
        }
        logEvent("audio_session_demo_${timeOfDay}_seek", params)
    }

    fun audioSessionDemoClose(timeOfDay: String, dayOffset: Int) {
        val params = Bundle().apply {
            putInt("day_offset", dayOffset)
        }
        logEvent("audio_session_demo_${timeOfDay}_close", params)
    }

    fun audioSessionUpgradeClick() {
        logEvent(eventName = "audio_session_upgrade_click")
    }

    fun audioSessionRateClick() {
        logEvent(eventName = "audio_session_rate_click")
    }

    fun audioSessionShare() {
        logEvent(eventName = "audio_session_share")
    }

    fun audioSessionBannerClick(bannerLink: String, bannerImage: String) {
        val params = Bundle().apply {
            putString("banner_link", bannerLink)
            putString("banner_image", bannerImage)
        }
        logEvent(eventName = "audio_session_banner_click", params = params)
    }

    fun audioSessionAvatarsClick() {
        logEvent(eventName = "audio_session_avatars_click")
    }

    fun audioSessionAvatarModeDisableClick() {
        logEvent(eventName = "audio_session_avatar_mode_disable_click")
    }

    fun audioSessionAvatarModeRandomClick() {
        logEvent(eventName = "audio_session_avatar_mode_random_click")
    }

    fun audioSessionAvatarModeStaticClick() {
        logEvent(eventName = "audio_session_avatar_mode_static_click")
    }

    fun voiceAvatarsMenuEditClick() {
        logEvent(eventName = "voice_avatars_menu_edit_click")
    }

    fun voiceAvatarsMenuDeleteClick() {
        logEvent(eventName = "voice_avatars_menu_delete_click")
    }

    fun voiceAvatarsMenuRestoreClick() {
        logEvent(eventName = "voice_avatars_menu_restore_click")
    }

    fun voiceAvatarsMenuSelectAllClick() {
        logEvent(eventName = "voice_avatars_menu_select_all_click")
    }

    fun voiceAvatarsItemClick(avatarId: String) {
        val params = Bundle().apply {
            putString("avatar_id", avatarId)
        }
        logEvent(eventName = "voice_avatars_item_click", params = params)
    }

    fun voiceAvatarsAddClick() {
        logEvent(eventName = "voice_avatars_add_click")
    }

    fun voiceAvatarsCustomAdded() {
        logEvent(eventName = "voice_avatars_custom_added")
    }

    fun voiceMusicsSelect(voiceMusicId: Int) {
        val params = Bundle().apply {
            putInt("voice_music_id", voiceMusicId)
        }
        logEvent(eventName = "voice_musics_select", params = params)
    }

    fun demoPeriodShow() {
        logEvent(eventName = "demo_period_show")
    }

    fun demoPeriodTimeOfDayClick(timeOfDay: String, demoPeriodDay: Int) {
        val params = Bundle().apply {
            putInt("demo_period_day", demoPeriodDay)
        }
        logEvent(eventName = "demo_period_${timeOfDay}_click", params = params)
    }

    fun sosShow() {
        logEvent(eventName = "sos_show")
    }

    fun sosOption1Click() {
        logEvent(eventName = "sos_option_1_click")
    }

    fun sosOption2Click() {
        logEvent(eventName = "sos_option_2_click")
    }

    fun sosCallClick() {
        logEvent(eventName = "sos_call_click")
    }

    fun sosContactsShow() {
        logEvent(eventName = "sos_contacts_show")
    }

    fun sosContactsSearch() {
        logEvent(eventName = "sos_contacts_search")
    }

    fun sosContactClick() {
        logEvent(eventName = "sos_contact_click")
    }

    fun faqShow() {
        logEvent(eventName = "faq_show")
    }

    fun faqItemClick() {
        logEvent(eventName = "faq_item_click")
    }

    fun faqSupportClick() {
        logEvent(eventName = "faq_support_click")
    }


}