package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncAccompanimentsUseCase @Inject constructor(
    private val accompanimentRepository: AccompanimentRepository
) {
    suspend operator fun invoke(demo: Boolean) : ResultWrapper<Unit> {
        return withContext(Dispatchers.IO) {
            accompanimentRepository.syncAccompaniments(demo)
        }
    }
}