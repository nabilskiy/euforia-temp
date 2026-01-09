package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.AppSettingsDao
import digital.euforia.app.data.db.entity.AppSettings
import digital.euforia.app.data.model.NetworkSettings
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppSettingsRepository @Inject constructor(
    private val api: EuforiaApi,
    private val dao: AppSettingsDao
) {

    fun observe(): Flow<AppSettings?> = dao.observe()

    suspend fun get(): ResultWrapper<AppSettings?> {
        val result = api.settings()
        return result.map {
            val settings = it.toEntity()
            settings.also {
                dao.insert(settings)
            }
        }
    }

    suspend fun save(entity: AppSettings) = dao.insert(entity)

    suspend fun saveFromNetwork(model: NetworkSettings) = dao.insert(model.toEntity())

    suspend fun sync() {
        val result = api.settings()
        result.onSuccess { net ->
            dao.insert(net.toEntity())
        }
    }

    suspend fun clear() = dao.clear()
}
