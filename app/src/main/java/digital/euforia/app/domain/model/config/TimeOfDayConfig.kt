package digital.euforia.app.domain.model.config

import digital.euforia.app.domain.model.TimeOfDay

data class TimeOfDayConfig(
    val morningBegin: Int,
    val morningNotification: Int,
    val daytimeBegin: Int,
    val daytimeNotification: Int,
    val eveningBegin: Int,
    val eveningNotification: Int
)

fun defaultTimeOfDayConfig(): TimeOfDayConfig = TimeOfDayConfig(
    morningBegin = 5,
    morningNotification = 8,
    daytimeBegin = 12,
    daytimeNotification = 14,
    eveningBegin = 19,
    eveningNotification = 20
)
/**
 * Returns true if [hour] is within the morning range.
 */
fun TimeOfDayConfig.isMorningRange(hour: Int): Boolean =
    hour in morningBegin until daytimeBegin

/**
 * Returns true if [hour] is within the daytime range.
 */
fun TimeOfDayConfig.isDaytimeRange(hour: Int): Boolean =
    hour in daytimeBegin until eveningBegin

/**
 * Returns true if [hour] is within the evening range.
 * If evening crosses midnight (e.g. eveningBegin = 18, morningBegin = 5),
 * this handles wrap-around correctly.
 */
fun TimeOfDayConfig.isEveningRange(hour: Int): Boolean =
    if (eveningBegin < morningBegin) {
        // e.g. evening = 18..23 or 0..4
        hour >= eveningBegin || hour < morningBegin
    } else {
        hour in eveningBegin..23 || hour in 0 until morningBegin
    }

/**
 * Convenience wrapper that returns which part of the day a given hour belongs to.
 */
fun TimeOfDayConfig.partOfDay(hour: Int): TimeOfDay = when {
    isMorningRange(hour) -> TimeOfDay.MORNING
    isDaytimeRange(hour) -> TimeOfDay.DAYTIME
    isEveningRange(hour) -> TimeOfDay.EVENING
    else -> TimeOfDay.MORNING
}

