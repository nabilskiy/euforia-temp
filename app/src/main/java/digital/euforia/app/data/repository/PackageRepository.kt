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
import digital.euforia.app.data.model.*
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.material.PublicationType
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
    private val appPreferences: AppPreferences,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun save(network: NetworkPackage) = withContext(ioDispatcher) {
        val pkgEntity = network.toEntity()
        packageDao.insert(pkgEntity)

        if (network.meditations.isNotEmpty()) {
            meditationDao.upsertAll(network.meditations.map { it.toEntity() })
        }
        if (network.exercises.isNotEmpty()) {
            exerciseDao.upsertAll(network.exercises.map { it.toEntity() })
        }
        if (network.articles.isNotEmpty()) {
            articleDao.upsertAll(network.articles.map { it.toEntity() })
        }
    }

    fun observePackageWithChildren(id: Int): Flow<PackageWithChildren?> =
        packageDao.getWithChildrenFlow(id)


    suspend fun syncPackages(ids: List<String>): Unit = withContext(ioDispatcher) {
        ids.forEach {
            val packagesResult = api.getPackages(id = it)
            packagesResult.onSuccess { networkPackage ->
                Timber.d("Syncing top packages: $networkPackage")
                if (networkPackage != null) save(networkPackage)
            }.onFailure {
                Timber.d("Failed to sync top packages: ${it.message}")
            }
        }
    }

    suspend fun getTopPackagesFlow(): Flow<List<PackageWithMeditations>> {
        return packageDao.getWithMeditationsByIdFlow(TOP_PACKAGES_IDS)
    }

    suspend fun getPackageByIdFlow(id: String): Flow<PackageWithMeditations?> {
        return packageDao.getWithMeditationsByIdFlow(id)
    }

    suspend fun getAllPackagesWithChildren(): ResultWrapper<List<PackageWithChildren>> {
        val shouldSync = appPreferences.getShouldSyncPackages()
        return if (shouldSync) {
            val pkgResult = api.getAllPackages()
            pkgResult.map { networkPackages ->
                val packages = if (networkPackages.isNotEmpty()) {
                    networkPackages.forEach { networkPackage -> save(networkPackage) }
                    appPreferences.setShouldSyncPackages(false)
                    packageDao.getAllWithChildren()
                } else {
                    emptyList()
                }
                Timber.d("Synced packages: ${packages.size}")
                packages
            }
        } else {
            val packages = packageDao.getAllWithChildren()
            ResultWrapper.Success(packages)
        }
    }

    suspend fun getProgramWithChildrenById(id: Int): ResultWrapper<PackageWithChildren?> {
        val pkg = packageDao.getWithChildrenById(id)
        if (pkg != null) {
            return ResultWrapper.Success(pkg)
        } else {
            val pkgResult = api.getPackages(id = id.toString())
            return pkgResult.map { networkPackage ->
                if (networkPackage != null) {
                    save(networkPackage)
                    packageDao.getWithChildrenById(id)
                } else {
                    null
                }
            }
        }
    }

    suspend fun getPublicationByIdAndType(
        id: Int,
        publicationType: PublicationType,
    ): ResultWrapper<PublicationInfo> {
        val publicationInfo = when (publicationType) {
            PublicationType.MEDITATION -> {
                val meditation = meditationDao.getById(id)
                if (meditation != null) {
                    PublicationInfo(
                        id = meditation.id,
                        isPremium = meditation.pro,
                        publicationType = PublicationType.MEDITATION,
                        title = meditation.name,
                        subtitle = meditation.subtitle,
                        imageUrl = meditation.imageUrl,
                        color1 = meditation.color1,
                        color2 = meditation.color2,
                        color3 = meditation.color3,
                        publishedAt = meditation.publishedAt,
                        durationMinutes = meditation.computeDurationMinutes(),
                    )
                } else null
            }

            PublicationType.EXERCISE -> {
                val exercise = exerciseDao.getById(id)
                if (exercise != null) {
                    PublicationInfo(
                        id = exercise.id,
                        isPremium = exercise.pro,
                        publicationType = PublicationType.EXERCISE,
                        title = exercise.name,
                        subtitle = exercise.subtitle,
                        imageUrl = exercise.imageUrl,
                        color1 = exercise.color1,
                        color2 = exercise.color2,
                        color3 = exercise.color3,
                        publishedAt = exercise.publishedAt,
                        durationMinutes = exercise.computeDurationMinutes(),
                    )
                } else null
            }

            PublicationType.ARTICLE -> {
                val article = articleDao.getById(id)
                if (article != null) {
                    PublicationInfo(
                        id = article.id,
                        isPremium = article.pro,
                        publicationType = PublicationType.ARTICLE,
                        title = article.name,
                        subtitle = article.subtitle,
                        imageUrl = article.imageUrl,
                        color1 = article.color1,
                        color2 = article.color2,
                        color3 = article.color3,
                        publishedAt = article.publishedAt,
                        durationMinutes = article.computeDurationMinutes(),
                    )
                } else null
            }
        }

        return if (publicationInfo != null) {
            ResultWrapper.Success(publicationInfo)
        } else {
            ResultWrapper.Failure(Exception("Publication not found"))
        }
    }
}
