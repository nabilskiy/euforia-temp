package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Article>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Article)

    @Query("SELECT * FROM articles WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Article?>

    @Query("SELECT * FROM articles WHERE main_package_id = :packageId")
    fun getByPackageIdFlow(packageId: Int): Flow<List<Article>>

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}