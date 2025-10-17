package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ArticleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ArticleEntity)

    @Query("SELECT * FROM articles WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<ArticleEntity?>

    @Query("SELECT * FROM articles WHERE main_package_id = :packageId")
    fun getByPackageIdFlow(packageId: Int): Flow<List<ArticleEntity>>

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}