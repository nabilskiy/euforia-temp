package digital.euforia.app.domain.model.settings


import androidx.annotation.StringRes

data class SettingsItem(
    val settingType: SettingType,
    val isSoon: Boolean = false
)

data class SettingGroup(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val settingItems: List<SettingsItem>
)