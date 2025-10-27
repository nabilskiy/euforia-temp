package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.AccompanimentDao
import digital.euforia.app.data.db.dao.AccompanimentItemDao
import digital.euforia.app.data.db.dao.FileDao
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.AccompanimentItem
import digital.euforia.app.data.db.entity.AccompanimentWithItems
import digital.euforia.app.domain.model.TimeOfDay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccompanimentRepository @Inject constructor(
    private val api: EuforiaApi,
    private val accompanimentDao: AccompanimentDao,
    private val fileDao: FileDao,
    private val accompanimentItemDao: AccompanimentItemDao
) {

    suspend fun syncAccompaniments(demo: Boolean) {
        //resync when demo completed
        api.getAccompanimentsPerWeek(demo).onSuccess { networkAccompaniments ->
            val entities = networkAccompaniments.map { it.toEntity() }
            if (entities.isNotEmpty()) {
                // Keep database consistent with network
                accompanimentDao.clearAll()
                val ids = entities.map { it.id }
                accompanimentDao.deleteAllExcept(ids)
                accompanimentDao.insertAll(entities)

                // For each accompaniment, ensure it has 3 items (one per TimeOfDay)
                entities.forEach { accompaniment ->
                    val existingItems = accompanimentItemDao.getAllByAccompaniment(accompaniment.id)
                    if (existingItems.isEmpty()) {
                        val items = listOf(
                            AccompanimentItem(accompanimentId = accompaniment.id, timeOfDay = TimeOfDay.MORNING),
                            AccompanimentItem(accompanimentId = accompaniment.id, timeOfDay = TimeOfDay.DAYTIME),
                            AccompanimentItem(accompanimentId = accompaniment.id, timeOfDay = TimeOfDay.EVENING),
                        )
                        accompanimentItemDao.insertAll(items)
                    }
                }
            }
        }
    }

    suspend fun getAllWithItemsFlow(): Flow<List<AccompanimentWithItems>> {
        return accompanimentDao.getAllWithItemsFlow()
    }

    suspend fun getAllFlow(isDemo: Boolean): Flow<List<Accompaniment>> {
        return accompanimentDao.getAllByDemo(isDemo)
    }

    suspend fun getTodayAccompaniments(demo: Boolean) =
        api.getTodayAccompaniments(demo)

    suspend fun getAccompanimentById(id: Int): Accompaniment? {
        return accompanimentDao.getById(id)
    }
}