package digital.euforia.app.data.repository

import digital.euforia.app.data.db.dao.ArticleDao
import digital.euforia.app.data.db.dao.ExerciseDao
import digital.euforia.app.data.db.dao.MeditationDao
import digital.euforia.app.data.db.dao.PackageDao
import digital.euforia.app.data.db.entity.PackageWithChildren
import digital.euforia.app.data.model.NetworkPackage
import digital.euforia.app.data.model.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageRepository @Inject constructor(
    private val packageDao: PackageDao,
    private val meditationDao: MeditationDao,
    private val exerciseDao: ExerciseDao,
    private val articleDao: ArticleDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun save(network: NetworkPackage) = withContext(ioDispatcher) {
        val pkgEntity = network.toEntity()
        packageDao.insert(pkgEntity)

        if (network.meditations.isNotEmpty()) {
            meditationDao.insertAll(network.meditations.map { it.toEntity() })
        }
        if (network.exercises.isNotEmpty()) {
            exerciseDao.insertAll(network.exercises.map { it.toEntity() })
        }
        if (network.articles.isNotEmpty()) {
            articleDao.insertAll(network.articles.map { it.toEntity() })
        }
    }

    fun getPackageWithChildrenFlow(id: Int): Flow<PackageWithChildren?> =
        packageDao.getWithChildrenFlow(id)
}
