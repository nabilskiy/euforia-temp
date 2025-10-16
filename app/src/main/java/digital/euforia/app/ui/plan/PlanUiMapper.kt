package digital.euforia.app.ui.plan

import digital.euforia.app.data.db.entity.AccompanimentWithItems
import digital.euforia.app.domain.model.TimeOfDay

/**
 * Pure mapping helpers to transform domain data to UI models for the Plan screen.
 * Keeping these outside of the ViewModel improves readability and testability.
 */

internal fun AccompanimentWithItems.toDayUi(
    isToday: Boolean,
    lockState: DayUi.LockState,
    timeOfDay: TimeOfDay,
): DayUi {
    val mapped = items.map { item ->
        val state = if (lockState != DayUi.LockState.UNLOCKED) {
            DayTimeItemUi.State.LOCKED
        } else if (isToday) {
            when {
                item.isCompleted -> DayTimeItemUi.State.COMPLETED
                item.timeOfDay <= timeOfDay -> DayTimeItemUi.State.AVAILABLE
                else -> DayTimeItemUi.State.SCHEDULED
            }
        } else {
            if (item.isCompleted) DayTimeItemUi.State.COMPLETED else DayTimeItemUi.State.LOCKED
        }
        DayTimeItemUi(item = item, state = state)
    }

    val ordered = if (isToday) {
        val sorted = mapped.sortedBy { it.item.timeOfDay }
        val idx = sorted.indexOfFirst { it.item.timeOfDay == timeOfDay }
        if (idx <= 0) sorted else buildList(sorted.size) {
            add(sorted[idx])
            addAll(sorted.subList(0, idx))
            if (idx + 1 < sorted.size) addAll(sorted.subList(idx + 1, sorted.size))
        }
    } else mapped

//    val title = if (isDemo)
    return DayUi(
        accompaniment = accompaniment,
        items = ordered,
        isToday = isToday,
        lockState = lockState,

    )
}

internal fun computeIsToday(
    index: Int,
    isDemo: Boolean,
    completedDays: Int,
    todayOffsetBefore: Int,
): Boolean = if (isDemo) completedDays == index else index == todayOffsetBefore

internal fun computeLockState(
    index: Int,
    isDemo: Boolean,
    isPremium: Boolean,
    completedDays: Int,
    freeDemoDays: Int,
    todayOffsetBefore: Int,
): DayUi.LockState {
    return if (isDemo) {
        if (isPremium) {
            if (index > completedDays) DayUi.LockState.LOCKED_BY_PREV_DAY else DayUi.LockState.UNLOCKED
        } else {
            when {
                index >= freeDemoDays -> DayUi.LockState.LOCKED_BY_PREMIUM
                index > completedDays -> DayUi.LockState.LOCKED_BY_PREV_DAY
                else -> DayUi.LockState.UNLOCKED
            }
        }
    } else {
        if (isPremium) {
            if (index > todayOffsetBefore) DayUi.LockState.LOCKED_BY_PREV_DAY else DayUi.LockState.UNLOCKED
        } else {
            DayUi.LockState.LOCKED_BY_PREMIUM
        }
    }
}
