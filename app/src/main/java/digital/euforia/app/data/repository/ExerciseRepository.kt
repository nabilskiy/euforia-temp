package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.ExerciseDao
import digital.euforia.app.data.db.entity.Exercise
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val api: EuforiaApi,
    private val exerciseDao: ExerciseDao,
    private val publicationInfoMapper: PublicationInfoMapper,
) {

    suspend fun getExerciseById(id: Int): ResultWrapper<Exercise> {
        return withContext(Dispatchers.IO) {
            val localExercise = exerciseDao.getById(id)
            if (localExercise != null) {
                ResultWrapper.Success(localExercise)
            } else {
                api.getExercise(id).map { networkExercise ->
                    networkExercise.toEntity().also {
                        exerciseDao.upsert(it)
                    }
//                    exerciseDao.upsert(networkExercise.toEntity())
//                    publicationInfoMapper.fromNetworkExercise(networkExercise)
                }
            }
        }
    }
}