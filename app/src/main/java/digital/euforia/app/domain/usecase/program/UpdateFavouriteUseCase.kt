package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.repository.ArticleRepository
import digital.euforia.app.data.repository.ExerciseRepository
import digital.euforia.app.data.repository.FavouritesRepository
import digital.euforia.app.data.repository.MeditationRepository
import digital.euforia.app.ui.programs.publication.PublicationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateFavouriteUseCase @Inject constructor(
    private val meditationRepository: MeditationRepository,
    private val articleRepository: ArticleRepository,
    private val exerciseRepository: ExerciseRepository,
    private val repository: FavouritesRepository,
) {

    suspend operator fun invoke(
        type: PublicationType,
        id: Int,
        isFavourite: Boolean
    ) {
        withContext(Dispatchers.IO) {
            if (isFavourite) {
                repository.insert(id, type)
            } else {
                repository.deleteByIdAndType(id, type)
            }
        }
    }
}