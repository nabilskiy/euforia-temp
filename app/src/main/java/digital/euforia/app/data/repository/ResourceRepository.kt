package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.ResourceDao
import digital.euforia.app.data.db.entity.Resource
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_VOICE_AVATAR
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_VOICE_MUSIC
import digital.euforia.app.data.model.NetworkResource
import digital.euforia.app.data.model.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceRepository @Inject constructor(
    private val api: EuforiaApi,
    private val resourceDao: ResourceDao,
) {
    /**
     * Saves a list of NetworkResource into DB. This keeps API concerns outside
     * until an endpoint is defined. Call this after fetching resources from network.
     */
    suspend fun saveAllFromNetwork(items: List<NetworkResource>) = withContext(Dispatchers.IO) {
        val entities = items.map(NetworkResource::toEntity)
        resourceDao.insertAll(entities)
    }

    suspend fun syncAll() {
        val aliases = "$CLASS_ALIAS_VOICE_AVATAR,$CLASS_ALIAS_VOICE_MUSIC"
        val result = api.getResources(classAlias = aliases,)
        result.onSuccess { networkResources ->
            val entities = networkResources.map(NetworkResource::toEntity)
            // Keep database consistent with network
            resourceDao.deleteAllExcept(entities.map { it.id })
            resourceDao.insertAll(entities)
        }.onFailure {
            Timber.e("Failed to sync resources: ${it.message}")
        }
    }

    fun getAllFlow(): Flow<List<Resource>> = resourceDao.getAllFlow()

    fun getByIdFlow(id: Int): Flow<Resource?> = resourceDao.getByIdFlow(id)

    suspend fun getByClassAlias(alias: String): List<Resource> = resourceDao.getByClassAlias(alias)

    fun getByCategoryIdFlow(categoryId: Int): Flow<List<Resource>> =
        resourceDao.getByCategoryIdFlow(categoryId)

    suspend fun clearAll() = withContext(Dispatchers.IO) { resourceDao.clearAll() }
}
