package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.FavouritePublicationsDao
import digital.euforia.app.data.db.entity.FavouritePublication
import digital.euforia.app.data.repository.FavouritesRepository
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.publication.PublicationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFavouritePublicationsUseCase @Inject constructor(
    private val repository: FavouritesRepository,
    private val api: EuforiaApi,
    private val publicationInfoMapper: PublicationInfoMapper,
) {

    suspend operator fun invoke(): ResultWrapper<FavouritePublicationsResult> {
        return withContext(Dispatchers.IO) {

            val favourites = repository.getAll()

            if (favourites.isEmpty()) {
                ResultWrapper.Success(FavouritePublicationsResult())
            } else {
                try {
                    val favouriteMeditations =
                        favourites.filter { it.publicationType == PublicationType.MEDITATION }
                    val favouriteExercises =
                        favourites.filter { it.publicationType == PublicationType.EXERCISE }
                    val favouriteArticles =
                        favourites.filter { it.publicationType == PublicationType.ARTICLE }

                    val meditationPublications = mutableListOf<PublicationInfo>()
                    val exercisePublications = mutableListOf<PublicationInfo>()
                    val articlePublications = mutableListOf<PublicationInfo>()

                    if (favouriteMeditations.isNotEmpty()) {
                        val ids =
                            favouriteMeditations.joinToString(",") { it.publicationId.toString() }
                        val networkMeditations = api.getMeditations(ids = ids).dataOrThrow
                        meditationPublications.addAll(networkMeditations.map {
                            publicationInfoMapper.fromNetworkMeditation(it)
                        })
                    }

                    if (favouriteExercises.isNotEmpty()) {
                        val ids =
                            favouriteExercises.joinToString(",") { it.publicationId.toString() }
                        val networkExercises =
                            api.getExercises(ids = ids).dataOrThrow
                        exercisePublications.addAll(networkExercises.map {
                            publicationInfoMapper.fromNetworkExercise(it)
                        })
                    }

                    if (favouriteArticles.isNotEmpty()) {
                        val ids =
                            favouriteArticles.joinToString(",") { it.publicationId.toString() }
                        val networkArticles =
                            api.getArticles(ids = ids).dataOrThrow
                        articlePublications.addAll(networkArticles.map {
                            publicationInfoMapper.fromNetworkArticle(it)
                        })
                    }

                    ResultWrapper.Success(
                        FavouritePublicationsResult(
                            meditations = meditationPublications,
                            exercises = exercisePublications,
                            articles = articlePublications
                        )
                    )
                } catch (e: Exception) {
                    ResultWrapper.Failure(e)
                }
            }
        }
    }
}

data class FavouritePublicationsResult(
    val meditations: List<PublicationInfo> = emptyList(),
    val exercises: List<PublicationInfo> = emptyList(),
    val articles: List<PublicationInfo> = emptyList(),
)