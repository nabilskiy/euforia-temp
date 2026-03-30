package digital.euforia.app.data.repository

import digital.euforia.app.data.db.dao.AccompanimentItemDao
import digital.euforia.app.data.db.entity.AccompanimentItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccompanimentItemRepository @Inject constructor(
    private val accompanimentItemDao: AccompanimentItemDao
) {
    suspend fun insert(item: AccompanimentItem): Long =
        accompanimentItemDao.insert(item)

    suspend fun insertAll(items: List<AccompanimentItem>) =
        accompanimentItemDao.insertAll(items)

    suspend fun update(item: AccompanimentItem) =
        accompanimentItemDao.update(item)

    suspend fun updateRatingById(id: Int, isRated: Boolean, rating: Int) =
        accompanimentItemDao.updateRatingById(id, isRated, rating)

    suspend fun getById(id: Int): AccompanimentItem? =
        accompanimentItemDao.getById(id)

    fun getAll(): Flow<List<AccompanimentItem>> =
        accompanimentItemDao.getAll()

    fun getAllByAccompaniment(accompanimentId: Int): List<AccompanimentItem> =
        accompanimentItemDao.getAllByAccompaniment(accompanimentId)

    fun getAllByAccompanimentFlow(accompanimentId: Int): Flow<List<AccompanimentItem>> =
        accompanimentItemDao.getAllByAccompanimentFlow(accompanimentId)

    fun getAllByAccompanimentIdsFlow(accompanimentIds: List<Int>): Flow<List<AccompanimentItem>> =
        accompanimentItemDao.getAllByAccompanimentIdsFlow(accompanimentIds)

    fun getAllByAccompanimentIds(accompanimentIds: List<Int>): List<AccompanimentItem> =
        accompanimentItemDao.getAllByAccompanimentIds(accompanimentIds)

    suspend fun clearAll() = accompanimentItemDao.clearAll()
}