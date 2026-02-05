package digital.euforia.app.data.db.converter

import androidx.room.TypeConverter
import digital.euforia.app.ui.programs.publication.PublicationType

class PublicationTypeConverter {

    @TypeConverter
    fun fromType(type: PublicationType): String = type.name

    @TypeConverter
    fun toType(value: String): PublicationType =
        PublicationType.valueOf(value)
}