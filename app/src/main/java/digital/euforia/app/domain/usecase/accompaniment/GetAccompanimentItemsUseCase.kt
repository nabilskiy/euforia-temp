package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.AccompanimentItem
import digital.euforia.app.data.repository.AccompanimentItemRepository
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.defaultTimeOfDayConfig
import digital.euforia.app.ui.util.getCurrentTimeOfDay
import digital.euforia.app.ui.util.isToday
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import kotlin.collections.listOf

class GetAccompanimentItemsUseCase @Inject constructor(
    private val accompanimentRepository: AccompanimentRepository,
    private val accompanimentItemRepository: AccompanimentItemRepository,
    private val config: EuforiaRemoteConfigFetcher
) {
    suspend operator fun invoke(
        accompaniments: List<Accompaniment>,
        isDemo: Boolean,
        completedDays: Int
    ): List<List<AccompanimentItem>> {
        return withContext(Dispatchers.IO) {
            val timeOfDayConfig = config.getTimeOfDayConfig() ?: defaultTimeOfDayConfig()
            val morningBegin = timeOfDayConfig.morningBegin
//            val isToday = isToday(Instant.now(), morningBegin)
            val currentTimeOfDay = getCurrentTimeOfDay(timeOfDayConfig)

//            accompaniments.flatMapIndexed { index, accompaniment ->
//                val items = accompanimentItemRepository.getAllByAccompanimentFlow(accompaniment.id).flatMapLatest {
//
//                }
//
//            }

            accompanimentRepository.getAllWithItemsFlow().collectLatest {
                Timber.d("GetAccompanimentItemsUseCase: $it")
            }
            accompaniments.mapIndexed { index, accompaniment ->
                accompanimentItemRepository.getAllByAccompaniment(accompaniment.id).let {
                    it.ifEmpty { createItemsForAccompaniment(accompaniment) }
                }.run {
                    val isToday = if (isDemo) {
                        completedDays == index
                    } else {
                        //additional logic of sorting and calculation after demo
                        index == 0
                    }
                    sortItems(this, isToday, currentTimeOfDay)
                }
            }

//            val items =
//                accompanimentItemRepository.getAllByAccompanimentIds(accompaniments.map { it.id })
//
//
//
//            if (items.isNotEmpty()) {
//                items.groupBy { it.accompanimentId }.mapNotNull { entry ->
//                    sortItems(entry.value, isToday, currentTimeOfDay)
//                }
//            } else {
//                accompaniments.map {
//                    createItemsForAccompaniment(it)
//                }.map {
//                    sortItems(
//                        it,
//                        isToday,
//                        currentTimeOfDay
//                    )
//                }
//
//            }
//            accompanimentItemRepository.syncForAccompaniments(accompaniments)
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