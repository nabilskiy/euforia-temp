package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.db.entity.AccompanimentWithItems
import digital.euforia.app.data.repository.AccompanimentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAccompanimentWithItemsUseCase @Inject constructor(
    private val accompanimentRepository: AccompanimentRepository
) {
    suspend operator fun invoke(id: Int): AccompanimentWithItems? {
        return withContext(Dispatchers.IO) {
            accompanimentRepository.getAccompanimentWithItemsById(id)
        }
    }
}