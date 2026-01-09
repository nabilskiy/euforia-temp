package digital.euforia.app.domain.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import digital.euforia.app.R
import digital.euforia.app.ui.theme.daytimeColors
import digital.euforia.app.ui.theme.eveningColors
import digital.euforia.app.ui.theme.morningColors

@Keep
enum class TimeOfDay { MORNING, DAYTIME, EVENING }

fun TimeOfDay.getLabelRes(): Int {
    return when (this) {
        TimeOfDay.MORNING -> R.string.vibes_morning_title
        TimeOfDay.DAYTIME -> R.string.vibes_daytime_title
        TimeOfDay.EVENING -> R.string.vibes_evening_title
    }
}

fun TimeOfDay.getColors(): List<Color> {
    return when (this) {
        TimeOfDay.MORNING -> morningColors
        TimeOfDay.DAYTIME -> daytimeColors
        TimeOfDay.EVENING -> eveningColors
    }
}

fun TimeOfDay.toEventParam(): String {
    return this.name.lowercase()
}