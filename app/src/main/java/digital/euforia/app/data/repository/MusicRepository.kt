package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.MusicDao
import digital.euforia.app.data.model.NetworkMusic
import digital.euforia.app.data.model.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import digital.euforia.app.data.db.entity.Music as MusicEntity

@Singleton
class MusicRepository @Inject constructor(
    private val api: EuforiaApi,
    private val musicDao: MusicDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun syncAll(): Unit = withContext(ioDispatcher) {
        val result = api.music()
        result.onSuccess { list ->
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
}