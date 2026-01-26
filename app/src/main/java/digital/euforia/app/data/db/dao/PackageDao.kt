package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import digital.euforia.app.data.db.entity.Article
import digital.euforia.app.data.db.entity.Exercise
import digital.euforia.app.data.db.entity.Meditation
import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.data.db.entity.PackageWithChildren
import digital.euforia.app.data.db.entity.PackageWithMeditations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
abstract class PackageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(pkg: Package)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<Package>)

    @Upsert
    abstract suspend fun upsert(pkg: Package)

    @Upsert
    abstract suspend fun upsertAll(items: List<Package>)

    @Query("SELECT * FROM packages WHERE id = :id")
    abstract fun getByIdFlow(id: Int): Flow<Package?>

    @Query("SELECT * FROM packages WHERE id = :id")
    abstract fun getById(id: Int): Package?

    @Transaction
    @Query("SELECT * FROM packages WHERE id = :id")
    abstract fun getWithChildrenFlow(id: Int): Flow<PackageWithChildren?>

    @Transaction
    @Query("SELECT * FROM packages WHERE id = :id")
    abstract fun getWithChildrenById(id: Int): PackageWithChildren?

    @Transaction
    @Query("SELECT * FROM packages")
    abstract suspend fun getAllWithChildren(): List<PackageWithChildren>

    @Transaction
    @Query("SELECT * FROM packages WHERE id IN (:ids)")
    abstract fun getWithMeditationsByIdFlow(ids: List<String>): Flow<List<PackageWithMeditations>>

    @Transaction
    @Query("SELECT * FROM packages WHERE id = :id")
    abstract fun getWithMeditationsByIdFlow(id: String): Flow<PackageWithMeditations?>

    @Query("DELETE FROM packages")
    abstract suspend fun clearAll()

    @Query("DELETE FROM packages WHERE id NOT IN (:ids)")
    abstract suspend fun deleteAllExcept(ids: List<Int>)

    @Query("DELETE FROM packages WHERE id IN (:ids)")
    abstract suspend fun deleteIn(ids: List<Int>)

    @Query("SELECT * FROM packages WHERE id IN (:ids)")
    abstract fun getPackagesByIdsFlow(ids: List<String>): Flow<List<Package>>

    @Query("SELECT * FROM meditations WHERE related_package_ids LIKE '%,' || :packageId || ',%' OR related_package_ids LIKE '[' || :packageId || ',%' OR related_package_ids LIKE '%,' || :packageId || ']' OR related_package_ids LIKE '[' || :packageId || ']'")
    protected abstract fun getRelatedMeditations(packageId: Int): List<Meditation>

    @Query("SELECT * FROM exercises WHERE related_package_ids LIKE '%,' || :packageId || ',%' OR related_package_ids LIKE '[' || :packageId || ',%' OR related_package_ids LIKE '%,' || :packageId || ']' OR related_package_ids LIKE '[' || :packageId || ']'")
    protected abstract fun getRelatedExercises(packageId: Int): List<Exercise>

    @Query("SELECT * FROM articles WHERE related_package_ids LIKE '%,' || :packageId || ',%' OR related_package_ids LIKE '[' || :packageId || ',%' OR related_package_ids LIKE '%,' || :packageId || ']' OR related_package_ids LIKE '[' || :packageId || ']'")
    protected abstract fun getRelatedArticles(packageId: Int): List<Article>

    @Transaction
    open fun getWithRelatedChildrenById(id: Int): PackageWithChildren? {
        val pkg = getById(id) ?: return null
        return PackageWithChildren(
            pkg = pkg,
            meditations = getRelatedMeditations(id),
            exercises = getRelatedExercises(id),
            articles = getRelatedArticles(id)
        )
    }

    @Query("SELECT * FROM meditations WHERE related_package_ids LIKE '%,' || :packageId || ',%' OR related_package_ids LIKE '[' || :packageId || ',%' OR related_package_ids LIKE '%,' || :packageId || ']' OR related_package_ids LIKE '[' || :packageId || ']'")
    protected abstract fun getRelatedMeditationsFlow(packageId: Int): Flow<List<Meditation>>

    @Query("SELECT * FROM exercises WHERE related_package_ids LIKE '%,' || :packageId || ',%' OR related_package_ids LIKE '[' || :packageId || ',%' OR related_package_ids LIKE '%,' || :packageId || ']' OR related_package_ids LIKE '[' || :packageId || ']'")
    protected abstract fun getRelatedExercisesFlow(packageId: Int): Flow<List<Exercise>>

    @Query("SELECT * FROM articles WHERE related_package_ids LIKE '%,' || :packageId || ',%' OR related_package_ids LIKE '[' || :packageId || ',%' OR related_package_ids LIKE '%,' || :packageId || ']' OR related_package_ids LIKE '[' || :packageId || ']'")
    protected abstract fun getRelatedArticlesFlow(packageId: Int): Flow<List<Article>>

    open fun getWithRelatedChildrenFlow(id: Int): Flow<PackageWithChildren?> {
        val pkgFlow = getByIdFlow(id)
        val meditationsFlow = getRelatedMeditationsFlow(id)
        val exercisesFlow = getRelatedExercisesFlow(id)
        val articlesFlow = getRelatedArticlesFlow(id)

        return combine(pkgFlow, meditationsFlow, exercisesFlow, articlesFlow) { pkg, meditations, exercises, articles ->
            pkg?.let {
                PackageWithChildren(
                    pkg = it,
                    meditations = meditations,
                    exercises = exercises,
                    articles = articles
                )
            }
        }
    }
}