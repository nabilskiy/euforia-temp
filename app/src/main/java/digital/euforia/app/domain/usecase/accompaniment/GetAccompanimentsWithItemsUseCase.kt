package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.AccompanimentItem
import digital.euforia.app.data.db.entity.AccompanimentWithItems
import digital.euforia.app.data.repository.AccompanimentItemRepository
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.util.ResultWrapper
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.listOf

class GetAccompanimentsWithItemsUseCase @Inject constructor(
    private val accompanimentRepository: AccompanimentRepository,
    private val accompanimentItemRepository: AccompanimentItemRepository,
    private val config: EuforiaRemoteConfigFetcher
) {
    suspend operator fun invoke(
        isDemo: Boolean
    ): ResultWrapper<List<AccompanimentWithItems>> {
        return withContext(Dispatchers.IO) {
            accompanimentRepository.getAccompanimentWithItems(isDemo)
        }
    }

    private suspend fun createItemsForAccompaniment(accompaniment: Accompaniment): List<AccompanimentItem> {
        val morningItem = AccompanimentItem(
            accompanimentId = accompaniment.id,
            timeOfDay = TimeOfDay.MORNING,
//            title = accompaniment
        )
        val dayTimeItem = AccompanimentItem(
            accompanimentId = accompaniment.id,
            timeOfDay = TimeOfDay.DAYTIME,
        )
        val eveningItem = AccompanimentItem(
            accompanimentId = accompaniment.id,
            timeOfDay = TimeOfDay.EVENING,
        )
        val items = listOf(morningItem, dayTimeItem, eveningItem)
        accompanimentItemRepository.insertAll(items)

        return items
    }
}


private fun sortItems(
    items: List<AccompanimentItem>,
    isToday: Boolean,
    currentTimeOfDay: TimeOfDay
): List<AccompanimentItem> {
    return if (!isToday) {
        items.sortedBy { it.timeOfDay }
    } else {
        // Sort by natural order of TimeOfDay, then move the currentTimeOfDay item to the first position
        val sorted = items.sortedBy { it.timeOfDay }
        val idx = sorted.indexOfFirst { it.timeOfDay == currentTimeOfDay }
        if (idx <= 0) {
            // either already first or not found; keep as is
            sorted
        } else {
            buildList(sorted.size) {
                add(sorted[idx])
                addAll(sorted.subList(0, idx))
                if (idx + 1 < sorted.size) addAll(sorted.subList(idx + 1, sorted.size))
            }
        }
    }
}