package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.db.dao.FavouritePublicationsDao
import digital.euforia.app.data.db.entity.FavouritePublication
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFavouritePublicationsUseCase @Inject constructor(
    private val dao: FavouritePublicationsDao,
    private val publicationInfoMapper: PublicationInfoMapper,
//    private val publicationRepository: PublicationRep
) {

    suspend operator fun invoke(): List<FavouritePublication> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}

data class FavouritePublicationsResult(
    val meditations: List<FavouritePublication>,
    val exercises: List<FavouritePublication>,
    val articles: List<FavouritePublication>,
)