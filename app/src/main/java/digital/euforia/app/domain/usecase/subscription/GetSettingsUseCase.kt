package digital.euforia.app.domain.usecase.subscription

import digital.euforia.app.R
import digital.euforia.app.domain.model.settings.SettingGroup
import digital.euforia.app.domain.model.settings.SettingType
import digital.euforia.app.domain.model.settings.SettingsItem
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor() {
    operator fun invoke(): List<SettingGroup> {
        return listOf(
            SettingGroup(
                titleRes = R.string.profile_more,
                subtitleRes = R.string.profile_more,
                settingItems = listOf(
                    SettingsItem(SettingType.DOWNLOADED),
                )
            ),
            SettingGroup(
                titleRes = R.string.profile_private_data,
                subtitleRes = R.string.profile_settings,
                settingItems = listOf(
                    SettingsItem(SettingType.NAME),
                    SettingsItem(SettingType.VOICE),
                    SettingsItem(SettingType.LANGUAGE),
                    SettingsItem(SettingType.NOTIFICATIONS),
                    SettingsItem(SettingType.EMAIL),
                    SettingsItem(SettingType.SUBSCRIPTIONS),
                    SettingsItem(SettingType.SYSTEM_SETTINGS),
                    SettingsItem(SettingType.PERSONAL_DATA),
                    SettingsItem(SettingType.SAVE_PROGRESS, true)
                )
            ),
            SettingGroup(
                titleRes = R.string.profile_about_app,
                subtitleRes = R.string.profile_info,
                settingItems = listOf(
                    SettingsItem(SettingType.USER_AGREEMENT),
                    SettingsItem(SettingType.PRIVACY_POLICY),
                    SettingsItem(SettingType.COPYRIGHT),
                    SettingsItem(SettingType.ABOUT)
                )
            ),
            SettingGroup(
                titleRes = R.string.profile_share,
                subtitleRes = R.string.profile_social_nets,
                settingItems = listOf(
                    SettingsItem(SettingType.SHARE_APP),
                    SettingsItem(SettingType.INSTAGRAM),
                    SettingsItem(SettingType.FACEBOOK),
                    SettingsItem(SettingType.YOUTUBE),
                    SettingsItem(SettingType.X),
                    SettingsItem(SettingType.TIKTOK)
                )
            ),
            SettingGroup(
                titleRes = R.string.profile_additionel_info,
                subtitleRes = R.string.profile_from_author,
                settingItems = listOf(
                    SettingsItem(SettingType.FAQ),
                    SettingsItem(SettingType.FEEDBACK),
                    SettingsItem(SettingType.SUPPORT)
                )
            )

        )
    }
}