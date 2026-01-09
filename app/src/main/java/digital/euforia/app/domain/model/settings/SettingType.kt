package digital.euforia.app.domain.model.settings

import androidx.annotation.Keep
import digital.euforia.app.R

@Keep
enum class SettingType(val iconRes: Int, val titleRes: Int) {
    NAME(iconRes = R.drawable.ic_name, titleRes = R.string.profile_name),
    VOICE(iconRes = R.drawable.ic_voice, titleRes = R.string.profile_voice),
    LANGUAGE(iconRes = R.drawable.ic_language, titleRes = R.string.profile_language),
    NOTIFICATIONS(iconRes = R.drawable.ic_bell, titleRes = R.string.profile_notifications),
    EMAIL(iconRes = R.drawable.ic_email, titleRes = R.string.profile_email),
    SUBSCRIPTIONS(iconRes = R.drawable.ic_subscriptions, titleRes = R.string.profile_subscriptions),
    SYSTEM_SETTINGS(iconRes = R.drawable.ic_system_settings, titleRes = R.string.profile_system_settings),
    PERSONAL_DATA(iconRes = R.drawable.ic_personal_data, titleRes = R.string.profile_personal_data),
    SAVE_PROGRESS(iconRes = R.drawable.ic_save_progress, titleRes = R.string.profile_save_progress),
    USER_AGREEMENT(iconRes = R.drawable.ic_agreement, titleRes = R.string.profile_terms),
    PRIVACY_POLICY(iconRes = R.drawable.ic_shield, titleRes = R.string.profile_privacy),
    COPYRIGHT(iconRes = R.drawable.ic_copyright, titleRes = R.string.profile_copyright_notice),
    ABOUT(iconRes = R.drawable.ic_about_app, titleRes = R.string.profile_about_app),
    SHARE_APP(iconRes = R.drawable.ic_app_share, titleRes = R.string.profile_share_app),
    INSTAGRAM(iconRes = R.drawable.ic_instagram, titleRes = R.string.profile_instagram),
    FACEBOOK(iconRes = R.drawable.ic_fb, titleRes = R.string.profile_facebook),
    YOUTUBE(iconRes = R.drawable.ic_youtube, titleRes = R.string.profile_youtube),
    X(iconRes = R.drawable.ic_twitter, titleRes = R.string.profile_x),
    TIKTOK(iconRes = R.drawable.ic_tiktok, titleRes = R.string.profile_tiktok),
    FAQ(iconRes = R.drawable.ic_faq, titleRes = R.string.profile_faq),
    FEEDBACK(iconRes = R.drawable.ic_feedback, titleRes = R.string.profile_feedback),
    SUPPORT(iconRes = R.drawable.ic_support, titleRes = R.string.profile_support),
}