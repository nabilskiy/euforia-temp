package digital.euforia.app.domain.model.plan

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import digital.euforia.app.R

data class DailyTask(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
)

fun demoDailyTasks() = listOf(
    DailyTask(
        titleRes = R.string.today_progress_free_1_title,
        descriptionRes = R.string.today_progress_free_1_text,
        iconRes = R.drawable.ic_checkbox_full
    ),
    DailyTask(
        titleRes = R.string.today_progress_free_2_title,
        descriptionRes = R.string.today_progress_free_2_text,
        iconRes = R.drawable.ic_checkbox_full
    ),
    DailyTask(
        titleRes = R.string.today_progress_free_3_title,
        descriptionRes = R.string.today_progress_free_3_text,
        iconRes = R.drawable.ic_checkbox_full
    ),
    DailyTask(
        titleRes = R.string.today_progress_free_4_title,
        descriptionRes = R.string.today_progress_free_4_text,
        iconRes = R.drawable.ic_checkbox_full
    ),
)

fun premiumDailyTasks() = listOf(
    DailyTask(
        titleRes = R.string.today_progress_premium_1_title,
        descriptionRes = R.string.today_progress_premium_1_text,
        iconRes = R.drawable.ic_checkbox_full
    ),
    DailyTask(
        titleRes = R.string.today_progress_premium_2_title,
        descriptionRes = R.string.today_progress_premium_2_text,
        iconRes = R.drawable.ic_checkbox_full
    ),
    DailyTask(
        titleRes = R.string.today_progress_premium_3_title,
        descriptionRes = R.string.today_progress_premium_3_text,
        iconRes = R.drawable.ic_checkbox_full
    ),
    DailyTask(
        titleRes = R.string.today_progress_premium_4_title,
        descriptionRes = R.string.today_progress_premium_4_text,
        iconRes = R.drawable.ic_checkbox_full
    ),
)
