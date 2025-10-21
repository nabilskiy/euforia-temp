package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.repository.AccompanimentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAccompanimentsFlowUseCase @Inject constructor(private val accompanimentRepository: AccompanimentRepository) {

    suspend operator fun invoke(isDemo: Boolean): Flow<List<Accompaniment>> {
        return withContext(Dispatchers.IO) {
            accompanimentRepository.getAllFlow(isDemo)
        }
    }
}