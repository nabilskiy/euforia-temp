package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.db.entity.AccompanimentItem
import digital.euforia.app.data.repository.AccompanimentItemRepository
import digital.euforia.app.data.repository.AccompanimentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateAccompanimentItemUseCase @Inject constructor(
    private val repository: AccompanimentItemRepository
) {
    suspend operator fun invoke(item: AccompanimentItem) {
        withContext(Dispatchers.IO) {
            repository.update(item)
        }
    }
}