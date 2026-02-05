package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import digital.euforia.app.ui.programs.publication.PublicationType

@Entity(tableName = "favourite_publications")
data class FavouritePublication(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "publication_id") val publicationId: Int,
    @ColumnInfo(name = "publication_type") val publicationType: PublicationType,
)