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
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class AccompanimentRepository @Inject constructor(
    private val api: EuforiaApi,
    private val accompanimentDao: AccompanimentDao,
    private val fileDao: FileDao,
    private val accompanimentItemDao: AccompanimentItemDao
) {

    suspend fun syncAccompaniments(demo: Boolean): ResultWrapper<Unit> {
        //resync when demo completed
//        val keepDemo = !demo
        return api.getAccompanimentsPerWeek(demo).map { networkAccompaniments ->
            val entities = networkAccompaniments.map { it.toEntity() }
            if (entities.isNotEmpty()) {
                val ids = entities.map { it.id }
                Timber.tag("ACC_SYNC").d("$ids")
                accompanimentDao.deleteAllExcept(ids, true)
                accompanimentDao.upsertAll(entities)

                // For each accompaniment, ensure it has 3 items (one per TimeOfDay)
                entities.forEach { accompaniment ->
                    val existingItems = accompanimentItemDao.getAllByAccompaniment(accompaniment.id)
                    Timber.tag("ACC_SYNC")
                        .d("Accompaniment ${accompaniment.id} has items: $existingItems")
                    if (existingItems.isEmpty()) {
                        val items = listOf(
                            AccompanimentItem(
                                accompanimentId = accompaniment.id,
                                timeOfDay = TimeOfDay.MORNING
                            ),
                            AccompanimentItem(
                                accompanimentId = accompaniment.id,
                                timeOfDay = TimeOfDay.DAYTIME
                            ),
                            AccompanimentItem(
                                accompanimentId = accompaniment.id,
                                timeOfDay = TimeOfDay.EVENING
                            ),
                        )
                        Timber.tag("ACC_SYNC")
                            .d("Inserting items for accompaniment ${accompaniment.id}")
                        accompanimentItemDao.insertAll(items)
                    }
                }
            }
        }
    }

    suspend fun getAccompanimentWithItems(isDemo: Boolean): ResultWrapper<List<AccompanimentWithItems>> {
        val result = api.getAccompanimentsPerWeek(isDemo)
        return result.map { networkAccompaniments ->
            val entities = networkAccompaniments.map { it.toEntity() }
            val ids = entities.map { it.id }
            Timber.tag("ACC_SYNC").d("$ids")
            accompanimentDao.deleteAllExcept(ids, true)
            accompanimentDao.upsertAll(entities)

            // For each accompaniment, ensure it has 3 items (one per TimeOfDay)
            entities.forEach { accompaniment ->
                val existingItems = accompanimentItemDao.getAllByAccompaniment(accompaniment.id)
                Timber.tag("ACC_SYNC")
                    .d("Accompaniment ${accompaniment.id} has items: $existingItems")
                if (existingItems.isEmpty()) {
                    val items = listOf(
                        AccompanimentItem(
                            accompanimentId = accompaniment.id,
                            timeOfDay = TimeOfDay.MORNING
                        ),
                        AccompanimentItem(
                            accompanimentId = accompaniment.id,
                            timeOfDay = TimeOfDay.DAYTIME
                        ),
                        AccompanimentItem(
                            accompanimentId = accompaniment.id,
                            timeOfDay = TimeOfDay.EVENING
                        ),
                    )
                    Timber.tag("ACC_SYNC")
                        .d("Inserting items for accompaniment ${accompaniment.id}")
                    accompanimentItemDao.insertAll(items)
                }
            }
//            entities.forEach { accompaniment ->
//                val existingItems = accompanimentItemDao.getAllByAccompaniment(accompaniment.id)
//                if (existingItems.isEmpty()) {
//                    val items = listOf(
//                        AccompanimentItem(accompanimentId = accompaniment.id, timeOfDay = TimeOfDay.MORNING),
//                        AccompanimentItem(accompanimentId = accompaniment.id, timeOfDay = TimeOfDay.DAYTIME),
//                        AccompanimentItem(accompanimentId = accompaniment.id, timeOfDay = TimeOfDay.EVENING),
//                    )
//                    accompanimentItemDao.insertAll(items)
//                }
//            }
            entities.map { accompaniment ->
                accompanimentDao.getWithItems(accompaniment.id)!!
            }
        }
    }

    suspend fun getAllWithItemsFlow(isDemo: Boolean): Flow<List<AccompanimentWithItems>> {
        return accompanimentDao.getAllWithItemsFlow(isDemo)
    }

    suspend fun getDemoWithItemsFlow(): Flow<List<AccompanimentWithItems>> {
        return accompanimentDao.getDemoWithItemsFlow()
    }

    suspend fun getAllFlow(isDemo: Boolean): Flow<List<Accompaniment>> {
        return accompanimentDao.getAllByDemo(isDemo)
    }

    suspend fun getTodayAccompaniments(demo: Boolean) =
        api.getTodayAccompaniments(demo)

    suspend fun getAccompanimentById(id: Int): Accompaniment? {
        return accompanimentDao.getById(id)
    }

    suspend fun getAccompanimentWithItemsById(id: Int): AccompanimentWithItems? {
        return accompanimentDao.getWithItems(id)
    }

    // Returns the number of accompaniments for which all related items are completed
    suspend fun getCompletedAccompanimentsCountFlow(): Flow<Int> {
        return accompanimentDao.getCompletedAccompanimentsCountFlow()
    }

    suspend fun getCompletedAccompanimentsCount(): Int {
        return withContext(Dispatchers.IO) { accompanimentDao.getCompletedAccompanimentsCount() }
    }
}