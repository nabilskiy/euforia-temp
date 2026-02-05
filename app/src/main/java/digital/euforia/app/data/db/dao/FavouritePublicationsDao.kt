package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.FavouritePublication

@Dao
interface FavouritePublicationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favouritePublication: FavouritePublication)

    @Query("SELECT * FROM favourite_publications")
    suspend fun getAll(): List<FavouritePublication>
}