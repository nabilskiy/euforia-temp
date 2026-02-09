package digital.euforia.app.data.repository

import digital.euforia.app.data.db.AppDatabase
import digital.euforia.app.data.db.dao.FavouritePublicationsDao
import digital.euforia.app.data.db.entity.FavouritePublication
import digital.euforia.app.ui.programs.publication.PublicationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesRepository @Inject constructor(
    private val database: AppDatabase,
) {

    private val favouritePublicationsDao = database.favouritePublicationsDao()

    suspend fun getAll() = favouritePublicationsDao.getAll()

    suspend fun getByIdAndType(publicationId: Int, publicationType: PublicationType) =
        favouritePublicationsDao.getByIdAndType(publicationId, publicationType)

    suspend fun observeByIdAndType(
        publicationId: Int,
        publicationType: PublicationType
    ): Flow<FavouritePublication?> {
        return withContext(Dispatchers.IO) {
            favouritePublicationsDao.observeByIdAndType(publicationId, publicationType)
        }
    }


    suspend fun insert(publicationId: Int, publicationType: PublicationType) {
        withContext(Dispatchers.IO) {
            favouritePublicationsDao.insert(
                FavouritePublication(
                    publicationId = publicationId,
                    publicationType = publicationType
                )
            )
        }
    }

    suspend fun deleteByIdAndType(publicationId: Int, publicationType: PublicationType) {
        favouritePublicationsDao.deleteByIdAndType(publicationId, publicationType)
    }
}