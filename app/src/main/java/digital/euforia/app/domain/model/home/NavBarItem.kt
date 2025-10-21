package digital.euforia.app.domain.model.home

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.HomeDestination

@Keep
enum class NavBarItem(
    val destination: HomeDestination, @DrawableRes val iconRes: Int
) {
    PLAN(destination = HomeDestination.Plan, iconRes = R.drawable.ic_nav_plan),
    PROGRAMS(destination = HomeDestination.Programs, iconRes = R.drawable.ic_nav_program),
    SOUNDSCAPES(destination = HomeDestination.Soundscapes, iconRes = R.drawable.ic_nav_soundscapes),
    SETTINGS(destination = HomeDestination.Settings, iconRes = R.drawable.ic_nav_settings),
    SETTINGS_MAX(destination = HomeDestination.Settings, iconRes = R.drawable.ic_nav_max);
}

val defaultNavBarItems = listOf(
    NavBarItem.PLAN,
    NavBarItem.PROGRAMS,
    NavBarItem.SOUNDSCAPES,
    NavBarItem.SETTINGS
)