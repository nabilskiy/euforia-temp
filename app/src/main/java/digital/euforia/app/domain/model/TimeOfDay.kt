package digital.euforia.app.domain.model

import androidx.annotation.Keep
import digital.euforia.app.R

@Keep
enum class TimeOfDay { MORNING, DAYTIME, EVENING }

fun TimeOfDay.getLabelRes(): Int {
    return when (this) {
        TimeOfDay.MORNING -> R.string.vibes_morning_title
        TimeOfDay.DAYTIME -> R.string.vibes_daytime_title
        TimeOfDay.EVENING -> R.string.vibes_evening_title
    }
}