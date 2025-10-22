package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.ArticleDao
import digital.euforia.app.data.db.dao.ExerciseDao
import digital.euforia.app.data.db.dao.MeditationDao
import digital.euforia.app.data.db.dao.PackageDao
import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.data.db.entity.Package.Companion.TOP_PACKAGES_IDS
import digital.euforia.app.data.db.entity.PackageWithChildren
import digital.euforia.app.data.db.entity.PackageWithMeditations
import digital.euforia.app.data.model.NetworkPackage
import digital.euforia.app.data.model.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageRepository @Inject constructor(
    private val api: EuforiaApi,
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

    fun observePackageWithChildren(id: Int): Flow<PackageWithChildren?> =
        packageDao.getWithChildrenFlow(id)

    suspend fun syncTopPackages(): Unit = withContext(ioDispatcher) {
        Package.TOP_PACKAGES_IDS.forEach {
            val packagesResult = api.getPackages(id = it)
            packagesResult.onSuccess { networkPackage ->
                Timber.d("Syncing top packages: $networkPackage")
                save(networkPackage)
            }.onFailure {
                Timber.d("Failed to sync top packages: ${it.message}")
            }
        }
    }

    suspend fun getTopPackagesFlow(): Flow<List<PackageWithMeditations>> {
        return packageDao.getWithMeditationsByIdFlow(TOP_PACKAGES_IDS)
    }
}
