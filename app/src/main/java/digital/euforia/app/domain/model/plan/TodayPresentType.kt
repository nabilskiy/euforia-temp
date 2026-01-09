package digital.euforia.app.domain.model.plan

import androidx.annotation.Keep

@Keep
enum class TodayPresentType {
    NONE, AUTO
}

fun String.toTodayPresentType(): TodayPresentType {
    return when (this.uppercase()) {
        "none" -> TodayPresentType.NONE
        "auto" -> TodayPresentType.AUTO
        else -> TodayPresentType.NONE
    }
}