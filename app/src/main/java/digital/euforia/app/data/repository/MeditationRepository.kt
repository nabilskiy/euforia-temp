package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.MeditationDao
import digital.euforia.app.data.db.entity.Meditation
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeditationRepository @Inject constructor(
    private val api: EuforiaApi,
    private val meditationDao: MeditationDao,
) {

    suspend fun getById(id: Int): ResultWrapper<Meditation> {
        return withContext(Dispatchers.IO) {
            val localMeditation = meditationDao.getById(id)
            if (localMeditation != null) {
                ResultWrapper.Success(localMeditation)
            } else {
                api.getMeditation(id).map { networkMeditation ->
                    networkMeditation.toEntity().also {
                        meditationDao.upsert(it)
                    }
                }
            }
        }
    }
}