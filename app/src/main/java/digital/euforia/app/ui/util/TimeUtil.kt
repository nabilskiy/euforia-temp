package digital.euforia.app.ui.util

import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.config.partOfDay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

const val WEEKS_IN_YEAR = 53
const val DAYS_IN_YEAR = 365
const val MONTH_IN_YEAR = 12
const val ONE_SECOND = 1000L
const val SECONDS_IN_HOUR = 3600
const val MINUTES_IN_HOUR = 60
const val MILLIS_IN_MINUTE = 60_000
const val MILLIS_IN_DAY = 86_400_000
private const val DELIMITER = ":"
private const val TIME_FORMAT = "%02d$DELIMITER%02d"
private const val TIME_FORMAT_SPACED = "%02d $DELIMITER %02d %s"
private const val AM = "AM"
private const val PM = "PM"
private const val HALF_DAY = 12
private const val TIMING_DIVIDER = 10
private const val PROJECT_DATE_FORMAT = "dd MMMM yyyy"


fun formatDateFromMillis(timeMillis: Long): String {
    val date = Date(timeMillis)
    val format = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    return format.format(date)
}


/**
 * Formats epoch millis to 24-hour time string in HH:mm (zero-padded) using the system default timezone.
 */
fun formatTime24h(timeMillis: Long): String {
    val zoned = Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault())
    val hour = zoned.hour
    val minute = zoned.minute
    return String.format(Locale.getDefault(), TIME_FORMAT, hour, minute)
}


/**
 * Перевіряє, чи дата входить у "сьогоднішній" день,
 * де день починається о 05:00 ранку.
 */
fun isToday(date: Instant, morningStartHour: Int): Boolean {
    val zone = ZoneId.systemDefault()
    val now = ZonedDateTime.now(zone)
    val currentHour = now.hour

    // Визначаємо початок поточного дня (05:00)
    var dayStart = now.truncatedTo(ChronoUnit.DAYS)
        .withHour(morningStartHour)

    // Якщо зараз 00:00–04:59 — вважаємо, що це ще попередній день
    if (currentHour < morningStartHour) {
        dayStart = dayStart.minusDays(1)
    }

    val dayEnd = dayStart.plusDays(1)
    val target = date.atZone(zone)

    return !target.isBefore(dayStart) && target.isBefore(dayEnd)
}

fun getCurrentTimeOfDay(timeOfDayConfig: TimeOfDayConfig) : TimeOfDay{
//return TimeOfDay.EVENING
    val currentHour = ZonedDateTime.now().hour
    return timeOfDayConfig.partOfDay(currentHour)
}

fun Long.toDateString(pattern: String = "dd.MM.yyyy"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return Instant.ofEpochSecond(this)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}