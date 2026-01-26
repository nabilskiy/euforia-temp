package digital.euforia.app.data.repository

import androidx.room.withTransaction
import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.AppDatabase
import digital.euforia.app.data.db.dao.ArticleDao
import digital.euforia.app.data.db.dao.ExerciseDao
import digital.euforia.app.data.db.dao.MeditationDao
import digital.euforia.app.data.db.dao.PackageDao
import digital.euforia.app.data.db.entity.Article
import digital.euforia.app.data.db.entity.Exercise
import digital.euforia.app.data.db.entity.Meditation
import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.data.db.entity.Package.Companion.TOP_PACKAGES_IDS
import digital.euforia.app.data.db.entity.PackageWithChildren
import digital.euforia.app.data.db.entity.PackageWithMeditations
import digital.euforia.app.data.model.NetworkPackage
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.model.PublicationInfo
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
    private val database: AppDatabase,
    private val packageDao: PackageDao,
    private val meditationDao: MeditationDao,
    private val exerciseDao: ExerciseDao,
    private val articleDao: ArticleDao,
    private val appPreferences: AppPreferences,
    private val publicationInfoMapper: PublicationInfoMapper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun save(network: NetworkPackage) = database.withTransaction {
        val pkgEntity = network.toEntity()
        packageDao.upsert(pkgEntity)

        val meditationIds = network.meditations.map { it.id }
        val existingMeditations = meditationDao.getByIds(meditationIds).associateBy { it.id }
        network.meditations
            .map { networkMeditation ->
                val existing = existingMeditations[networkMeditation.id]
                val relatedIds = (existing?.relatedPackageIds ?: emptyList()).toMutableList()
                if (pkgEntity.id !in relatedIds) {
                    relatedIds.add(pkgEntity.id)
                }
                networkMeditation.toEntity(pkgEntity.id).copy(relatedPackageIds = relatedIds)
            }
            .takeIf { it.isNotEmpty() }
            ?.let {
                meditationDao.upsertAll(it)
            }

        val exerciseIds = network.exercises.map { it.id }
        val existingExercises = exerciseDao.getByIds(exerciseIds).associateBy { it.id }
        network.exercises
            .map { networkExercise ->
                val existing = existingExercises[networkExercise.id]
                val relatedIds = (existing?.relatedPackageIds ?: emptyList()).toMutableList()
                if (pkgEntity.id !in relatedIds) {
                    relatedIds.add(pkgEntity.id)
                }
                networkExercise.toEntity(pkgEntity.id).copy(relatedPackageIds = relatedIds)
            }
            .takeIf { it.isNotEmpty() }
            ?.let {
                exerciseDao.upsertAll(it)
            }

        val articleIds = network.articles.map { it.id }
        val existingArticles = articleDao.getByIds(articleIds).associateBy { it.id }
        network.articles
            .map { networkArticle ->
                val existing = existingArticles[networkArticle.id]
                val relatedIds = (existing?.relatedPackageIds ?: emptyList()).toMutableList()
                if (pkgEntity.id !in relatedIds) {
                    relatedIds.add(pkgEntity.id)
                }
                networkArticle.toEntity(pkgEntity.id).copy(relatedPackageIds = relatedIds)
            }
            .takeIf { it.isNotEmpty() }
            ?.let {
                articleDao.upsertAll(it)
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
        return if (!shouldSync) {
            val packages = packageDao.getAllWithChildren()
            if (packages.isNotEmpty()) {
                ResultWrapper.Success(packages)
            } else {
                ResultWrapper.Failure(Exception("No packages found"))
            }
        } else {
            api.getAllPackages().map { networkPackages ->
                val packages = mutableListOf<Package>()
                val meditations = mutableListOf<Meditation>()
                val exercises = mutableListOf<Exercise>()
                val articles = mutableListOf<Article>()

                if (networkPackages.isNotEmpty()) {
                    appPreferences.setShouldSyncPackages(false)
                    val existingMeditations = meditationDao.getByIds(networkPackages.flatMap { it.meditations.map { m -> m.id } }).associateBy { it.id }
                    val existingExercises = exerciseDao.getByIds(networkPackages.flatMap { it.exercises.map { e -> e.id } }).associateBy { it.id }
                    val existingArticles = articleDao.getByIds(networkPackages.flatMap { it.articles.map { a -> a.id } }).associateBy { it.id }

                    networkPackages.map { networkPackage ->
                        networkPackage.toEntity().also { pkgEntity ->
                            packages.add(pkgEntity)
                        }
                        networkPackage.meditations.forEach { networkMeditation ->
                            val existing = existingMeditations[networkMeditation.id]
                            val relatedIds = (existing?.relatedPackageIds ?: emptyList()).toMutableList()
                            if (networkPackage.id !in relatedIds) {
                                relatedIds.add(networkPackage.id)
                            }
                            meditations.add(networkMeditation.toEntity(networkPackage.id).copy(relatedPackageIds = relatedIds))
                        }
                        networkPackage.exercises.forEach { networkExercise ->
                            val existing = existingExercises[networkExercise.id]
                            val relatedIds = (existing?.relatedPackageIds ?: emptyList()).toMutableList()
                            if (networkPackage.id !in relatedIds) {
                                relatedIds.add(networkPackage.id)
                            }
                            exercises.add(networkExercise.toEntity(networkPackage.id).copy(relatedPackageIds = relatedIds))
                        }
                        networkPackage.articles.forEach { networkArticle ->
                            val existing = existingArticles[networkArticle.id]
                            val relatedIds = (existing?.relatedPackageIds ?: emptyList()).toMutableList()
                            if (networkPackage.id !in relatedIds) {
                                relatedIds.add(networkPackage.id)
                            }
                            articles.add(networkArticle.toEntity(networkPackage.id).copy(relatedPackageIds = relatedIds))
                        }
                    }
                    database.withTransaction {
                        packageDao.upsertAll(packages)
                        if (meditations.isNotEmpty()) {
                            meditationDao.upsertAll(meditations)
                            meditationDao.deleteNotIn(meditations.map { it.id })
                        }
                        if (exercises.isNotEmpty()) {
                            exerciseDao.upsertAll(exercises)
                            exerciseDao.deleteNotIn(exercises.map { it.id })
                        }
                        if (articles.isNotEmpty()) {
                            articleDao.upsertAll(articles)
                            articleDao.deleteNotIn(articles.map { it.id })
                        }
                        packageDao.getAllWithChildren()
                    }
                } else {
                    Timber.d("No packages from API")
                    ResultWrapper.Failure(Exception("No packages from API"))
                    emptyList()
                }
            }
        }
    }

    suspend fun getProgramWithChildrenById(id: Int): ResultWrapper<PackageWithChildren?> {
        val pkg = packageDao.getWithRelatedChildrenById(id)
        val hasChildren = pkg != null && (
                pkg.meditations.isNotEmpty() ||
                        pkg.exercises.isNotEmpty() ||
                        pkg.articles.isNotEmpty()
                )

        if (hasChildren) {
            return ResultWrapper.Success(pkg)
        } else {
            val pkgResult = api.getPackages(id = id.toString())
            return pkgResult.map { networkPackage ->
                if (networkPackage != null) {
                    save(networkPackage)
                    packageDao.getWithChildrenById(id)
                } else {
                    pkg // Return what we had if API returned null
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
                    ?: api.getMeditations(ids = "$id").dataOrNull?.firstOrNull()?.let {
                        it.toEntity(it.mainPackageId)
                    }

                if (meditation != null) {
                    publicationInfoMapper.fromMeditation(meditation)
                } else null
            }

            PublicationType.EXERCISE -> {
                val exercise = exerciseDao.getById(id)
                    ?: api.getExercises(ids = "$id").dataOrNull?.firstOrNull()?.let {
                        it.toEntity(it.mainPackageId)
                    }
                if (exercise != null) {
                    publicationInfoMapper.fromExercise(exercise)
                } else null
            }

            PublicationType.ARTICLE -> {
                val article =
                    articleDao.getById(id)
                        ?: api.getArticles(ids = "$id").dataOrNull?.firstOrNull()
                            ?.let { it.toEntity(it.mainPackageId) }
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
                        articleDao.upsertAll(it.map { networkArticle ->
                            networkArticle.toEntity(networkArticle.mainPackageId)
                        })
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
                        exerciseDao.upsertAll(it.map { networkExercise ->
                            networkExercise.toEntity(networkExercise.mainPackageId)
                        })
                        networkExercises.map { publicationInfoMapper.fromNetworkExercise(it) }
                    }
                }
            }

            PublicationType.MEDITATION -> {
                meditationDao.getAllByMainCategoryId(categoryId).map { meditation ->
                    publicationInfoMapper.fromMeditation(meditation)
                }.ifEmpty {
                    val networkMeditations =
                        api.getMeditations(categoryId = categoryId).dataOrNull
                    networkMeditations?.let {
                        meditationDao.upsertAll(it.map { networkMeditation ->
                            networkMeditation.toEntity(networkMeditation.mainPackageId)
                        })
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

    suspend fun getByIds(
        type: PublicationType,
        ids: List<Int>
    ): ResultWrapper<List<PublicationInfo>> {
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

    suspend fun getById(id: Int): ResultWrapper<Package> {
        val localPackage = packageDao.getById(id)
        if (localPackage != null) {
            return ResultWrapper.Success(localPackage)
        }
        return api.getPackage(id).map { networkPackage ->
            networkPackage.toEntity().also { pkgEntity ->
                packageDao.upsert(pkgEntity)
            }
        }
    }
}
