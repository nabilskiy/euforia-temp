package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.ArticleDao
import digital.euforia.app.data.db.dao.ExerciseDao
import digital.euforia.app.data.db.dao.MeditationDao
import digital.euforia.app.data.db.dao.PackageDao
import digital.euforia.app.data.db.entity.Package.Companion.TOP_PACKAGES_IDS
import digital.euforia.app.data.db.entity.PackageWithChildren
import digital.euforia.app.data.db.entity.PackageWithMeditations
import digital.euforia.app.data.model.NetworkPackage
import digital.euforia.app.data.model.*
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.publication.PublicationType
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
    private val publicationInfoMapper: PublicationInfoMapper,
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
                    ?: api.getMeditations(ids = "$id").dataOrNull?.firstOrNull()?.toEntity()

                if (meditation != null) {
                    publicationInfoMapper.fromMeditation(meditation)
                } else null
            }

            PublicationType.EXERCISE -> {
                val exercise = exerciseDao.getById(id)
                    ?: api.getExercises(ids = "$id").dataOrNull?.firstOrNull()?.toEntity()
                if (exercise != null) {
                    publicationInfoMapper.fromExercise(exercise)
                } else null
            }

            PublicationType.ARTICLE -> {
                val article =
                    articleDao.getById(id) ?: api.getArticles(ids = "$id").dataOrNull?.firstOrNull()
                        ?.toEntity()
                if (article != null) {
                    publicationInfoMapper.fromArticle(article)
                } else null
            }
        }

        return if (publicationInfo != null) {
            ResultWrapper.Success(publicationInfo)
        } else {
            ResultWrapper.Failure(Exception("Publication not found"))
        }
    }

    suspend fun getSimilarPublications(
        publicationType: PublicationType,
        categoryId: Int?
    ): ResultWrapper<List<PublicationInfo>> {
        val publicationInfos = when (publicationType) {
            PublicationType.ARTICLE -> {
                articleDao.getAllByMainCategoryId(categoryId).map { article ->
                    publicationInfoMapper.fromArticle(article)
                }.ifEmpty {
                    val networkArticles = api.getArticles(categoryId = categoryId).dataOrNull
                    networkArticles?.let {
                        articleDao.upsertAll(it.map { networkArticle -> networkArticle.toEntity() })
                        networkArticles.map { publicationInfoMapper.fromNetworkArticle(it) }
                    }
                }
            }

            PublicationType.EXERCISE -> {
                exerciseDao.getAllByMainCategoryId(categoryId).map { exercise ->
                    publicationInfoMapper.fromExercise(exercise)
                }.ifEmpty {
                    val networkExercises = api.getExercises(categoryId = categoryId).dataOrNull
                    networkExercises?.let {
                        exerciseDao.upsertAll(it.map { networkExercise -> networkExercise.toEntity() })
                        networkExercises.map { publicationInfoMapper.fromNetworkExercise(it) }
                    }
                }
            }

            PublicationType.MEDITATION -> {
                meditationDao.getAllByMainCategoryId(categoryId).map { meditation ->
                    publicationInfoMapper.fromMeditation(meditation)
                }.ifEmpty {
                    val networkMeditations = api.getMeditations(categoryId = categoryId).dataOrNull
                    networkMeditations?.let {
                        meditationDao.upsertAll(it.map { networkMeditation -> networkMeditation.toEntity() })
                        networkMeditations.map { publicationInfoMapper.fromNetworkMeditation(it) }
                    }
                }
            }
        }

        return if (publicationInfos != null) {
            ResultWrapper.Success(publicationInfos)
        } else {
            ResultWrapper.Failure(Exception("Failed to load similar publications"))
        }
    }

    suspend fun getByIds(type: PublicationType, ids: List<Int>): ResultWrapper<List<PublicationInfo>> {
        val items = when (type) {
            PublicationType.MEDITATION -> {
                val meditations = meditationDao.getByIds(ids)
                meditations.map { publicationInfoMapper.fromMeditation(it) }
            }

            PublicationType.EXERCISE -> {
                val exercises = exerciseDao.getByIds(ids)
                exercises.map { publicationInfoMapper.fromExercise(it) }
            }

            PublicationType.ARTICLE -> {
                val articles = articleDao.getByIds(ids)
                articles.map { publicationInfoMapper.fromArticle(it) }
            }
        }

        return if (items.isNotEmpty()) {
            ResultWrapper.Success(items)
        } else {
            ResultWrapper.Failure(Exception("Items not found"))
        }
    }
}
