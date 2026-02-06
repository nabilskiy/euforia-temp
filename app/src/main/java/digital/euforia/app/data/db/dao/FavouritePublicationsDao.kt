package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.FavouritePublication
import digital.euforia.app.ui.programs.publication.PublicationType

@Dao
interface FavouritePublicationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favouritePublication: FavouritePublication)

    @Query("SELECT * FROM favourite_publications")
    suspend fun getAll(): List<FavouritePublication>

    @Query("SELECT * FROM favourite_publications WHERE publication_id = :publicationId AND publication_type = :publicationType LIMIT 1")
    suspend fun getByIdAndType(publicationId: Int, publicationType: PublicationType): FavouritePublication?

    @Delete
    suspend fun delete(favouritePublication: FavouritePublication)

    @Query("DELETE FROM favourite_publications WHERE publication_id = :publicationId AND publication_type = :publicationType")
    suspend fun deleteByIdAndType(publicationId: Int, publicationType: PublicationType)
}