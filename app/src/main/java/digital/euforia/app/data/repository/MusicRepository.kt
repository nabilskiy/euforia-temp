/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.MusicCategoryDao
import digital.euforia.app.data.db.dao.MusicDao
import digital.euforia.app.data.db.entity.MusicCategory as MusicCategoryEntity
import digital.euforia.app.data.model.NetworkMusic
import digital.euforia.app.data.model.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Singleton
import digital.euforia.app.data.db.entity.Music as MusicEntity

@Singleton
class MusicRepository(
    private val api: EuforiaApi,
    private val musicDao: MusicDao,
    private val musicCategoryDao: MusicCategoryDao,
) {
    suspend fun syncAll(): Unit = withContext(Dispatchers.IO) {
        syncCatalog()
    }

    suspend fun syncCatalog(): Unit = withContext(Dispatchers.IO) {
        val categoriesResult = api.getMusicCategories()
        val musicResult = api.music()

        categoriesResult.onSuccess { categories ->
            val entities = categories.mapIndexed { index, category -> category.toEntity(index) }
            if (entities.isNotEmpty()) {
                musicCategoryDao.upsertAll(entities)
                musicCategoryDao.deleteAllExcept(entities.map(MusicCategoryEntity::id))
            } else {
                musicCategoryDao.clear()
            }
        }.onFailure { e ->
            Timber.w(e, "Failed to fetch music categories")
        }

        musicResult.onSuccess { list ->
            Timber.d("Saving ${list.size} music items")
            val entities = list.map(NetworkMusic::toEntity)
            musicDao.insertAll(entities)
        }.onFailure { e ->
            Timber.w(e, "Failed to fetch music")
        }
    }

    fun getAllFlow(): Flow<List<MusicEntity>> = musicDao.getAllFlow()

    fun getByIdFlow(id: Int): Flow<MusicEntity?> = musicDao.getByIdFlow(id)

    fun getByCategoryIdFlow(categoryId: Int): Flow<List<MusicEntity>> =
        musicDao.getByCategoryIdFlow(categoryId)

    fun getAllCategoriesFlow(): Flow<List<MusicCategoryEntity>> = musicCategoryDao.getAllFlow()
}