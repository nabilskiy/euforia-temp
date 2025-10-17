package digital.euforia.app.domain.usecase.plan

import digital.euforia.app.data.store.AppPreferences
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class ComputeContinuousDaysUseCase @Inject constructor(
    private val appPreferences: AppPreferences
) {
    /**
     * Computes and updates the user's continuous daily launch streak.
     * - If the app is launched on consecutive days (yesterday -> today), increments the streak.
     * - If at least one day is missed (gap > 1 day), resets the streak to 0.
     * - If launched multiple times within the same day, the streak remains unchanged.
     * The last launch date is persisted via AppPreferences.
     *
     * @return the updated continuous days streak value
     */
    suspend operator fun invoke(): Int {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val lastLaunchInstant = appPreferences.getLastLaunchDate()
        var currentStreak = appPreferences.getContinuousDays()

        if (lastLaunchInstant == null) {
            // First launch: start streak from 1
            currentStreak = 1
            appPreferences.setContinuousDays(currentStreak)
            appPreferences.setLastLaunchDate()
            return currentStreak
        }

        val lastLaunchDate = lastLaunchInstant.atZone(zone).toLocalDate()
        val diffDays = ChronoUnit.DAYS.between(lastLaunchDate, today)

        val newStreak = when {
            diffDays < 0 -> {
                // Device time changed backwards; do not modify streak, just update last launch.
                currentStreak
            }
            diffDays == 0L -> {
                // Same day launch; keep streak as is
                currentStreak
            }
            diffDays == 1L -> {
                // Consecutive day; increment streak
                currentStreak + 1
            }
            else -> {
                // Missed at least one day; reset streak
                0
            }
        }

        if (newStreak != currentStreak) {
            appPreferences.setContinuousDays(newStreak)
        }
        // Update last launch date to now
        appPreferences.setLastLaunchDate()

        return newStreak
    }
}