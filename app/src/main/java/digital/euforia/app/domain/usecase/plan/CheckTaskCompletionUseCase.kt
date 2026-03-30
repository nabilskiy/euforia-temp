package digital.euforia.app.domain.usecase.plan

import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class CheckTaskCompletionUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
) {
    suspend operator fun invoke(): Flow<Int> {
        val isDemoFlow = profilePreferences.getIsDemoFlow()
        val isPremiumFlow = profilePreferences.getIsPremiumFlow()
        val completedDaysFlow = appPreferences.getCompletedDaysFlow()
        val dailyCompletedAccompanimentsCountFlow = appPreferences.getDailyCompletedAccompanimentsCountFlow()
        val dailyPlayedSecondsFlow = appPreferences.getDailyPlayedSecondsFlow()

        return combine(
            isDemoFlow,
            isPremiumFlow,
            completedDaysFlow,
            dailyCompletedAccompanimentsCountFlow,
            dailyPlayedSecondsFlow
        ) { isDemo, isPremium, completedDays, completedAccompanimentsCount, playedSeconds ->
            val isTodayAccompanimentCompleted = if (isDemo) {
                completedAccompanimentsCount >= 2
            } else {
                completedAccompanimentsCount >= 3
            }

            val result = computeStages(
                Params(
                    isPremium = isPremium,
                    isDemoPeriod = isDemo,
                    isTodayAccompanimentCompleted = isTodayAccompanimentCompleted,
                    todayPlayingDurationSeconds = playedSeconds
                )
            )

            result.completedStagesCount
        }.distinctUntilChanged()
    }

    data class Params(
        val isPremium: Boolean,
        val isDemoPeriod: Boolean,
        val isTodayAccompanimentCompleted: Boolean,
        val todayPlayingDurationSeconds: Long,
        val requiredPlayingDurationSeconds: Long = 10 * 60
    )

    data class Stage(
        val id: StageId,
        val isCompleted: Boolean
    )

    data class Result(
        val stages: List<Stage>,
        val completedStagesCount: Int,
        val progressAnchorIndex: Int
    )

    enum class StageId {
        PREMIUM_1,
        PREMIUM_2,
        PREMIUM_3,
        PREMIUM_4,
        FREE_1,
        FREE_2,
        FREE_3,
        FREE_4
    }

    fun computeStages(params: Params): Result {
        val stages = if (params.isPremium && !params.isDemoPeriod) {
            listOf(
                Stage(id = StageId.PREMIUM_1, isCompleted = true),
                Stage(id = StageId.PREMIUM_2, isCompleted = params.isTodayAccompanimentCompleted),
                Stage(
                    id = StageId.PREMIUM_3,
                    isCompleted = params.todayPlayingDurationSeconds >= params.requiredPlayingDurationSeconds
                ),
                Stage(id = StageId.PREMIUM_4, isCompleted = false)
            )
        } else {
            listOf(
                Stage(id = StageId.FREE_1, isCompleted = true),
                Stage(id = StageId.FREE_2, isCompleted = params.isPremium),
                Stage(id = StageId.FREE_3, isCompleted = !params.isDemoPeriod),
                Stage(id = StageId.FREE_4, isCompleted = false)
            )
        }

        val sortedStages = stages.sortedByDescending { it.isCompleted }
        val completedStagesCount = sortedStages.count { it.isCompleted }

        return Result(
            stages = sortedStages,
            completedStagesCount = completedStagesCount,
            progressAnchorIndex = completedStagesCount
        )
    }
}
