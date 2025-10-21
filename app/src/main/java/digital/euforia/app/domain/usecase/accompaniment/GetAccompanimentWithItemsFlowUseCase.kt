package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.db.entity.AccompanimentWithItems
import digital.euforia.app.data.repository.AccompanimentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAccompanimentWithItemsFlowUseCase @Inject constructor(
    private val accompanimentRepository: AccompanimentRepository
) {

    suspend operator fun invoke(): Flow<List<AccompanimentWithItems>> {
        return withContext(Dispatchers.IO) { accompanimentRepository.getAllWithItemsFlow() }
    }
}